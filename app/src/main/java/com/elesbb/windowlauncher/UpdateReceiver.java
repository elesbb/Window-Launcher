package com.elesbb.windowlauncher;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.content.pm.ResolveInfo;
import android.graphics.Bitmap;
import android.graphics.drawable.Drawable;
import android.util.Log;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.util.ArrayList;
import java.util.Base64;
import java.util.Comparator;
import java.util.List;

public class UpdateReceiver extends BroadcastReceiver {

    @Override
    public void onReceive(Context context, Intent intent) {
        try {
            Log.e("ELESBB", "Got update - " + intent.getAction());
        } catch (Exception ignored) {}
        if (intent != null && intent.getAction() != null && (intent.getAction().equals(Intent.ACTION_PACKAGE_ADDED) ||
                intent.getAction().equals(Intent.ACTION_PACKAGE_REMOVED) || intent.getAction().equals(Intent.ACTION_PACKAGE_CHANGED) ||
                intent.getAction().equals(Intent.ACTION_PACKAGE_FULLY_REMOVED)) ) {
            Log.e("ELESBB", "Got action: " + intent.getAction());
            MainActivity.SaveLocation = new File(context.getFilesDir(), "cached");

            new Thread() {
                @Override
                public void run() {
                    ArrayList<ResolveInfo> activitiesRI = new ArrayList<>();
                    ArrayList<MyObject> activities = new ArrayList<>();
//                    Log.e("ELESBB", "Starting thread...");
                    Intent i = new Intent(Intent.ACTION_MAIN);
                    i.addCategory(Intent.CATEGORY_LAUNCHER);
                    List<ResolveInfo> tmp = context.getPackageManager().queryIntentActivities(i, PackageManager.MATCH_ALL);
                    activitiesRI.addAll(tmp);
//                activitiesRI.sort(new Comparator<ResolveInfo>() {
//                    @Override
//                    public int compare(ResolveInfo o1, ResolveInfo o2) {
//                        String label1 = o1.loadLabel(getPackageManager()).toString(), label2 = o2.loadLabel(getPackageManager()).toString();
//                        return label1.compareToIgnoreCase(label2);
//                    }
//                });

                    for (ResolveInfo info : activitiesRI) {
                        String activityName = info.activityInfo.name,
                                packageName = info.activityInfo.packageName,
                                activityLabel = info.activityInfo.loadLabel(context.getPackageManager()).toString();

                        Drawable ico = info.activityInfo.loadIcon(context.getPackageManager());
                        Bitmap btm = MainActivity.getBitmapFromDrawable(ico);

                        ByteArrayOutputStream out = new ByteArrayOutputStream();
                        btm.compress(Bitmap.CompressFormat.PNG, 100, out);
                        String iconString = Base64.getEncoder().encodeToString(out.toByteArray());
                        MyObject myObject = new MyObject(packageName, activityName, iconString, activityLabel);
                        activities.add(myObject);
                    }

                    activities.sort(new Comparator<MyObject>() {
                        @Override
                        public int compare(MyObject o1, MyObject o2) {
                            return o1.GetActivityLabel().compareToIgnoreCase(o2.GetActivityLabel());
                        }
                    });

                    MainActivity.SaveCache(activities);
                }
            }.start();
        }
    }
}
