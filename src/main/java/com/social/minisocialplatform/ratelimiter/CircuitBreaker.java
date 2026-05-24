package com.social.minisocialplatform.ratelimiter;

public class CircuitBreaker {
    private CircuitBreakerState state = CircuitBreakerState.CLOSED;

    private int failureCount = 0;
    private final int failureThreshold = 3;
    private long lastFailureTime = 0;

    private final long retryTimeMillis = 60000; // 1 minute

    public synchronized boolean allowRequest() {
       
        if( state == CircuitBreakerState.OPEN) {
            long now = System.currentTimeMillis();
            if(now - lastFailureTime > retryTimeMillis) {
                state = CircuitBreakerState.HALF_OPEN;
                System.out.println("Circuit breaker is now HALF_OPEN. Allowing trial request.");
                return true; // Allow a trial request
            }
            return false; // Reject requests while OPEN
        }
        return true; // Allow requests while CLOSED or HALF_OPEN
    }

    public synchronized void recordFailure() {
        failureCount++;
        lastFailureTime = System.currentTimeMillis();
        if(failureCount >= failureThreshold) {
            state = CircuitBreakerState.OPEN;
            System.out.println("Circuit breaker is now OPEN due to failures.");
        }
    }

    public synchronized void recordSuccess() {
        failureCount = 0;
        state =CircuitBreakerState.CLOSED;
        System.out.println("Circuit breaker CLOSED");
    }

    public CircuitBreakerState getState() {
        return state;
    }
}
