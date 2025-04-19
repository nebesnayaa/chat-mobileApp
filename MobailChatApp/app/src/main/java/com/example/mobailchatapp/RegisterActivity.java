package com.example.mobailchatapp;

import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;
import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;

import java.io.Console;
import java.util.HashMap;
import java.util.Map;

public class RegisterActivity extends AppCompatActivity {

    EditText edtEmail, edtPassword, edtName;
    Button btnSubmitRegister;
    FirebaseAuth mAuth;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_register);

        edtEmail = findViewById(R.id.edtEmail);
        edtPassword = findViewById(R.id.edtPassword);
        edtName = findViewById(R.id.edtLogin);

        btnSubmitRegister = findViewById(R.id.btnSubmitRegister);
        mAuth = FirebaseAuth.getInstance();

        btnSubmitRegister.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                String email = edtEmail.getText().toString().trim();
                String password = edtPassword.getText().toString().trim();
                String userName = edtName.getText().toString().trim();

                if (email.isEmpty() || password.isEmpty() || userName.isEmpty()) {
                    Toast.makeText(RegisterActivity.this, "Будь ласка, введіть email та пароль", Toast.LENGTH_SHORT).show();
                    return;
                }

                Log.d("REGISTER", "Починаємо створення користувача...");
                mAuth.createUserWithEmailAndPassword(email, password)
                    .addOnCompleteListener(task -> {
                        if (task.isSuccessful()) {
                            Log.d("REGISTER", "Користувача створено");
                            FirebaseUser user = mAuth.getCurrentUser();

                            if (user != null) {
                                String uid = user.getUid();
                                String userEmail = user.getEmail();
                                Log.d("REGISTER", "UID: " + uid + ", email: " + userEmail);

                                Map<String, Object> userData = new HashMap<>();
                                userData.put("email", userEmail);
                                userData.put("name", userName);
                                userData.put("createdAt", System.currentTimeMillis());

                                FirebaseDatabase.getInstance().getReference("users")
                                    .child(uid)
                                    .setValue(userData)
                                    .addOnSuccessListener(unused -> {
                                        Log.d("REGISTER", "✅ Дані успішно записано в БД");
                                        Toast.makeText(RegisterActivity.this, "Реєстрація успішна", Toast.LENGTH_SHORT).show();
                                        startActivity(new Intent(RegisterActivity.this, ChatListActivity.class));
                                        finish();
                                    })
                                    .addOnFailureListener(e -> {
                                        Log.e("REGISTER", "❌ Помилка при записі в БД", e);
                                        Toast.makeText(RegisterActivity.this, "❌ DB Error: " + e.getMessage(), Toast.LENGTH_LONG).show();
                                    });
                            } else {
                                Log.e("REGISTER", "user == null після реєстрації");
                                Toast.makeText(RegisterActivity.this, "Помилка: user == null", Toast.LENGTH_LONG).show();
                            }

                        } else {
                            Log.e("REGISTER", "Помилка створення користувача", task.getException());
                            Toast.makeText(RegisterActivity.this, "Помилка: " + task.getException().getMessage(), Toast.LENGTH_LONG).show();
                            Log.d("ERROR: ", task.getException().getMessage());
                        }
                    });
            }
        });
    }
}