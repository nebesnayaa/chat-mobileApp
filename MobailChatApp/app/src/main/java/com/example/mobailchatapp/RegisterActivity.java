package com.example.mobailchatapp;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;
import androidx.appcompat.app.AppCompatActivity;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;

public class RegisterActivity extends AppCompatActivity {

    EditText edtEmail, edtPassword;
    Button btnSubmitRegister;
    FirebaseAuth mAuth;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_register);

        edtEmail = findViewById(R.id.edtEmail);
        edtPassword = findViewById(R.id.edtPassword);
        btnSubmitRegister = findViewById(R.id.btnSubmitRegister);
        mAuth = FirebaseAuth.getInstance();

        btnSubmitRegister.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                String email = edtEmail.getText().toString().trim();
                String password = edtPassword.getText().toString().trim();

                if (email.isEmpty() || password.isEmpty()) {
                    Toast.makeText(RegisterActivity.this, "Будь ласка, введіть email та пароль", Toast.LENGTH_SHORT).show();
                    return;
                }

                mAuth.createUserWithEmailAndPassword(email, password)
                        .addOnCompleteListener(task -> {
                            if (task.isSuccessful()) {
                                FirebaseUser user = mAuth.getCurrentUser();
                                Toast.makeText(RegisterActivity.this, "Реєстрація успішна", Toast.LENGTH_SHORT).show();

                                // Переход на ChatListActivity
                                Intent intent = new Intent(RegisterActivity.this, ChatListActivity.class);
                                startActivity(intent);
                                finish();
                            } else {
                                Toast.makeText(RegisterActivity.this, "Помилка: " + task.getException().getMessage(), Toast.LENGTH_LONG).show();
                            }
                        });
            }
        });
    }
}
