package com.social.minisocialplatform.ratelimiter;

public class TokenBucket {
    private int capacity;
    private int refillTokens;
    private int tokens;
    private long refillIntervalMillis;
    private long lastRefillTimestamp;

    public TokenBucket(int capacity, int refillTokens, long refillIntervalMillis) {
        this.capacity = capacity;
        this.refillTokens = refillTokens;
        this.tokens = capacity; // Start with a full bucket
        this.refillIntervalMillis = refillIntervalMillis;
        this.lastRefillTimestamp = System.currentTimeMillis();
    }

    public synchronized boolean allowRequest() {
        refillTokensIfNeeded();
        if (tokens > 0) {
            tokens--;
            return true;
        }
        return false;
    }

    private void refillTokensIfNeeded() {
        long now = System.currentTimeMillis();
        long elapsedTime = now - lastRefillTimestamp;
        if (elapsedTime >= refillIntervalMillis) {
            long intervals = elapsedTime / refillIntervalMillis;
            int refillAmount = (int) (intervals * refillTokens);
            tokens = Math.min(capacity, tokens + refillAmount);
            lastRefillTimestamp = now;
        }
    }
}
