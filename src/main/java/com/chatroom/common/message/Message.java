package com.chatroom.common.message;

import java.io.Serializable;
import java.time.LocalDateTime;

public abstract class Message implements Serializable {
    private final MessageContent content;
    private final LocalDateTime timestamp;

    public Message(MessageContent content) {
        this.content = content;
        this.timestamp = LocalDateTime.now();
    }

    public MessageContent getContent() {
        return content;
    }

    public LocalDateTime getTimestamp() {
        return timestamp;
    }

}
