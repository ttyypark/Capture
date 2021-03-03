package com.example.mediaplayer

import android.content.Intent
import android.os.Build
import android.os.Bundle
import android.util.Log
import android.view.*
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.core.app.ActivityOptionsCompat
import androidx.fragment.app.DialogFragment
import androidx.recyclerview.widget.GridLayoutManager
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.example.mediaplayer.PhotoAdapter.PhotoItem
import com.example.mediaplayer.frags.EditDialogFragment
import com.example.mediaplayer.frags.EditDialogFragment.OnCompleteListener

class PhotoGalleryActivity : AppCompatActivity(), OnCompleteListener {
    private var selectView: View? = null

    companion object {
        private const val TAG: String = "포토 갤러리"
    }

    // callBack 함수
    override fun onInputedData(item: PhotoItem?) {
        Toast.makeText(this, item!!.mDISPLAY_NAME, Toast.LENGTH_LONG).show()
        transition(selectView, item)
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_photogallery)
        Log.e(TAG, "onCreate")
        val recyclerView: RecyclerView = findViewById(R.id.recycler_photo)
        val mPhotoAdapter = PhotoAdapter(applicationContext, R.layout.photo_item)

        //       item click interface
        mPhotoAdapter.setOnItemClickListener(object : PhotoAdapter.onItemClickListener {
            override fun onItemClicked(item: PhotoItem, view: View?) {
//                Toast.makeText(getApplicationContext(), "아이템 선택됨: " + item.imgPath, Toast.LENGTH_SHORT).show();
//                if(item.mDate == "Title") {
//                    // input dialog
//                    // https://webnautes.tistory.com/m/1094

//                    dataModifyDialog(item);       // dialogFragment 사용......
                selectView = view
                //                } else {
                transition(view, item) //  transition - PhotoViewActivity 에서
                //  AlertDialog.Builder(this) 사용......
//                }
            }
        })
        recyclerView.adapter = mPhotoAdapter
        Log.i(TAG, "사진 수 : " + mPhotoAdapter.itemCount)
        val layoutManager = GridLayoutManager(applicationContext, 3)
        layoutManager.orientation = LinearLayoutManager.VERTICAL
        recyclerView.layoutManager = layoutManager
    }

    fun dataModifyDialog(item: PhotoItem?) {
        // item data 실어 보내기
        val args = Bundle()
        args.putSerializable("photoItem", item)
        val newFragment: DialogFragment = EditDialogFragment()
        newFragment.arguments = args
        newFragment.show(supportFragmentManager, "dialog")
    }

    private fun transition(view: View?, item: PhotoItem?) {
        if (Build.VERSION.SDK_INT < 21) {
            Toast.makeText(this@PhotoGalleryActivity, "21+ only, keep out", Toast.LENGTH_SHORT).show()
        } else {
            val intent = Intent(this@PhotoGalleryActivity, PhotoViewActivity::class.java)

            intent.putExtra("path", item!!.imgPath)
            val extra = Bundle()
            extra.putSerializable("photoItem", item)
            intent.putExtras(extra)
            val imgView: View = view!!.findViewById(R.id.imagephotoView)
//            Pair<View, String> pair_thumb = Pair.create(thumbView, thumbView.getTransitionName());
//            여러개 이미지를 사용할 경우
            val options: ActivityOptionsCompat = ActivityOptionsCompat.makeSceneTransitionAnimation(this@PhotoGalleryActivity, imgView, "transition")
            startActivity(intent, options.toBundle())
        }
    }

}