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
                    // Launch main_dashboard
                    startActivity(new Intent(login.this, main_dashboard.class));
                    finish();
                })
                .addOnFailureListener(e -> {
                    pd.dismiss();
                    Toast.makeText(this, "Login failed: " + e.getMessage(),
                            Toast.LENGTH_LONG).show();
                });
    }
}
