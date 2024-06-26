package com.chatroom.common.message;

/**
 * Represents a user message in the chatroom application.
 */
public abstract class UserMessage extends Message {

    private final String sender;
    private final boolean anonymous;

    /**
     * Creates a new instance of a user message.
     *
     * @param content   the content of the message
     * @param sender    the sender's username
     * @param anonymous true if the message is anonymous, false otherwise
     */
    public UserMessage(MessageContent content, String sender, boolean anonymous) {
        super(content);
        this.sender = sender;
        this.anonymous = anonymous;
    }

    /**
     * Gets the sender's username associated with the message.
     *
     * @return the sender's username
     */
    public String getSender() {
        return sender;
    }

    /**
     * Checks if the message is anonymous.
     *
     * @return true if the message is anonymous, false otherwise
     */
    public boolean isAnonymous() {
        return anonymous;
    }
}