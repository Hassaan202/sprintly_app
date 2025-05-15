package com.example.sprintly_app_smd_finale;

import android.content.Context;
import android.util.AttributeSet;
import android.util.Log;
import android.view.LayoutInflater;
import android.widget.LinearLayout;
import android.widget.TextView;

import androidx.annotation.Nullable;

import com.github.mikephil.charting.charts.PieChart;
import com.github.mikephil.charting.data.PieData;
import com.github.mikephil.charting.data.PieDataSet;
import com.github.mikephil.charting.data.PieEntry;
import com.github.mikephil.charting.utils.ColorTemplate;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.QueryDocumentSnapshot;

import java.util.ArrayList;

public class TaskVisualization extends LinearLayout {

    PieChart pieChart;
    TextView inProgressCount, DoneCount, notStartedCount;
    FirebaseFirestore db;
    private FirebaseAuth mAuth;
    private String email;

    public TaskVisualization(Context context, @Nullable AttributeSet attrs) {
        super(context, attrs);
        LayoutInflater.from(context).inflate(R.layout.task_visualization, this, true);

        pieChart = findViewById(R.id.pieChart);
        inProgressCount = findViewById(R.id.inProgressCount);
        DoneCount = findViewById(R.id.DoneCount);
        notStartedCount = findViewById(R.id.notStartedCount);

        db = FirebaseFirestore.getInstance();
        mAuth = FirebaseAuth.getInstance();
        fetchDataFromFirestore();
    }

    public void fetchDataFromFirestore() {
        FirebaseUser currentUser = mAuth.getCurrentUser();
        if (currentUser != null) {
            email = currentUser.getEmail();
        }

        FirebaseFirestore db = FirebaseFirestore.getInstance();
        db.collection("user_info")
                .whereEqualTo("email", email)
                .get()
                .addOnCompleteListener(task -> {
                    if (task.isSuccessful() && !task.getResult().isEmpty()) {
                        // Get the first document (assuming email is unique)
                        String userId = task.getResult().getDocuments().get(0).getId();

                        // Now access the 'tasks' subcollection
                        db.collection("user_info")
                                .document(userId)
                                .collection("tasks")
                                .get()
                                .addOnCompleteListener(taskSnapshot -> {
                                    if (taskSnapshot.isSuccessful()) {
                                        int inProgress = 0, Done = 0, notStarted = 0;

                                        for (QueryDocumentSnapshot document : taskSnapshot.getResult()) {
                                            String status = document.getString("status");
                                            if ("In Progress".equals(status)) {
                                                inProgress++;
                                            } else if ("Done".equals(status)) {
                                                Done++;
                                            } else if ("Not Started".equals(status)) {
                                                notStarted++;
                                            }
                                        }

                                        // ✅ **Update TextViews**
                                        inProgressCount.setText(String.valueOf(inProgress));
                                        DoneCount.setText(String.valueOf(Done));
                                        notStartedCount.setText(String.valueOf(notStarted));

                                        // ✅ **Update Pie Chart**
                                        updatePieChart(inProgress, Done, notStarted);

                                    } else {
                                        Log.e("Firestore", "Error fetching tasks: ", taskSnapshot.getException());
                                    }
                                });

                    } else {
                        Log.e("Firestore", "User with email not found: ", task.getException());
                    }
                });
    }


    private void updatePieChart(int inProgress, int completed, int notStarted) {
        ArrayList<PieEntry> entries = new ArrayList<>();
        entries.add(new PieEntry(inProgress, "In Progress"));
        entries.add(new PieEntry(completed, "Done"));
        entries.add(new PieEntry(notStarted, "Not Started"));

        PieDataSet dataSet = new PieDataSet(entries, "Task Status");
        dataSet.setColors(ColorTemplate.COLORFUL_COLORS);

        PieData data = new PieData(dataSet);
        data.setValueTextSize(14f);
        data.setValueTextColor(android.graphics.Color.BLACK);

        pieChart.setData(data);
        pieChart.getDescription().setText("Task Distribution");
        pieChart.getDescription().setTextSize(12f);
        pieChart.animateY(1000);
        pieChart.invalidate(); // Refresh the chart
    }
}
