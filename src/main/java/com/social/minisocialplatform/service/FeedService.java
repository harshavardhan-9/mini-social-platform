package com.social.minisocialplatform.service;

import java.util.*;
import org.springframework.jdbc.core.JdbcTemplate;

import java.util.concurrent.ConcurrentHashMap;

import com.social.minisocialplatform.cache.LRUCache;
import com.social.minisocialplatform.model.Post;

import com.social.minisocialplatform.messaging.PostCreatedEvent;
import com.social.minisocialplatform.messaging.PostEventPublisher;
import com.social.minisocialplatform.sharding.ShardRouter;
import org.springframework.stereotype.Service;

import com.social.minisocialplatform.ratelimiter.CircuitBreaker;

@Service
public class FeedService {
    LRUCache<String, List<Post>> feedCache;
    
    private Map<String, Object> requestLocks = new ConcurrentHashMap<>();
    private Map<String, Deque<Post>> pushFeedCache =
        new ConcurrentHashMap<>();
    
    private JdbcTemplate jdbcTemplate;
    private ShardRouter shardRouter;
    private PostEventPublisher postEventPublisher;
    private CircuitBreaker circuitBreaker = new CircuitBreaker();
    
    private static final int CELEBRITY_THRESHOLD = 1000;
    
    private static final double RECENCY_WEIGHT = 0.7;
    private static final double LIKE_WEIGHT = 0.3;

    public FeedService(JdbcTemplate jdbcTemplate, PostEventPublisher postEventPublisher) {
        this.jdbcTemplate = jdbcTemplate;
        this.postEventPublisher = postEventPublisher;
        this.shardRouter = new ShardRouter();
        feedCache = new LRUCache<>(5);
    }

    public List<Post> getFeed(String userId) {
        long start = System.currentTimeMillis();
        List<Post> feed = feedCache.get(userId);
        if(feed == null) {


            Object lock = requestLocks.computeIfAbsent(userId, k -> new Object());

            synchronized(lock) {

                feed = feedCache.get(userId);

                if(feed == null) {
                    System.out.println("Fetching from database for user: " + userId);

                    String shard = shardRouter.getShard(userId);
                    System.out.println("User " + userId + " is routed to shard: " + shard);

                    if (!circuitBreaker.allowRequest()) {
                        throw new RuntimeException( "Circuit breaker OPEN. Requests blocked.");
                    }
                    try{
                        if (userId.equals("999")) {
                            throw new RuntimeException("Simulated database failure");
                        }
                        List<Post> dbFeed = jdbcTemplate.query(
                            "SELECT user_id, content FROM posts WHERE user_id = ?",
                            (rs, rowNum) -> new Post(
                                rs.getInt("user_id"),
                                rs.getString("content"),
                                rs.getTimestamp("created_at"),
                                rs.getInt("like_count")
                            ),
                            Integer.parseInt(userId)
                        );
                        circuitBreaker.recordSuccess();
                        feed = dbFeed;
                        feedCache.put(userId, feed);
                    }
                    catch(Exception e) {
                        circuitBreaker.recordFailure();
                        throw new RuntimeException("DB failure simulated. Circuit breaker state: " + circuitBreaker.getState());    
                    }
                }
            }
        }
        long end = System.currentTimeMillis();
        System.out.println("Feed fetch time: " + (end - start) + " ms");
        return feed;
    }

    public void addPost(String username, String content, String traceId) {

        Integer userId = jdbcTemplate.queryForObject(
            "SELECT id FROM users WHERE LOWER(username) = LOWER(?)",
            Integer.class,
            username
        );

        String shard = shardRouter.getShard(String.valueOf(userId));
        System.out.println("Adding post for user " + userId + " to shard: " + shard);

        jdbcTemplate.update(
            "INSERT INTO posts(user_id, content) VALUES (?, ?)",
            userId,
            content
        );

        // String traceId =UUID.randomUUID().toString();

        PostCreatedEvent event = new PostCreatedEvent(UUID.randomUUID().toString(), String.valueOf(userId), content, traceId);
        postEventPublisher.publishPostCreatedEvent(event);
        //invalidate cache
        feedCache.invalidate(String.valueOf(userId));
    }

