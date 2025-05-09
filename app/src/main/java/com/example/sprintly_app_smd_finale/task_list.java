package com.example.sprintly_app_smd_finale;

import android.app.DatePickerDialog;
import android.content.DialogInterface;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.DatePicker;
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
import com.example.sprintly_app_smd_finale.*;

public class task_list extends AppCompatActivity {
    private RecyclerView rvTasks;
    private TaskListAdapter adapter;
    private List<task_item> taskList;

    private void showAddTaskDialog() {
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setTitle("Add New Task");

        View dialogView = getLayoutInflater().inflate(R.layout.dialog_add_task, null);
        builder.setView(dialogView);

        EditText etTitle = dialogView.findViewById(R.id.etTitle);
        EditText etAssignee = dialogView.findViewById(R.id.etAssignee);
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
                String assignee = etAssignee.getText().toString().trim();
                String dueDate = etDueDate.getText().toString().trim();
                String status = etStatus.getText().toString().trim();

                if (title.isEmpty() || assignee.isEmpty() || dueDate.isEmpty() || status.isEmpty()) {
                    Toast.makeText(task_list.this, "Please fill all fields", Toast.LENGTH_SHORT).show();
                } else {
                    task_item newTask = new task_item(title, assignee, dueDate, status);
                    taskList.add(newTask);
                    adapter.notifyItemInserted(taskList.size() - 1);
                    rvTasks.scrollToPosition(taskList.size() - 1); //scroll to added item
                    dialog.dismiss();
                }
            }
        });
    }


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_task_list);

        // SETTING UP THE RECYCLER VIEW
        rvTasks = findViewById(R.id.rvTasks);
        rvTasks.setLayoutManager(new LinearLayoutManager(this));

        taskList = new ArrayList<task_item>();
        taskList.add(new task_item("Example Task", "Hassaan", "2025-05-03",  "To Do"));

        adapter = new TaskListAdapter(taskList);
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
}