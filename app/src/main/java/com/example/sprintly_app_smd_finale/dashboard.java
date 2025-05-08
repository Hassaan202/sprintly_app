package com.example.sprintly_app_smd_finale;

import android.content.Intent;
import android.os.Bundle;
import android.text.Editable;
import android.text.TextWatcher;
import android.util.Log;
import android.widget.Button;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.google.android.material.textfield.TextInputEditText;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;

import java.util.ArrayList;
import java.util.List;

public class dashboard extends AppCompatActivity implements chat_interface {

    private RecyclerView contactsRecyclerView;
    private ContactsAdapter contactsAdapter;
    private List<Contact> contactList;

    private FirebaseFirestore db;
    List<Contact> contacts;

    private TextView userNameTextView;
    private LinearLayout calendarNavItem, tasksNavItem, homeNavItem, profileNavItem;
    private TextView calendarLabel, tasksLabel, homeLabel, profileLabel;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        Log.d("DEBUG_LOG", "in the oncreate");
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_dashboard);
        contactList = new ArrayList<>();
        contacts=contactList;
        currentUserId = FirebaseAuth.getInstance().getCurrentUser().getUid();
        db = FirebaseFirestore.getInstance();
        Log.d("DEBUG_LOG", "getting instance");
        // Setup welcome text
        userNameTextView = findViewById(R.id.usernameText);
        String userName = getIntent().getStringExtra("NAME");
        if (userName != null && !userName.isEmpty()) {
            userNameTextView.setText(userName);
        }
        Log.d("DEBUG_LOG", "getting username");

        setupBottomNavigation();

        // Setup RecyclerView
        contactsRecyclerView = findViewById(R.id.recycleContactList);
        contactsRecyclerView.setLayoutManager(new LinearLayoutManager(this, LinearLayoutManager.HORIZONTAL, false));

        contactsAdapter = new ContactsAdapter(this,contactList,this);
        contactsRecyclerView.setAdapter(contactsAdapter);
        Log.d("DEBUG_LOG", "after setting recycler and adapt");
        // Fetch contacts from Firestore
        fetchContactInfo();

        // Setup search
        /*TextInputEditText searchInput = findViewById(R.id.searchInput);
        searchInput.addTextChangedListener(new TextWatcher() {
            @Override public void beforeTextChanged(CharSequence s, int start, int count, int after) {}
            @Override public void afterTextChanged(Editable s) {}
            @Override public void onTextChanged(CharSequence s, int start, int before, int count) {
                filterContacts(s.toString());
            }
        });*/

        // Placeholder for group creation
       /* Button createContact = findViewById(R.id.createContactBtn);
        createContact.setOnClickListener(v ->
                Toast.makeText(dashboard.this, "Group creation feature coming soon!", Toast.LENGTH_SHORT).show()
        );*/
    }



    private void setupBottomNavigation() {
        Log.d("DEBUG_LOG", "setting bottom nav");
        calendarNavItem = findViewById(R.id.calendarNavItem);
        tasksNavItem = findViewById(R.id.tasksNavItem);
        homeNavItem = findViewById(R.id.homeNavItem);
        profileNavItem = findViewById(R.id.profileNavItem);

        calendarLabel = findViewById(R.id.calendarLabel);
        tasksLabel = findViewById(R.id.tasksLabel);
        homeLabel = findViewById(R.id.homeLabel);
        profileLabel = findViewById(R.id.profileLabel);

        calendarNavItem.setOnClickListener(v -> selectNavItem(calendarLabel));
        tasksNavItem.setOnClickListener(v -> selectNavItem(tasksLabel));
        homeNavItem.setOnClickListener(v -> selectNavItem(homeLabel));
        profileNavItem.setOnClickListener(v -> selectNavItem(profileLabel));
        selectNavItem(homeLabel); // Default selected
        Log.d("DEBUG_LOG", "afer bottom nav");

    }

    private void selectNavItem(TextView selectedLabel) {
        calendarLabel.setVisibility(TextView.GONE);
        tasksLabel.setVisibility(TextView.GONE);
        homeLabel.setVisibility(TextView.GONE);
        profileLabel.setVisibility(TextView.GONE);
        selectedLabel.setVisibility(TextView.VISIBLE);
        Log.d("DEBUG_LOG", "afer select nav item");

    }
    private String currentUserId;
    private String selectedContactId;
    void fetchContactInfo() {
        Log.d("DEBUG_LOG", "fetching contact");
        String email = getIntent().getStringExtra("EMAIL");
        String password = getIntent().getStringExtra("USER_Password");
        Log.d("DEBUG_LOG", " receiving email and pass from intent");
        // Step 1: Find user document by email
        db.collection("user_info")
                .whereEqualTo("email", email)
                .get()
                .addOnSuccessListener(queryDocumentSnapshots -> {
                    if (!queryDocumentSnapshots.isEmpty()) {
                        Log.d("DEBUG_LOG", " found user doc");
                        DocumentSnapshot userDoc = queryDocumentSnapshots.getDocuments().get(0);
                        currentUserId = userDoc.getId(); // Store user ID
                        Log.d("DEBUG_LOG", "Fetched user ID: " + currentUserId);

                        // Step 2: Fetch contacts from subcollection
                        db.collection("user_info")
                                .document(currentUserId)
                                .collection("contacts")
                                .get()
                                .addOnSuccessListener(querySnapshot -> {
                                    List<DocumentSnapshot> contactDocs = querySnapshot.getDocuments();
                                    Log.d("DEBUG_LOG", "Fetched contact documents: " + contactDocs.size());

                                    try {
                                        for (DocumentSnapshot doc : contactDocs) {
                                            // Safely get data with null checks
                                            if (doc != null && doc.exists()) {
                                                String name = doc.getString("name");
                                                String number = doc.getString("number");
                                                String selectedContactId = doc.getId(); // Store contact ID

                                                Contact newContact = new Contact(name, number, R.drawable.luffy);
                                                newContact.setContactId(selectedContactId); // Set the Firestore contactID


                                                /* newContact.setContactId(selectedContactId); // Assuming you add this setter to Contact class*/
                                                contacts.add(newContact);

                                                Log.d("DEBUG_LOG", "Contact ID: " + selectedContactId);
                                                Log.d("DEBUG_LOG", "Raw document data: " + doc.getData());




                                                if (name != null && number != null) {
                                                    Log.d("DEBUG_LOG", "Contact - Name: " + name + ", Number: " + number);
                                                } else {
                                                    Log.d("DEBUG_LOG", "Contact has missing fields - Name: " + name + ", Number: " + number);
                                                }
                                            } else {
                                                Log.d("DEBUG_LOG", "Found null or non-existent document");
                                            }
                                        }
                                        contactsAdapter.notifyDataSetChanged();
                                    } catch (Exception e) {
                                        Log.e("DEBUG_LOG", "Error processing contacts: ", e);
                                    }

                                    Log.d("DEBUG_LOG", "Finished processing contacts");
                                })
                                .addOnFailureListener(e -> Log.e("DEBUG_LOG", "Failed to fetch contacts: ", e));

                    } else {
                        Log.d("DEBUG_LOG", "User not found in user_info.");
                    }
                })
                .addOnFailureListener(e -> Log.e("DEBUG_LOG", "Failed to fetch user: ", e));
    }



    @Override
    public void on_item_click(int position) {
        Intent intent = new Intent(dashboard.this, chatting.class);
        intent.putExtra("name", contacts.get(position).getName());
        intent.putExtra("image", contacts.get(position).getImage());
        intent.putExtra("number", contacts.get(position).getNumber());
        intent.putExtra("userID", currentUserId);
        intent.putExtra("contactID", contacts.get(position).getContactId()); // send correct ID

        startActivity(intent);
    }
}

