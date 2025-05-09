package com.example.sprintly_app_smd_finale;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;
import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import java.util.ArrayList;
import java.util.List;

public class TaskListAdapter extends RecyclerView.Adapter<TaskListAdapter.TaskViewHolder> {
    private final List<task_item> tasks;
    private OnItemClickListener listener;

    public interface OnItemClickListener {
        void onItemClick(task_item task);
    }

    public void setOnItemClickListener(OnItemClickListener listener) {
        this.listener = listener;
    }

    public TaskListAdapter(List<task_item> taskList) {
        this.tasks = (taskList != null)
                ? taskList
                : new ArrayList<>();
    }

    @NonNull
    @Override
    public TaskViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View v = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.task_item_layout, parent, false);
        return new TaskViewHolder(v);
    }

    @Override
    public void onBindViewHolder(@NonNull TaskViewHolder holder, int position) {
        task_item t = tasks.get(position);
        holder.tvTitle.setText(t.getTitle());
        holder.tvAssignee.setText("Assignee: " + t.getAssignee());
        holder.tvDueDate.setText("Due: " + t.getDueDate());
        holder.tvStatus.setText("Status: " + t.getStatus());
    }

    @Override
    public int getItemCount() {
        return tasks.size();
    }

    class TaskViewHolder extends RecyclerView.ViewHolder {
        TextView tvTitle, tvAssignee, tvDueDate, tvStatus;

        TaskViewHolder(@NonNull View itemView) {
            super(itemView);
            tvTitle    = itemView.findViewById(R.id.tvTitle);
            tvAssignee = itemView.findViewById(R.id.tvAssignee);
            tvDueDate  = itemView.findViewById(R.id.tvDueDate);
            tvStatus   = itemView.findViewById(R.id.tvStatus);

            itemView.setOnClickListener(v -> {
                if (listener != null && getAdapterPosition() != RecyclerView.NO_POSITION) {
                    listener.onItemClick(tasks.get(getAdapterPosition()));
                }
            });
        }
    }
}
