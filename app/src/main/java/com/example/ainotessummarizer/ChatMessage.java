package com.example.ainotessummarizer;
public class ChatMessage {
    String message;
    String sentBy; // "user" ya "bot"

    public ChatMessage(String message, String sentBy) {
        this.message = message;
        this.sentBy = sentBy;
    }

    public String getMessage() { return message; }
    public String getSentBy() { return sentBy; }
}