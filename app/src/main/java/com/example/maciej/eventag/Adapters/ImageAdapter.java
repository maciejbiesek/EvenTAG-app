package com.example.maciej.eventag.Adapters;

import android.content.Context;
import android.content.SharedPreferences;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Canvas;
import android.graphics.Paint;
import android.os.AsyncTask;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.ImageView;
import android.widget.TextView;

import com.example.maciej.eventag.Helpers.NetworkProvider;
import com.example.maciej.eventag.Models.User;
import com.example.maciej.eventag.R;
import com.squareup.picasso.Picasso;

import java.io.IOException;
import java.io.InputStream;
import java.net.HttpURLConnection;
import java.util.ArrayList;
import java.util.List;

import static com.example.maciej.eventag.Models.Constants.*;

public class ImageAdapter extends BaseAdapter {

    private Context context;
    private List<User> users = new ArrayList<User>();
    private int myId;
    private int count;

    public ImageAdapter(Context context, int myId) {
        this.context = context;
        this.myId = myId;
        this.count = 0;
    }

    public void addUsers(List<User> users) {
        this.users.clear();
        this.users.addAll(users);
        this.count = checkIfOnList(users) ? 0 : 1;
    }

    @Override
    public int getCount() {
        return users.size() + count;
    }

    public void plusCount() {
        this.count = 1;
    }

    public void minusCount() {
        this.count = 0;
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

    private boolean checkIfOnList(List<User> users) {
        for (User user : users) {
            if (user.getId() == myId) {
                return true;
            }
        }
        return false;
    }

    private String getAvatarUrl() {
        NetworkProvider networkProvider = new NetworkProvider(context);
        networkProvider.getUserId();
        SharedPreferences prefs = context.getSharedPreferences(KEYS, Context.MODE_PRIVATE);

        networkProvider.getUser(myId);
        String userAvatar = prefs.getString(USER_AVATAR, null);

        return userAvatar;
    }

    private void bindImageToView(int position, View imageView) {
        ImageView image = (ImageView) imageView.findViewById(R.id.attender);
        TextView label = (TextView) imageView.findViewById(R.id.attender_label);
        label.setVisibility(View.INVISIBLE);

        if (position == users.size()) {
            Bitmap bmp = BitmapFactory.decodeResource(context.getResources(), R.drawable.join);
            (new AsyncImage(image, bmp)).execute();
        }
        else {
            User user = getItem(position);
            label.setText(user.getName());

            if (position != 0 && user.getId() == myId) {
                Bitmap bmp = BitmapFactory.decodeResource(context.getResources(), R.drawable.resign);
                (new AsyncImage(image, bmp)).execute();
            }
            else {
                Picasso.with(context).load(user.getAvatarUrl()).into(image);
            }
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
        Bitmap.Config config = b1.getConfig();
        if (config == null){
            config = Bitmap.Config.ARGB_8888;
        }

        Bitmap b2Scaled = Bitmap.createScaledBitmap(b2, b1.getWidth(), b1.getHeight(), false);

        newBitmap = Bitmap.createBitmap(b1.getWidth(), b1.getHeight(), config);
        Canvas newCanvas = new Canvas(newBitmap);

        newCanvas.drawBitmap(b1, 0, 0, null);

        Paint paint = new Paint();
        paint.setAlpha(128);
        newCanvas.drawBitmap(b2Scaled, 0, 0, paint);

        return newBitmap;
    }


    private class AsyncImage extends AsyncTask<String, Void, Bitmap> {

        private ImageView image;
        private String avatarUrl;
        private Bitmap bmp1;

        public AsyncImage(ImageView imageView, Bitmap bmp) {
            this.image = imageView;
            this.bmp1 = bmp;
        }

        @Override
        protected void onPostExecute(Bitmap result) {
            super.onPostExecute(result);

            Bitmap finalImage = processingBitmaps(bmp1, result);
            image.setImageBitmap(finalImage);

        }

        @Override
        protected void onPreExecute() {
            super.onPreExecute();
            avatarUrl = getAvatarUrl();
        }

        @Override
        protected Bitmap doInBackground(String... params) {
            return getImageFromUrl(avatarUrl);
        }
    }
}