package com.example.mediaplayer

import android.content.ComponentName
import android.content.Context
import android.content.Intent
import android.content.ServiceConnection
import android.net.Uri
import android.os.Build
import android.os.IBinder
import android.util.Log
import androidx.annotation.RequiresApi
import com.example.mediaplayer.AudioAdapter.AudioItem
import com.example.mediaplayer.services.MusicService
import com.example.mediaplayer.services.MusicService.LocalBinder
import java.util.*

class MusicServiceInterface constructor(context: Context) {
    private var mServiceConnection: ServiceConnection?
    var mService: MusicService? = null
//    fun setmSongList(songList: ArrayList<Uri>) {
//        if (mService != null) {
//            mService!!.setmSongList(songList)
//        }
//    }

    fun isPlaying(): Boolean {
        if (mService != null) {
            return mService!!.isPlaying()
        }
        return false
    }

    fun play(uri: Uri?) {
        if (mService != null) {
            mService!!.playMusic(uri)
        }
    }

    fun nextMusic() {
        if (mService != null) {
            mService!!.nextMusic()
        }
    }

    fun prevMusic() {
        if (mService != null) {
            mService!!.prevMusic()
        }
    }

    fun clickResumeButton() {
        if (mService != null) {
            mService!!.clickResumeButton()
        }
    }

    // 사용 안함
    //    public AudioAdapter.AudioItem getAudioItem() {
    //        if (mService != null) {
    //            return mService.getAudioItem();
    //        }
    //        return null;
    //    }
//    @RequiresApi(Build.VERSION_CODES.Q)
//    fun queryAudioItem(position: Int) {
//        if (mService != null) {
//            mService!!.queryAudioItem(position)
//        }
//    }

    fun mAudioItem(): AudioItem? {
        if (mService != null) {
            return mService!!.mAudioItem
        }
        return null
    }

    init {
        mServiceConnection = object : ServiceConnection {
            override fun onServiceConnected(name: ComponentName, service: IBinder) {
                mService = (service as LocalBinder).getService()
                Log.d("Interface", "서비스 연결")
            }

            override fun onServiceDisconnected(name: ComponentName) {
                mServiceConnection = null
                mService = null
                Log.d("Interface", "서비스 연결 해제")
            }
        }
        context.bindService(Intent(context, MusicService::class.java)
                .setPackage(context.packageName), mServiceConnection as ServiceConnection, Context.BIND_AUTO_CREATE)
    }
}