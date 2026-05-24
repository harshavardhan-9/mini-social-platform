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

    private JdbcTemplate jdbcTemplate;
    private ShardRouter shardRouter;
    private PostEventPublisher postEventPublisher;
    private CircuitBreaker circuitBreaker = new CircuitBreaker();

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
                                rs.getString("content")
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

    public void addPost(String userId, String content) {

        String shard = shardRouter.getShard(userId);
        System.out.println("Adding post for user " + userId + " to shard: " + shard);

        jdbcTemplate.update(
            "INSERT INTO posts(user_id, content) VALUES (?, ?)",
            Integer.parseInt(userId),
            content
        );

        PostCreatedEvent event = new PostCreatedEvent(UUID.randomUUID().toString(), userId, content);
        postEventPublisher.publishPostCreatedEvent(event);
        //invalidate cache
        feedCache.invalidate(userId);
    }

    public void deletePost(int postId) {
        jdbcTemplate.update("DELETE FROM posts WHERE id = ?", postId);
        System.out.println("Deleted post with ID: " + postId);
    } 
}
