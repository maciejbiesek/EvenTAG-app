package com.example.maciej.eventag.Models;


import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Canvas;
import android.graphics.Paint;
import android.graphics.PorterDuff;
import android.graphics.PorterDuffXfermode;
import android.graphics.Rect;
import android.os.AsyncTask;

import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.model.BitmapDescriptorFactory;
import com.google.android.gms.maps.model.Marker;
import com.google.android.gms.maps.model.MarkerOptions;

import java.io.IOException;
import java.io.InputStream;
import java.net.HttpURLConnection;
import java.util.HashMap;

public class CustomMarker {

    private static Bitmap getImageFromUrl(String urlPath) {
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

    private static Bitmap getCroppedBitmap(Bitmap bitmap) {
        Bitmap output = Bitmap.createBitmap(bitmap.getWidth(),
                bitmap.getHeight(), Bitmap.Config.ARGB_8888);
        Canvas canvas = new Canvas(output);

        final int color = 0xff424242;
        final Paint paint = new Paint();
        final Rect rect = new Rect(0, 0, bitmap.getWidth(), bitmap.getHeight());

        paint.setAntiAlias(true);
        canvas.drawARGB(0, 0, 0, 0);
        paint.setColor(color);
        // canvas.drawRoundRect(rectF, roundPx, roundPx, paint);
        canvas.drawCircle(bitmap.getWidth() / 2, bitmap.getHeight() / 2,
                bitmap.getWidth() / 2, paint);
        paint.setXfermode(new PorterDuffXfermode(PorterDuff.Mode.SRC_IN));
        canvas.drawBitmap(bitmap, rect, rect, paint);
        //Bitmap _bmp = Bitmap.createScaledBitmap(output, 60, 60, false);
        //return _bmp;
        return output;
    }

    private static Bitmap processingBitmaps(Bitmap b1, Bitmap b2) {
        Bitmap newBitmap = null;
        Bitmap.Config config = b1.getConfig();
        if (config == null){
            config = Bitmap.Config.ARGB_8888;
        }

        Bitmap b1Scaled = Bitmap.createScaledBitmap(b1, 120, 120, false);
        Bitmap b2Scaled = Bitmap.createScaledBitmap(b2, b1Scaled.getWidth() -40, b1Scaled.getHeight() -40, false);

        newBitmap = Bitmap.createBitmap(b1Scaled.getWidth(), b1Scaled.getHeight(), config);
        Canvas newCanvas = new Canvas(newBitmap);

        newCanvas.drawBitmap(b1Scaled,
                (newCanvas.getWidth() - b1Scaled.getWidth())/2,
                (newCanvas.getHeight() - b1Scaled.getHeight())/2,
                null
        );

        Paint paint = new Paint();
        //newCanvas.drawBitmap(b2Scaled, newCanvas.getHeight()/2, newCanvas.getWidth()/2, paint);

        newCanvas.drawBitmap(b2Scaled,
                (newCanvas.getWidth() - b2Scaled.getWidth())/2-1,
                (newCanvas.getHeight() - b2Scaled.getHeight())/2-8,
                paint
        );

        return newBitmap;
    }

    public static class AsyncImage extends AsyncTask<String, Void, Bitmap> {

        private MarkerOptions markerOptions;
        private GoogleMap map;
        private HashMap<Marker, Tag> mMarkersHashMap;
        private Tag tag;
        private String avatarUrl;
        private Bitmap bmp1;

        public AsyncImage(MarkerOptions markerOptions, GoogleMap map, HashMap<Marker, Tag> mMarkersHashMap, Bitmap bmp, String avatarUrl, Tag tag) {
            this.markerOptions = markerOptions;
            this.map = map;
            this.mMarkersHashMap = mMarkersHashMap;
            this.avatarUrl = avatarUrl;
            this.tag = tag;
            this.bmp1 = bmp;
        }

        @Override
        protected void onPostExecute(Bitmap result) {
            super.onPostExecute(result);
            Bitmap avatar = getCroppedBitmap(result);
            Bitmap finalImage = processingBitmaps(bmp1, avatar);

            this.markerOptions.icon(BitmapDescriptorFactory.fromBitmap(finalImage));
            Marker currentMarker = map.addMarker(this.markerOptions);
            mMarkersHashMap.put(currentMarker, tag);

        }

        @Override
        protected void onPreExecute() {
            super.onPreExecute();
        }

        @Override
        protected Bitmap doInBackground(String... params) {
            return getImageFromUrl(this.avatarUrl);
        }
    }
}
