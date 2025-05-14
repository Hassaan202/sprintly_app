package com.example.sprintly_app_smd_finale;

import android.app.Dialog;
import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.Window;
import android.view.WindowManager;
import android.widget.Button;
import android.widget.ImageButton;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import androidx.activity.EdgeToEdge;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.google.android.material.textfield.TextInputEditText;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;
import com.example.sprintly_app_smd_finale.ProfileActivity;


import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class main_dashboard extends AppCompatActivity implements chat_interface {

    private static final String TAG = "main_dashboard";
    List<Contact> contacts;
    private String currentUserId;

    // Navigation items
    private NavBarHelper navBarHelper;
    private LinearLayout homeNavItem;
    private String email;
    private Button createContactBtn;
    private Dialog contactDialog;
    private FirebaseAuth mAuth;
    private TextView user_name_view;

    interface OnContactCheckListener {
        void onCheck(boolean exists);
    }

    interface OnContactIdFetchListener {
        void onIdFetched(String contactId);
    }

    private void checkIfContactExists(String contactNumber, OnContactCheckListener callback) {
        FirebaseFirestore db = FirebaseFirestore.getInstance();
        db.collection("user_info")
                .document(currentUserId)
                .collection("contacts")
                .whereEqualTo("number", contactNumber)
                .get()
                .addOnSuccessListener(qs -> callback.onCheck(!qs.isEmpty()))
                .addOnFailureListener(e -> {
                    Log.e(TAG, "Error checking contact: ", e);
                    callback.onCheck(false);
                });
    }

    private void fetchContactDocId(String contactNumber, OnContactIdFetchListener callback) {
        Log.d(TAG, "Entering the fetch contact id");
        FirebaseFirestore db = FirebaseFirestore.getInstance();
        db.collection("user_info")
                .whereEqualTo("number", contactNumber)
                .get()
                .addOnSuccessListener(qs -> {
                    Log.d(TAG, "contact id is fetched");
                    if (!qs.isEmpty()) callback.onIdFetched(qs.getDocuments().get(0).getId());
                    else callback.onIdFetched(null);
                })
                .addOnFailureListener(e -> {
                    Log.e(TAG, "Failed to fetch contact Doc ID: ", e);
                    callback.onIdFetched(null);
                });
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_main_dashboard);

        // Initialize Firebase Auth
        mAuth = FirebaseAuth.getInstance();

        //initialize the user name in the dashboard
        user_name_view=findViewById(R.id.usernameText);
        FirebaseUser currentUser = mAuth.getCurrentUser();
        if (currentUser != null) {
            email = currentUser.getEmail();
        }
        FirebaseFirestore db_name=FirebaseFirestore.getInstance();
        db_name.collection("user_info")
                .whereEqualTo("email", email)
                .get()
                .addOnSuccessListener(qs -> {
                    if (qs.isEmpty()) {
                        Toast.makeText(this, "User record not found", Toast.LENGTH_SHORT).show();
                        finish();
                        return;
                    }
                    DocumentSnapshot doc = qs.getDocuments().get(0);
                    String userName = doc.getString("name"); // Assuming the field name is "name"
                    user_name_view.setText(userName); // Setting the name in TextView
                })
                .addOnFailureListener(e -> {
                    Log.e(TAG, "Error fetching user", e);
                    Toast.makeText(this, "Error loading profile", Toast.LENGTH_SHORT).show();
                    finish();
                });


        // Initialize UI
        createContactBtn = findViewById(R.id.createContactBtn);
        contactDialog = new Dialog(this);
        contactDialog.requestWindowFeature(Window.FEATURE_NO_TITLE);
        contactDialog.setContentView(R.layout.dialog_new_contact);
        contactDialog.setCancelable(false);
        WindowManager.LayoutParams lp = new WindowManager.LayoutParams();
        lp.copyFrom(contactDialog.getWindow().getAttributes());
        lp.width = WindowManager.LayoutParams.MATCH_PARENT;
        lp.height = WindowManager.LayoutParams.WRAP_CONTENT;
        contactDialog.getWindow().setAttributes(lp);
        ImageButton closeButton = contactDialog.findViewById(R.id.closeButton);
        Button findContactButton = contactDialog.findViewById(R.id.findContactButton);
        TextInputEditText contactNameEditText = contactDialog.findViewById(R.id.contactNameEditText);
        TextInputEditText contactNumberEditText = contactDialog.findViewById(R.id.contactNumberEditText);

        findContactButton.setOnClickListener(v -> {
            Log.d(TAG, "find contact button is clicked");
            String contactName = contactNameEditText.getText().toString().trim();
            String contactNumber = contactNumberEditText.getText().toString().trim();
            if (contactNumber.isEmpty()) {
                contactNumberEditText.setError("Enter a valid number");
                return;
            }
            checkIfContactExists(contactNumber, exists -> {
                if (exists) {
                    contactNumberEditText.setError("Contact already exists!");
                } else {
                    Log.d(TAG, "about to enter the fetch contact function,this is the number:"+contactNumber);
                    fetchContactDocId(contactNumber, contactId -> {
                        if (contactId != null) {
                            Log.d(TAG, "addiong the contact");
                            Map<String, Object> contactData = new HashMap<>();
                            contactData.put("name", contactName);
                            contactData.put("number", contactNumber);
                            FirebaseFirestore.getInstance()
                                    .collection("user_info")
                                    .document(currentUserId)
                                    .collection("contacts")
                                    .document(contactId)
                                    .set(contactData)
                                    .addOnSuccessListener(aVoid -> {
                                        Log.d(TAG, "Contact added successfully.");
                                        FirebaseFirestore.getInstance().collection("user_info")
                                                .document(currentUserId)
                                                .collection("contacts")
                                                .document(contactId)
                                                .collection("chats_info")
                                                .add(new HashMap<>())
                                                .addOnSuccessListener(docRef -> Log.d(TAG, "Chat Info initialized with auto ID: " + docRef.getId()))
                                                .addOnFailureListener(e -> Log.e(TAG, "Failed to create chat info: ", e));
                                        fetchContactInfo();
                                        contactDialog.dismiss();
                                    })
                                    .addOnFailureListener(e -> Log.e(TAG, "Failed to add contact: ", e));
                        }
                    });
                }
            });
        });
        closeButton.setOnClickListener(v -> contactDialog.dismiss());
        createContactBtn.setOnClickListener(v -> contactDialog.show());

        contacts = new ArrayList<>();
        getUserEmailAndFetchData();

        // Setup navigation
        navBarHelper = new NavBarHelper(findViewById(android.R.id.content), new NavBarListener() {
            @Override
            public void onCalendarSelected() {
                Intent i = new Intent(main_dashboard.this, CalendarActivity.class);
                startActivity(i);
            }

            @Override
            public void onTasksSelected() {
                Intent i = new Intent(main_dashboard.this, task_list.class);
                startActivity(i);
            }

            @Override
            public void onHomeSelected() {
                navigateToHome(this);
            }

            @Override
            public void onProfileSelected() {
                Intent i = new Intent(main_dashboard.this, ProfileActivity.class);
                startActivity(i);
            }

            @Override
            public void onCodeSelected() {
                startActivity(new Intent(main_dashboard.this, codeActivity.class));
            }
        });
        homeNavItem = findViewById(R.id.homeNavItem);
        navBarHelper.selectTab(homeNavItem);
    }

    @Override
    protected void onResume() {
        super.onResume();
        navBarHelper.selectTab(homeNavItem);
    }

    private void getUserEmailAndFetchData() {
        // Get current user from Firebase Authentication
        FirebaseUser currentUser = mAuth.getCurrentUser();

        if (currentUser != null) {
            // User is signed in
            email = currentUser.getEmail();
            Log.d(TAG, "User email from Firebase Auth: " + email);
            fetchContactInfo();
        } else {
            // If Firebase Auth doesn't have a logged in user, check for email from intent (as fallback)
            email = getIntent().getStringExtra("EMAIL");

            if (email != null && !email.isEmpty()) {
                Log.d(TAG, "User email from intent (fallback): " + email);
                fetchContactInfo();
            } else {
                Log.e(TAG, "No user logged in and no email in intent");
                // Redirect to login
                Intent loginIntent = new Intent(main_dashboard.this, MainActivity.class);
                startActivity(loginIntent);
                finish();
            }
        }
    }

    private void fetchContactInfo() {
        FirebaseFirestore db = FirebaseFirestore.getInstance();
        contacts.clear();
        Log.d(TAG, "Fetching contacts for email: " + email);
        db.collection("user_info")
                .whereEqualTo("email", email)
                .get()
                .addOnSuccessListener(qs -> {
                    if (qs.isEmpty()) {
                        Log.e(TAG, "No user found with email: " + email);
                        return;
                    }
                    currentUserId = qs.getDocuments().get(0).getId();
                    db.collection("user_info")
                            .document(currentUserId)
                            .collection("contacts")
                            .get()
                            .addOnSuccessListener(qs2 -> {
                                for (DocumentSnapshot doc : qs2) {
                                    String name = doc.getString("name");
                                    String number = doc.getString("number");
                                    if (name == null || number == null) continue;
                                    Contact c = new Contact(name, number, R.drawable.luffy);
                                    c.setContactId(doc.getId());
                                    contacts.add(c);
                                }
                                RecyclerView rv = findViewById(R.id.recycleContactList);
                                rv.setLayoutManager(new LinearLayoutManager(this));
                                rv.setAdapter(new ContactsAdapter(getApplicationContext(), contacts, this));
                            })
                            .addOnFailureListener(e -> Log.e(TAG, "Failed to fetch contact list: ", e));
                })
                .addOnFailureListener(e -> Log.e(TAG, "Failed to fetch user: ", e));
    }

    @Override
    public void on_item_click(int position) {
        Intent intent = new Intent(main_dashboard.this, chatting.class);
        Contact c = contacts.get(position);
        intent.putExtra("name", c.getName());
        intent.putExtra("image", c.getImage());
        intent.putExtra("number", c.getNumber());
        intent.putExtra("userID", currentUserId);
        intent.putExtra("contactID", c.getContactId());
        startActivity(intent);
    }

    public static void navigateToHome(NavBarListener context) {
        if (context instanceof android.content.Context) {
            Intent i = new Intent((android.content.Context) context, main_dashboard.class);
            i.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP | Intent.FLAG_ACTIVITY_SINGLE_TOP);
            ((android.content.Context) context).startActivity(i);
        }
    }
}