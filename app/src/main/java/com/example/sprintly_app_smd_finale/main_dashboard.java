package com.example.sprintly_app_smd_finale;

import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
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
    private NavBarHelper navBarHelper;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_main_dashboard);

        contacts = new ArrayList<>();
        fetchContactInfo();

        navBarHelper = new NavBarHelper(findViewById(android.R.id.content), new NavBarListener() {
            @Override
            public void onCalendarSelected() {
                // TODO: Launch the Calender Activity
            }

            @Override
            public void onTasksSelected() {
                startActivity(new Intent(main_dashboard.this, task_list.class));
            }

            @Override
            public void onHomeSelected() {
                navigateToHome(this);
            }

            @Override
            public void onProfileSelected() {
                // TODO: Launch the Profile Activity
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
    // Method to handle navigation to home dashboard
    public static void navigateToHome(NavBarListener context) {
        if (context instanceof android.content.Context) {
            // Create intent to launch main_dashboard activity
            Intent intent = new Intent((android.content.Context)context, main_dashboard.class);
            // FLAG_ACTIVITY_CLEAR_TOP will clear all activities on top of main_dashboard
            // So when user clicks home, they return to main_dashboard with a fresh state
            intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP | Intent.FLAG_ACTIVITY_SINGLE_TOP);
            ((android.content.Context)context).startActivity(intent);
        } else {
            Log.e("DEBUG_LOG", "Context passed to navigateToHome is not an Android Context");
        }
    }
}
