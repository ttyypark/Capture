package com.example.capture;

import android.app.Activity;
import android.app.Application;
import android.os.Bundle;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

public class Foreground implements Application.ActivityLifecycleCallbacks {
    private static Foreground instance;
    public static void init(Application app) {
        if (instance == null) {
            instance = new Foreground();
            app.registerActivityLifecycleCallbacks(instance);
        }
    }

    public static Foreground get() {
        return instance;
    }

    public Foreground() {
    }

    private static AppStatus mAppStatus;

    public AppStatus getAppStatus() {
        return mAppStatus;
    }

    // check if app is return foreground
     public static boolean isBackground() {
        return mAppStatus.ordinal() == AppStatus.BACKGROUND.ordinal();
    }

    public enum AppStatus {
        BACKGROUND, // app is background
        RETURNED_TO_FOREGROUND, // app returned to foreground(or first launch)
        FOREGROUND; // app is foreground
    }

    // running activity count
    private int running = 0;

    @Override
    public void onActivityCreated(@NonNull Activity activity, @Nullable Bundle savedInstanceState) {
    }

    @Override
    public void onActivityStarted(@NonNull Activity activity) {
        if (++running == 1) {
            mAppStatus = AppStatus.RETURNED_TO_FOREGROUND;        }
        else if (running > 1) {
            mAppStatus = AppStatus.FOREGROUND;
        }
    }

    @Override
    public void onActivityResumed(@NonNull Activity activity) {

    }

    @Override
    public void onActivityPaused(@NonNull Activity activity) {

    }

    @Override
    public void onActivityStopped(@NonNull Activity activity) {
        if (--running == 0) {
            mAppStatus = AppStatus.BACKGROUND;
        }
    }

    @Override
    public void onActivitySaveInstanceState(@NonNull Activity activity, @NonNull Bundle outState) {

    }

    @Override
    public void onActivityDestroyed(@NonNull Activity activity) {

    }
}
