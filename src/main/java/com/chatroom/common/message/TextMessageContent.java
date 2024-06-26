package com.chatroom.common.message;

/**
 * Represents the content of a text message in the chatroom application.
 */
public class TextMessageContent extends MessageContent {

    private final String text;

    /**
     * Creates a new instance of text message content.
     *
     * @param text the text content of the message
     */
    public TextMessageContent(String text) {
        this.text = text;
    }

    /**
     * Gets the raw content of the text message.
     *
     * @return the raw text content
     */
    public Object getContent() {
        return text;
    }

    /**
     * Gets the formatted text content of the message.
     *
     * @return the formatted text content
     */
    public String getText() {
        return text;
    }
}