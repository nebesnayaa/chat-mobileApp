package com.example.mobailchatapp;

import android.graphics.drawable.GradientDrawable;
import android.os.Bundle;
import android.widget.EditText;
import android.widget.Button;
import android.widget.Toast;
import android.widget.TextView;
import android.widget.ImageButton;
import android.view.Gravity;
import android.view.View;
import android.widget.LinearLayout;
import android.graphics.Color;
import android.view.ViewGroup;
import android.widget.ScrollView;
import androidx.appcompat.app.AppCompatActivity;

import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.ValueEventListener;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.FirebaseDatabase;

import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Locale;

public class ChatActivity extends AppCompatActivity {

    private EditText messageEditText;
    private Button sendButton;
    private String chatId;
    private String username;
    private TextView usernameTextView;
    private LinearLayout messagesContainer;
    private String currentUserId;
    private ScrollView scrollView;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_chat);

        messageEditText = findViewById(R.id.message_edit_text);
        sendButton = findViewById(R.id.send_button);
        usernameTextView = findViewById(R.id.username_text_view);
        messagesContainer = findViewById(R.id.messages_container);
        scrollView = findViewById(R.id.messages_scroll_view);

        chatId = getIntent().getStringExtra("chatId");
        username = getIntent().getStringExtra("username");
        currentUserId = FirebaseAuth.getInstance().getCurrentUser().getUid();

        usernameTextView.setText(username);

        listenForMessages(chatId);

        sendButton.setOnClickListener(v -> {
            String message = messageEditText.getText().toString().trim();
            if (!message.isEmpty()) {
                sendMessage(chatId, message);
            }
        });

        ImageButton logoutButton = findViewById(R.id.logout_button);
        logoutButton.setOnClickListener(v -> finish());
    }

    private void sendMessage(String chatId, String message) {
        DatabaseReference messagesRef = FirebaseDatabase.getInstance().getReference("messages").child(chatId).push();

        String senderId = FirebaseAuth.getInstance().getCurrentUser().getUid();
        long timestamp = System.currentTimeMillis();

        ChatMessage chatMessage = new ChatMessage(message, senderId, timestamp);
        messagesRef.setValue(chatMessage);
        messageEditText.setText("");
    }

    private void listenForMessages(String chatId) {
        DatabaseReference messagesRef = FirebaseDatabase.getInstance().getReference("messages").child(chatId);

        messagesRef.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot snapshot) {
                messagesContainer.removeAllViews();

                for (DataSnapshot messageSnapshot : snapshot.getChildren()) {
                    ChatMessage message = messageSnapshot.getValue(ChatMessage.class);
                    if (message != null) {
                        addMessageToChat(message);
                    }
                }

                scrollView.post(() -> scrollView.fullScroll(View.FOCUS_DOWN));
            }

            @Override
            public void onCancelled(DatabaseError error) {
                Toast.makeText(ChatActivity.this, "Ошибка загрузки сообщений", Toast.LENGTH_SHORT).show();
            }
        });
    }

    private void addMessageToChat(ChatMessage message) {
        boolean isCurrentUser = message.getSenderId().equals(currentUserId);

        // Внешний контейнер
        LinearLayout messageLayout = new LinearLayout(this);
        messageLayout.setOrientation(LinearLayout.HORIZONTAL);
        messageLayout.setPadding(10, 10, 10, 10);
        messageLayout.setGravity(isCurrentUser ? Gravity.END : Gravity.START);

        // Внутренний контейнер для имени, текста и времени
        LinearLayout innerLayout = new LinearLayout(this);
        innerLayout.setOrientation(LinearLayout.VERTICAL);
        innerLayout.setLayoutParams(new LinearLayout.LayoutParams(
                ViewGroup.LayoutParams.WRAP_CONTENT,
                ViewGroup.LayoutParams.WRAP_CONTENT
        ));

        // Имя отправителя (показывается всегда)
        TextView senderNameTextView = new TextView(this);
        senderNameTextView.setText(isCurrentUser ? "Вы" : username);
        senderNameTextView.setTextSize(12);
        senderNameTextView.setTextColor(Color.DKGRAY);
        senderNameTextView.setPadding(10, 0, 10, 4);
        innerLayout.addView(senderNameTextView);

        // Текст сообщения
        TextView messageTextView = new TextView(this);
        messageTextView.setText(message.getMessage());
        messageTextView.setTextSize(16);
        messageTextView.setTextColor(Color.WHITE);
        messageTextView.setPadding(20, 10, 20, 10);

        GradientDrawable background = new GradientDrawable();
        background.setCornerRadius(24);
        background.setColor(isCurrentUser ? Color.parseColor("#4CAF50") : Color.parseColor("#2196F3"));
        messageTextView.setBackground(background);
        innerLayout.addView(messageTextView);

        // Время
        TextView timeTextView = new TextView(this);
        timeTextView.setText(formatTime(message.getTimestamp()));
        timeTextView.setTextSize(12);
        timeTextView.setTextColor(Color.LTGRAY);
        timeTextView.setPadding(10, 2, 10, 0);
        timeTextView.setGravity(isCurrentUser ? Gravity.END : Gravity.START);
        innerLayout.addView(timeTextView);

        messageLayout.addView(innerLayout);
        messagesContainer.addView(messageLayout);
    }

    private String formatTime(long timestamp) {
        return new SimpleDateFormat("HH:mm", Locale.getDefault()).format(new Date(timestamp));
    }

    private static class ChatMessage {
        private String message;
        private String senderId;
        private long timestamp;

        public ChatMessage() {}

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