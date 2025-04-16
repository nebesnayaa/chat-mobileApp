package com.example.mobailchatapp;

import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.ActionBarDrawerToggle;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.SearchView;
import androidx.drawerlayout.widget.DrawerLayout;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import androidx.appcompat.widget.Toolbar;

import com.google.android.material.navigation.NavigationView;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import java.util.ArrayList;
import java.util.List;

public class ChatListActivity extends AppCompatActivity {
    private DrawerLayout drawerLayout;
    private NavigationView navView;
    private RecyclerView recyclerView;
    private SearchView searchView;
    private ChatAdapter adapter;
    private List<Chat> chatList = new ArrayList<>();
    private List<Chat> filteredList = new ArrayList<>();
    private TextView toolbarTitle; // Для отображения логина пользователя в Toolbar

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_chat_list);

        drawerLayout = findViewById(R.id.drawer_layout);
        navView = findViewById(R.id.nav_view);
        searchView = findViewById(R.id.search_view);
        recyclerView = findViewById(R.id.chat_list);
        Toolbar toolbar = findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);

        // Получаем ссылку на TextView для отображения логина пользователя в Toolbar
//        toolbarTitle = findViewById(R.id.toolbar_title);

        // Получаем текущего пользователя из Firebase
//        String userEmail = FirebaseAuth.getInstance().getCurrentUser().getEmail();
//        toolbarTitle.setText(userEmail); // Устанавливаем email пользователя в toolbar

        // Настройка Navigation Drawer
        ActionBarDrawerToggle toggle = new ActionBarDrawerToggle(this, drawerLayout, toolbar,
                R.string.app_name, R.string.app_name);
        drawerLayout.addDrawerListener(toggle);
        toggle.syncState();

        navView.setNavigationItemSelectedListener(item -> {
            if (item.getItemId() == R.id.menu_settings) {
                // Переход в экран настроек
                Intent intent = new Intent(ChatListActivity.this, SettingActivity.class);
                startActivity(intent);
            }
            drawerLayout.closeDrawers();
            return true;
        });

        // Firebase: получаем список чатов залогіненогго користувача
        FirebaseUser currentUser = FirebaseAuth.getInstance().getCurrentUser();

        if (currentUser != null) {
            String currentUserId = currentUser.getUid();
            DatabaseReference chatRef = FirebaseDatabase.getInstance()
                    .getReference("chats");

            chatRef.addValueEventListener(new ValueEventListener() {
                @Override
                public void onDataChange(@NonNull DataSnapshot snapshot) {
                    chatList.clear();
                    filteredList.clear();

                    // Перебираємо всі елементи всередині "chats"
                    for (DataSnapshot child : snapshot.getChildren()) {
                        String key = child.getKey();
                        if (key != null && key.equals(currentUserId)) {
                            for (DataSnapshot chatSnapshot : child.getChildren()) {
                                Chat chat = chatSnapshot.getValue(Chat.class);
                                if (chat != null) {
                                    chatList.add(chat);
                                    Log.d("ChatList", "Додано чат: " + chat.getName());
                                }
                            }
                            break;
                        }
                    }

                    filteredList.addAll(chatList);
                    adapter.notifyDataSetChanged();
                }

                @Override
                public void onCancelled(@NonNull DatabaseError error) {
                    Toast.makeText(ChatListActivity.this, "Помилка: " + error.getMessage(), Toast.LENGTH_SHORT).show();
                }
            });
        }

        adapter = new ChatAdapter(filteredList, chat -> {
            // При выборе пользователя, открываем экран чата
            Intent intent = new Intent(ChatListActivity.this, ChatActivity.class);
            intent.putExtra("userId", chat.getUserId());
            startActivity(intent);
        });

        recyclerView.setLayoutManager(new LinearLayoutManager(this));
        recyclerView.setAdapter(adapter);

        searchView.setOnQueryTextListener(new SearchView.OnQueryTextListener() {
            @Override
            public boolean onQueryTextSubmit(String query) { return false; }

            @Override
            public boolean onQueryTextChange(String newText) {
                filteredList.clear();
                for (Chat chat : chatList) {
                    if (chat.getName().toLowerCase().contains(newText.toLowerCase())) {
                        filteredList.add(chat);
                    }
                }
                adapter.notifyDataSetChanged();
                return true;
            }
        });
    }
}
