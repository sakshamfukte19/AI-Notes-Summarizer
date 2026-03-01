package com.example.ainotessummarizer.adapter;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.DiffUtil;
import androidx.recyclerview.widget.ListAdapter;
import androidx.recyclerview.widget.RecyclerView;

import com.example.ainotessummarizer.R;
import com.example.ainotessummarizer.databinding.ItemHistoryBinding;
import com.example.ainotessummarizer.model.ChatModel;

import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Locale;
import java.util.Objects;

/**
 * Adapter for displaying chat history items.
 * Uses ListAdapter + DiffUtil for efficient, animation-friendly updates.
 * No dummy data — list starts empty and is populated via {@link #submitList}.
 */
public class HistoryAdapter extends ListAdapter<ChatModel, HistoryAdapter.HistoryViewHolder> {

    /** Callback for item interactions — wire up in the future. */
    public interface OnHistoryClickListener {
        void onItemClick(ChatModel chat);
    }

    private OnHistoryClickListener listener;

    private static final DiffUtil.ItemCallback<ChatModel> DIFF_CALLBACK =
            new DiffUtil.ItemCallback<>() {
                @Override
                public boolean areItemsTheSame(@NonNull ChatModel oldItem,
                                               @NonNull ChatModel newItem) {
                    return Objects.equals(oldItem.getChatId(), newItem.getChatId());
                }

                @Override
                public boolean areContentsTheSame(@NonNull ChatModel oldItem,
                                                  @NonNull ChatModel newItem) {
                    return Objects.equals(oldItem.getTitle(), newItem.getTitle())
                            && Objects.equals(oldItem.getLastMessage(), newItem.getLastMessage())
                            && oldItem.getTimestamp() == newItem.getTimestamp()
                            && oldItem.isBookmarked() == newItem.isBookmarked();
                }
            };

    public HistoryAdapter() {
        super(DIFF_CALLBACK);
    }

    public void setOnHistoryClickListener(OnHistoryClickListener listener) {
        this.listener = listener;
    }

    @NonNull
    @Override
    public HistoryViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        ItemHistoryBinding binding = ItemHistoryBinding.inflate(
                LayoutInflater.from(parent.getContext()), parent, false);
        return new HistoryViewHolder(binding);
    }

    @Override
    public void onBindViewHolder(@NonNull HistoryViewHolder holder, int position) {
        holder.bind(getItem(position));
    }

    class HistoryViewHolder extends RecyclerView.ViewHolder {

        private final ItemHistoryBinding binding;

        HistoryViewHolder(@NonNull ItemHistoryBinding binding) {
            super(binding.getRoot());
            this.binding = binding;

            binding.getRoot().setOnClickListener(v -> {
                if (listener != null && getAdapterPosition() != RecyclerView.NO_POSITION) {
                    listener.onItemClick(getItem(getAdapterPosition()));
                }
            });
        }

        void bind(ChatModel chat) {
            binding.tvChatTitle.setText(chat.getTitle());
            binding.tvLastMessage.setText(chat.getLastMessage());

            // Show/hide bookmark icon
            binding.ivBookmarkIcon.setVisibility(
                    chat.isBookmarked() ? View.VISIBLE : View.GONE);

            // Format timestamp
            if (chat.getTimestamp() > 0) {
                SimpleDateFormat sdf = new SimpleDateFormat("MMM dd, hh:mm a", Locale.getDefault());
                binding.tvTimestamp.setText(sdf.format(new Date(chat.getTimestamp())));
            } else {
                binding.tvTimestamp.setText("");
            }
        }
    }
}
