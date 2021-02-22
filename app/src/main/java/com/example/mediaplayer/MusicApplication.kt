package com.example.mediaplayer

import android.app.Application

class MusicApplication constructor() : Application() {
    private var mInterface: MusicServiceInterface? = null
    override fun onCreate() {
        super.onCreate()
        mInterface = MusicServiceInterface(getApplicationContext())
        mInstance = this
    }

    fun getServiceInterface(): MusicServiceInterface? {
        return mInterface
    }

    companion object {
        private var mInstance: MusicApplication? = null
        fun getInstance(): MusicApplication? {
            return mInstance
        }
    }
}