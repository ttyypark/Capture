package com.example.mediaplayer.services

import android.app.*
import android.content.*
import android.database.Cursor
import android.media.AudioAttributes
import android.media.MediaMetadataRetriever
import android.media.MediaPlayer
import android.media.MediaPlayer.*
import android.net.Uri
import android.os.*
import android.provider.MediaStore
import android.util.Log
import androidx.annotation.RequiresApi
import com.example.mediaplayer.*
import com.example.mediaplayer.AudioAdapter.AudioItem
import com.example.mediaplayer.frags.MusicPlayerFragment
import com.example.mediaplayer.services.MusicService
import org.greenrobot.eventbus.EventBus
import java.io.IOException
import java.util.*

//        EventBus.getDefault().post(isPlaying());    // fragment쪽 UI
//        updateNotificationPlayer();                 // Notification쪽 UI
//        sendBroadcast(new Intent(BroadcastActions.PREPARED,  // Widget쪽

class MusicService : Service() {
    private var isPrepared: Boolean = false
    private var mSongList: ArrayList<Uri>? = null
    private var mSongID: ArrayList<Long>? = null
    var mAudioItem: AudioItem? = null
    private var mCurrentUri: Uri? = null
    private var mIndex: Int = 0 // mCurrentPosition;

    private val mBinder: IBinder = LocalBinder()
    var mMediaPlayer: MediaPlayer? = null
    private val mRetriever: MediaMetadataRetriever? = null
    private var mNotificationPlayer: NotificationPlayer? = null
    private var mContext: Context? = null

    object BroadcastActions {
        const val PREPARED: String = "PREPARED"
        const val PLAY_STATE_CHANGED: String = "PLAY_STATE_CHANGED"
    }

    companion object {
        const val ACTION_PREV: String = "prev"
        const val ACTION_NEXT: String = "next"
        const val ACTION_PLAY: String = "play"
        const val ACTION_RESUME: String = "resume"
        const val REWIND: String = "REWIND"
        const val TOGGLE_PLAY: String = "TOGGLE_PLAY"
        const val FORWARD: String = "FORWARD"
        const val CLOSE: String = "CLOSE"
        const val TAG: String = "MusicService"
    }

    inner class LocalBinder : Binder() {
        fun getService(): MusicService {
            // Return this instance of LocalService so clients can call public methods
            return this@MusicService
        }
    }


    override fun onTaskRemoved(rootIntent: Intent) {
        super.onTaskRemoved(rootIntent)
        stopSelf() // 서비스 종료 ?????
        Log.d(TAG, "*****************서비스 종료됨 onTaskRemoved")
    }

    override fun onDestroy() {
        Log.d(TAG, "*****************서비스 종료됨,  MP 종료")
        super.onDestroy()
        if (mMediaPlayer != null) {
            mMediaPlayer!!.stop()
            mMediaPlayer!!.release()
            mMediaPlayer = null
        }
    }

    override fun onBind(intent: Intent): IBinder? {
        Log.d(TAG, "Bind 시작됨")
        return mBinder
    }

    override fun onUnbind(intent: Intent): Boolean {
        Log.d(TAG, "Bind 끝남")
        return super.onUnbind(intent)
    }

