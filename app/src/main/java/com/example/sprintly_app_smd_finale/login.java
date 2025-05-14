package com.example.sprintly_app_smd_finale;

import android.app.ProgressDialog;
import android.content.Intent;
import android.os.Bundle;
import android.util.Patterns;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

import androidx.activity.EdgeToEdge;
import androidx.appcompat.app.AppCompatActivity;

import com.google.android.material.textfield.TextInputEditText;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.FirebaseFirestore;
import com.zegocloud.uikit.prebuilt.call.ZegoUIKitPrebuiltCallService;
import com.zegocloud.uikit.prebuilt.call.invite.ZegoUIKitPrebuiltCallInvitationConfig;

public class login extends AppCompatActivity {
    private TextInputEditText emailInput, passwordInput;
    private Button loginButton;
    private TextView signUpLink;
    private FirebaseAuth mAuth;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_login);

        mAuth = FirebaseAuth.getInstance();
        emailInput    = findViewById(R.id.emailInput);
        passwordInput = findViewById(R.id.passwordInput);
        loginButton   = findViewById(R.id.loginButton);
        signUpLink    = findViewById(R.id.signUpLink);

        loginButton.setOnClickListener(v -> attemptLogin());
        signUpLink .setOnClickListener(v ->
                startActivity(new Intent(login.this, signup_page.class))
        );
    }

    private void attemptLogin() {
        String email    = emailInput.getText().toString().trim();
        String password = passwordInput.getText().toString().trim();

        if (email.isEmpty() || !Patterns.EMAIL_ADDRESS.matcher(email).matches()) {
            emailInput.setError("Valid email required");
            emailInput.requestFocus();
            return;
        }
        if (password.isEmpty()) {
            passwordInput.setError("Password required");
            passwordInput.requestFocus();
            return;
        }

        ProgressDialog pd = new ProgressDialog(this);
        pd.setMessage("Signing in...");
        pd.setCancelable(false);
        pd.show();

        mAuth.signInWithEmailAndPassword(email, password)
                .addOnSuccessListener(authResult -> {
                    pd.dismiss();
                    Toast.makeText(this, "Login successful", Toast.LENGTH_SHORT).show();
                    String uid = mAuth.getCurrentUser().getUid();

                    FirebaseFirestore db = FirebaseFirestore.getInstance();
                    db.collection("user_info").document(uid).get()
                            .addOnSuccessListener(documentSnapshot -> {
                                if (documentSnapshot.exists()) {
                                    String name = documentSnapshot.getString("name");

                                    ZegoUIKitPrebuiltCallInvitationConfig callInvitationConfig = new ZegoUIKitPrebuiltCallInvitationConfig();
                                    ZegoUIKitPrebuiltCallService.init(
                                            getApplication(),
                                            AppConstants.APP_ID,
                                            AppConstants.APP_SIGN,
                                            uid,
                                            name != null ? name : "Unknown",
                                            callInvitationConfig
                                    );

                                    startActivity(new Intent(login.this, main_dashboard.class));
                                    finish();
                                } else {
                                    Toast.makeText(this, "User data not found", Toast.LENGTH_SHORT).show();
                                }
                            })
                            .addOnFailureListener(e -> {
                                Toast.makeText(this, "Failed to fetch user name", Toast.LENGTH_SHORT).show();
                            });

                })
                .addOnFailureListener(e -> {
                    pd.dismiss();
                    Toast.makeText(this, "Login failed: " + e.getMessage(),
                            Toast.LENGTH_LONG).show();
                });
    }
}
