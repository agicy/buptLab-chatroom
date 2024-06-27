package com.chatroom.server;

import com.chatroom.common.Constants;
import com.chatroom.common.message.Message;
import com.chatroom.common.message.UserPrivateMessage;

import java.io.IOException;
import java.net.ServerSocket;
import java.net.Socket;
import java.net.SocketException;
import java.util.List;
import java.util.Objects;
import java.util.concurrent.CopyOnWriteArrayList;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.stream.Collectors;

import static com.chatroom.common.Constants.LOG_FILE;
import static com.chatroom.common.Constants.USER_FILE;

public class Server {
    private final List<ClientHandler> clients;
    private final UserManager userManager;
    private final Logger logger;
    private final ExecutorService pool;
    private ServerSocket serverSocket;
    private boolean running;
    private ServerView serverView;

    public Server() {
        this.userManager = new UserManager(USER_FILE);
        this.logger = new Logger(LOG_FILE);
        this.clients = new CopyOnWriteArrayList<>();
        this.pool = Executors.newFixedThreadPool(10);
        this.running = false;
    }

    public static void main(String[] args) {
        new Server().start();
    }

    public void output(String message) {
        serverView.display(message);
        logger.log(message);
    }

    private void waitForClient() {

        try {
            serverSocket = new ServerSocket(Constants.PORT);
            running = true;
            output("Server started on port " + Constants.PORT);

            while (running) {
                Socket clientSocket = serverSocket.accept();
                ClientHandler clientHandler = new ClientHandler(clientSocket, this);
                clients.add(clientHandler);
                pool.execute(clientHandler);
            }
        } catch (SocketException ignored) {
        } catch (Exception e) {
            output("Error starting server: " + e.getMessage());
        } finally {
            stop();
        }
    }

    public void start() {
        serverView = new ServerView(this);
        new Thread(this::waitForClient).start();
    }

    public void stop() {
        running = false;
        try {
            if (serverSocket != null && !serverSocket.isClosed())
                serverSocket.close();
            for (ClientHandler clientHandler : clients)
                clientHandler.close(true);
            clients.clear();
            pool.shutdown();
        } catch (IOException e) {
            output("Error stopping server: " + e.getMessage());
        }
        System.exit(0);
    }

    public void broadcastMessage(Message message) {
        clients.stream()
                .filter(client -> client != null && client.isAuthenticated())
                .forEach(client -> client.sendMessage(message));
    }

    public void sendPrivateMessage(UserPrivateMessage message) {
        clients.stream()
                .filter(client -> client != null && client.isAuthenticated() && Objects.equals(client.getUsername(), message.getReceiver()))
                .forEach(client -> client.sendMessage(message));
    }

    public void handleServerCommands(String command) {
        switch (command.toLowerCase()) {
            case "list":
                output("Online users: " + getOnlineUsers());
                break;
            case "listall":
                output("All users: " + userManager.getAllUsers());
                break;
            case "quit":
                output("quit");
                stop();
                break;
            default:
                output("Unknown command. Available commands: list, listall, quit");
        }
    }

    public List<String> getOnlineUsers() {
        return clients.stream()
                .filter(client -> client != null && client.isAuthenticated())
                .map(ClientHandler::getUsername)
                .collect(Collectors.toList());
    }

    public boolean isUserAlreadyLogin(String username) {
        return getOnlineUsers().contains(username);
    }

    public UserManager getUserManager() {
        return userManager;
    }

    public void removeClient(ClientHandler clientHandler) {
        clients.remove(clientHandler);
    }

}