    override fun onCreate() {
        super.onCreate()
        Log.d(TAG, "********************서비스 시작됨, MP 생성")
        mContext = applicationContext
        val mAudioAdapter = AudioAdapter(mContext, null) // mSongList 설정
        mSongList = mAudioAdapter.mSongList
        mSongID = mAudioAdapter.mSongID
        mAudioItem = AudioItem()
//-----------------------------------------------------------
        mNotificationPlayer = NotificationPlayer(this) //****시작, 생성

        mMediaPlayer = MediaPlayer()
        mMediaPlayer!!.setWakeMode(applicationContext, PowerManager.PARTIAL_WAKE_LOCK)

//-----------------------------------------------------------
        mMediaPlayer!!.setOnPreparedListener(object : OnPreparedListener {
            @RequiresApi(Build.VERSION_CODES.Q)
            override fun onPrepared(mp: MediaPlayer) {
                isPrepared = true
                mp.start()
                mIndex = mSongList!!.indexOf(mCurrentUri) // 중복?
                queryAudioItem(mIndex)
                /**
                 * [MusicControllerFragment.updateUI]
                 */
                /**
                 * [MusicPlayerFragment.updateUI]
                 */
                EventBus.getDefault().post(isPlaying()) // fragment쪽 UI는  eventBus로
                updateNotificationPlayer() // Notification쪽
                sendBroadcast(Intent(BroadcastActions.PREPARED,  // Widget쪽 // action
                        Uri.EMPTY,  // data
                        applicationContext,  // context
                        CaptureWidgetProvider::class.java)) //class
            }
        })

        mMediaPlayer!!.setOnCompletionListener(object : OnCompletionListener {
            override fun onCompletion(mp: MediaPlayer) {
                nextMusic()
                updateNotificationPlayer() // Notification쪽
                sendBroadcast(Intent(BroadcastActions.PLAY_STATE_CHANGED)) // Widget 재생상태 변경
            }
        })

        mMediaPlayer!!.setOnErrorListener(object : MediaPlayer.OnErrorListener {
            override fun onError(mp: MediaPlayer, what: Int, extra: Int): Boolean {
                isPrepared = false
                updateNotificationPlayer() // Notification쪽
                sendBroadcast(Intent(BroadcastActions.PLAY_STATE_CHANGED)) // Widget 재생상태 변경
                return false
            }
        })

        mMediaPlayer!!.setOnSeekCompleteListener(object : OnSeekCompleteListener {
            override fun onSeekComplete(mp: MediaPlayer) {}
        })

//---------------------------------------------------------------------
    }

    //----------------------------------------------------------------------
    private fun updateNotificationPlayer() {
        if (mNotificationPlayer != null) {
            mNotificationPlayer!!.updateNotificationPlayer()
        }
    }

    fun removeNotificationPlayer() {
        if (mNotificationPlayer != null) {
            mNotificationPlayer!!.removeNotificationPlayer()
        }
    }

//----------------------------------------------------------------------
    override fun onStartCommand(intent: Intent, flags: Int, startId: Int): Int {
//      Error 인 상태에서는 음악 재생을 다시 시작...Stack check 필요?
        if (mMediaPlayer == null || mSongList == null) {
            val musicIntent = Intent(applicationContext, MusicPlayerActivity::class.java)
            musicIntent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
            startActivity(musicIntent)
        }

        val action: String? = intent.action
        Log.i(TAG, "onStartCommand 시작됨: $action")
        when (action) {
            ACTION_PLAY -> playMusic(intent.getParcelableExtra<Parcelable>("uri") as Uri?)
            ACTION_RESUME -> clickResumeButton()
            ACTION_NEXT -> nextMusic()
            ACTION_PREV -> prevMusic()
//-------------------------------------------
            TOGGLE_PLAY -> clickResumeButton()
            REWIND -> prevMusic()
            FORWARD -> nextMusic()
            CLOSE -> {
                //   foreground service가 종료된 경우
                removeNotificationPlayer()
                if (Foreground.isBackground()) {  // Activity 꺼지고 Notification만 있을때
                    Log.d(TAG, "Activity 가 백그라은드에 있음")
                    mMediaPlayer!!.stop()
                    mMediaPlayer!!.release()
                    mMediaPlayer = null
                } else {
                    Log.d(TAG, "Activity가 포그라은드에 있음")
                    /**
                     * [com.example.mediaplayer.MusicPlayerActivity.stopPlayer]
                     */
                    EventBus.getDefault().post(3)
                }

                // Widget 상태변경 ****
                sendBroadcast(Intent(BroadcastActions.PLAY_STATE_CHANGED,  // action
                        Uri.EMPTY,  // data
                        applicationContext,  // context
                        CaptureWidgetProvider::class.java)) //class
                try {
                    onDestroy() // 서비스 종료?
                } catch (e: Exception) {
                    Log.e(TAG, " *******  서비스 종료 error")
                }

// system 종료 -------------------------
                Process.killProcess(Process.myPid())
            }
        }
        return START_STICKY
//        return super.onStartCommand(intent, flags, startId);
    }

    fun getMediaPlayer(): MediaPlayer? {
        return mMediaPlayer
    }

    fun getMetaDataRetriever(): MediaMetadataRetriever? {
        return mRetriever
    }

    fun setmSongList(songList: ArrayList<Uri>) {
        if (mSongList != songList) {
            mSongList!!.clear()
            mSongList!!.addAll(songList)
        }
    }

