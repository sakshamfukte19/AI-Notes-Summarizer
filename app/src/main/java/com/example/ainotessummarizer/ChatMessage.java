package com.example.ainotessummarizer;

public class ChatMessage {
    String message;
    boolean isUser; // True = User, False = AI

    public ChatMessage(String message, boolean isUser) {
        this.message = message;
        this.isUser = isUser;
    }

    public String getMessage() { return message; }
    public boolean isUser() { return isUser; }
}