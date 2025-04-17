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
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.ValueEventListener;
import com.google.firebase.auth.FirebaseAuth;

public class ChatActivity extends AppCompatActivity {

    private EditText messageEditText;
    private Button sendButton;
    private String chatId;
    private String username;
    private TextView usernameTextView;
    private LinearLayout messagesContainer;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_chat);

        messageEditText = findViewById(R.id.message_edit_text);
        sendButton = findViewById(R.id.send_button);
        usernameTextView = findViewById(R.id.username_text_view);
        messagesContainer = findViewById(R.id.messages_container); // Теперь messagesContainer - LinearLayout

        // Получаем chatId и username из Intent
        chatId = getIntent().getStringExtra("chatId");
        username = getIntent().getStringExtra("username");
        usernameTextView.setText(username);

        // Прослушивание сообщений
        listenForMessages(chatId);

        sendButton.setOnClickListener(v -> {
            String message = messageEditText.getText().toString();
            if (!message.isEmpty()) {
                sendMessage(chatId, message);
            }
        });

        ImageButton logoutButton = findViewById(R.id.logout_button);
        logoutButton.setOnClickListener(v -> {
            finish();
        });
    }

    private void sendMessage(String chatId, String message) {
        // Получаем ссылку на базу данных для сообщений в данном чате
        DatabaseReference messagesRef = FirebaseDatabase.getInstance().getReference("messages").child(chatId).push();

        // Получаем UID текущего пользователя и текущее время
        String senderId = FirebaseAuth.getInstance().getCurrentUser().getUid();
        long timestamp = System.currentTimeMillis();

        // Создаём объект ChatMessage
        ChatMessage chatMessage = new ChatMessage(message, senderId, timestamp);

        // Сохраняем сообщение в Firebase
        messagesRef.setValue(chatMessage);

        // Очищаем поле ввода
        messageEditText.setText("");
    }

    private void listenForMessages(String chatId) {
        // Получаем ссылку на базу данных для сообщений в данном чате
        DatabaseReference messagesRef = FirebaseDatabase.getInstance().getReference("messages").child(chatId);

        // Добавляем слушатель для получения данных
        messagesRef.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                messagesContainer.removeAllViews();  // Очищаем контейнер сообщений

                // Проходим по всем сообщениям и добавляем их в интерфейс
                for (DataSnapshot snapshot : dataSnapshot.getChildren()) {
                    ChatMessage message = snapshot.getValue(ChatMessage.class);
                    if (message != null) {
                        addMessageToChat(message.getMessage());
                    }
                }
            }

            @Override
            public void onCancelled(DatabaseError databaseError) {
                // Ошибка при загрузке сообщений
                Toast.makeText(ChatActivity.this, "Failed to load messages", Toast.LENGTH_SHORT).show();
            }
        });
    }

    private void addMessageToChat(String message) {
        // Создаём новый TextView для отображения сообщения
        TextView messageTextView = new TextView(this);
        messageTextView.setText(message);
        messagesContainer.addView(messageTextView);  // Добавляем сообщение в контейнер
    }

    // Класс для представления сообщения
    private static class ChatMessage {
        private String message;
        private String senderId;
        private long timestamp;

        public ChatMessage() {
            // Пустой конструктор нужен для Firebase
        }

        public ChatMessage(String message, String senderId, long timestamp) {
            this.message = message;
            this.senderId = senderId;
            this.timestamp = timestamp;
        }

        public String getMessage() {
            return message;
        }

        public String getSenderId() {
            return senderId;
        }

        public long getTimestamp() {
            return timestamp;
        }
    }
}
