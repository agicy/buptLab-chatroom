package com.chatroom.client;

import com.chatroom.common.Constants;

import javax.swing.*;

public class LoginFrame extends JFrame {
    private final Client client;
    private JTextField usernameField;
    private JPasswordField passwordField;
    private JButton LoginButton;
    private JButton ExitButton;
    private JPanel loginPanel;

    public LoginFrame(Client client) {
        super("简易聊天室");
        this.client = client;
        LoginButton.addActionListener(e -> {
            String username = usernameField.getText();
            String password = new String(passwordField.getPassword());
            String result = client.authenticate(username, password);
            if (result == null) {
                dispose();
                client.loop();
            } else {
                JOptionPane.showMessageDialog(LoginFrame.this, result, "Login Error", JOptionPane.ERROR_MESSAGE);
            }
        });

        ExitButton.addActionListener(e -> System.exit(0));

        setIconImage(new ImageIcon(Constants.ICON_FILE).getImage());
        setContentPane(loginPanel);
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        pack();
        setLocationRelativeTo(null);
        setVisible(true);
    }


}
