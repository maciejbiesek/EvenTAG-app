package com.example.maciej.eventag;

import android.content.Context;
import android.content.Intent;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.os.AsyncTask;
import android.support.v4.view.GestureDetectorCompat;
import android.support.v7.app.ActionBarActivity;
import android.os.Bundle;
import android.view.GestureDetector;
import android.view.Menu;
import android.view.MenuItem;
import android.view.MotionEvent;
import android.view.View;
import android.widget.Button;
import android.widget.Toast;
import android.location.Location;
import android.widget.ViewAnimator;

import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.MapFragment;
import com.google.android.gms.maps.model.Marker;
import com.google.android.gms.maps.model.MarkerOptions;

import org.json.JSONException;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;


public class MapActivity extends ActionBarActivity {

    private List<Tag> tagList = new ArrayList<>();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_map);

        if (isOnline()) {
            (new AsyncNetworkTagsProvider()).execute();
        }
        else {
            Toast.makeText(this, "No internet access", Toast.LENGTH_LONG).show();
        }

        Button leftButton = (Button) findViewById(R.id.button_left);
        leftButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(MapActivity.this, TagListActivity.class);
                intent.putExtra("list", (java.io.Serializable) tagList);
                startActivity(intent);
                overridePendingTransition(R.anim.slide_right_out, R.anim.slide_right_in);
            }
        });

        Button addButton = (Button) findViewById(R.id.button_add);
        addButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(MapActivity.this, AddTagActivity.class);
                startActivity(intent);
                overridePendingTransition(R.anim.slide_bottom_out, R.anim.slide_bottom_in);

            }
        });
    }

    @Override
    protected void onResume() {
        super.onResume();
        ViewAnimator viewAnimator = (ViewAnimator) findViewById(R.id.animator);
        viewAnimator.setDisplayedChild(0);
        if (isOnline()) {
            (new AsyncNetworkTagsProvider()).execute();
        }
        else {
            Toast.makeText(this, "No internet access", Toast.LENGTH_LONG).show();
        }
    }

    public boolean isOnline() {
        ConnectivityManager connMgr = (ConnectivityManager)
                this.getSystemService(Context.CONNECTIVITY_SERVICE);
        NetworkInfo networkInfo = connMgr.getActiveNetworkInfo();
        return (networkInfo != null && networkInfo.isConnected());
    }




    private class AsyncNetworkTagsProvider extends AsyncTask<String, Void, List<Tag>> {

        private NetworkTagsProvider networkTagsProvider;

        @Override
        protected void onPostExecute(List<Tag> result) {
            super.onPostExecute(result);
            tagList.clear();
            tagList.addAll(result);
            ViewAnimator viewAnimator = (ViewAnimator) findViewById(R.id.animator);
            viewAnimator.setDisplayedChild(1);

        }

        @Override
        protected void onPreExecute() {
            super.onPreExecute();
            networkTagsProvider = new NetworkTagsProvider();
        }

        @Override
        protected List<Tag> doInBackground(String... params) {
            try {
                networkTagsProvider.getTagsFromServer();
            } catch (IOException e) {
                e.printStackTrace();
            } catch (JSONException e) {
                e.printStackTrace();
            }
            return networkTagsProvider.getAllTags();
        }

    }

}
