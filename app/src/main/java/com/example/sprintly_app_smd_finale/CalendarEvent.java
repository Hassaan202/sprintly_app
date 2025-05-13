package com.example.sprintly_app_smd_finale;

import java.util.Date;

/**
 * Data class representing a calendar event
 */
public class CalendarEvent {
    private String id;
    private String title;
    private String description;
    private Date startTime;
    private Date endTime;
    private String date;  // Format: yyyy-MM-dd

    // Empty constructor for Firebase
    public CalendarEvent() {
    }

    public CalendarEvent(String id, String title, String description, Date startTime, Date endTime, String date) {
        this.id = id;
        this.title = title;
        this.description = description;
        this.startTime = startTime;
        this.endTime = endTime;
        this.date = date;
    }

    // Getters and setters
    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public String getTitle() {
        return title;
    }

    public void setTitle(String title) {
        this.title = title;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public Date getStartTime() {
        return startTime;
    }

    public void setStartTime(Date startTime) {
        this.startTime = startTime;
    }

    public Date getEndTime() {
        return endTime;
    }

    public void setEndTime(Date endTime) {
        this.endTime = endTime;
    }

    public String getDate() {
        return date;
    }

    public void setDate(String date) {
        this.date = date;
    }
}