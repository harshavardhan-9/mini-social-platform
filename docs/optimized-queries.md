# Optimized SQL Queries

## 1. Get Followers Count

```sql
SELECT COUNT(*)
FROM follows
WHERE followee_id = 1;
```

Why efficient:
- uses indexed filtering
- avoids full table scan
- aggregation only on matching rows

---

## 2. Latest 20 Posts From Followed Users

```sql
SELECT p.*
FROM posts p
JOIN follows f
ON p.user_id = f.followee_id
WHERE f.follower_id = 1
ORDER BY p.created_at DESC
LIMIT 20;
```

Why efficient:
- indexed joins
- indexed sorting on created_at
- LIMIT reduces scanned rows

---

## 3. Most Liked Post In Last 24 Hours

```sql
SELECT p.id, p.content, COUNT(l.post_id) AS like_count
FROM posts p
JOIN likes l
ON p.id = l.post_id
WHERE p.created_at >= NOW() - INTERVAL '24 HOURS'
GROUP BY p.id
ORDER BY like_count DESC
LIMIT 1;
```

Why efficient:
- filters recent posts first
- indexed joins
- aggregation on filtered rows only