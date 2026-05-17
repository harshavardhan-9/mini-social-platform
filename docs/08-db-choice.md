# Database Choice for Mini Social Platform

## Chosen Database: PostgreSQL

PostgreSQL was chosen for this project because the platform data is highly relational and depends heavily on structured queries between multiple entities.

Main relationships include:

* users creating posts
* users following other users
* users liking posts

These relationships are naturally suited for relational databases and SQL joins.

---

# Why SQL Fits This Project

This platform contains structured relational data with frequent relationship-based queries.

Examples:

* latest posts from followed users
* followers count
* likes on posts
* feed generation queries

The application also requires:

* relational integrity
* joins between tables
* indexed queries
* structured schemas
* consistency for likes and follows

SQL databases handle these operations efficiently.

---

# Why PostgreSQL Was Chosen

PostgreSQL provides:

* ACID transactions
* strong consistency
* efficient B-tree indexing
* powerful joins and aggregations
* mature query optimization

It is a strong fit for backend systems that require:

* relational querying
* indexing
* transactional safety
* optimized feed queries

PostgreSQL also scales well for medium-to-large social platform workloads.

---

# Why Not NoSQL

NoSQL databases are useful for:

* flexible schemas
* extremely high write throughput
* distributed event ingestion

However, this project depends heavily on relationships between entities.

Using NoSQL here would increase complexity for:

* follows
* likes
* feed queries
* joins between users and posts

Since the workload is relational and query-driven, SQL is the better choice.

---

# Read / Write Pattern

The platform is primarily:

* read-heavy for feed requests
* moderately write-heavy for posts and likes

Most requests involve:

* fetching feeds
* sorting posts by recency
* counting followers
* aggregating likes

PostgreSQL performs well for these workloads because indexes significantly improve read-heavy queries.

---

# Database Schema

The platform contains four main tables:

* users
* posts
* follows
* likes

---

# ER Diagram

```text id="u6n4xy"
+---------+        +---------+        +---------+
|  users  |        |  posts  |        |  likes  |
+---------+        +---------+        +---------+
| user_id |<------ | user_id |        | like_id |
| username|        | post_id |<------ | post_id |
| email   |        | content |        | user_id |
+---------+        | created |        +---------+
                   +---------+

       ^
       |
       |
+-------------------+
|      follows      |
+-------------------+
| follower_id       |
| following_id      |
+-------------------+
```

---

# Relationship Explanation

* One user can create many posts.
* One user can follow many users.
* One post can receive many likes.
* One user can like many posts.

This structure models the core relationships required for a social platform feed system.

---

# Final Justification

PostgreSQL was chosen because:

* the schema is relational
* joins are important for feed generation
* indexing improves query performance
* consistency matters for likes and follows
* SQL simplifies relationship-based querying
