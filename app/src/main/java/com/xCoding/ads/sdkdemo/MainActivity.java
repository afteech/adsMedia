package com.xCoding.ads.sdkdemo;

import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;

import android.os.Bundle;

import com.xCoding.ads.sdk.format.AdNetwork;
import com.xCoding.ads.sdk.format.BannerAd;
import com.xCoding.ads.sdk.format.InterstitialAd;
import com.xCoding.ads.sdk.format.NativeAd;

public class MainActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
    }

}