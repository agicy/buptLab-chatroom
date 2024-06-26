package com.chatroom.common.message;

/**
 * Represents a system broadcast message in the chatroom application.
 */
public class SystemBroadcast extends SystemMessage {

    private final String type;
    private final String username;

    /**
     * Creates a new instance of a system broadcast message.
     *
     * @param content  the content of the message
     * @param type     the type of the broadcast (e.g., "announcement")
     * @param username the username associated with the broadcast
     */
    public SystemBroadcast(MessageContent content, String type, String username) {
        super(content);
        this.type = type;
        this.username = username;
    }

    /**
     * Gets the type of the system broadcast.
     *
     * @return the broadcast type
     */
    public String getType() {
        return type;
    }

    /**
     * Gets the username associated with the broadcast.
     *
     * @return the username
     */
    public String getUsername() {
        return username;
    }
}

