package com.example.ainotessummarizer;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;
import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;
import java.util.List;
import io.noties.markwon.Markwon;

public class ChatAdapter extends RecyclerView.Adapter<RecyclerView.ViewHolder> {
    private List<ChatMessage> messageList;
    private Markwon markwon;

    public ChatAdapter(List<ChatMessage> messageList, Context context) {
        this.messageList = messageList;
        this.markwon = Markwon.create(context); // Setup Markdown renderer
    }

    @Override
    public int getItemViewType(int position) {
        return "user".equals(messageList.get(position).getSentBy()) ? 0 : 1;
    }

    @NonNull
    @Override
    public RecyclerView.ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        int layout = (viewType == 0) ? R.layout.item_chat_user : R.layout.item_chat_ai;
        View view = LayoutInflater.from(parent.getContext()).inflate(layout, parent, false);
        return (viewType == 0) ? new UserViewHolder(view) : new AiViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull RecyclerView.ViewHolder holder, int position) {
        String msg = messageList.get(position).getMessage();
        if (holder instanceof UserViewHolder) {
            ((UserViewHolder) holder).textMessage.setText(msg);
        } else {
            markwon.setMarkdown(((AiViewHolder) holder).textMessage, msg); // Renders Gemini markdown
        }
    }

    @Override public int getItemCount() { return messageList.size(); }

    static class UserViewHolder extends RecyclerView.ViewHolder {
        TextView textMessage;
        UserViewHolder(View v) { super(v); textMessage = v.findViewById(R.id.txtUserMessage); }
    }

    static class AiViewHolder extends RecyclerView.ViewHolder {
        TextView textMessage;
        AiViewHolder(View v) { super(v); textMessage = v.findViewById(R.id.txtAiMessage); }
    }
}