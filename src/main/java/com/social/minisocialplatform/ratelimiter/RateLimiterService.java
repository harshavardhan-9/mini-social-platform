package com.social.minisocialplatform.ratelimiter;

import org.springframework.stereotype.Service;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

@Service
public class RateLimiterService {
    private final Map<String, TokenBucket> userBuckets = new ConcurrentHashMap<>();

    public boolean allowRequest(String userId) {
        TokenBucket bucket = userBuckets.computeIfAbsent(userId, k -> new TokenBucket(5, 5, 60000)); // 5 tokens, refill 5 tokens every 60 seconds
        return bucket.allowRequest();
    }
}
