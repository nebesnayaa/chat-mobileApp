package com.example.mobailchatapp;

public class Chat {
    private String chatId;
    private String name;
    private String lastMessage;

    public Chat() {}

    public Chat(String chatId, String name, String lastMessage) {
        this.chatId = chatId;
        this.name = name;
        this.lastMessage = lastMessage;
    }

    public String getChatId() {
        return chatId;
    }

    public void setChatId(String chatId) {
        this.chatId = chatId;
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
}
