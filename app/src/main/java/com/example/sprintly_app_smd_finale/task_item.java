package com.example.sprintly_app_smd_finale;

public class task_item {
    public String id;
    public String title;
    public String assignee;
    public String dueDate;
    public String status;

    public task_item(){}
    public task_item(String id, String title, String assignee, String dueDate, String status) {
        this.id = id;
        this.title = title;
        this.assignee = assignee;
        this.dueDate = dueDate;
        this.status = status;
    }

    public void setId(String id) {
        this.id = id;
    }

    public void setTitle(String title) {
        this.title = title;
    }

    public void setAssignee(String assignee) {
        this.assignee = assignee;
    }

    public void setDueDate(String dueDate) {
        this.dueDate = dueDate;
    }

    public void setStatus(String status) {
        this.status = status;
    }

    public String getId() { return id; }
    public String getTitle() { return title; }
    public String getAssignee() { return assignee; }
    public String getDueDate() { return dueDate; }
    public String getStatus() { return status; }
}
