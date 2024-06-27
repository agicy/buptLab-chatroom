package com.chatroom.common.message;

import org.jetbrains.annotations.NotNull;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.Objects;

/**
 * Utility class for formatting chat messages in the chatroom application.
 */
public class Messages {

    /**
     * Gets a formatted timestamp string.
     *
     * @param timestamp the timestamp to format
     * @return the formatted timestamp string
     */
    private static @NotNull String getFormattedTimestamp(@NotNull LocalDateTime timestamp) {
        DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss");
        return timestamp.format(formatter);
    }

    /**
     * Gets the prefix for a user broadcast message.
     *
     * @param message         the user broadcast message
     * @param currentUsername the current user's username
     * @return the formatted message prefix
     */
    private static @NotNull String getUserBroadcastMessagePrefix(@NotNull UserBroadcastMessage message, String currentUsername) {
        String sender = message.isAnonymous() ? "Anonymous" : message.getSender();
        return String.format("%s", Objects.equals(message.getSender(), currentUsername) ? sender + "(You)" : sender);
    }

    /**
     * Gets the prefix for a user private message.
     *
     * @param message         the user private message
     * @param currentUsername the current user's username
     * @return the formatted message prefix
     */
    private static @NotNull String getUserPrivateMessagePrefix(@NotNull UserPrivateMessage message, String currentUsername) {
        if (Objects.equals(currentUsername, message.getSender()))
            return String.format("(Private from you%s to %s)", message.isAnonymous() ? "(Anonymous)" : "", message.getReceiver());
        if (Objects.equals(currentUsername, message.getReceiver()))
            return String.format("(Private from %s to you)", message.isAnonymous() ? "Anonymous" : message.getSender());
        throw new IllegalArgumentException("Unintended private message");
    }


    /**
     * Gets the prefix for a user message (broadcast or private).
     *
     * @param message         the user message
     * @param currentUsername the current user's username
     * @return the formatted message prefix
     */
    private static String getUserMessagePrefix(@NotNull UserMessage message, String currentUsername) {
        return switch (message) {
            case UserBroadcastMessage bm -> getUserBroadcastMessagePrefix(bm, currentUsername);
            case UserPrivateMessage pm -> getUserPrivateMessagePrefix(pm, currentUsername);
            default -> throw new IllegalArgumentException("Unsupported message type: " + message.getClass());
        };
    }

    /**
     * Gets the prefix for a system message (broadcast or reply).
     *
     * @param message the system message
     * @return the formatted message prefix
     */
    private static @NotNull String getSystemMessagePrefix(@NotNull SystemMessage message) {
        return switch (message) {
            case SystemBroadcast ignored -> "[System Broadcast]";
            case SystemReply ignored -> "[System Reply]";
            default -> throw new IllegalArgumentException("Unintended message type: " + message.getClass());
        };
    }

    /**
     * Gets the complete formatted message prefix (including timestamp and sender information).
     *
     * @param message         the chat message
     * @param currentUsername the current user's username
     * @return the complete formatted message prefix
     */
    public static String getMessagePrefix(@NotNull Message message, String currentUsername) {
        return getFormattedTimestamp(message.getTimestamp()) + "\n" + switch (message) {
            case UserMessage um -> getUserMessagePrefix(um, currentUsername);
            case SystemMessage sm -> getSystemMessagePrefix(sm);
            default -> throw new IllegalArgumentException("Unsupported message type: " + message.getClass());
        } + ":";
    }

    /**
     * Gets the content of a chat message.
     *
     * @param message the chat message
     * @return the message content
     */
    public static String getMessageContent(@NotNull Message message) {
        MessageContent messageContent = message.getContent();
        return switch (messageContent) {
            case TextMessageContent tmc -> tmc.getText();
            default -> throw new IllegalStateException("Unsupported message content type: " + messageContent);
        };
    }

}
