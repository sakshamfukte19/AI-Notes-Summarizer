package com.example.ainotessummarizer;

import android.content.Intent;
import android.os.Bundle;

import androidx.appcompat.app.AppCompatActivity;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentManager;

import com.example.ainotessummarizer.Fragments.FlashcardFragment;
import com.example.ainotessummarizer.Fragments.HistoryFragment;
import com.example.ainotessummarizer.Fragments.HomeFragment;
import com.example.ainotessummarizer.Fragments.InformationFragment;
import com.example.ainotessummarizer.Fragments.ProfileFragment;
import com.example.ainotessummarizer.session.SessionManager;

/**
 * HomeActivity hosts all bottom-navigation fragments.
 *
 * Architecture Note:
 * Fragment state is preserved by using show()/hide() instead of replace().
 * All fragment instances are kept alive in the FragmentManager. Switching tabs
 * does NOT recreate fragments, so scroll positions, data, and UI state are
 * maintained throughout the session.
 *
 * Navigation Note:
 * The AI Chat tab opens ChatActivity. The Profile tab provides the logout action.
 * Back press on a non-home tab navigates to Home; a second back press exits.
 *
 * Animation Note:
 * Fragment transitions use fade-in/fade-out animations for smooth crossfade.
 * Activity transitions use slide animations for natural navigation flow.
 */
public class HomeActivity extends AppCompatActivity {

    // Fragment tag constants — used to find existing instances
    private static final String TAG_HOME = "tab_home";
    private static final String TAG_FLASHCARD = "tab_flashcard";
    private static final String TAG_HISTORY = "tab_history";
    private static final String TAG_PROFILE = "tab_profile";

    private Fragment activeFragment;
    private BottomNavHelper navHelper;
    private SessionManager sessionManager;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_home);

        sessionManager = new SessionManager(this);

        // Initialize all fragments once and add them to the back-stack cache
        // This runs only on first creation (not on rotation)
        if (savedInstanceState == null) {
            setupFragments();
        } else {
            // Restore active fragment reference after rotation
            activeFragment = getSupportFragmentManager()
                    .findFragmentByTag(TAG_HOME);
        }

        setupBottomNav();
    }

    /**
     * Creates and adds all fragment instances to the FragmentManager.
     * Only the HomeFragment is shown initially; the rest are hidden.
     *
     * Using show()/hide() means fragments are never destroyed between tab switches.
     * All fragments are added in a single transaction for consistency.
     */
    private void setupFragments() {
        FragmentManager fm = getSupportFragmentManager();

        Fragment homeFragment = new HomeFragment();
        Fragment flashcardFragment = new FlashcardFragment(); // NAYA FRAGMENT YAHAN DALO
        Fragment historyFragment = new HistoryFragment();
        Fragment profileFragment = new ProfileFragment();

        activeFragment = homeFragment;

        fm.beginTransaction()
                .add(R.id.fragmentContainer, homeFragment, TAG_HOME)
                .add(R.id.fragmentContainer, flashcardFragment, TAG_FLASHCARD) // YAHAN UPDATE KAREIN
                .add(R.id.fragmentContainer, historyFragment, TAG_HISTORY)
                .add(R.id.fragmentContainer, profileFragment, TAG_PROFILE)
                .hide(flashcardFragment) // YAHAN UPDATE KAREIN
                .hide(historyFragment)
                .hide(profileFragment)
                .commit();
    }

    private void setupBottomNav() {
        navHelper = new BottomNavHelper(this, tabId -> {
            switch (tabId) {
                case 1:
                    showFragment(TAG_HOME);
                    break;
                case 2:
                    showFragment(TAG_FLASHCARD);
                    break;
                case 3:
                    showFragment(TAG_HISTORY);
                    break;
                case 4:
                    // AI Chat opens a separate activity with slide animation
                    Intent chatIntent = new Intent(HomeActivity.this, ChatActivity.class);
                    startActivity(chatIntent);
                    overridePendingTransition(R.anim.slide_in_right, R.anim.slide_out_left);
                    break;
                case 5:
                    showFragment(TAG_PROFILE);
                    break;
            }
        });
    }

    /**
     * Shows the target fragment and hides the currently active one.
     * Uses fade animations for a smooth crossfade transition.
     *
     * @param tag The tag of the fragment to show.
     */
    private void showFragment(String tag) {
        Fragment target = getSupportFragmentManager().findFragmentByTag(tag);
        if (target == null || target == activeFragment)
            return;

        getSupportFragmentManager()
                .beginTransaction()
                .setCustomAnimations(R.anim.fade_in, R.anim.fade_out)
                .hide(activeFragment)
                .show(target)
                .commit();
        activeFragment = target;
    }

    /**
     * Back press behavior:
     * - If on a non-home tab → navigate back to Home tab
     * - If already on Home → exit the app
     */
    @Override
    public void onBackPressed() {
        Fragment homeFragment = getSupportFragmentManager().findFragmentByTag(TAG_HOME);
        if (activeFragment != homeFragment) {
            showFragment(TAG_HOME);
            navHelper.selectTab(1); // reset nav highlight to Home
        } else {
            super.onBackPressed();
        }
    }

    @Override
    protected void onResume() {
        super.onResume();
        // Refresh session last-active timestamp on every resume
        sessionManager.updateLastActive();

        // Reset nav to Home when coming back from ChatActivity
        // so that the AI Chat tab doesn't stay selected
        Fragment homeFragment = getSupportFragmentManager().findFragmentByTag(TAG_HOME);
        if (activeFragment != homeFragment) {
            showFragment(TAG_HOME);
        }
        navHelper.selectTab(1);
    }
}