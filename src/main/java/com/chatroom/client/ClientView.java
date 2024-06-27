package com.chatroom.client;

import com.chatroom.common.Constants;
import com.chatroom.common.message.*;

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
import java.util.Objects;

/**
 * The main client view for the chatroom application.
 */
public class ClientView extends JFrame {

    private final Client client;
    private final DefaultListModel<String> onlineUserListModel = new DefaultListModel<>();
    private JPanel chatRoom;
    private JTextField inputField;
    private JButton sendButton;
    private JLabel currentUsername;
    private JComboBox<String> anonymousSelect;
    private JList<String> onlineUserList;
    private JButton logoutButton;
    private JTextPane chatContent;
    private JScrollPane onlineUserPanel;
    private JScrollPane messagePanel;
    private JPanel userInfoPanel;
    private JLabel onlineUserLabel;
    private JPanel chatPanel;
    private JLabel chatLabel;
    private JLabel inputLabel;
    private JLabel authorLabel;

    /**
     * Creates a new instance of the client view.
     *
     * @param client the associated client
     */
    public ClientView(Client client) {
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
        setLocationRelativeTo(null);
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
        onlineUserList.addListSelectionListener(new ListSelectionListener() {
            /**
             * Called whenever the value of the selection changes.
             *
             * @param e the event that characterizes the change.
             */
            @Override
            public void valueChanged(ListSelectionEvent e) {
                if (!onlineUserList.isSelectionEmpty()) {
                    String selectedItem = onlineUserList.getSelectedValue();
                    if (inputField.getText().startsWith("@"))
                        JOptionPane.showMessageDialog(chatRoom, "输入框已经以 @ 开头", "操作错误", JOptionPane.ERROR_MESSAGE);
                    else
                        inputField.setText("@" + selectedItem + " " + inputField.getText());

                    onlineUserList.clearSelection();
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

    /**
     * Sends the user input to the server.
     */
    private void send() {
        String content = inputField.getText().trim();
        client.handleUserInput(content);
        inputField.setText("");
    }

    /**
     * Sets the anonymous mode for the client.
     *
     * @param isAnonymous true if the client is in anonymous mode, false otherwise
     */
    public void setAnonymous(boolean isAnonymous) {
        anonymousSelect.setSelectedIndex(isAnonymous ? 1 : 0);
    }

    /**
     * Adds a user to the online user list.
     *
     * @param username the username of the user to add
     */
    public void addUser(String username) {
        onlineUserListModel.addElement(username);
        onlineUserList.setModel(onlineUserListModel);
    }

    /**
     * Removes a user from the online user list.
     *
     * @param username the username of the user to remove
     */
    public void delUser(String username) {
        onlineUserListModel.removeElement(username);
        onlineUserList.setModel(onlineUserListModel);
    }

    /**
     * Sets the list of online users.
     *
     * @param users the list of usernames representing online users
     */
    public void setUserList(List<String> users) {
        onlineUserListModel.clear();
        for (String username : users)
            onlineUserListModel.addElement(username);
    }

    /**
     * Adds a text message to the chat content.
     *
     * @param message  the message to display
     * @param username the username associated with the message
     */
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
        if (message instanceof UserMessage um && Objects.equals(um.getSender(), username)) {
            StyleConstants.setAlignment(prefixStyle, StyleConstants.ALIGN_RIGHT);
            StyleConstants.setAlignment(textStyle, StyleConstants.ALIGN_RIGHT);
        } else {
            StyleConstants.setAlignment(prefixStyle, StyleConstants.ALIGN_LEFT);
            StyleConstants.setAlignment(textStyle, StyleConstants.ALIGN_LEFT);
        }
        try {
            doc.insertString(doc.getLength(), prefix + "\n", prefixStyle);
            doc.setParagraphAttributes(doc.getLength() - prefix.length() - 1, prefix.length() + 1, prefixStyle, false);
            doc.insertString(doc.getLength(), "\t" + text + "\n" + "\n", textStyle);
            doc.setParagraphAttributes(doc.getLength() - text.length() - 3, text.length() + 3, textStyle, false);
        } catch (BadLocationException ex) {
            System.err.println("Error: " + ex.getMessage());
        }
        if (message instanceof UserMessage um && Objects.equals(um.getSender(), username))
            chatContent.setCaretPosition(chatContent.getDocument().getLength());
    }

    /**
     * Displays a system message in the chat content.
     *
     * @param message the system message to display
     */
    public void displayMessage(String message) {
        StyledDocument doc = chatContent.getStyledDocument();
        SimpleAttributeSet textStyle = new SimpleAttributeSet();
        StyleConstants.setFontFamily(textStyle, "Consolas");
        StyleConstants.setFontSize(textStyle, 20);
        StyleConstants.setForeground(textStyle, Color.MAGENTA);
        StyleConstants.setAlignment(textStyle, StyleConstants.ALIGN_CENTER);
        try {
            doc.insertString(doc.getLength(), message + "\n", textStyle);
            doc.setParagraphAttributes(doc.getLength() - message.length() - 1, message.length() + 1, textStyle, false);

        } catch (BadLocationException ex) {
            System.err.println("Error: " + ex.getMessage());
        }
        chatContent.setCaretPosition(chatContent.getDocument().getLength());
    }
}
