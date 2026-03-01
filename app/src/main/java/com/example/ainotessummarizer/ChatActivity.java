package com.example.ainotessummarizer;

import android.content.Intent;
import android.os.Bundle;
import android.speech.RecognizerIntent;
import android.view.View;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.Toast;

import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.view.GravityCompat;
import androidx.drawerlayout.widget.DrawerLayout;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.example.ainotessummarizer.session.SessionManager;
import com.google.android.material.bottomsheet.BottomSheetDialog;
import com.google.android.material.navigation.NavigationView;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;

import org.json.JSONArray;
import org.json.JSONObject;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

import okhttp3.*;

public class ChatActivity extends AppCompatActivity {

    private RecyclerView recyclerView;
    private EditText etMessage;
    private ImageView btnSend, btnMenu, btnOptionsPlus, btnMic, btnNewChat;
    private DrawerLayout drawerLayout;

    private ChatAdapter adapter;
    private List<ChatMessage> chatList = new ArrayList<>();

    private String currentChatId;
    private DatabaseReference dbRef;

    private static final int SPEECH_REQUEST_CODE = 100;

    private static final String GROQ_URL = "https://api.groq.com/openai/v1/chat/completions";
    private static final String GROQ_API_KEY = BuildConfig.GROK_API_KEY;

