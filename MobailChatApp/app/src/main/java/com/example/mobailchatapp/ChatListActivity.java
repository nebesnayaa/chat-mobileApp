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
    private TextView toolbarTitle;
    private FirebaseUser currentUser;
    private DatabaseReference dbRef;

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

        toolbarTitle = findViewById(R.id.toolbar_title);
        currentUser = FirebaseAuth.getInstance().getCurrentUser();

        dbRef = FirebaseDatabase
                .getInstance("https://chat-mobileapp-b05c2-default-rtdb.europe-west1.firebasedatabase.app/")
                .getReference();

        if (currentUser != null) {
            String uid = currentUser.getUid();
            dbRef.child("users").child(uid).child("name")
                    .addListenerForSingleValueEvent(new ValueEventListener() {
                        @Override
                        public void onDataChange(@NonNull DataSnapshot snapshot) {
                            String userName = snapshot.getValue(String.class);
                            toolbarTitle.setText(userName != null ? userName : "Користувач");
                        }

                        @Override
                        public void onCancelled(@NonNull DatabaseError error) {
                            Log.e("USER_LOAD", "❌ Не вдалося зчитати логін", error.toException());
                            toolbarTitle.setText("Користувач");
                        }
                    });
        }

        ActionBarDrawerToggle toggle = new ActionBarDrawerToggle(this, drawerLayout, toolbar,
                R.string.app_name, R.string.app_name);
        drawerLayout.addDrawerListener(toggle);
        toggle.syncState();

        navView.setNavigationItemSelectedListener(item -> {
            if (item.getItemId() == R.id.menu_settings) {
                Intent intent = new Intent(ChatListActivity.this, SettingActivity.class);
                startActivity(intent);
            }
            drawerLayout.closeDrawers();
            return true;
        });

        // Настройка адаптера
        adapter = new ChatAdapter(filteredList, chat -> {
            Intent intent = new Intent(ChatListActivity.this, ChatActivity.class);
            intent.putExtra("userId", chat.getUserId());
            startActivity(intent);
        });
        recyclerView.setLayoutManager(new LinearLayoutManager(this));
        recyclerView.setAdapter(adapter);

        // Загрузка чатов
        if (currentUser != null) {
            String currentUserId = currentUser.getUid();
            dbRef.child("chats").child(currentUserId)
                    .addValueEventListener(new ValueEventListener() {
                        @Override
                        public void onDataChange(@NonNull DataSnapshot snapshot) {
                            chatList.clear();
                            filteredList.clear();

                            for (DataSnapshot chatSnapshot : snapshot.getChildren()) {
                                Chat chat = chatSnapshot.getValue(Chat.class);
                                if (chat != null) {
                                    chatList.add(chat);
                                    Log.d("ChatList", "Добавлен чат: " + chat.getName());
                                }
                            }

                            filteredList.addAll(chatList);
                            adapter.notifyDataSetChanged();
                        }

                        @Override
                        public void onCancelled(@NonNull DatabaseError error) {
                            Toast.makeText(ChatListActivity.this, "Ошибка: " + error.getMessage(), Toast.LENGTH_SHORT).show();
                        }
                    });
        }

        // Поиск
        searchView.setOnQueryTextListener(new SearchView.OnQueryTextListener() {
            @Override
            public boolean onQueryTextSubmit(String query) {
                return false;
            }

            @Override
            public boolean onQueryTextChange(String newText) {
                filteredList.clear();
                for (Chat chat : chatList) {
                    if (chat.getName() != null &&
                            chat.getName().toLowerCase().contains(newText.toLowerCase())) {
                        filteredList.add(chat);
                    }
                }
                adapter.notifyDataSetChanged();
                return true;
            }
        });
    }
}
