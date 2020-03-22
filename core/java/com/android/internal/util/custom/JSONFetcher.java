package com.android.internal.util.custom;

import android.util.Log;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;

public class JSONFetcher {

    private static final String TAG = "ElixirUtils: JSONFetcher";

    public interface JsonCallback {
        void onSuccess(String jsonData);
        void onFailure(Exception e);
    }

    public static void fetchDataAsync(final String url, final JsonCallback callback) {
        new Thread(() -> {
            HttpURLConnection urlConnection = null;
            BufferedReader reader = null;
            try {
                URL apiUrl = new URL(url);
                urlConnection = (HttpURLConnection) apiUrl.openConnection();
                InputStream inputStream = urlConnection.getInputStream();
                StringBuilder builder = new StringBuilder();
                reader = new BufferedReader(new InputStreamReader(inputStream));
                String line;
                while ((line = reader.readLine()) != null) {
                    builder.append(line);
                }
                final String jsonData = builder.toString();
                callback.onSuccess(jsonData);
            } catch (IOException e) {
                callback.onFailure(e);
            } finally {
                if (urlConnection != null) {
                    urlConnection.disconnect();
                }
                if (reader != null) {
                    try {
                        reader.close();
                    } catch (IOException e) {
                        Log.e(TAG, "Error closing stream", e);
                    }
                }
            }
        }).start();
    }
}
