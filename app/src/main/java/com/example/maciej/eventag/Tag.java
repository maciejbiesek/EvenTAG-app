package com.example.maciej.eventag;

import java.io.Serializable;
import java.util.Random;

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


    public Tag(int id, String name, String description, String shutdownTime, String lat, String lng, User owner) {
        this.id = id;
        this.userId = 0;
        this.name = name;
        this.description = description;
        this.shutdownTime = shutdownTime;
        this.lat = lat;
        this.lng = lng;
        this.owner = owner;
    }

    public Tag(int userId, String name, String description, String shutdownTime, String lat, String lng) {
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

    public void setAddress(String _address) { address = _address; }
    public void setDistance(double _distance) { distance = _distance; }
}
