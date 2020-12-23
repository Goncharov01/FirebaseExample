package com.example.firebaseexample;

import java.util.HashMap;

public class Message {

    String author;
    String body;

    public Message() {
    }

    public Message(String author, String body) {
        this.author = author;
        this.body = body;
    }


    public HashMap<String,Object> toMap(){
        HashMap<String,Object> result = new HashMap<>();
        result.put("author", author);
        result.put("body", body);

        return result;
    }


}
