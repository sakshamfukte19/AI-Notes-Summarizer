package com.example.ainotessummarizer.Fragments;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;

import com.example.ainotessummarizer.adapter.HistoryAdapter;
import com.example.ainotessummarizer.databinding.FragmentHistoryBinding;

/**
 * History tab — displays previous AI chat sessions.
 *
 * UI-only structure:
 * • Search bar (UI-only, no filtering logic)
 * • Clear History button (UI-only, no action)
 * • RecyclerView with HistoryAdapter (empty by default)
 * • Empty state shown when adapter has no items
 *
 * MVVM-ready: call {@code adapter.submitList(list)} from a ViewModel observer
 * to populate the list without changing any UI code.
 */
public class HistoryFragment extends Fragment {

    private FragmentHistoryBinding binding;
    private HistoryAdapter adapter;

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        binding = FragmentHistoryBinding.inflate(inflater, container, false);
        return binding.getRoot();
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        setupRecyclerView();
        setupSearchBar();
        setupClearHistory();
        updateEmptyState();
    }

    private void setupRecyclerView() {
        adapter = new HistoryAdapter();
        binding.rvHistory.setLayoutManager(new LinearLayoutManager(requireContext()));
        binding.rvHistory.setAdapter(adapter);

        // Future: observe ViewModel LiveData and call adapter.submitList(list)
        // Then call updateEmptyState() inside the observer.
    }

    /** UI-only search bar setup. Wire up TextWatcher filtering in the future. */
    private void setupSearchBar() {
        // Future: add TextWatcher to filter adapter list via ViewModel
    }

    /** UI-only clear history button. Wire up confirmation dialog + clearing in the future. */
    private void setupClearHistory() {
        // Future: show confirmation dialog, then call ViewModel.clearHistory()
    }

    /** Shows / hides the empty state based on adapter item count. */
    private void updateEmptyState() {
        boolean isEmpty = adapter.getItemCount() == 0;
        binding.layoutEmptyHistory.setVisibility(isEmpty ? View.VISIBLE : View.GONE);
        binding.rvHistory.setVisibility(isEmpty ? View.GONE : View.VISIBLE);
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        binding = null;  // Avoid memory leaks
    }
}