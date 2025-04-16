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

public class ChatActivity extends AppCompatActivity {

    private EditText messageEditText;
    private Button sendButton;
    private String userId;
    private String username;
    private TextView usernameTextView;
    private LinearLayout messagesContainer; // Меняем тип с View на LinearLayout

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_chat);

        // Инициализация элементов интерфейса
        messageEditText = findViewById(R.id.message_edit_text);
        sendButton = findViewById(R.id.send_button);
        usernameTextView = findViewById(R.id.username_text_view);
        messagesContainer = findViewById(R.id.messages_container); // Теперь messagesContainer - LinearLayout

        userId = getIntent().getStringExtra("userId");
        username = getIntent().getStringExtra("username");
        usernameTextView.setText(username);

        // Прослушивание сообщений
        listenForMessages(userId);

        // Установка обработчика кнопки отправки сообщения
        sendButton.setOnClickListener(v -> {
            String message = messageEditText.getText().toString();
            if (!message.isEmpty()) {
                sendMessage(userId, message);
            }
        });

        // Обработчик кнопки выхода
        ImageButton logoutButton = findViewById(R.id.logout_button);
        logoutButton.setOnClickListener(v -> {
            finish(); // Закрывает активити и возвращает в чат-лист
        });
    }

    // Метод для отправки сообщения в Firebase
    private void sendMessage(String userId, String message) {
        DatabaseReference messagesRef = FirebaseDatabase.getInstance().getReference("chats").child(userId).push();
        messagesRef.setValue(new ChatMessage(message)); // Сохраняем сообщение как объект
        messageEditText.setText(""); // Очищаем поле ввода
    }

    // Метод для получения сообщений из Firebase и их отображения
    private void listenForMessages(String userId) {
        DatabaseReference messagesRef = FirebaseDatabase.getInstance().getReference("chats").child(userId);

        messagesRef.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                messagesContainer.removeAllViews(); // Теперь это работает, так как messagesContainer - LinearLayout

                // Итерация по всем сообщениям
                for (DataSnapshot snapshot : dataSnapshot.getChildren()) {
                    ChatMessage message = snapshot.getValue(ChatMessage.class);
                    if (message != null) {
                        addMessageToChat(message.getMessage());
                    }
                }
            }

            @Override
            public void onCancelled(DatabaseError databaseError) {
                Toast.makeText(ChatActivity.this, "Failed to load messages", Toast.LENGTH_SHORT).show();
            }
        });
    }

    // Метод для добавления сообщений в контейнер чата
    private void addMessageToChat(String message) {
        TextView messageTextView = new TextView(this);
        messageTextView.setText(message);
        messagesContainer.addView(messageTextView);
    }

    // Класс для представления сообщения
    private static class ChatMessage {
        private String message;

        public ChatMessage() {

        }

        public ChatMessage(String message) {
            this.message = message;
        }

        public String getMessage() {
            return message;
        }
    }
}
