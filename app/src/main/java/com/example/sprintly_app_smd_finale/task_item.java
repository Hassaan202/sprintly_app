package com.example.sprintly_app_smd_finale;

public class task_item {
    public String title;
    public String assignee;
    public String dueDate;    // e.g. "2025-05-01"
    public String status;     // e.g. "To Do", "In Progress", "Done"

    public task_item(String title, String assignee, String dueDate, String status) {
        this.title = title;
        this.assignee = assignee;
        this.dueDate = dueDate;
        this.status = status;
    }

    public String getTitle() { return title; }
    public String getAssignee() { return assignee; }
    public String getDueDate() { return dueDate; }
    public String getStatus() { return status; }
}
