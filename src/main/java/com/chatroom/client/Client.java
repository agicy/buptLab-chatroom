/**
 * The `Client` class represents a chatroom client that communicates with the server.
 * It provides functionality for authentication, sending system requests, handling commands,
 * and managing the chat mode (anonymous or named).
 */
package com.chatroom.client;

import com.chatroom.common.message.*;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import javax.swing.*;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.net.Socket;
import java.util.List;
import java.util.Objects;

import static com.chatroom.common.message.SystemReply.LOGIN_SUCCESS;

/**
 * Represents a chatroom client.
 */
public class Client {

    private Socket socket;
    private ObjectOutputStream out;
    private ObjectInputStream in;
    private String username;
    private boolean isAnonymous;
    private ClientView clientView;

    /**
     * Constructs a new `Client` instance.
     */
    public Client() {
        this.isAnonymous = false;
    }

    /**
     * Main method to start the chatroom client.
     *
     * @param args Command-line arguments (not used).
     */
    public static void main(String[] args) {
        new Client().login();
    }

    /**
     * Sets the chat mode (anonymous or named).
     *
     * @param anonymous `true` if the client is in anonymous mode, `false` otherwise.
     */
    public void setAnonymous(boolean anonymous) {
        isAnonymous = anonymous;
        clientView.setAnonymous(anonymous);
    }

    /**
     * Sends a system request to the server.
     *
     * @param content The message content to send.
     */
    public void sendSystemRequest(MessageContent content) {
        try {
            out.writeObject(new SystemRequest(username, content));
        } catch (IOException e) {
            System.err.println("Error sending system request: " + e.getMessage());
        }
    }

    /**
     * Authenticates the client with the server.
     *
     * @param username The username to authenticate.
     * @param password The password for authentication.
     * @return `null` if authentication is successful, an error message otherwise.
     */
    public String authenticate(String username, String password) {
        this.username = username;
        sendSystemRequest(new TextMessageContent(username));
        sendSystemRequest(new TextMessageContent(password));
        try {
            SystemReply serverMessage = (SystemReply) in.readObject();
            String result = (String) serverMessage.getContent().getContent();
            if (Objects.equals(result, LOGIN_SUCCESS)) {
                return null;
            } else
                return result;
        } catch (IOException | ClassNotFoundException e) {
            throw new RuntimeException(e);
        }
    }

    /**
     * Handles a command from the user.
     *
     * @param command The command to handle.
     */
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
                clientView.displayMessage("Current chat mode: " + (isAnonymous ? "Anonymous" : "Named"));
                break;
            case "anonymous":
                setAnonymous(!isAnonymous);
                clientView.displayMessage("Chat mode changed to: " + (isAnonymous ? "Anonymous" : "Named"));
                break;
            default:
                clientView.displayMessage("Unknown command. Available commands: list, quit, showanonymous, anonymous");
        }
    }

    /**
     * Gets the current username.
     *
     * @return The username.
     */
    public String getUsername() {
        return username;
    }

    /**
     * Connects to the server.
     *
     * @param address The server address.
     * @param port    The server port.
     * @param isTest  `true` if in test mode, `false` otherwise.
     * @return `true` if connection is successful, `false` otherwise.
     */
    public boolean connect(String address, String port, boolean isTest) {
        try {
            socket = new Socket(address, Integer.parseInt(port));
            out = new ObjectOutputStream(socket.getOutputStream());
            in = new ObjectInputStream(socket.getInputStream());
            if (isTest) {
                in.close();
                in = null;
                out.close();
                out = null;
                socket.close();
                socket = null;
            }
        } catch (IOException e) {
            System.err.println("Error connecting to " + address + ":" + port);
            return false;
        }
        return true;
    }

    /**
     * Initiates the login process.
     */
    public void login() {
        new LoginFrame(this);
    }

    /**
     * Sets up a background worker to handle server messages and initializes the client view.
     */
    public void loop() {
        SwingWorker<Void, Message> worker = new SwingWorker<>() {
            @Override
            protected @Nullable Void doInBackground() {
                try {
                    Message serverMessage;
                    while ((serverMessage = (Message) in.readObject()) != null)
                        publish(serverMessage);
                } catch (Exception ignored) {
                    JOptionPane.showMessageDialog(clientView, "服务器连接错误", "服务器连接错误", JOptionPane.ERROR_MESSAGE);
                    stop();
                }
                return null;
            }

            @Override
            protected void process(@NotNull List<Message> chunks) {
                for (Message message : chunks)
                    handleServerMessage(message);
            }
        };
        worker.execute();
        clientView = new ClientView(this);
        clientView.setAnonymous(isAnonymous);
        sendSystemRequest(new TextMessageContent("list"));
    }

    /**
     * Stops the client by closing connections and exiting the application.
     */
    public void stop() {
        try {
            in.close();
            out.close();
            socket.close();
        } catch (IOException e) {
            System.err.println("Error closing client connection: " + e.getMessage());
        }
        System.exit(0);
    }

    /**
     * Handles user input, including commands and messages.
     *
     * @param content The user input content.
     */
    public void handleUserInput(@NotNull String content) {
        if (content.isEmpty()) {
            JOptionPane.showMessageDialog(clientView, "输入不能为空", "发送错误", JOptionPane.ERROR_MESSAGE);
            return;
        }
        if (content.startsWith("@@"))
            handleCommand(content.substring(2));
        else
            handleUserMessage(content);
    }

    /**
     * Handles user messages (broadcast or private) and sends them to the server.
     *
     * @param content The user message content.
     */
    public void handleUserMessage(@NotNull String content) {
        Message message;
        if (content.startsWith("@")) {
            int spaceIndex = content.indexOf(' ');
            if (spaceIndex != -1) {
                String recipient = content.substring(1, spaceIndex);
                if (recipient.equals(username)) {
                    JOptionPane.showMessageDialog(clientView, "不能给自己发私信", "发送错误", JOptionPane.ERROR_MESSAGE);
                    return;
                }
                String privateMessage = content.substring(spaceIndex + 1);
                message = new UserPrivateMessage(username, isAnonymous, recipient, new TextMessageContent(privateMessage));
            } else {
                JOptionPane.showMessageDialog(clientView, "消息不能为空", "发送错误", JOptionPane.ERROR_MESSAGE);
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

    /**
     * Handles a server message received from the server.
     *
     * @param message The server message to handle.
     */
    public void handleServerMessage(Message message) {
        if (message instanceof SystemBroadcast sb) {
            if (Objects.equals(sb.getType(), "join"))
                clientView.addUser(sb.getUsername());
            if (Objects.equals(sb.getType(), "left"))
                clientView.delUser(sb.getUsername());
        }
        if (message instanceof SystemUserList sul) {
            clientView.setUserList(sul.getUsers());
            return;
        }
        clientView.addTextMessage(message, username);
    }
}
