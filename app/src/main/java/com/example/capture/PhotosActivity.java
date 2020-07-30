package com.example.capture;

import android.os.Bundle;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentManager;
import androidx.fragment.app.FragmentPagerAdapter;
import androidx.viewpager.widget.ViewPager;

import com.example.capture.frags.PhotoFragment;
import com.example.capture.frags.PhotoViewFragment;

public class PhotosActivity extends AppCompatActivity
        implements FragmentCallback {
    private PhotoFragment mPhotoFragment;
    private PhotoViewFragment mPhotoViewFragment;
    private ViewPager viewPager;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_photo);

//        TabLayout tabLayout = findViewById(R.id.tab);
        viewPager = findViewById(R.id.view_pager);

        // 포토
        mPhotoFragment = new PhotoFragment();

        // 뷰어
        mPhotoViewFragment = new PhotoViewFragment();

        PhotoPagerAdapter adapter = new PhotoPagerAdapter(getSupportFragmentManager());
        viewPager.setAdapter(adapter);

//        tabLayout.setupWithViewPager(viewPager);

        viewPager.setCurrentItem(0);

    }

    @Override
    public void setPage(int pageNum) {
        viewPager.setCurrentItem(pageNum);
    }


    private class PhotoPagerAdapter extends FragmentPagerAdapter {

        public PhotoPagerAdapter(@NonNull FragmentManager fm) {
            super(fm);
        }

        @Override
        public int getCount() {
            return 2;
        }


        @NonNull
        @Override
        public Fragment getItem(int position) {
            switch (position) {
                case 0:
                    return mPhotoFragment;
                case 1:
                    return mPhotoViewFragment;
            }
            return null;
        }

        @Override
        public CharSequence getPageTitle(int position) {
            switch (position) {
                case 0:
                    return "사진목록";
                case 1:
                    return "사진보기";
            }
            return null;
        }
// *************************************************************************************
//  recyclerView 의 PhotoFragment 가 안 보이는 이유, onBindViewHolder 가 안되는 이유 !!!!!!
//  왜 이런 Override 가 있었지?
//        @Override
//        public boolean isViewFromObject(@NonNull View view, @NonNull Object object) {
//            return false;
//        }

    }
}
