package com.purpura.googlemaps2018.asynctasks;

import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.os.AsyncTask;
import android.util.Log;

import com.google.android.gms.maps.model.BitmapDescriptorFactory;

import java.io.IOException;
import java.io.InputStream;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.URL;

public class UrlToBitmapTask extends AsyncTask<String, Void, Bitmap> {

    private static final String TAG = "UrlToBitmapTask";

    private Bitmap imageBitmap = null;

    @Override
    protected Bitmap doInBackground(String... urls) {
        Log.d(TAG, "doInBackground: ");

        String imageDownloadUrl = urls[0];

        try {
            imageBitmap = BitmapFactory.decodeStream(new URL(imageDownloadUrl).openConnection().getInputStream());
        } catch (IOException e) {
            e.printStackTrace();
        }


        /*Bitmap imageBitmap = null;
        try {
            imageBitmap = getBitmapFromUrl(imageDownloadUrl);
        } catch (IOException e) {
            e.printStackTrace();
        }
        return imageBitmap;*/

        return imageBitmap;
    }

    @Override
    protected void onPostExecute(Bitmap bmp) {
        super.onPostExecute(bmp);
        Log.d(TAG, "onPostExecute: ");

        if (imageBitmap != null) {

        }

    }

    private Bitmap getBitmapFromUrl(String urlString) throws IOException {
        Log.d(TAG, "getBitmapFromUrl: ");
        try {
            URL url = new URL(urlString);
            HttpURLConnection connection = (HttpURLConnection) url.openConnection();
            connection.setDoInput(true);
            connection.connect();
            InputStream input = connection.getInputStream();
            return BitmapFactory.decodeStream(input);
        } catch (IOException e) {
            // Log exception
            Log.e(TAG, "getBitmapFromUrl: failed to make bitmap from url", e);
            e.printStackTrace();
        }
        return null;
    }
}