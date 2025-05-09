package com.example.sprintly_app_smd_finale;

import android.app.ProgressDialog;
import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.util.Patterns;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

import androidx.activity.EdgeToEdge;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;

import com.google.android.material.textfield.TextInputEditText;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.Source;
import com.zegocloud.uikit.prebuilt.call.ZegoUIKitPrebuiltCallService;
import com.zegocloud.uikit.prebuilt.call.invite.ZegoUIKitPrebuiltCallInvitationConfig;

public class login extends AppCompatActivity {

    // UI Elements\][
    private TextInputEditText emailInput;
    private TextInputEditText passwordInput;
    private Button loginButton;
    private TextView signUpLink;

    // Firebase references
    private FirebaseAuth auth;
    private FirebaseFirestore db;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_login);

        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main), (v, insets) -> {
            Insets systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom);
            return insets;
        });

        // Initialize Firebase Auth and Firestore
        auth = FirebaseAuth.getInstance();
        db = FirebaseFirestore.getInstance();

        // Initialize UI
        initializeUI();
    }

    private void initializeUI() {
        emailInput = findViewById(R.id.emailInput);
        passwordInput = findViewById(R.id.passwordInput);
        loginButton = findViewById(R.id.loginButton);
        signUpLink = findViewById(R.id.signUpLink);

        loginButton.setOnClickListener(v -> authenticateUser());
        signUpLink.setOnClickListener(v -> {
            startActivity(new Intent(login.this, signup_page.class));
        });
    }

    private void authenticateUser() {
        String email = emailInput.getText().toString().trim();
        String password = passwordInput.getText().toString().trim();

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

        ProgressDialog progressDialog = new ProgressDialog(this);
        progressDialog.setMessage("Logging in...");
        progressDialog.setCancelable(false);
        progressDialog.show();

        auth.signInWithEmailAndPassword(email, password)
                .addOnCompleteListener(this, task -> {
                    if (task.isSuccessful()) {
                        FirebaseUser firebaseUser = auth.getCurrentUser();
                        if (firebaseUser != null) {
                            String uid = firebaseUser.getUid();
                            // Fetch additional user info from Firestore
                            db.collection("user_info")
                                    .document(uid)
                                    .get(Source.DEFAULT)
                                    .addOnSuccessListener(DocumentSnapshot::getData)
                                    .addOnCompleteListener(snapshotTask -> {
                                        progressDialog.dismiss();
                                        if (snapshotTask.isSuccessful() && snapshotTask.getResult() != null) {
                                            DocumentSnapshot doc = snapshotTask.getResult();
                                            String name_user = doc.getString("name");
                                            String email_user = doc.getString("email");

                                            Toast.makeText(login.this, "Login Successful", Toast.LENGTH_SHORT).show();
                                            Intent intent = new Intent(login.this, main_dashboard.class);
                                            intent.putExtra("USER_ID", uid);
                                            intent.putExtra("NAME", name_user);
                                            intent.putExtra("EMAIL", email_user);

                                            // Initialize Zego Cloud
                                            ZegoUIKitPrebuiltCallInvitationConfig config = new ZegoUIKitPrebuiltCallInvitationConfig();
                                            ZegoUIKitPrebuiltCallService.init(
                                                    getApplication(),
                                                    AppConstants.APP_ID,
                                                    AppConstants.APP_SIGN,
                                                    uid,
                                                    name_user,
                                                    config
                                            );

                                            startActivity(intent);
                                            finish();
                                        } else {
                                            Toast.makeText(login.this, "Failed to retrieve user data.", Toast.LENGTH_SHORT).show();
                                        }
                                    })
                                    .addOnFailureListener(e -> {
                                        progressDialog.dismiss();
                                        Toast.makeText(login.this, "Error fetching user info: " + e.getMessage(), Toast.LENGTH_SHORT).show();
                                    });
                        }
                    } else {
                        progressDialog.dismiss();
                        Toast.makeText(login.this, "Authentication failed: " + task.getException().getMessage(), Toast.LENGTH_LONG).show();
                    }
                });
    }
}
