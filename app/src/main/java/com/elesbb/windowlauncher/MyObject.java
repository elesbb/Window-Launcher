package com.elesbb.windowlauncher;

import java.io.Serializable;

public class MyObject implements Serializable {
    private String pkgName, activityName, icon, activityLabel;
    public MyObject(String packageName, String activityName, String icon, String activityLabel) {
        this.pkgName = packageName;
        this.activityLabel = activityLabel;
        this.activityName = activityName;
        this.icon = icon;
    }

    String GetPackageName() {
        return this.pkgName;
    }

    String GetActivityName() {
        return this.activityName;
    }

    String GetIcon() {
        return this.icon;
    }

    String GetActivityLabel() {
        return this.activityLabel;
    }
}
