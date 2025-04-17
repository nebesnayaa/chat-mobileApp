package com.example.mobailchatapp;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.recyclerview.widget.RecyclerView;

import java.util.List;

public class ChatAdapter extends RecyclerView.Adapter<ChatAdapter.ChatViewHolder> {

    private List<Chat> chatList;
    private OnChatClickListener onChatClickListener;

    public ChatAdapter(List<Chat> chatList, OnChatClickListener onChatClickListener) {
        this.chatList = chatList;
        this.onChatClickListener = onChatClickListener;
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
}
