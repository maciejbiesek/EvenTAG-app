package com.example.maciej.eventag;

import android.app.Service;
import android.content.Intent;
import android.os.Build;
import android.os.IBinder;
import android.util.Log;
import android.widget.Toast;

import org.json.JSONException;

import java.io.IOException;
import java.util.ArrayList;
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
        networkTagsProvider = new NetworkTagsProvider();
    }

    @SuppressWarnings("deprecation")
    @Override
    public void onStart(Intent intent, int startid) {

        TimerTask serviceTask = new TimerTask() {
            @Override
            public void run() {
                Log.e(TAG, "Service is running!");
                try {
                    networkTagsProvider.getTagsFromServer();
                } catch (IOException e) {
                    e.printStackTrace();
                } catch (JSONException e) {
                    e.printStackTrace();
                }
                if (!tags.isEmpty()) {
                    tags.clear();
                }
                tags.addAll(networkTagsProvider.getAllTags());
                Log.e("log", "Pobrano z serwera");
            }
        };

        Timer serviceTimer = new Timer();
        long delay = 0;
        long intevalPeriod = 60 * 1000;
        serviceTimer.scheduleAtFixedRate(serviceTask, delay, intevalPeriod);
    }
}
