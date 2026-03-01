package com.example.ainotessummarizer.Fragments;

import android.content.Intent;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;

import com.example.ainotessummarizer.ChatActivity;
import com.example.ainotessummarizer.R;
import com.example.ainotessummarizer.session.SessionManager;
import com.google.android.material.bottomnavigation.BottomNavigationView;
import com.google.android.material.card.MaterialCardView;

import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Locale;

/** Home tab — dynamic landing page with greeting, stats, and quick actions. */
public class HomeFragment extends Fragment {

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, ViewGroup container,
            Bundle savedInstanceState) {
        return inflater.inflate(R.layout.fragment_home, container, false);
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        SessionManager session = new SessionManager(requireContext());

        // ── Greeting based on time of day ──
        TextView tvGreeting = view.findViewById(R.id.tvGreeting);
        if (tvGreeting != null) {
            int hour = java.util.Calendar.getInstance().get(java.util.Calendar.HOUR_OF_DAY);
            String greeting;
            if (hour < 12)
                greeting = "Good morning 👋";
            else if (hour < 17)
                greeting = "Good afternoon 👋";
            else
                greeting = "Good evening 👋";
            tvGreeting.setText(greeting);
        }

        // ── Username from email ──
        TextView tvUserName = view.findViewById(R.id.tvUserName);
        if (tvUserName != null) {
            String email = session.getUserEmail();
            if (email != null && email.contains("@")) {
                String name = email.substring(0, email.indexOf('@'));
                name = name.substring(0, 1).toUpperCase() + name.substring(1);
                tvUserName.setText(name + "'s Notes");
            }
        }

        // ── Profile icon (top-right) → navigate to Profile tab ──
        android.widget.ImageView ivProfile = view.findViewById(R.id.ivProfile);
        if (ivProfile != null) {
            ivProfile.setOnClickListener(v -> {
                BottomNavigationView nav = requireActivity().findViewById(R.id.bottomNavigation);
                if (nav != null)
                    nav.setSelectedItemId(R.id.nav_profile);
            });
        }

        // ── Navigate to chat ──
        View cardStartAI = view.findViewById(R.id.cardStartAI);
        if (cardStartAI != null) {
            cardStartAI.setOnClickListener(v -> openChat());
        }

        View btnStart = view.findViewById(R.id.btnStart);
        if (btnStart != null) {
            btnStart.setOnClickListener(v -> openChat());
        }

        MaterialCardView cardChatAI = view.findViewById(R.id.cardChatAI);
        if (cardChatAI != null) {
            cardChatAI.setOnClickListener(v -> openChat());
        }

        MaterialCardView cardHistory = view.findViewById(R.id.cardHistory);
        if (cardHistory != null) {
            cardHistory.setOnClickListener(v -> {
                BottomNavigationView nav = requireActivity().findViewById(R.id.bottomNavigation);
                if (nav != null)
                    nav.setSelectedItemId(R.id.nav_history);
            });
        }

        MaterialCardView cardBookmarks = view.findViewById(R.id.cardBookmarks);
        if (cardBookmarks != null) {
            cardBookmarks.setOnClickListener(v -> {
                BottomNavigationView nav = requireActivity().findViewById(R.id.bottomNavigation);
                if (nav != null)
                    nav.setSelectedItemId(R.id.nav_profile);
            });
        }
    }

    private void openChat() {
        startActivity(new Intent(requireActivity(), ChatActivity.class));
    }
}
