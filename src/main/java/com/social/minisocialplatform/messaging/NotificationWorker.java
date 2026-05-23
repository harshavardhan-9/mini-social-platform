package com.social.minisocialplatform.messaging;

import org.springframework.amqp.rabbit.annotation.RabbitListener;
import org.springframework.stereotype.Service;

import java.io.FileWriter;
import java.io.IOException;

@Service
public class NotificationWorker {
    
    @RabbitListener(queues = RabbitMQConfig.NOTIFICATION_QUEUE)
    public void handlePostCreatedEvent(PostCreatedEvent event) {
        System.out.println("Notification worker processing event: " + event.getEventId());
        String notification = "User" + event.getUserId() + " created a new post: " + event.getContent();

        // Simulate sending notification by writing to a file
        try {
            FileWriter writer = new FileWriter("notifications.log", true);
            writer.write(notification + "\n");
            writer.close();
            System.out.println("Notification logged");
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}
