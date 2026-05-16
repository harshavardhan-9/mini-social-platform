package com.social.minisocialplatform.benchmark;

import java.util.concurrent.*;
import com.social.minisocialplatform.service.FeedService;

public class CoalescingLoadTest {
    public static void main(String[] args) throws InterruptedException {
        FeedService feedService = new FeedService();
        ExecutorService executor = Executors.newFixedThreadPool(100);
        int totalRequests = 100;

        for(int i = 0; i<totalRequests; i++) {
            executor.submit(() -> {
                    feedService.getFeed("user1");
                });
        }
        executor.shutdown();
        executor.awaitTermination(1, TimeUnit.MINUTES);
    }
}
