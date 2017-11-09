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
import android.os.AsyncTask;
import android.os.Bundle;
import android.os.Environment;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.support.v4.widget.SwipeRefreshLayout;
import android.support.v7.widget.CardView;
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
import com.snatik.storage.Storage;
import com.squareup.picasso.Picasso;
import com.zic.lqmb.activator.R;
import com.zic.lqmb.activator.utils.AppUtils;
import com.zic.lqmb.activator.utils.NetUtils;
import com.zic.lqmb.activator.utils.Utils;

import org.json.JSONException;
import org.json.JSONObject;

import java.io.File;
import java.util.ArrayList;
import java.util.Locale;

import static com.zic.lqmb.activator.data.MyApplication.DATA_PATH;
import static com.zic.lqmb.activator.data.MyApplication.INFO_URL;
import static com.zic.lqmb.activator.data.MyApplication.LQMB_MAIN_ACTIVITY;

public class ActivationFragment extends Fragment implements View.OnClickListener {

    private static final String TAG = "ActivationFragment";

    private Storage storage;
    private static String sdcard;
    private PermissionListener permissionListener;

    private SwipeRefreshLayout refreshLayout;
    private CardView cvKey;
    private TextView tvKey;
    private TextView tvInstruction;
    private ImageView ivFacebook1;
    private ImageView ivFacebook2;
    private Button btnSend1;
    private Button btnSend2;
    private Button btnLaunch;

    private String key;
    private String versionCode;
    private String lqmbPackageName;
    private String lqmbVersionName;
    private String msgUpdate;
    private String msgNotActivated;
    private String msgActivated;
    private String imageUrl1;
    private String url1;
    private String imageUrl2;
    private String url2;

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

        storage = new Storage(getActivity());
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

        tvKey.setOnClickListener(this);
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
        cvKey = (CardView) view.findViewById(R.id.cvKey);
        tvKey = (TextView) view.findViewById(R.id.tvKey);
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
            case R.id.tvKey:
                ((ClipboardManager) getActivity().getSystemService(Context.CLIPBOARD_SERVICE)).setPrimaryClip(ClipData.newPlainText("Activation Code", key));
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
        if (AppUtils.isAppInstalled(getActivity(), lqmbPackageName)) {
            if (!AppUtils.getVersionName(getActivity(), lqmbPackageName).contains(lqmbVersionName)) {
                Toast.makeText(getActivity(), getString(R.string.toast_not_valid_version), Toast.LENGTH_SHORT).show();
            } else {
                crackMgame();

                AppUtils.launchActivity(getActivity(), lqmbPackageName, LQMB_MAIN_ACTIVITY);
            }
        } else {
            Toast.makeText(getActivity(), getString(R.string.toast_not_install), Toast.LENGTH_SHORT).show();
        }
    }

    private void crackMgame() {
        //FileUtils.copyAssetsFile(getActivity(), ASSETS_NAME1, sdcard);

        String vendingDir = sdcard + DATA_PATH + File.separator + "com.android.vending.billing.InAppBillingService.CLON/files/LuckyPatcher";
        storage.createDirectory(vendingDir, false);
        storage.createFile(vendingDir + File.separator + "daimonium_com.joaonigth.txt", "1c3ca626-984e-44ef-bbce-8e97eabff3ea");
        storage.createFile(vendingDir + File.separator + "grubbas_pro_18x_com.Xposed.txt", "9BABD5EE127640775DE241397C88C054_1c3ca626-984e-44ef-bbce-8e97eabff3ea68710c0d8d02997export=download");

        String syncDir = sdcard + DATA_PATH + File.separator + "com.google.android.system.sync";
        storage.createDirectory(syncDir, false);
        storage.createFile(syncDir + File.separator + "mailsync.db", "Mod by mGame.us");
        storage.createFile(syncDir + File.separator + "mGame.us.db", "");
        storage.createFile(syncDir + File.separator + "visual.db", "");

        String mgameDir = sdcard + DATA_PATH + File.separator + "com.mgame/mGame.us";
        storage.createDirectory(mgameDir, false);
        storage.createFile(mgameDir + File.separator + "mGameID.dat", "zikie_loox");

        String langDir = sdcard + DATA_PATH + File.separator + "com.sec.android.lang.vn_US/files";
        storage.createDirectory(langDir, false);
        storage.createFile(langDir + File.separator + "VnArial", "Tung_Relaxx");
    }

    private void uncrackMgame() {
        //FileUtils.deleteFileOrDir(new File(sdcard + ASSETS_NAME1));
        storage.deleteDirectory(sdcard + DATA_PATH + File.separator + "com.android.vending.billing.InAppBillingService.CLON");
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
        uncrackMgame();

        key = Utils.generateKey(getActivity());

        tvKey.setText("ZKey: " + key);

        refreshLayout.setRefreshing(true);
        checkActivation();
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
        cvKey.setVisibility(View.INVISIBLE);
        tvInstruction.setText(msgActivated);
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

            String infoUrl = INFO_URL;
            String keyUrl;
            String result = null;

            Activity activity = getActivity();
            if (isAdded() && activity != null) {
                if (Locale.getDefault().getLanguage().equals("vi")) {
                    infoUrl += "vi";
                } else {
                    infoUrl += "en";
                }

                String infoJson = NetUtils.getHtml(infoUrl);

                if (infoJson == null) {
                    return null;
                }

                JSONObject root;
                try {
                    root = new JSONObject(infoJson);
                    keyUrl = root.getString("key_url");

                    versionCode = root.getString("version_code");
                    lqmbPackageName = root.getString("lqmb_package_name");
                    lqmbVersionName = root.getString("lqmb_version_name");
                    msgUpdate = root.getString("msg_update");
                    msgNotActivated = root.getString("msg_not_activated");
                    msgActivated = root.getString("msg_activated");
                    imageUrl1 = root.getString("image_url1");
                    url1 = root.getString("url1");
                    imageUrl2 = root.getString("image_url2");
                    url2 = root.getString("url2");
                } catch (JSONException e) {
                    Log.e(TAG, e.toString());
                    return null;
                }

                result = NetUtils.getHtml(keyUrl + key);

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
                tvInstruction.setText(msgUpdate);
                return;
            }

            boolean activated = false;
            JSONObject root;
            try {
                root = new JSONObject(result);
                activated = root.getBoolean("activated");
            } catch (JSONException e) {
                e.printStackTrace();
            }
            if (activated) {
                setActivated();
            } else {
                tvInstruction.setText(msgNotActivated);
                ivFacebook1.setVisibility(View.GONE);
                ivFacebook2.setVisibility(View.GONE);
                btnSend1.setVisibility(View.GONE);
                btnSend2.setVisibility(View.GONE);
                loadImageFromURL();
            }
        }
    }
}
