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
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.QuerySnapshot;
import com.zegocloud.uikit.prebuilt.call.ZegoUIKitPrebuiltCallService;
import com.zegocloud.uikit.prebuilt.call.invite.ZegoUIKitPrebuiltCallInvitationConfig;

public class login extends AppCompatActivity {

    // UI Elements
    private TextInputEditText emailInput;
    private TextInputEditText passwordInput;
    private Button loginButton;
    private TextView signUpLink;

    // Firestore reference
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

        // Initialize Firestore
        db = FirebaseFirestore.getInstance();

        // Initialize UI
        initializeUI();
    }

    private void initializeUI() {
        emailInput = findViewById(R.id.emailInput);
        passwordInput = findViewById(R.id.passwordInput);
        loginButton = findViewById(R.id.loginButton);
        signUpLink = findViewById(R.id.signUpLink);

        loginButton.setOnClickListener(v -> checkUserInFirestore());
        signUpLink.setOnClickListener(v -> {
            startActivity(new Intent(login.this, signup_page.class));
        });
    }

    private void checkUserInFirestore() {
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
        progressDialog.setMessage("Checking credentials...");
        progressDialog.show();

        db.collection("user_info")
                .whereEqualTo("email", email)
                .whereEqualTo("password", password)
                .get()
                .addOnCompleteListener(task -> {
                    progressDialog.dismiss();
                    if (task.isSuccessful()) {
                        QuerySnapshot result = task.getResult();
                        if (!result.isEmpty()) {
                            DocumentSnapshot document = result.getDocuments().get(0);
                            String password_user = document.getString("password"); // fetch user's name
                            String Email_user=document.getString("email");
                            String name_user=document.getString("name");
                            String user_id=document.getId();
                            Toast.makeText(login.this, "Login Successful", Toast.LENGTH_SHORT).show();

                            // Intent with extra data
                            Intent intent = new Intent(login.this,main_dashboard.class);
                            intent.putExtra("USER_Password", password_user); // pass user name
                            intent.putExtra("EMAIL", Email_user); // pass user name
                            intent.putExtra("NAME", name_user);
                            Log.d("DEBUG_LOG", "starting the intent");

                            //Initizalizing the zeno cloud

                            ZegoUIKitPrebuiltCallInvitationConfig callInvitationConfig = new ZegoUIKitPrebuiltCallInvitationConfig();
                            ZegoUIKitPrebuiltCallService.init(getApplication(),AppConstants.APP_ID,AppConstants.APP_SIGN,user_id,name_user,callInvitationConfig);
                            Log.d("DEBUG_LOG", "user id:"+user_id+" user name:"+name_user);
                            startActivity(intent);
                            finish();
                        } else { 
                            Toast.makeText(login.this, "Incorrect email or password", Toast.LENGTH_SHORT).show();
                        }
                    } else {
                        Toast.makeText(login.this, "Login failed: " + task.getException().getMessage(), Toast.LENGTH_LONG).show();
                    }
                });
    }
}
