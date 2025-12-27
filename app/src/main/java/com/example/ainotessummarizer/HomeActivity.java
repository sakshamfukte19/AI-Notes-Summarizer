package com.example.ainotessummarizer;

import android.os.Bundle;

import androidx.appcompat.app.AppCompatActivity;
import androidx.fragment.app.Fragment;

import com.example.ainotessummarizer.Fragments.HomeFragment;
import com.example.ainotessummarizer.Fragments.InformationFragment;
import com.example.ainotessummarizer.Fragments.ProfileFragment;
import com.google.android.material.bottomnavigation.BottomNavigationView;

public class HomeActivity extends AppCompatActivity {

    private BottomNavigationView bottomNavigationView;

    private final HomeFragment homeFragment = new HomeFragment();
    private final ProfileFragment profileFragment = new ProfileFragment();
    private final InformationFragment informationFragment = new InformationFragment();

    //  ----------------    create the settings and history fragment object here  ---------------------

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        // IMPORTANT
        setContentView(R.layout.activity_home);

        bottomNavigationView = findViewById(R.id.bottomNavigation);

        // Load default fragment
        loadFragment(homeFragment);

        bottomNavigationView.setOnItemSelectedListener(item -> {

            int id = item.getItemId();

            if (id == R.id.nav_home) {
                loadFragment(homeFragment);
                return true;

            } else if (id == R.id.nav_info) {
                loadFragment(informationFragment);
                return true;

            } else if (id == R.id.nav_profile) {
                loadFragment(profileFragment);
                return true;
            }
            else if (id == R.id.nav_history) {
                // -----------    Paste the name of the history fragment object here  -----------------
                loadFragment(profileFragment);
                return true;
            }
            else if (id == R.id.nav_settings) {
                // -----------    Paste the name of the settings fragment object here  ----------------
                loadFragment(profileFragment);
                return true;
            }

            return false;
        });
    }

    private void loadFragment(Fragment fragment) {
        getSupportFragmentManager()
                .beginTransaction()
                .replace(R.id.fragmentContainer, fragment)
                .commit();
    }
}
