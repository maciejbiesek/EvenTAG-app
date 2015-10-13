package com.example.maciej.eventag.Models;

import java.io.Serializable;
import java.util.Random;

public class User implements Serializable {

    private final String IMAGES_URL = "http://eventag.websource.com.pl/";

    private int id;
    private String name;
    private String firstName;
    private String lastName;
    private String gender;
    private String avatarUrl;


    public User(int id, String name, String firstName, String lastName, String gender, String avatarUrl) {
        this.id = id;
        this.name = name;
        this.firstName = firstName;
        this.lastName = lastName;
        this.gender = gender;
        this.avatarUrl = IMAGES_URL + avatarUrl;
    }

    public int getId() { return id; }
    public String getName() { return name; }
    public String getFirstName() { return firstName; }
    public String getLastName() { return lastName; }
    public String getGender() { return gender; }
    public String getAvatarUrl() { return avatarUrl; }

}


