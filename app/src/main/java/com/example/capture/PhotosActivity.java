package com.example.capture;

import android.os.Bundle;
import android.view.View;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentManager;
import androidx.fragment.app.FragmentPagerAdapter;
import androidx.viewpager.widget.ViewPager;

import com.example.capture.frags.PhotoFragment;
import com.example.capture.frags.PhtoViewFragment;
import com.google.android.material.tabs.TabLayout;

public class PhotosActivity extends AppCompatActivity {
    private PhotoFragment mPhotoFragment;
    private PhtoViewFragment mPhotoViewFragment;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_photo);

        TabLayout tabLayout = findViewById(R.id.tab);
        ViewPager viewPager = findViewById(R.id.view_pager);

        // 포토
        mPhotoFragment = new PhotoFragment();

        // 뷰어
        mPhotoViewFragment = new PhtoViewFragment();

        PhtoPagerAdapter adapter = new PhtoPagerAdapter(getSupportFragmentManager());
        viewPager.setAdapter(adapter);

        tabLayout.setupWithViewPager(viewPager);


    }

    private class PhtoPagerAdapter extends FragmentPagerAdapter {

        public PhtoPagerAdapter(@NonNull FragmentManager fm) {
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

        @Override
        public boolean isViewFromObject(@NonNull View view, @NonNull Object object) {
            return false;
        }
    }
}
