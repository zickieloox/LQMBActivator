package com.zic.lqmb.activator.fragment;

import android.Manifest;
import android.annotation.SuppressLint;
import android.app.Activity;
import android.content.ClipData;
import android.content.ClipboardManager;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageInfo;
import android.content.pm.PackageManager;
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
import com.zic.lqmb.activator.utils.NetUtils;
import com.zic.lqmb.activator.utils.Utils;

import org.json.JSONException;
import org.json.JSONObject;

import java.io.File;
import java.util.ArrayList;
import java.util.Locale;
import java.util.regex.Pattern;

import static com.zic.lqmb.activator.data.MyApplication.ANDROID_DATA;
import static com.zic.lqmb.activator.data.MyApplication.ASSETS_NAME;
import static com.zic.lqmb.activator.data.MyApplication.LQMB_MAIN_ACTIVITY;
import static com.zic.lqmb.activator.data.MyApplication.LQMB_PACKAGE_NAME;
import static com.zic.lqmb.activator.data.MyApplication.MGAME_FOLDER;
import static com.zic.lqmb.activator.data.MyApplication.SOURCE_URL;

public class ActivationFragment extends Fragment implements View.OnClickListener {

    private static final String TAG = "ActivationFragment";

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
    private String versionCode;
    private String lqmbVersionCode = "z";
    private String textUpdate;
    private String textNotActivated;
    private String textActivated;
    private String url1;
    private String url2;
    private String imageUrl1;
    private String imageUrl2;

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

        tvInstruction.setText(getString(R.string.tv_not_activated));

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
        btnLaunch.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Toast.makeText(getActivity(), getString(R.string.toast_not_activated), Toast.LENGTH_SHORT).show();
            }
        });

        ivFacebook1.setVisibility(View.GONE);
        ivFacebook2.setVisibility(View.GONE);
        btnSend1.setVisibility(View.INVISIBLE);
        btnSend2.setVisibility(View.INVISIBLE);

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
                Toast.makeText(getActivity(), getString(R.string.toast_key_copied), Toast.LENGTH_SHORT).show();
                break;
            case R.id.ivFacebook1:
                visitLink(url1);
                break;
            case R.id.ivFacebook2:
                visitLink(url2);
                break;
            case R.id.btnSend1:
                visitLink(url1);
                break;
            case R.id.btnSend2:
                visitLink(url2);
                break;
            case R.id.btnLaunch:
                launchLqmb();
                break;
        }
    }

    private void launchLqmb() {
        if (AppUtils.isAppInstalled(getActivity(), LQMB_PACKAGE_NAME)) {
            if (!AppUtils.getVersionName(getActivity(), LQMB_PACKAGE_NAME).contains(lqmbVersionCode)) {
                Toast.makeText(getActivity(), getString(R.string.toast_not_valid_version), Toast.LENGTH_SHORT).show();
            } else {
                FileUtils.copyAssetsFile(getActivity(), ASSETS_NAME, sdcard + ANDROID_DATA);
                AppUtils.launchActivity(getActivity(), LQMB_PACKAGE_NAME, LQMB_MAIN_ACTIVITY);
            }
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

    @SuppressLint("SetTextI18n")
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

        tvActivationCode.setText("ZKey: " + code);

        refreshLayout.setRefreshing(true);
        checkActivation();
    }

    private void deleteCopiedAssets() {
        FileUtils.deleteFileOrDir(new File(sdcard + ANDROID_DATA + MGAME_FOLDER));
        FileUtils.deleteFileOrDir(new File(sdcard + ANDROID_DATA + ASSETS_NAME));
    }

    private void checkActivation() {
        new CheckActivationTask().executeOnExecutor(AsyncTask.THREAD_POOL_EXECUTOR);
    }

    private void loadImageFromURL() {
        Picasso.with(getActivity()).load(imageUrl1).into(ivFacebook1);
        Picasso.with(getActivity()).load(imageUrl2).into(ivFacebook2);
    }

    private void visitLink(String url) {
        Intent i = new Intent(Intent.ACTION_VIEW);
        i.setData(Uri.parse(url));
        startActivity(i);
    }

    private void setActivated() {
        cvCode.setVisibility(View.INVISIBLE);
        tvInstruction.setText(textActivated);
        ivFacebook1.setVisibility(View.GONE);
        ivFacebook2.setVisibility(View.GONE);
        btnSend1.setVisibility(View.INVISIBLE);
        btnSend2.setVisibility(View.INVISIBLE);
        btnLaunch.setOnClickListener(this);
        btnLaunch.setEnabled(true);
    }

    private class CheckActivationTask extends AsyncTask<Void, Void, String> {

        @Override
        protected String doInBackground(Void... params) {

            String result = null;

            Activity activity = getActivity();
            if (isAdded() && activity != null) {
                String source = NetUtils.getHtml(SOURCE_URL);

                if (source == null) {
                    return null;
                }

                String urls;
                JSONObject root;
                try {
                    root = new JSONObject(source);
                    urls = root.getString("bio");
                } catch (JSONException e) {
                    Log.e(TAG, e.toString());
                    return null;
                }

                String[] urlArr = urls.split(System.getProperty("line.separator"));
                result = NetUtils.getHtml(urlArr[0]);
                result += NetUtils.getHtml(urlArr[1]);

                String data = result.split("end")[0];
                String[] lines = data.split(System.getProperty("line.separator"));
                String info;
                if (Locale.getDefault().getLanguage().equals("vi")) {
                    info = lines[1];
                } else {
                    info = lines[0];
                }

                String[] infoArr = info.split(Pattern.quote("|"));
                versionCode = infoArr[0];
                lqmbVersionCode = infoArr[1];
                textUpdate = infoArr[2];
                textNotActivated = infoArr[3];
                textActivated = infoArr[4];
                url1 = infoArr[5];
                url2 = infoArr[6];
                imageUrl1 = infoArr[7];
                imageUrl2 = infoArr[8];
            }

            return result;
        }

        @Override
        protected void onPostExecute(String result) {
            refreshLayout.setRefreshing(false);

            if (result == null) {
                Toast.makeText(getActivity(), getString(R.string.toast_check_internet), Toast.LENGTH_SHORT).show();
                return;
            }

            String thisVersionCode;
            PackageInfo pInfo;
            try {
                pInfo = getActivity().getPackageManager().getPackageInfo(getActivity().getPackageName(), 0);
                thisVersionCode = String.valueOf(pInfo.versionCode);
            } catch (PackageManager.NameNotFoundException e) {
                Log.e(TAG, e.toString());
                return;
            }

            if (!thisVersionCode.equals(versionCode)) {
                ivFacebook1.setVisibility(View.GONE);
                ivFacebook2.setVisibility(View.GONE);
                btnSend1.setVisibility(View.INVISIBLE);
                btnSend2.setVisibility(View.INVISIBLE);
                tvInstruction.setText(textUpdate);
                return;
            }

            if (result.contains(code)) {
                setActivated();
            } else {
                tvInstruction.setText(textNotActivated);
                ivFacebook1.setVisibility(View.VISIBLE);
                ivFacebook2.setVisibility(View.VISIBLE);
                btnSend1.setVisibility(View.VISIBLE);
                btnSend2.setVisibility(View.VISIBLE);
                loadImageFromURL();
            }
        }
    }
}
