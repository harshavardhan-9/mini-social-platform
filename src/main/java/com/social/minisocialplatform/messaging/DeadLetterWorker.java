package com.social.minisocialplatform.messaging;

import org.springframework.amqp.rabbit.annotation.RabbitListener;
import org.springframework.stereotype.Service;

@Service
public class DeadLetterWorker {
    
    @RabbitListener(queues = RabbitMQConfig.DLQ_QUEUE)
    public void handleDeadLetter(PostCreatedEvent event) {
        System.out.println("DLQ received failed event: " + event.getEventId());

        System.out.println("Failed Content: " + event.getContent());
    }
}
