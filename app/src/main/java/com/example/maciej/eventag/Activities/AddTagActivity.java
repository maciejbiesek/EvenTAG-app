package com.example.maciej.eventag.Activities;

import android.content.Context;
import android.content.Intent;
import android.location.Address;
import android.location.Geocoder;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.os.AsyncTask;
import android.support.v7.app.ActionBar;
import android.support.v7.app.ActionBarActivity;
import android.os.Bundle;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.EditText;
import android.widget.Spinner;
import android.widget.Toast;

import com.example.maciej.eventag.Helpers.CommunicationHelper;
import com.example.maciej.eventag.R;
import com.example.maciej.eventag.Models.Tag;
import com.example.maciej.eventag.Models.User;

import org.json.JSONException;
import org.json.JSONObject;
import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStreamWriter;
import java.net.HttpURLConnection;
import java.net.URL;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.List;
import java.util.Locale;

import static com.example.maciej.eventag.Models.Constants.*;


public class AddTagActivity extends ActionBarActivity implements AdapterView.OnItemSelectedListener {

    private Spinner spinner;
    private static String[] shutdown;
    private String latitude;
    private String longitude;
    private int which;
    private CommunicationHelper comHelper;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.add_tag);
        showActionBar();

        Intent i = getIntent();
        latitude = i.getStringExtra(LAT);
        longitude = i.getStringExtra(LNG);

        shutdown = new String[] {
                getString(R.string.shutdown_option_1),
                getString(R.string.shutdown_option_2),
                getString(R.string.shutdown_option_3),
                getString(R.string.shutdown_option_4)
        };

        spinner = (Spinner)findViewById(R.id.spinner);
        ArrayAdapter<String> adapter = new ArrayAdapter<String>(AddTagActivity.this,
                R.layout.spinner_item, shutdown);
        adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        spinner.setAdapter(adapter);
        spinner.setSelection(0);
        spinner.setOnItemSelectedListener(this);

        comHelper = new CommunicationHelper(this);
    }

    private void addTag() {
        EditText nameEditText = (EditText) findViewById(R.id.new_tag_name);
        EditText descriptionEditText = (EditText) findViewById(R.id.new_tag_description);

        String name = nameEditText.getText().toString();
        String description = descriptionEditText.getText().toString();

        String shutdownTime = null;

        switch (which) {
            case 0:
                shutdownTime = addTime(15);
                break;
            case 1:
                shutdownTime = addTime(30);
                break;
            case 2:
                shutdownTime = addTime(60);
                break;
            case 3:
                shutdownTime = addTime(120);
                break;
        }

        int userId = 5;
        Tag tag = new Tag(userId, name, description, shutdownTime, latitude, longitude);

        if (!name.isEmpty()) {
            if (isOnline()) {
                (new AsyncNetworkTagsProvider()).execute(createJSON(tag));
                finish();
            }
            else {
                comHelper.showUserDialog("", getString(R.string.internet_not_found));
            }
        }
        else comHelper.showUserDialog("", getString(R.string.title_first));
    }

    public void onItemSelected(AdapterView<?> parent, View v, int position, long id) {
        which = position;
    }

    @Override
    public void onNothingSelected(AdapterView<?> parent) {

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
            URL url = new URL(TAGS_URL);
            HttpURLConnection conn = (HttpURLConnection) url.openConnection();
            conn.setDoOutput(true);
            conn.setRequestMethod("POST");
            conn.setRequestProperty("Content-Type", "application/json");
            OutputStreamWriter os = new OutputStreamWriter(conn.getOutputStream());
            os.write(jsonObject);
            os.flush();
            is = conn.getInputStream();

            runOnUiThread(new Runnable() {
                public void run() {
                    comHelper.showUserDialog("", getString(R.string.add_new_tag));
                }
            });
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
        return "ERROR";
    }

    public String createJSON(Tag tag) {
        JSONObject jObject = new JSONObject();
        try {
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

    private void getAdress(Tag tag) {
        Geocoder geocoder;
        List<Address> addresses;
        geocoder = new Geocoder(this, Locale.getDefault());

        double latitude = Double.parseDouble(tag.getLat());
        double longitude = Double.parseDouble(tag.getLng());

        if (latitude < 60 && longitude < 60) {
            addresses = null;
            try {
                addresses = geocoder.getFromLocation(latitude, longitude, 1);
            } catch (IOException e) {
                e.printStackTrace();
            }
            if (!addresses.isEmpty()) {
                String address = addresses.get(0).getAddressLine(0);
                String city = addresses.get(0).getLocality();
                tag.setAddress(address);
            }
            else tag.setAddress(latitude + ", " + longitude);
        } else tag.setAddress(getString(R.string.coords_fail));
    }


    private class AsyncNetworkTagsProvider extends AsyncTask<String, Void, String> {

        @Override
        protected void onPostExecute(String result) {
            super.onPostExecute(result);

            if (result != null && result != "ERROR") {
                try {
                    JSONObject jsonTag = new JSONObject(result);
                    Tag tag = new Tag(jsonTag.getInt("id"), jsonTag.getString("name"), jsonTag.getString("message"),
                            jsonTag.getString("shutdown_time"), jsonTag.getString("lat"), jsonTag.getString("lng"),
                            new User(5, "Ernestina Gerhold", "Maciej", "Biesek", "", "images/maciej.jpg"));
                    getAdress(tag);
                    MapActivity.tagList.add(0, tag);
                } catch (JSONException e) {
                    e.printStackTrace();
                }
            }
            else comHelper.showUserDialog("", getString(R.string.server_fail));
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



    // MENU

    private void showActionBar() {
        ActionBar actionBar = getSupportActionBar();
        actionBar.setDisplayHomeAsUpEnabled(false);
        actionBar.setDisplayShowHomeEnabled(false);
        actionBar.setDisplayShowCustomEnabled(true);
        actionBar.setDisplayShowTitleEnabled(false);
        View cView = getLayoutInflater().inflate(R.layout.custom_add_tag_menu, null);
        ActionBar.LayoutParams layout = new ActionBar.LayoutParams(ActionBar.LayoutParams.MATCH_PARENT, ActionBar.LayoutParams.WRAP_CONTENT);

        actionBar.setCustomView(cView, layout);
    }

    public void clickEvent(View v) {
        switch (v.getId()) {
            case R.id.back: {
                finish();
                break;
            }
            case R.id.logo: {
                Toast.makeText(this, getString(R.string.app_name), Toast.LENGTH_SHORT).show();
                break;
            }
            case R.id.yes: {
                addTag();
            }
        }
    }
}

