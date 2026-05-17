package com.social.minisocialplatform.benchmark;

import java.util.concurrent.*;

import org.springframework.boot.SpringApplication;
import org.springframework.context.ConfigurableApplicationContext;

import com.social.minisocialplatform.MiniSocialPlatformApplication;
import com.social.minisocialplatform.service.FeedService;

public class CoalescingLoadTest {

    public static void main(String[] args) throws InterruptedException {

        ConfigurableApplicationContext context =
                SpringApplication.run(MiniSocialPlatformApplication.class);

        FeedService feedService = context.getBean(FeedService.class);

        ExecutorService executor = Executors.newFixedThreadPool(100);

        int totalRequests = 100;

        for (int i = 0; i < totalRequests; i++) {

            executor.submit(() -> {
                feedService.getFeed("1");
            });
        }

        executor.shutdown();

        executor.awaitTermination(1, TimeUnit.MINUTES);

        context.close();
    }
}