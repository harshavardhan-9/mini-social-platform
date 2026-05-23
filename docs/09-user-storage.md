# Scalable User Storage for 100M Users

# Overview

A social platform serving 100 million users requires a highly scalable and fault-tolerant storage system.

The system must support:

- millions of feed reads per second
- high concurrent writes
- low latency responses
- reliable user storage
- high availability during failures

The architecture should also support future scaling without major redesigns.

---

# Functional Requirements

The storage system should support:

- user registration and profile storage
- creating posts
- following/unfollowing users
- feed generation
- likes and comments
- reading user feeds with low latency
- storing large volumes of user-generated content

The system should also support distributed storage across multiple servers.

---

# Non-Functional Requirements

## Scalability
- The platform should scale horizontally as users increase.
- Adding more servers should increase storage and throughput capacity.

---

## Availability

- The system should remain operational even if some database nodes fail.
- Users should still be able to read feeds during failures.

---

## Low Latency

- Feed requests should respond quickly even under heavy traffic.
- Caching and replication help reduce response times.

---

## Fault Tolerance

- Data should not be lost during node failures.
- Replication is required for backup and recovery.

---

## Consistency

- Some operations require strong consistency while others can tolerate eventual consistency.
Example:
- authentication requires strong consistency
- feed updates can tolerate small delays

---

# Sharding Strategy

The system uses database sharding to distribute data across multiple nodes.

## Chosen Sharding Key: user_id

Users are distributed using consistent hashing on user_id.

Example:

- user 101 → shard1
- user 202 → shard2
- user 303 → shard3

This keeps related user data grouped together.

Benefits:

- efficient feed queries
- improved locality of user data
- reduced cross-shard communication
- easier horizontal scaling

---

# Why Consistent Hashing

Consistent hashing minimizes redistribution when nodes are added or removed.

Without consistent hashing:
- adding one shard may remap almost all users

With consistent hashing:
- only nearby ownership ranges move

This improves scalability and system stability.

---

# Replication Topology

To use leader-follower replication.

Each shard contains:

- 1 leader node
- multiple follower replicas

Example:

Shard 1:
- leader
- follower A
- follower B

## Write Flow

All write operations go to the leader node.

Example:

- creating posts
- updating profiles
- follow/unfollow actions

---

## Read Flow

Read requests can be served from follower replicas.

Example:

- feed reads
- profile reads
- follower counts

Benefits:

- improved read scalability
- lower leader load
- higher availability

Replication is asynchronous to improve write performance.

---

# Consistency Choice

The system primarily uses eventual consistency for social features.

Example:
- a newly created follow may take a few seconds to appear in feed recommendations

This improves:

- scalability
- write throughput
- availability

However, strong consistency is still preferred for:

- authentication
- payments
- password updates
- security-related operations

The consistency model depends on the criticality of the feature.

---

# Hot Shard Mitigation

A major distributed systems problem is uneven traffic distribution.

Example:
- celebrity accounts may generate massive traffic

This can overload one shard.
The system can use multiple mitigation techniques to avoid these:

---

## Virtual Nodes

Virtual nodes improve data distribution across shards.

This reduces uneven ownership ranges.

---

## Caching

Frequently accessed feeds and profiles are cached.

Example:

- Redis cache
- CDN cache

This reduces database pressure.

---

## Read Replicas

Follower replicas distribute heavy read traffic.

This prevents the leader from becoming overloaded.

---

## Async Processing

Large fan-out operations are processed asynchronously using queues.

This prevents sudden traffic spikes from crashing the system.

---

# Handling Celebrity Users With 50M Followers

Celebrity users create extremely large fan-out workloads.

Example:

- one post may need delivery to millions of followers

Directly pushing posts to every follower feed becomes expensive.

## Hybrid Fan-out Strategy

### Normal Users

Use fan-out on write.

Posts are pushed directly into follower feeds.

---

### Celebrity Users

Use fan-out on read.

Posts are fetched dynamically during feed generation instead of pre-pushing them to all followers.

Benefits:

- avoids massive write amplification
- reduces storage overhead
- improves scalability

This strategy is commonly used in large social media systems.

---

# Failure Handling

If a leader node fails:

- a follower replica is promoted to leader
- traffic reroutes automatically

Replication ensures data durability and system recovery.

Consistent hashing minimizes redistribution during failures.

Only nearby ranges move to other shards.

---

# Final Design Summary

The final architecture combines:

- consistent hashing
- virtual nodes
- database sharding
- leader-follower replication
- eventual consistency
- caching
- asynchronous processing
