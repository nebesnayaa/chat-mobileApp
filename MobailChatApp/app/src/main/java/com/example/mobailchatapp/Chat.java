package com.example.mobailchatapp;

public class Chat {
    private String chatId;
    private String name;
    private String lastMessage;
    private String userId;  // Добавлен userId

    public Chat() {}

    public Chat(String chatId, String name, String lastMessage) {
        this.chatId = chatId;
        this.name = name;
        this.lastMessage = lastMessage;
        this.userId = extractUserIdFromChatId(chatId);  // Инициализируем userId
    }

    public String getChatId() {
        return chatId;
    }

    public void setChatId(String chatId) {
        this.chatId = chatId;
        this.userId = extractUserIdFromChatId(chatId);  // Обновляем userId при изменении chatId
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

    // Этот метод можно использовать, чтобы извлечь userId из chatId
    private String extractUserIdFromChatId(String chatId) {
        // Например, разделим chatId по символу "_" и вернем одну часть
        // Тут предполагается, что chatId состоит из двух частей, разделенных "_"
        String[] parts = chatId.split("_");
        return parts.length > 0 ? parts[0] : null;  // Возвращаем первый элемент как userId
    }

    public void setUserId(String userId) {
        this.userId = userId;
    }
}
