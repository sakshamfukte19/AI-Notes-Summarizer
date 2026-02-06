package com.example.ainotessummarizer; // Apna package name check kar lena

import android.app.Activity;
import android.graphics.Color;
import android.view.View;
import android.view.animation.OvershootInterpolator;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;

public class BottomNavHelper {

    private final Activity activity;
    private final NavSelectionListener listener;

    // Views
    private LinearLayout btnHome, btnInfo, btnHistory, btnProfile;
    private ImageView imgHome, imgInfo, imgHistory, imgProfile;
    private TextView txtHome, txtInfo, txtHistory, txtProfile;

    // Interface (Callback) - Taaki Activity ko pata chale ki kya click hua
    public interface NavSelectionListener {
        void onTabSelected(int tabId);
    }

    // Constructor
    public BottomNavHelper(Activity activity, NavSelectionListener listener) {
        this.activity = activity;
        this.listener = listener;
        initViews();
    }

    private void initViews() {
        // IDs find kar rahe hain
        btnHome = activity.findViewById(R.id.btn_home);
        btnInfo = activity.findViewById(R.id.btn_info);
        btnHistory = activity.findViewById(R.id.btn_history);
        btnProfile = activity.findViewById(R.id.btn_profile);

        imgHome = activity.findViewById(R.id.img_home);
        imgInfo = activity.findViewById(R.id.img_info);
        imgHistory = activity.findViewById(R.id.img_history);
        imgProfile = activity.findViewById(R.id.img_profile);

        txtHome = activity.findViewById(R.id.txt_home);
        txtInfo = activity.findViewById(R.id.txt_info);
        txtHistory = activity.findViewById(R.id.txt_history);
        txtProfile = activity.findViewById(R.id.txt_profile);

        setupClickListeners();
    }

    private void setupClickListeners() {
        // HOME
        btnHome.setOnClickListener(v -> {
            updateUI(imgHome, txtHome);
            listener.onTabSelected(1); // 1 = Home
        });

        // INFO
        btnInfo.setOnClickListener(v -> {
            updateUI(imgInfo, txtInfo);
            listener.onTabSelected(2); // 2 = Info
        });

        // HISTORY
        btnHistory.setOnClickListener(v -> {
            updateUI(imgHistory, txtHistory);
            listener.onTabSelected(3); // 3 = History
        });

        // PROFILE
        btnProfile.setOnClickListener(v -> {
            updateUI(imgProfile, txtProfile);
            listener.onTabSelected(4); // 4 = Profile
        });
    }

    // Wo Animation wala code yahan aa gaya
    private void updateUI(ImageView selectedImage, TextView selectedText) {
        int activeColor = Color.parseColor("#6200EE");
        int inactiveColor = Color.parseColor("#757575");

        ImageView[] allImages = {imgHome, imgInfo, imgHistory, imgProfile};
        TextView[] allTexts = {txtHome, txtInfo, txtHistory, txtProfile};

        // Reset All
        for (int i = 0; i < allImages.length; i++) {
            allImages[i].setColorFilter(inactiveColor);
            allTexts[i].setVisibility(View.GONE);

            allImages[i].animate()
                    .scaleX(1f)
                    .scaleY(1f)
                    .translationY(0)
                    .setDuration(200)
                    .start();
        }

        // Animate Selected
        selectedImage.setColorFilter(activeColor);
        selectedText.setVisibility(View.VISIBLE);

        selectedImage.animate()
                .scaleX(1.2f)
                .scaleY(1.2f)
                .translationY(-5)
                .setDuration(300)
                .setInterpolator(new OvershootInterpolator())
                .start();
    }
}