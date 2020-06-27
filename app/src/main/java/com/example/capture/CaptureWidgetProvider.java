package com.example.capture;

import android.app.PendingIntent;
import android.appwidget.AppWidgetManager;
import android.appwidget.AppWidgetProvider;
import android.content.ComponentName;
import android.content.ContentUris;
import android.content.Context;
import android.content.Intent;
import android.content.ServiceConnection;
import android.content.res.Resources;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.os.IBinder;
import android.preference.PreferenceManager;
import android.util.Log;
import android.util.TypedValue;
import android.widget.RemoteViews;
import android.widget.Toast;

import com.example.capture.services.MusicService;
import com.squareup.picasso.Picasso;

public class CaptureWidgetProvider extends AppWidgetProvider {
    public static MusicService mService = null;  // ** 서비스
    private static final String TAG = "Capture 위젯";

    @Override
    public void onEnabled(Context context) {
        super.onEnabled(context);
    }

    @Override
    public void onDisabled(Context context) {
        super.onDisabled(context);
    }

    @Override
    public void onReceive(Context context, Intent intent) {
        super.onReceive(context, intent);   // ** 처음 상태 체크
        RemoteViews remoteViews = new RemoteViews(context.getPackageName(),
                R.layout.appwidget);
        AppWidgetManager appWidgetManager = AppWidgetManager.getInstance(context);
        ComponentName thisAppWidget = new ComponentName(context, CaptureWidgetProvider.class);
        int[] appWidgets = appWidgetManager.getAppWidgetIds(thisAppWidget);

        final String action = intent.getAction();
        Log.e(TAG, "Provider onReceive action: " + action);

        if(AppWidgetManager.ACTION_APPWIDGET_UPDATE.equals(action)) {
            Bundle extras = intent.getExtras();
            if(extras!=null)
            {
                int [] appWidgetIds = extras.getIntArray(AppWidgetManager.EXTRA_APPWIDGET_IDS);
                if(appWidgetIds!=null && appWidgetIds.length>0)
                    this.onUpdate(context, appWidgetManager, appWidgetIds);
            }
        } else if(MusicService.BroadcastActions.PREPARED.equals(action)) {
            this.onUpdate(context, appWidgetManager, appWidgets); // 재생음악 파일이 변경된 경우.
        } else if(MusicService.BroadcastActions.PLAY_STATE_CHANGED.equals(action)) {
            this.onUpdate(context, appWidgetManager, appWidgets); //재생상태 변경
        }

//        if(MusicService.BroadcastActions.PREPARED.equals(action)) {
//
//        }
//        updatePlayState(context, remoteViews); // 재생상태 업데이트.
        updateCaptureAppWidget(context, remoteViews);
        updateWidget(context, remoteViews); // 앱위젯 업데이트.
    }

    @Override
    public void onUpdate(Context context, AppWidgetManager appWidgetManager, int[] appWidgetIds) {
        super.onUpdate(context, appWidgetManager, appWidgetIds);

//        RemoteViews remoteViews = new RemoteViews(context.getPackageName(), R.layout.appwidget);
////        updateCaptureAppWidget(context, remoteViews);
//        // There may be multiple widgets active, so update all of them
//        for (int appWidgetId : appWidgetIds) {
//            appWidgetManager.updateAppWidget(appWidgetId, remoteViews);
//            Log.d(TAG, "Update widget, ID: " + String.valueOf(appWidgetId));
//        }
    }

    public void updateCaptureAppWidget(Context context, RemoteViews remoteViews) {
//        RemoteViews remoteViews = new RemoteViews(context.getPackageName(), R.layout.appwidget);
//        Bitmap bitmap = MusicPlayerActivity.getInstance().getServiceInterface().getAudioItem().mBitmap;
//        remoteViews.setImageViewBitmap(R.id.img_albumart, bitmap);

        updatePlayState(context, remoteViews); // 재생상태 업데이트.

        Intent actionLaunch = new Intent(context, MusicPlayerActivity.class);
        Intent actionTogglePlay = new Intent(NotificationPlayer.CommandActions.TOGGLE_PLAY);
        Intent actionForward = new Intent(NotificationPlayer.CommandActions.FORWARD);
        Intent actionRewind = new Intent(NotificationPlayer.CommandActions.REWIND);
        Intent actionClose = new Intent(NotificationPlayer.CommandActions.CLOSE);

        PendingIntent launch = PendingIntent.getActivity(context, 0, actionLaunch, PendingIntent.FLAG_UPDATE_CURRENT);
        PendingIntent togglePlay = PendingIntent.getService(context, 0, actionTogglePlay, PendingIntent.FLAG_UPDATE_CURRENT);
        PendingIntent forward = PendingIntent.getService(context, 0, actionForward, PendingIntent.FLAG_UPDATE_CURRENT);
        PendingIntent rewind = PendingIntent.getService(context, 0, actionRewind, PendingIntent.FLAG_UPDATE_CURRENT);
        PendingIntent close = PendingIntent.getService(context, 0, actionClose, PendingIntent.FLAG_UPDATE_CURRENT);

        remoteViews.setOnClickPendingIntent(R.id.img_albumart, launch);
        remoteViews.setOnClickPendingIntent(R.id.btn_play_pause, togglePlay);
        remoteViews.setOnClickPendingIntent(R.id.btn_forward, forward);
        remoteViews.setOnClickPendingIntent(R.id.btn_rewind, rewind);
        remoteViews.setOnClickPendingIntent(R.id.btn_close, close);

        // Instruct the widget manager to update the widget
    }


