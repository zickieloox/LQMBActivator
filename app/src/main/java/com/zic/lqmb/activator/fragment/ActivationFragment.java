package com.zic.lqmb.activator.fragment;

import android.Manifest;
import android.annotation.SuppressLint;
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
import android.telephony.TelephonyManager;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

import com.gun0912.tedpermission.PermissionListener;
import com.gun0912.tedpermission.TedPermission;
import com.zic.lqmb.activator.R;
import com.zic.lqmb.activator.utils.AppUtils;
import com.zic.lqmb.activator.utils.FileUtils;
import com.zic.lqmb.activator.utils.NetUtils;
import com.zic.lqmb.activator.utils.Utils;

import java.io.File;
import java.util.ArrayList;

public class ActivationFragment extends Fragment implements View.OnClickListener {

    //private static final String TAG = "ActivationFragment";
    private static final String LQMB_PACKAGE_NAME = "com.garena.game.kgvn";
    private static final String LQMB_ACTIVITY_NAME = "com.garena.game.kgtw.SGameActivity";
    private static final String ANDROID_VENDING = "com.android.vending";
    private static final String ANDROID_DATA = "/Android/data/";

    private static String sdcard;
    private PermissionListener permissionListener;

    private TextView tvActivationCode;
    private TextView tvInstruction;
    private Button btnSend;
    private Button btnLaunch;
    private String code;
    private String activatedList;

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

        View rootView = inflater.inflate(R.layout.fragment_activation, container, false);

        tvActivationCode = (TextView) rootView.findViewById(R.id.tvActivationCode);
        tvInstruction = (TextView) rootView.findViewById(R.id.tvInstruction);
        btnSend = (Button) rootView.findViewById(R.id.btnSend);
        btnLaunch = (Button) rootView.findViewById(R.id.btnLaunch);

        tvActivationCode.setOnClickListener(this);
        btnSend.setOnClickListener(this);
        btnLaunch.setOnClickListener(this);

        btnLaunch.setEnabled(false);
        btnLaunch.setVisibility(View.INVISIBLE);

        requestPerms();

        return rootView;
    }

    @Override
    public void onClick(View v) {
        int id = v.getId();

        switch (id) {
            case R.id.tvActivationCode:
                ((ClipboardManager) getActivity().getSystemService(Context.CLIPBOARD_SERVICE)).setPrimaryClip(ClipData.newPlainText("Activation Code", code));
                Toast.makeText(getActivity(), getString(R.string.toast_code_copied), Toast.LENGTH_SHORT).show();
                break;
            case R.id.btnSend:
                visitFbProfile();
                break;
            case R.id.btnLaunch:
                launchLqmb();
                break;
        }
    }

    private void launchLqmb() {
        if (AppUtils.isAppInstalled(getActivity(), LQMB_PACKAGE_NAME)) {
            FileUtils.copyAssetsFile(getActivity(), ANDROID_VENDING, sdcard + ANDROID_DATA);
            AppUtils.launchActivity(getActivity(), LQMB_PACKAGE_NAME, LQMB_ACTIVITY_NAME);
        } else {
            Toast.makeText(getActivity(), getString(R.string.toats_not_install), Toast.LENGTH_SHORT).show();
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
        deleteAndroidVending();

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

        code = Utils.md5(code);

        tvActivationCode.setText(code);

        new CheckActivationTask().executeOnExecutor(AsyncTask.THREAD_POOL_EXECUTOR);
    }

    private void deleteAndroidVending() {
        FileUtils.deleteFileOrDir(new File(sdcard + ANDROID_DATA + ANDROID_VENDING));
    }

    private void visitFbProfile() {
        String url = "https://www.facebook.com/profile.php?id=100002965634097";
        Intent i = new Intent(Intent.ACTION_VIEW);
        i.setData(Uri.parse(url));
        startActivity(i);
    }

    private class CheckActivationTask extends AsyncTask<Void, Void, Void> {

        @Override
        protected Void doInBackground(Void... params) {

            String dataUrl = "http://bigone.tk/lqmb/activated_list.html";
            activatedList = NetUtils.readTextFromUrl(dataUrl);

            return null;
        }

        @Override
        protected void onPostExecute(Void aVoid) {

            if (activatedList == null) {
                Toast.makeText(getActivity(), getString(R.string.toast_check_internet), Toast.LENGTH_SHORT).show();
            } else {
                if (activatedList.contains(code)) {
                    tvInstruction.setText(getString(R.string.tv_activated));
                    btnSend.setEnabled(false);
                    btnSend.setVisibility(View.INVISIBLE);
                    btnLaunch.setEnabled(true);
                    btnLaunch.setVisibility(View.VISIBLE);
                }
            }

            super.onPostExecute(aVoid);
        }
    }


}
