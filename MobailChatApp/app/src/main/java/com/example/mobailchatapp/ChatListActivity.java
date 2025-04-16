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
import com.google.firebase.FirebaseApp;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import java.util.ArrayList;
import java.util.Iterator;
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

        FirebaseApp.initializeApp(this);
        FirebaseUser currentUser = FirebaseAuth.getInstance().getCurrentUser();

        if (currentUser != null) {
            String currentUserId = currentUser.getUid();
            toolbarTitle.setText(currentUser.getEmail());

            // Загружаем список чатов
            loadUserChats(currentUserId);
        }

        // Настройка бокового меню
        ActionBarDrawerToggle toggle = new ActionBarDrawerToggle(this, drawerLayout, toolbar,
                R.string.app_name, R.string.app_name);
        drawerLayout.addDrawerListener(toggle);
        toggle.syncState();

        navView.setNavigationItemSelectedListener(item -> {
            if (item.getItemId() == R.id.menu_settings) {
                startActivity(new Intent(ChatListActivity.this, SettingActivity.class));
            }
            drawerLayout.closeDrawers();
            return true;
        });

        adapter = new ChatAdapter(filteredList, chat -> {
            Intent intent = new Intent(ChatListActivity.this, ChatActivity.class);
            intent.putExtra("chatId", chat.getChatId());
            intent.putExtra("userId", chat.getChatId());
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

    private void loadUserChats(String currentUserId) {
        DatabaseReference userChatsRef = FirebaseDatabase.getInstance()
                .getReference("userChats").child(currentUserId);

        userChatsRef.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                List<String> chatIds = new ArrayList<>();
                for (DataSnapshot chatSnapshot : snapshot.getChildren()) {
                    chatIds.add(chatSnapshot.getKey());
                }
                loadChatsByIds(chatIds, currentUserId);
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {
                Toast.makeText(ChatListActivity.this, "Ошибка: " + error.getMessage(), Toast.LENGTH_SHORT).show();
            }
        });
    }

    private void loadChatsByIds(List<String> chatIds, String currentUserId) {
        DatabaseReference chatsRef = FirebaseDatabase.getInstance().getReference("chats");

        chatList.clear();
        filteredList.clear();

        for (String chatId : chatIds) {
            chatsRef.child(chatId).addListenerForSingleValueEvent(new ValueEventListener() {
                @Override
                public void onDataChange(@NonNull DataSnapshot snapshot) {
                    String lastMessage = "";

                    // Ищем последнее сообщение
                    DataSnapshot messagesSnapshot = snapshot.child("messages");
                    Iterator<DataSnapshot> iterator = messagesSnapshot.getChildren().iterator();
                    if (iterator.hasNext()) {
                        DataSnapshot messageSnap = iterator.next();
                        lastMessage = messageSnap.child("message").getValue(String.class);
                    }

                    Chat chat = new Chat();
                    chat.setChatId(snapshot.getKey());
                    chat.setLastMessage(lastMessage);
                    chat.setUserId(getOtherUserIdFromChatId(chatId, currentUserId));
                    chat.setName(chat.getUserId()); // Пока отображаем userId, позже можно заменить на username

                    chatList.add(chat);
                    filteredList.add(chat);
                    adapter.notifyDataSetChanged();
                }

                @Override
                public void onCancelled(@NonNull DatabaseError error) {
                    Log.e("ChatList", "Ошибка при загрузке чата: " + error.getMessage());
                }
            });
        }
    }

    private String getOtherUserIdFromChatId(String chatId, String currentUserId) {
        String[] parts = chatId.split("_");
        if (parts.length == 2) {
            return parts[0].equals(currentUserId) ? parts[1] : parts[0];
        }
        return "";
    }
}
