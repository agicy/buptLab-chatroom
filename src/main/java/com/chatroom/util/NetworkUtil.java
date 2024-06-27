package com.chatroom.util;

import java.net.Socket;

/**
 * Utility class for network-related operations in the chatroom application.
 */
public class NetworkUtil {

    /**
     * Gets the IP address associated with a socket.
     *
     * @param socket the socket
     * @return the IP address as a string, or "Unknown" if not available
     */
    public static String getIpAddress(Socket socket) {
        if (socket != null && socket.getInetAddress() != null) {
            return socket.getInetAddress().getHostAddress();
        }
        return "Unknown";
    }
}
