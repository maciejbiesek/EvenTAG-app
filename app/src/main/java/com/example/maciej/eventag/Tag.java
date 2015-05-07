package com.example.maciej.eventag;

import java.io.Serializable;

public class Tag implements Serializable {

    private int photoId;
    private int nameId;
    private int descriptionId;
    private int ownerId;
    private int localisationId;
    private int audienceId;
    private int dateId;
    private int durationId;

    public Tag(int photoId, int nameId, int descriptionId, int ownerId, int localisationId, int audienceId, int dateId, int durationId) {
        this.photoId = photoId;
        this.nameId = nameId;
        this.descriptionId = descriptionId;
        this.ownerId = ownerId;
        this.localisationId = localisationId;
        this.audienceId = audienceId;
        this.dateId = dateId;
        this.durationId = durationId;
    }

    public int getPhotoId() { return photoId; }
    public int getNameId() {
        return nameId;
    }
    public int getDescriptionId() { return descriptionId; }
    public int getOwnerId() {
        return ownerId;
    }
    public int getLocalisationId() {
        return localisationId;
    }
    public int getAudienceId() {
        return audienceId;
    }
    public int getDateId() {
        return dateId;
    }
    public int getDurationId() {
        return durationId;
    }
}
