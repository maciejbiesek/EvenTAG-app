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

import com.bluelinelabs.logansquare.LoganSquare;
import com.example.maciej.eventag.Adapters.CommentAdapter;
import com.example.maciej.eventag.Adapters.ImageAdapter;
import com.example.maciej.eventag.Adapters.TagAdapter;
import com.example.maciej.eventag.models.CircleGroup;
import com.example.maciej.eventag.models.Comment;
import com.example.maciej.eventag.models.CustomMarker;
import com.example.maciej.eventag.models.Tag;
import com.example.maciej.eventag.models.User;
import com.example.maciej.eventag.R;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.Marker;
import com.google.android.gms.maps.model.MarkerOptions;
import com.loopj.android.http.JsonHttpResponseHandler;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.Date;
import java.util.HashMap;
import java.util.List;

import cz.msebera.android.httpclient.Header;
import cz.msebera.android.httpclient.entity.StringEntity;
import cz.msebera.android.httpclient.message.BasicHeader;
import cz.msebera.android.httpclient.protocol.HTTP;

import static com.example.maciej.eventag.models.Constants.*;

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
                getUser(userId);
            }
        });
    }

    public void getTagsForList(final TagAdapter adapter, final ViewAnimator viewAnimator) {
        String get = "/tags?number=" + 50 + "&fields=user&active=0";

        this.restClient.get(get, null, new JsonHttpResponseHandler() {
            @Override
            public void onSuccess(int statusCode, Header[] headers, JSONArray response) {
                super.onSuccess(statusCode, headers, response);

                (new AsyncGetTagsForList(response, adapter, viewAnimator)).execute();
            }

            @Override
            public void onFailure(int statusCode, Header[] headers, Throwable throwable, JSONObject response) {
                comHelper.showUserDialog(context.getString(R.string.server_connection), context.getString(R.string.server_fail));
            }
        });
    }

    public void getTagsForMap(final List<Tag> tagsList, final int number, final GoogleMap map, final HashMap<Marker, Tag> mMarkersHashMap, final ActionBar actBar, final ViewAnimator mapAnimator) {
        String get = "/tags?number=" + 50 + "&fields=user";
        this.restClient.get(get, null, new JsonHttpResponseHandler() {

            @Override
            public void onSuccess(int statusCode, Header[] headers, JSONArray response) {
                super.onSuccess(statusCode, headers, response);
                isFinished = false;
                tagsList.clear();

                (new AsyncGetTagsForMap(tagsList, response, map, mMarkersHashMap, isFinished, actBar, mapAnimator)).execute();
            }

            @Override
            public void onFailure(int statusCode, Header[] headers, Throwable throwable, JSONObject response) {
                comHelper.showUserDialog(context.getString(R.string.server_connection), context.getString(R.string.server_fail));
            }
        });
    }

    public void sendTag(final Tag tag) throws IOException, JSONException {
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

    public void editTag(final Tag tag) throws JSONException, IOException {
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

    public void postCircle(int userId, String circleName) throws UnsupportedEncodingException, JSONException {
        String post = "/users/" + userId + "/circles";

        JSONObject circleNameJson = parseCircleNameToJson(circleName);
        StringEntity entity = new StringEntity(circleNameJson.toString());
        entity.setContentType(new BasicHeader(HTTP.CONTENT_TYPE, "application/json"));

        Log.i("TEST", "Poszlo!");

        this.restClient.post(post, entity, new JsonHttpResponseHandler() {
            @Override
            public void onSuccess(int statusCode, Header[] headers, JSONObject response) {
                Toast.makeText(context, "Dodawanie zakończone!", Toast.LENGTH_SHORT);
            }

            @Override
            public void onFailure(int statusCode, Header[] headers, Throwable throwable, JSONObject errorResponse) {
                Toast.makeText(context, "Coś poszło nie tak", Toast.LENGTH_SHORT);
            }

            @Override
            public void onFailure(int statusCode, Header[] headers, String responseString, Throwable throwable) {
                Toast.makeText(context, context.getString(R.string.server_fail), Toast.LENGTH_SHORT);
                Log.d("Failed: ", "" + statusCode);
                Log.d("Error : ", "" + throwable);
            }
        });
    }

    private JSONObject parseCircleNameToJson(String circleName) throws JSONException {
        JSONObject circleNameJson = new JSONObject();
        circleNameJson.put("name", circleName);

        return circleNameJson;
    }

    public void updateCircle(int userId, int circleId, String circleName, String usersParam) throws UnsupportedEncodingException, JSONException {
        String put = "/users/" + userId + "/circles/" + circleId;

        JSONObject circleNameJson = parseUpdateCircleToJson(circleName, usersParam);
        StringEntity entity = new StringEntity(circleNameJson.toString());
        entity.setContentType(new BasicHeader(HTTP.CONTENT_TYPE, "application/json"));

        Log.i("TEST", "Poszlo!");

        this.restClient.put(put, entity, new JsonHttpResponseHandler() {
            @Override
            public void onSuccess(int statusCode, Header[] headers, JSONObject response) {
                Toast.makeText(context, "Aktualizowanie zakończone!", Toast.LENGTH_SHORT);
            }

            @Override
            public void onFailure(int statusCode, Header[] headers, Throwable throwable, JSONObject errorResponse) {
                Toast.makeText(context, "Coś poszło nie tak", Toast.LENGTH_SHORT);
            }

            @Override
            public void onFailure(int statusCode, Header[] headers, String responseString, Throwable throwable) {
                Toast.makeText(context, context.getString(R.string.server_fail), Toast.LENGTH_SHORT);
                Log.d("Failed: ", "" + statusCode);
                Log.d("Error : ", "" + throwable);
            }
        });
    }

    private JSONObject parseUpdateCircleToJson(String circleName, String usersParam) throws JSONException {
        JSONObject circleNameJson = new JSONObject();
        circleNameJson.put("name", circleName);
        circleNameJson.put("users", usersParam);

        return circleNameJson;
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

    public void getCircleDetails(int userId, int circleId, final ArrayList<User> usersInCircle) {
        String url = "/users/" + userId + "/circles/" +circleId;
        this.restClient.get(url, null, new JsonHttpResponseHandler() {
            @Override
            public void onSuccess(int statusCode, Header[] headers, JSONArray response) {
                super.onSuccess(statusCode, headers, response);
                Log.d("NetworkProvider", "getCircleDetails SUCCEEDED");
                usersInCircle.clear();
                try {
                    usersInCircle.addAll(getCircleDetailsFromJson(response));
                } catch (JSONException e) {
                    e.printStackTrace();
                }
            }

            @Override
            public void onFailure(int statusCode, Header[] headers, Throwable throwable, JSONObject response) {
                comHelper.showUserDialog(context.getString(R.string.server_connection), context.getString(R.string.server_fail));
                Log.d("NetworkProvider", "getCircleDetails FAILED");
            }


            @Override
            public void onFailure(int statusCode, Header[] headers, String responseString, Throwable throwable) {
                super.onFailure(statusCode, headers, responseString, throwable);
                Log.d("Failed: ", "" + statusCode);
                Log.d("Error : ", "" + throwable);
            }

        });
    }

    private ArrayList<User> getCircleDetailsFromJson(JSONArray jArray) throws JSONException {
        ArrayList<User> userInCircles = new ArrayList<>();
        userInCircles.clear();

        for (int i = 0; i < jArray.length(); i++) {
            JSONObject jsonData = jArray.getJSONObject(i);
            User user = getCircleDetails(jsonData);
            userInCircles.add(user);
        }
        return userInCircles;
    }

    private User getCircleDetails(JSONObject jObject) {
        return new User(jObject.optInt("id"),
                jObject.optString("name"),
                jObject.optString("avatar"));
    }

    public void deleteCircle(Integer myId, Integer circleId) {
        String delete = "/users/" + myId + "/circles/" + circleId;

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

    public void postFriend(int senderId, String recipientEmail){
        // to do when Szymon will update server
        String postFriend = "";
        this.restClient.post(postFriend, null, new JsonHttpResponseHandler() {
            @Override
            public void onSuccess(int statusCode, Header[] headers, JSONArray response) {
                super.onSuccess(statusCode, headers, response);

//                try {
//
//                } catch (JSONException e) {
//                    e.printStackTrace();
//                }
            }

            @Override
            public void onFailure(int statusCode, Header[] headers, Throwable throwable, JSONObject response) {
                comHelper.showUserDialog(context.getString(R.string.server_connection), context.getString(R.string.server_fail));
                Log.d("NetworkProvider", "getCircleDetails FAILED");
            }
        });
    }

    public ArrayList<User> getFriends(final ArrayList<User> userList){
        String friends = "/friends";
        final ArrayList<User> returnUser = new ArrayList<>();

        this.restClient.get(friends, null, new JsonHttpResponseHandler() {
            @Override
            public void onSuccess(int statusCode, Header[] headers, JSONArray response) {
                userList.clear();
                try {
                    returnUser.addAll(getUsersFromJson(response));
                } catch (JSONException e) {
                    e.printStackTrace();
                }
            }

            @Override
            public void onFailure(int statusCode, Header[] headers, Throwable throwable, JSONObject errorResponse) {
                comHelper.showUserDialog(context.getString(R.string.server_connection), context.getString(R.string.server_fail));
            }

            @Override
            public void onFailure(int statusCode, Header[] headers, String responseString, Throwable throwable) {
                super.onFailure(statusCode, headers, responseString, throwable);
                Log.d("Failed: ", "" + statusCode);
                Log.d("Error : ", "" + throwable);
            }

        });

        return returnUser;
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

    public void getComments(final Tag tag, final CommentAdapter commentAdapter) {
        final String comments = "/tags/" + tag.getId() + "/comments";

        this.restClient.get(comments, null, new JsonHttpResponseHandler() {
            @Override
            public void onSuccess(int statusCode, Header[] headers, JSONArray response) {
                super.onSuccess(statusCode, headers, response);
                try {
                    Log.v("BANG", String.valueOf(response));
                    List<Comment> comments = getCommentsFromJson(response);
                    Log.v("BANG", String.valueOf(comments));
                    commentAdapter.setComments(comments);
                    commentAdapter.notifyDataSetChanged();
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

    public void sendComment(final Tag tag, final String comment, final CommentAdapter commentAdapter) throws IOException, JSONException {
        String addComment = "/tags/" + tag.getId() + "/comments";

        JSONObject jObject = new JSONObject();
        jObject.put("comment", comment);

        StringEntity entity = new StringEntity(jObject.toString());
        entity.setContentType(new BasicHeader(HTTP.CONTENT_TYPE, "application/json"));

        this.restClient.post(addComment, entity, new JsonHttpResponseHandler() {
            @Override
            public void onSuccess(int statusCode, Header[] headers, JSONObject response) {
                Toast.makeText(context, context.getString(R.string.add_new_comment), Toast.LENGTH_SHORT);
                getComments(tag, commentAdapter);

            }

            @Override
            public void onFailure(int statusCode, Header[] headers, Throwable throwable, JSONObject errorResponse) {
                Toast.makeText(context, context.getString(R.string.server_fail), Toast.LENGTH_SHORT);
            }
        });
    }

    public void editComment(final Tag tag, final Comment comment, final CommentAdapter commentAdapter) throws JSONException, IOException {
        String editComment = "/tags/" + tag.getId() + "/comments/" + comment.getId();
        JSONObject jsonComment = parseCommentToJson(comment);
        StringEntity entity = new StringEntity(jsonComment.toString());
        entity.setContentType(new BasicHeader(HTTP.CONTENT_TYPE, "application/json"));

        this.restClient.put(editComment, entity, new JsonHttpResponseHandler() {
            @Override
            public void onSuccess(int statusCode, Header[] headers, JSONObject response) {
                Toast.makeText(context, context.getString(R.string.edit_tag), Toast.LENGTH_SHORT);
                getComments(tag, commentAdapter);
            }

            @Override
            public void onFailure(int statusCode, Header[] headers, String responseString, Throwable throwable) {
                Toast.makeText(context, context.getString(R.string.server_fail), Toast.LENGTH_SHORT);
            }


            @Override
            public void onFailure(int statusCode, Header[] headers, Throwable throwable, JSONObject errorResponse) {
                Toast.makeText(context, context.getString(R.string.server_fail), Toast.LENGTH_SHORT);
            }
        });
    }

    public void deleteComment(final Tag tag, final Comment comment, final CommentAdapter commentAdapter) throws JSONException, UnsupportedEncodingException {
        String deleteComment = "/tags/" + tag.getId() + "/comments/" + comment.getId();

        this.restClient.delete(deleteComment, new JsonHttpResponseHandler() {
            @Override
            public void onSuccess(int statusCode, Header[] headers, JSONObject response) {
                Toast.makeText(context, context.getString(R.string.delete_tag), Toast.LENGTH_SHORT);
                getComments(tag, commentAdapter);
            }

            @Override
            public void onFailure(int statusCode, Header[] headers, String responseString, Throwable throwable) {
                Toast.makeText(context, context.getString(R.string.server_fail), Toast.LENGTH_SHORT);
            }
        });
    }

    private List<Comment> getCommentsFromJson(JSONArray jArray) throws JSONException {
        List<Comment> comments = new ArrayList<>();
        comments.clear();

        for (int i = 0; i < jArray.length(); i++) {
            JSONObject jsonData = jArray.getJSONObject(i);
            try {
                Comment comment = LoganSquare.parse(jsonData.toString(), Comment.class);
                comments.add(comment);
            } catch (IOException e) {
                e.printStackTrace();
            }
        }

        return comments;
    }

    private List<User> getUsersFromJson(JSONArray jArray) throws JSONException {
        List<User> users = new ArrayList<User>();
        users.clear();

        for (int i = 0; i < jArray.length(); i++) {
            JSONObject jsonData = jArray.getJSONObject(i);
            try {
                User user = getUser(jsonData);
                users.add(user);
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
        return users;
    }

    private User getUser(JSONObject jObject) throws IOException {
        return LoganSquare.parse(jObject.toString(), User.class);
    }

    private List<Tag> getTagsFromJson(JSONArray jArray, GoogleMap map, HashMap<Marker, Tag> mMarkersHashMap) throws JSONException {
        List<Tag> tags = new ArrayList<Tag>();
        tags.clear();

        for (int i = 0; i < jArray.length(); i++) {
            JSONObject jsonData = jArray.getJSONObject(i);
            try {
                Tag tag = getTag(jsonData, map, mMarkersHashMap);
            } catch (IOException e) {
                e.printStackTrace();
            }
            // Tag tag = getTag(jsonData, map, mMarkersHashMap);
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

    private Tag getTag(JSONObject jObject, GoogleMap map, final HashMap<Marker, Tag> mMarkersHashMap) throws IOException {
        AddressHelper helper = new AddressHelper(context);

        Tag tag = LoganSquare.parse(jObject.toString(), Tag.class);

//        String latStr = jObject.optString("lat");
//        String lngStr = jObject.optString("lng");
//
//        double lat = Double.parseDouble(latStr);
//        double lng = Double.parseDouble(lngStr);

        double lat = Double.parseDouble(tag.getLat());
        double lng = Double.parseDouble(tag.getLng());

        String address = helper.getAddress(lat, lng);
        tag.setAddress(address);

//        JSONObject userJson = jObject.optJSONObject("user");
//        User user = new User(userJson.optInt("id"), userJson.optString("name"), userJson.optString("avatar"));
//
//        final Tag tag = new Tag(jObject.optInt("id"),
//                jObject.optInt("user_id"),
//                jObject.optString("name"),
//                jObject.optString("message"),
//                jObject.optString("shutdown_time"),
//                latStr,
//                lngStr,
//                address,
//                user);

        MarkerOptions markerOption = new MarkerOptions().position(new LatLng(lat, lng)).title(tag.getName()).snippet(tag.getDescription());
        // markerOption.icon(BitmapDescriptorFactory.defaultMarker(BitmapDescriptorFactory.HUE_ORANGE));
        Bitmap bmp = BitmapFactory.decodeResource(context.getResources(), R.drawable.marker);

        (new CustomMarker.AsyncImage(markerOption, map, mMarkersHashMap, bmp, tag.getUser().getAvatarUrl(), tag)).execute();

        return tag;
    }

    private JSONObject parseTagToJson(Tag tag) throws JSONException, IOException {

        String jsonString = LoganSquare.serialize(tag);
        JSONObject tagJson = new JSONObject(jsonString);

//        tagJson.put("name", tag.getName());
//        tagJson.put("user_id", tag.getUserId());
//        tagJson.put("lat", tag.getLat());
//        tagJson.put("lng", tag.getLng());
//        tagJson.put("message", tag.getDescription());
//        tagJson.put("shutdown_time", tag.getShutdownTime());

        return tagJson;
    }

    private JSONObject parseCommentToJson(Comment comment) throws JSONException, IOException {

        String jsonString = LoganSquare.serialize(comment);
        JSONObject commentJson = new JSONObject(jsonString);

        return commentJson;
    }

    private void showActionBar(ActionBar actBar) {
        actBar.show();
        actBar.setDisplayHomeAsUpEnabled(false);
        actBar.setDisplayShowHomeEnabled(false);
        actBar.setDisplayShowCustomEnabled(true);
        actBar.setDisplayShowTitleEnabled(false);
        View cView = ((Activity) context).getLayoutInflater().inflate(R.layout.custom_menu, null);
        ActionBar.LayoutParams layout = new ActionBar.LayoutParams(ActionBar.LayoutParams.MATCH_PARENT, ActionBar.LayoutParams.WRAP_CONTENT);

        actBar.setCustomView(cView, layout);
    }

    public class AsyncGetTagsForMap extends AsyncTask<String, Void, Boolean> {

        private List<Tag> tagsList;
        private JSONArray response;
        private GoogleMap map;
        private HashMap<Marker, Tag> mMarkersHashMap;
        private boolean isFinished;
        private ActionBar actBar;
        private ViewAnimator mapAnimator;

        public AsyncGetTagsForMap(List<Tag> tagsList, JSONArray response, GoogleMap map, HashMap<Marker, Tag> mMarkersHashMap, boolean isFinished, ActionBar actBar, ViewAnimator mapAnimator) {
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

    public class AsyncGetTagsForList extends AsyncTask<String, Void, Boolean> {

        private List<Tag> tagsList;
        private JSONArray response;
        private TagAdapter adapter;
        private ViewAnimator viewAnimator;

        public AsyncGetTagsForList(JSONArray response, TagAdapter adapter, ViewAnimator viewAnimator) {
            this.tagsList = new ArrayList<Tag>();
            this.response = response;
            this.adapter = adapter;
            this.viewAnimator = viewAnimator;
        }

        @Override
        protected void onPostExecute(Boolean result) {
            super.onPostExecute(result);

            adapter.setTags(tagsList);
            adapter.notifyDataSetChanged();

            viewAnimator.setDisplayedChild(1);
        }

        @Override
        protected void onPreExecute() {
            super.onPreExecute();
            tagsList.clear();
        }

        @Override
        protected Boolean doInBackground(String... params) {
            try {
                tagsList.addAll(getTagsForListFromJson(response));
                sortTagList(tagsList);
                isFinished = true;
            } catch (JSONException e) {
                e.printStackTrace();
            }

            return true;
        }

    }

    private List<Tag> sortTagList(List<Tag> tags) {
        final SimpleDateFormat df = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
        Date todayDate = new Date();
        Collections.sort(tags, new Comparator<Tag>() {
            public int compare(Tag t1, Tag t2) {
                Date date1 = null;
                Date date2 = null;
                try {
                    date1 = df.parse(t1.getShutdownTime());
                    date2 = df.parse(t2.getShutdownTime());
                } catch (ParseException e) {
                    e.printStackTrace();
                }
                return date2.compareTo(date1);
            }
        });
        ArrayList tmpList1 = new ArrayList<Tag>();
        ArrayList tmpList2 = new ArrayList<Tag>();
        for (Tag tag : tags) {
            Date tagDate = null;
            try {
                tagDate = df.parse(tag.getShutdownTime());
            } catch (ParseException e) {
                e.printStackTrace();
            }
            if (todayDate.before(tagDate)) {
                tmpList1.add(tag);
            }
            else {
                tmpList2.add(tag);
            }
        }
        Collections.sort(tmpList1, new Comparator<Tag>() {
            public int compare(Tag t1, Tag t2) {
                return t1.getDistance().compareTo(t2.getDistance());
            }
        });
        ArrayList tmpList3 = new ArrayList<Tag>();
        tmpList3.addAll(tmpList1);
        tmpList3.addAll(tmpList2);
        tags.clear();
        tags.addAll(tmpList3);

        return tags;
    }

    private List<Tag> getTagsForListFromJson(JSONArray jArray) throws JSONException {
        List<Tag> tags = new ArrayList<Tag>();
        tags.clear();

        for (int i = 0; i < jArray.length(); i++) {
            JSONObject jsonData = jArray.getJSONObject(i);
            try {
                Tag tag = getTagForList(jsonData);
                tags.add(tag);
            } catch (IOException e) {
                e.printStackTrace();
            }
            // Tag tag = getTag(jsonData, map, mMarkersHashMap);
        }
        return tags;
    }

    private Tag getTagForList(JSONObject jsonObject) throws IOException {
        AddressHelper helper = new AddressHelper(context);

        Tag tag = LoganSquare.parse(jsonObject.toString(), Tag.class);

        double lat = Double.parseDouble(tag.getLat());
        double lng = Double.parseDouble(tag.getLng());

        String address = helper.getAddress(lat, lng);
        tag.setAddress(address);

        return tag;
    }


}