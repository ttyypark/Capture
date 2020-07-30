package com.example.capture.frags;

import android.content.Context;
import android.database.Cursor;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.net.Uri;
import android.os.Bundle;
import android.provider.MediaStore;
import android.text.TextUtils;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.GridLayoutManager;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.example.capture.AudioAdapter;
import com.example.capture.FragmentCallback;
import com.example.capture.PhotoAdapter;
import com.example.capture.R;

import org.greenrobot.eventbus.EventBus;

import java.util.ArrayList;

public class PhotoFragment extends Fragment {
    FragmentCallback callback;
    private static final String TAG = "사진";
    private Context mContext;
//    FragmentCallback callback;

    public PhotoFragment() {
    }

    @Override
    public void onAttach(@NonNull Context context) {
        super.onAttach(context);
        mContext = context;
        if (context instanceof FragmentCallback) {
            callback = (FragmentCallback) context;
        }
    }

    @Override
    public void onDetach() {
        super.onDetach();
        if(callback != null) callback = null;
    }

    @Nullable
    @Override
    public View onCreateView(
            LayoutInflater inflater, ViewGroup container,
            Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        return inflater.inflate(R.layout.fragment_photo, container, false);
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        RecyclerView recyclerView = view.findViewById(R.id.recycler_photo);

        PhotoAdapter mPhotoAdapter = new PhotoAdapter(mContext, R.layout.photo_item);

//       item click
        mPhotoAdapter.setOnItemClickListener(new PhotoAdapter.onItemClickListener(){
            @Override
            public void onItemClicked(PhotoAdapter.PhotoItem item, View view1) {
                Toast.makeText(getContext(), "아이템 선택됨: " + item.imgPath, Toast.LENGTH_SHORT).show();

//              PhotoViewFragment 호출
                //  EventBus로 PhotoViewFragment#playPhoto 사용
                /**
                 * 사진 보기
                 * {@link com.example.capture.frags.PhotoViewFragment#playPhoto(PhotoAdapter.PhotoItem)}                  */
                EventBus.getDefault().post(item);
                if (callback != null) callback.setPage(1);

            }
        });

        recyclerView.setAdapter(mPhotoAdapter);
        Log.i(TAG, "사진 수 : " + mPhotoAdapter.getItemCount());


        // 추가
        GridLayoutManager layoutManager = new GridLayoutManager(getActivity(), 3);
        layoutManager.setOrientation(LinearLayoutManager.VERTICAL);
        recyclerView.setLayoutManager(layoutManager);

    }



}
