package com.example.mediaplayer

import android.app.Application

class MusicApplication : Application() {
    private var mInterface: MusicServiceInterface? = null
    override fun onCreate() {
        super.onCreate()
        mInterface = MusicServiceInterface(applicationContext)
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