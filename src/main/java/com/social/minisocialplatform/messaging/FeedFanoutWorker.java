package com.social.minisocialplatform.messaging;

import org.springframework.amqp.rabbit.annotation.RabbitListener;
import org.springframework.stereotype.Service;

@Service
public class FeedFanoutWorker {
        
    private IdempotencyStore idempotencyStore;

    public FeedFanoutWorker(IdempotencyStore idempotencyStore) {
        this.idempotencyStore = idempotencyStore;
    }

    @RabbitListener(queues = RabbitMQConfig.FEED_QUEUE)
    public void handlePostCreatedEvent(PostCreatedEvent event) {
        if (idempotencyStore.isFeedEventProcessed(event.getEventId())) {
            System.out.println("Duplicate event skipped in feed worker: " + event.getEventId());
            return;
        }
        System.out.println("Feed worker processing event: " + event.getEventId());
        System.out.println("Updating feed for user: " + event.getUserId());

        idempotencyStore.markFeedEventProcessed(event.getEventId());
    }
}
