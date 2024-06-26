package com.chatroom.common.message;

import org.jetbrains.annotations.NotNull;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.Objects;

public class Messages {
    private static @NotNull String getFormattedTimestamp(@NotNull LocalDateTime timestamp) {
        DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss");
        return timestamp.format(formatter);
    }

    private static @NotNull String getUserBroadcastMessagePrefix(@NotNull UserBroadcastMessage message, String currentUsername) {
        String sender = message.isAnonymous() ? "Anonymous" : message.getSender();
        return String.format("%s (%s): ", Objects.equals(message.getSender(), currentUsername) ? sender + "(You)" : sender, getFormattedTimestamp(message.getTimestamp()));
    }

    private static @NotNull String getUserPrivateMessagePrefix(@NotNull UserPrivateMessage message, String currentUsername) {
        if (Objects.equals(currentUsername, message.getSender()))
            return String.format("(Private from you%s to %s) (%s): ", message.isAnonymous() ? "(Anonymous)" : "", message.getReceiver(), getFormattedTimestamp(message.getTimestamp()));
        if (Objects.equals(currentUsername, message.getReceiver()))
            return String.format("(Private from %s to you) (%s): ", message.isAnonymous() ? "Anonymous" : message.getSender(), getFormattedTimestamp(message.getTimestamp()));
        throw new IllegalArgumentException("Unintended private message");
    }


    private static String getUserMessagePrefix(@NotNull UserMessage message, String currentUsername) {
        return switch (message) {
            case UserBroadcastMessage bm -> getUserBroadcastMessagePrefix(bm, currentUsername);
            case UserPrivateMessage pm -> getUserPrivateMessagePrefix(pm, currentUsername);
            default -> throw new IllegalArgumentException("Unsupported message type: " + message.getClass());
        };
    }

    private static @NotNull String getSystemMessagePrefix(@NotNull SystemMessage message) {
        return switch (message) {
            case SystemBroadcast sb ->
                    String.format("[System Broadcast] (%s): ", getFormattedTimestamp(sb.getTimestamp()));
            case SystemReply sreply ->
                    String.format("[System Reply] (%s): ", getFormattedTimestamp(sreply.getTimestamp()));
            default -> throw new IllegalArgumentException("Unintended message type: " + message.getClass());
        };
    }

    public static String getMessagePrefix(@NotNull Message message, String currentUsername) {
        return switch (message) {
            case UserMessage um -> getUserMessagePrefix(um, currentUsername);
            case SystemMessage sm -> getSystemMessagePrefix(sm);
            default -> throw new IllegalArgumentException("Unsupported message type: " + message.getClass());
        };
    }

    public static String getMessageContent(@NotNull Message message) {
        MessageContent messageContent = message.getContent();
        return switch (messageContent) {
            case TextMessageContent tmc -> tmc.getContent();
            default -> throw new IllegalStateException("Unsupported message content type: " + messageContent);
        };
    }

}
