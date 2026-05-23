package com.social.minisocialplatform.messaging;

import org.springframework.boot.CommandLineRunner;
import org.springframework.stereotype.Component;

@Component
public class DuplicateEventTest implements CommandLineRunner {

    private PostEventPublisher postEventPublisher;

    public DuplicateEventTest(PostEventPublisher postEventPublisher) {
        this.postEventPublisher = postEventPublisher;
    }

    @Override
    public void run(String... args) throws Exception {
        PostCreatedEvent event = new PostCreatedEvent("test-event-id", "99", "Testing Duplicate Events");
        postEventPublisher.publishPostCreatedEvent(event);
        postEventPublisher.publishPostCreatedEvent(event); // Publish duplicate event
    }
}