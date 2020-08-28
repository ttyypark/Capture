package com.example.capture;

import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.ServiceConnection;
import android.net.Uri;
import android.os.IBinder;
import android.util.Log;

import com.example.capture.services.MusicService;

import java.util.ArrayList;

public class MusicServiceInterface {
    private ServiceConnection mServiceConnection;
    public MusicService mService;

    public MusicServiceInterface(Context context) {
        mServiceConnection = new ServiceConnection(){
            @Override
            public void onServiceConnected(ComponentName name, IBinder service) {
                mService = ((MusicService.LocalBinder) service).getService();
                Log.d("Interface" , "서비스 연결");
            }

            @Override
            public void onServiceDisconnected(ComponentName name) {
                mServiceConnection = null;
                mService = null;
                Log.d("Interface" , "서비스 연결 해제");
            }
        };
        context.bindService(new Intent(context, MusicService.class)
                .setPackage(context.getPackageName()), mServiceConnection, Context.BIND_AUTO_CREATE);
    }

    public void setmSongList(ArrayList<Uri> songList) {
        if (mService != null) {
            mService.setmSongList(songList);
        }
    }

    public boolean isPlaying() {
        if (mService != null) {
            return mService.isPlaying();
        }
        return false;
    }

    public void play(Uri uri){
        if (mService != null) {
            mService.playMusic(uri);
        }
    }

    public void nextMusic(){
        if (mService != null) {
            mService.nextMusic();
        }
    }

    public void prevMusic(){
        if (mService != null) {
            mService.prevMusic();
        }
    }

    public void clickResumeButton(){
        if (mService != null) {
            mService.clickResumeButton();
        }
    }


    // 사용 안함
//    public AudioAdapter.AudioItem getAudioItem() {
//        if (mService != null) {
//            return mService.getAudioItem();
//        }
//        return null;
//    }

    public void queryAudioItem(int position) {
        if (mService != null) {
            mService.queryAudioItem(position);
        }
    }

    public AudioAdapter.AudioItem mAudioItem() {
        if (mService != null) {
            return mService.mAudioItem;
        }
        return null;
    }

}
