package com.example.mediaplayer

import android.app.PendingIntent
import android.appwidget.AppWidgetManager
import android.appwidget.AppWidgetProvider
import android.content.*
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.os.Build
import android.os.Bundle
import android.util.Log
import android.util.TypedValue
import android.widget.RemoteViews
import com.example.mediaplayer.services.MusicService
import com.example.mediaplayer.services.MusicService.BroadcastActions

class CaptureWidgetProvider constructor() : AppWidgetProvider() {
    public override fun onEnabled(context: Context) {
        super.onEnabled(context)
    }

    public override fun onDisabled(context: Context) {
        super.onDisabled(context)
    }

    public override fun onReceive(context: Context, intent: Intent) {
        super.onReceive(context, intent) // ** 처음 상태 체크
        val remoteViews: RemoteViews = RemoteViews(context.getPackageName(),
                R.layout.appwidget)
        val appWidgetManager: AppWidgetManager = AppWidgetManager.getInstance(context)
        val thisAppWidget: ComponentName = ComponentName(context, CaptureWidgetProvider::class.java)
        val appWidgets: IntArray = appWidgetManager.getAppWidgetIds(thisAppWidget)
        val action: String? = intent.getAction()
        Log.e(TAG, "Provider onReceive action: " + action)
        if ((AppWidgetManager.ACTION_APPWIDGET_UPDATE == action)) {
            val extras: Bundle? = intent.getExtras()
            if (extras != null) {
                val appWidgetIds: IntArray? = extras.getIntArray(AppWidgetManager.EXTRA_APPWIDGET_IDS)
                if (appWidgetIds != null && appWidgetIds.size > 0) onUpdate(context, appWidgetManager, appWidgetIds)
            }
        } else if ((BroadcastActions.PREPARED == action)) {
            updateAlbumArt(context, remoteViews) // 재생음악 파일이 변경된 경우.
        } else if ((BroadcastActions.PLAY_STATE_CHANGED == action)) {
        }
        updateCaptureAppWidget(context, remoteViews) // 재생상태 업데이트.
        updateWidget(context, remoteViews) // 앱위젯 업데이트.
    }

    public override fun onDeleted(context: Context, appWidgetIds: IntArray) {
        // When the user deletes the widget, delete the preference associated with it.
        for (appWidgetId: Int in appWidgetIds) {
            CaptureWidgetConfigure.Companion.deleteTextSizePref(context)
        }
    }

    public override fun onUpdate(context: Context, appWidgetManager: AppWidgetManager, appWidgetIds: IntArray) {
        super.onUpdate(context, appWidgetManager, appWidgetIds)

//        RemoteViews remoteViews = new RemoteViews(context.getPackageName(), R.layout.appwidget);
////        updateCaptureAppWidget(context, remoteViews);
//        // There may be multiple widgets active, so update all of them
//        for (int appWidgetId : appWidgetIds) {
//            appWidgetManager.updateAppWidget(appWidgetId, remoteViews);
//            Log.d(TAG, "Update widget, ID: " + String.valueOf(appWidgetId));
//        }
    }

    fun updateCaptureAppWidget(context: Context, remoteViews: RemoteViews) {
        updatePlayState(context, remoteViews) // 재생상태 업데이트.
        val actionLaunch: Intent = Intent(context, MainActivity::class.java)
        val actionConfig: Intent = Intent(context, CaptureWidgetConfigure::class.java)
        val actionTogglePlay: Intent = Intent(MusicService.Companion.TOGGLE_PLAY)
        val actionForward: Intent = Intent(MusicService.Companion.FORWARD)
        val actionRewind: Intent = Intent(MusicService.Companion.REWIND)
        val actionClose: Intent = Intent(MusicService.Companion.CLOSE)
        val launch: PendingIntent = PendingIntent.getActivity(context, 0, actionLaunch, PendingIntent.FLAG_UPDATE_CURRENT)
        val config: PendingIntent = PendingIntent.getActivity(context, 0, actionConfig, PendingIntent.FLAG_UPDATE_CURRENT)
        val togglePlay: PendingIntent = PendingIntent.getService(context, 0, actionTogglePlay, PendingIntent.FLAG_UPDATE_CURRENT)
        val forward: PendingIntent = PendingIntent.getService(context, 0, actionForward, PendingIntent.FLAG_UPDATE_CURRENT)
        val rewind: PendingIntent = PendingIntent.getService(context, 0, actionRewind, PendingIntent.FLAG_UPDATE_CURRENT)
        val close: PendingIntent = PendingIntent.getService(context, 0, actionClose, PendingIntent.FLAG_UPDATE_CURRENT)
        remoteViews.setOnClickPendingIntent(R.id.img_albumart, launch)
        remoteViews.setOnClickPendingIntent(R.id.btn_setting, config)
        remoteViews.setOnClickPendingIntent(R.id.btn_play_pause, togglePlay)
        remoteViews.setOnClickPendingIntent(R.id.btn_forward, forward)
        remoteViews.setOnClickPendingIntent(R.id.btn_rewind, rewind)
        remoteViews.setOnClickPendingIntent(R.id.btn_close, close)

        // Instruct the widget manager to update the widget
    }

