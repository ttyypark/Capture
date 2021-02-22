package com.example.mediaplayer

import android.app.Activity
import android.app.Application
import android.app.Application.ActivityLifecycleCallbacks
import android.os.Bundle

class Foreground constructor() : ActivityLifecycleCallbacks {
    fun getAppStatus(): AppStatus? {
        return mAppStatus
    }

    enum class AppStatus {
        BACKGROUND,  // app is background
        RETURNED_TO_FOREGROUND,  // app returned to foreground(or first launch)
        FOREGROUND // app is foreground
    }

    // running activity count
    private var running: Int = 0
    public override fun onActivityCreated(activity: Activity, savedInstanceState: Bundle?) {}
    public override fun onActivityStarted(activity: Activity) {
        if (++running == 1) {
            mAppStatus = AppStatus.RETURNED_TO_FOREGROUND
        } else if (running > 1) {
            mAppStatus = AppStatus.FOREGROUND
        }
    }

    public override fun onActivityResumed(activity: Activity) {}
    public override fun onActivityPaused(activity: Activity) {}
    public override fun onActivityStopped(activity: Activity) {
        if (--running == 0) {
            mAppStatus = AppStatus.BACKGROUND
        }
    }

    public override fun onActivitySaveInstanceState(activity: Activity, outState: Bundle) {}
    public override fun onActivityDestroyed(activity: Activity) {}

    companion object {
        private var instance: Foreground? = null
        fun init(app: Application) {
            if (instance == null) {
                instance = Foreground()
                app.registerActivityLifecycleCallbacks(instance)
            }
        }

        fun get(): Foreground? {
            return instance
        }

        private var mAppStatus: AppStatus? = null

        // check if app is return foreground
        fun isBackground(): Boolean {
            return mAppStatus!!.ordinal == AppStatus.BACKGROUND.ordinal
        }
    }
}