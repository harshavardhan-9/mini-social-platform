package com.social.minisocialplatform.model;

public class Post {
    private String userId;
    private String content;

    public Post(String userId, String content) {
        this.userId = userId;
        this.content = content;
    }

    public Post(){

    }
    public String getUserId() {
        return userId;
    }

    public String getContent() {
        return content;
    }
}