package com.example.maciej.eventag.Activities;

import android.content.Intent;
import android.os.Bundle;
import android.support.v7.app.ActionBar;
import android.support.v7.app.ActionBarActivity;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import com.example.maciej.eventag.R;
import com.example.maciej.eventag.Models.Tag;
import com.pkmmte.view.CircularImageView;
import com.squareup.picasso.Picasso;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Date;

public class TagDetailsActivity extends ActionBarActivity {

    public static final String TAG_KEY = "tag";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.tag_details);
        showActionBar();

        Intent i = getIntent();
        Tag tag = (Tag) i.getExtras().getSerializable(TAG_KEY);

        showTag(tag);
    }

    private void showTag(Tag tag) {
        CircularImageView photo = (CircularImageView)findViewById(R.id.photo);
        TextView name = (TextView) findViewById(R.id.name);
        TextView description = (TextView) findViewById(R.id.description);
        TextView shutdown = (TextView) findViewById(R.id.shutdown);
        TextView localisation = (TextView) findViewById(R.id.localisation);
        TextView members = (TextView) findViewById(R.id.members);
        ImageButton toMap = (ImageButton) findViewById(R.id.to_map);

        name.setText(tag.getName());
        description.setText(tag.getDescription());
        localisation.setText(tag.getAddress());

        SimpleDateFormat df = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
        Date shutdownDate = null;

        try {
            shutdownDate = df.parse(tag.getShutdownTime());
        } catch (ParseException e) {
            e.printStackTrace();
        }
        String dateInfo = getTimeDiff(shutdownDate);
        shutdown.setText(dateInfo);

        if (dateInfo.toLowerCase().contains("wygasło")) {
            members.setText("Wzięli udział");
        }
        else members.setText("Biorą udział");

        loadImageWithPicasso(tag, photo);

        toMap.setOnClickListener(new OnClickListener() {
            @Override
            public void onClick(View v) {
                finish();
            }
        });
    }

    private void loadImageWithPicasso(Tag tag, ImageView tagPhoto) {
        Picasso.with(this).
                load(tag.getOwner().getAvatarUrl())
                .into(tagPhoto);
    }

    private String getTimeDiff(Date shutdownDate) {
        SimpleDateFormat df = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
        String timeDiff = "";
        Date todayDate = new Date();
        if (todayDate.before(shutdownDate)) {
            long diff = shutdownDate.getTime() - todayDate.getTime();
            long minutes = diff / (1000 * 60);
            if (minutes > 60) {
                long hours = minutes / 60;
                minutes = minutes % 60;
                if (minutes == 0) {
                    timeDiff += hours + " h";
                }
                else {
                    timeDiff += hours + " h " + minutes + " m";
                }

            }
            else {
                timeDiff += minutes + " min";
            }
            timeDiff += " do końca";
        }
        else {
            timeDiff += "Wygasło " + df.format(shutdownDate);
        }
        return timeDiff;

    }



    // MENU

    private void showActionBar() {
        ActionBar actionBar = getSupportActionBar();
        actionBar.setDisplayHomeAsUpEnabled(false);
        actionBar.setDisplayShowHomeEnabled(false);
        actionBar.setDisplayShowCustomEnabled(true);
        actionBar.setDisplayShowTitleEnabled(false);
        View cView = getLayoutInflater().inflate(R.layout.custom_details_menu, null);
        ActionBar.LayoutParams layout = new ActionBar.LayoutParams(ActionBar.LayoutParams.MATCH_PARENT, ActionBar.LayoutParams.WRAP_CONTENT);

        actionBar.setCustomView(cView, layout);
    }

    public void clickEvent(View v) {
        switch (v.getId()) {
            case R.id.back: {
                finish();
                break;
            }
            case R.id.logo: {
                Toast.makeText(this, "EvenTAG", Toast.LENGTH_SHORT).show();
                break;
            }
        }
    }


}
