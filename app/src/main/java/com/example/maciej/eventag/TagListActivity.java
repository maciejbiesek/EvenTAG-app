package com.example.maciej.eventag;

import android.app.ProgressDialog;
import android.content.Context;
import android.content.Intent;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.os.AsyncTask;
import android.os.Handler;
import android.support.v4.view.GestureDetectorCompat;
import android.support.v7.app.ActionBar;
import android.support.v7.app.ActionBarActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.GestureDetector;
import android.view.Menu;
import android.view.MenuItem;
import android.view.MotionEvent;
import android.view.View.OnTouchListener;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ListView;
import android.widget.Toast;
import android.widget.ViewAnimator;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;


public class TagListActivity extends ActionBarActivity {

    GestureDetectorCompat gestureDetectorCompat;
    private TagAdapter adapter;
    private List<Tag> tagList = new ArrayList<Tag>();
    private String latitude;
    private String longitude;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.tag_list);
        showActionBar();

        Intent i = getIntent();
        tagList.clear();
        tagList.addAll((ArrayList<Tag>) i.getSerializableExtra("list"));
        latitude = i.getStringExtra("lat");
        longitude = i.getStringExtra("lng");

        gestureDetectorCompat = new GestureDetectorCompat(this, new My2ndGestureListener());

        initializeList();
    }

    private void initializeList() {
        ListView list = (ListView) findViewById(R.id.tag_list);
        adapter = new TagAdapter(this);

        adapter.setTags(tagList);

        list.setAdapter(adapter);

        list.setOnTouchListener(new OnTouchListener() {
            @Override
            public boolean onTouch(View v, MotionEvent event) {
                return gestureDetectorCompat.onTouchEvent(event);
            }
        });

        list.setOnItemClickListener(new AdapterView.OnItemClickListener() {

            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                Tag tag = adapter.getItem(position);
                showTag(tag);
            }
        });
    }

    private void showTag(Tag tag) {
        Intent i = new Intent(this, TagDetailsActivity.class);

        i.putExtra(TagDetailsActivity.TAG_KEY, tag);
        startActivity(i);
    }

    @Override
    public boolean onTouchEvent(MotionEvent event) {
        if (gestureDetectorCompat.onTouchEvent(event)) {
            return true;
        }
        return false;
    }

    //handle 'swipe right' action only
    class My2ndGestureListener extends GestureDetector.SimpleOnGestureListener {

        @Override
        public boolean onFling(MotionEvent event1, MotionEvent event2,
                               float velocityX, float velocityY) {

            if(event2.getX() < event1.getX() && Math.abs(event2.getY() - event1.getY()) < 50){
                finish();
                overridePendingTransition(R.anim.slide_left_in, R.anim.slide_left_out);
            }

            return true;
        }
    }


    // MENU

    private void showActionBar() {
        ActionBar actionBar = getSupportActionBar();
        actionBar.setDisplayHomeAsUpEnabled(false);
        actionBar.setDisplayShowHomeEnabled(false);
        actionBar.setDisplayShowCustomEnabled(true);
        actionBar.setDisplayShowTitleEnabled(false);
        View cView = getLayoutInflater().inflate(R.layout.custom_tag_list_menu, null);
        ActionBar.LayoutParams layout = new ActionBar.LayoutParams(ActionBar.LayoutParams.MATCH_PARENT, ActionBar.LayoutParams.WRAP_CONTENT);

        actionBar.setCustomView(cView, layout);
    }

    public void clickEvent(View v) {
        switch (v.getId()) {
            case R.id.left: {
                // something
                break;
            }
            case R.id.logo: {
                Toast.makeText(this, "hehe", Toast.LENGTH_SHORT).show();
                break;
            }
            case R.id.right: {
                Intent intent = new Intent(TagListActivity.this, AddTagActivity.class);
                intent.putExtra("lat", latitude);
                intent.putExtra("lng", longitude);
                startActivity(intent);
                overridePendingTransition(R.anim.slide_bottom_out, R.anim.slide_bottom_in);
                finish();
            }
        }
    }

}
