package smp.model;

import java.util.Date;
import java.util.UUID;

public class Message {
    private UUID id;
    private UUID userId;
    private String message;
    private Date timestamp;

    public Message(UUID userId, String message) {
        setId(UUID.randomUUID());
        setUserId(userId);
        setMessage(message);
        setTimestamp(new Date());
    }

    public UUID getId() {
        return id;
    }

    private void setId(UUID id) {
        this.id = id;
    }

    public UUID getUserId() {
        return userId;
    }

    private void setUserId(UUID userId) {
        this.userId = userId;
    }

    public String getMessage() {
        return message;
    }

    public void setMessage(String message) {
        this.message = message;
    }

    public Date getTimestamp() {
        return timestamp;
    }

    private void setTimestamp(Date timestamp) {
        this.timestamp = timestamp;
    }
}
