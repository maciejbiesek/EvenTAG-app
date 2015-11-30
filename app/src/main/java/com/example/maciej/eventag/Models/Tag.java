package com.example.maciej.eventag.Models;

import java.io.Serializable;

public class Tag implements Serializable {

    private int id;
    private int userId;
    private String name;
    private String description;
    private String shutdownTime;
    private String lat;
    private String lng;
    private User owner;
    private String address;
    private double distance;
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
