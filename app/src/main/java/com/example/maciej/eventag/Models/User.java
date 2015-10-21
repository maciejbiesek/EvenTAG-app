package com.example.maciej.eventag.Models;

import java.io.Serializable;
import static com.example.maciej.eventag.Models.Constants.*;

public class User implements Serializable {

    private int id;
    private String name;
    private String avatarUrl;


    public User(int id, String name, String avatarUrl) {
        this.id = id;
        this.name = name;
        this.avatarUrl = avatarUrl;
    }

    public int getId() { return id; }
    public String getName() { return name; }
    public String getAvatarUrl() { return avatarUrl; }

}


