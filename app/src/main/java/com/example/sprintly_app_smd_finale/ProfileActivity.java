package com.example.sprintly_app_smd_finale;

import android.os.Bundle;
import android.util.Log;
import android.widget.Button;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import com.google.android.material.textfield.TextInputEditText;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;

import java.util.HashMap;
import java.util.Map;

public class ProfileActivity extends AppCompatActivity {
    private static final String TAG = "ProfileActivity";

    private TextInputEditText nameInput, emailInput, phoneInput;
    private TextInputEditText currentPwInput, newPwInput, confirmPwInput;
    private Button saveBtn;

    private FirebaseFirestore db;
    private String currentUserId, email;
    private String storedPassword;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_profile);

        db = FirebaseFirestore.getInstance();
        email = getIntent().getStringExtra("EMAIL");

        nameInput      = findViewById(R.id.nameInput);
        emailInput     = findViewById(R.id.emailInput);
        phoneInput     = findViewById(R.id.phoneInput);
        currentPwInput = findViewById(R.id.currentPasswordInput);
        newPwInput     = findViewById(R.id.newPasswordInput);
        confirmPwInput = findViewById(R.id.confirmPasswordInput);
        saveBtn        = findViewById(R.id.saveProfileBtn);

        // Load user doc
        db.collection("user_info")
                .whereEqualTo("email", email)
                .get()
                .addOnSuccessListener(qs -> {
                    if (qs.isEmpty()) {
                        Toast.makeText(this, "User record not found", Toast.LENGTH_SHORT).show();
                        finish();
                        return;
                    }
                    DocumentSnapshot doc = qs.getDocuments().get(0);
                    currentUserId = doc.getId();
                    loadProfile(doc);
                })
                .addOnFailureListener(e -> {
                    Log.e(TAG, "Error fetching user", e);
                    Toast.makeText(this, "Error loading profile", Toast.LENGTH_SHORT).show();
                    finish();
                });

        saveBtn.setOnClickListener(v -> saveProfile());
    }

    private void loadProfile(DocumentSnapshot doc) {
        nameInput.setText(doc.getString("name"));
        emailInput.setText(email);
        phoneInput.setText(doc.getString("phone"));
        storedPassword = doc.getString("password");
    }

    private void saveProfile() {
        String newName      = nameInput.getText().toString().trim();
        String newPhone     = phoneInput.getText().toString().trim();
        String currPw       = currentPwInput.getText().toString();
        String newPw        = newPwInput.getText().toString();
        String confirmPw    = confirmPwInput.getText().toString();

        if (newName.isEmpty()) {
            nameInput.setError("Name required");
            return;
        }
        if (newPhone.isEmpty()) {
            phoneInput.setError("Phone required");
            return;
        }

        Map<String,Object> updates = new HashMap<>();
        updates.put("name", newName);
        updates.put("phone", newPhone);

        // If any password field is filled, validate all
        if (!currPw.isEmpty() || !newPw.isEmpty() || !confirmPw.isEmpty()) {
            if (currPw.isEmpty() || newPw.isEmpty() || confirmPw.isEmpty()) {
                Toast.makeText(this, "Fill all password fields to change password", Toast.LENGTH_SHORT).show();
                return;
            }
            if (!currPw.equals(storedPassword)) {
                currentPwInput.setError("Current password incorrect");
                return;
            }
            if (!newPw.equals(confirmPw)) {
                confirmPwInput.setError("Passwords do not match");
                return;
            }
            if (newPw.length() < 6) {
                newPwInput.setError("Password must be at least 6 characters");
                return;
            }
            updates.put("password", newPw);
        }

        db.collection("user_info")
                .document(currentUserId)
                .update(updates)
                .addOnSuccessListener(a -> {
                    Toast.makeText(this, "Profile updated", Toast.LENGTH_LONG).show();
                    // Clear password fields
                    currentPwInput.setText("");
                    newPwInput.setText("");
                    confirmPwInput.setText("");
                })
                .addOnFailureListener(e -> {
                    Log.e(TAG, "Error saving profile", e);
                    Toast.makeText(this, "Save failed", Toast.LENGTH_SHORT).show();
                });
    }
}