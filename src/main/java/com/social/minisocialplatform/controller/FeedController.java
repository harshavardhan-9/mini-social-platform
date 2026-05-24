package com.social.minisocialplatform.controller;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import com.social.minisocialplatform.model.Post;
import com.social.minisocialplatform.service.FeedService;
import java.util.List;

import jakarta.servlet.http.HttpServletRequest;

import com.social.minisocialplatform.ratelimiter.RateLimiterService;

@RestController
@RequestMapping("/api")

public class FeedController {
    // private FeedService feedService = new FeedService();

    @Autowired
    private FeedService feedService;

    @Autowired
    private RateLimiterService rateLimiterService;

    @GetMapping("/feed/pull/{userId}")
    public List<Post> getPullFeed(@PathVariable String userId) {
        return feedService.getPullFeed(userId);
    }

    @GetMapping("/feed/push/{userId}")
    public List<Post> getPushFeed(@PathVariable String userId) {
        return feedService.getPushFeed(userId);
    }

    @GetMapping("/feed/hybrid/{userId}")
    public List<Post> getHybridFeed(@PathVariable String userId) {
        return feedService.getHybridFeed(userId);
    }

    @PostMapping("/post")
    public String addPost(@RequestBody Post post, HttpServletRequest request) {
        String username = (String) request.getAttribute("username");
        if(!rateLimiterService.allowRequest(username)) {
            return "Rate limit exceeded. Please try again later.";
        }
        String traceId = (String) request.getAttribute("requestId");
        feedService.addPost(username, post.getContent(), traceId);
        return "Post added successfully";
    }

    @DeleteMapping("/post/{postId}")
    public String deletePost(@PathVariable int postId, HttpServletRequest request) {
        String role = (String) request.getAttribute("role");
        if (!"ADMIN".equals(role)) {
            return "Unauthorized";
        }
        feedService.deletePost(postId);
        return "Post deleted successfully";
    }
}

