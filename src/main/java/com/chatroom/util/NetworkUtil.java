package com.chatroom.util;

import java.net.Socket;

public class NetworkUtil {
    public static String getIpAddress(Socket socket) {
        if (socket != null && socket.getInetAddress() != null) {
            return socket.getInetAddress().getHostAddress();
        }
        return "Unknown";
    }
}