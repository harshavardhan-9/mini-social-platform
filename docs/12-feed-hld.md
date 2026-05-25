# Designing the Twitter/Instagram Feed

## 1. Introduction

This project implements a distributed feed system inspired by architectures used in platforms such as Twitter and Instagram. The system combines pull-based feed generation, push-based fanout, and a hybrid celebrity-aware strategy to optimize both read and write performance.

The architecture also integrates:
- JWT authentication
- Rate limiting
- RabbitMQ asynchronous processing
- Feed ranking
- Cache-backed delivery
- Distributed tracing
- Circuit breakers
- Dead Letter Queues (DLQ)
- Database sharding simulation
- Observability and metrics

The goal of this project is to demonstrate how modern distributed systems handle feed generation, scalability bottlenecks, reliability concerns, and large-scale event-driven communication.

---

# 2. Functional Requirements

The system supports the following functional requirements:

- Users can create posts
- Users can follow other users
- Users can retrieve personalized feeds
- Feed generation supports pull, push, and hybrid models
- Celebrity users use pull-based fanout
- Regular users use push-based fanout
- Feeds support ranking based on recency and engagement
- JWT authentication secures API access
- Role-based authorization protects admin operations
- Rate limiting prevents abuse
- Notifications are asynchronously processed
- Failed queue events are routed to a Dead Letter Queue
- Feed requests expose metrics and tracing information

---

# 3. Non-Functional Requirements

| Requirement | Description |
|---|---|
| Scalability | Support large follower graphs and growing feed volume |
| Low Latency | Feed retrieval should remain fast under load |
| High Availability | Failures should not fully stop the system |
| Fault Tolerance | Queue failures and worker crashes must be recoverable |
| Observability | Request tracing and metrics should be available |
| Consistency | Feed ordering should remain predictable |
| Extensibility | Feed ranking and fanout logic should be configurable |
| Security | APIs must require authentication and authorization |

---

# 4. Core Entities

## User
Represents a registered platform user.

Attributes:
- id
- username
- password
- role

---

## Post
Represents content created by a user.

Attributes:
- userId
- content
- createdAt
- likeCount

---

## Follow
Represents follower-followee relationships.

Attributes:
- followerId
- followeeId

---

## PostCreatedEvent
Represents asynchronous events published into RabbitMQ after post creation.

Attributes:
- eventId
- userId
- content
- traceId

---

# 5. Feed Generation Models

## Pull Model (Fanout-on-Read)

In the pull model, feed generation happens during read requests. When a user requests a feed, the system queries posts from all followed users and merges them dynamically.

### Advantages
- Lower write amplification
- Reduced storage overhead
- Simpler cache management

### Disadvantages
- Higher read latency
- Expensive queries for users following many accounts
- Increased database load during feed reads

---

## Push Model (Fanout-on-Write)

In the push model, posts are precomputed into follower feed caches during post creation.

### Advantages
- Extremely fast feed reads
- Lower read-time computation
- Better read scalability

### Disadvantages
- High write amplification
- Large fanout cost for users with many followers
- Increased cache storage usage

---

## Hybrid Model

The hybrid architecture combines both strategies:
- normal users use push fanout
- celebrity users use pull fanout

This prevents large-scale fanout explosions while preserving low latency for most users.

The celebrity threshold is configurable using follower count limits.

---

# 6. High Level Architecture Diagram

