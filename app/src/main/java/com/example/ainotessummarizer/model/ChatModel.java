package com.example.ainotessummarizer.model;

/**
 * Data model representing a chat session.
 * Used by HistoryAdapter and BookmarkAdapter.
 * MVVM-ready — designed to be populated from a ViewModel/Repository.
 */
public class ChatModel {

    private String chatId;
    private String title;
    private String lastMessage;
    private long timestamp;
    private boolean isBookmarked;

    /** No-arg constructor (required for Firebase / serialisation). */
    public ChatModel() { }

    public ChatModel(String chatId, String title, String lastMessage,
                     long timestamp, boolean isBookmarked) {
        this.chatId = chatId;
        this.title = title;
        this.lastMessage = lastMessage;
        this.timestamp = timestamp;
        this.isBookmarked = isBookmarked;
    }

    // ── Getters ──

    public String getChatId() { return chatId; }
    public String getTitle() { return title; }
    public String getLastMessage() { return lastMessage; }
    public long getTimestamp() { return timestamp; }
    public boolean isBookmarked() { return isBookmarked; }

    // ── Setters ──

    public void setChatId(String chatId) { this.chatId = chatId; }
    public void setTitle(String title) { this.title = title; }
    public void setLastMessage(String lastMessage) { this.lastMessage = lastMessage; }
    public void setTimestamp(long timestamp) { this.timestamp = timestamp; }
    public void setBookmarked(boolean bookmarked) { isBookmarked = bookmarked; }
}
