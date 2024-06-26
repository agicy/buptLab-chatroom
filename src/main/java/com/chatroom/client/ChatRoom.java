package com.chatroom.client;

import com.chatroom.common.Constants;
import com.chatroom.common.message.Message;
import com.chatroom.common.message.Messages;
import com.chatroom.common.message.SystemMessage;
import com.chatroom.common.message.UserPrivateMessage;

import javax.swing.*;
import javax.swing.event.ListSelectionEvent;
import javax.swing.event.ListSelectionListener;
import javax.swing.text.BadLocationException;
import javax.swing.text.SimpleAttributeSet;
import javax.swing.text.StyleConstants;
import javax.swing.text.StyledDocument;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;
import java.util.List;

public class ChatRoom extends JFrame {
    private final Client client;
    private final DefaultListModel<String> onlineUserListModel = new DefaultListModel<>();
    private JPanel chatRoom;
    private JTextField inputField;
    private JButton sendButton;
    private JLabel currentUsername;
    private JComboBox<String> anonymousSelect;
    private JList<String> onlineUser;
    private JButton logoutButton;
    private JTextPane chatContent;

    public ChatRoom(Client client) {
        super("简易聊天室-主界面");
        this.client = client;
        currentUsername.setText("当前用户: " + client.getUsername());

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
        setContentPane(chatRoom);
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        pack();
        setVisible(true);
        anonymousSelect.addActionListener(new ActionListener() {
            /**
             * Invoked when an action occurs.
             *
             * @param e the event to be processed
             */
            @Override
            public void actionPerformed(ActionEvent e) {
                client.setAnonymous(anonymousSelect.getSelectedIndex() == 1);
            }
        });
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

        addWindowListener(new WindowAdapter() {
            @Override
            public void windowClosing(WindowEvent e) {
                client.handleCommand("quit");
                client.stop();
            }
        });
        onlineUser.addListSelectionListener(new ListSelectionListener() {
            /**
             * Called whenever the value of the selection changes.
             *
             * @param e the event that characterizes the change.
             */
            @Override
            public void valueChanged(ListSelectionEvent e) {
                if (!onlineUser.isSelectionEmpty()) {
                    String selectedItem = onlineUser.getSelectedValue();
                    if (inputField.getText().startsWith("@")) {
                        JOptionPane.showMessageDialog(chatRoom, "输入框已经以 @ 开头", "操作错误", JOptionPane.ERROR_MESSAGE);
                    } else {
                        System.out.println("Selected item: " + selectedItem);
                        inputField.setText("@" + selectedItem + " " + inputField.getText());
                    }
                    onlineUser.clearSelection();
                }
            }
        });
        logoutButton.addActionListener(new ActionListener() {
            /**
             * Invoked when an action occurs.
             *
             * @param e the event to be processed
             */
            @Override
            public void actionPerformed(ActionEvent e) {
                client.handleCommand("quit");
                client.stop();
            }
        });
    }

    private void send() {
        String content = inputField.getText().trim();
        client.handleUserInput(content);
        inputField.setText("");
    }

    public void setAnonymous(boolean isAnonymous) {
        anonymousSelect.setSelectedIndex(isAnonymous ? 1 : 0);
    }

    public void addUser(String username) {
        onlineUserListModel.addElement(username);
        onlineUser.setModel(onlineUserListModel);
    }

    public void delUser(String username) {
        onlineUserListModel.removeElement(username);
        onlineUser.setModel(onlineUserListModel);
    }

    public void setUserList(List<String> users) {
        onlineUserListModel.clear();
        for (String username : users)
            onlineUserListModel.addElement(username);
    }

    public void addTextMessage(Message message, String username) {

        String prefix = Messages.getMessagePrefix(message, username);
        String text = Messages.getMessageContent(message);

        StyledDocument doc = chatContent.getStyledDocument();
        SimpleAttributeSet prefixStyle = new SimpleAttributeSet();
        StyleConstants.setBold(prefixStyle, true);
        StyleConstants.setFontFamily(prefixStyle, "Consolas");
        StyleConstants.setFontSize(prefixStyle, 20);
        if (message instanceof SystemMessage)
            StyleConstants.setForeground(prefixStyle, Color.BLUE);
        if (message instanceof UserPrivateMessage)
            StyleConstants.setForeground(prefixStyle, new Color(118, 3, 137));

        SimpleAttributeSet textStyle = new SimpleAttributeSet();
        StyleConstants.setFontFamily(textStyle, "SansSerif");
        StyleConstants.setFontSize(textStyle, 20);

        try {
            doc.insertString(doc.getLength(), prefix + "\n", prefixStyle);
            doc.insertString(doc.getLength(), "\t" + text + "\n" + "\n", textStyle);
        } catch (BadLocationException ex) {
            System.err.println("Error: " + ex.getMessage());
        }
    }

    public void displayMessage(String message) {
        StyledDocument doc = chatContent.getStyledDocument();

        SimpleAttributeSet textStyle = new SimpleAttributeSet();
        StyleConstants.setFontFamily(textStyle, "Consolas");
        StyleConstants.setFontSize(textStyle, 20);
        StyleConstants.setForeground(textStyle, Color.MAGENTA);

        try {
            doc.insertString(doc.getLength(), "+====\n" + message + "\n" + "=====\n\n", textStyle);
        } catch (BadLocationException ex) {
            System.err.println("Error: " + ex.getMessage());
        }

    }

}
