package com.chatroom.common.message;

public class SystemRequest extends SystemMessage {
    private final String username;

    public SystemRequest(String username, MessageContent content) {
        super(content);
        this.username = username;
    }

    public String getUsername() {
        return this.username;
    }
}
