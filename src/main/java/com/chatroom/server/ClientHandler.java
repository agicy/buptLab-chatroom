package com.chatroom.server;

import com.chatroom.common.message.*;
import com.chatroom.util.NetworkUtil;
import org.jetbrains.annotations.NotNull;

import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.net.Socket;
import java.util.Objects;

import static com.chatroom.common.message.SystemReply.*;

public class ClientHandler implements Runnable {
    private final Socket socket;
    private final Server server;
    private ObjectOutputStream out;
    private ObjectInputStream in;
    private String username;
    private boolean authenticated = false;

    public ClientHandler(Socket socket, Server server) {
        this.socket = socket;
        this.server = server;
    }

    @Override
    public void run() {
        try {
            out = new ObjectOutputStream(socket.getOutputStream());
            in = new ObjectInputStream(socket.getInputStream());

            authenticate();

            Message inputMessage;
            while ((inputMessage = (Message) in.readObject()) != null) {
                handleClientMessage(inputMessage);
                if (socket.isClosed())
                    return;
            }
        } catch (IOException | ClassNotFoundException e) {
            server.getLogger().log("Error handling client: " + e.getMessage());
        } finally {
            close();
        }
    }

    private void authenticate() throws IOException, ClassNotFoundException {
        String username, password;
        while (!authenticated) {
            username = (String) ((SystemRequest) in.readObject()).getContent().getContent();
            password = (String) ((SystemRequest) in.readObject()).getContent().getContent();

            if (server.getUserManager().isUserExist(username)) {
                if (server.getUserManager().authenticate(username, password)) {
                    if (!server.isUserAlreadyLogin(username)) {
                        authenticated = true;
                        this.username = username;
                        out.writeObject(new SystemReply(new TextMessageContent(LOGIN_SUCCESS)));
                        out.writeObject(new SystemReply(new TextMessageContent("Authentication successful. Welcome to the chat room!")));
                        server.broadcastMessage(new SystemBroadcast(new TextMessageContent(username + " has joined the chat."), "join", username));
                    } else
                        out.writeObject(new SystemReply(new TextMessageContent(ALREADY_LOGIN)));
                } else
                    out.writeObject(new SystemReply(new TextMessageContent(PASSWORD_INCORRECT)));
            } else
                out.writeObject(new SystemReply(new TextMessageContent(USER_NOT_EXIST)));

            server.getLogger().logLogin(username, NetworkUtil.getIpAddress(socket), authenticated);

        }
    }

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

    private void handleCommand(@NotNull String command) {
        switch (command.toLowerCase()) {
            case "list":
                System.out.println(server.getOnlineUsers());
                sendMessage(new SystemUserList(new TextMessageContent(""), server.getOnlineUsers()));
                sendMessage(new SystemReply(new TextMessageContent("Online users: " + server.getOnlineUsers())));
                break;
            case "quit":
                close();
                break;
            default:
                sendMessage(new SystemReply(new TextMessageContent("Unknown command. Available commands: list, quit")));
        }
    }

    public void sendMessage(Message message) {
        try {
            out.writeObject(message);
        } catch (IOException e) {
            server.getLogger().log("Error sending message: " + e.getMessage());
        }
    }

    public void close() {
        try {
            server.removeClient(this);
            socket.close();
            if (username != null) {
                server.broadcastMessage(new SystemBroadcast(new TextMessageContent(username + " has left the chat."), "left", username));
                server.getLogger().logLogout(username);
                username = null;
                authenticated = false;
            }
        } catch (IOException e) {
            server.getLogger().log("Error closing client connection: " + e.getMessage());
        }
    }

    public String getUsername() {
        return username;
    }

    public boolean isAuthenticated() {
        return authenticated;
    }
}
