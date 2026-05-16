package com.social.minisocialplatform.service;

import java.io.*;
import java.util.*;

import com.social.minisocialplatform.cache.LRUCache;
import com.social.minisocialplatform.model.Post;

public class FeedService {
    LRUCache<String, List<Post>> feedCache;
    
    private Map<String, Object> requestLocks = new HashMap<>();

    public FeedService() {
        feedCache = new LRUCache<>(5);
    }

    public List<Post> getFeed(String userId) {
        List<Post> feed = feedCache.get(userId);
        if(feed == null) {
            //fetch from DB
            Object lock = requestLocks.computeIfAbsent(userId, k -> new Object());
            
            synchronized(lock){
                feed = feedCache.get(userId);
                if(feed == null) {
                    List<Post> dbFeed =new ArrayList<>(); //simulate DB fetch
                    try {
                        System.out.println("Reading file for user: " + userId);
                        BufferedReader reader = new BufferedReader(
                        new FileReader("src/main/resources/post.txt") );
                        
                        String line;
                        while((line = reader.readLine()) != null) {
                            String[] parts = line.split(",");
                            if(parts[0].equals(userId)) {
                                Post post = new Post(parts[0], parts[1]);
                                dbFeed.add(post);
                            }
                        }
                        reader.close();
                    }
                    catch(IOException e) {
                        e.printStackTrace();
                    }
                    feed = dbFeed;
                    feedCache.put(userId, feed);
                }
            }
        }
        return feed;
    }

    public void addPost(String userId, String content) {
        Post newPost = new Post(userId, content);
        //save to DB-Flat file
        try {
            BufferedWriter writer = new BufferedWriter(
                new FileWriter("src/main/resources/post.txt", true) );
            writer.write(userId + "," + content);
            writer.newLine();
            writer.close();
        }
        catch(IOException e) {
            e.printStackTrace();
        }
        //invalidate cache
        feedCache.invalidate(userId);
    }
}
