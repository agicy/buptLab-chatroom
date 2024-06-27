package com.chatroom.common.message;

public class TextMessageContent extends MessageContent {
    private final String text;

    public TextMessageContent(String text) {
        this.text = text;
    }

    public Object getContent() {
        return text;
    }

    public String getText() {
        return text;
    }
}
