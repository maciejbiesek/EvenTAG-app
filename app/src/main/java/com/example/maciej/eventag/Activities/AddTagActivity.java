package com.example.maciej.eventag.Activities;

import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
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
import com.example.maciej.eventag.Helpers.NetworkProvider;
import com.example.maciej.eventag.R;
import com.example.maciej.eventag.models.Tag;

import org.json.JSONException;

import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.text.SimpleDateFormat;
import java.util.Calendar;

import static com.example.maciej.eventag.models.Constants.*;


public class AddTagActivity extends ActionBarActivity implements AdapterView.OnItemSelectedListener {

    private Spinner spinner;
    private static String[] shutdown;
    private String latitude;
    private String longitude;
    private int which;
    private CommunicationHelper comHelper;
    private NetworkProvider networkProvider;

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
        networkProvider = new NetworkProvider(this);
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

        SharedPreferences prefs = getSharedPreferences(KEYS, MODE_PRIVATE);
        int userId = prefs.getInt(USER_ID, 0);
        Tag tag = new Tag(0, userId, name, description, shutdownTime, latitude, longitude);

        if (!name.isEmpty()) {
            if (isOnline()) {
                try {
                    networkProvider.sendTag(tag);
                    setResult(1);
                } catch (UnsupportedEncodingException e) {
                    e.printStackTrace();
                } catch (JSONException e) {
                    e.printStackTrace();
                } catch (IOException e) {
                    e.printStackTrace();
                }
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

    public boolean isOnline() {
        ConnectivityManager connMgr = (ConnectivityManager)
                this.getSystemService(Context.CONNECTIVITY_SERVICE);
        NetworkInfo networkInfo = connMgr.getActiveNetworkInfo();
        return (networkInfo != null && networkInfo.isConnected());
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

