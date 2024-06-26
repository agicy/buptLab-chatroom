package com.chatroom.common.message;

import java.io.Serializable;

public abstract class MessageContent implements Serializable {
    public abstract Object getContent();
}
