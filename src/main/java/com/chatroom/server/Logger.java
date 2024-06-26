package com.chatroom.server;

import java.io.BufferedWriter;
import java.io.FileWriter;
import java.io.IOException;
import java.io.PrintWriter;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;

public class Logger {
    private final String logfile ;
    private final DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss");

    public Logger(String logfile) {
        this.logfile=logfile;
    }
    public void log(String message) {
        String logMessage = String.format("[%s] %s", LocalDateTime.now().format(formatter), message);
        System.out.println(logMessage);
        writeToFile(logMessage);
    }

    public void logLogin(String username, String ip, boolean success) {
        String status = success ? "successful" : "failed";
        log(String.format("Login %s for user %s from IP %s", status, username, ip));
    }

    public void logLogout(String username) {
        log(String.format("User %s logged out", username));
    }

    private void writeToFile(String message) {
        try (FileWriter fw = new FileWriter(logfile, true);
             BufferedWriter bw = new BufferedWriter(fw);
             PrintWriter out = new PrintWriter(bw)) {
            out.println(message);
        } catch (IOException e) {
            System.err.println("Error writing to log file: " + e.getMessage());
        }
    }
}