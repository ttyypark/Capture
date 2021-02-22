package com.example.mediaplayer.frags

import android.content.ComponentName
import android.content.Intent
import android.content.ServiceConnection
import android.graphics.Bitmap
import android.media.MediaPlayer
import android.net.Uri
import android.os.Bundle
import android.os.CountDownTimer
import android.os.IBinder
import android.util.Log
import android.view.*
import android.widget.*
import android.widget.SeekBar.OnSeekBarChangeListener
import androidx.fragment.app.Fragment
import com.bumptech.glide.Glide
import com.example.mediaplayer.CaptureWidgetProvider
import com.example.mediaplayer.MainActivity
import com.example.mediaplayer.MusicApplication
import com.example.mediaplayer.R
import com.example.mediaplayer.services.MusicService
import com.example.mediaplayer.services.MusicService.BroadcastActions
import com.example.mediaplayer.services.MusicService.LocalBinder
import org.greenrobot.eventbus.EventBus
import org.greenrobot.eventbus.Subscribe
import java.util.*

//import com.squareup.otto.Subscribe;

class MusicPlayerFragment : Fragment(), View.OnClickListener {
    private var mService: MusicService? = null
    private var mBound: Boolean = false

    private var mAlbumImageView: ImageView? = null
    private var mDurationTextView: TextView? = null
    private var mCurrentTimeTextView: TextView? = null
    private var mCountDownTimer: CountDownTimer? = null
    private lateinit var mSeekBar: SeekBar

    private lateinit var mPlayLoop: Button
    private lateinit var mStopPlayer: Button
    private val TAG: String = "음악 플레이어"

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        return inflater.inflate(R.layout.music_player, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        mAlbumImageView = view.findViewById(R.id.album_image)
        mDurationTextView = view.findViewById(R.id.duration_text)
        mCurrentTimeTextView = view.findViewById(R.id.current_time_text)
        mSeekBar = view.findViewById(R.id.seekBar)
        mPlayLoop = view.findViewById(R.id.play_loop)
        mStopPlayer = view.findViewById(R.id.stop_player)

        mPlayLoop.setOnClickListener(this)
        mStopPlayer.setOnClickListener(this)
        mSeekBar.setOnSeekBarChangeListener(object : OnSeekBarChangeListener {
            override fun onProgressChanged(seekBar: SeekBar, progress: Int, fromUser: Boolean) {}
            override fun onStartTrackingTouch(seekBar: SeekBar) {}
            override fun onStopTrackingTouch(seekBar: SeekBar) {
                mService!!.getMediaPlayer()!!.seekTo(seekBar.progress)
            }
        })
    }

    override fun onStart() {
        super.onStart()
        Log.d(TAG, "시작됨 ")
        EventBus.getDefault().register(this)

// MusicApplication 활용
        if (mService == null) mService = MusicApplication.getInstance()!!.getServiceInterface()!!.mService
        updateUI(mService!!.isPlaying())
        mBound = true
//        Intent intent = new Intent(getActivity(), MusicService.class);
//        getActivity().bindService(intent, mConnection, Context.BIND_AUTO_CREATE);
    }

    override fun onStop() {
        super.onStop()
        EventBus.getDefault().unregister(this)
        if (mBound) {
//            // MusicApplication 활용
//            getActivity().unbindService(mConnection);
            mBound = false
        }
        Log.d(TAG, "종료됨 ")
    }

    private val mConnection: ServiceConnection = object : ServiceConnection {
        override fun onServiceConnected(className: ComponentName,
                                               service: IBinder) {
            // We've bound to LocalService, cast the IBinder and get LocalService instance
            val binder: LocalBinder = service as LocalBinder
            mService = binder.getService()
            mBound = true

            // UI 갱신
            updateUI(mService!!.isPlaying())
        }

        override fun onServiceDisconnected(arg0: ComponentName) {
            mBound = false
        }
    }

