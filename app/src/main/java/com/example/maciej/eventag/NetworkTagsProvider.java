package com.example.maciej.eventag;

import android.content.Context;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.widget.Toast;

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
import java.util.Collection;
import java.util.List;

/**
 * Created by Maciej on 2015-05-19.
 */
public class NetworkTagsProvider {

    private final Context context;
    private  final List<Tag> tags = new ArrayList<>();
    private final String TAGS_URL = "http://eventag.websource.com.pl/tags";
    private final String USERS_URL = "http://eventag.websource.com.pl/users/";

    public interface OnTagsDownloadedListener {
        void onTagsDownloaded();
    }

    public NetworkTagsProvider(Context context) {
        this.context = context;
    }

    public void getTags(OnTagsDownloadedListener listener) throws IOException, JSONException {
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

        listener.onTagsDownloaded();
    }

    public int getTagsNumber() {
        return tags.size();
    }

    public List<Tag> getAllTasks() {
        return tags;
    }

    private String downloadFromUrl(String URL) throws IOException {
        InputStream is = null;

        try {
            java.net.URL url = new URL(URL);
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
