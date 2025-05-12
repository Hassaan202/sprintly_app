package com.example.sprintly_app_smd_finale;
import com.google.firebase.Timestamp;
public class Chat_message {
    private String message;
    private String time;
    private Timestamp timestamp;
    private boolean isCurrentUser;



    public Chat_message(String message, String time, Timestamp timestamp,boolean isCurrentUser ) {
        this.message = message;
        this.time = time;
        this.timestamp = timestamp;
        this.isCurrentUser=isCurrentUser;
    }

    public String getMessage() {
        return message;
    }

    public boolean get_isCurrentUser() {
        return isCurrentUser;
    }

    public String getTime() {
        return time;
    }

    public Timestamp getTimestamp() {
        return timestamp;
    }
}
