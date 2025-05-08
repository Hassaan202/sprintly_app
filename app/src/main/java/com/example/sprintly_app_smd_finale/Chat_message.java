package com.example.sprintly_app_smd_finale;
import com.google.firebase.Timestamp;
public class Chat_message {
    private String message;
    private String time;
    private Timestamp timestamp;



    public Chat_message(String message, String time, Timestamp timestamp) {
        this.message = message;
        this.time = time;
        this.timestamp = timestamp;
    }

    public String getMessage() {
        return message;
    }

    public String getTime() {
        return time;
    }

    public Timestamp getTimestamp() {
        return timestamp;
    }
}
