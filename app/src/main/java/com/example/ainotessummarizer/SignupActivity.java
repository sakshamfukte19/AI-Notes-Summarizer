package com.example.ainotessummarizer;

import android.content.Intent;
import android.os.Bundle;
import android.text.TextUtils;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;

import java.util.HashMap;

public class SignupActivity extends AppCompatActivity {

    EditText name, email, pass;
    Button signup;
    TextView loginTv;
    FirebaseAuth mauth;
    DatabaseReference dbref;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_signup);
        name = findViewById(R.id.SignupName);
        email = findViewById(R.id.emailSignup);
        pass = findViewById(R.id.passwordSignup);
        signup = findViewById(R.id.SignupBtn);
        loginTv = findViewById(R.id.loginTv);
        mauth = FirebaseAuth.getInstance();
        dbref = FirebaseDatabase.getInstance().getReference("Users");


        signup.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                storeUser();
            }
        });

        loginTv.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(getApplicationContext(), LoginActivity.class);
                startActivity(intent);
            }
        });


    }

    private void storeUser() {
        String uName = name.getText().toString().trim();
        String uEmail = email.getText().toString().trim();
        String uPass = pass.getText().toString().trim();
        if (TextUtils.isEmpty(uName)) {
            name.setError("Name is required");
            return;
        }
        if (TextUtils.isEmpty(uEmail)) {
            email.setError("Emails is required");
            return;
        }
        if (TextUtils.isEmpty(uPass)) {
            pass.setError("Password is required");
            return;
        }

        if(uPass.length() < 6) {
            pass.setError("There should be password of 7 characters");
            return;
        }
        mauth.createUserWithEmailAndPassword(uEmail, uPass).addOnCompleteListener(task -> {
            if (task.isSuccessful()) {
                FirebaseUser currUser = mauth.getCurrentUser();
                if (currUser != null) {
                    String uid = currUser.getUid();
                    HashMap<String, String> map = new HashMap<>();
                    map.put("Name", uName);
                    map.put("Email", uEmail);
                    map.put("Password", uPass);

                    dbref.child(uid).setValue(map).addOnCompleteListener(task1 -> {
                        if (task1.isSuccessful()) {
                            name.setText("");
                            email.setText("");
                            pass.setText("");
                            Toast.makeText(SignupActivity.this, "Registered successfully", Toast.LENGTH_LONG).show();
                            Intent intent = new Intent(getApplicationContext(), HomeActivity.class);
                            startActivity(intent);
                            finish();
                        } else {
                            Toast.makeText(SignupActivity.this, "Registration Failed", Toast.LENGTH_LONG).show();

                        }
                    });
                }
            }
        });
    }
}