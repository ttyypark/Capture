package com.example.capture;

import android.content.Intent;
import android.os.Build;
import android.os.Bundle;
import android.os.Parcelable;
import android.util.Log;
import android.view.View;
import android.widget.Toast;

import androidx.annotation.Nullable;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityOptionsCompat;
import androidx.fragment.app.DialogFragment;
import androidx.recyclerview.widget.GridLayoutManager;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.example.capture.frags.EditDialogFragment;

import java.io.Serializable;

public class PhotoGalleryActivity extends AppCompatActivity
        implements EditDialogFragment.OnCompleteListener {
    private String TAG = "포토 갤러리";
    private View selectView;

    // callBack 함수
    @Override
    public void onInputedData(PhotoAdapter.PhotoItem item) {
        Toast.makeText(this, item.mDISPLAY_NAME,Toast.LENGTH_LONG).show();

        transition(selectView, item);
    }

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_photogallery);
        Log.e(TAG , "onCreate");

        RecyclerView recyclerView = findViewById(R.id.recycler_photo);
        PhotoAdapter mPhotoAdapter = new PhotoAdapter(getApplicationContext(),R.layout.photo_item);

        //       item click interface
        mPhotoAdapter.setOnItemClickListener(new PhotoAdapter.onItemClickListener(){
            @Override
            public void onItemClicked(PhotoAdapter.PhotoItem item, View view) {
//                Toast.makeText(getApplicationContext(), "아이템 선택됨: " + item.imgPath, Toast.LENGTH_SHORT).show();
//                if(item.mDate == "Title") {
//                    // input dialog
//                    // https://webnautes.tistory.com/m/1094

//                    dataModifyDialog(item);       // dialogFragment 사용......
                    selectView = view;
//                } else {
                    transition(view, item);        //  transition - PhotoViewActivity 에서
                                                   //  AlertDialog.Builder(this) 사용......
//                }
            }
        });

        recyclerView.setAdapter(mPhotoAdapter);
        Log.i(TAG, "사진 수 : " + mPhotoAdapter.getItemCount());

        GridLayoutManager layoutManager = new GridLayoutManager(getApplicationContext(), 3);
        layoutManager.setOrientation(LinearLayoutManager.VERTICAL);
        recyclerView.setLayoutManager(layoutManager);
    }

    public void dataModifyDialog(PhotoAdapter.PhotoItem item){
        // item data 실어 보내기
        Bundle args = new Bundle();
        args.putSerializable("photoItem", item);

        DialogFragment newFragment = new EditDialogFragment();
        newFragment.setArguments(args);

        newFragment.show(getSupportFragmentManager(), "dialog");
    }

    private void transition(View view, PhotoAdapter.PhotoItem item) {
        if (Build.VERSION.SDK_INT < 21) {
            Toast.makeText(PhotoGalleryActivity.this, "21+ only, keep out", Toast.LENGTH_SHORT).show();
        } else {
            Intent intent = new Intent(PhotoGalleryActivity.this, PhotoViewActivity.class);
            intent.putExtra("path", item.imgPath);

            Bundle extra = new Bundle();
            extra.putSerializable("photoItem", item);
            intent.putExtras(extra);

            View imgView = view.findViewById(R.id.imagephotoView);
//            Pair<View, String> pair_thumb = Pair.create(thumbView, thumbView.getTransitionName());
//            여러개 이미지를 사용할 경우
            ActivityOptionsCompat options = ActivityOptionsCompat.
                    makeSceneTransitionAnimation(PhotoGalleryActivity.this, imgView, "transition");
            startActivity(intent, options.toBundle());
        }
    }

}
