package com.social.minisocialplatform.service;

import java.util.*;
import org.springframework.jdbc.core.JdbcTemplate;

import java.util.concurrent.ConcurrentHashMap;

import com.social.minisocialplatform.cache.LRUCache;
import com.social.minisocialplatform.model.Post;

import org.springframework.stereotype.Service;

@Service
public class FeedService {
    LRUCache<String, List<Post>> feedCache;
    
    private Map<String, Object> requestLocks = new ConcurrentHashMap<>();

    private JdbcTemplate jdbcTemplate;

    public FeedService(JdbcTemplate jdbcTemplate) {
        this.jdbcTemplate = jdbcTemplate;
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
                    List<Post> dbFeed = jdbcTemplate.query(
                        "SELECT user_id, content FROM posts WHERE user_id = ?",
                        (rs, rowNum) -> new Post(
                            rs.getInt("user_id"),
                            rs.getString("content")
                        ),
                        Integer.parseInt(userId)
                    );

                    feed = dbFeed;
                    feedCache.put(userId, feed);
                }
            }
        }
        long end = System.currentTimeMillis();
        System.out.println("Feed fetch time: " + (end - start) + " ms");
        return feed;
    }

    public void addPost(String userId, String content) {
        jdbcTemplate.update(
            "INSERT INTO posts(user_id, content) VALUES (?, ?)",
            Integer.parseInt(userId),
            content
        );
        //invalidate cache
        feedCache.invalidate(userId);
    }
}
