package com.social.minisocialplatform.model;

import java.sql.Timestamp;

    public class Post {
        private int userId;
        private String content;
        private Timestamp createdAt;
        private int likeCount;
        
        public Post(int userId, String content) {
            this.userId = userId;
            this.content = content;
            this.likeCount = 0;
        }

        public Post(int userId, String content, Timestamp createdAt, int likeCount) {
            this.userId = userId;
            this.content = content;
            this.createdAt = createdAt;
            this.likeCount = likeCount;
        }

        public Post(){

        }
        public int getUserId() {
            return userId;
        }

        public String getContent() {
            return content;
        }

        public Timestamp getCreatedAt() {
            return createdAt;
        }

        public int getLikeCount() {
            return likeCount;
        }
    }