```text

                                ┌───────────────────────┐
                                │       Clients         │
                                │ Web / Mobile / API    │
                                └──────────┬────────────┘
                                           │
                                           ▼
                         ┌────────────────────────────────┐
                         │         API Gateway            │
                         │ Routing • Load Balancing       │
                         │ Request Validation             │
                         └──────────────┬─────────────────┘
                                        │
                                        ▼
                         ┌────────────────────────────────┐
                         │      Authentication Layer      │
                         │ JWT Validation • RBAC          │
                         └──────────────┬─────────────────┘
                                        │
                                        ▼
                         ┌────────────────────────────────┐
                         │        Rate Limiter            │
                         │ Token Bucket Protection        │
                         └──────────────┬─────────────────┘
                                        │
                                        ▼
                  ┌────────────────────────────────────────────┐
                  │              App Servers                   │
                  │--------------------------------------------│
                  │ FeedController                             │
                  │ FeedService                                │
                  │ Hybrid Feed Router                         │
                  │ Ranking Engine                             │
                  │ Circuit Breaker                            │
                  │ Observability + Metrics                    │
                  └──────────────┬─────────────────────────────┘
                                 │
          ┌──────────────────────┼───────────────────────────┐
          │                      │                           │
          ▼                      ▼                           ▼

┌──────────────────┐  ┌──────────────────────┐   ┌─────────────────────┐
│ Push Feed Cache  │  │      RabbitMQ        │   │   Pull Feed Query   │
│------------------│  │----------------------│   │---------------------│
│ Precomputed Feed │  │ PostCreatedEvent     │   │ Dynamic Feed Merge  │
│ Timeline Cache   │  │ Async Fanout Queue   │   │ Ranking + Sorting   │
└────────┬─────────┘  └──────────┬───────────┘   └──────────┬──────────┘
         │                       │                          │
         │                       ▼                          │
         │        ┌──────────────────────────────┐          │
         │        │      Feed Fanout Worker      │          │
         │        │------------------------------│          │
         │        │ Push Fanout                  │          │
         │        │ Backpressure Handling        │          │
         │        │ DLQ Routing                  │          │
         │        │ Idempotency Validation       │          │
         │        └─────────────┬────────────────┘          │
         │                      │                           │
         │                      ▼                           │
         │        ┌──────────────────────────────┐          │
         │        │     Notification Worker      │          │
         │        │------------------------------│          │
         │        │ Async Notification Delivery  │          │
         │        └──────────────────────────────┘          │
         │                                                  │
         └──────────────────────────┬───────────────────────┘
                                    │
                                    ▼

                 ┌──────────────────────────────────────┐
                 │        Database Shard Router         │
                 │--------------------------------------│
                 │ userId Based Partitioning            │
                 └────────────────┬─────────────────────┘
                                  │
          ┌───────────────────────┼────────────────────────┐
          │                       │                        │
          ▼                       ▼                        ▼

┌───────────────────┐  ┌───────────────────┐  ┌───────────────────┐
│   PostgreSQL      │  │   PostgreSQL      │  │   PostgreSQL      │
│     Shard 1       │  │     Shard 2       │  │     Shard 3       │
│-------------------│  │-------------------│  │-------------------│
│ Users             │  │ Users             │  │ Users             │
│ Posts             │  │ Posts             │  │ Posts             │
│ Follows           │  │ Follows           │  │ Follows           │
└─────────┬─────────┘  └─────────┬─────────┘  └─────────┬─────────┘
          │                      │                      │
          ▼                      ▼                      ▼

┌───────────────────┐  ┌───────────────────┐  ┌───────────────────┐
│ Read Replica 1    │  │ Read Replica 2    │  │ Read Replica 3    │
│-------------------│  │-------------------│  │-------------------│
│ Feed Reads        │  │ Feed Reads        │  │ Feed Reads        │
│ Analytics Queries │  │ Analytics Queries │  │ Analytics Queries │
└───────────────────┘  └───────────────────┘  └───────────────────┘


                    ┌─────────────────────────────┐
                    │      Observability Layer    │
                    │-----------------------------│
                    │ Metrics Endpoint            │
                    │ Structured Logs             │
                    │ Trace IDs                   │
                    │ Latency Monitoring          │
                    └─────────────────────────────┘

```

---

# 7. System Components

## API Gateway

Acts as the entry point for all client requests. In production systems, the gateway handles:
- request routing
- load balancing
- authentication forwarding
- traffic filtering

---

## Authentication Layer

JWT-based authentication validates user identity for every request.

The authentication filter:
- validates JWT tokens
- extracts username and role
- propagates user identity into the request context

---

## Rate Limiter

A token bucket rate limiter protects APIs from abuse and excessive traffic.

The limiter:
- tracks request frequency per user
- blocks excessive requests
- prevents queue overload

---

## Feed Controller

Handles:
- post creation requests
- feed retrieval requests
- hybrid feed routing

The controller delegates business logic to the FeedService layer.

---

## Feed Service

Core business layer responsible for:
- pull feed generation
- push feed retrieval
- hybrid feed routing
- ranking
- cache interaction
- shard routing

---

## RabbitMQ

RabbitMQ acts as the asynchronous event broker.

Responsibilities:
- decouple write path from fanout
- enable async processing
- improve system responsiveness
- support retries and DLQ handling

---

## Feed Fanout Worker

Consumes post creation events and:
- performs push fanout
- updates follower feed caches
- handles backpressure
- routes failed events into DLQ

---

## Notification Worker

Processes asynchronous notification delivery independently from feed generation.

---

## Cache Layer

The push feed cache stores precomputed feeds for regular users.

This reduces:
- database reads
- feed merge computation
- request latency

---

## PostgreSQL Database

Stores:
- users
- posts
- follows
- engagement metadata

---

## Sharding Layer

Shard routing distributes users across simulated database shards using userId partitioning.

This improves:
- horizontal scalability
- write distribution
- hotspot mitigation

---

## Database Replicas

In production systems, read replicas handle feed reads while primary nodes handle writes.

Replication:
- improves read scalability
- reduces write contention
- isolates heavy read traffic

