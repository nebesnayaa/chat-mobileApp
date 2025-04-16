package com.example.mobailchatapp;

import android.os.Bundle;
import android.widget.EditText;
import android.widget.Button;
import android.widget.Toast;
import androidx.appcompat.app.AppCompatActivity;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;

public class ChatActivity extends AppCompatActivity {

    private EditText messageEditText;
    private Button sendButton;
    private String userId;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_chat);

        messageEditText = findViewById(R.id.message_edit_text);
        sendButton = findViewById(R.id.send_button);
        userId = getIntent().getStringExtra("userId");

        sendButton.setOnClickListener(v -> {
            String message = messageEditText.getText().toString();
            if (!message.isEmpty()) {
                sendMessage(userId, message);
            }
        });
    }

    private void sendMessage(String userId, String message) {
        DatabaseReference messagesRef = FirebaseDatabase.getInstance().getReference("chats").child(userId).push();
        messagesRef.setValue(new ChatMessage(message));
        messageEditText.setText("");
    }

    private static class ChatMessage {
        private String message;

        public ChatMessage(String message) {
            this.message = message;
        }

        public String getMessage() {
            return message;
        }
    }
}
