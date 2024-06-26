package com.chatroom.common.message;

/**
 * Represents a user broadcast message in the chatroom application.
 */
public class UserBroadcastMessage extends UserMessage {

    /**
     * Creates a new instance of a user broadcast message.
     *
     * @param sender    the sender's username
     * @param anonymous true if the message is anonymous, false otherwise
     * @param content   the content of the message
     */
    public UserBroadcastMessage(String sender, boolean anonymous, MessageContent content) {
        super(content, sender, anonymous);
    }
}