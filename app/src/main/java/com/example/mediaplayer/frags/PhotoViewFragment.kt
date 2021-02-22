package com.example.mediaplayer.frags

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import com.example.mediaplayer.PhotoAdapter.PhotoItem
import com.example.mediaplayer.R
import com.github.chrisbanes.photoview.PhotoView
import org.greenrobot.eventbus.EventBus
import org.greenrobot.eventbus.Subscribe

class PhotoViewFragment constructor() : Fragment() {
    private var photoView: PhotoView? = null
    public override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {

        // EventBus에 구독자로 현재 액티비티 추가
        EventBus.getDefault().register(this)
        return inflater.inflate(R.layout.fragment_photoplayer, container, false)
    }

    public override fun onDestroyView() {
        super.onDestroyView()
        // EventBus에 구독자에서 제거
        EventBus.getDefault().unregister(this)
    }

    public override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        photoView = view.findViewById(R.id.photoview)
    }

    @Subscribe
    fun playPhoto(item: PhotoItem) {
        photoView!!.setImageURI(item.mUri)
    }

    companion object {
        private val TAG: String = "포토 플레이어"
    }
}