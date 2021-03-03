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
            updateAlbumArt(context, remoteViews);           // 재생음악 파일이 변경된 경우.
        } else if(MusicService.BroadcastActions.PLAY_STATE_CHANGED.equals(action)) {
        }

        updateCaptureAppWidget(context, remoteViews);   // 재생상태 업데이트.
        updateWidget(context, remoteViews);             // 앱위젯 업데이트.
    }

    @Override
    public void onDeleted(Context context, int[] appWidgetIds) {
        // When the user deletes the widget, delete the preference associated with it.
        for (int appWidgetId : appWidgetIds) {
            CaptureWidgetConfigure.deleteTextSizePref(context);
        }
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

        updatePlayState(context, remoteViews); // 재생상태 업데이트.

        Intent actionLaunch = new Intent(context, MainActivity.class);
        Intent actionConfig = new Intent(context, CaptureWidgetConfigure.class);
        Intent actionTogglePlay = new Intent(MusicService.TOGGLE_PLAY);
        Intent actionForward = new Intent(MusicService.FORWARD);
        Intent actionRewind = new Intent(MusicService.REWIND);
        Intent actionClose = new Intent(MusicService.CLOSE);

        PendingIntent launch = PendingIntent.getActivity(context, 0, actionLaunch, PendingIntent.FLAG_UPDATE_CURRENT);
        PendingIntent config = PendingIntent.getActivity(context, 0, actionConfig, PendingIntent.FLAG_UPDATE_CURRENT);
        PendingIntent togglePlay = PendingIntent.getService(context, 0, actionTogglePlay, PendingIntent.FLAG_UPDATE_CURRENT);
        PendingIntent forward = PendingIntent.getService(context, 0, actionForward, PendingIntent.FLAG_UPDATE_CURRENT);
        PendingIntent rewind = PendingIntent.getService(context, 0, actionRewind, PendingIntent.FLAG_UPDATE_CURRENT);
        PendingIntent close = PendingIntent.getService(context, 0, actionClose, PendingIntent.FLAG_UPDATE_CURRENT);

        remoteViews.setOnClickPendingIntent(R.id.img_albumart, launch);
        remoteViews.setOnClickPendingIntent(R.id.btn_setting, config);
        remoteViews.setOnClickPendingIntent(R.id.btn_play_pause, togglePlay);
        remoteViews.setOnClickPendingIntent(R.id.btn_forward, forward);
        remoteViews.setOnClickPendingIntent(R.id.btn_rewind, rewind);
        remoteViews.setOnClickPendingIntent(R.id.btn_close, close);

        // Instruct the widget manager to update the widget
    }


    private void updateAlbumArt(Context context, RemoteViews remoteViews) {
//        AppWidgetManager appWidgetManager = AppWidgetManager.getInstance(context);
//        int appWidgetIds[] = appWidgetManager.getAppWidgetIds(new ComponentName(context, getClass()));
//        long albumId = MusicPlayerActivity.getInstance().getServiceInterface().mAudioItem().mAlbumId;
//        Uri albumArtUri = ContentUris.withAppendedId(Uri.parse("content://media/external/audio/albumart"), albumId);
//        Picasso.get().load(albumArtUri).into(remoteViews, R.id.img_albumart, appWidgetIds);

        Bitmap bitmap = null;
        if(MusicApplication.getInstance().getServiceInterface().mAudioItem() != null) {
            bitmap = MusicApplication.getInstance().getServiceInterface().mAudioItem().mBitmap;
        }
        if(bitmap == null) bitmap = BitmapFactory.decodeResource(context.getResources(), R.drawable.snow);
        remoteViews.setImageViewBitmap(R.id.img_albumart, bitmap);
    }

    private void updatePlayState(Context context, RemoteViews remoteViews) {

        if (MusicApplication.getInstance().getServiceInterface().isPlaying()) {   // NullPointerException
            remoteViews.setImageViewResource(R.id.btn_play_pause, R.drawable.pause);
        } else {
            remoteViews.setImageViewResource(R.id.btn_play_pause, R.drawable.play);
        }
        String title = "재생중인 음악이 없습니다.";
        String artist = "무명";
        if (MusicApplication.getInstance().getServiceInterface().mAudioItem() != null) {
            title = MusicApplication.getInstance().getServiceInterface().mAudioItem().mTitle;
            artist = MusicApplication.getInstance().getServiceInterface().mAudioItem().mArtist;
        }
//        int fontSize = PreferenceManager.getDefaultSharedPreferences(context)
//                .getInt("font_size", 12);
        int fontSize = CaptureWidgetConfigure.loadTextSizePref(context);  // Configure의 SharedPreference에서 가져옴.
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.JELLY_BEAN) {
            remoteViews.setTextViewTextSize(R.id.txt_title, TypedValue.COMPLEX_UNIT_SP, fontSize);
        }
        remoteViews.setTextViewText(R.id.txt_title, title);
        remoteViews.setTextViewText(R.id.txt_artist, artist);

        Log.d(TAG, "updatePlayState : " + title + ",  Playing : "
                + MusicApplication.getInstance().getServiceInterface().isPlaying());
    }

    private void updateWidget(Context context, RemoteViews remoteViews) {
        AppWidgetManager appWidgetManager = AppWidgetManager.getInstance(context);
        int[] appWidgetIds = appWidgetManager.getAppWidgetIds(new ComponentName(context, getClass()));
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
