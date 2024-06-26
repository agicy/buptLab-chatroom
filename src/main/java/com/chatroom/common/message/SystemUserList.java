package com.chatroom.common.message;

import java.util.List;

public class SystemUserList extends SystemMessage {
    private final List<String> users;

    public SystemUserList(MessageContent content, List<String> users) {
        super(content);
        this.users = users;
    }

    public List<String> getUsers() {
        return users;
    }
}
