package com.chatroom.server;

import com.chatroom.common.Constants;

import javax.swing.*;
import javax.swing.text.BadLocationException;
import javax.swing.text.StyledDocument;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;

/**
 * Represents the server view for the chatroom application.
 */
public class ServerView extends JFrame {

    private final Server server;
    private JTextPane messagePanel;
    private JPanel serverPanel;
    private JTextField inputField;
    private JButton sendButton;
    private JLabel inputLabel;
    private JScrollPane messageOuterPanel;

    /**
     * Creates a new instance of the server view.
     *
     * @param server the associated server
     */
    public ServerView(Server server) {
        super("简易聊天室-服务器");
        this.server = server;

        // Set up action listeners for input field and send button
        inputField.addActionListener(e -> send());
        sendButton.addActionListener(e -> send());

        // Set icon image and configure frame
        setIconImage(new ImageIcon(Constants.ICON_FILE).getImage());
        setContentPane(serverPanel);
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        pack();
        setLocationRelativeTo(null);
        setVisible(true);
    }

    /**
     * Sends the input content to the server for handling.
     */
    private void send() {
        String content = inputField.getText().trim();
        server.handleServerCommands(content);
        inputField.setText("");
    }

    /**
     * Displays a log message in the message panel.
     *
     * @param message the log message to display
     */
    public void display(String message) {
        String logMessage = String.format("[%s] %s\n", LocalDateTime.now().format(DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss")), message);
        StyledDocument doc = messagePanel.getStyledDocument();
        try {
            doc.insertString(doc.getLength(), logMessage, null);
        } catch (BadLocationException e) {
            throw new RuntimeException(e);
        }
    }
}
