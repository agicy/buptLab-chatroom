package com.chatroom.common.message;

public class UserBroadcastMessage extends UserMessage {
    public UserBroadcastMessage(String sender, boolean anonymous, MessageContent content) {
        super(content, sender, anonymous);
    }
}
