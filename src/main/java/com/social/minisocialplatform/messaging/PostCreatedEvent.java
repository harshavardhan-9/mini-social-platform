package com.social.minisocialplatform.messaging;

import java.io.Serializable;

public class PostCreatedEvent implements Serializable {
    private String eventId;
    private String userId;
    private String content;
    private String traceId;

    public PostCreatedEvent() {
    }

    public PostCreatedEvent(String eventId, String userId, String content, String traceId) {
        this.eventId = eventId;
        this.userId = userId;
        this.content = content;
        this.traceId = traceId;
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

    public String getTraceId() {
        return traceId;
    }
}
