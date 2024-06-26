package com.chatroom.server;

import com.chatroom.common.Constants;
import com.chatroom.common.message.Message;
import com.chatroom.common.message.UserPrivateMessage;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.ServerSocket;
import java.net.Socket;
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

    public void start() {
        try {
            serverSocket = new ServerSocket(Constants.PORT);
            running = true;
            logger.log("Server started on port " + Constants.PORT);

            new Thread(this::handleServerCommands).start();

            while (running) {
                Socket clientSocket = serverSocket.accept();
                ClientHandler clientHandler = new ClientHandler(clientSocket, this);
                clients.add(clientHandler);
                pool.execute(clientHandler);
            }
        } catch (IOException e) {
            logger.log("Error starting server: " + e.getMessage());
        } finally {
            stop();
        }
    }

    public void stop() {
        running = false;
        try {
            if (serverSocket != null && !serverSocket.isClosed()) {
                serverSocket.close();
            }
            pool.shutdown();
            clients.forEach(ClientHandler::close);
        } catch (IOException e) {
            logger.log("Error stopping server: " + e.getMessage());
        }
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

    private void handleServerCommands() {
        try (BufferedReader reader = new BufferedReader(new InputStreamReader(System.in))) {
            String command;
            while (running && (command = reader.readLine()) != null) {
                switch (command.toLowerCase()) {
                    case "list":
                        System.out.println("Online users: " + getOnlineUsers());
                        break;
                    case "listall":
                        System.out.println("All users: " + userManager.getAllUsers());
                        break;
                    case "quit":
                        stop();
                        break;
                    default:
                        System.out.println("Unknown command. Available commands: list, listall, quit");
                }
            }
        } catch (IOException e) {
            logger.log("Error reading server commands: " + e.getMessage());
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

    public Logger getLogger() {
        return logger;
    }

    public void removeClient(ClientHandler clientHandler) {
        clients.remove(clientHandler);
    }
}
