package com.example.maciej.eventag;

/**
 * Created by Kostek on 2015-06-05.
 */

public class MyMarker
{
    private Integer mId;
    private String mLabel;
    private String mIcon;
    private Double mLatitude;
    private Double mLongitude;

    public MyMarker(Integer id, String label, String icon, Double latitude, Double longitude)
    {
        this.mId = id;
        this.mLabel = label;
        this.mLatitude = latitude;
        this.mLongitude = longitude;
        this.mIcon = icon;
    }

    public Integer getmId()
    {
        return mId;
    }

    public void setmId(Integer mId)
    {
        this.mId = mId;
    }

    public String getmLabel()
    {
        return mLabel;
    }

    public void setmLabel(String mLabel)
    {
        this.mLabel = mLabel;
    }

    public String getmIcon()
    {
        return mIcon;
    }

    public void setmIcon(String icon)
    {
        this.mIcon = icon;
    }

    public Double getmLatitude()
    {
        return mLatitude;
    }

    public void setmLatitude(Double mLatitude)
    {
        this.mLatitude = mLatitude;
    }

    public Double getmLongitude()
    {
        return mLongitude;
    }

    public void setmLongitude(Double mLongitude)
    {
        this.mLongitude = mLongitude;
    }
}