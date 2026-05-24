package com.social.minisocialplatform.observability;

import org.springframework.stereotype.Service;

import java.util.concurrent.atomic.AtomicInteger;
import java.util.concurrent.atomic.AtomicLong;

@Service
public class MetricsService {
    private final AtomicInteger totalRequests = new AtomicInteger(0);

    private final AtomicLong totalLatency = new AtomicLong(0);

    public void recordRequest(long latency) {
        totalRequests.incrementAndGet();
        totalLatency.addAndGet(latency);
    }

    public int getTotalRequests() {
        return totalRequests.get();
    }
    
    public long getAverageLatency() {
        if(totalRequests.get() == 0) return 0;

        return totalLatency.get() / totalRequests.get();
    }
}
