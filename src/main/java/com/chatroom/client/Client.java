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

public class Client {
    private Socket socket;
    private ObjectOutputStream out;
    private ObjectInputStream in;
    private String username;
    private boolean isAnonymous;
    private ClientView clientView;

    public Client() {
        this.isAnonymous = false;
    }

    public static void main(String[] args) {
        new Client().login();
    }

    public void setAnonymous(boolean anonymous) {
        isAnonymous = anonymous;
        clientView.setAnonymous(anonymous);
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

    public String getUsername() {
        return username;
    }

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

    public void login() {

        new LoginFrame(this);
    }

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
                // 处理接收到的消息，例如更新聊天界面
                for (Message message : chunks) {
                    handleServerMessage(message);
                }
            }
        };

        worker.execute();
        clientView = new ClientView(this);
        clientView.setAnonymous(isAnonymous);
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
            JOptionPane.showMessageDialog(clientView, "输入不能为空", "发送错误", JOptionPane.ERROR_MESSAGE);
            return;
        }
        if (content.startsWith("@@"))
            handleCommand(content.substring(2));
        else
            handleUserMessage(content);
    }

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
