package com.social.minisocialplatform.messaging;

import org.springframework.stereotype.Component;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;

@Component
public class IdempotencyStore {
    private Set<String> processedFeedEvents = ConcurrentHashMap.newKeySet();
    private Set<String> processedNotificationEvents = ConcurrentHashMap.newKeySet();

    public boolean isFeedEventProcessed(String eventId) {
        return processedFeedEvents.contains(eventId);
    }

    public void markFeedEventProcessed(String eventId) {
        processedFeedEvents.add(eventId);
    }
    public boolean isNotificationEventProcessed(String eventId) {
        return processedNotificationEvents.contains(eventId);
    }

    public void markNotificationEventProcessed(String eventId) {
        processedNotificationEvents.add(eventId);
    }
}
