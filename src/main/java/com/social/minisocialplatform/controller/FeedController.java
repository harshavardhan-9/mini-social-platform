package com.social.minisocialplatform.controller;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import com.social.minisocialplatform.model.Post;
import com.social.minisocialplatform.service.FeedService;
import java.util.List;

@RestController
@RequestMapping("/api")

public class FeedController {
    // private FeedService feedService = new FeedService();

    @Autowired
    private FeedService feedService;

    @GetMapping("/feed/{userId}")
    public List<Post> getFeed(@PathVariable String userId) {
        return feedService.getFeed(userId);
    }

    @PostMapping("/post")
    public String addPost(@RequestBody Post post) {
        feedService.addPost(String.valueOf(post.getUserId()), post.getContent());
        return "Post added successfully";
    }
}

