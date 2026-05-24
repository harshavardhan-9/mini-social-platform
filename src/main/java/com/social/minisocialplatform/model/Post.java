package com.social.minisocialplatform.model;

import java.sql.Timestamp;

    public class Post {
        private int userId;
        private String content;
        private Timestamp createdAt;
        
        public Post(int userId, String content) {
            this.userId = userId;
            this.content = content;
        }


        public Post(int userId, String content, Timestamp createdAt) {
            this.userId = userId;
            this.content = content;
            this.createdAt = createdAt;
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
    }