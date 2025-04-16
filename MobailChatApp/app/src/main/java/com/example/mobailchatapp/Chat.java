package com.example.mobailchatapp;

public class Chat {
    private String name;
    private String lastMessage;
    private String userId;

    public Chat() {
    }

    public Chat(String name, String lastMessage, String userId) {
        this.name = name;
        this.lastMessage = lastMessage;
        this.userId = userId;
    }

    public String getName() {
        return name;
    }

    public String getLastMessage() {
        return lastMessage;
    }

    public String getUserId() {
        return userId;
    }

    public void setName(String name) {
        this.name = name;
    }

    public void setLastMessage(String lastMessage) {
        this.lastMessage = lastMessage;
    }

    public void setUserId(String userId) {
        this.userId = userId;
    }
}
