package com.example.mobailchatapp;

public class Chat {
    private String chatId;
    private String name;
    private String lastMessage;
    private String userId;
    private long lastMessageTimestamp;

    public Chat() {}

    public Chat(String chatId, String name, String lastMessage, long lastMessageTimestamp) {
        this.chatId = chatId;
        this.name = name;
        this.lastMessage = lastMessage;
        this.userId = extractUserIdFromChatId(chatId);
        this.lastMessageTimestamp = lastMessageTimestamp;
    }

    // Геттеры и сеттеры
    public String getChatId() {
        return chatId;
    }

    public void setChatId(String chatId) {
        this.chatId = chatId;
        this.userId = extractUserIdFromChatId(chatId);
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getLastMessage() {
        return lastMessage;
    }

    public void setLastMessage(String lastMessage) {
        this.lastMessage = lastMessage;
    }

    public String getUserId() {
        return userId;
    }

    public void setUserId(String userId) {
        this.userId = userId;
    }

    public long getLastMessageTimestamp() {
        return lastMessageTimestamp;
    }

    public void setLastMessageTimestamp(long lastMessageTimestamp) {
        this.lastMessageTimestamp = lastMessageTimestamp;
    }

    private String extractUserIdFromChatId(String chatId) {
        String[] parts = chatId.split("_");
        return parts.length > 0 ? parts[0] : null;
    }
}
