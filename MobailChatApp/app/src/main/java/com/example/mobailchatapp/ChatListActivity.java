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
import com.google.firebase.database.*;

import java.util.*;

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

    private Map<String, User> allUsers = new HashMap<>();

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
                startActivity(new Intent(this, SettingActivity.class));
            }
            drawerLayout.closeDrawers();
            return true;
        });

        adapter = new ChatAdapter(filteredList, chat -> {
            Intent intent = new Intent(ChatListActivity.this, ChatActivity.class);
            intent.putExtra("chatId", chat.getChatId());
            intent.putExtra("userId", chat.getUserId());
            startActivity(intent);
        });
        recyclerView.setLayoutManager(new LinearLayoutManager(this));
        recyclerView.setAdapter(adapter);

        if (currentUser != null) {
            loadAllUsersAndChats();
        }

        searchView.setOnQueryTextListener(new SearchView.OnQueryTextListener() {
            @Override public boolean onQueryTextSubmit(String query) { return false; }
            @Override public boolean onQueryTextChange(String newText) {
                filterChatsByName(newText);
                return true;
            }
        });
    }

    private void filterChatsByName(String query) {
        filteredList.clear();
        for (Chat chat : chatList) {
            if (chat.getName() != null && chat.getName().toLowerCase().contains(query.toLowerCase())) {
                filteredList.add(chat);
            }
        }
        adapter.notifyDataSetChanged();
    }

    private void loadAllUsersAndChats() {
        dbRef.child("users").addListenerForSingleValueEvent(new ValueEventListener() {
            @Override public void onDataChange(@NonNull DataSnapshot snapshot) {
                for (DataSnapshot snap : snapshot.getChildren()) {
                    String uid = snap.getKey();
                    String name = snap.child("name").getValue(String.class);
                    allUsers.put(uid, new User(uid, name));
                }
                loadUserChats();
            }

            @Override public void onCancelled(@NonNull DatabaseError error) {}
        });
    }

    private void loadUserChats() {
        String currentUserId = currentUser.getUid();
        dbRef.child("userChats").child(currentUserId)
                .addListenerForSingleValueEvent(new ValueEventListener() {
                    @Override public void onDataChange(@NonNull DataSnapshot snapshot) {
                        chatList.clear();
                        filteredList.clear();
                        for (DataSnapshot chatSnap : snapshot.getChildren()) {
                            String chatId = chatSnap.getKey();
                            dbRef.child("chats").child(chatId)
                                    .addListenerForSingleValueEvent(new ValueEventListener() {
                                        @Override public void onDataChange(@NonNull DataSnapshot chatData) {
                                            Chat chat = chatData.getValue(Chat.class);
                                            if (chat != null) {
                                                chat.setChatId(chatId);
                                                String otherUserId = getOtherUserId(chatId);
                                                User user = allUsers.get(otherUserId);
                                                if (user != null) chat.setName(user.getName());
                                                chatList.add(chat);
                                                filteredList.add(chat);
                                                adapter.notifyDataSetChanged();
                                            }
                                        }

                                        @Override public void onCancelled(@NonNull DatabaseError error) {}
                                    });
                        }
                    }

                    @Override public void onCancelled(@NonNull DatabaseError error) {}
                });
    }

    private String getOtherUserId(String chatId) {
        String[] parts = chatId.split("_");
        if (parts.length != 2) return "";
        return parts[0].equals(currentUser.getUid()) ? parts[1] : parts[0];
    }

    // Функция создания чата при выборе пользователя
    private void startChatWithUser(String otherUserId) {
        String currentUserId = currentUser.getUid();
        String chatId = currentUserId.compareTo(otherUserId) < 0 ?
                currentUserId + "_" + otherUserId : otherUserId + "_" + currentUserId;

        dbRef.child("chats").child(chatId).child("lastMessage").setValue("Чат создан");
        dbRef.child("userChats").child(currentUserId).child(chatId).setValue(true);
        dbRef.child("userChats").child(otherUserId).child(chatId).setValue(true);
    }
}
