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

import org.w3c.dom.Text;

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
        //ImageView photo = (ImageView) findViewById(R.id.photo);
        TextView name = (TextView) findViewById(R.id.name);
        TextView description = (TextView) findViewById(R.id.description);
        TextView shutdown = (TextView) findViewById(R.id.shutdown);
        TextView localisation = (TextView) findViewById(R.id.localisation);

        //photo.setImageResource(tag.getPhotoId());
        name.setText(tag.getName());
        description.setText(tag.getDescription());
        shutdown.setText(tag.getShutdownTime());
        String localisationString = "" + tag.getLat() + ", " + tag.getLng();
        localisation.setText(localisationString);
    }

}
