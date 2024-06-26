package com.chatroom.server;

import java.io.BufferedReader;
import java.io.FileReader;
import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Manages user authentication and user-related operations in the chatroom application.
 */
public class UserManager {

    private final Map<String, String> users;

    /**
     * Creates a new instance of the user manager.
     *
     * @param userFilePath the path to the user file containing account information
     */
    public UserManager(String userFilePath) {
        users = new HashMap<>();
        loadUsers(userFilePath);
    }

    /**
     * Loads user data from the specified user file.
     *
     * @param userFilePath the path to the user file
     */
    private void loadUsers(String userFilePath) {
        try (BufferedReader reader = new BufferedReader(new FileReader(userFilePath))) {
            String line;
            while ((line = reader.readLine()) != null) {
                String[] parts = line.split(":");
                if (parts.length == 2) {
                    users.put(parts[0].trim(), parts[1].trim());
                }
            }
        } catch (IOException e) {
            System.err.println("Error loading user file: " + e.getMessage());
        }
    }

    /**
     * Authenticates a user based on the provided username and password.
     *
     * @param username the username to authenticate
     * @param password the password to verify
     * @return true if authentication is successful, false otherwise
     */
    public boolean authenticate(String username, String password) {
        return users.containsKey(username) && users.get(username).equals(password);
    }

    /**
     * Retrieves a list of all registered usernames.
     *
     * @return a list of usernames
     */
    public List<String> getAllUsers() {
        return new ArrayList<>(users.keySet());
    }

    /**
     * Checks if a user with the specified username exists.
     *
     * @param username the username to check
     * @return true if the user exists, false otherwise
     */
    public boolean isUserExist(String username) {
        return users.containsKey(username);
    }
}