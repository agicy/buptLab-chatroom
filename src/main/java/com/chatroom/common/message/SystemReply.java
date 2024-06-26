package com.chatroom.common.message;

/**
 * Represents a system reply message in the chatroom application.
 */
public class SystemReply extends SystemMessage {

    /**
     * Indicates a successful login.
     */
    public static final String LOGIN_SUCCESS = "Login Success";

    /**
     * Indicates that the user is already logged in.
     */
    public static final String ALREADY_LOGIN = "Already Login";

    /**
     * Indicates that the specified user does not exist.
     */
    public static final String USER_NOT_EXIST = "User NOT Exist";

    /**
     * Indicates an incorrect password.
     */
    public static final String PASSWORD_INCORRECT = "Password Incorrect";

    /**
     * Creates a new instance of a system reply message.
     *
     * @param content the content of the message
     */
    public SystemReply(MessageContent content) {
        super(content);
    }
}
