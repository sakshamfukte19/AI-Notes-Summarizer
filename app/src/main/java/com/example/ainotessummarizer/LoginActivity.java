package com.example.ainotessummarizer;

import android.content.Intent;
import android.content.SharedPreferences;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.os.Bundle;
import android.os.CountDownTimer;
import android.text.InputType;
import android.util.TypedValue;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

public class LoginActivity extends AppCompatActivity {

    EditText username, password;
    CheckBox showPassword;
    Button loginButton;
    TextView click_Here;
    private DatabaseHelper db;
    private static final String PASSWORD_PATTERN = "^(?=.*[A-Za-z]).{6,}$";
    private static final int MAX_ATTEMPTS = 5;
    private static final long LOCK_DURATION = 60_000; // 1 minute

    @Override
    protected void onCreate(Bundle savedInstanceState) {

        SharedPreferences sharedPreferences = getSharedPreferences("MyAppPrefs", MODE_PRIVATE);
        boolean isLoggedIn = sharedPreferences.getBoolean("isLoggedIn", false);
        if (isLoggedIn) {
            startActivity(new Intent(getApplicationContext(), HomeActivity.class));
            finish();
            return;
        }

        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_login);

        username = findViewById(R.id.username);
        password = findViewById(R.id.password);
        showPassword = findViewById(R.id.show_password);
        loginButton = findViewById(R.id.login_button);
        click_Here = findViewById(R.id.click_here);

        db = new DatabaseHelper(this);

        showPassword.setOnCheckedChangeListener((buttonView, isChecked) -> {
            if (isChecked)
                password.setInputType(InputType.TYPE_TEXT_VARIATION_VISIBLE_PASSWORD);
            else
                password.setInputType(InputType.TYPE_CLASS_TEXT | InputType.TYPE_TEXT_VARIATION_PASSWORD);
            password.setSelection(password.length());
        });

        loginButton.setOnClickListener(v -> attemptLogin());

        click_Here.setOnClickListener(v -> startActivity(new Intent(getApplicationContext(), SignupActivity.class)));
    }

    private void attemptLogin() {
        String user = username.getText().toString().trim();
        String pass = password.getText().toString().trim();

        if (user.isEmpty() || pass.isEmpty()) {
            Toast.makeText(this, "Enter username & password", Toast.LENGTH_SHORT).show();
            return;
        }

        if (!pass.matches(PASSWORD_PATTERN)) {
            Toast.makeText(this, "Password must have at least 6 characters", Toast.LENGTH_LONG).show();
            return;
        }

        long remainingLockTime = getRemainingLockTime(user);
        if (remainingLockTime > 0) {
            startLockCountdown(user, remainingLockTime);
            return;
        }

        boolean success = db.loginUser(user, pass);

        if (!success) {
            incrementFailedAttempts(user);
            int attemptsLeft = MAX_ATTEMPTS - getFailedAttempts(user);

            if (attemptsLeft <= 0) {
                setLockTime(user);
                startLockCountdown(user, LOCK_DURATION);
            } else {
                Toast.makeText(this, "Wrong password! Attempts left: " + attemptsLeft, Toast.LENGTH_SHORT).show();
            }
            return;
        }

        resetFailedAttempts(user); // reset attempts after successful login

        SharedPreferences.Editor editor = getSharedPreferences("MyAppPrefs", MODE_PRIVATE).edit();
        editor.putBoolean("isLoggedIn", true);
        editor.apply();

        Toast.makeText(this, "Login successful", Toast.LENGTH_SHORT).show();
        startActivity(new Intent(getApplicationContext(), HomeActivity.class));
        finish();
    }

    // --- Database helpers ---
    private int getFailedAttempts(String email) {
        SQLiteDatabase dbRead = db.getReadableDatabase();
        Cursor c = dbRead.rawQuery("SELECT failed_attempts FROM users WHERE email=?", new String[]{email});
        int attempts = 0;
        if (c.moveToFirst()) attempts = c.getInt(0);
        c.close();
        return attempts;
    }

    private void incrementFailedAttempts(String email) {
        int attempts = getFailedAttempts(email) + 1;
        SQLiteDatabase dbWrite = db.getWritableDatabase();
        dbWrite.execSQL("UPDATE users SET failed_attempts=? WHERE email=?", new Object[]{attempts, email});
    }

    private void resetFailedAttempts(String email) {
        SQLiteDatabase dbWrite = db.getWritableDatabase();
        dbWrite.execSQL("UPDATE users SET failed_attempts=0, lock_time=0 WHERE email=?", new Object[]{email});
    }

    private void setLockTime(String email) {
        SQLiteDatabase dbWrite = db.getWritableDatabase();
        long currentTime = System.currentTimeMillis();
        dbWrite.execSQL("UPDATE users SET lock_time=? WHERE email=?", new Object[]{currentTime, email});
    }

    private long getRemainingLockTime(String email) {
        SQLiteDatabase dbRead = db.getReadableDatabase();
        Cursor c = dbRead.rawQuery("SELECT lock_time FROM users WHERE email=?", new String[]{email});
        long remaining = 0;
        if (c.moveToFirst()) {
            long lockTime = c.getLong(0);
            if (lockTime > 0) {
                long elapsed = System.currentTimeMillis() - lockTime;
                remaining = Math.max(0, LOCK_DURATION - elapsed);
            }
        }
        c.close();
        return remaining;
    }

    // --- Countdown timer with seconds countdown ---
    private void startLockCountdown(String email, long millis) {
        loginButton.setEnabled(false);
        loginButton.setBackgroundColor(getResources().getColor(android.R.color.darker_gray));
        loginButton.setTextSize(TypedValue.COMPLEX_UNIT_SP, 12); // smaller text

        new CountDownTimer(millis, 1000) {
            @Override
            public void onTick(long millisUntilFinished) {
                int seconds = (int) (millisUntilFinished / 1000);
                loginButton.setText("Locked: " + seconds + "s");
            }

            @Override
            public void onFinish() {
                loginButton.setEnabled(true);
                loginButton.setText("Login");
                loginButton.setBackgroundColor(getResources().getColor(R.color.login_normal)); // original color
                loginButton.setTextSize(TypedValue.COMPLEX_UNIT_SP, 16); // restore original size
                resetFailedAttempts(email);
            }
        }.start();

        Toast.makeText(this, "Account locked! Wait 1 minute", Toast.LENGTH_SHORT).show();
    }
}