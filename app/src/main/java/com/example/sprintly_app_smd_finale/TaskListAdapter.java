package com.example.sprintly_app_smd_finale;

import android.util.SparseBooleanArray;
import androidx.appcompat.view.ActionMode;
import androidx.appcompat.view.ActionMode.Callback;
import androidx.appcompat.app.AppCompatActivity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.LinearLayout;
import android.widget.TextView;
import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.RecyclerView;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.firestore.FirebaseFirestore;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

public class TaskListAdapter extends RecyclerView.Adapter<TaskListAdapter.TaskViewHolder> {
    private final List<task_item> tasks;
    private OnItemClickListener listener;
    private SparseBooleanArray selectedItems = new SparseBooleanArray();
    private final ActionMode.Callback actionModeCallback;
    private final AppCompatActivity hostActivity;
    private ActionMode actionMode;
    FirebaseFirestore db;
    FirebaseAuth mAuth;


    class TaskViewHolder extends RecyclerView.ViewHolder {
        TextView tvTitle, tvAssignee, tvDueDate, tvStatus;
        LinearLayout container;

        TaskViewHolder(@NonNull View itemView) {
            super(itemView);
            tvTitle    = itemView.findViewById(R.id.tvTitle);
            tvAssignee = itemView.findViewById(R.id.tvAssignee);
            tvDueDate  = itemView.findViewById(R.id.tvDueDate);
            tvStatus   = itemView.findViewById(R.id.tvStatus);
            container = itemView.findViewById(R.id.container);

            itemView.setOnClickListener(v -> {
                if (listener != null && getAdapterPosition() != RecyclerView.NO_POSITION) {
                    listener.onItemClick(tasks.get(getAdapterPosition()));
                }
            });
        }
    }

    public interface OnItemClickListener { void onItemClick(task_item task); }

    public void setOnItemClickListener(OnItemClickListener listener) {
        this.listener = listener;
    }

    public TaskListAdapter(List<task_item> taskList, ActionMode.Callback actionModeCallback, ActionMode actionMode, AppCompatActivity hostActivity) {
        this.tasks = (taskList != null)
                ? taskList
                : new ArrayList<>();
        this.actionModeCallback = actionModeCallback;
        this.hostActivity = hostActivity;
        this.actionMode = actionMode;
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

        // checks if the view is selected and sets the internal state
        // activated as true
        // Upon long click, toggleSelection() called which invokes
        // notify() function that calls the onBind()/this function
        // again so that activated status updated
        boolean isSelected = selectedItems.get(position, false);
        holder.container.setActivated(isSelected);

        holder.container.setOnClickListener(v -> {
            if (getSelectedCount() > 0) {
                toggleSelection(position);
                actionMode.invalidate();  // update title
            }
        });

        holder.container.setOnLongClickListener(v -> {
            if (actionMode == null) {
                actionMode = ((AppCompatActivity) v.getContext())
                        .startSupportActionMode(actionModeCallback);
            }
            toggleSelection(position);
            actionMode.invalidate();
            return true;
        });

    }

    @Override
    public int getItemCount() {
        return tasks.size();
    }

    // HELPER FUNCTIONS FOR SELECTION AND DELETION
    public void setActionMode(ActionMode mode){
        this.actionMode = mode;
    }
    public void toggleSelection(int pos) {
        if (selectedItems.get(pos, false)) selectedItems.delete(pos);
        else selectedItems.put(pos, true);
        notifyItemChanged(pos);
    }
    public int getSelectedCount() {
        return selectedItems.size();
    }
    public List<Integer> getSelectedPositions() {
        List<Integer> list = new ArrayList<>();
        for (int i = 0; i < selectedItems.size(); i++) {
            list.add(selectedItems.keyAt(i));
        }
        return list;
    }
    public void clearSelection() {
        List<Integer> old = getSelectedPositions();
        selectedItems.clear();
        for (Integer i : old) notifyItemChanged(i);
    }

    // DELETE SELECTED ITEMS
    public void deleteSelected() {
        db = FirebaseFirestore.getInstance();
        mAuth = FirebaseAuth.getInstance();
        FirebaseUser usr = mAuth.getCurrentUser();

        // remove highest indexes first
        List<Integer> sel = getSelectedPositions();
        sel.sort(Collections.reverseOrder());
        for (int pos : sel) {
            //remove from the firestore db
            task_item t = tasks.get(pos);
            if (usr != null && t != null){
                db.collection("user_info")
                        .document(usr.getUid())
                        .collection("tasks")
                        .document(t.getId())
                        .delete();
            }
            tasks.remove(pos);
            notifyItemRemoved(pos);
        }
        clearSelection();
    }
}
