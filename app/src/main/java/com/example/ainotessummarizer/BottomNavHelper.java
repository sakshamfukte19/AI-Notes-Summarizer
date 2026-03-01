package com.example.ainotessummarizer;

import android.app.Activity;
import android.view.MenuItem;
import android.view.View;

import androidx.annotation.NonNull;

import com.google.android.material.bottomnavigation.BottomNavigationView;
import com.google.android.material.navigation.NavigationBarView;

/**
 * BottomNavHelper manages the Material 3 BottomNavigationView.
 *
 * Architecture Note:
 * This helper wraps the BottomNavigationView and provides a clean callback
 * interface for tab selection. It handles the menu item → tab ID mapping
 * and provides programmatic tab selection for back-press scenarios.
 *
 * The BottomNavigationView uses Material 3 styling with:
 * - Pill-shaped active indicator
 * - Smooth icon/label transitions (built-in)
 * - Proper ripple feedback
 */
public class BottomNavHelper {

    private final Activity activity;
    private final NavSelectionListener listener;
    private BottomNavigationView bottomNav;

    public interface NavSelectionListener {
        void onTabSelected(int tabId);
    }

    public BottomNavHelper(Activity activity, NavSelectionListener listener) {
        this.activity = activity;
        this.listener = listener;
        initViews();
    }

    private void initViews() {
        bottomNav = activity.findViewById(R.id.bottomNavigation);

        bottomNav.setOnItemSelectedListener(item -> {
            int itemId = item.getItemId();

            if (itemId == R.id.nav_home) {
                listener.onTabSelected(1);
            } else if (itemId == R.id.nav_flashcard) {
                listener.onTabSelected(2);
            } else if (itemId == R.id.nav_history) {
                listener.onTabSelected(3);
            } else if (itemId == R.id.nav_ai_chat) {
                listener.onTabSelected(4);
            } else if (itemId == R.id.nav_profile) {
                listener.onTabSelected(5);
            }

            // Add a subtle scale animation to the selected item icon
            animateSelectedItem(item);

            return true;
        });

        // Set Home as the default selected item
        bottomNav.setSelectedItemId(R.id.nav_home);
    }

    /**
     * Adds a subtle bounce animation to the selected navigation item's icon.
     */
    private void animateSelectedItem(MenuItem item) {
        View itemView = bottomNav.findViewById(item.getItemId());
        if (itemView != null) {
            itemView.animate()
                    .scaleX(1.1f).scaleY(1.1f)
                    .setDuration(150)
                    .withEndAction(() ->
                            itemView.animate()
                                    .scaleX(1f).scaleY(1f)
                                    .setDuration(150)
                                    .start()
                    )
                    .start();
        }
    }

    /**
     * Programmatically selects a tab (used by HomeActivity on back-press to restore Home).
     *
     * @param tabId The tab ID to visually activate (1=Home, 2=Info, 3=History, 4=AI Chat, 5=Profile).
     */
    public void selectTab(int tabId) {
        switch (tabId) {
            case 1:
                bottomNav.setSelectedItemId(R.id.nav_home);
                break;
            case 2:
                bottomNav.setSelectedItemId(R.id.nav_flashcard);
                break;
            case 3:
                bottomNav.setSelectedItemId(R.id.nav_history);
                break;
            case 4:
                bottomNav.setSelectedItemId(R.id.nav_ai_chat);
                break;
            case 5:
                bottomNav.setSelectedItemId(R.id.nav_profile);
                break;
        }
    }

    /**
     * Returns the underlying BottomNavigationView for direct access if needed.
     */
    public BottomNavigationView getBottomNavigationView() {
        return bottomNav;
    }
}