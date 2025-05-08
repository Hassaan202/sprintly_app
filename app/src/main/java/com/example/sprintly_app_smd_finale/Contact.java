package com.example.sprintly_app_smd_finale;



public class Contact {
    private String name;
    private String number;
    private int image;
    private String contactId; // Add this
    public Contact() {
        // Required for Firestore
    }

    public Contact(String name, String number,int image) {
        this.name = name;
        this.number = number;
        this.image=image;
    }

    public String getName() {
        return name;
    }

    public String getNumber() {
        return number;
    }
    public int getImage(){
        return image;
    }
    public String getContactId() {
        return contactId;
    }

    public void setContactId(String contactId) {
        this.contactId = contactId;
    }
}
