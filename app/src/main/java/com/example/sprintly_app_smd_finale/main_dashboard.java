package com.example.sprintly_app_smd_finale;

import android.app.Dialog;
import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.view.Window;
import android.view.WindowManager;
import android.widget.Button;
import android.widget.ImageButton;
import android.widget.LinearLayout;
import android.widget.TextView;

import androidx.activity.EdgeToEdge;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.google.android.material.textfield.TextInputEditText;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class main_dashboard extends AppCompatActivity implements chat_interface {

    List<Contact> contacts;
    private String currentUserId;
    private String selectedContactId;

    // Navigation items
    private LinearLayout calendarNavItem, tasksNavItem, homeNavItem, profileNavItem;
    private TextView calendarLabel, tasksLabel, homeLabel, profileLabel;
    private NavBarHelper navBarHelper;
    private String email;
    Button createContactBtn;
    Dialog contactDialog;

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
                .addOnSuccessListener(queryDocumentSnapshots -> {
                    callback.onCheck(!queryDocumentSnapshots.isEmpty());
                })
                .addOnFailureListener(e -> {
                    Log.e("DEBUG_LOG", "Error checking contact: ", e);
                    callback.onCheck(false);
                });
    }

    private void fetchContactDocId(String contactNumber, OnContactIdFetchListener callback) {
        FirebaseFirestore db = FirebaseFirestore.getInstance();
        db.collection("user_info")
                .whereEqualTo("number", contactNumber)
                .get()
                .addOnSuccessListener(querySnapshot -> {
                    if (!querySnapshot.isEmpty()) {
                        DocumentSnapshot doc = querySnapshot.getDocuments().get(0);
                        callback.onIdFetched(doc.getId());
                    } else {
                        callback.onIdFetched(null);
                    }
                })
                .addOnFailureListener(e -> {
                    Log.e("DEBUG_LOG", "Failed to fetch contact Doc ID: ", e);
                    callback.onIdFetched(null);
                });
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_main_dashboard);

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

        FirebaseFirestore db = FirebaseFirestore.getInstance();

        findContactButton.setOnClickListener(v -> {
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
                    fetchContactDocId(contactNumber, contactId -> {
                        if (contactId != null) {
                            Map<String, Object> contactData = new HashMap<>();
                            contactData.put("name", contactName);
                            contactData.put("number", contactNumber);

                            db.collection("user_info")
                                    .document(currentUserId)
                                    .collection("contacts")
                                    .document(contactId)
                                    .set(contactData)
                                    .addOnSuccessListener(aVoid -> {
                                        Log.d("DEBUG_LOG", "Contact added successfully with Doc ID.");
                                        fetchContactInfo();
                                        contactDialog.dismiss();
                                    })
                                    .addOnFailureListener(e -> {
                                        Log.e("DEBUG_LOG", "Failed to add contact with Doc ID: ", e);
                                    });
                        } else {
                            Log.e("DEBUG_LOG", "Contact not found in user_info.");
                        }
                    });
                }
            });
        });

        closeButton.setOnClickListener(v -> contactDialog.dismiss());
        createContactBtn.setOnClickListener(v -> contactDialog.show());

        contacts = new ArrayList<>();
        fetchContactInfo();

        navBarHelper = new NavBarHelper(findViewById(android.R.id.content), new NavBarListener() {
            @Override
            public void onCalendarSelected() {
                String email = getIntent().getStringExtra("EMAIL");
                Intent intent = new Intent(main_dashboard.this, CalendarActivity.class);
                intent.putExtra("EMAIL", email);
                startActivity(intent);
            }

            @Override
            public void onTasksSelected() {
                String email = getIntent().getStringExtra("EMAIL");
                Intent intent = new Intent(main_dashboard.this, task_list.class);
                intent.putExtra("EMAIL", email);
                startActivity(intent);
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
        email = getIntent().getStringExtra("EMAIL");
        contacts.clear();

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
                                        if (name == null || number == null) continue;
                                        Contact newContact = new Contact(name, number, R.drawable.luffy);
                                        newContact.setContactId(doc.getId());
                                        contacts.add(newContact);
                                    }
                                    RecyclerView recycler_contact = findViewById(R.id.recycleContactList);
                                    recycler_contact.setLayoutManager(new LinearLayoutManager(this));
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

    public static void navigateToHome(NavBarListener context) {
        if (context instanceof android.content.Context) {
            Intent intent = new Intent((android.content.Context) context, main_dashboard.class);
            intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP | Intent.FLAG_ACTIVITY_SINGLE_TOP);
            ((android.content.Context) context).startActivity(intent);
        } else {
            Log.e("DEBUG_LOG", "Context passed to navigateToHome is not an Android Context");
        }
    }
}
