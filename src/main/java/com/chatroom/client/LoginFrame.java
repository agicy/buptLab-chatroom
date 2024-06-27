package com.chatroom.client;

import com.chatroom.common.Constants;

import javax.swing.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;

/**
 * The login frame for the chatroom application.
 */
public class LoginFrame extends JFrame {

    private JTextField usernameField;
    private JPasswordField passwordField;
    private JButton LoginButton;
    private JButton ExitButton;
    private JPanel loginPanel;
    private JTextField serverAddressField;
    private JTextField portField;
    private JLabel usernameLabel;
    private JLabel passwordLabel;
    private JLabel serverAddressLabel;
    private JLabel portLabel;
    private JButton testButton;
    private JLabel picLabel;

    /**
     * Creates a new instance of the login frame.
     *
     * @param client the associated client
     */
    public LoginFrame(Client client) {
        super("简易聊天室");
        LoginButton.addActionListener(e -> {
            if (client.connect(serverAddressField.getText(), portField.getText(), false)) {
                String username = usernameField.getText();
                String password = new String(passwordField.getPassword());
                String result = client.authenticate(username, password);
                if (result == null) {
                    dispose();
                    client.loop();
                } else
                    JOptionPane.showMessageDialog(LoginFrame.this, result, "登录失败", JOptionPane.ERROR_MESSAGE);
            } else
                JOptionPane.showMessageDialog(LoginFrame.this, "服务器连接错误", "连接错误", JOptionPane.ERROR_MESSAGE);

        });
        ExitButton.addActionListener(e -> System.exit(0));
        setIconImage(new ImageIcon(Constants.ICON_FILE).getImage());
        setContentPane(loginPanel);
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        pack();
        setLocationRelativeTo(null);
        setVisible(true);
        testButton.addActionListener(new ActionListener() {
            /**
             * Invoked when an action occurs.
             *
             * @param e the event to be processed
             */
            @Override
            public void actionPerformed(ActionEvent e) {
                if (client.connect(serverAddressField.getText(), portField.getText(), true))
                    JOptionPane.showMessageDialog(LoginFrame.this, "服务器连接成功，请输入账号密码", "测试连接成功", JOptionPane.INFORMATION_MESSAGE);
                else
                    JOptionPane.showMessageDialog(LoginFrame.this, "服务器连接错误", "连接错误", JOptionPane.ERROR_MESSAGE);
            }
        });
    }
}
