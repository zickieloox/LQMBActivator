package com.zic.lqmb.activator.utils;

import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageInfo;
import android.content.pm.PackageManager;
import android.net.Uri;
import android.util.Log;

import java.io.File;

public class AppUtils {

    private static final String TAG = "AppUtils";

    public static void installApk(Context context, String apkPath) {
        Intent installIntent = new Intent(Intent.ACTION_VIEW);
        installIntent.setDataAndType(Uri.fromFile(new File(apkPath)), "application/vnd.android.package-archive");
        installIntent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
        context.startActivity(installIntent);
    }

    public static void uninstallApp(Context context, String packageName) {
        Intent intent = new Intent(Intent.ACTION_DELETE);
        intent.setData(Uri.parse("package:" + packageName));
        context.startActivity(intent);
    }

    public static String getVersionName(Context context, String packageName) {
        PackageInfo packageInfo = null;
        try {
            packageInfo = context.getPackageManager().getPackageInfo(packageName, 0);
        } catch (PackageManager.NameNotFoundException e) {
            Log.e(TAG, "getVersionName: " + e.toString());
        }

        assert packageInfo != null;
        return packageInfo.versionName;
    }

    public static boolean isAppInstalled(Context context, String packageName) {
        PackageManager pm = context.getPackageManager();

        try {
            pm.getPackageInfo(packageName, PackageManager.GET_ACTIVITIES);

            return true;
        } catch (PackageManager.NameNotFoundException e) {
            Log.e(TAG, "isAppInstalled " + e.toString());
        }

        return false;
    }

    public static void launchActivity(Context context, String packageName, String activityName) {
        Intent intent = new Intent();
        intent.setComponent(new ComponentName(packageName, activityName));
        context.startActivity(intent);
    }
}
