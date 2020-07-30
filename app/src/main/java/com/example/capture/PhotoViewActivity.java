package com.example.capture;

import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.ImageView;

import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;

import com.bumptech.glide.Glide;

public class PhotoViewActivity extends AppCompatActivity implements View.OnClickListener {
    private Context mContext;
    private PhotoAdapter.PhotoItem mItem;
    private String mPath;
    private ImageView mImageView;

    @Override
    public void onEnterAnimationComplete() {
        super.onEnterAnimationComplete();
        setInit();
    }

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_photoview);

        mContext = this;
        getData();
    }

    private void getData(){
        Intent intent = getIntent();
//        mItem = (PhotoAdapter.PhotoItem) intent.getSerializableExtra("item");
        mPath = intent.getStringExtra("item");
    }

    private void setInit(){
        mImageView = findViewById(R.id.photoview);
        Glide.with(mContext).load(mPath).into(mImageView);
    }

    @Override
    public void onClick(View v) {

    }
}