    private fun prepare(uri: Uri?) {
        try {
//            // 현재 재생중인 정보
//            mRetriever = new MediaMetadataRetriever();
//            mRetriever.setDataSource(this, uri);
            if (mMediaPlayer != null) {
                mMediaPlayer!!.stop()
                mMediaPlayer!!.reset()
            } else {
                mMediaPlayer = MediaPlayer()
            }
//            mMediaPlayer.setAudioStreamType(AudioManager.STREAM_MUSIC);
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
                mMediaPlayer!!.setAudioAttributes(
                        AudioAttributes.Builder()
                                .setContentType(AudioAttributes.CONTENT_TYPE_MUSIC)
                                .build())
            }
            mMediaPlayer!!.setDataSource(this, (uri)!!)
            mMediaPlayer!!.prepareAsync()
        } catch (e: IOException) {
            e.printStackTrace()
        }
    }

    private fun stop() {
        mMediaPlayer!!.stop()
        mMediaPlayer!!.reset()
    }

    fun playMusic(uri: Uri?) {
        mCurrentUri = uri
//        mIndex = mSongList.indexOf(mCurrentUri);
//        queryAudioItem(mIndex);
        stop()
        prepare(uri)
    }

    fun nextMusic() {
        mIndex++
        if (mIndex > mSongList!!.size - 1) {
            mIndex = 0
        }
        playMusic(mSongList!![mIndex])
    }

    fun prevMusic() {
        mIndex--
        if (mIndex < 0) {
            mIndex = mSongList!!.size - 1
        }
        playMusic(mSongList!![mIndex])
    }

    fun clickResumeButton() {
        if (mMediaPlayer == null) return  // widget에서 toggle
        if (isPlaying()) {
            mMediaPlayer!!.pause()
        } else {
            mMediaPlayer!!.start()
        }
        // Fragment 상태변경 ****
        /**
         * [com.example.mediaplayer.frags.MusicControllerFragment.updateUI] (Boolean)}
         */
        /**
         * [MusicPlayerFragment.updateUI] (Boolean)}
         */
        EventBus.getDefault().post(isPlaying())

// Widget 상태변경 ****
        sendBroadcast(Intent(BroadcastActions.PLAY_STATE_CHANGED,  // action
                Uri.EMPTY,  // data
                applicationContext,  // context
                CaptureWidgetProvider::class.java)) //class

// Notification 상태변경 ****
//            // foreground service
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {   //**************
//            startForegroundService();
            updateNotificationPlayer()
        }
    }

    fun isPlaying(): Boolean {
        if (mMediaPlayer != null) {
            return mMediaPlayer!!.isPlaying
        }
        return false
    }

    // 사용 안함
    //    public AudioAdapter.AudioItem getAudioItem(){
    ////---------------------------------- AudioItem 만들기 from mCurrentUri
    //        if (mCurrentUri == null) return null;
    //        MediaMetadataRetriever retriever = new MediaMetadataRetriever();
    //        retriever.setDataSource(getApplicationContext(), mCurrentUri);
    //        final AudioAdapter.AudioItem audioItem = AudioAdapter.AudioItem.bindRetriever(retriever);
    //        return audioItem;
    //    }

    @RequiresApi(Build.VERSION_CODES.Q)
    fun queryAudioItem(position: Int) {
        val audioId: Long = mSongID!![position]
        val uri: Uri = MediaStore.Audio.Media.EXTERNAL_CONTENT_URI
        val projection: Array<String> = arrayOf(
                MediaStore.Audio.Media._ID,
                MediaStore.Audio.Media.TITLE,
                MediaStore.Audio.Media.ARTIST,
                MediaStore.Audio.Media.ALBUM,
                MediaStore.Audio.Media.ALBUM_ID,
                MediaStore.Audio.Media.DURATION,
                MediaStore.Audio.Media.DATA
        )
        val selection: String = MediaStore.Audio.Media._ID + " = ?"
        val selectionArgs: Array<String> = arrayOf(audioId.toString())
        val cursor: Cursor? = contentResolver.query(uri, projection, selection, selectionArgs, null)
        if (cursor != null) {
            if (cursor.count > 0) {
                cursor.moveToFirst()
                mAudioItem = AudioItem.bindCursor(mContext, cursor)
            }
            cursor.close()
        }
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