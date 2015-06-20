package com.example.maciej.eventag;

import android.content.Context;
import android.content.Intent;
import android.content.IntentSender;
import android.location.Location;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.os.AsyncTask;
import android.os.Bundle;
import android.support.v7.app.ActionBarActivity;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.Toast;
import android.widget.ViewAnimator;

import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.api.GoogleApiClient;
import com.google.android.gms.location.LocationServices;
import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.maps.model.BitmapDescriptorFactory;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.Marker;
import com.google.android.gms.maps.model.MarkerOptions;

import org.json.JSONException;

import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;


public class MapActivity extends ActionBarActivity implements
        OnMapReadyCallback,
        GoogleApiClient.ConnectionCallbacks,
        GoogleApiClient.OnConnectionFailedListener {

    double user_location_longitude;
    double user_location_latitude;
    GoogleMap map;
    Location location;
    LatLng user_location;
    public static final String TAG = MapActivity.class.getSimpleName();
    public static List<Tag> tagList = new ArrayList<>();
    private HashMap<Marker, Tag> mMarkersHashMap;
    private GoogleApiClient mGoogleApiClient;
    private final static int CONNECTION_FAILURE_RESOLUTION_REQUEST = 9000;

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
                intent.putExtra("lat", user_location_latitude);
                intent.putExtra("lng", user_location_longitude);
                startActivity(intent);
                overridePendingTransition(R.anim.slide_bottom_out, R.anim.slide_bottom_in);
            }
        });

        // Initialize the HashMap for Markers and MyMarker object
        mMarkersHashMap = new HashMap<>();

        mGoogleApiClient = new GoogleApiClient.Builder(this)
                .addConnectionCallbacks(this)
                .addOnConnectionFailedListener(this)
                .addApi(LocationServices.API)
                .build();

        SupportMapFragment supportMapFragment = (SupportMapFragment) getSupportFragmentManager()
                .findFragmentById(R.id.map);
        supportMapFragment.getMapAsync(this);

    }

    @Override
    protected void onResume() {
        super.onResume();
        mGoogleApiClient.connect();
        ViewAnimator viewAnimator = (ViewAnimator) findViewById(R.id.animator);
        viewAnimator.setDisplayedChild(0);
        if (isOnline()) {
            (new AsyncNetworkTagsProvider()).execute();
        }
        else {
            Toast.makeText(this, "No internet access", Toast.LENGTH_LONG).show();
        }
    }

    @Override
    protected  void onPause() {
        super.onPause();
        if (mGoogleApiClient.isConnected()) {
            mGoogleApiClient.disconnect();
        }
    }

    public boolean isOnline() {
        ConnectivityManager connMgr = (ConnectivityManager)
                this.getSystemService(Context.CONNECTIVITY_SERVICE);
        NetworkInfo networkInfo = connMgr.getActiveNetworkInfo();
        return (networkInfo != null && networkInfo.isConnected());
    }

    @Override
    public void onMapReady(GoogleMap map) {
        Log.i(TAG, "Initializing map success");
        // Sample marker for test
        map.addMarker(new MarkerOptions()
                .position(new LatLng(52.4, 16.82))
                .title("test")
                .alpha(0.4f)
                .icon(BitmapDescriptorFactory
                        .defaultMarker(BitmapDescriptorFactory.HUE_GREEN)));

        map.addMarker(new MarkerOptions()
                .position(new LatLng(52.43, 16.85))
                .title("test")
                .icon(BitmapDescriptorFactory
                        .defaultMarker(BitmapDescriptorFactory.HUE_BLUE)));

        setUpMap();

    }

    private void setUpMap()
    {
        if (map == null)
        {
            map = ((SupportMapFragment) getSupportFragmentManager().findFragmentById(R.id.map)).getMap();
        }
    }

    private void plotMarkers(List<Tag> tags)
    {
        if(tags.size() > 0)
        {
            for (Tag tag : tags)
            {
                // Create user marker with custom icon and other options
                    MarkerOptions markerOption = new MarkerOptions().position(new LatLng(tag.getLat(), tag.getLng()));
                markerOption.icon(BitmapDescriptorFactory
                        .defaultMarker(BitmapDescriptorFactory.HUE_ORANGE));
                /*
                TO DO
                markerOption.icon(BitmapDescriptorFactory.fromResource(R.drawable.user_customIcon));
                 */

                Marker currentMarker = map.addMarker(markerOption);
                mMarkersHashMap.put(currentMarker, tag);

            }
        }
    }

    @Override
    public void onConnected(Bundle bundle) {
        Log.i(TAG, "Location services connected.");
        location = LocationServices.FusedLocationApi.getLastLocation(mGoogleApiClient);
        user_location_latitude = location.getLatitude();
        user_location_longitude = location.getLongitude();
        user_location = new LatLng(user_location_latitude, user_location_longitude);
        map.animateCamera(CameraUpdateFactory.newLatLngZoom(user_location, 12));
    }

    @Override
    public void onConnectionSuspended(int i) {
        Log.i(TAG, "Location services suspended. Please reconnect.");
    }

    @Override
    public void onConnectionFailed(ConnectionResult connectionResult) {
        if (connectionResult.hasResolution()) {
            try {
                // Start an Activity that tries to resolve the error
                connectionResult.startResolutionForResult(this, CONNECTION_FAILURE_RESOLUTION_REQUEST);
            } catch (IntentSender.SendIntentException e) {
                e.printStackTrace();
            }
        } else {
            Log.i(TAG, "Location services connection failed with code " + connectionResult.getErrorCode());
        }
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
            plotMarkers(tagList);
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
