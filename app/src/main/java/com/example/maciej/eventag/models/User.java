package com.example.maciej.eventag.models;

import com.bluelinelabs.logansquare.annotation.JsonField;
import com.bluelinelabs.logansquare.annotation.JsonObject;

import java.io.Serializable;

@JsonObject
public class User implements Serializable {

    @JsonField
    private int id;

    @JsonField
    private String name;

    @JsonField(name = "avatar")
    private String avatarUrl;

    public User() {}

    public User(int id, String name, String avatarUrl) {
        this.id = id;
        this.name = name;
        this.avatarUrl = avatarUrl;
    }

    public int getId() { return id; }
    public String getName() { return name; }
    public String getAvatarUrl() { return avatarUrl; }

    public void setId(int id) { this.id = id; }
    public void setName(String name) { this.name = name; }
    public void setAvatarUrl(String avatarUrl) { this.avatarUrl = avatarUrl; }

    public void setUser(int id, String name, String avatarUrl) {
        this.id = id;
        this.name = name;
        this.avatarUrl = avatarUrl;
    }

}


