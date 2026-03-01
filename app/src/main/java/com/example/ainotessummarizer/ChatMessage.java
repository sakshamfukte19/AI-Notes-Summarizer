package com.example.ainotessummarizer;

public class ChatMessage {
    private static final String SENT_BY_USER = "user";

    private String message;
    private String sentBy; // "user" or "bot"

    // Required no-arg constructor for Firebase Realtime Database deserialization
    public ChatMessage() {
    }

    public ChatMessage(String message, String sentBy) {
        this.message = message;
        this.sentBy = sentBy;
    }

    public String getMessage() {
        return message;
    }

    public String getSentBy() {
        return sentBy;
    }

    // Setters required by Firebase
    public void setMessage(String message) {
        this.message = message;
    }

    public void setSentBy(String sentBy) {
        this.sentBy = sentBy;
    }

    public boolean isUser() {
        return SENT_BY_USER.equals(sentBy);
    }
}
