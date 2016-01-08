package com.example.maciej.eventag.models;

import com.bluelinelabs.logansquare.annotation.JsonField;
import com.bluelinelabs.logansquare.annotation.JsonObject;

/**
 * Created by maciej on 07.01.16.
 */
@JsonObject
public class Comment {

    @JsonField
    private int id;

    @JsonField
    private String comment;

    @JsonField(name = "user_id")
    private int userId;

    @JsonField(name = "tag_id")
    private int tagId;

    @JsonField
    private User user;


    public int getId() { return id; }
    public String getComment() { return comment; }
    public int getUserId() { return userId; }
    public int getTagId() { return tagId; }
    public User getUser() { return user; }

    public void setId(int id) { this.id = id; }
    public void setComment(String comment) { this.comment = comment; }
    public void setUserId(int userId) { this.userId = userId; }
    public void setTagId(int tagId) { this.tagId = tagId; }
    public void setUser(User user) { this.user = user; }


}
