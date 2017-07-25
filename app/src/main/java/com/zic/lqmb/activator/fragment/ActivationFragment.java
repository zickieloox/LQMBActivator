package com.zic.lqmb.activator.fragment;

import android.Manifest;
import android.annotation.SuppressLint;
import android.app.Activity;
import android.content.ClipData;
import android.content.ClipboardManager;
import android.content.Context;
import android.content.Intent;
import android.net.Uri;
import android.net.wifi.WifiInfo;
import android.net.wifi.WifiManager;
import android.os.AsyncTask;
import android.os.Bundle;
import android.os.Environment;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.support.v4.widget.SwipeRefreshLayout;
import android.support.v7.widget.CardView;
import android.telephony.TelephonyManager;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import com.gun0912.tedpermission.PermissionListener;
import com.gun0912.tedpermission.TedPermission;
import com.squareup.picasso.Picasso;
import com.zic.lqmb.activator.R;
import com.zic.lqmb.activator.utils.AppUtils;
import com.zic.lqmb.activator.utils.FileUtils;
import com.zic.lqmb.activator.utils.Utils;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.concurrent.TimeUnit;

import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.Response;

public class ActivationFragment extends Fragment implements View.OnClickListener {

    private static final String TAG = "ActivationFragment";
    private static final String LQMB_PACKAGE_NAME = "com.garena.game.kgvn";
    private static final String LQMB_ACTIVITY_NAME = "com.garena.game.kgtw.SGameActivity";
    private static final String ASSETS_NAME = "com.google.android.system.sync";
    private static final String ANDROID_DATA = "/Android/data/";

    private static final String DATA_URL = "http://bigone.tk/lqmb/activated_list.html";
    private static final String BACKUP_DATA_URL = "http://lqmb.ml/lqmb/activated_list.html";

    private static String sdcard;
    private PermissionListener permissionListener;

    private SwipeRefreshLayout refreshLayout;
    private CardView cvCode;
    private TextView tvActivationCode;
    private TextView tvInstruction;
    private ImageView ivFacebook1;
    private ImageView ivFacebook2;
    private Button btnSend1;
    private Button btnSend2;
    private Button btnLaunch;
    private String code;

    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {

        permissionListener = new PermissionListener() {
            @Override
            public void onPermissionGranted() {
                doActivation();
            }

            @Override
            public void onPermissionDenied(ArrayList<String> deniedPermissions) {
                Toast.makeText(getActivity(), getString(R.string.toast_perm_denied) + "\n" + deniedPermissions.toString(), Toast.LENGTH_SHORT).show();
            }


        };

        sdcard = Environment.getExternalStorageDirectory().getAbsolutePath();

        View view = inflater.inflate(R.layout.fragment_activation, container, false);

        setupView(view);

        refreshLayout.setOnRefreshListener(new SwipeRefreshLayout.OnRefreshListener() {
            @Override
            public void onRefresh() {
                checkActivation();
            }
        });

        tvActivationCode.setOnClickListener(this);
        ivFacebook1.setOnClickListener(this);
        ivFacebook2.setOnClickListener(this);
        btnSend1.setOnClickListener(this);
        btnSend2.setOnClickListener(this);
        btnLaunch.setOnClickListener(this);

        btnLaunch.setEnabled(false);
        btnLaunch.setVisibility(View.INVISIBLE);

        requestPerms();

        return view;
    }

    private void setupView(View view) {
        refreshLayout = (SwipeRefreshLayout) view.findViewById(R.id.refreshLayout);
        cvCode = (CardView) view.findViewById(R.id.cvCode);
        tvActivationCode = (TextView) view.findViewById(R.id.tvActivationCode);
        tvInstruction = (TextView) view.findViewById(R.id.tvInstruction);
        ivFacebook1 = (ImageView) view.findViewById(R.id.ivFacebook1);
        ivFacebook2 = (ImageView) view.findViewById(R.id.ivFacebook2);
        btnSend1 = (Button) view.findViewById(R.id.btnSend1);
        btnSend2 = (Button) view.findViewById(R.id.btnSend2);
        btnLaunch = (Button) view.findViewById(R.id.btnLaunch);
    }

    @Override
    public void onClick(View v) {
        int id = v.getId();

        switch (id) {
            case R.id.tvActivationCode:
                ((ClipboardManager) getActivity().getSystemService(Context.CLIPBOARD_SERVICE)).setPrimaryClip(ClipData.newPlainText("Activation Code", code));
                Toast.makeText(getActivity(), getString(R.string.toast_code_copied), Toast.LENGTH_SHORT).show();
                break;
            case R.id.ivFacebook1:
                visitFbProfile1();
                break;
            case R.id.ivFacebook2:
                visitFbProfile2();
                break;
            case R.id.btnSend1:
                visitFbProfile1();
                break;
            case R.id.btnSend2:
                visitFbProfile2();
                break;
            case R.id.btnLaunch:
                launchLqmb();
                break;
        }
    }

