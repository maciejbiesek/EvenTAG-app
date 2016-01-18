package com.example.maciej.eventag.Activities;

import android.content.Intent;
import android.content.SharedPreferences;
import android.support.v7.app.ActionBar;
import android.support.v7.app.ActionBarActivity;
import android.os.Bundle;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ImageButton;
import android.widget.ListView;
import android.widget.Toast;
import android.widget.ViewAnimator;

import com.example.maciej.eventag.Helpers.NetworkProvider;
import com.example.maciej.eventag.R;
import com.example.maciej.eventag.models.Tag;
import com.example.maciej.eventag.Adapters.TagAdapter;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.Date;
import java.util.List;

import static com.example.maciej.eventag.models.Constants.*;


public class TagListActivity extends BaseActivity {
    private TagAdapter adapter;
    private ViewAnimator viewAnimator;
    private NetworkProvider networkProvider;

    private Float latitude;
    private Float longitude;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.tag_list);
        actBar = getSupportActionBar();
        showActionBar(actBar);

        getLatLngFromPref();

        viewAnimator = (ViewAnimator) findViewById(R.id.animator2);
        viewAnimator.setDisplayedChild(0);
        initializeList();

        networkProvider = new NetworkProvider(this);
        networkProvider.getTagsForList(adapter, viewAnimator);
    }


    private void initializeList() {
        ListView list = (ListView) findViewById(R.id.tag_list);
        adapter = new TagAdapter(this);

        list.setAdapter(adapter);

        list.setOnItemClickListener(new AdapterView.OnItemClickListener() {

            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                Tag tag = adapter.getItem(position);
                showTag(tag);
            }
        });

        ImageButton addButton = (ImageButton) findViewById(R.id.list_fab_add);
        addButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(TagListActivity.this, AddTagActivity.class);
                intent.putExtra(LAT, String.valueOf(latitude));
                intent.putExtra(LNG, String.valueOf(longitude));
                startActivityForResult(intent, TAG_ADD_RESULT);
                overridePendingTransition(R.anim.slide_bottom_out, R.anim.slide_bottom_in);
            }
        });
    }

    private void showTag(Tag tag) {
        Intent i = new Intent(this, TagDetailsActivity.class);

        i.putExtra(TAG_KEY, tag);
        startActivityForResult(i, TAG_DETAILS_RESULT);
    }

    private void getLatLngFromPref() {
        SharedPreferences prefs = getSharedPreferences(PREFS, MODE_PRIVATE);

        latitude = prefs.getFloat(LAT, 0);
        longitude = prefs.getFloat(LNG, 0);
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        if (resultCode == 1) {
            finish();
        }
    }
}
