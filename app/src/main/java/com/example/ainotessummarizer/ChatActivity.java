package com.example.ainotessummarizer;

import android.os.Bundle;
import android.view.View;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.TimeUnit;

import okhttp3.Call;
import okhttp3.Callback;
import okhttp3.MediaType;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.RequestBody;
import okhttp3.Response;

public class ChatActivity extends AppCompatActivity {
    private RecyclerView recyclerView;
    private EditText etMessage;
    private ImageView btnSend;
    private ChatAdapter adapter;
    private final List<ChatMessage> chatList = new ArrayList<>();

    private static final String ROLE_USER = "user";
    private static final String ROLE_MODEL = "model";
    private static final String TYPING_TEXT = "Typing...";

    private static final String API_KEY = BuildConfig.GEMINI_API_KEY;
    private static final String MODEL = "gemini-1.5-flash-latest";
    private static final String ENDPOINT = "https://generativelanguage.googleapis.com/v1beta/models/" + MODEL + ":generateContent";
    private static final MediaType JSON = MediaType.get("application/json; charset=utf-8");

    private final OkHttpClient client = new OkHttpClient.Builder()
            .connectTimeout(20, TimeUnit.SECONDS)
            .readTimeout(60, TimeUnit.SECONDS)
            .writeTimeout(20, TimeUnit.SECONDS)
            .build();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_chat);

        recyclerView = findViewById(R.id.chatRecyclerView);
        etMessage = findViewById(R.id.etMessage);
        btnSend = findViewById(R.id.btnSend);

        adapter = new ChatAdapter(chatList, this);
        LinearLayoutManager manager = new LinearLayoutManager(this);
        manager.setStackFromEnd(true);
        recyclerView.setLayoutManager(manager);
        recyclerView.setAdapter(adapter);

        if (API_KEY == null || API_KEY.trim().isEmpty()) {
            Toast.makeText(this, "Gemini API key missing. Add GEMINI_API_KEY in local.properties.", Toast.LENGTH_LONG).show();
        }

        btnSend.setOnClickListener(v -> {
            String userText = etMessage.getText().toString().trim();
            if (!userText.isEmpty()) {
                addMessage(userText, true);
                etMessage.setText("");
                callGeminiAPI();
            }
        });
    }

    private void addMessage(String message, boolean isUser) {
        String sentBy = isUser ? ROLE_USER : ROLE_MODEL;
        chatList.add(new ChatMessage(message, sentBy));
        runOnUiThread(() -> {
            adapter.notifyItemInserted(chatList.size() - 1);
            recyclerView.smoothScrollToPosition(chatList.size() - 1);
        });
    }

    private void setLoadingState(boolean loading) {
        runOnUiThread(() -> {
            btnSend.setEnabled(!loading);
            btnSend.setAlpha(loading ? 0.5f : 1f);
            etMessage.setEnabled(!loading);
        });
    }

    private void callGeminiAPI() {
        if (API_KEY == null || API_KEY.trim().isEmpty()) {
            addMessage("Config Error: API key missing. Set GEMINI_API_KEY in local.properties.", false);
            return;
        }

        setLoadingState(true);
        addMessage(TYPING_TEXT, false);

        JSONObject jsonBody;
        try {
            jsonBody = buildConversationRequestBody();
        } catch (JSONException e) {
            removeTypingIndicator();
            setLoadingState(false);
            addMessage("Request Error: Unable to build request.", false);
            return;
        }

        RequestBody body = RequestBody.create(jsonBody.toString(), JSON);
        Request request = new Request.Builder()
                .url(ENDPOINT + "?key=" + API_KEY)
                .post(body)
                .addHeader("Content-Type", "application/json")
                .build();

        client.newCall(request).enqueue(new Callback() {
            @Override
            public void onFailure(@NonNull Call call, @NonNull IOException e) {
                removeTypingIndicator();
                setLoadingState(false);
                addMessage("Connection Error: " + e.getMessage(), false);
            }

            @Override
            public void onResponse(@NonNull Call call, @NonNull Response response) throws IOException {
                String responseBody = response.body() != null ? response.body().string() : "";

                removeTypingIndicator();
                setLoadingState(false);

                if (response.isSuccessful()) {
                    handleSuccessfulResponse(responseBody);
                } else {
                    handleErrorResponse(response.code(), responseBody, response.header("Retry-After"));
                }
            }
        });
    }

    private JSONObject buildConversationRequestBody() throws JSONException {
        JSONArray contents = new JSONArray();

        for (ChatMessage message : chatList) {
            if (TYPING_TEXT.equals(message.getMessage())) {
                continue;
            }

            JSONObject content = new JSONObject();
            JSONObject part = new JSONObject();
            part.put("text", message.getMessage());

            content.put("parts", new JSONArray().put(part));
            content.put("role", ROLE_USER.equals(message.getSentBy()) ? ROLE_USER : ROLE_MODEL);
            contents.put(content);
        }

        JSONObject body = new JSONObject();
        body.put("contents", contents);
        return body;
    }

    private void handleSuccessfulResponse(String responseBody) {
        try {
            JSONObject jsonResponse = new JSONObject(responseBody);
            JSONArray candidates = jsonResponse.optJSONArray("candidates");

            if (candidates == null || candidates.length() == 0) {
                addMessage("Empty response from AI. Please try again.", false);
                return;
            }

            JSONObject firstCandidate = candidates.getJSONObject(0);
            JSONObject content = firstCandidate.optJSONObject("content");
            if (content == null) {
                addMessage("AI returned invalid content. Please retry.", false);
                return;
            }

            JSONArray parts = content.optJSONArray("parts");
            if (parts == null || parts.length() == 0) {
                addMessage("AI returned no text. Please retry.", false);
                return;
            }

            StringBuilder aiText = new StringBuilder();
            for (int i = 0; i < parts.length(); i++) {
                JSONObject part = parts.optJSONObject(i);
                if (part != null) {
                    String text = part.optString("text", "");
                    if (!text.isEmpty()) {
                        if (aiText.length() > 0) aiText.append("\n");
                        aiText.append(text);
                    }
                }
            }

            if (aiText.length() == 0) {
                addMessage("AI returned empty text. Please retry.", false);
            } else {
                addMessage(aiText.toString(), false);
            }
        } catch (JSONException e) {
            addMessage("Parsing Error: " + e.getMessage(), false);
        }
    }

    private void handleErrorResponse(int statusCode, String responseBody, String retryAfterHeader) {
        String apiMessage = extractApiErrorMessage(responseBody);

        switch (statusCode) {
            case 400:
                addMessage("Bad Request (400): " + (apiMessage.isEmpty() ? "Please check request format or prompt size." : apiMessage), false);
                break;
            case 404:
                addMessage("Not Found (404): Model/endpoint not found. Verify model name and API version.", false);
                break;
            case 429:
                String retryHint = (retryAfterHeader != null && !retryAfterHeader.isEmpty())
                        ? " Retry after " + retryAfterHeader + " seconds."
                        : " Please wait and try again.";
                addMessage("Rate Limit (429): " + (apiMessage.isEmpty() ? "Too many requests." : apiMessage) + retryHint, false);
                break;
            case 401:
            case 403:
                addMessage("Auth Error (" + statusCode + "): Invalid/unauthorized API key.", false);
                break;
            default:
                addMessage("API Error (" + statusCode + "): " + (apiMessage.isEmpty() ? "Unexpected server response." : apiMessage), false);
                break;
        }
    }

    private String extractApiErrorMessage(String responseBody) {
        if (responseBody == null || responseBody.isEmpty()) {
            return "";
        }

        try {
            JSONObject json = new JSONObject(responseBody);
            JSONObject errorObj = json.optJSONObject("error");
            if (errorObj != null) {
                return errorObj.optString("message", "");
            }
        } catch (JSONException ignored) {
            // Ignore parse failure and fallback to empty message.
        }

        return "";
    }

    private void removeTypingIndicator() {
        runOnUiThread(() -> {
            if (!chatList.isEmpty()) {
                int lastIndex = chatList.size() - 1;
                if (TYPING_TEXT.equals(chatList.get(lastIndex).getMessage())) {
                    chatList.remove(lastIndex);
                    adapter.notifyItemRemoved(lastIndex);
                }
            }
        });
    }
}
