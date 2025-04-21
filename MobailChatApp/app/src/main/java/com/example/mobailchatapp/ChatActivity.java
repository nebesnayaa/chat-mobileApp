package com.example.mobailchatapp;

import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.content.Context;
import android.graphics.drawable.GradientDrawable;
import android.os.Build;
import android.os.Bundle;
import android.util.Log;
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

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.NotificationCompat;
import java.io.InputStream;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.Collections;

import org.json.JSONObject;
import java.io.OutputStream;
import java.util.HashMap;
import java.util.Map;
import java.util.Scanner;

import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.ValueEventListener;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.functions.FirebaseFunctions;

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
        usernameTextView = findViewById(R.id.chat_name);
        messagesContainer = findViewById(R.id.messages_container);
        scrollView = findViewById(R.id.messages_scroll_view);

        chatId = getIntent().getStringExtra("chatId");
        username = getIntent().getStringExtra("username");
        currentUserId = FirebaseAuth.getInstance().getCurrentUser().getUid();

        if (username != null && !username.isEmpty()) {
            usernameTextView.setText(username);
        } else {
            usernameTextView.setText("Чат");
        }

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
        messagesRef.setValue(chatMessage).addOnCompleteListener(task -> {
            if(task.isSuccessful()){
                DatabaseReference chatRef = FirebaseDatabase.getInstance().getReference("chats").child(chatId).child("lastMessage");
                chatRef.setValue(chatMessage.getMessage());
                String otherUserId = getOtherUserIdFromChatId(chatId, currentUserId);
                fetchTokenAndSendNotification(otherUserId, "Нове повідомлення", message);
            }
        });
        messageEditText.setText("");
    }

<<<<<<< Updated upstream
    private void updateChatWithLastMessage(String chatId, String message, long timestamp) {
        DatabaseReference chatRef = FirebaseDatabase.getInstance().getReference("chats").child(chatId);
        chatRef.child("lastMessage").setValue(message);
        chatRef.child("lastMessageTimestamp").setValue(timestamp);
=======
    private void fetchTokenAndSendNotification(String receiverUserId, String title, String body) {
        DatabaseReference tokenRef = FirebaseDatabase.getInstance().getReference("users")
                .child(receiverUserId)
                .child("fcmToken");

        tokenRef.get().addOnSuccessListener(dataSnapshot -> {
            String token = dataSnapshot.getValue(String.class);
            if (token != null && !token.isEmpty()) {
                sendPushNotificationToBackend(token, title, body);
            } else {
                Log.w("PushNotif", "FCM token is null or empty for user: " + receiverUserId);
            }
        }).addOnFailureListener(e -> Log.e("PushNotif", "Failed to fetch FCM token", e));
    }

    private void sendPushNotificationToBackend(String token, String title, String body) {
        new Thread(() -> {
            try {
                System.out.println("sending new message");
                URL url = new URL("https://pushnotificationservice-cpfffxhfdygvhzf7.swedencentral-01.azurewebsites.net/api/notifications/send");
                HttpURLConnection conn = (HttpURLConnection) url.openConnection();
                conn.setRequestMethod("POST");
                conn.setDoOutput(true);
                conn.setRequestProperty("Content-Type", "application/x-www-form-urlencoded");

                String params = "token=" + token + "&title=" + title + "&body=" + body;

                OutputStream os = conn.getOutputStream();
                os.write(params.getBytes());
                os.flush();
                os.close();

                int responseCode = conn.getResponseCode();
                Log.d("PushNotif", "Backend responded with code: " + responseCode);
            } catch (Exception e) {
                Log.e("PushNotif", "Error sending push notification", e);
            }
        }).start();
    }

    private String getOtherUserIdFromChatId(String chatId, String myUserId) {
        String[] parts = chatId.split("_");
        if (parts.length != 2) return "";
        return parts[0].equals(myUserId) ? parts[1] : parts[0];
>>>>>>> Stashed changes
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

        LinearLayout messageLayout = new LinearLayout(this);
        messageLayout.setOrientation(LinearLayout.HORIZONTAL);
        messageLayout.setPadding(10, 10, 10, 10);
        messageLayout.setGravity(isCurrentUser ? Gravity.END : Gravity.START);

        LinearLayout innerLayout = new LinearLayout(this);
        innerLayout.setOrientation(LinearLayout.VERTICAL);
        innerLayout.setLayoutParams(new LinearLayout.LayoutParams(
                ViewGroup.LayoutParams.WRAP_CONTENT,
                ViewGroup.LayoutParams.WRAP_CONTENT
        ));

        TextView senderNameTextView = new TextView(this);
        senderNameTextView.setText(isCurrentUser ? "Вы" : username);
        senderNameTextView.setTextSize(12);
        senderNameTextView.setTextColor(Color.DKGRAY);
        senderNameTextView.setPadding(10, 0, 10, 4);
        innerLayout.addView(senderNameTextView);

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

<<<<<<< Updated upstream
    // Уведомление пока не используется — закомментировано
    /*
    private void showNotification(String messageText) {
        String channelId = "chat_notifications";
        String channelName = "Chat Messages";

        NotificationManager notificationManager = (NotificationManager) getSystemService(Context.NOTIFICATION_SERVICE);

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            NotificationChannel channel = new NotificationChannel(
                    channelId,
                    channelName,
                    NotificationManager.IMPORTANCE_DEFAULT
            );
            notificationManager.createNotificationChannel(channel);
        }

        NotificationCompat.Builder builder = new NotificationCompat.Builder(this, channelId)
                .setSmallIcon(android.R.drawable.ic_dialog_email)
                .setContentTitle("Новое сообщение от " + username)
                .setContentText(messageText)
                .setPriority(NotificationCompat.PRIORITY_DEFAULT)
                .setAutoCancel(true);

        notificationManager.notify(1, builder.build());
    }
    */

=======
>>>>>>> Stashed changes
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
