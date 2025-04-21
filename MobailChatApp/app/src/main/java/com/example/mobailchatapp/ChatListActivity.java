package com.example.mobailchatapp;

import android.content.Intent;
import android.os.Bundle;
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
    private boolean firstLaunch = true;

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
            String currentUserId = currentUser.getUid();
            String otherUserId = chat.getUserId();

            // Якщо чат ще не створений, створити його
            if (chat.getChatId() == null || chat.getChatId().isEmpty()) {
                String chatId = currentUserId.compareTo(otherUserId) < 0 ?
                        currentUserId + "_" + otherUserId : otherUserId + "_" + currentUserId;

                startChatWithUser(otherUserId); // створення чату

                // Створюємо намір для переходу
                Intent intent = new Intent(ChatListActivity.this, ChatActivity.class);
                intent.putExtra("chatId", chatId);
                intent.putExtra("userId", otherUserId);
                startActivity(intent);
            } else {
                // Інакше — перейти в існуючий чат
                Intent intent = new Intent(ChatListActivity.this, ChatActivity.class);
                intent.putExtra("chatId", chat.getChatId());
                intent.putExtra("userId", chat.getUserId());
                startActivity(intent);
            }
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

        if (query.isEmpty()) {
            filteredList.addAll(chatList);  // Показуємо всі наявні чати без "віртуальних"
            adapter.notifyDataSetChanged();
            return;
        }

        for (Chat chat : chatList) {  // Пошук по існуючих чатах
            if (chat.getName() != null && chat.getName().toLowerCase().contains(query.toLowerCase())) {
                filteredList.add(chat);
            }
        }

        // Пошук по всіх юзерах (для створення нового чату)
        for (Map.Entry<String, User> entry : allUsers.entrySet()) {
            String userId = entry.getKey();
            User user = entry.getValue();

            if (userId.equals(currentUser.getUid())) continue;

            // Перевірка: чи існує чат з цим користувачем?
            boolean chatExists = false;
            for (Chat chat : chatList) {
                String chatUid = chat.getUserId();
                if (chatUid != null && chatUid.equals(userId)) {
                    chatExists = true;
                    break;
                }
            }

            if (!chatExists && user.getName().toLowerCase().contains(query.toLowerCase())) {
                Chat virtualChat = new Chat();
                virtualChat.setName(user.getName());
                virtualChat.setUserId(userId);
                virtualChat.setLastMessage("Натисни, щоб створити чат");
                filteredList.add(virtualChat);
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
                @Override
                public void onDataChange(@NonNull DataSnapshot snapshot) {
                    chatList.clear();
                    filteredList.clear();
                    for (DataSnapshot chatSnap : snapshot.getChildren()) {
                        String chatId = chatSnap.getKey();
                        dbRef.child("chats").child(chatId)
                            .addListenerForSingleValueEvent(new ValueEventListener() {
                                    @Override
                                    public void onDataChange(@NonNull DataSnapshot chatData) {
                                        Chat chat = chatData.getValue(Chat.class);
                                        if (chat != null) {
                                            chat.setChatId(chatId);
                                            String otherUserId = getOtherUserId(chatId);
                                            User user = allUsers.get(otherUserId);
                                            if (user != null) chat.setName(user.getName());

                                            // Подгружаем последнее сообщение
                                            dbRef.child("messages")
                                                    .child(chatId)
                                                    .limitToLast(1) // Берем только последнее сообщение
                                                    .addListenerForSingleValueEvent(new ValueEventListener() {
                                                        @Override
                                                        public void onDataChange(@NonNull DataSnapshot messageSnap) {
                                                            if (messageSnap.exists()) {
                                                                String lastMessage = messageSnap.getChildren().iterator().next().child("message").getValue(String.class);
                                                                chat.setLastMessage(lastMessage != null ? lastMessage : "Нет сообщений");
                                                            } else {
                                                                chat.setLastMessage("Нет сообщений");
                                                            }
                                                            chatList.add(chat);
                                                            filteredList.add(chat);
                                                            adapter.notifyDataSetChanged();
                                                        }

                                                        @Override
                                                        public void onCancelled(@NonNull DatabaseError error) {
                                                            chat.setLastMessage("Не удалось загрузить сообщение");
                                                            chatList.add(chat);
                                                            filteredList.add(chat);
                                                            adapter.notifyDataSetChanged();
                                                        }
                                                    });
                                        }
                                    }

                                    @Override
                                    public void onCancelled(@NonNull DatabaseError error) {}
                                });
                    }
                }

                @Override
                public void onCancelled(@NonNull DatabaseError error) {}
            });
    }
    @Override
    protected void onResume(){
        super.onResume();
        if(this.firstLaunch){
            this.firstLaunch = false;
            return;
        }
        loadUserChats();
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

        DatabaseReference chatRef = dbRef.child("chats").child(chatId);

        Map<String, Object> chatData = new HashMap<>();
        chatData.put("lastMessage", "Чат створено");
        chatData.put("isGroup", false);

        Map<String, String> members = new HashMap<>();
        members.put("userId1", currentUserId);
        members.put("userId2", otherUserId);
        chatData.put("members", members);

        chatRef.setValue(chatData);
        dbRef.child("userChats").child(currentUserId).child(chatId).setValue(true);
        dbRef.child("userChats").child(otherUserId).child(chatId).setValue(true);

        Toast.makeText(this, "Чат створено", Toast.LENGTH_SHORT).show();
    }

}
