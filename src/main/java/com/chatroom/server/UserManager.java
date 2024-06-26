package com.chatroom.server;

import java.io.BufferedReader;
import java.io.FileReader;
import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class UserManager {
    private final Map<String, String> users;

    public UserManager(String userFilePath) {
        users = new HashMap<>();
        loadUsers(userFilePath);
    }

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

    public boolean authenticate(String username, String password) {
        return users.containsKey(username) && users.get(username).equals(password);
    }

    public List<String> getAllUsers() {
        return new ArrayList<>(users.keySet());
    }
}