---

# 8. POST /post Request Lifecycle

1. User authenticates using JWT access token.
2. API Gateway forwards request to the application server.
3. JWT filter validates token and extracts username and role.
4. Rate limiter validates request frequency.
5. FeedService stores the post in PostgreSQL.
6. A PostCreatedEvent is published to RabbitMQ.
7. FeedFanoutWorker asynchronously consumes the event.
8. NotificationWorker processes notification delivery.
9. Feed worker updates push-feed caches for followers.
10. Failed events are routed into the Dead Letter Queue (DLQ).
11. Trace IDs are propagated across services for observability.

---

# 9. GET /feed Request Lifecycle

## Pull Feed

1. Fetch followees from follows table.
2. Query posts from followed users.
3. Merge posts ordered by timestamp.
4. Apply ranking logic.
5. Return top feed entries.

---

## Push Feed

1. Feed is precomputed during post creation.
2. Feed worker writes posts into follower feed caches.
3. GET request directly reads from cache.
4. Ranked posts are returned immediately.

---

## Hybrid Feed

1. Normal users use push fanout.
2. Celebrity users use pull fanout.
3. FeedService dynamically decides strategy based on follower count threshold.
4. Feed ranking is applied before response delivery.

---

# 10. Caching, Queueing, Sharding, and Replication

## Caching

The push feed cache stores precomputed feeds for regular users. This reduces database reads and significantly improves feed retrieval latency.

The cache is updated asynchronously through RabbitMQ workers during post fanout.

---

## Queueing

RabbitMQ decouples feed generation from API request processing.

When a user creates a post:
1. The API stores the post in PostgreSQL
2. A PostCreatedEvent is published into RabbitMQ
3. Workers asynchronously process fanout and notifications

This prevents slow fanout operations from blocking user requests.

---

## Sharding

User traffic is distributed across simulated database shards using shard routing logic.

Sharding helps:
- distribute write load
- reduce hotspotting
- improve horizontal scalability

The feed service routes users to shards using userId-based partitioning.

---

## Replication

In production systems, read replicas are typically used for feed reads while primary databases handle writes.

This architecture assumes:
- primary nodes process writes
- replicas handle heavy feed read traffic

Replication improves read scalability and reduces database contention.

---

# 11. Feed Ranking

The system supports ranked feeds instead of pure reverse chronological ordering.

Ranking score:

```text
score = recency_weight + like_count_weight
```

The implementation includes:
- configurable ranking weights
- recency scoring
- engagement scoring
- top-20 ranked feed generation

This simulates real-world feed ranking systems used by social media platforms.

---

# 12. Failure Modes and Mitigations

| Failure | Mitigation |
|---|---|
| Cache miss storm | Cache precomputation + cache invalidation |
| Queue overload | Backpressure protection |
| Worker failures | Dead Letter Queue |
| Duplicate event delivery | Idempotency store |
| Database failures | Circuit breaker |
| Hot celebrity users | Hybrid feed model |
| Shard hot key | Sharding distribution |
| Slow downstream services | Async processing |

---

# 13. Tradeoffs

## Push Fanout

### Advantages
- lower read latency
- faster feed retrieval
- reduced DB reads

### Disadvantages
- higher storage usage
- high write amplification
- expensive celebrity fanout

---

## Pull Fanout

### Advantages
- lower storage cost
- simpler write path
- reduced fanout overhead

### Disadvantages
- slower reads
- expensive feed queries
- higher read-time computation

---

## Hybrid Model

### Advantages
- balanced scalability
- optimized celebrity handling
- lower infrastructure overhead

### Disadvantages
- increased operational complexity
- more difficult feed merging
- harder cache consistency management

---

## Ranking

### Advantages
- improves user engagement
- prioritizes relevant content
- enables personalization

### Disadvantages
- increases computational cost
- complicates caching
- requires additional scoring logic

---

# 14. Observability

The system includes:
- request tracing
- trace ID propagation
- metrics collection
- latency monitoring
- structured logging
- queue event visibility

### Observability Features
- /metrics endpoint
- distributed trace logs
- RabbitMQ worker logs
- latency benchmarking
- structured JSON request logs

---

# 15. Conclusion

This project demonstrates a production-inspired social media feed architecture using modern distributed systems concepts.

The implementation combines:
- asynchronous event-driven processing
- scalable feed generation
- caching strategies
- distributed systems resiliency patterns
- observability
- authentication and authorization

The architecture closely mirrors the evolution path used by large-scale systems such as Twitter and Instagram.

The project demonstrates how real-world feed systems balance:
- scalability
- latency
- reliability
- consistency
- operational complexity

through the combined use of queues, caching, sharding, ranking, and hybrid fanout strategies.