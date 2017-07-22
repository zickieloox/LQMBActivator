package com.zic.lqmb.activator.fragment;

import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;

import com.google.android.gms.ads.AdListener;
import com.google.android.gms.ads.AdRequest;
import com.google.android.gms.ads.InterstitialAd;
import com.google.android.gms.ads.MobileAds;
import com.zic.lqmb.activator.R;

public class InfoFragment extends Fragment implements View.OnClickListener {

    private static final String TAG = "InfoFragment";
    private static final String ADMOB_APP_ID = "ca-app-pub-1603834578752860~9473301633";

    private InterstitialAd mInterstitialAd;

    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {

        View rootView = inflater.inflate(R.layout.fragment_info, container, false);

        Button btnJoin = (Button) rootView.findViewById(R.id.btnJoin);
        Button btnDonate = (Button) rootView.findViewById(R.id.btnDonate);

        mInterstitialAd = new InterstitialAd(getActivity());
        MobileAds.initialize(getActivity(), ADMOB_APP_ID);
        mInterstitialAd.setAdUnitId(getString(R.string.ad_unit_interstitial));
        mInterstitialAd.setAdListener(new AdListener() {

            @Override
            public void onAdClosed() {
                visitFbGroup();
                loadInterstitialAd();
            }
        });

        btnJoin.setOnClickListener(this);
        btnDonate.setOnClickListener(this);
        loadInterstitialAd();

        return rootView;
    }

    private void loadInterstitialAd() {
        if (mInterstitialAd != null) {
            AdRequest adRequest = new AdRequest.Builder().build();
            mInterstitialAd.loadAd(adRequest);
        }
    }

    @Override
    public void onClick(View v) {
        int id = v.getId();

        switch (id) {
            case R.id.btnJoin:
                if (mInterstitialAd.isLoaded()) {
                    mInterstitialAd.show();
                } else {
                    Log.d(TAG, "The interstitial wasn't loaded yet.");
                    visitFbGroup();
                }
                break;
            case R.id.btnDonate:
                visitDonateLink();
                break;
        }
    }

    private void visitFbGroup() {
        String url = "https://www.facebook.com/groups/292094057881113";
        Intent i = new Intent(Intent.ACTION_VIEW);
        i.setData(Uri.parse(url));
        startActivity(i);
    }

    private void visitDonateLink() {
        String url = "https://megacard.vn/nap-the-link/15171-chjmung";
        Intent i = new Intent(Intent.ACTION_VIEW);
        i.setData(Uri.parse(url));
        startActivity(i);
    }
}
