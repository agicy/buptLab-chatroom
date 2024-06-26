package com.chatroom.common.message;

import java.util.List;

/**
 * Represents a system message containing a list of online users in the chatroom application.
 */
public class SystemUserList extends SystemMessage {

    private final List<String> users;

    /**
     * Creates a new instance of a system user list message.
     *
     * @param content the content of the message
     * @param users   the list of usernames representing online users
     */
    public SystemUserList(MessageContent content, List<String> users) {
        super(content);
        this.users = users;
    }

    /**
     * Gets the list of online users.
     *
     * @return the list of usernames representing online users
     */
    public List<String> getUsers() {
        return users;
    }
}
