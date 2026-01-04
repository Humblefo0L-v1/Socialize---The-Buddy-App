package com.socialize.chat.exception;

public class GroupChatNotFoundException extends RuntimeException {
    public GroupChatNotFoundException(String message) {
        super(message);
    }
}