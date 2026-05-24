package com.social.minisocialplatform.ratelimiter;

import org.junit.jupiter.api.Test;
import static org.junit.jupiter.api.Assertions.*;

public class RateLimiterTest {
    
    @Test
    void shouldBlockSixthRequest() {
        TokenBucket bucket = new TokenBucket(5, 5, 60000);
        for (int i = 0; i < 5; i++) {
            assertTrue(bucket.allowRequest(), "Request " + (i + 1) + " should be allowed");
        }
        assertFalse(bucket.allowRequest(), "6th request should be blocked");
    }
}
