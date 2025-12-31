package com.example.ainotessummarizer;
import android.graphics.Color;
import android.os.Bundle;
import android.view.View;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;

import androidx.appcompat.app.AppCompatActivity;
import androidx.fragment.app.Fragment;

import com.example.ainotessummarizer.Fragments.HomeFragment;
import com.example.ainotessummarizer.Fragments.InformationFragment;
import com.example.ainotessummarizer.Fragments.ProfileFragment;
import com.example.ainotessummarizer.R;
import com.google.android.material.floatingactionbutton.FloatingActionButton;
import android.view.animation.OvershootInterpolator;

public class HomeActivity extends AppCompatActivity {

    // Wrappers (Click karne ke liye)
    LinearLayout btnHome, btnInfo, btnHistory, btnProfile;

    // Icons (Color badalne ke liye)
    ImageView imgHome, imgInfo, imgHistory, imgProfile;

    // Texts (Dikhaane/Chupaane ke liye)
    TextView txtHome, txtInfo, txtHistory, txtProfile;

    FloatingActionButton fab;

    // Fragments declare kar lo...

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_home);

        // 1. Initializing Views
        btnHome = findViewById(R.id.btn_home);
        btnInfo = findViewById(R.id.btn_info);
        btnHistory = findViewById(R.id.btn_history);
        btnProfile = findViewById(R.id.btn_profile);

        imgHome = findViewById(R.id.img_home);
        imgInfo = findViewById(R.id.img_info);
        imgHistory = findViewById(R.id.img_history);
        imgProfile = findViewById(R.id.img_profile);

        txtHome = findViewById(R.id.txt_home);
        txtInfo = findViewById(R.id.txt_info);
        txtHistory = findViewById(R.id.txt_history);
        txtProfile = findViewById(R.id.txt_profile);

        fab = findViewById(R.id.fab);

        // Load Default
        loadFragment(new HomeFragment());

        // 2. Setting Click Listeners on Layouts (Buttons)

        btnHome.setOnClickListener(v -> {
            loadFragment(new HomeFragment());
            updateNavigation(imgHome, txtHome); // Pass specific Image and Text
        });

        btnInfo.setOnClickListener(v -> {
            loadFragment(new InformationFragment());
            updateNavigation(imgInfo, txtInfo);
        });

        btnHistory.setOnClickListener(v -> {
            loadFragment(new ProfileFragment());
            updateNavigation(imgHistory, txtHistory);
        });

        btnProfile.setOnClickListener(v -> {
            loadFragment(new ProfileFragment());
            updateNavigation(imgProfile, txtProfile);
        });

        // Fab listener...
    }

    // Naya Update Navigation Function (With Bouncy Animation)
    private void updateNavigation(ImageView selectedImage, TextView selectedText) {

        // Colors
        int activeColor = Color.parseColor("#6200EE");
        int inactiveColor = Color.parseColor("#757575");

        // ---------------- STEP 1: RESET ALL (Sabko wapas normal size aur grey karo) ----------------

        // Helper arrays to loop through all items quickly
        ImageView[] allImages = {imgHome, imgInfo, imgHistory, imgProfile};
        TextView[] allTexts = {txtHome, txtInfo, txtHistory, txtProfile};

        for (int i = 0; i < allImages.length; i++) {
            // Color Grey karo
            allImages[i].setColorFilter(inactiveColor);

            // Text Chupa do
            allTexts[i].setVisibility(View.GONE);

            // Animation Reset: Size wapas 1x kar do (Normal)
            allImages[i].animate()
                    .scaleX(1f)
                    .scaleY(1f)
                    .translationY(0) // Wapas apni jagah par
                    .setDuration(200)
                    .start();
        }

        // ---------------- STEP 2: ANIMATE SELECTED (Jo click hua use bada karo) ----------------

        // Color Purple
        selectedImage.setColorFilter(activeColor);

        // Text Dikhao
        selectedText.setVisibility(View.VISIBLE);

        // --- THE POPUP ANIMATION ---
        // Icon ko thoda bada (1.2 times) aur thoda upar uthaenge
        selectedImage.animate()
                .scaleX(1.2f)    // X axis par bada
                .scaleY(1.2f)    // Y axis par bada
                .translationY(-5) // Thoda sa upar move karega
                .setDuration(300) // 300ms ki speed
                .setInterpolator(new android.view.animation.OvershootInterpolator()) // BOING Effect!
                .start();
    }

    private void loadFragment(Fragment fragment) {
        getSupportFragmentManager()
                .beginTransaction()
                .replace(R.id.fragmentContainer, fragment)
                .commit();
    }
}