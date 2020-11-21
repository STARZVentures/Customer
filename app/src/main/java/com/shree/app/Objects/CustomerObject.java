package com.shree.app.Objects;

import com.google.firebase.database.DataSnapshot;

public class CustomerObject {


    private String id = "",
            name = "",
            phone = "",
            profileImage = "default",
            notificationKey = "";

    public CustomerObject(String id) {
        this.id = id;
    }

    /**
     * CustomerObject constructor
     * Creates an empty object
     */
    public CustomerObject() {
    }


    /**
     * Parse datasnapshot into this object
     *
     * @param dataSnapshot - customer info fetched from the database
     */
    public void parseData(DataSnapshot dataSnapshot) {
        id = dataSnapshot.getKey();
        if (dataSnapshot.child("name").getValue() != null) {
            name = dataSnapshot.child("name").getValue().toString();
        }
        if (dataSnapshot.child("phone").getValue() != null) {
            phone = dataSnapshot.child("phone").getValue().toString();
        }
        if (dataSnapshot.child("profileImageUrl").getValue() != null) {
            profileImage = dataSnapshot.child("profileImageUrl").getValue().toString();
        }
        if(dataSnapshot.child("notificationKey").getValue()!=null){
            notificationKey = dataSnapshot.child("notificationKey").getValue().toString();
        }
    }


    public String getId() {
        return id;
    }


    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getPhone() {
        return phone;
    }

    public void setPhone(String phone) {
        this.phone = phone;
    }

    public String getProfileImage() {
        return profileImage;
    }

    public void setProfileImage(String profileImage) {
        this.profileImage = profileImage;
    }

    public String getNotificationKey() {
        return notificationKey;
    }
}
