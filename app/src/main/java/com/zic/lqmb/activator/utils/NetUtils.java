package com.zic.lqmb.activator.utils;

import android.util.Log;

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.net.URL;
import java.net.URLConnection;

public class NetUtils {

    private static final String TAG = "NetUtils";

    public static String readTextFromUrl(String theUrl) {
        StringBuilder content = new StringBuilder();
        try {
            // Create a url object
            URL url = new URL(theUrl);

            // Create a UrlConnection object
            URLConnection urlConnection = url.openConnection();

            // Wrap the UrlConnection in a BufferedReader
            BufferedReader bufferedReader = new BufferedReader(new InputStreamReader(urlConnection.getInputStream()));

            String line;

            // Read HTML from the UrlConnection via the BufferedReader
            while ((line = bufferedReader.readLine()) != null) {
                content.append(line + "\n");
            }
            bufferedReader.close();
        } catch (Exception e) {
            Log.e(TAG, e.toString());
        }
        return content.toString();
    }
}
