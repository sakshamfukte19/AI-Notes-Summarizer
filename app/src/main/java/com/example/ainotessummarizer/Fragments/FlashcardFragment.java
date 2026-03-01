package com.example.ainotessummarizer.Fragments;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import com.google.android.material.floatingactionbutton.FloatingActionButton;

import com.example.ainotessummarizer.CreateFlashcardActivity;
import com.example.ainotessummarizer.FlashcardDetailActivity;
import com.example.ainotessummarizer.R;
import com.example.ainotessummarizer.adapter.FlashcardSetAdapter;
import com.example.ainotessummarizer.model.FlashcardSetModel;

import java.util.ArrayList;
import java.util.List;

public class FlashcardFragment extends Fragment {

    private RecyclerView recyclerView;
    private FlashcardSetAdapter adapter;
    private List<FlashcardSetModel> flashcardSets;
    private FloatingActionButton fabAddFlashcard;

    // CreateFlashcardActivity se naya set wapas receive karne ke liye
    private final ActivityResultLauncher<Intent> createFlashcardLauncher = registerForActivityResult(
            new ActivityResultContracts.StartActivityForResult(),
            result -> {
                if (result.getResultCode() == Activity.RESULT_OK && result.getData() != null) {
                    FlashcardSetModel newSet = (FlashcardSetModel) result.getData().getSerializableExtra("newFlashcardSet");
                    if (newSet != null) {
                        flashcardSets.add(newSet);
                        adapter.notifyItemInserted(flashcardSets.size() - 1);
                    }
                }
            });

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        // Aapke fragment ki XML file ka naam (e.g., fragment_flashcard.xml)
        View view = inflater.inflate(R.layout.fragment_flashcard, container, false);

        recyclerView = view.findViewById(R.id.rvFlashcardSets);
        fabAddFlashcard = view.findViewById(R.id.fabAddFlashcard);

        flashcardSets = new ArrayList<>();

        recyclerView.setLayoutManager(new LinearLayoutManager(getContext()));
        adapter = new FlashcardSetAdapter(flashcardSets, position -> {
            FlashcardSetModel clickedSet = flashcardSets.get(position);
            Intent intent = new Intent(getContext(), FlashcardDetailActivity.class);
            intent.putExtra("selectedFlashcardSet", clickedSet);
            startActivity(intent);
        });
        recyclerView.setAdapter(adapter);

        fabAddFlashcard.setOnClickListener(v -> {
            Intent intent = new Intent(getContext(), CreateFlashcardActivity.class);
            createFlashcardLauncher.launch(intent);
        });

        return view;
    }
}