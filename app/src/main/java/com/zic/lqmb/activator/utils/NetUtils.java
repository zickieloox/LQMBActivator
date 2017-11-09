package com.zic.lqmb.activator.utils;

import android.util.Log;

import java.io.IOException;
import java.util.concurrent.TimeUnit;

import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.Response;

public class NetUtils {

    private static final String TAG = "NetUtils";
    private static final int TIMEOUT_MIN = 2;

    public static String getHtml(String url) {
        OkHttpClient client = new OkHttpClient.Builder()
                .connectTimeout(TIMEOUT_MIN, TimeUnit.MINUTES)
                .readTimeout(TIMEOUT_MIN, TimeUnit.MINUTES)
                .writeTimeout(TIMEOUT_MIN, TimeUnit.MINUTES)
                .retryOnConnectionFailure(true)
                .build();

        Request request = new Request.Builder()
                .url(url)
                .build();

        Response response;
        try {
            response = client.newCall(request).execute();

            String body = response.body().string();
            return body;
//            if (response.isSuccessful()) {
//                return body;
//            } else {
//                Log.e(TAG, "GetHtml: " + "Unexpected code " + response);
//                Log.e(TAG, "GetHtml: " + "Response Body " + body);
//            }
        } catch (IOException e) {
            Log.e(TAG, "GetHtml: " + e.toString());
        }

        return null;
    }
}