    @Subscribe
    fun updateUI(playing: Boolean) {
        if (playing) {
            if (mService == null) return  // *** player가 service에 연결되기 전
//            MediaMetadataRetriever retriever = mService.getMetaDataRetriever();
//            if (retriever != null) {
// ms값
            val longDuration: Int = mService!!.getMediaPlayer()!!.duration
            val min: Int = longDuration / 1000 / 60
            val sec: Int = longDuration / 1000 % 60

            mDurationTextView!!.text = String.format(Locale.KOREA, "%d:%02d", min, sec)
            mSeekBar.max = longDuration

            // 오디오 앨범 자켓 이미지
            val bitmap: Bitmap? = mService!!.mAudioItem!!.mBitmap

//                byte albumImage[] = retriever.getEmbeddedPicture();
            if (null != bitmap) {
                Glide.with(this).load(bitmap).into((mAlbumImageView)!!)
            } else {
                Glide.with(this).load(R.drawable.snow).into((mAlbumImageView)!!)
            }

            if (mService!!.getMediaPlayer()!!.isLooping) {
                mPlayLoop!!.text = "연속재생"
            } else {
                mPlayLoop!!.text = "반복재생"
            }
            //            }
        }
        updateTimer(playing)
    }

    private fun updateTimer(isPlaying: Boolean) {
        if (mService == null) return
        val mMediaPlayer: MediaPlayer? = mService!!.getMediaPlayer()
        if (!isPlaying) {
            if (mCountDownTimer != null) {
                mCountDownTimer!!.cancel()
            }
            mCountDownTimer = null
        } else {
            val duration: Int = mMediaPlayer!!.duration - mMediaPlayer!!.currentPosition
            // 카운트다운 시작
            if (mCountDownTimer != null) {
                mCountDownTimer!!.cancel()
                mCountDownTimer = null
            }
            mCountDownTimer = object : CountDownTimer(duration.toLong(), 1000) {
                override fun onTick(millisUntilFinished: Long) {
                    val mMediaPlayer: MediaPlayer = mService!!.getMediaPlayer() ?: return
                    val currentPosition: Int = mMediaPlayer.currentPosition
                    mSeekBar.progress = currentPosition
                    val min: Int = currentPosition / 1000 / 60
                    val sec: Int = currentPosition / 1000 % 60
                    mCurrentTimeTextView!!.text = String.format(Locale.KOREA, "%d:%02d", min, sec)
                }

                override fun onFinish() {
                    mCountDownTimer = null
                }
            }.start()
        }
    }

    override fun onClick(v: View) {
        when (v.id) {
            R.id.play_loop -> if (mService!!.getMediaPlayer()!!.isLooping) {
                mService!!.getMediaPlayer()!!.isLooping = false
                mPlayLoop!!.text = "반복재생"
            } else {
                mService!!.getMediaPlayer()!!.isLooping = true
                mPlayLoop!!.text = "연속재생"
            }
            R.id.stop_player -> stopPlayer()
            else -> {
            }
        }
    }

    private fun stopPlayer() {

        // TODO: 2020-07-07
        mService!!.getMediaPlayer()!!.stop()
        mService!!.getMediaPlayer()!!.release()
        mService!!.mMediaPlayer = null
        mService!!.removeNotificationPlayer() // notification 삭제
        // widget 종료
        mService!!.sendBroadcast(Intent(BroadcastActions.PLAY_STATE_CHANGED,  // action
                Uri.EMPTY,  // data
                context,  // context
                CaptureWidgetProvider::class.java)) //class
        // Service 종료
        val serviceIntent = Intent(context, MusicService::class.java)
        requireContext().stopService(serviceIntent)
        try {
            System.gc()
//            finalize() // ???? 정리?  finish() ?
        } catch (throwable: Throwable) {
            throwable.printStackTrace()
        }
        val intent = Intent(mService!!.applicationContext, MainActivity::class.java)
        startActivity(intent)
    }

    protected fun finalize(){
        //
    }
}