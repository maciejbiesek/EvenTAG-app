package com.example.maciej.eventag;

import java.io.Serializable;
import java.util.Random;

public class Tag implements Serializable {

    private int photoId;
    private int id;
    private String name;
    private String description;
    private String shutdownTime;
    private String lat;
    private String lng;


    public Tag(int id, String name, String description, String shutdownTime, String lat, String lng) {
        this.id = id;
        this.name = name;
        this.description = description;
        this.shutdownTime = shutdownTime;
        this.lat = lat;
        this.lng = lng;

        this.photoId = R.drawable.tag1;

    }

    public int getPhotoId() { return photoId; }
    public int getId() { return id; }
    public String getName() { return name; }
    public String getDescription() { return description; }
    public String getShutdownTime() { return shutdownTime; }
    public String getLat() { return lat; }
    public String getLng() { return lng; }
}
