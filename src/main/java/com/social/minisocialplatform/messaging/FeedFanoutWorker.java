package com.social.minisocialplatform.messaging;

import org.springframework.amqp.rabbit.annotation.RabbitListener;
import org.springframework.stereotype.Service;

@Service
public class FeedFanoutWorker {
        
    @RabbitListener(queues = RabbitMQConfig.FEED_QUEUE)
    public void handlePostCreatedEvent(PostCreatedEvent event) {
        System.out.println("Feed worker processing event: " + event.getEventId());
        System.out.println("Updating feed for user: " + event.getUserId());
    }
}
