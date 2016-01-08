package com.example.maciej.eventag.models;

import com.bluelinelabs.logansquare.annotation.JsonField;
import com.bluelinelabs.logansquare.annotation.JsonObject;

import java.io.Serializable;

@JsonObject
public class Tag implements Serializable  {

    @JsonField
    private int id;

    @JsonField(name = "user_id")
    private int userId;

    @JsonField
    private String name;

    @JsonField(name = "message")
    private String description;

    @JsonField(name = "shutdown_time")
    private String shutdownTime;

    @JsonField
    private String lat;

    @JsonField
    private String lng;

    private User owner;
    private String address;
    private double distance;

    @JsonField
    private User user;

    private boolean isActive;

    public Tag() {}


    public Tag(int id, int userId, String name, String description, String shutdownTime, String lat, String lng, String address, User user) {
        this.id = id;
        this.userId = userId;
        this.name = name;
        this.description = description;
        this.shutdownTime = shutdownTime;
        this.lat = lat;
        this.lng = lng;
        this.address = address;
        this.user = user;
    }

    public Tag(int id, int userId, String name, String description, String shutdownTime, String lat, String lng) {
        this.id = id;
        this.userId = userId;
        this.name = name;
        this.description = description;
        this.shutdownTime = shutdownTime;
        this.lat = lat;
        this.lng = lng;
        this.owner = null;
    }


    public int getId() { return id; }
    public int getUserId() { return userId; }
    public String getName() { return name; }
    public String getDescription() { return description; }
    public String getShutdownTime() { return shutdownTime; }
    public String getLat() { return lat; }
    public String getLng() { return lng; }
    public User getOwner() { return owner; }
    public String getAddress() { return address; }
    public Double getDistance() { return distance; }
    public User getUser() {return user; }
    public boolean getIsActive() { return isActive; }

    public void setId(int id) { this.id = id; }
    public void setUserId(int userId) { this.userId = userId; }
    public void setName(String name) { this.name = name; }
    public void setDescription(String description) { this.description = description; }
    public void setShutdownTime(String shutdownTime) { this.shutdownTime = shutdownTime; }
    public void setLat(String lat) { this.lat = lat; }
    public void setLng(String lng) { this.lng = lng; }
    public void setAddress(String _address) { address = _address; }
    public void setDistance(double _distance) { distance = _distance; }
    public void setUser(User user) { this.user = user; }
    public void setIsActive(boolean isActive) {this.isActive = isActive; }

    public void setTag(int id, int userId, String name, String description, String shutdownTime, String lat, String lng, String address, User user) {
        this.id = id;
        this.userId = userId;
        this.name = name;
        this.description = description;
        this.shutdownTime = shutdownTime;
        this.lat = lat;
        this.lng = lng;
        this.address = address;
        this.user = user;
    }
}
