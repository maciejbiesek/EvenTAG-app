package com.example.maciej.eventag;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.util.ArrayList;
import java.util.List;

/**
 * Created by Maciej on 2015-05-27.
 */
public class NetworkTagsProvider {

    private final String TAGS_URL = "http://eventag.websource.com.pl/tags";
    private final String USERS_URL = "http://eventag.websource.com.pl/users/";

    private List<Tag> tags;

    public NetworkTagsProvider() { this.tags = new ArrayList<Tag>(); }

    public void getTagsFromServer() throws IOException, JSONException {

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

    public List<Tag> getAllTags() { return tags; }

    public Tag getLastTag() { return tags.get(tags.size() - 1); }
}