    public void deletePost(int postId) {
        jdbcTemplate.update("DELETE FROM posts WHERE id = ?", postId);
        System.out.println("Deleted post with ID: " + postId);
    } 

    public List<Post> getPullFeed(String userId) {
        long start = System.currentTimeMillis();

        List<Integer> followees = jdbcTemplate.query(
            "SELECT followee_id FROM follows WHERE follower_id = ?",
            (rs, rowNum) -> rs.getInt("followee_id"),
            Integer.parseInt(userId)
        );

        if(followees.isEmpty()) {
            return new ArrayList<>();
        }

        String inSql = String.join(",", Collections.nCopies(followees.size(), "?"));

        String sql =
        "SELECT user_id, content, created_at, like_count " +
        "FROM posts " +
        "WHERE user_id IN (" + inSql + ") " +
        "ORDER BY created_at DESC " +
        "LIMIT 20";

        List<Post> feed = jdbcTemplate.query(sql, (rs, rowNum) -> new Post(
            rs.getInt("user_id"),
            rs.getString("content"),
            rs.getTimestamp("created_at"),
            rs.getInt("like_count")
        ), followees.toArray());

        long end = System.currentTimeMillis();
        System.out.println("Pull feed fetch latency: " + (end - start) + " ms");
        return feed;
    }

    public void pushPostToFollowers(String authorId, Post post) {
        List<Integer> followers = jdbcTemplate.query(
            "SELECT follower_id FROM follows WHERE followee_id = ?",
            (rs, rowNum) -> rs.getInt("follower_id"),
            Integer.parseInt(authorId)
        );

        for(Integer followerId : followers) {
            pushFeedCache.computeIfAbsent(String.valueOf(followerId), k -> new LinkedList<>());
            pushFeedCache.get(String.valueOf(followerId)).addFirst(post);
        }
    }

    public List<Post> getPushFeed(String userId) {
        return new ArrayList<>(pushFeedCache.getOrDefault(userId, new LinkedList<>()));
    }

    public boolean isCelebrityUser(String userId) {

        Integer followerCount = jdbcTemplate.queryForObject(
            "SELECT COUNT(*) FROM follows WHERE followee_id = ?",
            Integer.class,
            Integer.parseInt(userId)
        );

        return followerCount > CELEBRITY_THRESHOLD;
    }

    public List<Post> getHybridFeed(String userId) {
        long start = System.currentTimeMillis();

        List<Post> finalFeed = new ArrayList<>();

        List<Integer> followees = jdbcTemplate.query(
            "SELECT followee_id FROM follows WHERE follower_id = ?",
            (rs, rowNum) -> rs.getInt("followee_id"),
            Integer.parseInt(userId)
        );

        for(Integer followeeId : followees) {
            String followee = String.valueOf(followeeId);
            if(isCelebrityUser(followee)) {

                List<Post> celebPosts = jdbcTemplate.query(
                    "SELECT user_id, content, created_at, like_count FROM posts WHERE user_id = ? ORDER BY created_at DESC LIMIT 5",
                    (rs, rowNum) -> new Post(
                        rs.getInt("user_id"),
                        rs.getString("content"),
                        rs.getTimestamp("created_at"),
                        rs.getInt("like_count")
                    ),
                    followeeId
                );
                finalFeed.addAll(celebPosts);
            }
            else{
                finalFeed.addAll(getPushFeed(userId));
            }
        }
        finalFeed.sort((a, b) -> Double.compare(calculateScore(b), calculateScore(a)));
        for(Post post : finalFeed) {
            System.out.println(
                post.getContent()
                + " score = "
                + calculateScore(post)
            );
        }
        
        if(finalFeed.size() > 20) {
            finalFeed = finalFeed.subList(0, 20);
        }
        
        long end = System.currentTimeMillis();
        System.out.println("Hybrid feed fetch latency: " + (end - start) + " ms");
        
        return finalFeed;
    }

    private double calculateScore(Post post) {
        long ageInMinutes = (System.currentTimeMillis() - post.getCreatedAt().getTime());
        double recencyScore = 1.0 / (1 + ageInMinutes / 60000.0);
        double likeScore = post.getLikeCount();

        return (RECENCY_WEIGHT * recencyScore) + (LIKE_WEIGHT * likeScore);
    }
}
