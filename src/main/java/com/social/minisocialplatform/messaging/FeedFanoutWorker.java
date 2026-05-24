package com.social.minisocialplatform.messaging;

import org.springframework.amqp.rabbit.annotation.RabbitListener;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.stereotype.Service;

import com.social.minisocialplatform.model.Post;
import com.social.minisocialplatform.service.FeedService;

@Service
public class FeedFanoutWorker {
        
    private IdempotencyStore idempotencyStore;

    private static int pendingEvents = 0;

    private RabbitTemplate rabbitTemplate;

    private FeedService feedService;

    public FeedFanoutWorker(IdempotencyStore idempotencyStore, RabbitTemplate rabbitTemplate, FeedService feedService) {
        this.idempotencyStore = idempotencyStore;
        this.rabbitTemplate = rabbitTemplate;
        this.feedService = feedService;
    }

    @RabbitListener(queues = RabbitMQConfig.FEED_QUEUE)
    public void handlePostCreatedEvent(PostCreatedEvent event) {
        pendingEvents++;
        System.out.println("Current pending events in feed worker: " + pendingEvents);
        
        if(pendingEvents > 5) {
            System.out.println("Backpressure triggered. Dropping event: " + event.getEventId());
            pendingEvents--;
            return;
        }

        if (idempotencyStore.isFeedEventProcessed(event.getEventId())) {
            System.out.println("Duplicate event skipped in feed worker: " + event.getEventId());
            return;
        }
        
        new Thread(() -> {
            System.out.println("Trace ID: " + event.getTraceId());
            System.out.println("Feed worker processing event: " + event.getEventId());
            System.out.println("Updating feed for user: " + event.getUserId());

            Post post = new Post(Integer.parseInt(event.getUserId()), event.getContent(),
                        new java.sql.Timestamp(System.currentTimeMillis()), 0);

            feedService.pushPostToFollowers(event.getUserId(), post);

            if(event.getContent().toUpperCase().contains("FAIL")) {
                System.out.println("Event failed processing. Sending to DLQ: " + event.getEventId());
                rabbitTemplate.convertAndSend(RabbitMQConfig.DLQ_QUEUE, event);
                pendingEvents--;
                return;
            }

            try {
                Thread.sleep(5000);
            } catch (InterruptedException e) {
                e.printStackTrace();
            }

            idempotencyStore.markFeedEventProcessed(event.getEventId());
            pendingEvents--;

        }).start();
    }
}
