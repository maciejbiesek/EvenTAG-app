package com.example.maciej.eventag;

import android.content.Context;

import java.util.ArrayList;
import java.util.List;


public class TagProvider {

    private static final List<Tag> TAGS = new ArrayList<>();

    public TagProvider(Context context) {
        TAGS.clear();
        TAGS.add(new Tag(R.drawable.tag1, R.string.tag_name1, R.string.tag_description1, R.string.tag_owner1, R.string.tag_localisation1, R.string.tag_audience, R.string.tag_date1, R.string.tag_duration1));
        TAGS.add(new Tag(R.drawable.tag2, R.string.tag_name2, R.string.tag_description2, R.string.tag_owner2, R.string.tag_localisation2, R.string.tag_audience, R.string.tag_date2, R.string.tag_duration2));
        TAGS.add(new Tag(R.drawable.tag3, R.string.tag_name3, R.string.tag_description3, R.string.tag_owner3, R.string.tag_localisation3, R.string.tag_audience, R.string.tag_date3, R.string.tag_duration3));
        TAGS.add(new Tag(R.drawable.tag4, R.string.tag_name4, R.string.tag_description4, R.string.tag_owner4, R.string.tag_localisation4, R.string.tag_audience, R.string.tag_date4, R.string.tag_duration4));

    }

    public Tag getTag(int position) {
        return TAGS.get(position);
    }

    public int getTagsNumber() {
        return TAGS.size();
    }

}
