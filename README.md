# Mini Social Platform

A Spring Boot backend project implementing custom caching systems and feed optimization techniques.

## Features

* LRU Cache implementation from scratch
* LFU Cache implementation from scratch
* Cache-aside feed caching
* Cache invalidation on new posts
* Request coalescing for cache stampede protection
* Concurrent load testing
* LRU vs LFU benchmark
* Feed API using PostgreSQL
* Indexed database queries
* PostgreSQL migration from flat-file storage

---

# Tech Stack

* Java 21
* Spring Boot
* Maven
* JUnit
* PostgreSQL
* Spring JDBC
* Spring Data JPA

---

# Project Structure

```text id="8rx2o5"
src/main/java/com/social/minisocialplatform
├── cache
├── controller
├── service
├── model
├── benchmark
```

---

# APIs

## Get Feed

```http id="o7e0qv"
GET /api/feed/{userId}
```

Example:

```http id="xzyt63"
GET http://localhost:8080/api/feed/1
```

---

## Add Post

```http id="qiv1k6"
POST /api/post
```

Request Body:

```json id="bzfztl"
{
  "userId":1,
  "content":"Hello World"
}
```

---

# Running the Application

Start Spring Boot server:

```bash id="x3c6s8"
.\mvnw.cmd spring-boot:run
```

Application runs on:

```text id="rq2hbo"
http://localhost:8080
```

---

# Running Benchmark

Runs LRU vs LFU benchmark with an 80/20 skewed access pattern.

```bash id="rlu5z8"
.\mvnw.cmd exec:java "-Dexec.mainClass=com.social.minisocialplatform.benchmark.CacheBenchmark"
```

---

# Running Request Coalescing Load Test

Simulates 100 concurrent requests hitting the same cold cache key.

```bash id="1mxyjr"
.\mvnw.cmd exec:java "-Dexec.mainClass=com.social.minisocialplatform.benchmark.CoalescingLoadTest"
```

Observed behavior:

* only one request fetchea from database
* remaining requests wait and reuse cached data

---

# Cache Latency Comparison

| Scenario                   | Latency |
| -------------------------- | ------- |
| Cache Miss (First Request) | 191 ms  |
| Cache Hit (Second Request) | 10 ms   |

### Cache Miss (First Request)
![alt text](image.png) 

### Cache Hit (Second Request)
![alt text](image-1.png)

The first request fetches data from PostgreSQL and populates the cache.
Subsequent requests are served directly from the in-memory LRU cache.

---

# PostgreSQL Migration

Assignment 7 replaces flat-file storage with PostgreSQL.

Implemented:
- relational schema
- indexed queries
- JDBC integration
- optimized feed queries

Database tables:
- users
- posts
- follows
- likes

---

# Database Indexing

Added indexes:

```sql
CREATE INDEX idx_posts_user_id ON posts(user_id);
CREATE INDEX idx_posts_created_at ON posts(created_at);
CREATE INDEX idx_follows_follower_id ON follows(follower_id);
```

---

# Query Optimization

## Before Indexing

Query used sequential scan.

![alt text](before-index.png)

---

## After Indexing

Query used bitmap index scan.

![alt text](after-index.png)

---

# Implemented Concepts

* Cache-aside pattern
* LRU eviction
* LFU eviction
* Cache invalidation
* Request coalescing
* Concurrent load handling
* Cache stampede protection

---

# Documentation

docs/07-caching.md - Adding Caching to Instagram Feed

docs/08-db-choice.md - Database choice and ER design

docs/08-ecommerce-db.md - E-commerce database design

docs/optimized-queries.md - Optimized SQL queries

docs/explain-before.txt - Query plan before indexes

docs/explain-after.txt - Query plan after indexes