package com.example.ainotessummarizer;
public class ChatMessage {
    private static final String SENT_BY_USER = "user";

    String message;
    String sentBy; // "user" ya "bot"

    public ChatMessage(String message, String sentBy) {
        this.message = message;
        this.sentBy = sentBy;
    }

    public String getMessage() { return message; }
    public String getSentBy() { return sentBy; }

    public boolean isUser() {
        return SENT_BY_USER.equals(sentBy);
    }
}
