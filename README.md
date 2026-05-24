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
* Consistent hashing with virtual nodes
* Shard routing simulation
* Leader-follower replication simulation
* Eventual consistency lag demonstration

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

```text
src/main/java/com/social/minisocialplatform
├── benchmark
├── cache
├── controller
├── messaging
├── model
├── replication
├── service
├── sharding
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

## Auth APIs

### Signup

```http
POST /api/auth/signup
```

Body:

```json
{
  "username":"harsha",
  "password":"123456"
}
```

---

### Login

```http
POST /api/auth/login
```

Body:

```json
{
  "username":"harsha",
  "password":"123456"
}
```

Response:

```json
{
  "accessToken":"...",
  "refreshToken":"..."
}
```

# Metrics Endpoint

```http
GET /metrics
```

Example Response:

```json
{
  "totalRequests": 12,
  "averageLatencyMs": 45
}
```

Structured request logs are generated for every request.

Example:

```json
{
  "requestId":"abc123",
  "method":"POST",
  "path":"/api/post",
  "status":200,
  "latencyMs":45
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

# RabbitMQ Setup

RabbitMQ is used as the message broker for asynchronous event processing.

Download and run RabbitMQ:

```bash id="x3c6s8"
https://www.rabbitmq.com/download.html
```

Default configuration used:

```properties
spring.rabbitmq.host=localhost
spring.rabbitmq.port=5672
spring.rabbitmq.username=guest
spring.rabbitmq.password=guest
```

## RabbitMQ dashboard:
```bash id="x3c6s8"
http://localhost:15672
```

## Default credentials:
```bash id="x3c6s8"
username: guest
password: guest
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

* only one request fetches from database
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

The project replaces flat-file storage with PostgreSQL.

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



---

# Backpressure Handling
Implemented backpressure handling using bounded concurrent processing and load shedding.

Simulation:
- slow consumer simulated using Thread.sleep(5000)
- pending event counter tracks worker load
- if pending events exceed threshold, events are dropped

Example:
```text
Current pending events in feed worker: 6
Backpressure triggered. Dropping event
```

This prevents worker overload during traffic spikes.
---

# Distributed Tracing

Request IDs are propagated through asynchronous RabbitMQ workers.

Flow:

HTTP Request
↓
RequestLoggingFilter generates requestId
↓
Trace ID attached to event
↓
FeedFanoutWorker receives trace ID
↓
NotificationWorker receives trace ID

This enables end-to-end tracing across async services.

---

## Security and Observability Features

- JWT authentication
- Refresh token flow
- JWT verification middleware
- RBAC authorization
- Admin-only endpoints
- Token bucket rate limiter
- Circuit breaker pattern
- Structured JSON request logs
- Metrics endpoint
- Distributed trace propagation
- Request IDs and trace IDs
- RabbitMQ async event tracing

---
# Implemented Concepts

* Cache-aside pattern
* LRU eviction
* LFU eviction
* Cache invalidation
* Request coalescing
* Concurrent load handling
* Cache stampede protection
* PostgreSQL integration
* Database indexing
* Query optimization
* Consistent hashing
* Virtual nodes
* Shard routing
* Distributed storage simulation
* Leader-follower replication
* Eventual consistency simulation
* RabbitMQ messaging
* At-least-once delivery
* Idempotent consumers
* Backpressure handling
* Load shedding
* Dead Letter Queue (DLQ)
* Asynchronous processing

---

# Documentation

- docs/07-caching.md - Adding Caching to Instagram Feed
- docs/08-db-choice.md - Database choice and ER design
- docs/08-ecommerce-db.md - E-commerce database design
- docs/optimized-queries.md - Optimized SQL queries
- docs/explain-before.txt - Query plan before indexes
- docs/explain-after.txt - Query plan after indexes
- docs/09-sharding-decisions.md - Sharding key trade-offs and decisions
- docs/09-user-storage.md - Scalable user storage design for 100M users
- docs/10-order-processing.md - Scalable order processing system design
