package com.example.mobailchatapp;

import android.os.Bundle;
import android.widget.EditText;
import android.widget.Button;
import android.widget.Toast;
import android.widget.TextView;
import android.widget.ImageButton;
import android.view.View;
import android.widget.LinearLayout;
import androidx.appcompat.app.AppCompatActivity;
import com.google.firebase.database.*;

public class ChatActivity extends AppCompatActivity {

    private EditText messageEditText;
    private Button sendButton;
    private LinearLayout messagesContainer;
    private TextView usernameTextView;

    private String currentUserId;
    private String otherUserId;
    private String chatId;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_chat);

        messageEditText = findViewById(R.id.message_edit_text);
        sendButton = findViewById(R.id.send_button);
        messagesContainer = findViewById(R.id.messages_container);
        usernameTextView = findViewById(R.id.username_text_view);

        // Получаем ID текущего и другого пользователя
        currentUserId = getIntent().getStringExtra("currentUserId");
        otherUserId = getIntent().getStringExtra("otherUserId");
        chatId = generateChatId(currentUserId, otherUserId);

        usernameTextView.setText("Чат с " + otherUserId);

        sendButton.setOnClickListener(v -> {
            String message = messageEditText.getText().toString();
            if (!message.isEmpty()) {
                sendMessage(message);
            }
        });

        listenForMessages();
        saveChatToUserChats();
    }

    // Генерация chatId по алфавиту (один и тот же ID у обоих)
    private String generateChatId(String user1, String user2) {
        return (user1.compareTo(user2) < 0) ? user1 + "_" + user2 : user2 + "_" + user1;
    }

    private void sendMessage(String message) {
        DatabaseReference chatRef = FirebaseDatabase.getInstance().getReference("chats").child(chatId).child("messages");
        String messageId = chatRef.push().getKey();

        ChatMessage chatMessage = new ChatMessage(currentUserId, message);
        if (messageId != null) {
            chatRef.child(messageId).setValue(chatMessage);
            messageEditText.setText("");
        }
    }

    private void listenForMessages() {
        DatabaseReference messagesRef = FirebaseDatabase.getInstance().getReference("chats").child(chatId).child("messages");

        messagesRef.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot snapshot) {
                messagesContainer.removeAllViews();
                for (DataSnapshot messageSnapshot : snapshot.getChildren()) {
                    ChatMessage message = messageSnapshot.getValue(ChatMessage.class);
                    if (message != null) {
                        addMessageToChat(message.getSenderId(), message.getMessage());
                    }
                }
            }

            @Override
            public void onCancelled(DatabaseError error) {
                Toast.makeText(ChatActivity.this, "Ошибка загрузки сообщений", Toast.LENGTH_SHORT).show();
            }
        });
    }

    private void addMessageToChat(String senderId, String messageText) {
        TextView messageView = new TextView(this);
        messageView.setText(senderId + ": " + messageText);
        messagesContainer.addView(messageView);
    }

    // Добавляем чат в userChats, чтобы потом видеть список чатов
    private void saveChatToUserChats() {
        DatabaseReference userChatsRef = FirebaseDatabase.getInstance().getReference("userChats");

        userChatsRef.child(currentUserId).child(chatId).setValue(true);
        userChatsRef.child(otherUserId).child(chatId).setValue(true);
    }

    // Класс сообщения
    public static class ChatMessage {
        private String senderId;
        private String message;

        public ChatMessage() {}

        public ChatMessage(String senderId, String message) {
            this.senderId = senderId;
            this.message = message;
        }

        public String getSenderId() {
            return senderId;
        }

        public String getMessage() {
            return message;
        }
    }
}
