package com.example.maciej.eventag.Helpers;

import android.app.Activity;
import android.content.Context;
import android.content.SharedPreferences;
import android.support.v7.app.ActionBar;
import android.util.Log;
import android.view.View;
import android.widget.Toast;
import android.widget.ViewAnimator;

import com.example.maciej.eventag.Models.Tag;
import com.example.maciej.eventag.Models.User;
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

import java.io.UnsupportedEncodingException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

import cz.msebera.android.httpclient.Header;
import cz.msebera.android.httpclient.entity.StringEntity;
import cz.msebera.android.httpclient.message.BasicHeader;
import cz.msebera.android.httpclient.protocol.HTTP;

import static com.example.maciej.eventag.Models.Constants.*;

public class NetworkProvider {

    private Context context;
    private RestClient restClient;
    private CommunicationHelper comHelper;
    public boolean isFinished;
    private SharedPreferences preferences;

    public NetworkProvider(Context context) {
        this.context = context;
        this.preferences = context.getSharedPreferences(KEYS, Context.MODE_PRIVATE);
        String accessKey = preferences.getString(ACCESS, "");
        this.restClient = new RestClient(context, accessKey);
        this.comHelper = new CommunicationHelper(context);
        this.isFinished = false;
    }

    public void getUserId() {
        String me = "/users?me";
        this.restClient.get(me, null, new JsonHttpResponseHandler() {
            @Override
            public void onSuccess(int statusCode, Header[] headers, JSONObject response) {
                super.onSuccess(statusCode, headers, response);
                int userId = response.optInt("id");
                SharedPreferences.Editor editor = preferences.edit();
                editor.putInt(USER_ID, userId);
                editor.commit();
            }
        });
    }

    public void getTags(final List<Tag> tagsList, final GoogleMap map, final HashMap<Marker, Tag> mMarkersHashMap, final ActionBar actBar, final ViewAnimator mapAnimator) {
        String get = "/tags?number=20&fields=user";
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

    public void sendTag(final Tag tag) throws UnsupportedEncodingException, JSONException {
        String post = "/tags";

        JSONObject jsonTag = parseTagToJson(tag);
        StringEntity entity = new StringEntity(jsonTag.toString());
        entity.setContentType(new BasicHeader(HTTP.CONTENT_TYPE, "application/json"));


        this.restClient.post(post, entity, new JsonHttpResponseHandler() {
            @Override
            public void onSuccess(int statusCode, Header[] headers, JSONObject response) {
                Toast.makeText(context, context.getString(R.string.add_new_tag), Toast.LENGTH_SHORT);
            }

            @Override
            public void onFailure(int statusCode, Header[] headers, Throwable throwable, JSONObject errorResponse) {
                Toast.makeText(context, context.getString(R.string.server_fail), Toast.LENGTH_SHORT);
            }
        });
    }

    public void editTag(final Tag tag) throws JSONException, UnsupportedEncodingException {
        String edit = "/tags/" + tag.getId();

        JSONObject jsonTag = parseTagToJson(tag);
        StringEntity entity = new StringEntity(jsonTag.toString());
        entity.setContentType(new BasicHeader(HTTP.CONTENT_TYPE, "application/json"));

        this.restClient.put(edit, entity, new JsonHttpResponseHandler() {
            @Override
            public void onSuccess(int statusCode, Header[] headers, JSONObject response) {
                Toast.makeText(context, context.getString(R.string.edit_tag), Toast.LENGTH_SHORT);
            }

            @Override
            public void onFailure(int statusCode, Header[] headers, String responseString, Throwable throwable) {
                Toast.makeText(context, context.getString(R.string.server_fail), Toast.LENGTH_SHORT);
            }


            @Override
            public void onFailure(int statusCode, Header[] headers, Throwable throwable, JSONObject errorResponse) {
                Log.e("ERR", errorResponse.toString());
                Toast.makeText(context, context.getString(R.string.server_fail), Toast.LENGTH_SHORT);
            }
        });
    }

    public void deleteTag(final Tag tag) throws JSONException, UnsupportedEncodingException {
        String delete = "/tags/" + tag.getId();

        this.restClient.delete(delete, new JsonHttpResponseHandler() {
            @Override
            public void onSuccess(int statusCode, Header[] headers, JSONObject response) {
                Toast.makeText(context, context.getString(R.string.delete_tag), Toast.LENGTH_SHORT);
            }

            @Override
            public void onFailure(int statusCode, Header[] headers, String responseString, Throwable throwable) {
                Toast.makeText(context, context.getString(R.string.server_fail), Toast.LENGTH_SHORT);
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

        JSONObject userJson = jObject.optJSONObject("user");
        User user = new User(userJson.optInt("id"), userJson.optString("name"), userJson.optString("avatar"));

        Tag tag = new Tag(jObject.optInt("id"),
                jObject.optInt("user_id"),
                jObject.optString("name"),
                jObject.optString("message"),
                jObject.optString("shutdown_time"),
                latStr,
                lngStr,
                address,
                user);

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

    private JSONObject parseTagToJson(Tag tag) throws JSONException {
        JSONObject tagJson = new JSONObject();
        tagJson.put("name", tag.getName());
        tagJson.put("user_id", tag.getUserId());
        tagJson.put("lat", tag.getLat());
        tagJson.put("lng", tag.getLng());
        tagJson.put("message", tag.getDescription());
        tagJson.put("shutdown_time", tag.getShutdownTime());

        return tagJson;
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