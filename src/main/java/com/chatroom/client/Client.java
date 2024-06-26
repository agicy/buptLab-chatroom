package com.chatroom.client;

import com.chatroom.common.Constants;
import com.chatroom.common.message.*;
import org.jetbrains.annotations.NotNull;

import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.net.Socket;
import java.net.SocketException;
import java.util.Objects;

import static com.chatroom.common.message.SystemReply.LOGIN_SUCCESS;

public class Client {
    private Socket socket;
    private ObjectOutputStream out;
    private ObjectInputStream in;
    private String username;
    private boolean login = false;
    private boolean isAnonymous;
    private ClientGUI gui;

    public Client() {
        this.isAnonymous = false;
    }

    public static void main(String[] args) {
        new Client().start();
    }

    public boolean isLogin() {
        return login;
    }

    public void start() {
        try {
            socket = new Socket(Constants.HOST, Constants.PORT);
            out = new ObjectOutputStream(socket.getOutputStream());
            in = new ObjectInputStream(socket.getInputStream());

            gui = new ClientGUI(this);

            while (!login)
                gui.showLoginDialog();

            gui.setVisible(true);

            Message serverMessage;
            while ((serverMessage = (Message) in.readObject()) != null) {
                handleServerMessage(serverMessage);
            }
        } catch (SocketException ignored) {
        } catch (IOException | ClassNotFoundException e) {
            System.err.println("Error connecting to server: " + e.getMessage());
        } finally {
            stop();
        }
    }

    public void stop() {
        try {
            in.close();
            out.close();
            socket.close();
            login = false;
        } catch (IOException e) {
            System.err.println("Error closing client connection: " + e.getMessage());
        }
        System.exit(0);
    }

    public void sendUserMessage(@NotNull String content) {
        Message message;
        if (content.startsWith("@")) {
            int spaceIndex = content.indexOf(' ');
            if (spaceIndex != -1) {
                String recipient = content.substring(1, spaceIndex);
                if (recipient.equals(username)) {
                    gui.displayMessage("You cannot send private messages to yourself");
                    return;
                }
                String privateMessage = content.substring(spaceIndex + 1);
                message = new UserPrivateMessage(username, isAnonymous, recipient, new TextMessageContent(privateMessage));
            } else {
                gui.displayMessage("Message cannot be empty");
                return;
            }
        } else
            message = new UserBroadcastMessage(username, isAnonymous, new TextMessageContent(content));

        try {
            out.writeObject(message);
        } catch (IOException e) {
            System.err.println("Error sending message: " + e.getMessage());
        }
    }

    public void handleServerMessage(Message message) {
        if (login)
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
        try {
            SystemReply serverMessage = (SystemReply) in.readObject();
            String result = (String) serverMessage.getContent().getContent();
            if (Objects.equals(result, LOGIN_SUCCESS))
                login = true;
            else
                gui.showLoginErrorMessage(result);
        } catch (IOException | ClassNotFoundException e) {
            throw new RuntimeException(e);
        }
    }

    public void handleCommand(@NotNull String command) {
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
