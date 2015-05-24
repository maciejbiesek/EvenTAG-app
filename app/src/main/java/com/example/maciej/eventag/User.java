package com.example.maciej.eventag;

import java.io.Serializable;
import java.util.Random;

public class User implements Serializable {

    private int id;
    private String name;
    private String firstName;
    private String lastName;
    private String gender;
    private String avatar;


    public User(int id, String name, String firstName, String lastName, String gender, String avatar) {
        this.id = id;
        this.name = name;
        this.firstName = firstName;
        this.lastName = lastName;
        this.gender = gender;
        this.avatar = avatar;
    }

    public int getId() { return id; }
    public String getName() { return name; }
    public String getFirstName() { return firstName; }
    public String getLastName() { return lastName; }
    public String getGender() { return gender; }
    public String getAvatar() { return avatar; }

}


