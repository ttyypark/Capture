package com.example.capture;

import android.app.Application;

public class MusicApplication extends Application {
    private static MusicApplication mInstance;
    private MusicServiceInterface mInterface;

    @Override
    public void onCreate() {
        super.onCreate();
        mInterface = new MusicServiceInterface(getApplicationContext());
        mInstance = this;
    }

    public static MusicApplication getInstance() {
        return mInstance;
    }

    public MusicServiceInterface getServiceInterface() {
        return mInterface;
    }
}
