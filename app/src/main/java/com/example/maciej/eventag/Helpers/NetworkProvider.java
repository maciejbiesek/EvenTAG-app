package com.example.maciej.eventag.Helpers;

import android.app.Activity;
import android.content.Context;
import android.support.v7.app.ActionBar;
import android.view.View;
import android.widget.ViewAnimator;

import com.example.maciej.eventag.Models.Tag;
import com.example.maciej.eventag.R;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.model.BitmapDescriptorFactory;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.Marker;
import com.google.android.gms.maps.model.MarkerOptions;
import com.loopj.android.http.JsonHttpResponseHandler;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

import cz.msebera.android.httpclient.Header;

import static com.example.maciej.eventag.Models.Constants.*;

public class NetworkProvider {

    private Context context;
    private RestClient restClient;
    private CommunicationHelper comHelper;
    public boolean isFinished;

    public NetworkProvider(Context context, String accessKey) {
        this.context = context;
        this.restClient = new RestClient(accessKey);
        this.comHelper = new CommunicationHelper(context);
        this.isFinished = false;
    }

    public void getTags(final List<Tag> tagsList, final GoogleMap map, final HashMap<Marker, Tag> mMarkersHashMap, final ActionBar actBar, final ViewAnimator mapAnimator) {
        String get = "/tags";
        this.restClient.get(get, null, new JsonHttpResponseHandler() {

            @Override
            public void onSuccess(int statusCode, Header[] headers, JSONArray response) {
                super.onSuccess(statusCode, headers, response);
                isFinished = false;
                tagsList.clear();
                try {
                    tagsList.addAll(getTagsFromJson(response, map, mMarkersHashMap));
                    isFinished = true;
                } catch (JSONException e) {
                    e.printStackTrace();
                }
                showActionBar(actBar);
                mapAnimator.setDisplayedChild(1);
            }

            @Override
            public void onFailure(int statusCode, Header[] headers, Throwable throwable, JSONObject response) {
                comHelper.showUserDialog(context.getString(R.string.server_connection), context.getString(R.string.server_fail));
            }
        });
    }

    private List<Tag> getTagsFromJson(JSONArray jArray, GoogleMap map, HashMap<Marker, Tag> mMarkersHashMap) throws JSONException {
        List<Tag> tags = new ArrayList<Tag>();
        tags.clear();

        for (int i = 0; i < jArray.length(); i++) {
            JSONObject jsonData = jArray.getJSONObject(i);
            Tag tag = getTag(jsonData, map, mMarkersHashMap);
            tags.add(tag);
        }
        return tags;
    }

    private Tag getTag(JSONObject jObject, GoogleMap map, HashMap<Marker, Tag> mMarkersHashMap) {
        AddressHelper helper = new AddressHelper(context);

        String latStr = jObject.optString("lat");
        String lngStr = jObject.optString("lng");

        double lat = Double.parseDouble(latStr);
        double lng = Double.parseDouble(lngStr);

        String address = helper.getAddress(lat, lng);

        Tag tag = new Tag(jObject.optInt("id"),
                jObject.optInt("user_id"),
                jObject.optString("name"),
                jObject.optString("message"),
                jObject.optString("shutdown_time"),
                latStr,
                lngStr,
                address);

        MarkerOptions markerOption = new MarkerOptions().position(new LatLng(lat, lng)).title(tag.getName()).snippet(tag.getDescription());
        markerOption.icon(BitmapDescriptorFactory.defaultMarker(BitmapDescriptorFactory.HUE_ORANGE));
            /*
               TO DO
               markerOption.icon(BitmapDescriptorFactory.fromResource(R.drawable.user_customIcon));
               */

        Marker currentMarker = map.addMarker(markerOption);
        mMarkersHashMap.put(currentMarker, tag);

        return tag;
    }

    private void showActionBar(ActionBar actBar) {
        actBar.show();
        actBar.setDisplayHomeAsUpEnabled(false);
        actBar.setDisplayShowHomeEnabled(false);
        actBar.setDisplayShowCustomEnabled(true);
        actBar.setDisplayShowTitleEnabled(false);
        View cView = ((Activity) context).getLayoutInflater().inflate(R.layout.custom_map_menu, null);
        ActionBar.LayoutParams layout = new ActionBar.LayoutParams(ActionBar.LayoutParams.MATCH_PARENT, ActionBar.LayoutParams.WRAP_CONTENT);

        actBar.setCustomView(cView, layout);
    }
}