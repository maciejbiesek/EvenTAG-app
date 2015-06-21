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
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.RadioButton;
import android.widget.RadioGroup;
import android.widget.Spinner;
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


public class AddTagActivity extends ActionBarActivity implements AdapterView.OnItemSelectedListener {

    private final String TAGS_URL = "http://eventag.websource.com.pl/tags";
    private Spinner spinner;
    private static final String[] shutdown = {"15 minut", "30 minut", "1 godzina", "2 godziny"};
    private String latitude;
    private String longitude;
    private int which;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.add_tag);

        Intent i = getIntent();
        latitude = i.getStringExtra("lat");
        longitude = i.getStringExtra("lng");

        spinner = (Spinner)findViewById(R.id.spinner);
        ArrayAdapter<String> adapter = new ArrayAdapter<String>(AddTagActivity.this,
                android.R.layout.simple_spinner_item, shutdown);
        adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        spinner.setAdapter(adapter);
        spinner.setSelection(0);
        spinner.setOnItemSelectedListener(this);
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

        int userId = 1;
        Tag tag = new Tag(userId, name, description, shutdownTime, latitude, longitude);

        if (!name.isEmpty()) {
            if (isOnline()) {
                (new AsyncNetworkTagsProvider()).execute(createJSON(tag));
                finish();
            }
            else {
                Toast.makeText(AddTagActivity.this, "Brak dostępu do sieci", Toast.LENGTH_SHORT).show();
            }
        }
        else Toast.makeText(AddTagActivity.this, "Najpierw podaj tytuł!", Toast.LENGTH_SHORT).show();
    }

    public void onItemSelected(AdapterView<?> parent, View v, int position, long id) {
        which = position;
    }

    @Override
    public void onNothingSelected(AdapterView<?> parent) {

    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {

        MenuInflater inflater = getMenuInflater();
        inflater.inflate(R.menu.menu_add_tag, menu);

        return super.onCreateOptionsMenu(menu);
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {

        switch (item.getItemId()) {
            case R.id.add: {
                addTag();
                finish();
                return true;
            }

            default: {
                return super.onOptionsItemSelected(item);
            }
        }
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
            int code = conn.getResponseCode();

            Toast.makeText(AddTagActivity.this, "Dodano nowe zdarzenie", Toast.LENGTH_SHORT).show();
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

            if (result != null && result != "ERROR") {
                try {
                    JSONObject jsonTag = new JSONObject(result);
                    Tag tag = new Tag(jsonTag.getInt("id"), jsonTag.getString("name"), jsonTag.getString("message"),
                            jsonTag.getString("shutdown_time"), jsonTag.getString("lat"), jsonTag.getString("lng"),
                            new User(1, "Mrs. Ena Medhurst III", "Lee", "Spencer", "female", "images/tdayeycfgayvnkmkhsz"));
                    MapActivity.tagList.add(tag);
                } catch (JSONException e) {
                    e.printStackTrace();
                }
            }
            else Toast.makeText(AddTagActivity.this, "Serwer zwrócił niepoprawne dane", Toast.LENGTH_SHORT).show();
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

