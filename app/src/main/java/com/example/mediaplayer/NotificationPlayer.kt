package com.example.mediaplayer

import android.annotation.SuppressLint
import android.app.*
import android.content.*
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.graphics.Color
import android.net.Uri
import android.os.AsyncTask
import android.os.Build
import android.util.Log
import android.widget.RemoteViews
import androidx.core.app.NotificationCompat
import com.example.mediaplayer.services.MusicService
import com.squareup.picasso.Picasso

class NotificationPlayer constructor(private val mService: MusicService) {
    private val mNotificationManager: NotificationManager
    private var mNotificationManagerBuilder: NotificationManagerBuilder? = null
    private var isForeground: Boolean
    private val CHANNEL_ID: String = "My channel ID"
    private val CHANNEL_NAME: String = "My channel"
    @SuppressLint("StaticFieldLeak")
    fun updateNotificationPlayer() {
        cancel()
        // ==================================== NotificationManagerBuilder() 없이 하나로 실행..
        object : AsyncTask<Void?, Void?, Notification>() {
            var mNotificationBuilder: NotificationCompat.Builder? = null
            override fun doInBackground(vararg params: Void?): Notification? {
//   bitmap largeIcon 만드는 법
//                Uri albumArtUri = ContentUris.withAppendedId(Uri.parse("content://media/external/audio/albumart"), mService.mAudioItem.mAlbumId);
//                Bitmap largIcon = null;
//                try {
//                    largIcon = Picasso.with(mService).load(albumArtUri).get();
//                } catch (IOException e) {
//                    e.printStackTrace();
//                }
                var largIcon: Bitmap? = mService.mAudioItem!!.mBitmap
                if (largIcon == null) {
                    largIcon = BitmapFactory.decodeResource(mService.getApplicationContext().getResources(),
                            R.drawable.snow) //?
                }
                val mainActivity: Intent = Intent(mService, MusicPlayerActivity::class.java)
                val actionTogglePlay: Intent = Intent(MusicService.Companion.TOGGLE_PLAY)
                val actionForward: Intent = Intent(MusicService.Companion.FORWARD)
                val actionRewind: Intent = Intent(MusicService.Companion.REWIND)
                val actionClose: Intent = Intent(MusicService.Companion.CLOSE)
                val togglePlay: PendingIntent = PendingIntent.getService(mService, 0, actionTogglePlay, 0)
                val forward: PendingIntent = PendingIntent.getService(mService, 0, actionForward, 0)
                val rewind: PendingIntent = PendingIntent.getService(mService, 0, actionRewind, PendingIntent.FLAG_UPDATE_CURRENT)
                val close: PendingIntent = PendingIntent.getService(mService, 0, actionClose, 0)
                val main: PendingIntent = PendingIntent.getActivity(mService, 0, mainActivity, 0)

//-------------------------------------------------------
                val manager: NotificationManager? = mService.getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager?
                mNotificationBuilder = NotificationCompat.Builder(mService, CHANNEL_ID)
                mNotificationBuilder!!
                        .setContentTitle(mService.mAudioItem!!.mTitle)
                        .setContentText(mService.mAudioItem!!.mArtist) //.setStyle(new NotificationCompat.BigTextStyle().bigText("더 많은 내용을 보여줘야 하는 경우..."))
                        .setLargeIcon(largIcon)
                        .setContentIntent(main)
                        .setPriority(NotificationCompat.PRIORITY_DEFAULT)
                        .setAutoCancel(true)
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                    mNotificationBuilder!!.setSmallIcon(R.drawable.music_circle) // xml file only?
                    val channel: NotificationChannel = NotificationChannel(CHANNEL_ID, CHANNEL_NAME,
                            NotificationManager.IMPORTANCE_DEFAULT)
                    channel.setDescription("오레오 이상?")
                    assert(manager != null)
                    manager!!.createNotificationChannel(channel)
                } else {
                    mNotificationBuilder!!.setSmallIcon(R.drawable.musiccircle) // png file only??
                }
                mNotificationBuilder!!.addAction(NotificationCompat.Action(R.drawable.skip_previous, "", rewind))
                mNotificationBuilder!!.addAction(NotificationCompat.Action(if (mService.isPlaying()) R.drawable.pause else R.drawable.play, "", togglePlay))
                mNotificationBuilder!!.addAction(NotificationCompat.Action(R.drawable.skip_next, "", forward))
                mNotificationBuilder!!.addAction(NotificationCompat.Action(R.drawable.stop, "", close))
                val actionsViewIndexs: IntArray = intArrayOf(0, 1, 2)
                mNotificationBuilder!!.setStyle(androidx.media.app.NotificationCompat.MediaStyle().setShowActionsInCompactView(*actionsViewIndexs))
                val notification: Notification = mNotificationBuilder!!.build()
                assert(manager != null)
                manager!!.notify(NOTIFICATION_PLAYER_ID, notification)
                if (!isForeground) {
                    isForeground = true
                    // 서비스를 Foreground 상태로 만든다
                    mService.startForeground(NOTIFICATION_PLAYER_ID, notification)
                }
                return notification
            } // doBackground
        }.execute()
        // ====================================
    }

    fun removeNotificationPlayer() {
        cancel()
        mService.stopForeground(true)
        isForeground = false
    }

    private fun cancel() {
        if (mNotificationManagerBuilder != null) {
            mNotificationManagerBuilder!!.cancel(true)
            mNotificationManagerBuilder = null
        }
    }

    // ========사용하지 않음
    @SuppressLint("StaticFieldLeak")
    private inner class NotificationManagerBuilder constructor() : AsyncTask<Void?, Void?, Notification>() {
        private var mRemoteViews: RemoteViews? = null
        private var mNotificationBuilder: NotificationCompat.Builder? = null
        private var mMainPendingIntent: PendingIntent? = null

        override fun doInBackground(vararg params: Void?): Notification? {
            Log.d("Notification", "Background 시작됨")
            mNotificationBuilder!!.setContent(mRemoteViews)
            mNotificationBuilder!!.setContentIntent(mMainPendingIntent)
            mNotificationBuilder!!.setPriority(Notification.PRIORITY_MAX)
            val notification: Notification = mNotificationBuilder!!.build()
            updateRemoteView(mRemoteViews, notification)
            return notification
        }

        override fun onPreExecute() {
            super.onPreExecute()
            Log.d("Notification", "PreExecute 수행됨")
            val mainActivity: Intent = Intent(mService, MusicPlayerActivity::class.java)
            mMainPendingIntent = PendingIntent.getActivity(mService, 0, mainActivity, 0)
            mRemoteViews = createRemoteView(R.layout.remote_view)
            //            mRemoteViews.setInt(R.id.artist_text, "setBackgroundColor", Color.YELLOW);
//            remoteViews.setTextViewText(R.id.resume_button1, "재생");

            val notification: Notification = createNotification()
            if (!isForeground) {
                isForeground = true
                // 서비스를 Foreground 상태로 만든다
                mService.startForeground(NOTIFICATION_PLAYER_ID, notification)
            }
        }

        private fun createNotification(): Notification {
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                val channel: NotificationChannel = NotificationChannel(CHANNEL_ID, CHANNEL_NAME,
                        NotificationManager.IMPORTANCE_DEFAULT)
                val manager: NotificationManager? = mService.getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager?
                assert(manager != null)
                manager!!.createNotificationChannel(channel)
                mNotificationBuilder = NotificationCompat.Builder(mService, CHANNEL_ID)
            } else {
                mNotificationBuilder = NotificationCompat.Builder(mService)
            }
            mNotificationBuilder!!.setSmallIcon(R.mipmap.ic_launcher)
                    .setOngoing(true)
                    .setContentIntent(mMainPendingIntent) //                    .setStyle(new androidx.media.app.NotificationCompat.MediaStyle().setMediaSession(MediaS))
                    .setContent(mRemoteViews)
            //---------------------------------

            // 색상
//            mNotificationBuilder.setColorized(true);
            mNotificationBuilder!!.setColor(Color.GREEN)

            //---------------------------------
            mNotificationBuilder!!.setPriority(Notification.PRIORITY_DEFAULT)
            return mNotificationBuilder!!.build()
        }

        override fun onPostExecute(notification: Notification) {
            super.onPostExecute(notification)
            Log.d("Notification", "PostExecute 수행됨")
            try {
                mNotificationManager.notify(NOTIFICATION_PLAYER_ID, notification)
            } catch (e: Exception) {
                e.printStackTrace()
            }
        }

        private fun createRemoteView(layoutId: Int): RemoteViews {
            val remoteView: RemoteViews = RemoteViews(mService.getPackageName(), layoutId)
            val actionTogglePlay: Intent = Intent(MusicService.Companion.TOGGLE_PLAY)
            val actionForward: Intent = Intent(MusicService.Companion.FORWARD)
            val actionRewind: Intent = Intent(MusicService.Companion.REWIND)
            val actionClose: Intent = Intent(MusicService.Companion.CLOSE)
            val togglePlay: PendingIntent = PendingIntent.getService(mService, 0, actionTogglePlay, 0)
            val forward: PendingIntent = PendingIntent.getService(mService, 0, actionForward, 0)
            val rewind: PendingIntent = PendingIntent.getService(mService, 0, actionRewind, 0)
            val close: PendingIntent = PendingIntent.getService(mService, 0, actionClose, 0)
            remoteView.setOnClickPendingIntent(R.id.btn_play_pause, togglePlay)
            remoteView.setOnClickPendingIntent(R.id.btn_forward, forward)
            remoteView.setOnClickPendingIntent(R.id.btn_rewind, rewind)
            remoteView.setOnClickPendingIntent(R.id.btn_close, close)
            return remoteView
        }

        private fun updateRemoteView(remoteViews: RemoteViews?, notification: Notification) {
            if (mService.isPlaying()) {
                remoteViews!!.setImageViewResource(R.id.btn_play_pause, R.drawable.pause)
            } else {
                remoteViews!!.setImageViewResource(R.id.btn_play_pause, R.drawable.play)
            }
            val title: String? = mService.mAudioItem!!.mTitle
            val artist: String? = mService.mAudioItem!!.mArtist
            val bitmap: Bitmap? = mService.mAudioItem!!.mBitmap
            remoteViews.setTextViewText(R.id.title_text, title)
            remoteViews.setTextViewText(R.id.artist_text, artist)
            val albumArtUri: Uri = ContentUris.withAppendedId(Uri.parse("content://media/external/audio/albumart"), mService.mAudioItem!!.mAlbumId)
            Picasso.get().load(albumArtUri).error(R.drawable.musiccircle).into((remoteViews), R.id.img_albumart, NOTIFICATION_PLAYER_ID, notification)
            //            if(bitmap != null) {
//                remoteViews.setImageViewBitmap(R.id.album_image, bitmap);
//            } else{
//                remoteViews.setImageViewResource(R.id.album_image, R.drawable.music_circle);
//            }
        }
    }

    companion object {
        private val NOTIFICATION_PLAYER_ID: Int = 0x342
    }

    init {
        mNotificationManager = mService.getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
        isForeground = false
    }
}