    private void updateAlbumArt(Context context, RemoteViews remoteViews) {
        AppWidgetManager appWidgetManager = AppWidgetManager.getInstance(context);
        int appWidgetIds[] = appWidgetManager.getAppWidgetIds(new ComponentName(context, getClass()));
        long albumId = MusicPlayerActivity.getInstance().getServiceInterface().getAudioItem().mAlbumId;
        Uri albumArtUri = ContentUris.withAppendedId(Uri.parse("content://media/external/audio/albumart"), albumId);
        Picasso.get().load(albumArtUri).into(remoteViews, R.id.img_albumart, appWidgetIds);
    }

    private void updatePlayState(Context context, RemoteViews remoteViews) {

        Bitmap bitmap = null;
//        bitmap = BitmapFactory.decodeResource(Resources.getSystem(),  R.drawable.music_circle);

        if(MusicApplication.getInstance().getServiceInterface().getAudioItem() != null) {
            bitmap = MusicApplication.getInstance().getServiceInterface().getAudioItem().mBitmap;
        }
        if(bitmap == null) bitmap = BitmapFactory.decodeResource(context.getResources(), R.drawable.snow);

        remoteViews.setImageViewBitmap(R.id.img_albumart, bitmap);

//        try {
//            bitmap = MusicApplication.getInstance().getServiceInterface().getAudioItem().mBitmap;
//            remoteViews.setImageViewBitmap(R.id.img_albumart, bitmap);
//        } catch (Exception e){
//            remoteViews.setImageViewBitmap(R.id.img_albumart,
//                    BitmapFactory.decodeResource(context.getResources(), R.drawable.snow));
//        }


        String title = "재생중인 음악이 없습니다.";
        if (MusicApplication.getInstance().getServiceInterface().getAudioItem() != null) {
            title = MusicApplication.getInstance().getServiceInterface().getAudioItem().mTitle;
        }
        int fontSize = PreferenceManager.getDefaultSharedPreferences(context)
                .getInt("font_size", 12);
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.JELLY_BEAN) {
            remoteViews.setTextViewTextSize(R.id.txt_title, TypedValue.COMPLEX_UNIT_SP, fontSize);
        }
        remoteViews.setTextViewText(R.id.txt_title, title);


//        if(MusicApplication.getInstance() != null) {
        if (MusicApplication.getInstance().getServiceInterface().isPlaying()) {   // NullPointerException
            remoteViews.setImageViewResource(R.id.btn_play_pause, R.drawable.pause);
        } else {
            remoteViews.setImageViewResource(R.id.btn_play_pause, R.drawable.play);
        }
//        } else {
//            remoteViews.setImageViewResource(R.id.btn_play_pause, R.drawable.play);
//        }

        Log.d(TAG, "updatePlayState : " + title + ",  Playing : "
                + MusicApplication.getInstance().getServiceInterface().isPlaying());
    }

    private void updateWidget(Context context, RemoteViews remoteViews) {
        AppWidgetManager appWidgetManager = AppWidgetManager.getInstance(context);
        int appWidgetIds[] = appWidgetManager.getAppWidgetIds(new ComponentName(context, getClass()));
        if (appWidgetIds != null && appWidgetIds.length > 0) {
            appWidgetManager.updateAppWidget(appWidgetIds, remoteViews);
        }
    }


//    public class PlayerWidgetConnection implements ServiceConnection {
//        @Override
//        public void onServiceConnected(ComponentName name, IBinder service) {
//            CaptureWidgetProvider.mService = MusicApplication.getInstance().getServiceInterface().mService;
////                    ((MusicService.PlayerWidgetServiceBinder)service).getService();
//            CaptureWidgetProvider.mService.sendBroadcast(
//                    new Intent(MusicService.BroadcastActions.PLAY_STATE_CHANGED));
////                    new Intent(MusicService.WIDGET_UPDATE_ACTION));
//            Intent i = new Intent();
//            i.setAction("android.intent.action.MEDIA_SVC");
//            CaptureWidgetProvider.mService.sendBroadcast( i );
//        }
//
//        @Override
//        public void onServiceDisconnected(ComponentName name) {
//            CaptureWidgetProvider.mService = null;
//        }
//    }
}
