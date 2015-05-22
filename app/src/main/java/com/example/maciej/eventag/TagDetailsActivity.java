package com.example.maciej.eventag;

import android.content.Intent;
import android.os.Bundle;
import android.support.v7.app.ActionBarActivity;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.ScrollView;
import android.widget.TextView;

import com.example.maciej.eventag.R;

public class TagDetailsActivity extends ActionBarActivity {

    public static final String TAG_KEY = "tag";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.tag_details);

        Intent i = getIntent();
        Tag tag = (Tag) i.getExtras().getSerializable(TAG_KEY);

        showTag(tag);
    }

    private void showTag(Tag tag) {
        ImageView photo = (ImageView) findViewById(R.id.photo);
        TextView name = (TextView) findViewById(R.id.name);
        TextView description = (TextView) findViewById(R.id.description);
        TextView owner = (TextView) findViewById(R.id.owner);
        TextView localisation = (TextView) findViewById(R.id.localisation);
        TextView audience = (TextView) findViewById(R.id.audience);
        TextView date = (TextView) findViewById(R.id.date);
        TextView duration = (TextView) findViewById(R.id.duration);

        photo.setImageResource(tag.getPhotoId());
        name.setText(tag.getNameId());
        description.setText(tag.getDescriptionId());
        owner.setText(tag.getOwnerId());
        localisation.setText(tag.getLocalisationId());
        audience.setText(tag.getAudienceId());
        date.setText(tag.getDateId());
        duration.setText(tag.getDurationId());
    }

}
