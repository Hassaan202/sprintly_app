package com.example.sprintly_app_smd_finale;

import android.app.DatePickerDialog;
import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import androidx.appcompat.view.ActionMode;

import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;
import androidx.activity.EdgeToEdge;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.List;
import java.util.Objects;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;

import timber.log.Timber;


public class task_list extends AppCompatActivity {
    private RecyclerView rvTasks;
    private TaskListAdapter adapter;
    private List<task_item> taskList;
    FirebaseAuth mAuth;
    FirebaseFirestore db;
    String userName;
    private ActionMode actionMode;
    private NavBarHelper navBarHelper;
    private View taskNavItem;



    // Setting up the action mode for task selection and operation
    private ActionMode.Callback actionModeCallback = new ActionMode.Callback() {
        @Override
        public boolean onCreateActionMode(ActionMode mode, Menu menu) {
            mode.getMenuInflater().inflate(R.menu.delete_menu, menu);
            return true;
        }

        @Override
        public boolean onPrepareActionMode(ActionMode mode, Menu menu) {
            int count = adapter.getSelectedCount();
            mode.setTitle(count + " selected");
            return false;
        }

        @Override
        public boolean onActionItemClicked(ActionMode mode, MenuItem item) {
            if (item.getItemId() == R.id.action_delete) {
                adapter.deleteSelected();
                mode.finish();
                return true;
            }
            return false;
        }

        @Override
        public void onDestroyActionMode(ActionMode mode) {
            adapter.clearSelection();
            adapter.setActionMode(null);
            actionMode = null;
        }
    };


    private void showAddTaskDialog() {
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setTitle("Add New Task");

        View dialogView = getLayoutInflater().inflate(R.layout.dialog_add_task, null);
        builder.setView(dialogView);

        EditText etTitle = dialogView.findViewById(R.id.etTitle);
        EditText etDueDate = dialogView.findViewById(R.id.etDueDate);
        EditText etStatus = dialogView.findViewById(R.id.etStatus);

        etDueDate.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                final Calendar calendar = Calendar.getInstance();
                int year = calendar.get(Calendar.YEAR);
                int month = calendar.get(Calendar.MONTH);
                int day = calendar.get(Calendar.DAY_OF_MONTH);

                DatePickerDialog datePickerDialog = new DatePickerDialog(task_list.this,
                        (view, selectedYear, selectedMonth, selectedDay) -> {
                            String monthStr, dayStr;
                            selectedMonth += 1;
                            monthStr = selectedMonth < 10? "0"+selectedMonth:String.valueOf(selectedMonth);
                            dayStr = selectedDay < 10? "0"+selectedDay: String.valueOf(selectedDay);
                            String selectedDate = selectedYear + "-" + monthStr + "-" + dayStr;
                            etDueDate.setText(selectedDate);
                        }, year, month, day);
                datePickerDialog.show();
            }
        });

        builder.setPositiveButton("Add", null);
        builder.setNegativeButton("Cancel", null);

        AlertDialog dialog = builder.create();
        dialog.show();

        dialog.getButton(AlertDialog.BUTTON_POSITIVE).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                String title = etTitle.getText().toString().trim();
                String assignee = userName;
                String dueDate = etDueDate.getText().toString().trim();
                String status = etStatus.getText().toString().trim();

                if (title.isEmpty() || assignee.isEmpty() || dueDate.isEmpty() || status.isEmpty()) {
                    Toast.makeText(task_list.this, "Please fill all fields", Toast.LENGTH_SHORT).show();
                } else {
                    task_item newTask = new task_item(null, title, assignee, dueDate, status);
                    FirebaseUser fbUser = mAuth.getCurrentUser();
                    // add to the firestore db
                    if (fbUser != null){
                        String userId = fbUser.getUid();
                        db.collection("user_info")
                                .document(userId)
                                .collection("tasks")
                                .add(newTask)
                                .addOnSuccessListener( docRef -> {
                                            newTask.setId(docRef.getId()); // also store the id for later access
                                            taskList.add(newTask);
                                            adapter.notifyItemInserted(taskList.size() - 1);
                                            rvTasks.scrollToPosition(taskList.size() - 1); //scroll to added item
                                            dialog.dismiss();
                                        }
                                )
                                .addOnFailureListener(e -> {
                                            Log.e("FireStore", "Error querying documents");
                                        }
                                );
                    }
                }
            }
        });
    }


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_task_list);
//getting the email

        // SETTING UP THE RECYCLER VIEW
        rvTasks = findViewById(R.id.rvTasks);
        rvTasks.setLayoutManager(new LinearLayoutManager(this));

        // Setting up the navbar
        navBarHelper = new NavBarHelper(findViewById(android.R.id.content), new NavBarListener() {
            @Override
            public void onCalendarSelected() {
                // TODO: launch Calender activity
            }

            @Override
            public void onTasksSelected() {
                // TODO: no action here

            }

            @Override
            public void onHomeSelected() {
                 String email = getIntent().getStringExtra("EMAIL");
                Intent intent = new Intent(task_list.this, main_dashboard.class);
                intent.putExtra("EMAIL", email);
                startActivity(intent);
            }

            @Override
            public void onProfileSelected() {
                // TODO: launch Calender activity

            }

            @Override
            public void onCodeSelected() {
                startActivity(new Intent(task_list.this, codeActivity.class));
            }
        });
        taskNavItem = findViewById(R.id.tasksNavItem);
        navBarHelper.selectTab(taskNavItem);

        // Connecting the tasks with the user account
        mAuth = FirebaseAuth.getInstance();
        db = FirebaseFirestore.getInstance();

        // init the task array with existing data in the firestore db
        taskList = new ArrayList<task_item>();
        FirebaseUser fbUser = mAuth.getCurrentUser();

        if (fbUser != null){
            String userId = fbUser.getUid();

            db.collection("user_info")
                    .document(userId)
                    .collection("tasks")
                    .get()
                    .addOnSuccessListener( querySnapshot -> {
                            for (DocumentSnapshot doc : querySnapshot.getDocuments()) {
                                task_item t = doc.toObject(task_item.class);
                                // only keep the id stored on the client side
                                if (t != null) {
                                    t.setId(doc.getId());
                                }
                                taskList.add(t);
                                }
                            adapter.notifyItemInserted(taskList.size()-1);
                            }
                    )
                    .addOnFailureListener(e -> {
                            Timber.tag("FireStore").e("Error querying documents");
                            }
                    );


            db.collection("user_info").document(fbUser.getUid())
                    .get()
                    .addOnSuccessListener(documentSnapshot ->
                            userName = Objects.requireNonNull(documentSnapshot.get("name")).toString() );
        }


        adapter = new TaskListAdapter(taskList, actionModeCallback, actionMode,this);
        adapter.setOnItemClickListener(new TaskListAdapter.OnItemClickListener() {
            @Override
            public void onItemClick(task_item task) {
                Toast.makeText(task_list.this, "Clicked: " + task.getTitle(), Toast.LENGTH_SHORT).show();
            }
        });
        rvTasks.setAdapter(adapter);

        //ADDING THE MECHANISM TO ADD NEW TASKS TO THE LIST
        Button btnAddTask = findViewById(R.id.btnAddTask);
        btnAddTask.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                showAddTaskDialog();
            }
        });
    }

    @Override
    protected void onResume() {
        super.onResume();
        navBarHelper.selectTab(taskNavItem);
    }
}