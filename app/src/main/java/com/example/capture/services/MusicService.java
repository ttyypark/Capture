package com.example.capture.services;

import android.app.ActivityManager;
import android.app.Notification;
import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.app.Service;
import android.content.Context;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.drawable.Icon;
import android.media.AudioAttributes;
import android.media.AudioManager;
import android.media.MediaMetadataRetriever;
import android.media.MediaPlayer;
import android.net.ProxyInfo;
import android.net.Uri;
import android.net.sip.SipSession;
import android.os.Binder;
import android.os.Build;
import android.os.IBinder;
import android.os.PowerManager;
import android.util.Log;

import androidx.annotation.Nullable;
import androidx.annotation.RequiresApi;
import androidx.core.app.ActivityCompat;

import android.widget.RemoteViews;

import com.example.capture.AudioAdapter;
import com.example.capture.CaptureWidget;
import com.example.capture.CaptureWidgetProvider;
import com.example.capture.Foreground;
import com.example.capture.FragmentCallback;
import com.example.capture.MainActivity;
import com.example.capture.MusicPlayerActivity;
import com.example.capture.NotificationPlayer;
import com.example.capture.R;
import com.example.capture.frags.MusicControllerFragment;
import com.example.capture.frags.PlayerFragment;
import com.example.capture.frags.SongFragment;

import org.greenrobot.eventbus.EventBus;

import java.io.IOException;
import java.util.ArrayList;

//        EventBus.getDefault().post(isPlaying());    // fragment쪽 UI
//        updateNotificationPlayer();                 // Notification쪽 UI
//        sendBroadcast(new Intent(BroadcastActions.PREPARED,  // Widget쪽

public class MusicService extends Service {
    public static final String ACTION_PREV = "prev";
    public static final String ACTION_NEXT = "next";
    public static final String ACTION_PLAY = "play";
    public static final String ACTION_RESUME = "resume";
    public final static String REWIND = "REWIND";
    public final static String TOGGLE_PLAY = "TOGGLE_PLAY";
    public final static String FORWARD = "FORWARD";
    public final static String CLOSE = "CLOSE";
    public static final String TAG = "MusicService";

    private boolean isPrepared;
    private Context mContext;
    private ArrayList<Uri> mSongList;
    private Uri mCurrentUri;
    private int mIndex = 0;

    private final IBinder mBinder = new LocalBinder();
    public MediaPlayer mMediaPlayer;
    private MediaMetadataRetriever mRetriever;
    private NotificationPlayer mNotificationPlayer;
    private AudioAdapter mAudioAdapter;


    @Override
    public void onTaskRemoved(Intent rootIntent) {
        super.onTaskRemoved(rootIntent);
        stopSelf();   // 서비스 종료 ?????
        Log.d(TAG, "*****************서비스 종료됨 onTaskRemoved");
    }

    @Override
    public void onDestroy() {
        Log.d(TAG, "*****************서비스 종료됨,  MP 종료");
        super.onDestroy();
        if (mMediaPlayer != null) {
            mMediaPlayer.stop();
            mMediaPlayer.release();
            mMediaPlayer = null;
        }
    }

    @Override
    public void onCreate() {
        super.onCreate();
        Log.d(TAG, "********************서비스 시작됨, MP 생성");

        mContext = getApplicationContext();

        mAudioAdapter = new AudioAdapter(mContext);    // mSongList 설정
        mSongList = mAudioAdapter.mSongList;
//-----------------------------------------------------------
        mNotificationPlayer = new NotificationPlayer(this);  //****시작, 생성

        mMediaPlayer = new MediaPlayer();
        mMediaPlayer.setWakeMode(getApplicationContext(), PowerManager.PARTIAL_WAKE_LOCK);

//-----------------------------------------------------------
        mMediaPlayer.setOnPreparedListener(new MediaPlayer.OnPreparedListener() {
            @Override
            public void onPrepared(MediaPlayer mp) {
                isPrepared = true;
                mp.start();
                mIndex = mSongList.indexOf(mCurrentUri);
                /**
                 * {@link MusicControllerFragment#updateUI(Boolean)}
                 */
                /**
                 * {@link PlayerFragment#updateUI(Boolean)}
                 */
                EventBus.getDefault().post(isPlaying());    // fragment쪽 UI는  eventBus로
                updateNotificationPlayer();                 // Notification쪽
                sendBroadcast(new Intent(BroadcastActions.PREPARED,  // Widget쪽 // action
                        Uri.EMPTY,                                               // data
                        getApplicationContext(),                                 // context
                        CaptureWidgetProvider.class));                           //class
            }
        });

        mMediaPlayer.setOnCompletionListener(new MediaPlayer.OnCompletionListener() {
            @Override
            public void onCompletion(MediaPlayer mp) {
                nextMusic();
                updateNotificationPlayer();                                     // Notification쪽
                sendBroadcast(new Intent(BroadcastActions.PLAY_STATE_CHANGED)); // Widget 재생상태 변경
            }
        });

        mMediaPlayer.setOnErrorListener(new MediaPlayer.OnErrorListener() {
            @Override
            public boolean onError(MediaPlayer mp, int what, int extra) {
                isPrepared = false;
                updateNotificationPlayer();                                     // Notification쪽
                sendBroadcast(new Intent(BroadcastActions.PLAY_STATE_CHANGED)); // Widget 재생상태 변경
                return false;
            }
        });
        mMediaPlayer.setOnSeekCompleteListener(new MediaPlayer.OnSeekCompleteListener() {
            @Override
            public void onSeekComplete(MediaPlayer mp) {

            }
        });

//---------------------------------------------------------------------
    }

//----------------------------------------------------------------------
    private void updateNotificationPlayer() {
        if (mNotificationPlayer != null) {
            mNotificationPlayer.updateNotificationPlayer();
        }
    }

