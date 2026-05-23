# Sharding Decisions for Posts

## Chosen Sharding Key: user_id

The project shards posts using user_id.
This means all posts created by a user are stored on the same shard.

Example:
- user 1 posts → shard1
- user 2 posts → shard2

This improves feed generation because fetching posts for a user's network becomes easier.

---

# Option 1 — Sharding by user_id

## Advantages
- user data stays grouped together
- efficient feed queries
- easier follower-based lookups
- simpler caching patterns

## Disadvantages
- celebrity users may create hot shards
- uneven traffic distribution possible

Example:
A celebrity with millions of followers may overload one shard.

---

# Option 2 — Sharding by post_id

## Advantages
- evenly distributes writes
- avoids hot users overloading one shard

## Disadvantages
- posts from same user become scattered
- feed queries require cross-shard aggregation

This increases query complexity.

---

# Option 3 — Sharding by created_at

## Advantages
- useful for time-series workloads
- recent posts grouped together

## Disadvantages
- recent shard becomes overloaded
- poor user-based query performance
- historical queries become difficult

Example:
All current traffic may hit the newest shard.

---

# Final Decision

The project uses user_id as the sharding key because:

- feed generation is user-centric
- follower queries are common
- user locality improves performance
- implementation is simpler for social platforms
