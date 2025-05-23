package com.example.mobailchatapp;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.recyclerview.widget.RecyclerView;

import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ChildEventListener;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;

import java.util.List;

public class ChatAdapter extends RecyclerView.Adapter<ChatAdapter.ChatViewHolder> {

    private List<Chat> chatList;
    private OnChatClickListener onChatClickListener;
    private DatabaseReference database;

    public ChatAdapter(List<Chat> chatList, OnChatClickListener onChatClickListener) {
        this.chatList = chatList;
        this.onChatClickListener = onChatClickListener;

        // Подключаемся к Firebase
        database = FirebaseDatabase.getInstance().getReference("chats");
        listenForChanges();
    }

    @Override
    public ChatViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_chat, parent, false);
        return new ChatViewHolder(view);
    }

    @Override
    public void onBindViewHolder(ChatViewHolder holder, int position) {
        Chat chat = chatList.get(position);
        holder.nameTextView.setText(chat.getName());
        holder.lastMessageTextView.setText(chat.getLastMessage());

        holder.itemView.setOnClickListener(v -> onChatClickListener.onChatClick(chat));
    }

    @Override
    public int getItemCount() {
        return chatList.size();
    }

    public static class ChatViewHolder extends RecyclerView.ViewHolder {
        TextView nameTextView;
        TextView lastMessageTextView;

        public ChatViewHolder(View itemView) {
            super(itemView);
            nameTextView = itemView.findViewById(R.id.chat_name);
            lastMessageTextView = itemView.findViewById(R.id.last_message);
        }
    }

    public interface OnChatClickListener {
        void onChatClick(Chat chat);
    }

    // Слушаем изменения в реальном времени
    private void listenForChanges() {
        database.addChildEventListener(new ChildEventListener() {
            @Override
            public void onChildAdded(DataSnapshot dataSnapshot, String previousChildName) {
                Chat chat = dataSnapshot.getValue(Chat.class);
                chatList.add(chat);
                notifyItemInserted(chatList.size() - 1); // Добавляем новый элемент в список
            }

            @Override
            public void onChildChanged(DataSnapshot dataSnapshot, String previousChildName) {
                Chat chat = dataSnapshot.getValue(Chat.class);
                int index = findChatPosition(chat.getChatId());
                if (index != -1) {
                    chatList.set(index, chat);
                    notifyItemChanged(index); // Обновляем элемент в списке
                }
            }

            @Override
            public void onChildRemoved(DataSnapshot dataSnapshot) {
                // Обрабатываем удаление чата
            }

            @Override
            public void onChildMoved(DataSnapshot dataSnapshot, String previousChildName) {
                // Обрабатываем изменения позиции чата
            }

            @Override
            public void onCancelled(DatabaseError databaseError) {
                // Обрабатываем ошибки
            }
        });
    }

    private int findChatPosition(String chatId) {
        for (int i = 0; i < chatList.size(); i++) {
            if (chatList.get(i).getChatId().equals(chatId)) {
                return i;
            }
        }
        return -1;
    }
}