    public void removeNotificationPlayer() {
        if (mNotificationPlayer != null) {
            mNotificationPlayer.removeNotificationPlayer();
        }
    }
//----------------------------------------------------------------------

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
//      Error 인 상태에서는 음악 재생을 다시 시작...Stack check 필요?
        if(mMediaPlayer == null || mSongList == null) {
            Intent musicIntent = new Intent(getApplicationContext(), MusicPlayerActivity.class);
            musicIntent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
            startActivity(musicIntent);
        }

        String action = intent.getAction();
        Log.i(TAG, "onStartCommand 시작됨: " + action);

        switch (action) {
            case ACTION_PLAY:
                playMusic((Uri) intent.getParcelableExtra("uri"));
                break;
            case ACTION_RESUME:
                clickResumeButton();
                break;
            case ACTION_NEXT:
                nextMusic();
                break;
            case ACTION_PREV:
                prevMusic();
                break;
//-------------------------------------------
            case TOGGLE_PLAY:
                clickResumeButton();
                break;
            case REWIND:
                prevMusic();
                break;
            case FORWARD:
                nextMusic();
                break;
            case CLOSE:

                // TODO: 2020-07-07

//   foreground service가 종료된 경우
                removeNotificationPlayer();

                if(Foreground.isBackground()){  // Activity 꺼지고 Notification만 있을때
                    Log.d(TAG, "Activity가 백그라은드에 있음");
                    mMediaPlayer.stop();
                    mMediaPlayer.release();
                    mMediaPlayer = null;
                } else {
                    Log.d(TAG, "Activity가 포그라은드에 있음");
                    /**
                     * {@link com.example.capture.MusicPlayerActivity#stopPlayer(Integer)}
                     */
                    EventBus.getDefault().post(3);
                };

                // Widget 상태변경 ****
                sendBroadcast(new Intent(BroadcastActions.PLAY_STATE_CHANGED,   // action
                        Uri.EMPTY,                                              // data
                        getApplicationContext(),                                // context
                        CaptureWidgetProvider.class));                          //class

                try{
                    MusicService.this.onDestroy();   // 서비스 종료?
                } catch (Exception e){
                    Log.e(TAG, " *******  서비스 종료 error");
                }

// system 종료 -------------------------
                android.os.Process.killProcess(android.os.Process.myPid());
//                moveTaskToBack(true);
//                System.exit(0);

                break;
        }
        return START_STICKY;
//        return super.onStartCommand(intent, flags, startId);
    }

    public MediaPlayer getMediaPlayer() {
        return mMediaPlayer;
    }

    public void setmSongList(ArrayList<Uri> songList){
        if(!mSongList.equals(songList)){
            mSongList.clear();
            mSongList.addAll(songList);
        }
    }

    public void playMusic(final Uri uri){
        mCurrentUri = uri;
        try {
            // 현재 재생중인 정보
            mRetriever = new MediaMetadataRetriever();
            mRetriever.setDataSource(this, uri);

            if (mMediaPlayer != null){
                mMediaPlayer.stop();
                mMediaPlayer.reset();
            } else {
                mMediaPlayer = new MediaPlayer();
            }
//            mMediaPlayer.setAudioStreamType(AudioManager.STREAM_MUSIC);
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
                mMediaPlayer.setAudioAttributes(
                        new AudioAttributes
                                .Builder()
                                .setContentType(AudioAttributes.CONTENT_TYPE_MUSIC)
                                .build());
            }

            mMediaPlayer.setDataSource(this, uri);
            mMediaPlayer.prepareAsync();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public MediaMetadataRetriever getMetaDataRetriever() {
        return mRetriever;
    }

    public void nextMusic(){
        mIndex++;
        if(mIndex > mSongList.size() - 1){
            mIndex = 0;
        }
        playMusic(mSongList.get(mIndex));
    }

    public void prevMusic() {
        mIndex--;
        if (mIndex < 0) {
            mIndex = mSongList.size() - 1;
        }
        playMusic(mSongList.get(mIndex));
    }

    public void clickResumeButton(){
        if(mMediaPlayer == null) return;   // widget에서 toggle
        if(isPlaying()) {
            mMediaPlayer.pause();
        } else {
            mMediaPlayer.start();
        }
        /**
         * {@link com.example.capture.frags.MusicControllerFragment#updateUI(Boolean)} (Boolean)}
         */
        /**
         * {@link com.example.capture.frags.PlayerFragment#updateUI(Boolean)} (Boolean)}
         */
        EventBus.getDefault().post(isPlaying());

// Widget 상태변경 ****
        sendBroadcast(new Intent(BroadcastActions.PLAY_STATE_CHANGED,   // action
                Uri.EMPTY,                                              // data
                getApplicationContext(),                                // context
                CaptureWidgetProvider.class));                          //class

//            // foreground service
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {   //**************
//            startForegroundService();
            updateNotificationPlayer();
        }
    }

    public boolean isPlaying() {
        if (mMediaPlayer != null) {
            return mMediaPlayer.isPlaying();
        }
        return false;
    }


    @Nullable
    @Override
    public IBinder onBind(Intent intent) {
        Log.d(TAG, "Bind 시작됨");
        return mBinder;
    }

    @Override
    public boolean onUnbind(Intent intent) {
        Log.d(TAG, "Bind 끝남");
        return super.onUnbind(intent);
    }


    public class LocalBinder extends Binder{
        public MusicService getService(){
            // Return this instance of LocalService so clients can call public methods
            return MusicService.this;
        }
    }

    public AudioAdapter.AudioItem getAudioItem(){
//---------------------------------- AudioItem 만들기
        if (mCurrentUri == null) return null;
        MediaMetadataRetriever retriever = new MediaMetadataRetriever();
        retriever.setDataSource(getApplicationContext(), mCurrentUri);
        AudioAdapter.AudioItem audioItem = AudioAdapter.AudioItem.bindRetriever(retriever);
        return audioItem;
    }

    public static class BroadcastActions {
        public final static String PREPARED = "PREPARED";
        public final static String PLAY_STATE_CHANGED = "PLAY_STATE_CHANGED";
    }


    // NotificationPlayer로 대체 ======================================================
//    // Foreground Service -----------------------------
//    @RequiresApi(api = Build.VERSION_CODES.M)


//    public void startForegroundService(){  //===================
////        Bitmap bitmap = null;
//        RemoteViews remoteViews = new RemoteViews(getPackageName(), R.layout.remote_view);
//        Notification.Builder builder;
//
//        String CHANNEL_ID = "song";
//        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
//            NotificationChannel channel = new NotificationChannel(CHANNEL_ID, "Song Channel",
//                    NotificationManager.IMPORTANCE_DEFAULT);
//            ((NotificationManager)getSystemService(Context.NOTIFICATION_SERVICE))
//                    .createNotificationChannel(channel);
//            builder = new Notification.Builder(this, CHANNEL_ID);
//
//        } else {
//            builder = new Notification.Builder(this);
//        }
////----------------------------------
//        String title = getAudioItem().mTitle;
//        String artist = getAudioItem().mArtist;
//        Bitmap bitmap = getAudioItem().mBitmap;
//
////        String title = mRetriever.extractMetadata((MediaMetadataRetriever.METADATA_KEY_TITLE));
////        String artist = mRetriever.extractMetadata((MediaMetadataRetriever.METADATA_KEY_ARTIST));
//        if(title == null) title = "UnKnown";
//        if(artist == null) artist = "UnKnown";
//
//        remoteViews.setTextViewText(R.id.title_text, title);
//        remoteViews.setTextViewText(R.id.artist_text, artist);
//
//        if(bitmap != null) {
////                Picasso.get().load(albumArtUri).error(R.drawable.musiccircle).into(remoteViews, R.id.img_albumart, NOTIFICATION_PLAYER_ID, notification);
//            remoteViews.setImageViewBitmap(R.id.album_image, bitmap);
//        } else{
//            remoteViews.setImageViewResource(R.id.album_image, R.drawable.music_circle);
//        }
//
////        byte albumImage[] = mRetriever.getEmbeddedPicture();
////
////        if(albumImage == null) {
////            remoteViews.setImageViewResource(R.id.album_image, R.mipmap.ic_launcher);
////            builder.setSmallIcon(R.mipmap.ic_launcher);
////        } else {
////            bitmap = BitmapFactory.decodeByteArray(albumImage, 0,albumImage.length);
////            remoteViews.setImageViewBitmap(R.id.album_image, bitmap);
////            builder.setSmallIcon(Icon.createWithBitmap(bitmap));
////        }
//
//// ****
////        RemoteViews.RemoteResponse response = new RemoteViews.RemoteResponse();
////        remoteViews.setOnClickResponse(R.id.btn_rewind, response);
//
//
////        remoteViews.setInt(R.id.resume_button2,
////                "setBackgroundColor", Color.RED);
//
////        if(isPlaying()){
////            remoteViews.setTextViewText(R.id.resume_button1, "중지");
////        } else {
////            remoteViews.setTextViewText(R.id.resume_button1, "재생");
////        }
////        remoteViews.setTextViewText(R.id.resume_button2, "앞곡");
////        remoteViews.setTextViewText(R.id.resume_button3, "뒷곡");
//
////        builder.setLargeIcon(bitmap);
////        builder.setSmallIcon(Icon.createWithBitmap(bitmap));
////----------------------------------
//        Intent notificationIntent = new Intent(this, MusicPlayerActivity.class);
//        PendingIntent pendingIntent = PendingIntent.getActivity(this, 0, notificationIntent, 0);
////        builder.setSmallIcon(R.mipmap.ic_launcher)
////        builder.setSmallIcon(Icon.createWithBitmap(bitmap))
//        builder.setContent(remoteViews)
//                .setContentIntent(pendingIntent);
//
//        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.JELLY_BEAN) {
//            startForeground(1, builder.build());
//        } else {
////            startForeground(1, builder.build());
//        }
//
//    }
// NotificationPlayer로 대체 ======================================================

// notification => Foreground Service 로 대체 ----------------------------------------


//    private void showNotification(){
//        String title = mRetriever.extractMetadata((MediaMetadataRetriever.METADATA_KEY_TITLE));
//        String artist = mRetriever.extractMetadata((MediaMetadataRetriever.METADATA_KEY_ARTIST));
//
//        Builder builder = new Builder(this, "Song");
////        NotificationCompat.Builder builder = new NotificationCompat.Builder(this);
//
////        builder.setContentTitle(title);
////        builder.setContentText(artist);
//
//        builder.setSmallIcon(R.mipmap.ic_launcher);
//
//        RemoteViews remoteViews = new RemoteViews(getPackageName(), R.layout.remote_view);
//        remoteViews.setTextViewText(R.id.title_text, title);
//        remoteViews.setTextViewText(R.id.artist_text, artist);
//
//        builder.setCustomContentView(remoteViews);
//
//        Bitmap bitmap = BitmapFactory.decodeResource(
//                getResources(), R.mipmap.ic_launcher);
//
////        builder.setLargeIcon(bitmap);
//        remoteViews.setImageViewBitmap(R.id.album_image, bitmap);
//
//        // 알림을 클릭하면 수행될 인텐트
//        Intent resultIntent = new Intent(this, MusicPlayerActivity.class);
//        PendingIntent pendingIntent = PendingIntent.getActivity(this, 1010,
//                resultIntent, PendingIntent.FLAG_UPDATE_CURRENT);
//        builder.setContentIntent(pendingIntent);
//
//        // 클릭하면 날리기
//        builder.setAutoCancel(true);
//
//        // 색상
//        builder.setColor(Color.YELLOW);
//
//        // 기본 알림음
//        Uri uri = RingtoneManager.getDefaultUri(RingtoneManager.TYPE_NOTIFICATION);
//        builder.setSound(uri);
//
//        // 진동
//        builder.setVibrate(new long[]{100, 200, 300});
//
//        Intent stopIntent = new Intent(this, MusicService.class);
//        stopIntent.setAction(ACTION_RESUME);
//        stopIntent.setAction(ACTION_PREV);
//        stopIntent.setAction(ACTION_NEXT);
//        PendingIntent stopPendingIntent = PendingIntent.getService(this, 1,
//                stopIntent, PendingIntent.FLAG_CANCEL_CURRENT);
//
//        // 액션
////        builder.addAction(R.mipmap.ic_launcher, "중지", stopPendingIntent);
////        builder.addAction(R.mipmap.ic_launcher, "다음곡", pendingIntent);
////        builder.addAction(R.mipmap.ic_launcher, "이전곡", pendingIntent);
//
//        // 알림 표시
//        startForeground(1, builder.build());
//    }

}

