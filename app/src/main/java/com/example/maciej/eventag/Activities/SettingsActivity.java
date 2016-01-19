package com.example.maciej.eventag.Activities;

import android.content.Intent;
import android.os.Bundle;
import android.app.Activity;
import android.view.View;
import android.widget.SeekBar;
import android.widget.TextView;

import com.example.maciej.eventag.R;
import com.facebook.login.LoginManager;

public class SettingsActivity extends BaseActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_settings);
        actBar = getSupportActionBar();
        showActionBar(actBar);

        SeekBar distanceBar = (SeekBar) findViewById(R.id.seekBar);
        final TextView distanceStatus = (TextView) findViewById(R.id.seekBar_status);
        distanceBar.setOnSeekBarChangeListener(new SeekBar.OnSeekBarChangeListener() {
            @Override
            public void onProgressChanged(SeekBar seekBar, int progress, boolean fromUser) {
                seekBar.setProgress(progress);
                distanceStatus.setText(progress + " km");
            }

            @Override
            public void onStartTrackingTouch(SeekBar seekBar) {

            }

            @Override
            public void onStopTrackingTouch(SeekBar seekBar) {

            }
        });
    }

    public void logout(View v) {
        System.out.println("Bang");
        LoginManager.getInstance().logOut();
        Intent i = new Intent(this, LoginActivity.class);
        i.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TOP);
        startActivity(i);
        finish();
    }

}
