package com.example.ainotessummarizer;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.view.GravityCompat;
import androidx.drawerlayout.widget.DrawerLayout;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.google.android.material.bottomsheet.BottomSheetDialog;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import okhttp3.Call;
import okhttp3.Callback;
import okhttp3.MediaType;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.RequestBody;
import okhttp3.Response;

public class ChatActivity extends AppCompatActivity {

    RecyclerView recyclerView;
    EditText etMessage;
    ImageView btnSend, btnMic, btnOptionsPlus;
    ChatAdapter adapter;
    List<ChatMessage> chatList;
    DrawerLayout drawerLayout;

    // --- GEMINI API SETUP ---
    public static final String API_KEY = "AIzaSyDz87M6wyePxymnDkEAp40OsjMg93pGIWw";
    public static final MediaType JSON = MediaType.get("application/json; charset=utf-8");
    OkHttpClient client = new OkHttpClient();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_chat);

        // Init Views
        recyclerView = findViewById(R.id.chatRecyclerView);
        etMessage = findViewById(R.id.etMessage);
        btnSend = findViewById(R.id.btnSend);
        btnMic = findViewById(R.id.btnMic);
        btnOptionsPlus = findViewById(R.id.btnOptionsPlus);
        drawerLayout = findViewById(R.id.drawerLayout);
        ImageView btnMenu = findViewById(R.id.btnMenu);

        // Setup Recycler
        chatList = new ArrayList<>();
        // Note: Ensure your Adapter constructor matches. Using (List, Context) based on your code.
        adapter = new ChatAdapter(chatList, this);
        LinearLayoutManager manager = new LinearLayoutManager(this);
        manager.setStackFromEnd(true);
        recyclerView.setLayoutManager(manager);
        recyclerView.setAdapter(adapter);

        // SEND BUTTON LOGIC
        btnSend.setOnClickListener(v -> {
            String userText = etMessage.getText().toString().trim();
            if (!userText.isEmpty()) {
                // 1. User Message Add
                addMessage(userText, true);
                etMessage.setText("");

                // 2. Call Real API
                callGeminiAPI(userText);
            }
        });

        // MIC BUTTON
        btnMic.setOnClickListener(v -> Toast.makeText(this, "Listening...", Toast.LENGTH_SHORT).show());

        // MENU BUTTON (Drawer Open)
        btnMenu.setOnClickListener(v -> drawerLayout.openDrawer(GravityCompat.START));

        // PLUS BUTTON (Bottom Sheet)
        btnOptionsPlus.setOnClickListener(v -> showBottomSheetDialog());
    }

    // --- HELPER METHODS ---

    private void addMessage(String message, boolean isUser) {
        // ERROR FIX: Convert boolean to String for ChatMessage class
        String sentBy = isUser ? "user" : "bot";

        chatList.add(new ChatMessage(message, sentBy));

        // UI Updates on Main Thread
        runOnUiThread(() -> {
            adapter.notifyItemInserted(chatList.size() - 1);
            recyclerView.smoothScrollToPosition(chatList.size() - 1);
        });
    }

    private void callGeminiAPI(String question) {
        // Typing indicator
        addMessage("Typing...", false);

        JSONObject jsonBody = new JSONObject();
        try {
            JSONObject content = new JSONObject();
            JSONObject part = new JSONObject();
            part.put("text", question);
            JSONArray parts = new JSONArray();
            parts.put(part);
            content.put("parts", parts);
            JSONArray contents = new JSONArray();
            contents.put(content);
            jsonBody.put("contents", contents);
        } catch (JSONException e) {
            e.printStackTrace();
        }

        RequestBody body = RequestBody.create(jsonBody.toString(), JSON);
        Request request = new Request.Builder()
                .url("https://generativelanguage.googleapis.com/v1beta/models/gemini-1.5-flash:generateContent?key=" + API_KEY)
                .post(body)
                .build();

        client.newCall(request).enqueue(new Callback() {
            @Override
            public void onFailure(@NonNull Call call, @NonNull IOException e) {
                removeTypingIndicator();
                addMessage("Error: " + e.getMessage(), false);
            }

            @Override
            public void onResponse(@NonNull Call call, @NonNull Response response) throws IOException {
                removeTypingIndicator();

                String responseBody = response.body() != null ? response.body().string() : "";

                if (!response.isSuccessful()) {
                    addMessage(buildApiErrorMessage(response.code(), responseBody), false);
                    return;
                }

                try {
                    String result = extractAiText(responseBody);
                    if (result == null || result.trim().isEmpty()) {
                        addMessage("No response text returned by Gemini.", false);
                    } else {
                        addMessage(result.trim(), false);
                    }
                } catch (JSONException e) {
                    e.printStackTrace();
                    addMessage("Response parse failed. Please try again.", false);
                }
            }
        });
    }

    private String extractAiText(String responseBody) throws JSONException {
        JSONObject jsonResponse = new JSONObject(responseBody);
        JSONArray candidates = jsonResponse.optJSONArray("candidates");
        if (candidates == null || candidates.length() == 0) {
            return null;
        }

        JSONObject firstCandidate = candidates.optJSONObject(0);
        if (firstCandidate == null) {
            return null;
        }

        JSONObject content = firstCandidate.optJSONObject("content");
        if (content == null) {
            return null;
        }

        JSONArray parts = content.optJSONArray("parts");
        if (parts == null || parts.length() == 0) {
            return null;
        }

        JSONObject firstPart = parts.optJSONObject(0);
        return firstPart != null ? firstPart.optString("text", null) : null;
    }

    private String buildApiErrorMessage(int statusCode, String responseBody) {
        String apiMessage = "";
        try {
            JSONObject errorJson = new JSONObject(responseBody).optJSONObject("error");
            if (errorJson != null) {
                apiMessage = errorJson.optString("message", "");
            }
        } catch (JSONException ignored) {
        }

        if (statusCode == 429) {
            return "Gemini rate limit hit (429). Please wait and try again.";
        }
        if (statusCode == 404) {
            return "Gemini endpoint/model not found (404). Enable Generative Language API and verify model name.";
        }

        if (!apiMessage.isEmpty()) {
            return "Gemini API error (" + statusCode + "): " + apiMessage;
        }
        return "Gemini API error (" + statusCode + ").";
    }

    private void removeTypingIndicator() {
        runOnUiThread(() -> {
            if (!chatList.isEmpty() && chatList.get(chatList.size() - 1).getMessage().equals("Typing...")) {
                chatList.remove(chatList.size() - 1);
                adapter.notifyItemRemoved(chatList.size());
            }
        });
    }

    private void showBottomSheetDialog() {
        final BottomSheetDialog bottomSheetDialog = new BottomSheetDialog(this);
        View bottomSheetView = LayoutInflater.from(this).inflate(R.layout.layout_bottom_sheet, null);

        LinearLayout btnCamera = bottomSheetView.findViewById(R.id.sheetBtnCamera);
        LinearLayout btnGallery = bottomSheetView.findViewById(R.id.sheetBtnGallery);
        LinearLayout btnFile = bottomSheetView.findViewById(R.id.sheetBtnFile);

        btnCamera.setOnClickListener(v -> {
            Toast.makeText(ChatActivity.this, "Camera", Toast.LENGTH_SHORT).show();
            bottomSheetDialog.dismiss();
        });
        btnGallery.setOnClickListener(v -> {
            Toast.makeText(ChatActivity.this, "Gallery", Toast.LENGTH_SHORT).show();
            bottomSheetDialog.dismiss();
        });
        btnFile.setOnClickListener(v -> {
            Toast.makeText(ChatActivity.this, "File", Toast.LENGTH_SHORT).show();
            bottomSheetDialog.dismiss();
        });

        bottomSheetDialog.setContentView(bottomSheetView);
        try {
            bottomSheetDialog.getWindow().setBackgroundDrawableResource(android.R.color.transparent);
        } catch (Exception e) { e.printStackTrace(); }
        bottomSheetDialog.show();
    }
}