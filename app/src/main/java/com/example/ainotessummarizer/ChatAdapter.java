package com.example.ainotessummarizer;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView; // ✅ ADDED THIS IMPORT

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import java.util.List;

import io.noties.markwon.Markwon;

public class ChatAdapter extends RecyclerView.Adapter<RecyclerView.ViewHolder> {

    private List<ChatMessage> messageList;
    private Markwon markwon;

    public ChatAdapter(List<ChatMessage> messageList, Context context) {
        this.messageList = messageList;
        this.markwon = Markwon.create(context);
    }

    @Override
    public int getItemViewType(int position) {
        // 0 for User, 1 for AI
        return messageList.get(position).isUser() ? 0 : 1;
    }

    @NonNull
    @Override
    public RecyclerView.ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        if (viewType == 0) {
            // User Layout
            View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_chat_user, parent, false);
            return new UserViewHolder(view);
        } else {
            // AI Layout
            View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_chat_ai, parent, false);
            return new AiViewHolder(view);
        }
    }

    @Override
    public void onBindViewHolder(@NonNull RecyclerView.ViewHolder holder, int position) {
        String msg = messageList.get(position).getMessage();
        if (holder instanceof UserViewHolder) {
            ((UserViewHolder) holder).textMessage.setText(msg);
        } else {
            // Markwon needs a TextView, now it will recognize it
            markwon.setMarkdown(((AiViewHolder) holder).textMessage, msg);
        }
    }

    @Override
    public int getItemCount() {
        return messageList.size();
    }

    // ViewHolders
    static class UserViewHolder extends RecyclerView.ViewHolder {
        TextView textMessage;
        UserViewHolder(View v) {
            super(v);
            textMessage = v.findViewById(R.id.txtUserMessage);
        }
    }

    static class AiViewHolder extends RecyclerView.ViewHolder {
        TextView textMessage;
        AiViewHolder(View v) {
            super(v);
            textMessage = v.findViewById(R.id.txtAiMessage);
        }
    }
}