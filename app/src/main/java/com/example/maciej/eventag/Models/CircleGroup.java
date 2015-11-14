package com.example.maciej.eventag.Models;

import java.io.Serializable;

public class CircleGroup implements Serializable {
    private int id;
    private String name;

    public CircleGroup(int id, String name){
        this.id = id;
        this.name = name;
    }

    public int getId() { return id; }
    public String getName() { return name; }

    public void setId(Integer _id) { id = _id; }
    public void setName(String _name) { name = _name; }
}
