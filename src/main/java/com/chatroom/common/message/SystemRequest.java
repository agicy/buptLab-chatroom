package com.chatroom.common.message;

/**
 * Represents a system request message in the chatroom application.
 */
public class SystemRequest extends SystemMessage {

    private final String username;

    /**
     * Creates a new instance of a system request message.
     *
     * @param username the username associated with the request
     * @param content  the content of the message
     */
    public SystemRequest(String username, MessageContent content) {
        super(content);
        this.username = username;
    }

    /**
     * Gets the username associated with the request.
     *
     * @return the username
     */
    public String getUsername() {
        return this.username;
    }

    /**
     * Gets the content of the request as text.
     *
     * @return the text content of the request
     */
    @Override
    public TextMessageContent getContent() {
        return (TextMessageContent) super.getContent();
    }
}
