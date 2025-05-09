package com.example.sprintly_app_smd_finale;

import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.LinearLayout;
import android.widget.TextView;

import androidx.activity.EdgeToEdge;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;

import java.util.ArrayList;
import java.util.List;

public class main_dashboard extends AppCompatActivity implements chat_interface {

    List<Contact> contacts;
    private String currentUserId;
    private String selectedContactId;

    // Navigation items
    private LinearLayout calendarNavItem, tasksNavItem, homeNavItem, profileNavItem;
    private TextView calendarLabel, tasksLabel, homeLabel, profileLabel;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_main_dashboard);

        contacts = new ArrayList<>();
        fetchContactInfo();

        // Initialize navigation items
        calendarNavItem = findViewById(R.id.calendarNavItem);
        tasksNavItem = findViewById(R.id.tasksNavItem);
        homeNavItem = findViewById(R.id.homeNavItem);
        profileNavItem = findViewById(R.id.profileNavItem);

        // Initialize labels
        calendarLabel = findViewById(R.id.calendarLabel);
        tasksLabel = findViewById(R.id.tasksLabel);
        homeLabel = findViewById(R.id.homeLabel);
        profileLabel = findViewById(R.id.profileLabel);

        // Set Home as default selected
        setSelectedNav(homeNavItem);

        // Navigation Listeners
        calendarNavItem.setOnClickListener(v -> {
            setSelectedNav(calendarNavItem);
            // TODO: Start Calendar Activity
        });

        tasksNavItem.setOnClickListener(v -> {
            setSelectedNav(tasksNavItem);
            startActivity(new Intent(main_dashboard.this, task_list.class));
        });

        homeNavItem.setOnClickListener(v -> {
            setSelectedNav(homeNavItem);
            // TODO: Start Home Activity
        });

        profileNavItem.setOnClickListener(v -> {
            setSelectedNav(profileNavItem);
            // TODO: Start Profile Activity
        });
    }

    // Method to set the selected navigation and hide the rest
    private void setSelectedNav(LinearLayout selectedNav) {
        calendarLabel.setVisibility(View.GONE);
        tasksLabel.setVisibility(View.GONE);
        homeLabel.setVisibility(View.GONE);
        profileLabel.setVisibility(View.GONE);

        if (selectedNav == calendarNavItem) {
            calendarLabel.setVisibility(View.VISIBLE);
        } else if (selectedNav == tasksNavItem) {
            tasksLabel.setVisibility(View.VISIBLE);
        } else if (selectedNav == homeNavItem) {
            homeLabel.setVisibility(View.VISIBLE);
        } else if (selectedNav == profileNavItem) {
            profileLabel.setVisibility(View.VISIBLE);
        }
    }

    void fetchContactInfo() {
        FirebaseFirestore db = FirebaseFirestore.getInstance();
        String email = getIntent().getStringExtra("EMAIL");

        db.collection("user_info")
                .whereEqualTo("email", email)
                .get()
                .addOnSuccessListener(queryDocumentSnapshots -> {
                    if (!queryDocumentSnapshots.isEmpty()) {
                        DocumentSnapshot userDoc = queryDocumentSnapshots.getDocuments().get(0);
                        currentUserId = userDoc.getId();
                        db.collection("user_info")
                                .document(currentUserId)
                                .collection("contacts")
                                .get()
                                .addOnSuccessListener(querySnapshot -> {
                                    for (DocumentSnapshot doc : querySnapshot) {
                                        String name = doc.getString("name");
                                        String number = doc.getString("number");
                                        Contact newContact = new Contact(name, number, R.drawable.luffy);
                                        newContact.setContactId(doc.getId());
                                        contacts.add(newContact);
                                    }

                                    RecyclerView recycler_contact = findViewById(R.id.recycleContactList);
                                    recycler_contact.setLayoutManager(new LinearLayoutManager(this, LinearLayoutManager.HORIZONTAL, false));
                                    recycler_contact.setAdapter(new ContactsAdapter(getApplicationContext(), contacts, this));
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
        Intent intent = new Intent(main_dashboard.this, chatting.class);
        intent.putExtra("name", contacts.get(position).getName());
        intent.putExtra("image", contacts.get(position).getImage());
        intent.putExtra("number", contacts.get(position).getNumber());
        intent.putExtra("userID", currentUserId);
        intent.putExtra("contactID", contacts.get(position).getContactId());
        startActivity(intent);
    }
}
