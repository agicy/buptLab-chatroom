package com.chatroom.common.message;

public class UserPrivateMessage extends UserMessage {
    private final String receiver;

    public UserPrivateMessage(String sender, boolean anonymous, String receiver, MessageContent content) {
        super(content, sender, anonymous);
        this.receiver = receiver;
    }

    public String getReceiver() {
        return receiver;
    }
}
