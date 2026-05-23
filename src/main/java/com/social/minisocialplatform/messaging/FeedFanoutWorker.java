package com.social.minisocialplatform.messaging;

import org.springframework.amqp.rabbit.annotation.RabbitListener;
import org.springframework.stereotype.Service;

@Service
public class FeedFanoutWorker {
        
    private IdempotencyStore idempotencyStore;

    private static int pendingEvents = 0;

    public FeedFanoutWorker(IdempotencyStore idempotencyStore) {
        this.idempotencyStore = idempotencyStore;
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
            System.out.println("Feed worker processing event: " + event.getEventId());
            System.out.println("Updating feed for user: " + event.getUserId());

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
