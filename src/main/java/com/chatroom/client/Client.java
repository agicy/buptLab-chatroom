package com.chatroom.client;

import com.chatroom.common.Constants;
import com.chatroom.common.message.*;
import com.chatroom.util.NetworkUtil;

import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.net.Socket;

public class Client {
    private Socket socket;
    private ObjectOutputStream out;
    private String username;
    private boolean isAnonymous;
    private ClientGUI gui;

    public Client() {
        this.isAnonymous = false;
    }

    public static void main(String[] args) {
        new Client().start();
    }

    public void start() {
        try {
            if(!NetworkUtil.isValidIpAddress(Constants.HOST))
                throw new IllegalArgumentException("Invalid IP address");
            socket = new Socket(Constants.HOST, Constants.PORT);
            out = new ObjectOutputStream(socket.getOutputStream());
            ObjectInputStream in = new ObjectInputStream(socket.getInputStream());

            gui = new ClientGUI(this);
            gui.setVisible(true);

            Message serverMessage;
            while ((serverMessage = (Message) in.readObject()) != null) {
                handleServerMessage(serverMessage);
            }
        } catch (IOException | ClassNotFoundException e) {
            System.err.println("Error connecting to server: " + e.getMessage());
        } finally {
            stop();
        }
    }

    public void stop() {
        try {
            if (socket != null && !socket.isClosed()) {
                socket.close();
            }
        } catch (IOException e) {
            System.err.println("Error closing client connection: " + e.getMessage());
        }
    }

    public void sendUserMessage(String content) {
        Message message;
        if (content.startsWith("@")) {
            int spaceIndex = content.indexOf(' ');
            if (spaceIndex != -1) {
                String recipient = content.substring(1, spaceIndex);
                String privateMessage = content.substring(spaceIndex + 1);
                message = new UserPrivateMessage(username, isAnonymous, recipient, new TextMessageContent(privateMessage));
            } else
                return;
        } else
            message = new UserBroadcastMessage(username, isAnonymous, new TextMessageContent(content));

        try {
            out.writeObject(message);
        } catch (IOException e) {
            System.err.println("Error sending message: " + e.getMessage());
        }
    }

    public void handleServerMessage(Message message) {
        gui.displayMessage(Messages.getMessagePrefix(message, username) + Messages.getMessageContent(message));
    }

    public void sendSystemRequest(MessageContent content) {
        try {
            out.writeObject(new SystemRequest(username, content));
        } catch (IOException e) {
            System.err.println("Error sending system request: " + e.getMessage());
        }
    }

    public void authenticate(String username, String password) {
        this.username = username;
        sendSystemRequest(new TextMessageContent(username));
        sendSystemRequest(new TextMessageContent(password));
    }

    public void handleCommand(String command) {
        switch (command.toLowerCase()) {
            case "list":
                sendSystemRequest(new TextMessageContent("list"));
                break;
            case "quit":
                sendSystemRequest(new TextMessageContent("quit"));
                stop();
                System.exit(0);
                break;
            case "showanonymous":
                gui.displayMessage("Current chat mode: " + (isAnonymous ? "Anonymous" : "Named"));
                break;
            case "anonymous":
                isAnonymous = !isAnonymous;
                gui.displayMessage("Chat mode changed to: " + (isAnonymous ? "Anonymous" : "Named"));
                break;
            default:
                gui.displayMessage("Unknown command. Available commands: list, quit, showanonymous, anonymous");
        }
    }
}
