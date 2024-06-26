package com.chatroom.common.message;

public abstract class UserMessage extends Message {
    private final String sender;
    private final boolean anonymous;

    public UserMessage(MessageContent content, String sender, boolean anonymous) {
        super(content);
        this.sender = sender;
        this.anonymous = anonymous;
    }

    public String getSender() {
        return sender;
    }

    public boolean isAnonymous() {
        return anonymous;
    }

}
