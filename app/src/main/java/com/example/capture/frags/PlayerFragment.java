package com.example.capture.frags;

import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.ServiceConnection;
import android.graphics.Bitmap;
import android.media.MediaMetadataRetriever;
import android.media.MediaPlayer;
import android.net.Uri;
import android.os.Bundle;
import android.os.CountDownTimer;
import android.os.IBinder;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.SeekBar;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;

import com.bumptech.glide.Glide;
import com.example.capture.CaptureWidgetProvider;
import com.example.capture.FragmentCallback;
import com.example.capture.MainActivity;
import com.example.capture.MusicApplication;
import com.example.capture.MusicPlayerActivity;
import com.example.capture.R;
import com.example.capture.services.MusicService;

import org.greenrobot.eventbus.EventBus;
import org.greenrobot.eventbus.Subscribe;
//import com.squareup.otto.Subscribe;

import java.util.Locale;

public class PlayerFragment extends Fragment implements View.OnClickListener {
    private MusicService mService;
    private boolean mBound = false;

    private ImageView mAlbumImageView;
    private TextView mDurationTextView;
    private SeekBar mSeekBar;
    private TextView mCurrentTimeTextView;
    private CountDownTimer mCountDownTimer;

    private Button mPlayLoop, mStopPlayer;
    private String TAG = "음악 플레이어";

    public PlayerFragment() {
    }

//    @Override
//    public void onAttach(@NonNull Context context) {
//        super.onAttach(context);
//        if (context instanceof FragmentCallback) {
//            callback = (FragmentCallback) context;
//        }
//    }
//    @Override
//    public void onDetach() {
//        super.onDetach();
//            if(callback != null) callback = null;
//    }

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        return inflater.inflate(R.layout.music_player, container, false);
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        mAlbumImageView = view.findViewById(R.id.album_image);
        mDurationTextView = view.findViewById(R.id.duration_text);
        mCurrentTimeTextView = view.findViewById(R.id.current_time_text);
        mSeekBar = view.findViewById(R.id.seekBar);
        mPlayLoop = view.findViewById(R.id.play_loop);
        mStopPlayer = view.findViewById(R.id.stop_player);

        mPlayLoop.setOnClickListener(this);
        mStopPlayer.setOnClickListener(this);

        mSeekBar.setOnSeekBarChangeListener(new SeekBar.OnSeekBarChangeListener() {
            @Override
            public void onProgressChanged(SeekBar seekBar, int progress, boolean fromUser) {
            }

            @Override
            public void onStartTrackingTouch(SeekBar seekBar) {
            }

            @Override
            public void onStopTrackingTouch(SeekBar seekBar) {
                mService.getMediaPlayer().seekTo(seekBar.getProgress());
            }
        });


    }

    @Override
    public void onStart() {
        super.onStart();

        Log.d(TAG, "시작됨 ");

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

        EventBus.getDefault().unregister(this);

        if (mBound) {
//            // MusicApplication 활용
//            getActivity().unbindService(mConnection);
            mBound = false;
        }
        Log.d(TAG, "종료됨 ");
    }

    private ServiceConnection mConnection = new ServiceConnection() {

        @Override
        public void onServiceConnected(ComponentName className,
                                       IBinder service) {
            // We've bound to LocalService, cast the IBinder and get LocalService instance
            MusicService.LocalBinder binder = (MusicService.LocalBinder) service;
            mService = binder.getService();
            mBound = true;

            // UI 갱신
            updateUI(mService.isPlaying());
        }

        @Override
        public void onServiceDisconnected(ComponentName arg0) {
            mBound = false;
        }
    };

    @Subscribe
    public void updateUI(Boolean playing) {
        if (playing) {
            if(mService == null) return;  // *** player가 service에 연결되기 전
            MediaMetadataRetriever retriever = mService.getMetaDataRetriever();
            if (retriever != null) {
                // ms값
                int longDuration = mService.getMediaPlayer().getDuration();

                int min = longDuration / 1000 / 60;
                int sec = longDuration / 1000 % 60;

                mDurationTextView.setText(String.format(Locale.KOREA, "%d:%02d", min, sec));

                mSeekBar.setMax(longDuration);

                // 오디오 앨범 자켓 이미지
                Bitmap bitmap = mService.getAudioItem().mBitmap;

//                byte albumImage[] = retriever.getEmbeddedPicture();
                if (null != bitmap) {
                    Glide.with(this).load(bitmap).into(mAlbumImageView);
                } else {
                    Glide.with(this).load(R.drawable.snow).into(mAlbumImageView);
                }

//                if(mService.getMediaPlayer().isLooping()) {
//                    mPlayLoop.setText("연속재생");
//                } else {
//                    mPlayLoop.setText("반복재생");
//                }
                if(mService.getMediaPlayer().isLooping()) {
                    mPlayLoop.setText("연속재생");
                } else {
                    mPlayLoop.setText("반복재생");
                }
            }
        }
        updateTimer(playing);
    }

    public void updateTimer(boolean isPlaying) {
        MediaPlayer mMediaPlayer;
        if(mService == null) return;
        mMediaPlayer = mService.getMediaPlayer();
        if (!isPlaying) {
            if (mCountDownTimer != null) {
                mCountDownTimer.cancel();
            }
            mCountDownTimer = null;
        } else {

            int duration = mMediaPlayer.getDuration() - mMediaPlayer.getCurrentPosition();
            // 카운트다운 시작
            if (mCountDownTimer != null) {
                mCountDownTimer.cancel();
                mCountDownTimer = null;
            }
            mCountDownTimer = new CountDownTimer(duration, 1000) {
                @Override
                public void onTick(long millisUntilFinished) {
                    MediaPlayer mMediaPlayer = mService.getMediaPlayer();
                    if(mMediaPlayer == null) return;
                    int currentPosition = mMediaPlayer.getCurrentPosition();
                    mSeekBar.setProgress(currentPosition);

                    int min = currentPosition / 1000 / 60;
                    int sec = currentPosition / 1000 % 60;

                    mCurrentTimeTextView.setText(String.format(Locale.KOREA, "%d:%02d", min, sec));
                }

                @Override
                public void onFinish() {
                    mCountDownTimer = null;
                }
            }.start();
        }
    }

    @Override
    public void onClick(View v) {
        switch (v.getId()) {
            case R.id.play_loop:
                if(mService.getMediaPlayer().isLooping()) {
                    mService.getMediaPlayer().setLooping(false);
                    mPlayLoop.setText("반복재생");
                } else {
                    mService.getMediaPlayer().setLooping(true);
                    mPlayLoop.setText("연속재생");
                }
                break;
            case R.id.stop_player:
                stopPlayer();
                break;

            default:
                break;
        }
    }

    public void stopPlayer(){

        // TODO: 2020-07-07

        mService.getMediaPlayer().stop();
        mService.getMediaPlayer().release();
        mService.mMediaPlayer = null;

        mService.removeNotificationPlayer();    // notification 삭제
        // widget 종료
        mService.sendBroadcast(new Intent(MusicService.BroadcastActions.PLAY_STATE_CHANGED,   // action
                Uri.EMPTY,                                              // data
                getContext(),                                           // context
                CaptureWidgetProvider.class));                          //class
        // Service 종료
        final Intent serviceIntent = new Intent(getContext(), MusicService.class);
        getContext().stopService(serviceIntent);

        try {
            finalize();         // ???? 정리?  finish() ?
        } catch (Throwable throwable) {
            throwable.printStackTrace();
        }
        final Intent intent = new Intent(mService.getApplicationContext(), MainActivity.class);
        startActivity(intent);
    }
}
