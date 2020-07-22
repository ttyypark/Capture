package com.example.capture;

import android.annotation.SuppressLint;
import android.app.Notification;
import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.ContentUris;
import android.content.Context;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Color;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Build;
import android.util.Log;
import android.widget.RemoteViews;

import androidx.core.app.NotificationCompat;
import androidx.core.app.NotificationManagerCompat;

import com.example.capture.services.MusicService;
import com.squareup.picasso.Picasso;

import java.io.IOException;

public class NotificationPlayer {
    private final static int NOTIFICATION_PLAYER_ID = 0x342;
    private MusicService mService;
    private NotificationManager mNotificationManager;
    private NotificationManagerBuilder mNotificationManagerBuilder;
    private boolean isForeground;
    private final String CHANNEL_ID = "My channel ID";
    private final String CHANNEL_NAME = "My channel";

    public NotificationPlayer(MusicService service) {
        mService = service;
        mNotificationManager = (NotificationManager) service.getSystemService(Context.NOTIFICATION_SERVICE);
        isForeground = false;
    }

    @SuppressLint("StaticFieldLeak")
    public void updateNotificationPlayer() {
        cancel();
//        mNotificationManagerBuilder = new NotificationManagerBuilder();
//        mNotificationManagerBuilder.execute();

        // ==================================== NotificationManagerBuilder() 없이 하나로 실행..
        new AsyncTask<Void, Void, Notification>() {
            NotificationCompat.Builder mNotificationBuilder;

            @Override
            protected Notification doInBackground(Void... params) {
//                Bitmap largIcon = null;
//                Uri albumArtUri = ContentUris.withAppendedId(Uri.parse("content://media/external/audio/albumart"), mService.getAudioItem().mAlbumId);
//                try {
//                    largIcon = Picasso.get().load(albumArtUri).get();
//                } catch (IOException e) {
//                    e.printStackTrace();
//                }
                Bitmap largIcon = mService.getAudioItem().mBitmap;
                if(largIcon == null) {
                    largIcon = BitmapFactory.decodeResource(mService.getApplicationContext().getResources(),
                    R.drawable.snow);  //?
                }

                Intent mainActivity = new Intent(mService, MusicPlayerActivity.class);
                Intent actionTogglePlay = new Intent(mService.TOGGLE_PLAY);
                Intent actionForward = new Intent(mService.FORWARD);
                Intent actionRewind = new Intent(mService.REWIND);
                Intent actionClose = new Intent(mService.CLOSE);
                PendingIntent togglePlay = PendingIntent.getService(mService, 0, actionTogglePlay, 0);
                PendingIntent forward = PendingIntent.getService(mService, 0, actionForward, 0);
                PendingIntent rewind = PendingIntent.getService(mService, 0, actionRewind, PendingIntent.FLAG_UPDATE_CURRENT);
                PendingIntent close = PendingIntent.getService(mService, 0, actionClose, 0);
                PendingIntent main = PendingIntent.getActivity(mService, 0, mainActivity, 0);

//-------------------------------------------------------
                NotificationManager manager = (NotificationManager) mService.getSystemService(Context.NOTIFICATION_SERVICE);
                mNotificationBuilder = new NotificationCompat.Builder(mService, CHANNEL_ID);
                mNotificationBuilder
                        .setContentTitle(mService.getAudioItem().mTitle)
                        .setContentText(mService.getAudioItem().mArtist)
                        //.setStyle(new NotificationCompat.BigTextStyle().bigText("더 많은 내용을 보여줘야 하는 경우..."))
                        .setLargeIcon(largIcon)
                        .setContentIntent(main)
                        .setPriority(NotificationCompat.PRIORITY_DEFAULT)
                        .setAutoCancel(true);


                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                    mNotificationBuilder.setSmallIcon(R.drawable.music_circle);   // xml file only?
                    NotificationChannel channel = new NotificationChannel(CHANNEL_ID, CHANNEL_NAME,
                            NotificationManager.IMPORTANCE_DEFAULT);
                    channel.setDescription("오레오 이상?");
                    // 노티피케이션 채널을 시스템에 등록
                    assert manager != null;
                    manager.createNotificationChannel(channel);
                } else {
                    mNotificationBuilder.setSmallIcon(R.drawable.musiccircle);   // png file only??
                }


                mNotificationBuilder.addAction(new NotificationCompat.Action(R.drawable.skip_previous, "", rewind));
                mNotificationBuilder.addAction(new NotificationCompat.Action(mService.isPlaying() ? R.drawable.pause : R.drawable.play, "", togglePlay));
                mNotificationBuilder.addAction(new NotificationCompat.Action(R.drawable.skip_next, "", forward));
                mNotificationBuilder.addAction(new NotificationCompat.Action(R.drawable.stop, "", close));

                int[] actionsViewIndexs = new int[]{0,1,2};
                mNotificationBuilder.setStyle(new androidx.media.app.NotificationCompat.MediaStyle().setShowActionsInCompactView(actionsViewIndexs));

                Notification notification = mNotificationBuilder.build();

                assert manager != null;
                manager.notify(NOTIFICATION_PLAYER_ID, notification);

                if (!isForeground) {
                    isForeground = true;
                    // 서비스를 Foreground 상태로 만든다
                    mService.startForeground(NOTIFICATION_PLAYER_ID, notification);
                }

                return notification;
            }  // doBackground

        }.execute();
        // ====================================

    }

    public void removeNotificationPlayer() {
        cancel();
        mService.stopForeground(true);
        isForeground = false;
    }

    private void cancel() {
        if (mNotificationManagerBuilder != null) {
            mNotificationManagerBuilder.cancel(true);
            mNotificationManagerBuilder = null;
        }
    }

    @SuppressLint("StaticFieldLeak")
    private class NotificationManagerBuilder extends AsyncTask<Void, Void, Notification> {
        private RemoteViews mRemoteViews;
        private NotificationCompat.Builder mNotificationBuilder;
        private PendingIntent mMainPendingIntent;

        @Override
        protected void onPreExecute() {
            super.onPreExecute();
            Log.d("Notification", "PreExecute 수행됨");

            Intent mainActivity = new Intent(mService, MusicPlayerActivity.class);
            mMainPendingIntent = PendingIntent.getActivity(mService, 0, mainActivity, 0);
            mRemoteViews = createRemoteView(R.layout.remote_view);
//            mRemoteViews.setInt(R.id.artist_text, "setBackgroundColor", Color.YELLOW);
//            remoteViews.setTextViewText(R.id.resume_button1, "재생");

//  methodName 에서 사용할 수 있는 method
//            setContentDescription(CharSequence contentDescription)
//            setAccessibilityTraversalBefore(@IdRes int beforeId)
//            setAccessibilityTraversalAfter(@IdRes int afterId)
//            setLabelFor(@IdRes int id)
//            setVisibility(@Visibility int visibility)
//            setEnabled(boolean enabled)
//            setLayoutDirection(@LayoutDir int layoutDirection)
//            setBackgroundColor(@ColorInt int color)
//            setBackgroundResource(@DrawableRes int resid)
//            setMinimumHeight(int minHeight)

//            setDisplayedChild
//            setVisibility
//            setText, setTextColor
//            setImageResource
//            setImageURI
//            setImageBitmap
//            setImageIcon
//            setBase
//            setFormat
//            setStarted
//            setCountDown
//            setIndeterminate, setMax, setProgress
//            setProgressTintList
//            setProgressBackgroundTintList
//            setContentDescription
//            setLabelFor


            Notification notification = createNotification();

            if (!isForeground) {
                isForeground = true;
                // 서비스를 Foreground 상태로 만든다
                mService.startForeground(NOTIFICATION_PLAYER_ID, notification);
            }

        }

        private Notification createNotification(){
            if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.O) {
                NotificationChannel channel = new NotificationChannel(CHANNEL_ID, CHANNEL_NAME,
                        NotificationManager.IMPORTANCE_DEFAULT);
                NotificationManager manager = (NotificationManager) mService.getSystemService(Context.NOTIFICATION_SERVICE);
                assert manager != null;
                manager.createNotificationChannel(channel);
                mNotificationBuilder = new NotificationCompat.Builder(mService, CHANNEL_ID);
            } else {
                mNotificationBuilder = new NotificationCompat.Builder(mService);
            }
            mNotificationBuilder.setSmallIcon(R.mipmap.ic_launcher)
                    .setOngoing(true)
                    .setContentIntent(mMainPendingIntent)
//                    .setStyle(new androidx.media.app.NotificationCompat.MediaStyle().setMediaSession(MediaS))
                    .setContent(mRemoteViews);
            //---------------------------------

            // 색상
//            mNotificationBuilder.setColorized(true);
            mNotificationBuilder.setColor(Color.GREEN);

//            // 기본 알림음
//            Uri uri = RingtoneManager.getDefaultUri(RingtoneManager.TYPE_NOTIFICATION);
//            mNotificationBuilder.setSound(uri);
//
//            // 진동
//            mNotificationBuilder.setVibrate(new long[]{100, 200, 300});
//
            //---------------------------------
            mNotificationBuilder.setPriority(Notification.PRIORITY_DEFAULT);

            Notification notification = mNotificationBuilder.build();
            return  notification;
        }

        @Override
        protected Notification doInBackground(Void... params) {
            Log.d("Notification", "Background 시작됨");
            mNotificationBuilder.setContent(mRemoteViews);
            mNotificationBuilder.setContentIntent(mMainPendingIntent);
            mNotificationBuilder.setPriority(Notification.PRIORITY_MAX);
            Notification notification = mNotificationBuilder.build();
            updateRemoteView(mRemoteViews, notification);
            return notification;
        }

        @Override
        protected void onPostExecute(Notification notification) {
            super.onPostExecute(notification);
            Log.d("Notification", "PostExecute 수행됨");
            try {
                mNotificationManager.notify(NOTIFICATION_PLAYER_ID, notification);
            } catch (Exception e) {
                e.printStackTrace();
            }
        }

        private RemoteViews createRemoteView(int layoutId) {
            RemoteViews remoteView = new RemoteViews(mService.getPackageName(), layoutId);
            Intent actionTogglePlay = new Intent(mService.TOGGLE_PLAY);
            Intent actionForward = new Intent(mService.FORWARD);
            Intent actionRewind = new Intent(mService.REWIND);
            Intent actionClose = new Intent(mService.CLOSE);
            PendingIntent togglePlay = PendingIntent.getService(mService, 0, actionTogglePlay, 0);
            PendingIntent forward = PendingIntent.getService(mService, 0, actionForward, 0);
            PendingIntent rewind = PendingIntent.getService(mService, 0, actionRewind, 0);
            PendingIntent close = PendingIntent.getService(mService, 0, actionClose, 0);

            remoteView.setOnClickPendingIntent(R.id.btn_play_pause, togglePlay);
            remoteView.setOnClickPendingIntent(R.id.btn_forward, forward);
            remoteView.setOnClickPendingIntent(R.id.btn_rewind, rewind);
            remoteView.setOnClickPendingIntent(R.id.btn_close, close);
            return remoteView;
        }

        private void updateRemoteView(RemoteViews remoteViews, Notification notification) {
            if (mService.isPlaying()) {
                remoteViews.setImageViewResource(R.id.btn_play_pause, R.drawable.pause);
            } else {
                remoteViews.setImageViewResource(R.id.btn_play_pause, R.drawable.play);
            }

            String title = mService.getAudioItem().mTitle;
            String artist = mService.getAudioItem().mArtist;
            Bitmap bitmap = mService.getAudioItem().mBitmap;

            remoteViews.setTextViewText(R.id.title_text, title);
            remoteViews.setTextViewText(R.id.artist_text, artist);
//            Uri albumArtUri = ContentUris.withAppendedId(Uri.parse("content://media/external/audio/albumart"), mService.getAudioItem().mAlbumId);
            if(bitmap != null) {
//                Picasso.get().load(albumArtUri).error(R.drawable.musiccircle).into(remoteViews, R.id.img_albumart, NOTIFICATION_PLAYER_ID, notification);
                remoteViews.setImageViewBitmap(R.id.album_image, bitmap);
            } else{
                remoteViews.setImageViewResource(R.id.album_image, R.drawable.music_circle);
            }
        }
    }

//    public class CommandActions {
//        public final static String REWIND = "REWIND";
//        public final static String TOGGLE_PLAY = "TOGGLE_PLAY";
//        public final static String FORWARD = "FORWARD";
//        public final static String CLOSE = "CLOSE";
//    }

}


