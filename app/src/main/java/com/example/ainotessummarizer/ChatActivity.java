package com.example.ainotessummarizer;

import android.os.Bundle;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.google.ai.client.generativeai.GenerativeModel;
import com.google.ai.client.generativeai.java.GenerativeModelFutures;
import com.google.ai.client.generativeai.type.Content;
import com.google.ai.client.generativeai.type.GenerateContentResponse;
import com.google.common.util.concurrent.FutureCallback;
import com.google.common.util.concurrent.Futures;
import com.google.common.util.concurrent.ListenableFuture;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.Executor;
import java.util.concurrent.Executors;

public class ChatActivity extends AppCompatActivity {
    private RecyclerView recyclerView;
    private EditText etMessage;
    private ImageView btnSend;
    private ChatAdapter adapter;
    private List<ChatMessage> chatList = new ArrayList<>();

    // SDK Variables
    private GenerativeModelFutures model;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_chat);

        // 1. Initialize Gemini Model using BuildConfig
        try {
            GenerativeModel gm = new GenerativeModel("gemini-1.5-flash", BuildConfig.GEMINI_API_KEY);
            model = GenerativeModelFutures.from(gm);
        } catch (Exception e) {
            Toast.makeText(this, "Model Initialization Failed", Toast.LENGTH_SHORT).show();
        }

        recyclerView = findViewById(R.id.chatRecyclerView);
        etMessage = findViewById(R.id.etMessage);
        btnSend = findViewById(R.id.btnSend);

        adapter = new ChatAdapter(chatList, this);
        LinearLayoutManager manager = new LinearLayoutManager(this);
        manager.setStackFromEnd(true); // Chat hamesha niche se start hoga
        recyclerView.setLayoutManager(manager);
        recyclerView.setAdapter(adapter);

        btnSend.setOnClickListener(v -> {
            String text = etMessage.getText().toString().trim();
            if (!text.isEmpty()) {
                sendMessageToGemini(text);
                etMessage.setText("");
            }
        });
    }

    private void sendMessageToGemini(String userPrompt) {
        // User message add karein
        addMessage(userPrompt, "user");

        // Typing indicator add karein
        addMessage("Typing...", "bot");

        Content content = new Content.Builder().addText(userPrompt).build();

        // SDK request
        ListenableFuture<GenerateContentResponse> response = model.generateContent(content);

        Futures.addCallback(response, new FutureCallback<GenerateContentResponse>() {
            @Override
            public void onSuccess(GenerateContentResponse result) {
                String aiResponse = result.getText();
                runOnUiThread(() -> {
                    removeTypingIndicator();
                    addMessage(aiResponse, "bot");
                });
            }

            @Override
            public void onFailure(Throwable t) {
                runOnUiThread(() -> {
                    removeTypingIndicator();
                    addMessage("Error: " + t.getMessage(), "bot");
                });
            }
        }, this.getMainExecutor()); // Ye ensures karta hai ki updates Main UI thread par hon
    }

    private void addMessage(String message, String role) {
        runOnUiThread(() -> {
            chatList.add(new ChatMessage(message, role));
            adapter.notifyItemInserted(chatList.size() - 1);
            recyclerView.smoothScrollToPosition(chatList.size() - 1);
        });
    }

    private void removeTypingIndicator() {
        if (!chatList.isEmpty()) {
            int lastIndex = chatList.size() - 1;
            if (chatList.get(lastIndex).getMessage().equals("Typing...")) {
                chatList.remove(lastIndex);
                adapter.notifyItemRemoved(lastIndex);
            }
        }
    }
}