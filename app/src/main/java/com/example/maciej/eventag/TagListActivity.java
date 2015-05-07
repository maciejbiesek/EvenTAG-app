package com.example.maciej.eventag;

import android.content.Intent;
import android.support.v4.view.GestureDetectorCompat;
import android.support.v7.app.ActionBarActivity;
import android.os.Bundle;
import android.view.GestureDetector;
import android.view.Menu;
import android.view.MenuItem;
import android.view.MotionEvent;
import android.view.View.OnTouchListener;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ListView;
import android.widget.Toast;


public class TagListActivity extends ActionBarActivity {

    GestureDetectorCompat gestureDetectorCompat;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.tag_list);

        gestureDetectorCompat = new GestureDetectorCompat(this, new My2ndGestureListener());
        initializeList();
    }

    private void initializeList() {
        ListView list = (ListView) findViewById(R.id.tag_list);
        final TagAdapter adapter = new TagAdapter(this);
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

            if(event2.getX() < event1.getX()){
                finish();
                overridePendingTransition(R.anim.slide_left_in, R.anim.slide_left_out);
            }

            return true;
        }
    }
}
