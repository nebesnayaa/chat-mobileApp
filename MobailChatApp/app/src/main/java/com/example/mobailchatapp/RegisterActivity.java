package com.example.mobailchatapp;

import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;
import androidx.appcompat.app.AppCompatActivity;

import com.example.mobailchatapp.ValidationUtils;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.messaging.FirebaseMessaging;

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

                if (!ValidationUtils.isValidEmail(email)) {
                    Toast.makeText(RegisterActivity.this, "Email має закінчуватись на @gmail.com", Toast.LENGTH_SHORT).show();
                    return;
                }

                if (!ValidationUtils.isValidPassword(password)) {
                    Toast.makeText(RegisterActivity.this, "Пароль має бути від 4 до 16 символів без пробілів", Toast.LENGTH_SHORT).show();
                    return;
                }

                if (!ValidationUtils.isValidUsername(userName)) {
                    Toast.makeText(RegisterActivity.this, "Введіть ім’я користувача", Toast.LENGTH_SHORT).show();
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

<<<<<<< Updated upstream
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
=======
                                    // Отримуємо токен FCM для поточного користувача
                                    FirebaseMessaging.getInstance().getToken()
                                            .addOnSuccessListener(token -> {
                                                Log.d("REGISTER", "FCM Token: " + token);

                                                // Збираємо дані користувача
                                                Map<String, Object> userData = new HashMap<>();
                                                userData.put("email", userEmail);
                                                userData.put("name", userName);
                                                userData.put("createdAt", System.currentTimeMillis());
                                                userData.put("fcmToken", token); // Додаємо токен у базу даних

                                                // Записуємо дані в Firebase Database
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
                                            })
                                            .addOnFailureListener(e -> {
                                                Log.e("REGISTER", "Помилка отримання FCM токена", e);
                                                Toast.makeText(RegisterActivity.this, "Помилка: " + e.getMessage(), Toast.LENGTH_LONG).show();
>>>>>>> Stashed changes
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
