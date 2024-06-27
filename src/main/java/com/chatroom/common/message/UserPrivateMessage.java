package com.chatroom.common.message;

/**
 * Represents a user private message in the chatroom application.
 */
public class UserPrivateMessage extends UserMessage {

    private final String receiver;

    /**
     * Creates a new instance of a user private message.
     *
     * @param sender    the sender's username
     * @param anonymous true if the message is anonymous, false otherwise
     * @param receiver  the receiver's username
     * @param content   the content of the message
     */
    public UserPrivateMessage(String sender, boolean anonymous, String receiver, MessageContent content) {
        super(content, sender, anonymous);
        this.receiver = receiver;
    }

    /**
     * Gets the receiver's username associated with the private message.
     *
     * @return the receiver's username
     */
    public String getReceiver() {
        return receiver;
    }
}