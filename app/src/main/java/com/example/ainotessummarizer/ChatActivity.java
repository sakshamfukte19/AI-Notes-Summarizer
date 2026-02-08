package com.example.ainotessummarizer;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.Toast;
import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
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
    ImageView btnSend;
    ChatAdapter adapter;
    List<ChatMessage> chatList;

    // Replace with your actual Gemini API Key
    public static final String API_KEY = "YOUR_VALID_GEMINI_API_KEY";
    public static final MediaType JSON = MediaType.get("application/json; charset=utf-8");
    OkHttpClient client = new OkHttpClient();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_chat);

        recyclerView = findViewById(R.id.chatRecyclerView);
        etMessage = findViewById(R.id.etMessage);
        btnSend = findViewById(R.id.btnSend);

        chatList = new ArrayList<>();
        adapter = new ChatAdapter(chatList, this);
        LinearLayoutManager manager = new LinearLayoutManager(this);
        manager.setStackFromEnd(true); // Ensures chat starts from bottom
        recyclerView.setLayoutManager(manager);
        recyclerView.setAdapter(adapter);

        btnSend.setOnClickListener(v -> {
            String userText = etMessage.getText().toString().trim();
            if (!userText.isEmpty()) {
                addMessage(userText, true); // Add user message to UI
                etMessage.setText("");
                callGeminiAPI(userText); // Trigger AI response
            }
        });
    }

    private void addMessage(String message, boolean isUser) {
        String sentBy = isUser ? "user" : "bot";
        chatList.add(new ChatMessage(message, sentBy));
        runOnUiThread(() -> {
            adapter.notifyItemInserted(chatList.size() - 1);
            recyclerView.smoothScrollToPosition(chatList.size() - 1);
        });
    }

    private void callGeminiAPI(String question) {
        addMessage("Typing...", false); // Show indicator

        JSONObject jsonBody = new JSONObject();
        try {
            JSONObject content = new JSONObject();
            JSONObject part = new JSONObject();
            part.put("text", question);
            content.put("parts", new JSONArray().put(part));
            jsonBody.put("contents", new JSONArray().put(content));
        } catch (JSONException e) { e.printStackTrace(); }

        RequestBody body = RequestBody.create(jsonBody.toString(), JSON);
        Request request = new Request.Builder()
                .url("https://generativelanguage.googleapis.com/v1beta/models/gemini-1.5-flash:generateContent?key=" + API_KEY)
                .post(body)
                .build();

        client.newCall(request).enqueue(new Callback() {
            @Override
            public void onFailure(@NonNull Call call, @NonNull IOException e) {
                removeTypingIndicator();
                addMessage("Connection Error: " + e.getMessage(), false);
            }

            @Override
            public void onResponse(@NonNull Call call, @NonNull Response response) throws IOException {
                removeTypingIndicator();
                if (response.isSuccessful()) {
                    try {
                        JSONObject jsonResponse = new JSONObject(response.body().string());
                        String result = jsonResponse.getJSONArray("candidates")
                                .getJSONObject(0).getJSONObject("content")
                                .getJSONArray("parts").getJSONObject(0).getString("text");
                        addMessage(result, false);
                    } catch (Exception e) { addMessage("Parsing Error.", false); }
                } else {
                    addMessage("API Error: " + response.code(), false);
                }
            }
        });
    }

    private void removeTypingIndicator() {
        runOnUiThread(() -> {
            if (!chatList.isEmpty() && chatList.get(chatList.size() - 1).getMessage().equals("Typing...")) {
                chatList.remove(chatList.size() - 1);
                adapter.notifyItemRemoved(chatList.size());
            }
        });
    }
}