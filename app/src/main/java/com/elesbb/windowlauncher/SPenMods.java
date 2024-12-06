package com.elesbb.windowlauncher;

import android.app.ActivityOptions;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;

import de.robv.android.xposed.XC_MethodHook;
import de.robv.android.xposed.XposedBridge;
import de.robv.android.xposed.XposedHelpers;
import de.robv.android.xposed.callbacks.XC_LoadPackage;

public class SPenMods {

    static void Hook(XC_LoadPackage.LoadPackageParam loadPackageParam) {
        if (!loadPackageParam.packageName.equals("com.samsung.android.service.aircommand")) return;

        Class<?> i0 = XposedHelpers.findClass("a5.i0", loadPackageParam.classLoader),
                v8_a = XposedHelpers.findClass("v8.a", loadPackageParam.classLoader);

        XposedHelpers.findAndHookMethod(i0, "A", Context.class, v8_a, Intent.class, new XC_MethodHook() {
            @Override
            protected void beforeHookedMethod(MethodHookParam param) throws Throwable {
                XposedBridge.log("Launched application");
                Context context = (Context) param.args[0];
                Intent i = (Intent) param.args[2];
                ActivityOptions ops = ActivityOptions.makeBasic();
                int heightx = context.getResources().getDisplayMetrics().heightPixels / 2,
                        widthx = context.getResources().getDisplayMetrics().widthPixels / 2;
                int left = widthx - 250, right = widthx + 250, top = heightx - 350, bottom = heightx + 350;
//                ops.setLaunchBounds(new Rect(left, top, right, bottom));
                Bundle b = ops.toBundle();
                b.putInt("android.activity.windowingMode", 5);
                context.startActivity(i, b);
                param.setResult(null);
            }
        });
    }
}
