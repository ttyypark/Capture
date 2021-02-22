package com.example.mediaplayer

import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import androidx.fragment.app.Fragment
import androidx.fragment.app.FragmentManager
import androidx.fragment.app.FragmentPagerAdapter
import androidx.viewpager.widget.ViewPager
import com.example.mediaplayer.frags.PhotoFragment
import com.example.mediaplayer.frags.PhotoViewFragment

class PhotosActivity constructor() : AppCompatActivity(), FragmentCallback {
    private var mPhotoFragment: PhotoFragment? = null
    private var mPhotoViewFragment: PhotoViewFragment? = null
    private lateinit var viewPager: ViewPager

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_photo)

//        TabLayout tabLayout = findViewById(R.id.tab);
        viewPager = findViewById(R.id.view_pager)

        // 포토
        mPhotoFragment = PhotoFragment()

        // 뷰어
        mPhotoViewFragment = PhotoViewFragment()
        val adapter = PhotoPagerAdapter(supportFragmentManager)
        viewPager.adapter = adapter

//        tabLayout.setupWithViewPager(viewPager);
        viewPager.currentItem = 0
    }

    public override fun setPage(pageNum: Int) {
        viewPager!!.currentItem = pageNum
    }

    private inner class PhotoPagerAdapter constructor(fm: FragmentManager) : FragmentPagerAdapter(fm) {
        public override fun getCount(): Int {
            return 2
        }

        public override fun getItem(position: Int): Fragment {
            when (position) {
                0 -> return (mPhotoFragment)!!
                1 -> return (mPhotoViewFragment)!!
            }
            return (mPhotoFragment)!!       //null
        }

        public override fun getPageTitle(position: Int): CharSequence? {
            when (position) {
                0 -> return "사진목록"
                1 -> return "사진보기"
            }
            return null
        } // *************************************************************************************
        //  recyclerView 의 PhotoFragment 가 안 보이는 이유, onBindViewHolder 가 안되는 이유 !!!!!!
        //  왜 이런 Override 가 있었지?
        //        @Override
        //        public boolean isViewFromObject(@NonNull View view, @NonNull Object object) {
        //            return false;
        //        }
    }
}