package com.example.sprintly_app_smd_finale;

import android.app.ProgressDialog;
import android.content.Intent;
import android.os.Bundle;
import android.util.Patterns;
import android.widget.Button;
import android.widget.Toast;

import androidx.activity.EdgeToEdge;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;

import com.google.android.material.textfield.TextInputEditText;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.firestore.FirebaseFirestore;

import java.util.HashMap;
import java.util.Map;

public class signup_page extends AppCompatActivity {

    // UI Elements
    private TextInputEditText emailInput;
    private TextInputEditText passwordInput;
    private TextInputEditText confirmPasswordInput;
    private TextInputEditText nameInput;
    private TextInputEditText phoneInput;
    private Button signupButton;

    // Firebase references
    private FirebaseAuth auth;
    private FirebaseFirestore db;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_signup_page);

        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main), (v, insets) -> {
            Insets systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom);
            return insets;
        });

        // Initialize Firebase
        auth = FirebaseAuth.getInstance();
        db = FirebaseFirestore.getInstance();

        // Initialize UI
        initializeUI();
    }

    private void initializeUI() {
        nameInput = findViewById(R.id.nameInput);
        phoneInput = findViewById(R.id.phoneInput);
        emailInput = findViewById(R.id.emailInput);
        passwordInput = findViewById(R.id.passwordInput);
        confirmPasswordInput = findViewById(R.id.confirmPasswordInput);
        signupButton = findViewById(R.id.signupButton);

        signupButton.setOnClickListener(v -> validateAndCreateAccount());
    }

    private void validateAndCreateAccount() {
        String name = nameInput.getText().toString().trim();
        String phone = phoneInput.getText().toString().trim();
        String email = emailInput.getText().toString().trim();
        String password = passwordInput.getText().toString().trim();
        String confirmPassword = confirmPasswordInput.getText().toString().trim();

        // Validation
        if (name.isEmpty()) {
            nameInput.setError("Name is required");
            nameInput.requestFocus();
            return;
        }

        if (phone.isEmpty()) {
            phoneInput.setError("Phone number is required");
            phoneInput.requestFocus();
            return;
        }

        if (!phone.matches("\\d{11}")) {
            phoneInput.setError("Enter a valid 11-digit number");
            phoneInput.requestFocus();
            return;
        }

        if (email.isEmpty()) {
            emailInput.setError("Email is required");
            emailInput.requestFocus();
            return;
        }

        if (!Patterns.EMAIL_ADDRESS.matcher(email).matches()) {
            emailInput.setError("Please enter a valid email");
            emailInput.requestFocus();
            return;
        }

        if (password.isEmpty()) {
            passwordInput.setError("Password is required");
            passwordInput.requestFocus();
            return;
        }

        if (password.length() < 6) {
            passwordInput.setError("Password must be at least 6 characters");
            passwordInput.requestFocus();
            return;
        }

        if (!password.equals(confirmPassword)) {
            confirmPasswordInput.setError("Passwords do not match");
            confirmPasswordInput.requestFocus();
            return;
        }

        // Show progress dialog
        ProgressDialog progressDialog = new ProgressDialog(this);
        progressDialog.setMessage("Creating account...");
        progressDialog.setCancelable(false);
        progressDialog.show();

        // Create user with Firebase Auth
        auth.createUserWithEmailAndPassword(email, password)
                .addOnCompleteListener(this, task -> {
                    if (task.isSuccessful()) {
                        FirebaseUser firebaseUser = auth.getCurrentUser();
                        if (firebaseUser != null) {
                            // Prepare user data for Firestore
                            Map<String, Object> userMap = new HashMap<>();
                            userMap.put("name", name);
                            userMap.put("phone", phone);
                            userMap.put("email", email);

                            // Save additional user info under uid
                            db.collection("user_info")
                                    .document(firebaseUser.getUid())
                                    .set(userMap)
                                    .addOnSuccessListener(aVoid -> {
                                        progressDialog.dismiss();
                                        Toast.makeText(signup_page.this, "Account created successfully", Toast.LENGTH_SHORT).show();
                                        startActivity(new Intent(signup_page.this, login.class));
                                        finish();
                                    })
                                    .addOnFailureListener(e -> {
                                        progressDialog.dismiss();
                                        Toast.makeText(signup_page.this, "Error saving user data: " + e.getMessage(), Toast.LENGTH_SHORT).show();
                                    });
                        }
                    } else {
                        progressDialog.dismiss();
                        Toast.makeText(signup_page.this, "Authentication failed: " + task.getException().getMessage(), Toast.LENGTH_SHORT).show();
                    }
                });
    }
}
