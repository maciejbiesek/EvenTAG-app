package com.example.maciej.eventag.Activities;

import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.location.Address;
import android.location.Geocoder;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.os.AsyncTask;
import android.support.v7.app.ActionBar;
import android.support.v7.app.ActionBarActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.EditText;
import android.widget.Spinner;
import android.widget.Toast;

import com.example.maciej.eventag.Helpers.CommunicationHelper;
import com.example.maciej.eventag.Helpers.NetworkProvider;
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
import java.io.UnsupportedEncodingException;
import java.net.HttpURLConnection;
import java.net.URL;
import java.text.DateFormat;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;
import java.util.List;
import java.util.Locale;

import static com.example.maciej.eventag.Models.Constants.*;


public class EditTagActivity extends ActionBarActivity implements AdapterView.OnItemSelectedListener {

    private Spinner spinner;
    private static String[] shutdown;
    private Tag tag;
    private int which;
    private CommunicationHelper comHelper;
    private NetworkProvider networkProvider;

    private EditText nameEditText;
    private EditText descriptionEditText;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.add_tag);
        showActionBar();

        Intent i = getIntent();
        tag = (Tag) i.getExtras().getSerializable(TAG_KEY);

        nameEditText = (EditText) findViewById(R.id.new_tag_name);
        descriptionEditText = (EditText) findViewById(R.id.new_tag_description);

        shutdown = new String[] {
                getString(R.string.shutdown_option_1),
                getString(R.string.shutdown_option_2),
                getString(R.string.shutdown_option_3),
                getString(R.string.shutdown_option_4)
        };

        spinner = (Spinner)findViewById(R.id.spinner);
        ArrayAdapter<String> adapter = new ArrayAdapter<String>(EditTagActivity.this,
                R.layout.spinner_item, shutdown);
        adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        spinner.setAdapter(adapter);
        spinner.setSelection(getShutdownOption());
        spinner.setOnItemSelectedListener(this);

        nameEditText.setText(tag.getName());
        nameEditText.setSelection(tag.getName().length());
        descriptionEditText.setText(tag.getDescription());
        descriptionEditText.setSelection(tag.getDescription().length());

        comHelper = new CommunicationHelper(this);
        networkProvider = new NetworkProvider(this);
    }

    private void addTag() {
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

        SharedPreferences prefs = getSharedPreferences(KEYS, MODE_PRIVATE);
        int userId = prefs.getInt(USER_ID, 0);
        Tag tagEdited = new Tag(tag.getId(), userId, name, description, shutdownTime, tag.getLat(), tag.getLng());

        if (!name.isEmpty()) {
            if (isOnline()) {
                try {
                    networkProvider.editTag(tagEdited);
                    setResult(1);
                    finish();
                } catch (UnsupportedEncodingException e) {
                    e.printStackTrace();
                } catch (JSONException e) {
                    e.printStackTrace();
                }
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

    public boolean isOnline() {
        ConnectivityManager connMgr = (ConnectivityManager)
                this.getSystemService(Context.CONNECTIVITY_SERVICE);
        NetworkInfo networkInfo = connMgr.getActiveNetworkInfo();
        return (networkInfo != null && networkInfo.isConnected());
    }

    public int getShutdownOption() {
        try {
            SimpleDateFormat df = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");

            Date now = new Date();
            Date tagShutdown = df.parse(tag.getShutdownTime());
            long diff = tagShutdown.getTime() - now.getTime();
            long minutes = diff / (1000 * 60);
            if (minutes <= 15) {
                return 0;
            } else if (minutes > 15 && minutes <= 30) {
                return 1;
            } else if (minutes > 30 && minutes <= 60) {
                return 2;
            } else return 3;
        }
        catch (ParseException e) {
            return 0;
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

