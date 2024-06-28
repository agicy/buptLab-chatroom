package com.chatroom.server;

import com.chatroom.common.Constants;
import com.chatroom.common.message.Message;
import com.chatroom.common.message.UserPrivateMessage;
import org.jetbrains.annotations.NotNull;

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

/**
 * Represents a chat server that handles client connections and messages.
 */
public class Server {

    private final List<ClientHandler> clients;
    private final UserManager userManager;
    private final Logger logger;
    private final ExecutorService pool;
    private ServerSocket serverSocket;
    private boolean running;
    private ServerView serverView;

    /**
     * Constructs a new Server instance.
     */
    public Server() {
        this.userManager = new UserManager(USER_FILE);
        this.logger = new Logger(LOG_FILE);
        this.clients = new CopyOnWriteArrayList<>();
        this.pool = Executors.newCachedThreadPool();
        this.running = false;
    }

    /**
     * Main method to start the server.
     *
     * @param args Command-line arguments (not used)
     */
    public static void main(String[] args) {
        new Server().start();
    }

    /**
     * Displays a message on the server view and logs it.
     *
     * @param message The message to display and log
     */
    public void output(String message) {
        serverView.display(message);
        logger.log(message);
    }

    /**
     * Waits for client connections and handles them.
     */
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

    /**
     * Starts the server by initializing the server view and waiting for clients.
     */
    public void start() {
        serverView = new ServerView(this);
        new Thread(this::waitForClient).start();
    }

    /**
     * Stops the server by closing sockets and shutting down the thread pool.
     */
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

    /**
     * Broadcasts a message to all authenticated clients.
     *
     * @param message The message to broadcast
     */
    public void broadcastMessage(Message message) {
        clients.stream()
                .filter(client -> client != null && client.isAuthenticated())
                .forEach(client -> client.sendMessage(message));
    }

    /**
     * Sends a private message to a specific user.
     *
     * @param message The private message to send
     */
    public void sendPrivateMessage(UserPrivateMessage message) {
        clients.stream()
                .filter(client -> client != null && client.isAuthenticated() && Objects.equals(client.getUsername(), message.getReceiver()))
                .forEach(client -> client.sendMessage(message));
    }

    /**
     * Handles server commands based on user input.
     *
     * @param command The command to process
     */
    public void handleServerCommands(@NotNull String command) {
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

    /**
     * Retrieves a list of online users.
     *
     * @return List of online usernames
     */
    public List<String> getOnlineUsers() {
        return clients.stream()
                .filter(client -> client != null && client.isAuthenticated())
                .map(ClientHandler::getUsername)
                .collect(Collectors.toList());
    }

    /**
     * Checks if a user is already logged in.
     *
     * @param username The username to check
     * @return True if the user is already logged in, false otherwise
     */
    public boolean isUserAlreadyLogin(String username) {
        return getOnlineUsers().contains(username);
    }

    /**
     * Gets the user manager instance.
     *
     * @return The user manager
     */
    public UserManager getUserManager() {
        return userManager;
    }

    /**
     * Removes a client handler from the list of connected clients.
     *
     * @param clientHandler The client handler to remove
     */
    public void removeClient(ClientHandler clientHandler) {
        clients.remove(clientHandler);
    }

}
