package com.example.mediaplayer.frags

import android.media.MediaPlayer
import android.media.MediaPlayer.OnPreparedListener
import android.net.Uri
import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.MediaController
import android.widget.VideoView
import androidx.fragment.app.Fragment
import com.example.mediaplayer.R
import org.greenrobot.eventbus.EventBus
import org.greenrobot.eventbus.Subscribe

class VideoPlayerFragment : Fragment() {
    private lateinit var videoView: VideoView

    override fun onCreateView(
            inflater: LayoutInflater, container: ViewGroup?,
            savedInstanceState: Bundle?): View? {

        // EventBus에 구독자로 현재 액티비티 추가
        EventBus.getDefault().register(this)
        Log.d(TAG, "player start")
        // Inflate the layout for this fragment
        return inflater.inflate(R.layout.fragment_videoplayer, container, false)
    }

    override fun onDestroyView() {
        super.onDestroyView()
        // EventBus에 구독자에서 제거
        EventBus.getDefault().unregister(this)
        Log.d(TAG, "player stop")
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        videoView = view.findViewById(R.id.videoview)
        val mc = MediaController(context)
        videoView.setMediaController(mc)
    }

    @Subscribe
    fun playVideo(uri: Uri) {
//        String path = "https://sites.google.com/site/ubiaccessmobile/sample_video.mp4";
//        videoView.setVideoPath(path);
        videoView!!.setVideoURI(uri)
        videoView!!.requestFocus()
        videoView!!.setOnPreparedListener(object : OnPreparedListener {
            override fun onPrepared(mp: MediaPlayer) {
                videoView!!.seekTo(0)
                videoView!!.start()
                Log.d(TAG, "재생시작 Uri : $uri")
            }
        })
    }

    companion object {
        private const val TAG: String = "비디오 플레이어"
    }
}