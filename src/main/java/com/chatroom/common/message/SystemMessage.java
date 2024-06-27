package com.chatroom.common.message;

/**
 * Represents a system message in the chatroom application.
 */
public abstract class SystemMessage extends Message {

    /**
     * Creates a new instance of a system message.
     *
     * @param content the content of the message
     */
    public SystemMessage(MessageContent content) {
        super(content);
    }
}
