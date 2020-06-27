package com.example.capture.frags;

import android.media.MediaPlayer;
import android.net.Uri;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.MediaController;
import android.widget.VideoView;

import androidx.annotation.NonNull;
import androidx.fragment.app.Fragment;
import androidx.navigation.fragment.NavHostFragment;

import com.example.capture.R;

import org.greenrobot.eventbus.EventBus;
import org.greenrobot.eventbus.Subscribe;

public class VideoPlayerFragment extends Fragment {
    private static final String TAG = "비디오 플레이어";
    private VideoView videoView;

    @Override
    public View onCreateView(
            LayoutInflater inflater, ViewGroup container,
            Bundle savedInstanceState) {

        // EventBus에 구독자로 현재 액티비티 추가
        EventBus.getDefault().register(this);

        Log.d(TAG, "player start");
        // Inflate the layout for this fragment
        return inflater.inflate(R.layout.fragment_videoplayer, container, false);
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        // EventBus에 구독자에서 제거
        EventBus.getDefault().unregister(this);
        Log.d(TAG, "player stop");
    }

    public void onViewCreated(@NonNull View view, Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        videoView = view.findViewById(R.id.videoview);

        MediaController mc = new MediaController(getContext());
        videoView.setMediaController(mc);

    }

    @Subscribe
    public void playVideo(final Uri uri){
        String path = "https://sites.google.com/site/ubiaccessmobile/sample_video.mp4";
        videoView.setVideoPath(path);
//        videoView.setVideoURI(uri);
        videoView.requestFocus();

        videoView.setOnPreparedListener(new MediaPlayer.OnPreparedListener() {
            @Override
            public void onPrepared(MediaPlayer mp) {
                videoView.seekTo(0);
                videoView.start();

                Log.d(TAG, "재생시작 Uri : " + uri);
            }
        });
    }
}
