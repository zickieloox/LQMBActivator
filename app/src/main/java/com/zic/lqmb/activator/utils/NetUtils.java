package com.zic.lqmb.activator.utils;

import android.util.Log;

import java.io.IOException;
import java.util.concurrent.TimeUnit;

import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.Response;

public class NetUtils {

    private static final String TAG = "NetUtils";

    public static String getHtml(String url) {
        OkHttpClient client = new OkHttpClient.Builder()
                .connectTimeout(15000, TimeUnit.MILLISECONDS)
                .retryOnConnectionFailure(true)
                .build();

        Request request = new Request.Builder()
                .url(url)
                .build();

        Response response;
        try {
            response = client.newCall(request).execute();
            if (response.isSuccessful()) {
                return response.body().string();
            } else {
                Log.e(TAG, "GetHtml: " + "Unexpected code " + response);
                Log.e(TAG, "GetHtml: " + "Response Body " + request.body().toString());
            }
        } catch (IOException e) {
            Log.e(TAG, "GetHtml: " + e.toString());
        }

        return null;
    }
}
