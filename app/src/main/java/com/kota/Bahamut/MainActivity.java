package com.kota.Bahamut;

import android.os.Bundle;

import androidx.appcompat.app.AppCompatActivity;

import com.kota.bahamut_bbs_2.R;

public class MainActivity extends AppCompatActivity {
    /* access modifiers changed from: protected */
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView((int) R.layout.activity_main);
        BahamutController BahamutController = new BahamutController();
        BahamutController.onControllerWillLoad();
    }
}
