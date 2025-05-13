package com.example.sprintly_app_smd_finale;

import android.app.Dialog;
import android.app.TimePickerDialog;
import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.Window;
import android.view.WindowManager;
import android.widget.Button;
import android.widget.CalendarView;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import androidx.activity.EdgeToEdge;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.google.android.material.floatingactionbutton.FloatingActionButton;
import com.google.android.material.textfield.TextInputEditText;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.QueryDocumentSnapshot;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Collections;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;

public class CalendarActivity extends AppCompatActivity implements EventAdapter.EventClickListener {

    private static final String TAG = "CalendarActivity";

    private CalendarView calendarView;
    private TextView selectedDateText;
    private RecyclerView eventRecyclerView;
    private FloatingActionButton fabAddEvent;

    private NavBarHelper navBarHelper;
    private LinearLayout calendarNavItem;

    private FirebaseFirestore db;
    private String currentUserId;
    private String currentDateStr; // yyyy-MM-dd

    private List<CalendarEvent> eventsList;
    private EventAdapter eventAdapter;

    private SimpleDateFormat dateFormatter = new SimpleDateFormat("yyyy-MM-dd", Locale.getDefault());
    private SimpleDateFormat displayDateFormatter = new SimpleDateFormat("MMMM d, yyyy", Locale.getDefault());
    private SimpleDateFormat timeFormatter = new SimpleDateFormat("h:mm a", Locale.getDefault());

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.calendar_dashboard);

        db = FirebaseFirestore.getInstance();

        initializeUI();
        setupNavigation();

        String email = getIntent().getStringExtra("EMAIL");
        if (email != null) {
            fetchUserIdFromEmail(email);
        } else {
            Log.e(TAG, "No email provided in intent");
            Toast.makeText(this, "Error: User information not available", Toast.LENGTH_SHORT).show();
            finish();
        }

        Calendar today = Calendar.getInstance();
        currentDateStr = dateFormatter.format(today.getTime());
        updateSelectedDateDisplay(today.getTime());

        eventsList = new ArrayList<>();
        eventRecyclerView.setLayoutManager(new LinearLayoutManager(this));
        eventAdapter = new EventAdapter(this, eventsList, this);
        eventRecyclerView.setAdapter(eventAdapter);
    }

    private void initializeUI() {
        calendarView = findViewById(R.id.calendarView);
        selectedDateText = findViewById(R.id.selectedDateText);
        eventRecyclerView = findViewById(R.id.eventRecyclerView);
        fabAddEvent = findViewById(R.id.fabAddEvent);

        calendarView.setOnDateChangeListener((view, year, month, dayOfMonth) -> {
            Calendar sel = Calendar.getInstance();
            sel.set(year, month, dayOfMonth);
            currentDateStr = dateFormatter.format(sel.getTime());
            updateSelectedDateDisplay(sel.getTime());
            if (currentUserId != null) fetchEventsForDate(currentDateStr);
        });

        fabAddEvent.setOnClickListener(v -> showEventDialog(null));
    }

    private void setupNavigation() {
        navBarHelper = new NavBarHelper(findViewById(android.R.id.content), new NavBarListener() {
            @Override public void onCalendarSelected() {}
            @Override public void onTasksSelected() {
                Intent intent = new Intent(CalendarActivity.this, task_list.class);
                intent.putExtra("EMAIL", getIntent().getStringExtra("EMAIL"));
                startActivity(intent);
            }
            @Override public void onHomeSelected() {
                Intent intent = new Intent(CalendarActivity.this, main_dashboard.class);
                intent.putExtra("EMAIL", getIntent().getStringExtra("EMAIL"));
                intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
                startActivity(intent);
            }
            @Override public void onProfileSelected() {}
            @Override public void onCodeSelected() {
                startActivity(new Intent(CalendarActivity.this, codeActivity.class));
            }
        });
        calendarNavItem = findViewById(R.id.calendarNavItem);
        navBarHelper.selectTab(calendarNavItem);
    }

    private void fetchUserIdFromEmail(String email) {
        db.collection("user_info")
                .whereEqualTo("email", email)
                .get()
                .addOnSuccessListener(qs -> {
                    if (!qs.isEmpty()) {
                        currentUserId = qs.getDocuments().get(0).getId();
                        fetchEventsForDate(currentDateStr);
                    } else {
                        Log.e(TAG, "User not found for email: " + email);
                        Toast.makeText(this, "User not found", Toast.LENGTH_SHORT).show();
                    }
                })
                .addOnFailureListener(e -> {
                    Log.e(TAG, "Error fetching user: ", e);
                    Toast.makeText(this, "Error: " + e.getMessage(), Toast.LENGTH_SHORT).show();
                });
    }

    private void updateSelectedDateDisplay(Date date) {
        selectedDateText.setText("Events on " + displayDateFormatter.format(date));
    }

    private void fetchEventsForDate(String dateStr) {
        if (currentUserId == null) return;

        db.collection("user_info")
                .document(currentUserId)
                .collection("calendar_events")
                .whereEqualTo("date", dateStr)
                .get()
                .addOnSuccessListener(qs -> {
                    eventsList.clear();
                    for (QueryDocumentSnapshot doc : qs) {
                        CalendarEvent evt = doc.toObject(CalendarEvent.class);
                        evt.setId(doc.getId());
                        eventsList.add(evt);
                    }
                    // Sort by startTime locally
                    Collections.sort(eventsList, (a, b) -> a.getStartTime().compareTo(b.getStartTime()));
                    eventAdapter.updateEvents(eventsList);
                    if (eventsList.isEmpty()) Log.d(TAG, "No events for date: " + dateStr);
                })
                .addOnFailureListener(e -> {
                    Log.e(TAG, "Error fetching events: ", e);
                    Toast.makeText(this, "Error loading events: " + e.getMessage(), Toast.LENGTH_LONG).show();
                });
    }

    private void showEventDialog(CalendarEvent existing) {
        Dialog dlg = new Dialog(this);
        dlg.requestWindowFeature(Window.FEATURE_NO_TITLE);
        dlg.setContentView(R.layout.dialogue_add_event);
        WindowManager.LayoutParams lp = new WindowManager.LayoutParams();
        lp.copyFrom(dlg.getWindow().getAttributes());
        lp.width = WindowManager.LayoutParams.MATCH_PARENT;
        lp.height = WindowManager.LayoutParams.WRAP_CONTENT;
        dlg.getWindow().setAttributes(lp);

        TextInputEditText titleInput = dlg.findViewById(R.id.eventTitleInput);
        TextInputEditText startInput = dlg.findViewById(R.id.eventStartTimeInput);
        TextInputEditText endInput = dlg.findViewById(R.id.eventEndTimeInput);
        TextInputEditText descInput = dlg.findViewById(R.id.eventDescriptionInput);
        Button cancelBtn = dlg.findViewById(R.id.cancelEventButton);
        Button saveBtn = dlg.findViewById(R.id.saveEventButton);

        Calendar startCal = Calendar.getInstance();
        Calendar endCal = Calendar.getInstance();
        endCal.add(Calendar.HOUR_OF_DAY, 1);

        if (existing != null) {
            titleInput.setText(existing.getTitle());
            descInput.setText(existing.getDescription());
            startCal.setTime(existing.getStartTime());
            endCal.setTime(existing.getEndTime());
            startInput.setText(timeFormatter.format(startCal.getTime()));
            endInput.setText(timeFormatter.format(endCal.getTime()));
            saveBtn.setText("Update");
        } else {
            startInput.setText(timeFormatter.format(startCal.getTime()));
            endInput.setText(timeFormatter.format(endCal.getTime()));
        }

        startInput.setOnClickListener(v -> new TimePickerDialog(
                this,
                (view, hour, min) -> {
                    startCal.set(Calendar.HOUR_OF_DAY, hour);
                    startCal.set(Calendar.MINUTE, min);
                    startInput.setText(timeFormatter.format(startCal.getTime()));
                }, startCal.get(Calendar.HOUR_OF_DAY), startCal.get(Calendar.MINUTE), false
        ).show());

        endInput.setOnClickListener(v -> new TimePickerDialog(
                this,
                (view, hour, min) -> {
                    endCal.set(Calendar.HOUR_OF_DAY, hour);
                    endCal.set(Calendar.MINUTE, min);
                    endInput.setText(timeFormatter.format(endCal.getTime()));
                }, endCal.get(Calendar.HOUR_OF_DAY), endCal.get(Calendar.MINUTE), false
        ).show());

        cancelBtn.setOnClickListener(v -> dlg.dismiss());
        saveBtn.setOnClickListener(v -> {
            String title = titleInput.getText().toString().trim();
            if (title.isEmpty()) {
                titleInput.setError("Title is required");
                return;
            }
            if (startCal.after(endCal)) {
                Toast.makeText(this, "Start time must be before end time", Toast.LENGTH_SHORT).show();
                return;
            }
            Map<String,Object> data = new HashMap<>();
            data.put("title", title);
            data.put("description", descInput.getText().toString().trim());
            data.put("startTime", startCal.getTime());
            data.put("endTime", endCal.getTime());
            data.put("date", currentDateStr);
            if (existing != null) updateEvent(existing.getId(), data, dlg);
            else createEvent(data, dlg);
        });

        dlg.show();
    }

    private void createEvent(Map<String, Object> eventData, Dialog dlg) {
        db.collection("user_info")
                .document(currentUserId)
                .collection("calendar_events")
                .add(eventData)
                .addOnSuccessListener(ref -> {
                    Log.d(TAG, "Event created: " + ref.getId());
                    Toast.makeText(this, "Event created successfully", Toast.LENGTH_SHORT).show();
                    fetchEventsForDate(currentDateStr);
                    dlg.dismiss();
                })
                .addOnFailureListener(e -> {
                    Log.e(TAG, "Error creating event: ", e);
                    Toast.makeText(this, "Error creating event: " + e.getMessage(), Toast.LENGTH_SHORT).show();
                });
    }

    private void updateEvent(String id, Map<String, Object> data, Dialog dlg) {
        db.collection("user_info")
                .document(currentUserId)
                .collection("calendar_events")
                .document(id)
                .update(data)
                .addOnSuccessListener(aVoid -> {
                    Log.d(TAG, "Event updated");
                    Toast.makeText(this, "Event updated successfully", Toast.LENGTH_SHORT).show();
                    fetchEventsForDate(currentDateStr);
                    dlg.dismiss();
                })
                .addOnFailureListener(e -> {
                    Log.e(TAG, "Error updating event: ", e);
                    Toast.makeText(this, "Error updating event: " + e.getMessage(), Toast.LENGTH_SHORT).show();
                });
    }

    private void deleteEvent(String id) {
        new AlertDialog.Builder(this)
                .setTitle("Delete Event")
                .setMessage("Are you sure you want to delete this event?")
                .setPositiveButton("Delete", (dialog, which) -> {
                    db.collection("user_info")
                            .document(currentUserId)
                            .collection("calendar_events")
                            .document(id)
                            .delete()
                            .addOnSuccessListener(aVoid -> {
                                Log.d(TAG, "Event deleted");
                                Toast.makeText(this, "Event deleted", Toast.LENGTH_SHORT).show();
                                fetchEventsForDate(currentDateStr);
                            })
                            .addOnFailureListener(e -> {
                                Log.e(TAG, "Error deleting event: ", e);
                                Toast.makeText(this, "Error deleting event", Toast.LENGTH_SHORT).show();
                            });
                })
                .setNegativeButton("Cancel", null)
                .show();
    }

    @Override
    public void onEditClick(CalendarEvent event, int position) {
        showEventDialog(event);
    }

    @Override
    public void onDeleteClick(CalendarEvent event, int position) {
        deleteEvent(event.getId());
    }
}