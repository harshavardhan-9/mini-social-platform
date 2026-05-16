# Adding Caching to Instagram Feed

## What to Cache

The primary thing to cache is the user feed response.

Example:

```text id="6h6j0d"
GET /feed/{userId}
```

Feed generation is expensive because it may involve:

* fetching posts
* sorting by recency/relevance
* merging followed users' posts
* recommendation logic

Other useful cache candidates:

* user profiles
* follower counts
* trending posts
* media metadata

---

# Where to Cache

## Client Cache

The mobile app or browser can cache:

* images
* videos
* recently viewed posts

Benefits:

* reduced backend requests
* faster user experience

Limitations:

* stale data
* limited device storage

---

## CDN / Edge Cache

CDNs are useful for:

* images
* reels/videos
* static assets

Benefits:

* low global latency
* reduced backend bandwidth

Limitations:

* not suitable for highly personalized feeds

---

## Backend Cache

Backend caching stores:

* feed responses
* recommendation results

### In-Memory Cache

Example:

* LRU cache inside application memory

Benefits:

* extremely fast
* very low latency

Limitations:

* cache lost on restart
* limited by server memory

---

### Distributed Cache

Example:

* Redis

Benefits:

* shared across multiple servers
* horizontally scalable

Limitations:

* additional infrastructure complexity
* network overhead

---

# Cache Invalidation

Feed cache should be invalidated when:

* user creates a post
* post is deleted
* follow/unfollow changes occur

Example:

```text id="97c94r"
POST /post
```

should invalidate affected feed caches so users receive fresh content.

---

# Eviction Policy

## LRU (Least Recently Used)

Removes least recently accessed entries.

Works well because recently viewed feeds are likely to be viewed again.

---

## LFU (Least Frequently Used)

Removes least frequently accessed entries.

Useful for workloads with stable popularity patterns.

Example:

* trending or viral posts

---

# Cache Stampede Protection

A cache stampede occurs when many requests hit the same expired cache key simultaneously.

Without protection:

```text id="kjlwmr"
100 requests
→ 100 database reads
```

This can overload backend systems.

---

# Request Coalescing

The project uses request coalescing.

Flow:

1. first request acquires a lock
2. only first request computes the result
3. remaining requests wait
4. cache gets populated
5. waiting requests reuse cached result

Benefits:

* prevents duplicate work
* reduces backend load
* improves scalability

---

# Stale-While-Revalidate

Another possible strategy is stale-while-revalidate.

Flow:

* serve stale cached data immediately
* refresh cache asynchronously

Benefits:

* lower latency
* smoother traffic spikes

Limitation:

* temporarily stale content may be shown

---

# Probabilistic Early Expiration

Cache entries expire slightly earlier using randomness.

Benefits:

* prevents many keys expiring simultaneously

Limitation:

* some cache entries may expire earlier than necessary
