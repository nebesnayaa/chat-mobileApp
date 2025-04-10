// RegisterActivity.java
package com.example.mobailchatapp;

import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import androidx.appcompat.app.AppCompatActivity;

public class RegisterActivity extends AppCompatActivity {

    EditText edtEmail, edtPassword;
    Button btnSubmitRegister;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_register);

        edtEmail = findViewById(R.id.edtEmail);
        edtPassword = findViewById(R.id.edtPassword);
        btnSubmitRegister = findViewById(R.id.btnSubmitRegister);

        btnSubmitRegister.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                // Тут можно добавить код для обработки регистрации
            }
        });
    }
}
