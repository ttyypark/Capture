package com.example.capture;

import android.media.AudioManager;
import android.media.MediaPlayer;
import android.os.Bundle;


import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentManager;
import androidx.fragment.app.FragmentPagerAdapter;
import androidx.viewpager.widget.ViewPager;

import android.view.View;

import com.example.capture.frags.PlayerFragment;
import com.example.capture.frags.SongFragment;
import com.example.capture.frags.VideoFragment;
import com.example.capture.frags.VideoPlayerFragment;
import com.google.android.material.tabs.TabLayout;

public class VideoPlayerActivity extends AppCompatActivity
        implements FragmentCallback {
    private VideoFragment mVideoFragment;
    private VideoPlayerFragment mVideoPlayerFragment;

    private ViewPager viewPager;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_video_player);

        TabLayout tabLayout = findViewById(R.id.tab);
        viewPager = findViewById(R.id.view_pager);

        // 플레이어
        mVideoPlayerFragment = new VideoPlayerFragment();

        // 비디오
        mVideoFragment = new VideoFragment();

        VideoPlayerPagerAdapter adapter = new VideoPlayerPagerAdapter(getSupportFragmentManager());
        viewPager.setAdapter(adapter);

        tabLayout.setupWithViewPager(viewPager);

//      ===========================================
        MediaPlayer mMediaPlayer = new MediaPlayer();

    }

    @Override
    public void setPage(int pageNum) {   // event bus 아님
        viewPager.setCurrentItem(pageNum);
    }
//    public void stopPlayer(){
//    };


    private class VideoPlayerPagerAdapter extends FragmentPagerAdapter {
        public VideoPlayerPagerAdapter(@NonNull FragmentManager fm) {
            super(fm);
        }

        @NonNull
        @Override
        public Fragment getItem(int position) {
            switch (position) {
                case 0:
                    return mVideoFragment;
                case 1:
                    return mVideoPlayerFragment;
            }
            return null;
        }

        @Override
        public int getCount() {
            return 2;
        }

        @Override
        public CharSequence getPageTitle(int position) {
            switch (position) {
                case 0:
                    return "비디오목록";
                case 1:
                    return "비디오 플레이어";
            }
            return null;
        }

    }
}
