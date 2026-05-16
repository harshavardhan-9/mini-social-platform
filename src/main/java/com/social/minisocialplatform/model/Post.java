package com.social.minisocialplatform.model;

public class Post {
    String userId;
    String content;

    public Post(String userId, String content) {
        this.userId = userId;
        this.content = content;
    }

    public String getUserId() {
        return userId;
    }

    public String getContent() {
        return content;
    }
}