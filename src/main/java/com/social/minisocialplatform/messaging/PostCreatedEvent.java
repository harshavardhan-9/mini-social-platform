package com.social.minisocialplatform.messaging;

import java.io.Serializable;

public class PostCreatedEvent implements Serializable {
    private String eventId;
    private String userId;
    private String content;

    public PostCreatedEvent() {
    }

    public PostCreatedEvent(String eventId, String userId, String content) {
        this.eventId = eventId;
        this.userId = userId;
        this.content = content;
    }

    public String getEventId() {
        return eventId;
    }

    public String getUserId() {
        return userId;
    }

    public String getContent() {
        return content;
    }
}
