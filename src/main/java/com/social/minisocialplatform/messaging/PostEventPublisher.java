package com.social.minisocialplatform.messaging;

import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.stereotype.Service;

@Service
public class PostEventPublisher {
    private final RabbitTemplate rabbitTemplate;

    public PostEventPublisher(RabbitTemplate rabbitTemplate) {
        this.rabbitTemplate = rabbitTemplate;
    }

    public void publishPostCreatedEvent(PostCreatedEvent event) {
        rabbitTemplate.convertAndSend(RabbitMQConfig.EXCHANGE, "", event);
        System.out.println("Published PostCreatedEvent: " + event.getEventId());
    }

}