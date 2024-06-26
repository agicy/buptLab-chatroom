package com.chatroom.common.message;

public class SystemReply extends SystemMessage {
    public static final String LOGIN_SUCCESS = "Login Success";
    public static final String ALREADY_LOGIN = "Already Login";
    public static final String USER_NOT_EXIST = "User NOT Exist";
    public static final String PASSWORD_INCORRECT = "Password Incorrect";

    public SystemReply(MessageContent content) {
        super(content);
    }
}

