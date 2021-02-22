package com.example.mediaplayer.frags

import android.content.Context
import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.fragment.app.Fragment
import androidx.recyclerview.widget.GridLayoutManager
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.example.mediaplayer.FragmentCallback
import com.example.mediaplayer.PhotoAdapter
import com.example.mediaplayer.R
import org.greenrobot.eventbus.EventBus

class PhotoFragment : Fragment() {
    var callback: FragmentCallback? = null
    private var mContext: Context? = null
    override fun onAttach(context: Context) {
        super.onAttach(context)
        mContext = context
        if (context is FragmentCallback) {
            callback = context
        }
    }

    override fun onDetach() {
        super.onDetach()
        if (callback != null) callback = null
    }

    override fun onCreateView(
            inflater: LayoutInflater, container: ViewGroup?,
            savedInstanceState: Bundle?): View? {
        // Inflate the layout for this fragment
        return inflater.inflate(R.layout.fragment_photo, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        val recyclerView: RecyclerView = view.findViewById(R.id.recycler_photo)
        val mPhotoAdapter = PhotoAdapter(mContext, R.layout.photo_item)

//       item click
        mPhotoAdapter.setOnItemClickListener(object : PhotoAdapter.onItemClickListener {
            public override fun onItemClicked(item: PhotoAdapter.PhotoItem, view: View?) {
                Toast.makeText(context, "아이템 선택됨: " + item.imgPath, Toast.LENGTH_SHORT).show()

//              PhotoViewFragment 호출
                //  EventBus로 PhotoViewFragment#playPhoto 사용
                /**
                 * 사진 보기
                 * [com.example.mediaplayer.frags.PhotoViewFragment.playPhoto]                   */
                EventBus.getDefault().post(item)
                if (callback != null) callback!!.setPage(1)
            }
        })

        recyclerView.adapter = mPhotoAdapter
        Log.i(TAG, "사진 수 : " + mPhotoAdapter.itemCount)


        // 추가
        val layoutManager = GridLayoutManager(activity, 3)
        layoutManager.orientation = LinearLayoutManager.VERTICAL
        recyclerView.layoutManager = layoutManager
    }

    companion object {
        private const val TAG = "사진"
    }
}