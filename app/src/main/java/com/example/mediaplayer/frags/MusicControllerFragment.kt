package com.example.mediaplayer.frags

import android.content.ComponentName
import android.content.ServiceConnection
import android.graphics.Bitmap
import android.os.Bundle
import android.os.IBinder
import android.view.*
import android.widget.*
import androidx.fragment.app.Fragment
import com.bumptech.glide.Glide
import com.example.mediaplayer.MusicApplication
import com.example.mediaplayer.R
import com.example.mediaplayer.services.MusicService
import com.example.mediaplayer.services.MusicService.LocalBinder
import org.greenrobot.eventbus.EventBus
import org.greenrobot.eventbus.Subscribe

class MusicControllerFragment : Fragment(), View.OnClickListener {
    private var mAlbumImageView: ImageView? = null
    private var mTitleTextView: TextView? = null
    private var mArtistTextView: TextView? = null
    private var mPlayButton: Button? = null
    private var mService: MusicService? = null
    private var mBound: Boolean = false

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        return inflater.inflate(R.layout.fragment_music_controller, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        mAlbumImageView = view.findViewById<View>(R.id.album_image) as ImageView?
        mTitleTextView = view.findViewById<View>(R.id.title_text) as TextView?
        mArtistTextView = view.findViewById<View>(R.id.artist_text) as TextView?

        mPlayButton = view.findViewById<View>(R.id.play_button) as Button?
        mPlayButton!!.setOnClickListener(this)

        view.findViewById<View>(R.id.prev_music_button).setOnClickListener(this)
        view.findViewById<View>(R.id.next_music_button).setOnClickListener(this)
    }

    private fun updateMetaData() {
        var title: String? = mService!!.mAudioItem!!.mTitle
        var artist: String? = mService!!.mAudioItem!!.mArtist
        val bitmap: Bitmap? = mService!!.mAudioItem!!.mBitmap
        if (title == null) title = "UnKnown"
        if (artist == null) artist = "UnKnown"

// 오디오 앨범 자켓 이미지,  AudioItem의 정보 활용?
//            byte albumImage[] = retriever.getEmbeddedPicture();
        if (bitmap != null) {
            Glide.with(this).load(bitmap).into((mAlbumImageView)!!)
        } else {
//                Glide.with(this).load(R.drawable.musiccircle).into(mAlbumImageView);
            mAlbumImageView!!.setImageResource(R.drawable.music_circle)
        }
        // **** Glide 사용법
        mTitleTextView!!.text = title
        mArtistTextView!!.text = artist
    }

    // Service 이용하는 방법 ===============================
    @Subscribe
    fun updateUI(isPlaying: Boolean) {       // boolean 안됨
        if (mService == null) return  // *** player가 service에 연결되기 전
        mPlayButton!!.text = if (isPlaying) "중지" else "재생"
        updateMetaData()

//        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
//            mService.startForegroundService();
//        }
    }

    //    // EventBus 만을 이용하는 방법 ===============================
    //    @Subscribe
    //    public void updateUI(final MediaMetadataRetriever retriever) {
    //        updateMetaData(retriever);
    //    }
    //
    //    @Subscribe
    //    public void updatePlayButton(Boolean isPlaying){
    //        mPlayButton.setText(isPlaying ? "중지" : "재생");
    //    }
    //    // ===============================

    override fun onStart() {
        super.onStart()

        // EventBus 설정
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

        // EventBus 해제
        EventBus.getDefault().unregister(this)

        // Service 이용 해제
        if (mBound) {
//            // MusicApplication 활용
//            getActivity().unbindService(mConnection);
            mBound = false
        }
    }

    override fun onClick(v: View) {
        when (v.id) {
            R.id.play_button -> MusicApplication.getInstance()!!.getServiceInterface()!!.clickResumeButton()
            R.id.prev_music_button -> MusicApplication.getInstance()!!.getServiceInterface()!!.prevMusic()
            R.id.next_music_button -> MusicApplication.getInstance()!!.getServiceInterface()!!.nextMusic()
        }
    }

    // Service 사용
    private val mConnection: ServiceConnection = object : ServiceConnection {
        override fun onServiceConnected(name: ComponentName, service: IBinder) {
            // We've bound to LocalService, cast the IBinder and get LocalService instance
            val binder: LocalBinder = service as LocalBinder
            mService = binder.getService()
            mBound = true

            // Service 사용
            // UI 갱신
            updateUI(mService!!.isPlaying())
        }

        override fun onServiceDisconnected(name: ComponentName) {
            mBound = false
        }
    }
}