// LoginActivity.java
package com.example.mobailchatapp;

import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;
import android.content.Intent;
import android.text.TextUtils;
import androidx.appcompat.app.AppCompatActivity;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;

public class LoginActivity extends AppCompatActivity {

    EditText edtLoginEmail, edtLoginPassword;
    Button btnSubmitLogin;
    private FirebaseAuth mAuth;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_login);

        edtLoginEmail = findViewById(R.id.edtLoginEmail);
        edtLoginPassword = findViewById(R.id.edtLoginPassword);
        btnSubmitLogin = findViewById(R.id.btnSubmitLogin);
        mAuth = FirebaseAuth.getInstance();

        btnSubmitLogin.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                String email = edtLoginEmail.getText().toString().trim();
                String password = edtLoginPassword.getText().toString().trim();

                if (TextUtils.isEmpty(email) || TextUtils.isEmpty(password)) {
                    Toast.makeText(LoginActivity.this, "Будь ласка, введіть email та пароль", Toast.LENGTH_SHORT).show();
                    return;
                }

                // Логіка для логіну користувача
                mAuth.signInWithEmailAndPassword(email, password)
                    .addOnCompleteListener(task -> {
                        if (task.isSuccessful()) {
                            FirebaseUser user = mAuth.getCurrentUser();
                            Toast.makeText(LoginActivity.this, "Успішний вхід", Toast.LENGTH_SHORT).show();

                            // Перехід до наступного екрану (наприклад, ChatActivity)
                            Intent intent = new Intent(LoginActivity.this, ChatListActivity.class);
                            startActivity(intent);
                            finish();
                        } else {
                            Toast.makeText(LoginActivity.this, "Помилка входу: " + task.getException().getMessage(), Toast.LENGTH_LONG).show();
                        }
                    });
            }
        });
    }
}
