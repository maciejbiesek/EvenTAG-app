package com.example.maciej.eventag;

import android.app.ProgressDialog;
import android.content.Context;
import android.content.Intent;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.os.AsyncTask;
import android.os.Handler;
import android.support.v4.view.GestureDetectorCompat;
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

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.tag_list);

        gestureDetectorCompat = new GestureDetectorCompat(this, new My2ndGestureListener());

        if (isOnline()) {
            initializeList();
        }
        else {
            Toast.makeText(this, "No internet access", Toast.LENGTH_LONG).show();
            finish();
        }
    }

    private void initializeList() {
        ListView list = (ListView) findViewById(R.id.tag_list);
        adapter = new TagAdapter(this);
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

        (new AsyncNetworkTagsProvider()).execute();
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

    public boolean isOnline() {
        ConnectivityManager connMgr = (ConnectivityManager)
                this.getSystemService(Context.CONNECTIVITY_SERVICE);
        NetworkInfo networkInfo = connMgr.getActiveNetworkInfo();
        return (networkInfo != null && networkInfo.isConnected());
    }




    private class AsyncNetworkTagsProvider extends AsyncTask<String, Void, List<Tag>> {

        private final String TAGS_URL = "http://eventag.websource.com.pl/tags";
        private final String USERS_URL = "http://eventag.websource.com.pl/users/";

        @Override
        protected void onPostExecute(List<Tag> result) {
            super.onPostExecute(result);
            adapter.setTags(result);
            adapter.notifyDataSetChanged();
            ViewAnimator animator = (ViewAnimator) findViewById(R.id.animator);
            animator.setDisplayedChild(1);
        }

        @Override
        protected void onPreExecute() {
            super.onPreExecute();
        }

        @Override
        protected List<Tag> doInBackground(String... params) {
            try {
                return getTags();
            } catch (IOException e) {
                e.printStackTrace();
            } catch (JSONException e) {
                e.printStackTrace();
            }
            return null;
        }

        public List<Tag> getTags() throws IOException, JSONException {
            List<Tag> tags = new ArrayList<Tag>();

            String s = downloadFromUrl(TAGS_URL);
            JSONArray jArray = new JSONArray(s);
            for (int i = 0; i < jArray.length(); i++) {
                JSONObject jsonData = jArray.getJSONObject(i);
                int id = jsonData.getInt("user_id");
                String userString = downloadFromUrl(USERS_URL + id);
                JSONObject jsonUser = new JSONObject(userString);
                User user = new User(jsonUser.getInt("id"), jsonUser.getString("name"), jsonUser.getString("first_name"),
                        jsonUser.getString("last_name"), jsonUser.getString("gender"), jsonUser.getString("avatar"));

                Tag tag = new Tag(jsonData.getInt("id"), jsonData.getString("name"), jsonData.getString("message"),
                        jsonData.getString("shutdown_time"), jsonData.getString("lat"), jsonData.getString("lng"), user);
                tags.add(tag);
            }
            return tags;
        }

        private String downloadFromUrl(String URL) throws IOException {
            InputStream is = null;

            try {
                java.net.URL url = new java.net.URL(URL);
                HttpURLConnection conn = (HttpURLConnection) url.openConnection();
                conn.setReadTimeout(10000 /* milliseconds */);
                conn.setConnectTimeout(15000 /* milliseconds */);
                conn.setRequestMethod("GET");
                conn.setDoInput(true);
                conn.connect();
                is = conn.getInputStream();

                return readStream(is);
            }
            finally {
                if (is != null) {
                    is.close();
                }
            }
        }

        public String readStream(InputStream stream) throws IOException {
            BufferedReader r = new BufferedReader(new InputStreamReader(stream));
            StringBuilder total = new StringBuilder();
            String line;
            while ((line = r.readLine()) != null) {
                total.append(line);
            }
            return total.toString();
        }

    }
}
