package com.chatroom.server;

import java.io.BufferedWriter;
import java.io.FileWriter;
import java.io.IOException;
import java.io.PrintWriter;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;

/**
 * Manages logging operations for the chatroom server.
 */
public class Logger {

    private final String logfile;
    private final DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss");

    /**
     * Creates a new instance of the logger.
     *
     * @param logfile the path to the log file
     */
    public Logger(String logfile) {
        this.logfile = logfile;
    }

    /**
     * Logs a message with a timestamp.
     *
     * @param message the message to log
     */
    public synchronized void log(String message) {
        String logMessage = String.format("[%s] %s", LocalDateTime.now().format(formatter), message);
        writeToFile(logMessage);
    }

    /**
     * Writes the log message to the log file.
     *
     * @param message the log message to write
     */
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