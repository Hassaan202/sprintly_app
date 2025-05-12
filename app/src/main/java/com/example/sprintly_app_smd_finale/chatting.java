package com.example.sprintly_app_smd_finale;

import android.os.Bundle;
import android.util.Log;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.google.firebase.Timestamp;
import com.google.firebase.firestore.CollectionReference;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.QueryDocumentSnapshot;
import com.zegocloud.uikit.prebuilt.call.invite.widget.ZegoSendCallInvitationButton;
import com.zegocloud.uikit.service.defines.ZegoUIKitUser;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;

public class chatting extends AppCompatActivity {

    ImageView profileImageView;
    TextView nameTextView;
    RecyclerView chatRecyclerView;

    String userId, contactId;
    List<Chat_message> chatMessages = new ArrayList<>();
    ChatAdapter chatAdapter;
    private EditText messageInput;
    private ImageView sendButton;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_chatting);

        profileImageView = findViewById(R.id.profileImage);
        nameTextView = findViewById(R.id.profileName);
        chatRecyclerView = findViewById(R.id.recyclerChat);

        // Intent
        userId = getIntent().getStringExtra("userID");
        contactId = getIntent().getStringExtra("contactID");
        String name = getIntent().getStringExtra("name");
        int imageResId = getIntent().getIntExtra("image", R.drawable.profile);
// zego buttons
        ZegoSendCallInvitationButton call_btn;
        call_btn = findViewById(R.id.zengo_voiceCall);
        ZegoSendCallInvitationButton video_call_btn;
        video_call_btn = findViewById(R.id.zengo_videoCall);

        //setting the button for voice call
        call_btn.setIsVideoCall(false);
        call_btn.setResourceID("zego_uikit_call");
        call_btn.setInvitees(Collections.singletonList(new ZegoUIKitUser(contactId, name)));
        Log.d("DEBUG_LOG", "target user id voice call" + contactId);
        //setting the button for video call
        video_call_btn.setIsVideoCall(true);
        video_call_btn.setResourceID("zego_uikit_call");
        Log.d("DEBUG_LOG", "target user id video call" + contactId);
        video_call_btn.setInvitees(Collections.singletonList(new ZegoUIKitUser(contactId, name)));

        profileImageView.setImageResource(imageResId);
        nameTextView.setText(name);

        chatAdapter = new ChatAdapter(chatMessages);
        chatRecyclerView.setLayoutManager(new LinearLayoutManager(this));
        chatRecyclerView.setAdapter(chatAdapter);


        fetchChatMessages();
        messageInput = findViewById(R.id.messageInput);
        sendButton = findViewById(R.id.sendButton);

        sendButton.setOnClickListener(v -> sendMessage());
    }

    private void sendMessage() {
        String messageText = messageInput.getText().toString().trim();
        if (messageText.isEmpty()) {
            return;
        }

        // Get current timestamp
        Timestamp timestamp = Timestamp.now();
        SimpleDateFormat sdf = new SimpleDateFormat("hh:mm a", Locale.getDefault());
        String formattedTime = sdf.format(timestamp.toDate());

        // Create message object
        Chat_message newMessage = new Chat_message(messageText, formattedTime, timestamp, true);

        // Add to RecyclerView
        chatMessages.add(newMessage);
        chatAdapter.notifyDataSetChanged();
        chatRecyclerView.scrollToPosition(chatMessages.size() - 1);

        // Save to Firestore
        FirebaseFirestore db = FirebaseFirestore.getInstance();
        Map<String, Object> messageData = new HashMap<>();
        messageData.put("curr_text", messageText);
        messageData.put("time", timestamp);
        Map<String, Object> receiverMessage = new HashMap<>();
        receiverMessage.put("receiver_text", messageText);
        receiverMessage.put("time_recv", timestamp);

        db.collection("user_info")
                .document(userId)
                .collection("contacts")
                .document(contactId)
                .collection("chats_info")
                .add(messageData)
                .addOnSuccessListener(documentReference -> {
                    // Clear input after successful send
                    messageInput.setText("");
                })
                .addOnFailureListener(e -> {
                    Log.e("DEBUG_LOG", "Error sending message: ", e);
                });

        db.collection("user_info")
                .document(contactId) // Y
                .collection("contacts")
                .document(userId) // X
                .collection("chats_info")
                .add(receiverMessage)
                .addOnSuccessListener(documentReference -> {
                    Log.e("DEBUG_LOG", "message saved in receiver: ");
                });
        fetchChatMessages();

    }

    private void fetchChatMessages() {
        FirebaseFirestore db = FirebaseFirestore.getInstance();
        CollectionReference chatsRef = db.collection("user_info")
                .document(userId)
                .collection("contacts")
                .document(contactId)
                .collection("chats_info");

        // 🟢 Real-time listener
        chatsRef.addSnapshotListener((queryDocumentSnapshots, error) -> {
            if (error != null) {
                Log.e("DEBUG_LOG", "Listen failed.", error);
                return;
            }

            if (queryDocumentSnapshots != null) {
                SimpleDateFormat sdf = new SimpleDateFormat("hh:mm a", Locale.getDefault());
                chatMessages.clear(); // Clear the list to avoid duplication

                for (QueryDocumentSnapshot doc : queryDocumentSnapshots) {

                    if (doc.contains("curr_text") && doc.contains("time")) {
                        String message = doc.getString("curr_text");
                        Timestamp time = doc.getTimestamp("time");
                        String formattedTime = sdf.format(time.toDate());
                        chatMessages.add(new Chat_message(message, formattedTime, time, true));
                    }

                    if (doc.contains("receiver_text") && doc.contains("time_recv")) {
                        String message = doc.getString("receiver_text");
                        Timestamp time = doc.getTimestamp("time_recv");
                        String formattedTime = sdf.format(time.toDate());
                        chatMessages.add(new Chat_message(message, formattedTime, time, false));
                    }
                }

                // Sort by timestamp ascending
                Collections.sort(chatMessages, Comparator.comparing(Chat_message::getTimestamp));

                // Notify adapter and scroll to the latest message
                chatAdapter.notifyDataSetChanged();
                chatRecyclerView.scrollToPosition(chatMessages.size() - 1);
            }
        });
    }

}
