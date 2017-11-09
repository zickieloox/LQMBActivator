package com.zic.lqmb.activator.activity;

import android.content.Intent;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Build;
import android.os.Bundle;
import android.os.Environment;
import android.os.StrictMode;
import android.support.design.widget.TabLayout;
import android.support.v4.view.ViewPager;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.view.Menu;
import android.view.MenuItem;

import com.google.android.gms.ads.AdRequest;
import com.google.android.gms.ads.AdView;
import com.google.android.gms.ads.MobileAds;
import com.zic.lqmb.activator.R;
import com.zic.lqmb.activator.adapter.MainFragmentPagerAdapter;
import com.zic.lqmb.activator.fragment.ActivationFragment;
import com.zic.lqmb.activator.fragment.InfoFragment;
import com.zic.lqmb.activator.fragment.PaymentFragment;
import com.zic.lqmb.activator.utils.FileUtils;

import java.io.File;

import static com.zic.lqmb.activator.utils.Utils.generateLog;

public class MainActivity extends AppCompatActivity {

    private static final String ADMOB_APP_ID = "ca-app-pub-1603834578752860~9473301633";
    private static String sdcard;
    private AdView mAdView;
    private File logFile;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        // Prevent android.os.FileUriExposedException in Android 7 and higher
        // https://stackoverflow.com/posts/40674771/revisions
        // method: AppUtils.installApk()
        if (android.os.Build.VERSION.SDK_INT >= Build.VERSION_CODES.N) {
            StrictMode.VmPolicy.Builder builder = new StrictMode.VmPolicy.Builder();
            StrictMode.setVmPolicy(builder.build());
        }

        sdcard = Environment.getExternalStorageDirectory().getAbsolutePath();

        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        TabLayout tabLayout = (TabLayout) findViewById(R.id.tabLayout);
        ViewPager viewPager = (ViewPager) findViewById(R.id.viewPager);

        setSupportActionBar(toolbar);
        getSupportActionBar().setDisplayShowHomeEnabled(true);
        getSupportActionBar().setLogo(R.drawable.ic_home);
        getSupportActionBar().setDisplayUseLogoEnabled(true);

        if (viewPager != null) {
            setupViewPager(viewPager);
        }

        tabLayout.setupWithViewPager(viewPager);

        mAdView = (AdView) findViewById(R.id.adView);
        MobileAds.initialize(this, ADMOB_APP_ID);
        AdRequest adRequest = new AdRequest.Builder().build();
        mAdView.loadAd(adRequest);
    }

    private void setupViewPager(ViewPager viewPager) {
        MainFragmentPagerAdapter adapter = new MainFragmentPagerAdapter(getSupportFragmentManager());
        adapter.addFragment(new ActivationFragment(), getString(R.string.title_activation));
        adapter.addFragment(new PaymentFragment(), getString(R.string.title_payment));
        //adapter.addFragment(new InstallationFragment(), getString(R.string.title_installation));
        adapter.addFragment(new InfoFragment(), getString(R.string.title_info));
        viewPager.setAdapter(adapter);
    }

    @Override
    protected void onResume() {
        super.onResume();
        if (mAdView != null) {
            mAdView.resume();
        }
    }

    @Override
    protected void onPause() {
        super.onPause();
        if (mAdView != null) {
            mAdView.pause();
        }
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        if (mAdView != null) {
            mAdView.destroy();
        }
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.menu_main, menu);

        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        int id = item.getItemId();

        switch (id) {
            case R.id.item_exit:
                this.finish();
                return true;
            case R.id.item_send_log:
                new CreateLogFileTask().executeOnExecutor(AsyncTask.THREAD_POOL_EXECUTOR);
                return true;
            case R.id.item_contact:
                Intent i = new Intent(Intent.ACTION_VIEW);
                i.setData(Uri.parse("https://m.me/100006594040887"));
                startActivity(i);
                return true;
        }

        return super.onOptionsItemSelected(item);
    }

    private class CreateLogFileTask extends AsyncTask<Void, Void, Void> {

        @Override
        protected Void doInBackground(Void... params) {
            logFile = new File(sdcard + "/LQMB_Activator_Logcat.txt");


            String log = generateLog();
            if (log != null) {
                FileUtils.writeToFile(logFile, log);
            }

            return null;
        }

        @Override
        protected void onPostExecute(Void aVoid) {

            String logFilePath = logFile.getAbsolutePath();
            if (logFile.exists()) {
                Intent emailIntent = new Intent(
                        android.content.Intent.ACTION_SEND);
                emailIntent.setType("text/plain");
                emailIntent.putExtra(android.content.Intent.EXTRA_SUBJECT, "LQMB Activator Log");
                emailIntent.putExtra(android.content.Intent.EXTRA_TEXT, logFile.getName());
                emailIntent.putExtra(Intent.EXTRA_STREAM,
                        Uri.parse("file:///" + logFilePath));
                startActivity(Intent.createChooser(emailIntent, "Send"));
            }
        }
    }
}
