package com.example.maciej.eventag;

import android.app.Service;
import android.content.Intent;
import android.os.Build;
import android.os.IBinder;
import android.util.Log;
import android.widget.Toast;

import org.json.JSONException;

import java.io.IOException;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.Date;
import java.util.List;
import java.util.Timer;
import java.util.TimerTask;


public class DownloadTagService extends Service {
    private static final String TAG = "MyService";
    private NetworkTagsProvider networkTagsProvider;
    public static List<Tag> tags = new ArrayList<Tag>();

    @Override
    public IBinder onBind(Intent intent) {
        return null;
    }

    @Override
    public void onCreate() {
        super.onCreate();
    }

    @SuppressWarnings("deprecation")
    @Override
    public void onStart(Intent intent, int startid) {

        TimerTask serviceTask = new TimerTask() {
            @Override
            public void run() {
                networkTagsProvider = new NetworkTagsProvider();
                Log.e(TAG, "Service is running!");
                try {
                    networkTagsProvider.getTagsFromServer();
                } catch (IOException e) {
                    e.printStackTrace();
                } catch (JSONException e) {
                    e.printStackTrace();
                }
                tags.clear();
                tags.addAll(networkTagsProvider.getAllTags());

                Log.e("log", "Pobrano z serwera");
                Log.e("ile", "" + tags.size());
            }
        };

        Timer serviceTimer = new Timer();
        long delay = 0;
        long intevalPeriod = 60 * 1000;
        serviceTimer.scheduleAtFixedRate(serviceTask, delay, intevalPeriod);
    }
}