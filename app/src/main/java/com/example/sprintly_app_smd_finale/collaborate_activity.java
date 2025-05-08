//package com.example.sprintly_app_smd_finale;
//
//import android.os.Bundle;
//import android.widget.Button;
//import android.widget.EditText;
//import android.widget.TextView;
//import androidx.annotation.NonNull;
//import androidx.appcompat.app.AppCompatActivity;
//import androidx.recyclerview.widget.LinearLayoutManager;
//import androidx.recyclerview.widget.RecyclerView;
//import com.google.firebase.firestore.*;
//import java.util.ArrayList;
//import java.util.List;
//
//public class collaborate_activity extends AppCompatActivity {
//
//    private TextView welcomeText, usernameText;
//    private EditText searchBar;
//    private RecyclerView contactRecyclerView;
//    private Button createGroupButton;
//
//    private FirebaseFirestore db;
//    private String userId = "user123"; // Replace with dynamic user ID
//    private List<Contact> contactList = new ArrayList<>();
//    private ContactsAdapter contactsAdapter;
//
//    @Override
//    protected void onCreate(Bundle savedInstanceState) {
//        super.onCreate(savedInstanceState);
//        setContentView(R.layout.activity_dashboard);
//
//        welcomeText = findViewById(R.id.welcomeText);
//        usernameText = findViewById(R.id.userName);
//        searchBar = findViewById(R.id.searchInput);
//        contactRecyclerView = findViewById(R.id.contactsRecyclerView);
//        createGroupButton = findViewById(R.id.createGroupButton);
//
//        db = FirebaseFirestore.getInstance();
//        contactRecyclerView.setLayoutManager(new LinearLayoutManager(this, LinearLayoutManager.HORIZONTAL, false));
//        contactsAdapter = new ContactsAdapter(contactList);
//        contactRecyclerView.setAdapter(contactsAdapter);
//
//        String username = getIntent().getStringExtra("username");
//        if (username != null) {
//            usernameText.setText(username);
//        }
//
//        loadContacts();
//    }
//
//    private void loadContacts() {
//        db.collection("user_info").document(userId).collection("contacts")
//                .get()
//                .addOnCompleteListener(task -> {
//                    if (task.isSuccessful()) {
//                        contactList.clear();
//                        for (QueryDocumentSnapshot doc : task.getResult()) {
//                            String name = doc.getString("name");
//                                String number = doc.getString("number");
//                            contactList.add(new Contact(name, number));
//                        }
//                        contactsAdapter.notifyDataSetChanged();
//                    }
//                });
//    }
//}
