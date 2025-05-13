package com.example.sprintly_app_smd_finale;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import java.text.SimpleDateFormat;
import java.util.List;
import java.util.Locale;

public class EventAdapter extends RecyclerView.Adapter<EventAdapter.EventViewHolder> {

    private List<CalendarEvent> events;
    private Context context;
    private EventClickListener eventClickListener;

    // Format for displaying time
    private SimpleDateFormat timeFormat = new SimpleDateFormat("h:mm a", Locale.getDefault());

    public interface EventClickListener {
        void onEditClick(CalendarEvent event, int position);
        void onDeleteClick(CalendarEvent event, int position);
    }

    public EventAdapter(Context context, List<CalendarEvent> events, EventClickListener listener) {
        this.context = context;
        this.events = events;
        this.eventClickListener = listener;
    }

    @NonNull
    @Override
    public EventViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(context).inflate(R.layout.item_event, parent, false);
        return new EventViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull EventViewHolder holder, int position) {
        CalendarEvent event = events.get(position);

        holder.titleText.setText(event.getTitle());

        // Format the time display
        String timeDisplay = timeFormat.format(event.getStartTime()) + " - " +
                timeFormat.format(event.getEndTime());
        holder.timeText.setText(timeDisplay);

        holder.descriptionText.setText(event.getDescription());

        // Set click listeners for edit and delete buttons
        holder.editButton.setOnClickListener(v -> {
            if (eventClickListener != null) {
                eventClickListener.onEditClick(event, holder.getAdapterPosition());
            }
        });

        holder.deleteButton.setOnClickListener(v -> {
            if (eventClickListener != null) {
                eventClickListener.onDeleteClick(event, holder.getAdapterPosition());
            }
        });
    }

    @Override
    public int getItemCount() {
        return events != null ? events.size() : 0;
    }

    public void updateEvents(List<CalendarEvent> newEvents) {
        this.events = newEvents;
        notifyDataSetChanged();
    }

    static class EventViewHolder extends RecyclerView.ViewHolder {
        TextView titleText;
        TextView timeText;
        TextView descriptionText;
        Button editButton;
        Button deleteButton;

        public EventViewHolder(@NonNull View itemView) {
            super(itemView);
            titleText = itemView.findViewById(R.id.eventTitleText);
            timeText = itemView.findViewById(R.id.eventTimeText);
            descriptionText = itemView.findViewById(R.id.eventDescriptionText);
            editButton = itemView.findViewById(R.id.editEventButton);
            deleteButton = itemView.findViewById(R.id.deleteEventButton);
        }
    }
}