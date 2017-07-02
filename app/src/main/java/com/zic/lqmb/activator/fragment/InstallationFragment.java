package com.zic.lqmb.activator.fragment;

import android.Manifest;
import android.content.pm.PackageManager;
import android.graphics.drawable.Drawable;
import android.os.AsyncTask;
import android.os.Bundle;
import android.os.Environment;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
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
import com.rey.material.widget.ProgressView;
import com.zic.lqmb.activator.R;
import com.zic.lqmb.activator.utils.AppUtils;
import com.zic.lqmb.activator.utils.FileUtils;

import org.zeroturnaround.zip.NameMapper;
import org.zeroturnaround.zip.ZipUtil;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;

public class InstallationFragment extends Fragment implements View.OnClickListener {

    private static final String TAG = "InstallationFragment";
    private static final String LQMB_PACKAGE_NAME = "com.garena.game.kgvn";
    private static final String HACK_FILE_NAME = "lqmb.zic";
    private static final String HACK_FILE_MD5 = "4da986db746515f07d6af90831bccf1a";

    private PermissionListener permissionListener;
    private ProgressView proviewInstall;
    private TextView tvProgress;

    private String sdcard;
    private Button btnInstall;

    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {

        permissionListener = new PermissionListener() {
            @Override
            public void onPermissionGranted() {
                installLqmb();
            }

            @Override
            public void onPermissionDenied(ArrayList<String> deniedPermissions) {
                Toast.makeText(getActivity(), getString(R.string.toast_perm_denied) + "\n" + deniedPermissions.toString(), Toast.LENGTH_SHORT).show();
            }


        };

        sdcard = Environment.getExternalStorageDirectory().getAbsolutePath();

        View rootView = inflater.inflate(R.layout.fragment_installation, container, false);

        ImageView imgGameIcon = (ImageView) rootView.findViewById(R.id.imgGameIcon);
        Button btnUninstall = (Button) rootView.findViewById(R.id.btnUninstall);
        btnInstall = (Button) rootView.findViewById(R.id.btnInstall);
        proviewInstall = (ProgressView) rootView.findViewById(R.id.proviewInstall);
        tvProgress = (TextView) rootView.findViewById(R.id.tv_progress);

        if (AppUtils.isAppInstalled(getActivity(), LQMB_PACKAGE_NAME)) {
            Drawable icon = null;
            try {
                icon = getActivity().getPackageManager().getApplicationIcon(LQMB_PACKAGE_NAME);
            } catch (PackageManager.NameNotFoundException e) {
                Log.e(TAG, "Game Icon: " + e.toString());
            }
            imgGameIcon.setImageDrawable(icon);

            if (!AppUtils.getVersionName(getActivity(), LQMB_PACKAGE_NAME).contains("z")) {
                tvProgress.setText(getString(R.string.tv_not_hacked));
            } else {
                tvProgress.setText(getString(R.string.tv_hacked));
            }

        } else {
            imgGameIcon.setImageResource(android.R.drawable.ic_delete);
            btnUninstall.setText(getString(R.string.btn_not_installed));
            btnUninstall.setEnabled(false);
            tvProgress.setText(getString(R.string.tv_not_installed));
        }

        btnUninstall.setOnClickListener(this);
        btnInstall.setOnClickListener(this);

        return rootView;
    }

    @Override
    public void onClick(View v) {
        int id = v.getId();

        switch (id) {

            case R.id.btnUninstall:
                AppUtils.uninstallApp(getActivity(), LQMB_PACKAGE_NAME);
                break;

            case R.id.btnInstall:
                if (!FileUtils.isAvailableToInstallLqmb()) {
                    Toast.makeText(getActivity(), getString(R.string.toast_not_enough) + FileUtils.formatSize(FileUtils.getAvailableInternalMemorySize()), Toast.LENGTH_LONG).show();
                    return;
                }
                requestPerms();
                break;
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

    private void installLqmb() {
        new ExtractDataTask().executeOnExecutor(AsyncTask.THREAD_POOL_EXECUTOR);
    }

    private class ExtractDataTask extends AsyncTask<Void, String, Void> {

        @Override
        protected void onPreExecute() {
            btnInstall.setEnabled(false);
            tvProgress.setText(getString(R.string.tv_searching_file));
            proviewInstall.setVisibility(View.VISIBLE);
        }

        @Override
        protected Void doInBackground(Void... params) {

            String hackFilePath = FileUtils.findFile(sdcard, HACK_FILE_NAME);

            if (hackFilePath == null) {
                publishProgress(getString(R.string.tv_not_found));
                cancel(true);
            }

            publishProgress(getString(R.string.tv_checking_md5));

            String md5 = null;
            try {
                md5 = FileUtils.getFileChecksum(new File(hackFilePath));
            } catch (IOException e) {
                Log.e(TAG, "getFileChecksum: " + e.toString());
            }

            assert md5 != null;
            if (!md5.equalsIgnoreCase(HACK_FILE_MD5)) {
                publishProgress(getString(R.string.tv_md5_invalid));
                cancel(true);
            }


            publishProgress(getString(R.string.tv_copying_data));

            ZipUtil.unpack(new File(hackFilePath), new File(sdcard + "/Android"), new NameMapper() {
                public String map(String name) {
                    return name.startsWith("obb/") ? name : null;
                }
            });

            publishProgress(getString(R.string.tv_hacking_map));

            ZipUtil.unpack(new File(hackFilePath), new File(sdcard + "/Android"), new NameMapper() {
                public String map(String name) {
                    return name.startsWith("data/") ? name : null;
                }
            });

            ZipUtil.unpackEntry(new File(hackFilePath), "com.garena.game.kgvn.apk", new File(sdcard + "/Android/com.garena.game.kgvn.apk"));

            publishProgress(getString(R.string.tv_install_lqmb));

            try {
                Thread.sleep(3000);
            } catch (InterruptedException e) {
                e.printStackTrace();
            }

            if (md5.equalsIgnoreCase(HACK_FILE_MD5)) {
                AppUtils.installApk(getActivity(), sdcard + "/Android/com.garena.game.kgvn.apk");
            }
            return null;
        }

        @Override
        protected void onProgressUpdate(String... values) {

            tvProgress.setText(values[0]);
        }

        @Override
        protected void onPostExecute(Void aVoid) {
            btnInstall.setEnabled(true);
            Toast.makeText(getActivity(), getString(R.string.toast_grant_install), Toast.LENGTH_SHORT).show();
            tvProgress.setText(getString(R.string.tv_done));
            proviewInstall.setVisibility(View.INVISIBLE);
        }

    }

}
