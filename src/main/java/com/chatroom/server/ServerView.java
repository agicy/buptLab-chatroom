package com.chatroom.server;

import com.chatroom.common.Constants;

import javax.swing.*;
import javax.swing.text.BadLocationException;
import javax.swing.text.StyledDocument;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;

public class ServerView extends JFrame {
    private final Server server;
    private JTextPane messagePanel;
    private JPanel serverPanel;
    private JTextField inputField;
    private JButton sendButton;
    private JLabel inputLabel;
    private JScrollPane messageOuterPanel;

    public ServerView(Server server) {
        this.server = server;
        inputField.addActionListener(new ActionListener() {
            /**
             * Invoked when an action occurs.
             *
             * @param e the event to be processed
             */
            @Override
            public void actionPerformed(ActionEvent e) {
                send();
            }
        });
        sendButton.addActionListener(new ActionListener() {
            /**
             * Invoked when an action occurs.
             *
             * @param e the event to be processed
             */
            @Override
            public void actionPerformed(ActionEvent e) {
                send();
            }
        });


        setIconImage(new ImageIcon(Constants.ICON_FILE).getImage());
        setContentPane(serverPanel);
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        pack();
        setLocationRelativeTo(null);
        setVisible(true);
    }

    private void send() {
        String content = inputField.getText().trim();
        server.handleServerCommands(content);
        inputField.setText("");
    }

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
