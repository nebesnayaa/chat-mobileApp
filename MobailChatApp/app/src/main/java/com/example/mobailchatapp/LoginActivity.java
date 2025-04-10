// LoginActivity.java
package com.example.mobailchatapp;

import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import androidx.appcompat.app.AppCompatActivity;

public class LoginActivity extends AppCompatActivity {

    EditText edtLoginEmail, edtLoginPassword;
    Button btnSubmitLogin;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_login);

        edtLoginEmail = findViewById(R.id.edtLoginEmail);
        edtLoginPassword = findViewById(R.id.edtLoginPassword);
        btnSubmitLogin = findViewById(R.id.btnSubmitLogin);

        btnSubmitLogin.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                // Тут можно добавить код для обработки авторизации
            }
        });
    }
}
