package com.example.maciej.eventag.Adapters;

import android.content.Context;
import android.content.SharedPreferences;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Canvas;
import android.graphics.Paint;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.ImageView;

import com.example.maciej.eventag.Helpers.NetworkProvider;
import com.example.maciej.eventag.Models.User;
import com.example.maciej.eventag.R;
import com.squareup.picasso.Picasso;

import java.io.IOException;
import java.io.InputStream;
import java.net.HttpURLConnection;
import java.util.ArrayList;
import java.util.List;

public class ImageAdapter extends BaseAdapter {

    private Context context;
    private List<User> users = new ArrayList<User>();
    private int myId;

    public ImageAdapter(Context context, int myId) {
        this.context = context;
        this.myId = myId;
    }

    public void addUsers(List<User> users) {
        this.users.clear();
        this.users.addAll(users);
    }

    @Override
    public int getCount() {
        return users.size() + 1;
    }

    public int getLast() { return users.size(); }

    @Override
    public User getItem(int position) {
        return users.get(position);
    }

    @Override
    public long getItemId(int position) {
        return position;
    }

    @Override
    public View getView(int position, View convertView, ViewGroup parent) {
        View imageView;

        if (convertView == null) {
            imageView = LayoutInflater.from(context).inflate(R.layout.tag_attender, parent, false);
        } else {
            imageView = convertView;
        }

        bindImageToView(position, imageView);

        return imageView;
    }

    private String getAvatarUrl() {
        NetworkProvider networkProvider = new NetworkProvider(context);
        networkProvider.getUserId();
        SharedPreferences prefs = context.getSharedPreferences(KEYS, MODE_PRIVATE);

        networkProvider.getUser(myId);
        String userAvatar = prefs.getString(USER_AVATAR, null);

        return userAvatar;
    }

    private void bindImageToView(int position, View imageView) {
        ImageView image = (ImageView) imageView.findViewById(R.id.attender);
        User user = getItem(position);

        if (position == users.size()) {
            Bitmap bmp1 = BitmapFactory.decodeResource(context.getResources(), R.drawable.join);
            Bitmap bmp2 = getImageFromUrl(getAvatarUrl());
            Bitmap finalImage = processingBitmaps(bmp1, bmp2);
            image.setImageBitmap(finalImage);
        }
        else {
            Picasso.with(context).load(user.getAvatarUrl()).into(image);
        }
    }

    private Bitmap getImageFromUrl(String urlPath) {
        try {
            java.net.URL url = new java.net.URL(urlPath);
            HttpURLConnection connection = (HttpURLConnection) url.openConnection();
            connection.setDoInput(true);
            connection.connect();
            Bitmap bitmap = null;
            if (connection.getResponseCode() == HttpURLConnection.HTTP_OK) {
                InputStream input = connection.getInputStream();
                bitmap = BitmapFactory.decodeStream(input);
            }
            return bitmap;
        } catch (IOException e) {
            e.printStackTrace();
            return null;
        }
    }

    private Bitmap processingBitmaps(Bitmap b1, Bitmap b2) {
        Bitmap newBitmap = null;
        Bitmap.Config config = b2.getConfig();
        if(config == null){
            config = Bitmap.Config.ARGB_8888;
        }

        newBitmap = Bitmap.createBitmap(b2.getWidth(), b2.getHeight(), config);
        Canvas newCanvas = new Canvas(newBitmap);

        newCanvas.drawBitmap(b1, 0, 0, null);

        Paint paint = new Paint();
        paint.setAlpha(128);
        newCanvas.drawBitmap(b2, 0, 0, paint);

        return newBitmap;
    }
}