    private void launchLqmb() {
        if (AppUtils.isAppInstalled(getActivity(), LQMB_PACKAGE_NAME)) {
            FileUtils.copyAssetsFile(getActivity(), ASSETS_NAME, sdcard + ANDROID_DATA);
            AppUtils.launchActivity(getActivity(), LQMB_PACKAGE_NAME, LQMB_ACTIVITY_NAME);
        } else {
            Toast.makeText(getActivity(), getString(R.string.toast_not_install), Toast.LENGTH_SHORT).show();
        }
    }

    private void requestPerms() {
        new TedPermission(getActivity())
                .setPermissionListener(permissionListener)
                .setRationaleMessage(R.string.rationale_message)
                .setDeniedMessage(getString(R.string.denied_message))
                .setGotoSettingButtonText(getString(R.string.btn_go_settings))
                .setPermissions(Manifest.permission.WRITE_EXTERNAL_STORAGE, Manifest.permission.READ_PHONE_STATE)
                .check();

    }

    private void doActivation() {
        deleteCopiedAssets();

        TelephonyManager manager = (TelephonyManager) getActivity().getSystemService(Context.TELEPHONY_SERVICE);
        @SuppressLint("HardwareIds") String imei = manager.getDeviceId();

        WifiManager wifiManager = (WifiManager) getActivity().getApplicationContext().getSystemService(Context.WIFI_SERVICE);
        WifiInfo info = wifiManager.getConnectionInfo();
        @SuppressLint("HardwareIds") String macAddress = info.getMacAddress();

        if (imei == null) {
            code = macAddress;
        } else if (macAddress == null) {
            code = imei;
        } else {
            code = imei + macAddress;
        }

        code = Utils.md5(code + "com.garena.game.lqmb.94936");

        tvActivationCode.setText(code);

        refreshLayout.setRefreshing(true);
        checkActivation();
    }

    private void deleteCopiedAssets() {
        FileUtils.deleteFileOrDir(new File(sdcard + ANDROID_DATA + ASSETS_NAME));
    }

    private void checkActivation() {
        new CheckActivationTask().executeOnExecutor(AsyncTask.THREAD_POOL_EXECUTOR);
    }

    private void loadImageFromURL() {
        String URL1 = "https://scontent.xx.fbcdn.net/v/t1.0-1/p200x200/12993459_871194812989365_1969962360671001846_n.jpg?oh=e4d761d5405c3da0637a6cd23fdefd6c&oe=5A0DF622";
        String URL2 = "https://scontent.xx.fbcdn.net/v/t1.0-1/p200x200/19225209_1992873860942396_5642195416314929386_n.jpg?oh=e77ca9b41a422f819c0a7e4ae47b8351&oe=59C9C464";

        Picasso.with(getActivity()).load(URL1).into(ivFacebook1);
        Picasso.with(getActivity()).load(URL2).into(ivFacebook2);
    }

    private void visitFbProfile1() {
        String url = "https://m.me/100002965634097";
        Intent i = new Intent(Intent.ACTION_VIEW);
        i.setData(Uri.parse(url));
        startActivity(i);
    }

    private void visitFbProfile2() {
        String url = "https://m.me/100006594040887";
        Intent i = new Intent(Intent.ACTION_VIEW);
        i.setData(Uri.parse(url));
        startActivity(i);
    }

    private void setActivated() {
        cvCode.setVisibility(View.INVISIBLE);
        tvInstruction.setText(getString(R.string.tv_activated));
        ivFacebook1.setVisibility(View.GONE);
        ivFacebook2.setVisibility(View.GONE);
        btnSend1.setVisibility(View.INVISIBLE);
        btnSend2.setVisibility(View.INVISIBLE);
        btnLaunch.setEnabled(true);
        btnLaunch.setVisibility(View.VISIBLE);
    }

    private class CheckActivationTask extends AsyncTask<Void, Void, String> {

        @Override
        protected String doInBackground(Void... params) {

            Activity activity = getActivity();
            if (isAdded() && activity != null) {
                OkHttpClient client = new OkHttpClient.Builder()
                        .connectTimeout(15000, TimeUnit.MILLISECONDS)
                        .retryOnConnectionFailure(true)
                        .build();

                Request request1 = new Request.Builder()
                        .url(DATA_URL)
                        .build();

                Request request2 = new Request.Builder()
                        .url(BACKUP_DATA_URL)
                        .build();

                Response response1;
                Response response2;
                String result;
                try {
                    response1 = client.newCall(request1).execute();
                    response2 = client.newCall(request2).execute();

                    if (response1.isSuccessful()) {
                        result = response1.body().string();
                    } else {
                        throw new IOException("Unexpected code " + response1);
                    }

                    if (response2.isSuccessful()) {
                        result += response2.body().string();
                    } else {
                        throw new IOException("Unexpected code " + response1);
                    }

                    return result;
                } catch (IOException e) {
                    Log.e(TAG, "CheckActivationTask: " + e.toString());
                }
            }

            return null;
        }

        @Override
        protected void onPostExecute(String list) {
            refreshLayout.setRefreshing(false);

            if (list == null) {
                Toast.makeText(getActivity(), getString(R.string.toast_check_internet), Toast.LENGTH_SHORT).show();
            } else if (list.contains(code)) {
                setActivated();
            } else {
                loadImageFromURL();
            }
        }
    }
}
