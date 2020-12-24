package com.example.firebaseexample;


import com.google.firebase.database.IgnoreExtraProperties;

@IgnoreExtraProperties
public class User {

    String name;
    String email;

    public User() {
    }

    public User(String name, String email) {
        this.name = name;
        this.email = email;
    }

}
