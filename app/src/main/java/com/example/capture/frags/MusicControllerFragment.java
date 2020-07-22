package com.example.capture.frags;  //package com.example.capture.fragments;

import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.ServiceConnection;
import android.graphics.Bitmap;
import android.media.MediaMetadataRetriever;
import android.os.Build;
import android.os.Bundle;
import android.os.IBinder;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;

import com.bumptech.glide.Glide;
import com.example.capture.MusicApplication;
import com.example.capture.R;
import com.example.capture.services.MusicService;
//import com.squareup.otto.Subscribe;

import org.greenrobot.eventbus.EventBus;
import org.greenrobot.eventbus.Subscribe;

public class MusicControllerFragment extends Fragment implements View.OnClickListener {
    private ImageView mAlbumImageView;
    private TextView mTitleTextView;
    private TextView mArtistTextView;
    private Button mPlayButton;

    private MusicService mService;
    private boolean mBound = false;

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        return inflater.inflate(R.layout.fragment_music_controller, container, false);
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        mAlbumImageView = (ImageView) view.findViewById(R.id.album_image);
        mTitleTextView = (TextView) view.findViewById(R.id.title_text);
        mArtistTextView = (TextView) view.findViewById(R.id.artist_text);

        mPlayButton = (Button) view.findViewById(R.id.play_button);
        mPlayButton.setOnClickListener(this);

        view.findViewById(R.id.prev_music_button).setOnClickListener(this);
        view.findViewById(R.id.next_music_button).setOnClickListener(this);

    }

    private void updateMetaData(MediaMetadataRetriever retriever){
        if(retriever != null){
//            String title = retriever.extractMetadata(MediaMetadataRetriever.METADATA_KEY_TITLE);
//            String artist = retriever.extractMetadata(MediaMetadataRetriever.METADATA_KEY_ARTIST);
            String title = mService.getAudioItem().mTitle;
            String artist = mService.getAudioItem().mArtist;
            Bitmap bitmap = mService.getAudioItem().mBitmap;

            if(title == null) title = "UnKnown";
            if(artist == null) artist = "UnKnown";

            // 오디오 앨범 자켓 이미지,  AudioItem의 정보 활용?
//            byte albumImage[] = retriever.getEmbeddedPicture();
            if(bitmap != null) {
                Glide.with(this).load(bitmap).into(mAlbumImageView);
            } else{
//                Glide.with(this).load(R.drawable.musiccircle).into(mAlbumImageView);
                mAlbumImageView.setImageResource(R.drawable.music_circle);
            }
// **** Glide 사용법
            mTitleTextView.setText(title);
            mArtistTextView.setText(artist);
        }
    }

  // Service 이용하는 방법 ===============================
    @Subscribe
    public void updateUI(Boolean isPlaying) {       // boolean 안됨
        if(mService == null) return;  // *** player가 service에 연결되기 전
        mPlayButton.setText(isPlaying ? "중지" : "재생");
        updateMetaData(mService.getMetaDataRetriever());

//        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
//            mService.startForegroundService();
//        }
    }

//    // EventBus 만을 이용하는 방법 ===============================
//    @Subscribe
//    public void updateUI(final MediaMetadataRetriever retriever) {
//        updateMetaData(retriever);
//    }
//
//    @Subscribe
//    public void updatePlayButton(Boolean isPlaying){
//        mPlayButton.setText(isPlaying ? "중지" : "재생");
//    }
//    // ===============================

    @Override
    public void onStart() {
        super.onStart();

        // EventBus 설정
        EventBus.getDefault().register(this);

// MusicApplication 활용
        if(mService == null) mService = MusicApplication.getInstance().getServiceInterface().mService;
        updateUI(mService.isPlaying());
        mBound = true;
//        Intent intent = new Intent(getActivity(), MusicService.class);
//        getActivity().bindService(intent, mConnection, Context.BIND_AUTO_CREATE);
    }

    @Override
    public void onStop() {
        super.onStop();

        // EventBus 해제
        EventBus.getDefault().unregister(this);

        // Service 이용 해제
        if (mBound) {
//            // MusicApplication 활용
//            getActivity().unbindService(mConnection);
            mBound = false;
        }
    }

    @Override
    public void onClick(View v) {
        // Service 사용
        Intent intent = new Intent(getActivity(), MusicService.class);

        switch (v.getId()) {
            case R.id.play_button:

//                // EventBus 사용
//                /**
//                 * {@link com.example.capture.MusicPlayerActivity#clickPlayButton(View)}
//                 */
//                EventBus.getDefault().post(v);

                // Service 사용
                intent.setAction(MusicService.ACTION_RESUME);
                break;
            case R.id.prev_music_button:
                intent.setAction(MusicService.ACTION_PREV);
                break;
            case R.id.next_music_button:
                intent.setAction(MusicService.ACTION_NEXT);
                break;
        }

        // Service 사용
        getActivity().startService(intent);
    }

    // Service 사용
    private ServiceConnection mConnection = new ServiceConnection() {
        @Override
        public void onServiceConnected(ComponentName name, IBinder service) {
            // We've bound to LocalService, cast the IBinder and get LocalService instance
            MusicService.LocalBinder binder = (MusicService.LocalBinder) service;
            mService = binder.getService();
            mBound = true;

            // Service 사용
            // UI 갱신
            updateUI(mService.isPlaying());
        }

        @Override
        public void onServiceDisconnected(ComponentName name) {
            mBound = false;
        }
    };
}
