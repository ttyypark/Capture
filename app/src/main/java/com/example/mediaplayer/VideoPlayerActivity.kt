package com.example.mediaplayer

import android.media.MediaPlayer
import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import androidx.fragment.app.Fragment
import androidx.fragment.app.FragmentManager
import androidx.fragment.app.FragmentPagerAdapter
import androidx.viewpager.widget.ViewPager
import com.example.mediaplayer.frags.VideoFragment
import com.example.mediaplayer.frags.VideoPlayerFragment
import com.google.android.material.tabs.TabLayout

class VideoPlayerActivity : AppCompatActivity(), FragmentCallback {
    private var mVideoFragment: VideoFragment? = null
    private var mVideoPlayerFragment: VideoPlayerFragment? = null
    private lateinit var viewPager: ViewPager
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_video_player)
        val tabLayout: TabLayout = findViewById(R.id.tab)
        viewPager = findViewById(R.id.view_pager)

        // 플레이어
        mVideoPlayerFragment = VideoPlayerFragment()

        // 비디오
        mVideoFragment = VideoFragment()
        val adapter = VideoPlayerPagerAdapter(supportFragmentManager)
        viewPager.adapter = adapter
        tabLayout.setupWithViewPager(viewPager)

//      ===========================================
        val mMediaPlayer = MediaPlayer()
    }

    override fun setPage(pageNum: Int) {   // event bus 아님
        viewPager.currentItem = pageNum
    }

    //    public void stopPlayer(){
    //    };
    private inner class VideoPlayerPagerAdapter constructor(fm: FragmentManager) : FragmentPagerAdapter(fm) {
        override fun getItem(position: Int): Fragment {
            when (position) {
                0 -> return (mVideoFragment)!!
                1 -> return (mVideoPlayerFragment)!!
            }
            return (mVideoFragment)!!   //null
        }

        override fun getCount(): Int {
            return 2
        }

        override fun getPageTitle(position: Int): CharSequence? {
            when (position) {
                0 -> return "비디오목록"
                1 -> return "비디오 플레이어"
            }
            return null
        }
    }
}