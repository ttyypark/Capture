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

import java.util.ArrayList;

public class PhotoFragment extends Fragment {
    private static final String TAG = "사진";
    private Context mContext;
//    FragmentCallback callback;

    public PhotoFragment() {
    }

    @Override
    public void onAttach(@NonNull Context context) {
        super.onAttach(context);
        mContext = context;
    }

    @Override
    public void onDetach() {
        super.onDetach();
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

//        //   cursor 로 데이터를 가져와서 Adapter 에 넣기
//        Cursor cursor = getActivity().getContentResolver()
//                .query(MediaStore.Images.Media.EXTERNAL_CONTENT_URI,
//                        null, null, null, null);

        PhotoAdapter mPhotoAdapter = new PhotoAdapter(mContext, R.layout.photo_item);

// ? item click
////        mPhotoAdapter.setOnItemClickListener(new PhotoAdapter.onItemClickListener(){
////            @Override
////            public void onItemClicked(RecyclerView.ViewHolder holder, View view, int position) {
////                PhotoAdapter.PhotoItem item = mPhotoAdapter.getItem(position);
////                Toast.makeText(getContext(), "아이템 선택됨: " + position, Toast.LENGTH_SHORT).show();
////            }
////        });

        recyclerView.setAdapter(mPhotoAdapter);
        Log.i(TAG, "사진 수 : " + mPhotoAdapter.getItemCount());


        // 추가
        GridLayoutManager layoutManager = new GridLayoutManager(getActivity(), 3);
        layoutManager.setOrientation(LinearLayoutManager.VERTICAL);
        recyclerView.setLayoutManager(layoutManager);

    }



}
