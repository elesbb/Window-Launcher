package com.elesbb.windowlauncher;

import android.app.Activity;
import android.app.ActivityOptions;
import android.content.Intent;
import android.os.Bundle;

public class LauncherActivity extends Activity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        Intent i = new Intent(LauncherActivity.this, MainActivity.class);
        i.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
        Bundle b = ActivityOptions.makeBasic().toBundle();
        b.putInt("android.activity.windowingMode", 5);
        startActivity(i, b);
        finish();
    }
}
