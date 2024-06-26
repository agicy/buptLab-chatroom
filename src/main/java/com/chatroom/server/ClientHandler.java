package com.chatroom.server;

import com.chatroom.common.message.*;
import com.chatroom.util.NetworkUtil;
import org.jetbrains.annotations.NotNull;

import java.io.EOFException;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.net.Socket;
import java.net.SocketException;
import java.util.Objects;

import static com.chatroom.common.message.SystemReply.*;

/**
 * Represents a client handler responsible for managing communication with a connected client.
 */
public class ClientHandler implements Runnable {

    private final Socket socket;
    private final Server server;
    private ObjectOutputStream out;
    private ObjectInputStream in;
    private String username;
    private boolean authenticated = false;

    /**
     * Creates a new instance of the client handler.
     *
     * @param socket the client socket
     * @param server the associated server
     */
    public ClientHandler(Socket socket, Server server) {
        this.socket = socket;
        this.server = server;
    }

    /**
     * Main loop for handling client messages.
     */
    private void loop() {
        try {
            Message inputMessage;
            while ((inputMessage = (Message) in.readObject()) != null) {
                handleClientMessage(inputMessage);
                if (socket.isClosed())
                    close(false);
            }
        } catch (SocketException ignored) {
        } catch (IOException | ClassNotFoundException e) {
            server.output("Error handling client: " + e.getMessage());
        }
    }

    /**
     * Handles the main execution logic for the client handler.
     */
    @Override
    public void run() {
        try {
            in = new ObjectInputStream(socket.getInputStream());
            out = new ObjectOutputStream(socket.getOutputStream());
            if (authenticate())
                loop();
        } catch (IOException | ClassNotFoundException e) {
            server.output("Error handling client: " + e.getMessage());
        } finally {
            close(false);
        }

    }

    /**
     * Logs a login attempt.
     *
     * @param username the attempted username
     * @param ip       the IP address of the client
     * @param success  true if login was successful, false otherwise
     */
    public void logLogin(String username, String ip, boolean success) {
        String status = success ? "successful" : "failed";
        server.output(String.format("Login %s for user %s from IP %s", status, username, ip));
    }

    /**
     * Logs a user logout.
     *
     * @param username the logged-out username
     */
    public void logLogout(String username) {
        server.output(String.format("User %s logged out", username));
    }

    /**
     * Authenticates the client based on provided credentials.
     *
     * @return true if authentication is successful, false otherwise
     * @throws IOException            if an I/O error occurs
     * @throws ClassNotFoundException if the class of a serialized object cannot be found
     */
    private boolean authenticate() throws IOException, ClassNotFoundException {
        String username, password;
        while (!authenticated) {
            try {
                username = ((SystemRequest) in.readObject()).getContent().getText();
                password = ((SystemRequest) in.readObject()).getContent().getText();
            } catch (EOFException e) {
                return false;
            }

            if (server.getUserManager().isUserExist(username)) {
                if (server.getUserManager().authenticate(username, password)) {
                    if (!server.isUserAlreadyLogin(username)) {
                        authenticated = true;
                        this.username = username;
                        out.writeObject(new SystemReply(new TextMessageContent(LOGIN_SUCCESS)));
                        out.writeObject(new SystemReply(new TextMessageContent("Authentication successful. Welcome to the chat room!")));
                        server.broadcastMessage(new SystemBroadcast(new TextMessageContent(username + " has joined the chat."), "join", username));
                        logLogin(username, NetworkUtil.getIpAddress(socket), authenticated);
                        return true;
                    } else
                        out.writeObject(new SystemReply(new TextMessageContent(ALREADY_LOGIN)));
                } else
                    out.writeObject(new SystemReply(new TextMessageContent(PASSWORD_INCORRECT)));
            } else
                out.writeObject(new SystemReply(new TextMessageContent(USER_NOT_EXIST)));

            logLogin(username, NetworkUtil.getIpAddress(socket), authenticated);
        }
        return false;
    }

    /**
     * Handles incoming messages from the client.
     *
     * @param message the received message
     */
    private void handleClientMessage(@NotNull Message message) {
        switch (message) {
            case UserBroadcastMessage ubm:
                server.broadcastMessage(ubm);
                break;
            case UserPrivateMessage upm:
                boolean userExist = server.isUserAlreadyLogin(upm.getReceiver());
                if (!userExist)
                    sendMessage(new SystemReply(new TextMessageContent("User " + upm.getReceiver() + " is not online or existed. Please try again.")));
                else {
                    server.sendPrivateMessage(upm);
                    sendMessage(upm);
                }
                break;
            case SystemRequest sr:
                if (!Objects.equals(username, sr.getUsername()))
                    throw new IllegalStateException("Unexpected value: " + sr);
                handleCommand((String) sr.getContent().getContent());
                break;
            default:
                throw new IllegalStateException("Unexpected value: " + message);
        }
    }

    /**
     * Handles server commands issued by the client.
     *
     * @param command the command to process
     */
    private void handleCommand(@NotNull String command) {
        switch (command.toLowerCase()) {
            case "list":
                sendMessage(new SystemUserList(new TextMessageContent(""), server.getOnlineUsers()));
                sendMessage(new SystemReply(new TextMessageContent("Online users: " + server.getOnlineUsers())));
                break;
            case "quit":
                close(false);
                break;
            default:
                sendMessage(new SystemReply(new TextMessageContent("Unknown command. Available commands: list, quit")));
        }
    }

    /**
     * Sends a message to the client.
     *
     * @param message the message to send
     */
    public void sendMessage(Message message) {
        try {
            out.writeObject(message);
        } catch (IOException e) {
            server.output("Error sending message: " + e.getMessage());
        }
    }

    /**
     * Closes the client connection.
     *
     * @param shutdown true if the server is shutting down, false otherwise
     */
    public void close(boolean shutdown) {
        try {
            if (username != null) {
                server.removeClient(this);
                if (!shutdown)
                    server.broadcastMessage(new SystemBroadcast(new TextMessageContent(username + " has left the chat."), "left", username));
                logLogout(username);
                username = null;
                authenticated = false;
            }
            socket.close();
        } catch (IOException e) {
            server.output("Error closing client connection: " + e.getMessage());
        }
    }

    /**
     * Gets the username associated with this client.
     *
     * @return the username
     */
    public String getUsername() {
        return username;
    }

    /**
     * Checks if the client is authenticated.
     *
     * @return true if authenticated, false otherwise
     */
    public boolean isAuthenticated() {
        return authenticated;
    }
}
