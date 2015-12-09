package com.example.maciej.eventag.Helpers;

import android.app.Activity;
import android.content.Context;
import android.content.SharedPreferences;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.os.AsyncTask;
import android.support.v7.app.ActionBar;
import android.util.Log;
import android.view.View;
import android.widget.Toast;
import android.widget.ViewAnimator;

import com.example.maciej.eventag.Adapters.ImageAdapter;
import com.example.maciej.eventag.Models.CircleGroup;
import com.example.maciej.eventag.Models.CustomMarker;
import com.example.maciej.eventag.Models.Tag;
import com.example.maciej.eventag.Models.User;
import com.example.maciej.eventag.R;
import com.google.android.gms.maps.GoogleMap;
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

    public void getUser(int userId) {
        String me = "/users/" + userId;
        this.restClient.get(me, null, new JsonHttpResponseHandler() {
            @Override
            public void onSuccess(int statusCode, Header[] headers, JSONObject response) {
                super.onSuccess(statusCode, headers, response);
                int userId = response.optInt("id");
                String name = response.optString("name");
                String avatar = response.optString("avatar");
                SharedPreferences.Editor editor = preferences.edit();
                editor.putInt(USER_ID, userId);
                editor.putString(USER_NAME, name);
                editor.putString(USER_AVATAR, avatar);
                editor.commit();
            }
        });
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
        String get = "/tags?number=10&fields=user";
        this.restClient.get(get, null, new JsonHttpResponseHandler() {

            @Override
            public void onSuccess(int statusCode, Header[] headers, JSONArray response) {
                super.onSuccess(statusCode, headers, response);
                isFinished = false;
                tagsList.clear();

                (new AsyncGetTags(tagsList, response, map, mMarkersHashMap, isFinished, actBar, mapAnimator)).execute();
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

        System.out.println(jsonTag);


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

    public void attend(final Tag tag, final ImageAdapter adapter) {
        String attend = "/tags/" + tag.getId() + "/attenders";

        this.restClient.post(attend, null, new JsonHttpResponseHandler() {
            @Override
            public void onSuccess(int statusCode, Header[] headers, JSONObject response) {
                Toast.makeText(context, context.getString(R.string.attend_success), Toast.LENGTH_SHORT);
                getAttenders(tag, adapter);
            }

            @Override
            public void onFailure(int statusCode, Header[] headers, Throwable throwable, JSONObject errorResponse) {
                comHelper.showUserDialog(context.getString(R.string.server_connection), context.getString(R.string.server_fail));
            }
        });
    }

    public void resign(final Tag tag, final ImageAdapter adapter) {
        String resign = "/tags/" + tag.getId() + "/attenders/what";

        this.restClient.delete(resign, new JsonHttpResponseHandler() {
            @Override
            public void onSuccess(int statusCode, Header[] headers, JSONObject response) {
                Toast.makeText(context, context.getString(R.string.resign_success), Toast.LENGTH_SHORT);
                getAttenders(tag, adapter);
            }

            @Override
            public void onFailure(int statusCode, Header[] headers, Throwable throwable, JSONObject errorResponse) {
                comHelper.showUserDialog(context.getString(R.string.server_connection), context.getString(R.string.server_fail));
            }
        });
    }

    public void getCircles(int userId, final ArrayList<CircleGroup> circleGroup) {
        String url = "/users/" + userId + "/circles";
        this.restClient.get(url, null, new JsonHttpResponseHandler() {
            @Override
            public void onSuccess(int statusCode, Header[] headers, JSONArray response) {
                super.onSuccess(statusCode, headers, response);
                Log.d("NetworkProvider", "getCircles SUCCEEDED");
                circleGroup.clear();
                try {
                    circleGroup.addAll(getCirclesFromJson(response));
                } catch (JSONException e) {
                    e.printStackTrace();
                }
            }

            @Override
            public void onFailure(int statusCode, Header[] headers, Throwable throwable, JSONObject response) {
                comHelper.showUserDialog(context.getString(R.string.server_connection), context.getString(R.string.server_fail));
                Log.d("NetworkProvider", "getCircles FAILED");
            }


            @Override
            public void onFailure(int statusCode, Header[] headers, String responseString, Throwable throwable) {
                super.onFailure(statusCode, headers, responseString, throwable);
                Log.d("Failed: ", "" + statusCode);
                Log.d("Error : ", "" + throwable);
            }

        });
    }

    public void getAttenders(final Tag tag, final ImageAdapter adapter) {
        String attenders = "/tags/" + tag.getId() + "/attenders";

        this.restClient.get(attenders, null, new JsonHttpResponseHandler() {
            @Override
            public void onSuccess(int statusCode, Header[] headers, JSONArray response) {
                try {
                    List<User> users = getUsersFromJson(response);
                    adapter.addUsers(users);
                    adapter.notifyDataSetChanged();
                } catch (JSONException e) {
                    e.printStackTrace();
                }
            }

            @Override
            public void onFailure(int statusCode, Header[] headers, Throwable throwable, JSONObject errorResponse) {
                comHelper.showUserDialog(context.getString(R.string.server_connection), context.getString(R.string.server_fail));
            }
        });
    }

    private List<User> getUsersFromJson(JSONArray jArray) throws JSONException {
        List<User> users = new ArrayList<User>();
        users.clear();

        for (int i = 0; i < jArray.length(); i++) {
            JSONObject jsonData = jArray.getJSONObject(i);
            User user = getUser(jsonData);
            users.add(user);
        }
        return users;
    }

    private User getUser(JSONObject jObject) {
        return new User(jObject.optInt("id"),
                    jObject.optString("name"),
                    jObject.optString("avatar"));
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


    private ArrayList<CircleGroup> getCirclesFromJson(JSONArray jArray) throws JSONException {
        ArrayList<CircleGroup> circlesGroup = new ArrayList<>();
        circlesGroup.clear();

        for (int i = 0; i < jArray.length(); i++) {
            JSONObject jsonData = jArray.getJSONObject(i);
            CircleGroup circleGroup = getCircle(jsonData);
            circlesGroup.add(circleGroup);
        }
        return circlesGroup;
    }

    private CircleGroup getCircle(JSONObject jObject) {
        return new CircleGroup(jObject.optInt("id"),
                jObject.optString("name"));
    }

    private Tag getTag(JSONObject jObject, GoogleMap map, final HashMap<Marker, Tag> mMarkersHashMap) {
        AddressHelper helper = new AddressHelper(context);

        String latStr = jObject.optString("lat");
        String lngStr = jObject.optString("lng");

        double lat = Double.parseDouble(latStr);
        double lng = Double.parseDouble(lngStr);

        String address = helper.getAddress(lat, lng);

        JSONObject userJson = jObject.optJSONObject("user");
        User user = new User(userJson.optInt("id"), userJson.optString("name"), userJson.optString("avatar"));

        final Tag tag = new Tag(jObject.optInt("id"),
                jObject.optInt("user_id"),
                jObject.optString("name"),
                jObject.optString("message"),
                jObject.optString("shutdown_time"),
                latStr,
                lngStr,
                address,
                user);

        MarkerOptions markerOption = new MarkerOptions().position(new LatLng(lat, lng)).title(tag.getName()).snippet(tag.getDescription());
        // markerOption.icon(BitmapDescriptorFactory.defaultMarker(BitmapDescriptorFactory.HUE_ORANGE));
        Bitmap bmp = BitmapFactory.decodeResource(context.getResources(), R.drawable.marker);

        (new CustomMarker.AsyncImage(markerOption, map, mMarkersHashMap, bmp, user.getAvatarUrl(), tag)).execute();


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

    public class AsyncGetTags extends AsyncTask<String, Void, Boolean> {

        private List<Tag> tagsList;
        private JSONArray response;
        private GoogleMap map;
        private HashMap<Marker, Tag> mMarkersHashMap;
        private boolean isFinished;
        private ActionBar actBar;
        private ViewAnimator mapAnimator;

        public AsyncGetTags(List<Tag> tagsList, JSONArray response, GoogleMap map, HashMap<Marker, Tag> mMarkersHashMap, boolean isFinished, ActionBar actBar, ViewAnimator mapAnimator) {
            this.tagsList = tagsList;
            this.response = response;
            this.map = map;
            this.mMarkersHashMap = mMarkersHashMap;
            this.isFinished = isFinished;
            this.actBar = actBar;
            this.mapAnimator = mapAnimator;
        }

        @Override
        protected void onPostExecute(Boolean result) {
            super.onPostExecute(result);
            showActionBar(actBar);
            mapAnimator.setDisplayedChild(1);

        }

        @Override
        protected void onPreExecute() {
            super.onPreExecute();
            tagsList.clear();
        }

        @Override
        protected Boolean doInBackground(String... params) {
            try {
                tagsList.addAll(getTagsFromJson(response, map, mMarkersHashMap));
                isFinished = true;
            } catch (JSONException e) {
                e.printStackTrace();
            }

            return isFinished;
        }

    }


}