package com.example.sprintly_app_smd_finale;

import android.content.Intent;
import android.os.Bundle;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.LinearLayout;

import androidx.activity.EdgeToEdge;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import java.util.ArrayList;
import java.util.List;

public class codeActivity extends AppCompatActivity {

    private RecyclerView chatRecyclerView;
    private EditText codeInputEditText;
    private ImageButton sendButton;
    private List<Message> messageList;
    private codeAdapter codeAdapter;
    private OpenAIHelper openAIHelper;
    String apiKey = "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"; // Replace with your actual OpenAI API key
    String model = "gpt-4.1-nano"; // cheapest model for free usage

    // Navigation items
    private LinearLayout codeNavItem;
    private NavBarHelper navBarHelper;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_code);

        // Initialize UI components
        chatRecyclerView = findViewById(R.id.chatRecyclerView);
        codeInputEditText = findViewById(R.id.codeInputEditText);
        sendButton = findViewById(R.id.sendButton);

        // Initialize message list and adapter
        messageList = new ArrayList<>();
        codeAdapter = new codeAdapter(this, messageList);

        // Set up RecyclerView
        chatRecyclerView.setLayoutManager(new LinearLayoutManager(this));
        chatRecyclerView.setAdapter(codeAdapter);

        // Add welcome message
        addBotMessage("Hi there! I'm your coding assistant. Ask me any coding question, and I'll do my best to help.", null);

        // Set up send button click listener
        sendButton.setOnClickListener(v -> sendMessage());

        // Setup navigation bar
        setupNavbar();

        // Set up OpenAI
        openAIHelper = new OpenAIHelper(apiKey, model);
    }

    private void setupNavbar() {
        navBarHelper = new NavBarHelper(findViewById(android.R.id.content), new NavBarListener() {
            @Override
            public void onCalendarSelected() {
                startActivity(new Intent(codeActivity.this, CalendarActivity.class));
            }

            @Override
            public void onTasksSelected() {
                startActivity(new Intent(codeActivity.this, task_list.class));
            }

            @Override
            public void onHomeSelected() {
                startActivity(new Intent(codeActivity.this, main_dashboard.class));
            }

            @Override
            public void onProfileSelected() {
                startActivity(new Intent(codeActivity.this, ProfileActivity.class));
            }

            @Override
            public void onCodeSelected() {
                // Already in Code Activity, do nothing
            }
        });

        codeNavItem = findViewById(R.id.codeNavItem);
        navBarHelper.selectTab(codeNavItem);
    }

    @Override
    protected void onResume() {
        super.onResume();
        navBarHelper.selectTab(codeNavItem);
    }

    private void sendMessage() {
        String message = codeInputEditText.getText().toString().trim();
        if (message.isEmpty()) {
            return;
        }

        // Add user message to chat
        addUserMessage(message);

        // Clear input field
        codeInputEditText.setText("");

        // Get response from OpenAI
        openAIHelper.askCodingQuestion(message, (response, codeBlock) -> {
            // Add bot response to chat
            addBotMessage(response, codeBlock);
            // Scroll to the bottom of the chat
            chatRecyclerView.smoothScrollToPosition(messageList.size() - 1);
        });
    }

    private void addUserMessage(String content) {
        Message message = new Message(content, Message.TYPE_USER);
        messageList.add(message);
        codeAdapter.notifyItemInserted(messageList.size() - 1);
        chatRecyclerView.smoothScrollToPosition(messageList.size() - 1);
    }

    private void addBotMessage(String content, String codeBlock) {
        Message message = new Message(content, codeBlock, Message.TYPE_BOT);
        messageList.add(message);
        codeAdapter.notifyItemInserted(messageList.size() - 1);
        chatRecyclerView.smoothScrollToPosition(messageList.size() - 1);
    }
}