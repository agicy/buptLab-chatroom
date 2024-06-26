package com.chatroom.common.message;

import java.io.Serializable;

/**
 * Represents the content of a chat message in the chatroom application.
 */
public abstract class MessageContent implements Serializable {

    /**
     * Gets the content of the message.
     *
     * @return the message content
     */
    public abstract Object getContent();
}
