package com.example.sprintly_app_smd_finale;

import android.content.Intent;
import android.os.Bundle;
import android.util.Log;

import androidx.activity.EdgeToEdge;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.firestore.CollectionReference;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;

import java.util.ArrayList;
import java.util.List;

public class main_dashboard extends AppCompatActivity implements chat_interface {


    List<Contact> contacts;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_main_dashboard);


        contacts=new ArrayList<Contact>();
        /*contacts.add(new Contact("ibrahim","123456",R.drawable.luffy));
        contacts.add(new Contact("ali","123456",R.drawable.luffy));
        contacts.add(new Contact("hassan","123456",R.drawable.luffy));
        contacts.add(new Contact("rahat","123456",R.drawable.luffy));
        contacts.add(new Contact("fahad","123456",R.drawable.luffy));
        contacts.add(new Contact("asim","123456",R.drawable.luffy));
        contacts.add(new Contact("husnain","123456",R.drawable.luffy));
        RecyclerView recycler_contact=findViewById(R.id.recycleContactList);
        recycler_contact.setLayoutManager(new LinearLayoutManager(this,LinearLayoutManager.HORIZONTAL,false));
        recycler_contact.setAdapter(new ContactsAdapter(getApplicationContext(),contacts,this));*/
        fetchContactInfo();

    }
    private String currentUserId;
    private String selectedContactId;
    void fetchContactInfo() {
        FirebaseFirestore db = FirebaseFirestore.getInstance();
        String email = getIntent().getStringExtra("EMAIL");
        String password=getIntent().getStringExtra("USER_Password");



        // Step 1: Find user document by email
        db.collection("user_info")
                .whereEqualTo("email", email)
                .get()
                .addOnSuccessListener(queryDocumentSnapshots -> {
                    if (!queryDocumentSnapshots.isEmpty()) {
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
                                                contacts.add(newContact);

                                                /* newContact.setContactId(selectedContactId); // Assuming you add this setter to Contact class*/
                                                contacts.add(newContact);

                                                Log.d("DEBUG_LOG", "Contact ID: " + selectedContactId);
                                                Log.d("DEBUG_LOG", "Raw document data: " + doc.getData());

                                                RecyclerView recycler_contact = findViewById(R.id.recycleContactList);
                                                recycler_contact.setLayoutManager(new LinearLayoutManager(this, LinearLayoutManager.HORIZONTAL, false));
                                                recycler_contact.setAdapter(new ContactsAdapter(getApplicationContext(), contacts, this));

                                                if (name != null && number != null) {
                                                    Log.d("DEBUG_LOG", "Contact - Name: " + name + ", Number: " + number);
                                                } else {
                                                    Log.d("DEBUG_LOG", "Contact has missing fields - Name: " + name + ", Number: " + number);
                                                }
                                            } else {
                                                Log.d("DEBUG_LOG", "Found null or non-existent document");
                                            }
                                        }
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
        Intent intent = new Intent(main_dashboard.this, chatting.class);
        intent.putExtra("name", contacts.get(position).getName());
        intent.putExtra("image", contacts.get(position).getImage());
        intent.putExtra("number", contacts.get(position).getNumber());
        intent.putExtra("userID", currentUserId);
        Log.d("DEBUG_LOG", "userID:"+contacts.get(position).getContactId());
        intent.putExtra("contactID", contacts.get(position).getContactId()); // send correct ID

        startActivity(intent);
    }

}