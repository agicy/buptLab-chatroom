package com.chatroom.common.message;

public class SystemBroadcast extends SystemMessage {
    private final String type;
    private final String username;

    public SystemBroadcast(MessageContent content, String type, String username) {
        super(content);
        this.type = type;
        this.username = username;
    }

    public String getType() {
        return type;
    }

    public String getUsername() {
        return username;
    }
}

