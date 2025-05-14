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
import com.google.firebase.auth.AuthResult;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseAuthUserCollisionException;
import com.google.firebase.firestore.FirebaseFirestore;

import java.util.HashMap;
import java.util.Map;

public class signup_page extends AppCompatActivity {

    private TextInputEditText emailInput, passwordInput, confirmPasswordInput, nameInput, phoneInput;
    private Button signupButton;

    private FirebaseAuth auth;
    private FirebaseFirestore db;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_signup_page);

        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main), (v, insets) -> {
            Insets bars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
            v.setPadding(bars.left, bars.top, bars.right, bars.bottom);
            return insets;
        });

        auth = FirebaseAuth.getInstance();
        db   = FirebaseFirestore.getInstance();

        nameInput            = findViewById(R.id.nameInput);
        phoneInput           = findViewById(R.id.phoneInput);
        emailInput           = findViewById(R.id.emailInput);
        passwordInput        = findViewById(R.id.passwordInput);
        confirmPasswordInput = findViewById(R.id.confirmPasswordInput);
        signupButton         = findViewById(R.id.signupButton);

        signupButton.setOnClickListener(v -> validateAndCreateAccount());
    }

    private void validateAndCreateAccount() {
        String name    = nameInput.getText().toString().trim();
        String phone   = phoneInput.getText().toString().trim();
        String email   = emailInput.getText().toString().trim();
        String pwd     = passwordInput.getText().toString().trim();
        String confirm = confirmPasswordInput.getText().toString().trim();

        if (name.isEmpty()) {
            nameInput.setError("Name is required");
            nameInput.requestFocus();
            return;
        }
        if (phone.isEmpty() || !phone.matches("\\d{11}")) {
            phoneInput.setError("Enter a valid 11-digit phone");
            phoneInput.requestFocus();
            return;
        }
        if (email.isEmpty() || !Patterns.EMAIL_ADDRESS.matcher(email).matches()) {
            emailInput.setError("Valid email is required");
            emailInput.requestFocus();
            return;
        }
        if (pwd.isEmpty() || pwd.length() < 6) {
            passwordInput.setError("Password >= 6 chars");
            passwordInput.requestFocus();
            return;
        }
        if (!pwd.equals(confirm)) {
            confirmPasswordInput.setError("Passwords do not match");
            confirmPasswordInput.requestFocus();
            return;
        }

        ProgressDialog pd = new ProgressDialog(this);
        pd.setMessage("Creating account...");
        pd.setCancelable(false);
        pd.show();

        // 1. Create user in FirebaseAuth
        auth.createUserWithEmailAndPassword(email, pwd)
                .addOnSuccessListener((AuthResult authResult) -> {
                    String uid = authResult.getUser().getUid();
                    // 2. Save extra details in Firestore
                    Map<String,Object> user = new HashMap<>();
                    user.put("name", name);
                    user.put("phone", phone);
                    user.put("email", email);
                    user.put("password", pwd);

                    db.collection("user_info").document(uid)
                            .set(user)
                            .addOnSuccessListener(aVoid -> {
                                // Optional: initialize empty contacts subcollection
                                db.collection("user_info").document(uid)
                                        .collection("contacts").add(new HashMap<>());

                                pd.dismiss();
                                Toast.makeText(this, "Account created successfully", Toast.LENGTH_SHORT).show();
                                // Auto-login and go to dashboard
                                startActivity(new Intent(signup_page.this, login.class));
                                finish();
                            })
                            .addOnFailureListener(e -> {
                                pd.dismiss();
                                Toast.makeText(this, "Error saving user data: " + e.getMessage(), Toast.LENGTH_LONG).show();
                            });
                })
                .addOnFailureListener(e -> {
                    pd.dismiss();
                    if (e instanceof FirebaseAuthUserCollisionException) {
                        emailInput.setError("Email already registered");
                        emailInput.requestFocus();
                    } else {
                        Toast.makeText(this, "Signup failed: " + e.getMessage(), Toast.LENGTH_LONG).show();
                    }
                });
    }
}
