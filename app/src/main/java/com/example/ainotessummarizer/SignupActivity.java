package com.example.ainotessummarizer;

import android.content.Intent;
import android.os.Bundle;
import android.util.Patterns;
import android.widget.Toast;
import androidx.appcompat.app.AppCompatActivity;

import com.google.android.material.button.MaterialButton;
import com.google.android.material.checkbox.MaterialCheckBox;
import com.google.android.material.textfield.TextInputEditText;
import com.google.android.material.textfield.TextInputLayout;

public class SignupActivity extends AppCompatActivity {

    private TextInputEditText emailEditText, passwordEditText, confirmPasswordEditText;
    private MaterialCheckBox termsCheckbox;
    private MaterialButton registerButton;

    // ✅ ADDED: DatabaseHelper
    private DatabaseHelper db;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_signup);

        // Bind UI elements
        emailEditText = findViewById(R.id.emailEditText);
        passwordEditText = findViewById(R.id.passwordEditText);
        confirmPasswordEditText = findViewById(R.id.confirmPasswordEditText);
        termsCheckbox = findViewById(R.id.termsCheckbox);
        registerButton = findViewById(R.id.registerButton);

        // ✅ ADDED: Initialize database
        db = new DatabaseHelper(this);

        registerButton.setOnClickListener(v -> registerUser());
    }

    private void registerUser() {
        String email = emailEditText.getText().toString().trim();
        String password = passwordEditText.getText().toString().trim();
        String confirmPassword = confirmPasswordEditText.getText().toString().trim();
        boolean isTermsAgreed = termsCheckbox.isChecked();
        clearErrors();

        if(email.isEmpty()){
            emailEditText.setError("Email field can't be empty");
        }


        if (password.isEmpty()) {
            ((TextInputLayout)findViewById(R.id.passwordInputLayout))
                    .setError("password cannot be empty");
            return;
        }

        if (!Patterns.EMAIL_ADDRESS.matcher(email).matches()) {
            ((TextInputLayout)findViewById(R.id.emailInputLayout))
                    .setError("Enter a valid email");
            return;
        }

        if (password.length() < 6) {
            ((TextInputLayout)findViewById(R.id.passwordInputLayout))
                    .setError("Password must be at least 6 characters");
            return;
        }

        if (!password.equals(confirmPassword)) {
            ((TextInputLayout)findViewById(R.id.confirmPasswordInputLayout))
                    .setError("Passwords do not match");
            return;
        }

        if (!isTermsAgreed) {
            Toast.makeText(this, "Please agree to Terms & Conditions", Toast.LENGTH_SHORT).show();
            return;
        }

        // ✅ ADDED: Check email uniqueness
        if (db.isEmailExists(email)) {
            ((TextInputLayout)findViewById(R.id.emailInputLayout))
                    .setError("Email already registered");
            return;
        }

        // ✅ ADDED: Register user in SQLite (with hashing inside DB)
        boolean success = db.registerUser(email, password);

        if (!success) {
            Toast.makeText(this, "Registration failed", Toast.LENGTH_SHORT).show();
            return;
        }

        Toast.makeText(this, "Registration successful", Toast.LENGTH_SHORT).show();

        startActivity(new Intent(this, LoginActivity.class));
        finish();
    }

    private void clearErrors() {
        ((TextInputLayout)findViewById(R.id.emailInputLayout)).setError(null);
        ((TextInputLayout)findViewById(R.id.passwordInputLayout)).setError(null);
        ((TextInputLayout)findViewById(R.id.confirmPasswordInputLayout)).setError(null);
    }
}