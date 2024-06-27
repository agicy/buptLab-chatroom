package com.chatroom.common.message;

import java.io.Serializable;
import java.time.LocalDateTime;

/**
 * Represents a chat message in the chatroom application.
 */
public abstract class Message implements Serializable {

    private final MessageContent content;
    private final LocalDateTime timestamp;

    /**
     * Creates a new instance of a chat message.
     *
     * @param content the content of the message
     */
    public Message(MessageContent content) {
        this.content = content;
        this.timestamp = LocalDateTime.now();
    }

    /**
     * Gets the content of the message.
     *
     * @return the message content
     */
    public MessageContent getContent() {
        return content;
    }

    /**
     * Gets the timestamp when the message was created.
     *
     * @return the message timestamp
     */
    public LocalDateTime getTimestamp() {
        return timestamp;
    }

}
