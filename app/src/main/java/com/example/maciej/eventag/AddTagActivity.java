package com.example.maciej.eventag;

import android.content.Context;
import android.content.Intent;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.os.AsyncTask;
import android.support.v7.app.ActionBarActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.RadioButton;
import android.widget.RadioGroup;
import android.widget.Toast;
import android.widget.ViewAnimator;

import org.json.JSONException;
import org.json.JSONObject;
import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.io.OutputStreamWriter;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.ProtocolException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.List;


public class AddTagActivity extends ActionBarActivity {

    private final String TAGS_URL = "http://eventag.websource.com.pl/tags";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.add_tag);

        final GPSTracker gpsTracker = new GPSTracker(this);

        final EditText nameEditText = (EditText) findViewById(R.id.new_tag_name);
        final EditText descriptionEditText = (EditText) findViewById(R.id.new_tag_description);
        final RadioGroup radioGroup = (RadioGroup) findViewById(R.id.radioList);

        Button addTag = (Button) findViewById(R.id.new_tag_save);
        addTag.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                String name = nameEditText.getText().toString();
                String description = descriptionEditText.getText().toString();

                String shutdownTime = null;

                int selectedId = radioGroup.getCheckedRadioButtonId();
                final RadioButton checkedRatio = (RadioButton) findViewById(selectedId);
                int which = radioGroup.indexOfChild(checkedRatio);

                switch (which) {
                    case 1:
                        shutdownTime = addTime(15);
                        break;
                    case 2:
                        shutdownTime = addTime(30);
                        break;
                    case 3:
                        shutdownTime = addTime(60);
                        break;
                    case 4:
                        shutdownTime = addTime(120);
                        break;
                }

                String latitude = "";
                String longitude = "";
                if (gpsTracker.getIsGPSTrackingEnabled()) {
                    latitude = String.valueOf(gpsTracker.latitude);
                    longitude = String.valueOf(gpsTracker.longitude);
                }
                else
                {
                    // can't get location
                    // GPS or Network is not enabled
                    // Ask user to enable GPS/network in settings
                    gpsTracker.showSettingsAlert();
                }

                int userId = 1;
                Tag tag = new Tag(userId, name, description, shutdownTime, latitude, longitude);

                if (!name.isEmpty()) {
                    if (isOnline()) {
                        (new AsyncNetworkTagsProvider()).execute(createJSON(tag));
                        Toast.makeText(AddTagActivity.this, "Dodano nowe zdarzenie", Toast.LENGTH_LONG).show();
                        finish();
                    }
                    else {
                        Toast.makeText(AddTagActivity.this, "Brak dostępu do sieci", Toast.LENGTH_LONG).show();
                    }
                }
                else Toast.makeText(AddTagActivity.this, "Najpierw podaj tytuł!", Toast.LENGTH_SHORT).show();
            }
        });
    }

    public String addTime(int minutes) {
        Calendar date = Calendar.getInstance();
        Calendar finalDate = date;
        SimpleDateFormat df = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
        finalDate.add(date.MINUTE, minutes);
        return df.format(finalDate.getTime());
    }

    public String sendToServer(String jsonObject) throws IOException {
        InputStream is = null;

        try {
            java.net.URL url = new java.net.URL(TAGS_URL);
            HttpURLConnection conn = (HttpURLConnection) url.openConnection();
            conn.setDoOutput(true);
            conn.setRequestMethod("POST");
            conn.setRequestProperty("Content-Type", "application/json");
            OutputStreamWriter os = new OutputStreamWriter(conn.getOutputStream());
            os.write(jsonObject);
            os.flush();
            is = conn.getInputStream();

            return readStream(is);
        }
        catch (Exception e) {
            e.printStackTrace();
        }
        finally {
            if (is != null) {
                is.close();
            }
        }
        return jsonObject;
    }

    public String createJSON(Tag tag) {
        JSONObject jObject = new JSONObject();
        try {
            jObject.put("id", tag.getId());
            jObject.put("name", tag.getName());
            jObject.put("user_id", tag.getUserId());
            jObject.put("lat", tag.getLat());
            jObject.put("lng", tag.getLng());
            jObject.put("message", tag.getDescription());
            jObject.put("shutdown_time", tag.getShutdownTime());
        } catch (JSONException e) {
            e.printStackTrace();
        }
        return jObject.toString();
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

    public boolean isOnline() {
        ConnectivityManager connMgr = (ConnectivityManager)
                this.getSystemService(Context.CONNECTIVITY_SERVICE);
        NetworkInfo networkInfo = connMgr.getActiveNetworkInfo();
        return (networkInfo != null && networkInfo.isConnected());
    }


    private class AsyncNetworkTagsProvider extends AsyncTask<String, Void, String> {

        @Override
        protected void onPostExecute(String result) {
            super.onPostExecute(result);
        }

        @Override
        protected void onPreExecute() {
            super.onPreExecute();
        }

        @Override
        protected String doInBackground(String... params) {
            try {
                return sendToServer(params[0]);
            } catch (IOException e) {
                e.printStackTrace();
            }
            return null;
        }

    }
}

