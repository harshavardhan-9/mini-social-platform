    package com.social.minisocialplatform.model;

    public class Post {
        private int userId;
        private String content;

        public Post(int userId, String content) {
            this.userId = userId;
            this.content = content;
        }

        public Post(){

        }
        public int getUserId() {
            return userId;
        }

        public String getContent() {
            return content;
        }
    }