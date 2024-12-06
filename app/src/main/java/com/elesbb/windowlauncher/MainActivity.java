package com.elesbb.windowlauncher;

import android.app.Activity;
import android.app.ActivityOptions;
import android.content.ComponentName;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.content.pm.ResolveInfo;
import android.content.res.Configuration;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Canvas;
import android.graphics.drawable.BitmapDrawable;
import android.graphics.drawable.Drawable;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.util.Log;
import android.util.TypedValue;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.GridView;
import android.widget.TextView;

import androidx.annotation.NonNull;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.nio.file.Files;
import java.util.ArrayList;
import java.util.Base64;
import java.util.Comparator;
import java.util.List;

public class MainActivity extends Activity {

    private final Handler mHandler = new Handler(Looper.getMainLooper());
    static File SaveLocation = null;
    private ArrayAdapter<MyObject> adapter;
    private ArrayList<MyObject> activities = new ArrayList<>();

    private GridView allAppsView;

    static SharedPreferences myPrefs;

    @Override
    protected void onResume() {
        super.onResume();
        if (!SaveLocation.exists()) {

            new Thread() {
                @Override
                public void run() {
                    ArrayList<ResolveInfo> activitiesRI = new ArrayList<>();
                    Intent i = new Intent(Intent.ACTION_MAIN);
                    i.addCategory(Intent.CATEGORY_LAUNCHER);
                    List<ResolveInfo> tmp =  getPackageManager().queryIntentActivities(i, PackageManager.MATCH_ALL);
                    activitiesRI.addAll(tmp);

                    for (ResolveInfo info : activitiesRI) {
                        String activityName = info.activityInfo.name,
                                packageName = info.activityInfo.packageName,
                                activityLabel = info.activityInfo.loadLabel(getPackageManager()).toString();

                        Drawable ico = info.activityInfo.loadIcon(getPackageManager());
                        Bitmap btm = getBitmapFromDrawable(ico);

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

                    mHandler.post(new Runnable() {
                        @Override
                        public void run() {
                            adapter.notifyDataSetChanged();
                            SaveCache(activities);
                        }
                    });
                }
            }.start();
        } else {
            try {
                ObjectInputStream ois = new ObjectInputStream(Files.newInputStream(SaveLocation.toPath()));
                activities = (ArrayList<MyObject>) ois.readObject();
                adapter.notifyDataSetChanged();
            } catch (Exception e) {
                Log.e("ELESBB", "Failed loading cached apps", e);
            }
        }
    }

    @Override
    public void onConfigurationChanged(@NonNull Configuration newConfig) {
        super.onConfigurationChanged(newConfig);
        SetMaxColumns(newConfig);
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        int iconBounds = (int)TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_DIP, 70, getResources().getDisplayMetrics());
        myPrefs = getSharedPreferences("cached", MODE_PRIVATE);
        SaveLocation = new File(getFilesDir(), "cached");
        setContentView(R.layout.activity_main);


        allAppsView = findViewById(R.id.all_apps_gridview);

        adapter = new ArrayAdapter<MyObject>(MainActivity.this, android.R.layout.simple_list_item_1, activities) {
            @Override
            public View getView(int position, View convertView, ViewGroup parent) {
                if (convertView == null) {
                    convertView = LayoutInflater.from(MainActivity.this).inflate(android.R.layout.simple_list_item_1, null, false);
                }

                TextView tv = (TextView) convertView;
                tv.setLines(2);

                if (position >= activities.size()) {
                    return tv;
                }

                MyObject info = activities.get(position);
                try {
                    String label = info.GetActivityLabel();
                    byte[] buf = Base64.getDecoder().decode(info.GetIcon());
                    Drawable icon = new BitmapDrawable(BitmapFactory.decodeByteArray(buf, 0, buf.length));
                    icon.setBounds(0, 0, iconBounds, iconBounds);
                    tv.setText(label);
                    tv.setPadding(0,0,0,0);
                    tv.setGravity(Gravity.CENTER_HORIZONTAL);
                    tv.setCompoundDrawables(null, icon, null, null);

                } catch (Exception e) {
                    Log.e("ELESBB", "Failed to load label/icon", e);
                }

                return tv;
            }

            @Override
            public int getCount() {
                return activities.size();
            }
        };

        allAppsView.setAdapter(adapter);



        allAppsView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                MyObject app = activities.get(position);
                Intent i = new Intent();
                i.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
                i.setComponent(new ComponentName(app.GetPackageName(), app.GetActivityName()));
                Bundle ops = ActivityOptions.makeBasic().toBundle();
                ops.putInt("android.activity.windowingMode", 5);
                startActivity(i, ops);
                finish();
//                moveTaskToBack(true);
            }
        });
    }

    @Override
    protected void onStart() {
        super.onStart();
        Configuration config = getResources().getConfiguration();
        SetMaxColumns(config);
    }

    final void SetMaxColumns(Configuration config) {
        int num = (int)Math.floor(config.screenWidthDp / 80d);
        allAppsView.setNumColumns(num);
        allAppsView.invalidate();
        allAppsView.requestLayout();
    }

    static void SaveCache(ArrayList<MyObject> fetched) {
        new Thread() {
            @Override
            public void run() {
                try {
                    ObjectOutputStream out = new ObjectOutputStream(Files.newOutputStream(SaveLocation.toPath()));
                    out.writeObject(fetched);
                    out.flush();
                    out.close();
                } catch (Exception e) {
                    Log.e("ELESBB", "Failed to write array list", e);
                }
            }
        }.start();
    }

    static Bitmap getBitmapFromDrawable(Drawable drawable) {
        final Bitmap bmp = Bitmap.createBitmap(drawable.getIntrinsicWidth(), drawable.getIntrinsicHeight(), Bitmap.Config.ARGB_8888);
        final Canvas canvas = new Canvas(bmp);
        drawable.setBounds(0, 0, canvas.getWidth(), canvas.getHeight());
        drawable.draw(canvas);
        return bmp;
    }
}