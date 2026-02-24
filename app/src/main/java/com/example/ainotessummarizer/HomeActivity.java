package com.example.ainotessummarizer;

import android.content.Intent;
import android.os.Bundle;
import android.widget.Toast;
import androidx.appcompat.app.AppCompatActivity;
import androidx.fragment.app.Fragment;

import com.example.ainotessummarizer.Fragments.BookmarkFragment;
import com.example.ainotessummarizer.Fragments.HomeFragment;
import com.example.ainotessummarizer.Fragments.HistoryFragment;
import com.example.ainotessummarizer.Fragments.InformationFragment;
import com.google.android.material.floatingactionbutton.FloatingActionButton;

public class HomeActivity extends AppCompatActivity {

    // Sirf Helper Class ka object banayenge
    private BottomNavHelper navHelper;
    private FloatingActionButton fab;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_home);

        fab = findViewById(R.id.fab);

        // Default Fragment Load

        loadFragment(new HomeFragment());

        // --- MAGIC LINE ---
        // Saara navigation logic ab iss ek line me hai
        navHelper = new BottomNavHelper(this, tabId -> {

            // Ye code tab chalega jab user click karega
            if (tabId == 1) {
                loadFragment(new HomeFragment());
            }
            else if (tabId == 2) {
                loadFragment(new InformationFragment());
            }
            else if (tabId == 3) {
                loadFragment(new HistoryFragment()); // History Fragment
            }
            else if (tabId == 4) {
                loadFragment(new BookmarkFragment());
            }
        });


        // FAB click alag se handle kar lo
        fab.setOnClickListener(v -> {
            Toast.makeText(this, "AI Chat Opening...", Toast.LENGTH_SHORT).show();
            Intent intent = new Intent(HomeActivity.this, ChatActivity.class);
            startActivity(intent);
        });
    }

    private void loadFragment(Fragment fragment) {
        getSupportFragmentManager()
                .beginTransaction()
                .replace(R.id.fragmentContainer, fragment)
                .commit();
    }
}