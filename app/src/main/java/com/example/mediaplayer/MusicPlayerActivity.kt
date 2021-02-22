package com.example.mediaplayer

import android.media.AudioAttributes
import android.media.MediaPlayer
import android.os.Build
import android.os.Bundle
import android.util.Log
import android.view.*
import androidx.appcompat.app.AppCompatActivity
import androidx.core.app.ActivityCompat
import androidx.fragment.app.Fragment
import androidx.fragment.app.FragmentManager
import androidx.fragment.app.FragmentPagerAdapter
import androidx.viewpager.widget.ViewPager
import com.example.mediaplayer.frags.ListViewFragment
import com.example.mediaplayer.frags.MusicFragment
import com.example.mediaplayer.frags.MusicPlayerFragment
import com.google.android.material.tabs.TabLayout
import org.greenrobot.eventbus.EventBus
import org.greenrobot.eventbus.Subscribe
import java.util.*
import kotlin.system.exitProcess

// Fragment updateUI 정의?
//
class MusicPlayerActivity : AppCompatActivity(), FragmentCallback {
    private var mMusicPlayerFragment: MusicPlayerFragment? = null
    private var mListViewFragment: ListViewFragment? = null
    private var mMusicFragment: MusicFragment? = null

    //    public static ArrayList<Uri> mSongList; // uri로 모든 음악정보 추출 가능?
    private var tabLayout: TabLayout? = null
    private var viewPager: ViewPager? = null
    private var mMediaPlayer: MediaPlayer? = null

    private var mInterface: MusicServiceInterface? = null


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_music_player)
        tabLayout = findViewById<View>(R.id.tab) as TabLayout?
        viewPager = findViewById<View>(R.id.view_pager) as ViewPager?

        // 플레이어
        mMusicPlayerFragment = MusicPlayerFragment()

        // 아티스트
        val artistList: MutableList<String> = ArrayList()
        for (i in 0..19) {
            artistList.add("가수 $i")
        }
        mListViewFragment = ListViewFragment.newInstance(artistList)

        // 노래
        mMusicFragment = MusicFragment()
        val adapter = MusicPlayerPagerAdapter(supportFragmentManager)
        viewPager!!.adapter = adapter
        tabLayout!!.setupWithViewPager(viewPager)
        viewPager!!.currentItem = 0

//      ===========================================
        mMediaPlayer = MediaPlayer()

//        mMediaPlayer.setAudioStreamType(AudioManager.STREAM_MUSIC);
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
            mMediaPlayer!!.setAudioAttributes(
                    AudioAttributes.Builder()
                            .setContentType(AudioAttributes.CONTENT_TYPE_MUSIC)
                            .build())
        }

        //      =========================================== connection
        mInterface = MusicServiceInterface(applicationContext)
        mInstance = this
    }

    fun getServiceInterface(): MusicServiceInterface? {
        return mInterface
    }

    //==========================================================================
    // Interface callback
    override fun setPage(pageNum: Int) {
        viewPager!!.currentItem = pageNum
    }

    /** =========================================
     * EventBus 설정
     *
     */
    override fun onStart() {
        super.onStart()
        // EventBus에 구독자로 현재 액티비티 추가
        EventBus.getDefault().register(this)
        Log.d(TAG, "시작됨")
    }

    override fun onStop() {
        super.onStop()
        // EventBus에 구독자에서 제거
        EventBus.getDefault().unregister(this)
        Log.d(TAG, "종료됨")
    }

    ////    ===========================================
    //    /**
    //     * EventBus 에서 보내는 이벤트 수신하는 콜백 메서드
    //     * @param uri
    //     */
    //    @Subscribe
    //    public void playMusic(Uri uri){
    //        final Uri mUri = uri;
    //        try {
    //            if(isPlaying()) {
    //                mMediaPlayer.reset();
    //            }
    //            mMediaPlayer.setDataSource(this, uri);
    //            mMediaPlayer.prepareAsync();
    //            mMediaPlayer.setOnPreparedListener(new MediaPlayer.OnPreparedListener() {
    //                @Override
    //                public void onPrepared(MediaPlayer mp) {
    //                    mp.start();
    //                    Log.d(TAG, "재생 Uri : " + mUri);
    //                    /**
    //                     * {@link com.example.capture.frags.MusicControllerFragment#updateUI(Boolean)} (Boolean)}
    //                     */
    //                    /**
    //                     * {@link com.example.capture.frags.PlayerFragment#updateUI(Boolean)} (Boolean)}
    //                     */
    //                    EventBus.getDefault().post(isPlaying());
    //                }
    //            });
    //        } catch (IOException e) {
    //            e.printStackTrace();
    //        }
    //    }
    ////    ===========================================
    @Subscribe
    fun stopPlayer(integer: Int) {
        if (integer == 3) {
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.JELLY_BEAN) {
                finishAffinity()
            } else {
                ActivityCompat.finishAffinity(parent)
            }
            System.runFinalization()
            exitProcess(0)
        }
    }

    private inner class MusicPlayerPagerAdapter constructor(fm: FragmentManager) : FragmentPagerAdapter(fm) {
        override fun getItem(position: Int): Fragment {
            when (position) {
                0 -> return (mMusicFragment)!!
                1 -> return (mMusicPlayerFragment)!!
                2 -> return (mListViewFragment)!!
            }
            return (mMusicFragment)!!  //  null
        }

        override fun getCount(): Int {
            return 3
        }

        override fun getPageTitle(position: Int): CharSequence? {
            when (position) {
                0 -> return "노래목록"
                1 -> return "플레이어"
                2 -> return "아티스트"
            }
            return null
        }
    }

    companion object {
        private const val LOADER_ID: Int = 10101
        private const val TAG: String = "음악플레이어 Activity"

        // MusicServiceInterface 추가 ------------
        private var mInstance: MusicPlayerActivity? = null
        fun getInstance(): MusicPlayerActivity? {
            return mInstance
        }
    }
}