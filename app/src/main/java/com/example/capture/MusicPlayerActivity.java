package com.example.capture;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentManager;
import androidx.fragment.app.FragmentPagerAdapter;
import androidx.loader.app.LoaderManager;
import androidx.loader.content.CursorLoader;
import androidx.loader.content.Loader;
import androidx.recyclerview.widget.RecyclerView;
import androidx.viewpager.widget.ViewPager;

import android.database.Cursor;
import android.media.AudioManager;
import android.media.MediaPlayer;
import android.net.Uri;
import android.os.Bundle;
import android.provider.MediaStore;
import android.util.Log;
import android.view.View;

import com.example.capture.frags.ListViewFragment;
import com.example.capture.frags.PlayerFragment;
import com.example.capture.frags.SongFragment;
import com.example.capture.services.MusicService;
import com.google.android.material.tabs.TabLayout;

import org.greenrobot.eventbus.EventBus;
import org.greenrobot.eventbus.Subscribe;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

public class MusicPlayerActivity extends AppCompatActivity {
//        implements FragmentCallback {
    private static final int LOADER_ID = 10101;
    private PlayerFragment mPlayerFragment;
    private ListViewFragment mListViewFragment;
    private SongFragment mSongFragment;
    private static final String TAG = "음악플레이어 Activity";

    public static ArrayList<Uri> mSongList; // uri로 모든 음악정보 추출 가능?
    public TabLayout tabLayout;
    private ViewPager viewPager;
    private MediaPlayer mMediaPlayer;

    // MusicServiceInterface 추가 ------------
    private static MusicPlayerActivity mInstance;
    private MusicServiceInterface mInterface;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_music_player);

        tabLayout = (TabLayout) findViewById(R.id.tab);
        viewPager = (ViewPager) findViewById(R.id.view_pager);

        mSongList = new ArrayList<>();

        // 플레이어
        mPlayerFragment = new PlayerFragment();

        // 아티스트
        List<String> artistList = new ArrayList<>();
        for (int i = 0; i < 20; i++) {
            artistList.add("가수 " + i);
        }
        mListViewFragment = ListViewFragment.newInstance(artistList);

        // 노래
        mSongFragment = new SongFragment();

        MusicPlayerPagerAdapter adapter = new MusicPlayerPagerAdapter(getSupportFragmentManager());
        viewPager.setAdapter(adapter);

        tabLayout.setupWithViewPager(viewPager);

        viewPager.setCurrentItem(0);

//      ===========================================
        mMediaPlayer = new MediaPlayer();
        mMediaPlayer.setAudioStreamType(AudioManager.STREAM_MUSIC);

//      =========================================== connection
        mInterface = new MusicServiceInterface(getApplicationContext());
        mInstance = this;
    }

    public static MusicPlayerActivity getInstance() {
        return mInstance;
    }

    public MusicServiceInterface getServiceInterface() {
        return mInterface;
    }

//==========================================================================
// Interface callback
    @Subscribe
    public void setPage(Integer pageNum){
        viewPager.setCurrentItem(pageNum);
    }

    /** =========================================
     * EventBus 설정
     *
     */
    @Override
    protected void onStart() {
        super.onStart();
        // EventBus에 구독자로 현재 액티비티 추가
        EventBus.getDefault().register(this);
        Log.d(TAG, "시작됨");
    }

    @Override
    protected void onStop() {
        super.onStop();
        // EventBus에 구독자에서 제거
        EventBus.getDefault().unregister(this);
        Log.d(TAG, "종료됨");
    }
//    ===========================================
    /**
     * EventBus 에서 보내는 이벤트 수신하는 콜백 메서드
     * @param uri
     */
    @Subscribe
    public void playMusic(Uri uri){
        final Uri mUri = uri;
        try {
            if(isPlaying()) {
                mMediaPlayer.reset();
            }
            mMediaPlayer.setDataSource(this, uri);
            mMediaPlayer.prepareAsync();
            mMediaPlayer.setOnPreparedListener(new MediaPlayer.OnPreparedListener() {
                @Override
                public void onPrepared(MediaPlayer mp) {
                    mp.start();
                    Log.d(TAG, "재생 Uri : " + mUri);
                    /**
                     * {@link com.example.capture.frags.MusicControllerFragment#updatePlayButton(Boolean)}
                     */
                    EventBus.getDefault().post(isPlaying());
                }
            });
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
//    ===========================================

    @Subscribe
    public void stopPlayer(Integer integer){
        if(integer == 3) mPlayerFragment.stopPlayer();
    };

    @Subscribe
    public void clickPlayButton(View v){
        if(isPlaying()){
            mMediaPlayer.pause();
        } else {
            mMediaPlayer.start();
        }
        /**
         * {@link com.example.capture.frags.MusicControllerFragment#updatePlayButton(Boolean)}
         */
        EventBus.getDefault().post(isPlaying());
    }

    public boolean isPlaying(){
        if(mMediaPlayer != null){
            return mMediaPlayer.isPlaying();
        }
        return false;
    }

    private class MusicPlayerPagerAdapter extends FragmentPagerAdapter{

        public MusicPlayerPagerAdapter(@NonNull FragmentManager fm) {
            super(fm);
        }

        @NonNull
        @Override
        public Fragment getItem(int position) {
            switch (position) {
                case 0:
                    return mSongFragment;
                case 1:
                    return mPlayerFragment;
                case 2:
                    return mListViewFragment;
            }
            return null;
        }

        @Override
        public int getCount() {
            return 3;
        }

        @Override
        public CharSequence getPageTitle(int position) {
            switch (position) {
                case 0:
                    return "노래목록";
                case 1:
                    return "플레이어";
                case 2:
                    return "아티스트";
            }
            return null;
        }
    }

}