    private fun updateAlbumArt(context: Context, remoteViews: RemoteViews) {
//        AppWidgetManager appWidgetManager = AppWidgetManager.getInstance(context);
//        int appWidgetIds[] = appWidgetManager.getAppWidgetIds(new ComponentName(context, getClass()));
//        long albumId = MusicPlayerActivity.getInstance().getServiceInterface().mAudioItem().mAlbumId;
//        Uri albumArtUri = ContentUris.withAppendedId(Uri.parse("content://media/external/audio/albumart"), albumId);
//        Picasso.get().load(albumArtUri).into(remoteViews, R.id.img_albumart, appWidgetIds);
        var bitmap: Bitmap? = null
        if (MusicApplication.Companion.getInstance()!!.getServiceInterface()!!.mAudioItem() != null) {
            bitmap = MusicApplication.Companion.getInstance()!!.getServiceInterface()!!.mAudioItem()!!.mBitmap
        }
        if (bitmap == null) bitmap = BitmapFactory.decodeResource(context.getResources(), R.drawable.snow)
        remoteViews.setImageViewBitmap(R.id.img_albumart, bitmap)
    }

    private fun updatePlayState(context: Context, remoteViews: RemoteViews) {
        if (MusicApplication.Companion.getInstance()!!.getServiceInterface()!!.isPlaying()) {   // NullPointerException
            remoteViews.setImageViewResource(R.id.btn_play_pause, R.drawable.pause)
        } else {
            remoteViews.setImageViewResource(R.id.btn_play_pause, R.drawable.play)
        }
        var title: String? = "재생중인 음악이 없습니다."
        var artist: String? = "무명"
        if (MusicApplication.Companion.getInstance()!!.getServiceInterface()!!.mAudioItem() != null) {
            title = MusicApplication.Companion.getInstance()!!.getServiceInterface()!!.mAudioItem()!!.mTitle
            artist = MusicApplication.Companion.getInstance()!!.getServiceInterface()!!.mAudioItem()!!.mArtist
        }
        //        int fontSize = PreferenceManager.getDefaultSharedPreferences(context)
//                .getInt("font_size", 12);
        val fontSize: Int = CaptureWidgetConfigure.Companion.loadTextSizePref(context) // Configure의 SharedPreference에서 가져옴.
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.JELLY_BEAN) {
            remoteViews.setTextViewTextSize(R.id.txt_title, TypedValue.COMPLEX_UNIT_SP, fontSize.toFloat())
        }
        remoteViews.setTextViewText(R.id.txt_title, title)
        remoteViews.setTextViewText(R.id.txt_artist, artist)
        Log.d(TAG, ("updatePlayState : " + title + ",  Playing : "
                + MusicApplication.Companion.getInstance()!!.getServiceInterface()!!.isPlaying()))
    }

    private fun updateWidget(context: Context, remoteViews: RemoteViews) {
        val appWidgetManager: AppWidgetManager = AppWidgetManager.getInstance(context)
        val appWidgetIds: IntArray? = appWidgetManager.getAppWidgetIds(ComponentName(context, javaClass))
        if (appWidgetIds != null && appWidgetIds.size > 0) {
            appWidgetManager.updateAppWidget(appWidgetIds, remoteViews)
        }
    } //    public class PlayerWidgetConnection implements ServiceConnection {

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
    companion object {
        private val TAG: String = "Capture 위젯"
    }
}