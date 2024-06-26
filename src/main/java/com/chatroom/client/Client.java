package com.chatroom.client;

import com.chatroom.common.Constants;
import com.chatroom.common.message.*;
import org.jetbrains.annotations.NotNull;

import javax.swing.*;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.net.Socket;
import java.util.List;
import java.util.Objects;

import static com.chatroom.common.message.SystemReply.LOGIN_SUCCESS;

public class Client {
    private Socket socket;
    private ObjectOutputStream out;
    private ObjectInputStream in;
    private String username;
    private boolean isAnonymous;
    private ChatRoom chatRoom;

    public Client() {
        this.isAnonymous = false;
    }

    public static void main(String[] args) {
        new Client().login();
    }

    public void setAnonymous(boolean anonymous) {
        isAnonymous = anonymous;
        chatRoom.setAnonymous(anonymous);
    }

    public void sendSystemRequest(MessageContent content) {
        try {
            out.writeObject(new SystemRequest(username, content));
        } catch (IOException e) {
            System.err.println("Error sending system request: " + e.getMessage());
        }
    }

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
                chatRoom.displayMessage("Current chat mode: " + (isAnonymous ? "Anonymous" : "Named"));
                break;
            case "anonymous":
                setAnonymous(!isAnonymous);
                chatRoom.displayMessage("Chat mode changed to: " + (isAnonymous ? "Anonymous" : "Named"));
                break;
            default:
                chatRoom.displayMessage("Unknown command. Available commands: list, quit, showanonymous, anonymous");
        }
    }

    public String getUsername() {
        return username;
    }

    public void login() {
        try {
            socket = new Socket(Constants.HOST, Constants.PORT);
            out = new ObjectOutputStream(socket.getOutputStream());
            in = new ObjectInputStream(socket.getInputStream());
            new LoginFrame(this);
        } catch (IOException e) {
            System.err.println("Error connecting to server: " + e.getMessage());
        }
    }

    public void loop() {
        SwingWorker<Void, Message> worker = new SwingWorker<>() {
            @Override
            protected Void doInBackground() {
                try {
                    Message serverMessage;
                    while ((serverMessage = (Message) in.readObject()) != null) {
                        publish(serverMessage);
                    }
                } catch (ClassNotFoundException | IOException e) {
                    System.err.println("Error during the main loop: " + e.getMessage());
                }
                return null;
            }

            @Override
            protected void process(List<Message> chunks) {
                // 处理接收到的消息，例如更新聊天界面
                for (Message message : chunks) {
                    handleServerMessage(message);
                }
            }
        };

        worker.execute();
        chatRoom = new ChatRoom(this);
        chatRoom.setAnonymous(isAnonymous);
        sendSystemRequest(new TextMessageContent("list"));
    }

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

    public void handleUserInput(@NotNull String content) {
        if (content.isEmpty()) {
            JOptionPane.showMessageDialog(chatRoom, "输入不能为空", "发送错误", JOptionPane.ERROR_MESSAGE);
            return;
        }
        if (content.startsWith("@@"))
            handleCommand(content.substring(2));
        else
            handleUserMessage(content);
    }

    public void handleUserMessage(String content) {
        Message message;
        if (content.startsWith("@")) {
            int spaceIndex = content.indexOf(' ');
            if (spaceIndex != -1) {
                String recipient = content.substring(1, spaceIndex);
                if (recipient.equals(username)) {
                    JOptionPane.showMessageDialog(chatRoom, "不能给自己发私信", "发送错误", JOptionPane.ERROR_MESSAGE);
                    return;
                }
                String privateMessage = content.substring(spaceIndex + 1);
                message = new UserPrivateMessage(username, isAnonymous, recipient, new TextMessageContent(privateMessage));
            } else {
                JOptionPane.showMessageDialog(chatRoom, "消息不能为空", "发送错误", JOptionPane.ERROR_MESSAGE);
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
        if (message instanceof SystemBroadcast sb) {
            if (Objects.equals(sb.getType(), "join"))
                chatRoom.addUser(sb.getUsername());
            if (Objects.equals(sb.getType(), "left"))
                chatRoom.delUser(sb.getUsername());
        }
        if (message instanceof SystemUserList sul) {
            System.out.println("received SystemUserList");
            System.out.println(sul.getUsers());
            chatRoom.setUserList(sul.getUsers());
            return;
        }
        chatRoom.addTextMessage(message, username);
    }


}
