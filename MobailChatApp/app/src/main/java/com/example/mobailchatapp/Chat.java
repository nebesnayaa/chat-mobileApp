package com.example.mobailchatapp;

public class Chat {
    private String name;
    private String lastMessage;

    public Chat(String name, String lastMessage) {
        this.name = name;
        this.lastMessage = lastMessage;
    }

    public String getName() { return name; }
    public String getLastMessage() { return lastMessage; }
}
