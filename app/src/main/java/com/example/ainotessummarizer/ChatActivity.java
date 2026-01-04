package com.example.ainotessummarizer;

import android.os.Bundle;
import android.os.Handler;
import android.widget.EditText;   // ✅ ADDED
import android.widget.ImageView;  // ✅ ADDED
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;
import androidx.drawerlayout.widget.DrawerLayout;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView; // ✅ ADDED

import java.util.ArrayList;
import java.util.List;        // ✅ ADDED

import androidx.core.view.GravityCompat;
import android.view.View;

import com.google.android.material.bottomsheet.BottomSheetDialog;
import android.view.LayoutInflater;
import android.widget.LinearLayout;
import android.widget.Toast; // Testing ke liye

import java.lang.reflect.Field;
import androidx.customview.widget.ViewDragHelper;
import android.util.DisplayMetrics;

public class ChatActivity extends AppCompatActivity {

    RecyclerView recyclerView;
    EditText etMessage;
    ImageView btnSend, btnMic, btnOptionsPlus;
    ChatAdapter adapter;
    List<ChatMessage> chatList;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_chat);

        // Init Views
        recyclerView = findViewById(R.id.chatRecyclerView);
        etMessage = findViewById(R.id.etMessage);
        btnSend = findViewById(R.id.btnSend);
        btnMic = findViewById(R.id.btnMic);
        btnOptionsPlus = findViewById(R.id.btnOptionsPlus); // Initialized Attach button too
        DrawerLayout drawerLayout = findViewById(R.id.drawerLayout);


        // Setup Recycler
        chatList = new ArrayList<>();
        adapter = new ChatAdapter(chatList, this);
        LinearLayoutManager manager = new LinearLayoutManager(this);
        manager.setStackFromEnd(true);
        recyclerView.setLayoutManager(manager);
        recyclerView.setAdapter(adapter);

        // SEND BUTTON LOGIC
        btnSend.setOnClickListener(v -> {
            String userText = etMessage.getText().toString().trim();
            if (!userText.isEmpty()) {
                // 1. User ka message add karo
                addMessage(userText, true);
                etMessage.setText("");

                // 2. AI ko API request bhejo (Abhi Dummy hai)
                getAiResponse(userText);
            }
        });

        // MIC BUTTON LOGIC (Speech to Text)
        btnMic.setOnClickListener(v -> {
            Toast.makeText(this, "Listening...", Toast.LENGTH_SHORT).show();
        });

        // References
        ImageView btnMenu = findViewById(R.id.btnMenu);

       // Click listener to open drawer
        btnMenu.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                drawerLayout.openDrawer(GravityCompat.START);
            }
        });



        btnOptionsPlus.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                // Jab plus dabega, ye function call hoga
                showBottomSheetDialog();
            }
        });


    }

    private void showBottomSheetDialog() {
        // 1. BottomSheetDialog create karein
        final BottomSheetDialog bottomSheetDialog = new BottomSheetDialog(this);

        // Container ki jagah 'null' pass karein
        View bottomSheetView = LayoutInflater.from(this)
                .inflate(R.layout.layout_bottom_sheet, null);

        // 3. Sheet ke andar ke buttons dhundhein
        LinearLayout btnCamera = bottomSheetView.findViewById(R.id.sheetBtnCamera);
        LinearLayout btnGallery = bottomSheetView.findViewById(R.id.sheetBtnGallery);
        LinearLayout btnFile = bottomSheetView.findViewById(R.id.sheetBtnFile);

        // 4. In buttons par click listener lagayein (Abhi ke liye sirf Toast)
        btnCamera.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Toast.makeText(ChatActivity.this, "Camera clicked", Toast.LENGTH_SHORT).show();
                bottomSheetDialog.dismiss(); // Click ke baad sheet band karein
            }
        });

        btnGallery.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Toast.makeText(ChatActivity.this, "Gallery clicked", Toast.LENGTH_SHORT).show();
                bottomSheetDialog.dismiss();
            }
        });

        btnFile.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Toast.makeText(ChatActivity.this, "File clicked", Toast.LENGTH_SHORT).show();
                bottomSheetDialog.dismiss();
            }
        });

        // 5. View set karein aur dialog show karein
        bottomSheetDialog.setContentView(bottomSheetView);

        // Zaroori: Sheet ka background transparent karein taaki rounded corners dikhein
        try {
            bottomSheetDialog.getWindow().setBackgroundDrawableResource(android.R.color.transparent);
        } catch (Exception e) {
            e.printStackTrace();
        }

        bottomSheetDialog.show();
    }

    private void addMessage(String message, boolean isUser) {
        chatList.add(new ChatMessage(message, isUser));
        adapter.notifyItemInserted(chatList.size() - 1);
        recyclerView.smoothScrollToPosition(chatList.size() - 1);
    }

    private void getAiResponse(String query) {
        new Handler().postDelayed(() -> {
            String dummyResponse = "**AI Notes Summarizer:**\n\n Maine aapka message padha: _\"" + query + "\"_. \n\n* Point 1\n* Point 2\n* Point 3";
            addMessage(dummyResponse, false);
        }, 1000);
    }

}