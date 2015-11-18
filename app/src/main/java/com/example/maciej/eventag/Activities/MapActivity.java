package com.example.maciej.eventag.Activities;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.content.IntentSender;
import android.content.SharedPreferences;
import android.location.Location;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.os.Bundle;
import android.support.v7.app.ActionBar;
import android.support.v7.app.ActionBarActivity;
import android.util.Log;
import android.view.View;
import android.widget.ImageButton;
import android.widget.ViewAnimator;

import com.example.maciej.eventag.Helpers.CommunicationHelper;
import com.example.maciej.eventag.Helpers.NetworkProvider;
import com.example.maciej.eventag.R;
import com.example.maciej.eventag.Models.Tag;
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
import com.google.gson.Gson;
import com.google.gson.GsonBuilder;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;

import static com.example.maciej.eventag.Models.Constants.*;

public class MapActivity extends ActionBarActivity implements
        OnMapReadyCallback,
        GoogleApiClient.ConnectionCallbacks,
        GoogleApiClient.OnConnectionFailedListener {

    double user_location_longitude;
    double user_location_latitude;
    double latIntent;
    double lngIntent;
    GoogleMap map;
    Location location;
    LatLng user_location;
    private boolean isFirst;
    private Gson gson;

    public static final String TAG = MapActivity.class.getSimpleName();
    public static List<Tag> tagList;
    private String accessKey;
    private NetworkProvider networkProvider;
    private HashMap<Marker, Tag> mMarkersHashMap;
    private GoogleApiClient mGoogleApiClient;
    private CommunicationHelper comHelper;
    private ViewAnimator mapAnimator;
    private ActionBar actBar;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        actBar = getSupportActionBar();
        actBar.hide();
        setContentView(R.layout.activity_map);
        GsonBuilder gsonBuilder = new GsonBuilder();
        gson = gsonBuilder.create();

        getUserId();
        initialize();

        Intent i = getIntent();
        if (i != null && i.getExtras() != null) {
            latIntent = Double.parseDouble(i.getStringExtra(LAT));
            lngIntent = Double.parseDouble(i.getStringExtra(LNG));
            isFirst = false;
        }
        else {
            isFirst = true;
        }
    }

    private void initialize() {
        tagList = new ArrayList<Tag>();

        comHelper = new CommunicationHelper(this);
        mapAnimator = (ViewAnimator) findViewById(R.id.map_animator);

        mGoogleApiClient = new GoogleApiClient.Builder(this)
                .addConnectionCallbacks(this)
                .addOnConnectionFailedListener(this)
                .addApi(LocationServices.API)
                .build();

        mGoogleApiClient.connect();

        ImageButton addButton = (ImageButton) findViewById(R.id.button_add);
        addButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(MapActivity.this, AddTagActivity.class);
                intent.putExtra(LAT, String.valueOf(user_location_latitude));
                intent.putExtra(LNG, String.valueOf(user_location_longitude));
                isFirst = true;
                startActivity(intent);
                overridePendingTransition(R.anim.slide_bottom_out, R.anim.slide_bottom_in);
            }
        });


        // Initialize the HashMap for Markers and MyMarker object
        mMarkersHashMap = new HashMap<Marker, Tag>();

        SupportMapFragment supportMapFragment = (SupportMapFragment) getSupportFragmentManager()
                .findFragmentById(R.id.map);
        supportMapFragment.getMapAsync(this);
    }

    @Override
    public void onSaveInstanceState(Bundle savedInstanceState) {
        super.onSaveInstanceState(savedInstanceState);
        savedInstanceState.putDouble(STATE_LOCATION_LAT, user_location_latitude);
        savedInstanceState.putDouble(STATE_LOCATION_LNG, user_location_longitude);
    }

    public void onRestoreInstanceState(Bundle savedInstanceState) {
        super.onRestoreInstanceState(savedInstanceState);

        // Restore state members from saved instance
        user_location_latitude = savedInstanceState.getDouble(STATE_LOCATION_LAT);
        user_location_longitude = savedInstanceState.getDouble(STATE_LOCATION_LNG);
        user_location = new LatLng(user_location_latitude, user_location_longitude);
    }

    public void onBackPressed() {
        moveTaskToBack(true);
    }

    @Override
    protected  void onPause() {
        super.onPause();
    }

    @Override
    protected void onResume() {
        super.onResume();
        if ( mGoogleApiClient.isConnected() ) {
            location = LocationServices.FusedLocationApi.getLastLocation(mGoogleApiClient);
            user_location_latitude = location.getLatitude();
            user_location_longitude = location.getLongitude();
            user_location = new LatLng(user_location_latitude, user_location_longitude);
            map.moveCamera(CameraUpdateFactory.newLatLngZoom(user_location, 12));
        }
        setUpMap();
    }

    @Override
    protected void onStart() {
        super.onStart();
    }

    @Override
    protected  void onStop() {
        super.onStop();
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
        setUpMap();
    }

    private void setUpMap() {
        if (map == null) {
            map = ((SupportMapFragment) getSupportFragmentManager().findFragmentById(R.id.map)).getMap();
            map.getUiSettings().setMapToolbarEnabled(false);
        }
        if (isFirst) {
            getTags();
        }
        else {
            getTagsFromGson();
        }

    }

    private void getTags() {
        actBar.hide();
        mapAnimator.setDisplayedChild(0);
        networkProvider = new NetworkProvider(this);
        networkProvider.getTags(tagList, map, mMarkersHashMap, actBar, mapAnimator);
    }

    private void getTagsFromGson() {
        SharedPreferences prefs = getSharedPreferences(PREFS, Context.MODE_PRIVATE);
        String value = prefs.getString(TAGS_GSON, null);
        Tag[] tags = gson.fromJson(value, Tag[].class);
        tagList = new ArrayList<Tag>(Arrays.asList(tags));

        for (Tag tag : tagList) {
            MarkerOptions markerOption = new MarkerOptions().position(new LatLng(Double.parseDouble(tag.getLat()), Double.parseDouble(tag.getLng()))).title(tag.getName()).snippet(tag.getDescription());
            markerOption.icon(BitmapDescriptorFactory.defaultMarker(BitmapDescriptorFactory.HUE_ORANGE));
            Marker currentMarker = map.addMarker(markerOption);
            mMarkersHashMap.put(currentMarker, tag);
        }
        mapAnimator.setDisplayedChild(1);
        showActionBar(actBar);
    }

    @Override
    public void onConnected(Bundle bundle) {
        Log.i(TAG, "Location services connected.");
        location = LocationServices.FusedLocationApi.getLastLocation(mGoogleApiClient);
        user_location_latitude = location.getLatitude();
        user_location_longitude = location.getLongitude();
        user_location = new LatLng(user_location_latitude, user_location_longitude);
        map.setMyLocationEnabled(true);
        if (isFirst) {
            map.moveCamera(CameraUpdateFactory.newLatLng(user_location));
            map.animateCamera(CameraUpdateFactory.newLatLngZoom(user_location, 12));
        }
        else {
            LatLng location = new LatLng(latIntent, lngIntent);
            map.moveCamera(CameraUpdateFactory.newLatLng(location));
            map.animateCamera(CameraUpdateFactory.newLatLngZoom(location, 12));
        }

        map.setOnInfoWindowClickListener(new GoogleMap.OnInfoWindowClickListener() {
            @Override
            public void onInfoWindowClick(Marker marker) {
                Tag tagFromHashMap = mMarkersHashMap.get(marker);
                Intent intent = new Intent(MapActivity.this, TagDetailsActivity.class);
                intent.putExtra(TAG_KEY, tagFromHashMap);
                startActivity(intent);
            }
        });
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

    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        if (resultCode == 0) {
            if (mGoogleApiClient.isConnected()) {
                location = LocationServices.FusedLocationApi.getLastLocation(mGoogleApiClient);
                user_location_latitude = location.getLatitude();
                user_location_longitude = location.getLongitude();
                user_location = new LatLng(user_location_latitude, user_location_longitude);
                map.moveCamera(CameraUpdateFactory.newLatLngZoom(user_location, 12));
            }
            setUpMap();
        }
    }

    private void getUserId() {
        NetworkProvider networkProvider = new NetworkProvider(this);
        networkProvider.getUserId();
    }

    private void showTagList() {
        Intent intent = new Intent(MapActivity.this, TagListActivity.class);
        intent.putExtra(TAGS_LIST, (java.io.Serializable) tagList);
        intent.putExtra(LAT, String.valueOf(user_location_latitude));
        intent.putExtra(LNG, String.valueOf(user_location_longitude));

        String value = gson.toJson(tagList);
        SharedPreferences prefs = getSharedPreferences(PREFS, Context.MODE_PRIVATE);
        SharedPreferences.Editor tagEditor = prefs.edit();
        tagEditor.putString(TAGS_GSON, value);
        tagEditor.commit();

        startActivityForResult(intent, TAG_RESULT);
        overridePendingTransition(R.anim.slide_right_out, R.anim.slide_right_in);
    }

    // MENU

    private void showActionBar(ActionBar actBar) {
        actBar.show();
        actBar.setDisplayHomeAsUpEnabled(false);
        actBar.setDisplayShowHomeEnabled(false);
        actBar.setDisplayShowCustomEnabled(true);
        actBar.setDisplayShowTitleEnabled(false);
        View cView = getLayoutInflater().inflate(R.layout.custom_map_menu, null);
        ActionBar.LayoutParams layout = new ActionBar.LayoutParams(ActionBar.LayoutParams.MATCH_PARENT, ActionBar.LayoutParams.WRAP_CONTENT);

        actBar.setCustomView(cView, layout);
    }

    public void clickEvent(View v) {
        switch (v.getId()) {
            case R.id.left: {
                showTagList();
                break;
            }
            case R.id.logo: {
                isFirst = true;
                Intent intent = new Intent(MapActivity.this, UserProfileActivity.class);
                startActivity(intent);
                break;
            }
        }
    }


}