    private OkHttpClient client = new OkHttpClient();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_chat);

        // ---------------- SESSION CHECK ----------------
        SessionManager sessionManager = new SessionManager(this);
        String userEmail = sessionManager.getUserEmail();

        if (userEmail == null || !sessionManager.isSessionValid()) {
            Toast.makeText(this, "Session expired. Please login again.", Toast.LENGTH_SHORT).show();
            startActivity(new Intent(this, LoginActivity.class)
                    .addFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK));
            finish();
            return;
        }

        String safeEmail = userEmail.replace(".", "_").replace("@", "_at_");

        dbRef = FirebaseDatabase.getInstance()
                .getReference("UserChats")
                .child(safeEmail);

        // ---------------- UI INIT ----------------
        drawerLayout = findViewById(R.id.drawerLayout);
        recyclerView = findViewById(R.id.chatRecyclerView);
        etMessage = findViewById(R.id.etMessage);
        btnSend = findViewById(R.id.btnSend);
        btnMenu = findViewById(R.id.btnMenu);
        btnOptionsPlus = findViewById(R.id.btnOptionsPlus);
        btnMic = findViewById(R.id.btnMic);
        btnNewChat = findViewById(R.id.btnNewChat);

        setupRecyclerView();
        startNewChat();

        btnOptionsPlus.setOnClickListener(v -> showBottomSheet());
        btnMic.setOnClickListener(v -> startVoiceInput());
        btnMenu.setOnClickListener(v -> drawerLayout.openDrawer(GravityCompat.START));
        btnNewChat.setOnClickListener(v -> startNewChat());

        btnSend.setOnClickListener(v -> {
            String text = etMessage.getText() != null
                    ? etMessage.getText().toString().trim()
                    : "";

            if (!text.isEmpty()) {
                sendMessage(text);
                etMessage.setText("");
            }
        });

        NavigationView navigationView = findViewById(R.id.navView);
        navigationView.setNavigationItemSelectedListener(item -> {
            drawerLayout.closeDrawer(GravityCompat.START);
            return true;
        });
    }

    // ---------------- START NEW CHAT ----------------
    private void startNewChat() {
        chatList.clear();
        adapter.notifyDataSetChanged();

        currentChatId = dbRef.push().getKey();

        addMessage(
                "Hello! I can summarize your notes.\n\nChoose format:\n• Short Summary\n• Detailed Breakdown\n• Key Bullet Points",
                "bot");
    }

    // ---------------- BOTTOM SHEET ----------------
    private void showBottomSheet() {
        View view = getLayoutInflater().inflate(R.layout.layout_bottom_sheet, null);
        BottomSheetDialog dialog = new BottomSheetDialog(this);
        dialog.setContentView(view);
        dialog.show();
    }

    // ---------------- VOICE INPUT ----------------
    private void startVoiceInput() {
        Intent intent = new Intent(RecognizerIntent.ACTION_RECOGNIZE_SPEECH);
        intent.putExtra(RecognizerIntent.EXTRA_LANGUAGE_MODEL,
                RecognizerIntent.LANGUAGE_MODEL_FREE_FORM);
        intent.putExtra(RecognizerIntent.EXTRA_LANGUAGE,
                Locale.getDefault());
        intent.putExtra(RecognizerIntent.EXTRA_PROMPT,
                "Speak your note...");

        try {
            startActivityForResult(intent, SPEECH_REQUEST_CODE);
        } catch (Exception e) {
            Toast.makeText(this, "Voice not supported", Toast.LENGTH_SHORT).show();
        }
    }

    @Override
    protected void onActivityResult(int requestCode,
            int resultCode,
            @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        if (requestCode == SPEECH_REQUEST_CODE &&
                resultCode == RESULT_OK &&
                data != null) {

            ArrayList<String> result = data.getStringArrayListExtra(RecognizerIntent.EXTRA_RESULTS);

            if (result != null && !result.isEmpty()) {
                etMessage.setText(result.get(0));
            }
        }
    }

    // ---------------- SEND MESSAGE ----------------
    private void sendMessage(String userPrompt) {

        addMessage(userPrompt, "user");
        saveMessageToFirebase(new ChatMessage(userPrompt, "user"));

        addMessage("Typing...", "bot");

        JSONObject jsonBody = new JSONObject();

        try {
            jsonBody.put("model", "llama-3.3-70b-versatile");

            JSONArray messages = new JSONArray();
            messages.put(new JSONObject()
                    .put("role", "user")
                    .put("content", userPrompt));

            jsonBody.put("messages", messages);

        } catch (Exception e) {
            e.printStackTrace();
        }

        RequestBody body = RequestBody.create(
                jsonBody.toString(),
                MediaType.get("application/json; charset=utf-8"));

        Request request = new Request.Builder()
                .url(GROQ_URL)
                .header("Authorization", "Bearer " + GROQ_API_KEY)
                .post(body)
                .build();

        client.newCall(request).enqueue(new Callback() {

            @Override
            public void onFailure(Call call, IOException e) {
                runOnUiThread(() -> {
                    removeTypingIndicator();
                    addMessage("Network error. Please try again.", "bot");
                });
            }

            @Override
            public void onResponse(Call call, Response response) throws IOException {

                if (!response.isSuccessful()) {
                    runOnUiThread(() -> {
                        removeTypingIndicator();
                        addMessage("Server error. Try again later.", "bot");
                    });
                    return;
                }

                try {
                    String data = response.body().string();

                    String aiResponse = new JSONObject(data)
                            .getJSONArray("choices")
                            .getJSONObject(0)
                            .getJSONObject("message")
                            .getString("content");

                    runOnUiThread(() -> {
                        removeTypingIndicator();
                        addMessage(aiResponse, "bot");
                        saveMessageToFirebase(new ChatMessage(aiResponse, "bot"));
                    });

                } catch (Exception e) {
                    e.printStackTrace();
                }
            }
        });
    }

    // ---------------- SAVE MESSAGE ----------------
    private void saveMessageToFirebase(ChatMessage message) {
        if (currentChatId != null && dbRef != null) {
            dbRef.child(currentChatId)
                    .push()
                    .setValue(message);
        }
    }

    // ---------------- ADD MESSAGE ----------------
    private void addMessage(String message, String role) {
        runOnUiThread(() -> {
            chatList.add(new ChatMessage(message, role));
            adapter.notifyItemInserted(chatList.size() - 1);
            recyclerView.smoothScrollToPosition(chatList.size() - 1);
        });
    }

    // ---------------- REMOVE TYPING ----------------
    private void removeTypingIndicator() {
        if (!chatList.isEmpty()) {
            int lastIndex = chatList.size() - 1;

            if (chatList.get(lastIndex).getMessage().equals("Typing...")) {
                chatList.remove(lastIndex);
                adapter.notifyItemRemoved(lastIndex);
            }
        }
    }

    // ---------------- SETUP RECYCLER ----------------
    private void setupRecyclerView() {
        adapter = new ChatAdapter(chatList, this);
        recyclerView.setLayoutManager(new LinearLayoutManager(this));
        recyclerView.setAdapter(adapter);
    }

    @Override
    public void onBackPressed() {
        super.onBackPressed();
        overridePendingTransition(R.anim.slide_in_left, R.anim.slide_out_right);
    }
}