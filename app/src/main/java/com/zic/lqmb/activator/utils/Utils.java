package com.zic.lqmb.activator.utils;

import android.content.Context;
import android.os.Build;
import android.telephony.TelephonyManager;
import android.text.TextUtils;
import android.util.Log;

import com.jaredrummler.android.device.DeviceName;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;

public class Utils {

    private static final String TAG = "Utils";

    public static String md5(String s) {
        String MD5 = "MD5";
        try {
            MessageDigest digest = MessageDigest.getInstance(MD5);
            digest.update(s.getBytes());
            byte[] messageDigest = digest.digest();
            StringBuilder hexString = new StringBuilder();
            for (byte aMessageDigest : messageDigest) {
                String h = Integer.toHexString(aMessageDigest & 255);
                while (h.length() < 2) {
                    h = "0" + h;
                }
                hexString.append(h);
            }
            return hexString.toString().toUpperCase();
        } catch (NoSuchAlgorithmException e) {
            Log.e(TAG, "md5: " + e.toString());
            return "";
        }
    }

    public static String generateKey(Context context) {
        TelephonyManager manager = (TelephonyManager) context.getSystemService(Context.TELEPHONY_SERVICE);
        String imei = manager.getDeviceId();

        final String[] codename = new String[1];
        DeviceName.with(context).request(new DeviceName.Callback() {

            @Override
            public void onFinished(DeviceName.DeviceInfo info, Exception error) {
//                String manufacturer = info.manufacturer;  // "Samsung"
//                String name = info.marketName;            // "Galaxy S7 Edge"
//                String model = info.model;                // "SAMSUNG-SM-G935A"
//                String deviceName = info.getName();       // "Galaxy S7 Edge"
                codename[0] = info.codename;          // "hero2lte"
            }
        });

        String key = "ZZ";

        if (imei == null) {
            key += Utils.md5(codename[0]).substring(0, 16);
        } else if (codename[0] == null) {
            key += Utils.md5(imei).substring(0, 16);
        } else {
            key += Utils.md5(imei).substring(0, 8) + Utils.md5(codename[0]).substring(0, 8);
        }

        return key;
    }

    public static String generateLog() {
        StringBuilder log = new StringBuilder();
        try {
            Process process = Runtime.getRuntime().exec("logcat -d");
            BufferedReader bufferedReader = new BufferedReader(
                    new InputStreamReader(process.getInputStream()));

            String line;
            while ((line = bufferedReader.readLine()) != null) {
                log.append(line + "\n");
            }
        } catch (IOException e) {
            Log.e(TAG, "generateLog: " + e.toString());
            return null;
        }

        return log.toString();
    }

    public static String getDeviceName() {
        String manufacturer = Build.MANUFACTURER;
        String model = Build.MODEL;
        if (model.startsWith(manufacturer)) {
            return capitalize(model);
        }
        return capitalize(manufacturer) + " " + model;
    }

    private static String capitalize(String str) {
        if (TextUtils.isEmpty(str)) {
            return str;
        }
        char[] arr = str.toCharArray();
        boolean capitalizeNext = true;
        String phrase = "";
        for (char c : arr) {
            if (capitalizeNext && Character.isLetter(c)) {
                phrase += Character.toUpperCase(c);
                capitalizeNext = false;
                continue;
            } else if (Character.isWhitespace(c)) {
                capitalizeNext = true;
            }
            phrase += c;
        }
        return phrase;
    }
}
