package com.example.mediaplayer

import android.annotation.SuppressLint
import android.content.*
import android.database.Cursor
import android.net.Uri
import android.os.Build
import android.os.Bundle
import android.provider.MediaStore
import android.util.Log
import android.view.LayoutInflater
import android.view.MotionEvent
import android.view.View
import android.view.View.OnGenericMotionListener
import android.view.View.OnLongClickListener
import android.widget.EditText
import android.widget.ImageView
import android.widget.LinearLayout
import androidx.annotation.RequiresApi
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import com.bumptech.glide.Glide
import com.example.mediaplayer.PhotoAdapter.PhotoItem
import com.github.chrisbanes.photoview.PhotoView
import java.io.File
import java.text.ParseException
import java.text.SimpleDateFormat
import java.util.*

class PhotoViewActivity : AppCompatActivity(), View.OnClickListener {
    private var mContext: Context? = null
    private var mItem: PhotoItem? = null
    private var mPath: String? = null
    private val mImageView: ImageView? = null

    override fun onEnterAnimationComplete() {
        super.onEnterAnimationComplete()
        setInit()
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_photoview)
        mContext = this
        getData()
    }

    private fun getData() {
        val intent: Intent = intent
        mPath = intent.getStringExtra("path")
        mItem = intent.extras!!.get("photoItem") as PhotoItem?
    }

    private fun setInit() {
        val mImageView: PhotoView = findViewById<View>(R.id.photoview) as PhotoView
        Glide.with((mContext)!!).load(mPath).into(mImageView)
        mImageView.setOnLongClickListener(object : OnLongClickListener {
            override fun onLongClick(v: View): Boolean {
                Log.d("사진사건", "onLongClick")
                //                PhotoGalleryActivity.dataModifyDialog()  ==> eventBus로도?
                editDialog(mItem)
                return false
            }
        })
        mImageView.setOnGenericMotionListener(object : OnGenericMotionListener {
            override fun onGenericMotion(v: View, event: MotionEvent): Boolean {
                Log.d("사진사건", "onGenericMotion")
                return false
            }
        })
    }

    @Suppress("NULLABILITY_MISMATCH_BASED_ON_JAVA_ANNOTATIONS")
    private fun editDialog(item: PhotoItem?) {
        val vi: LayoutInflater = getSystemService(LAYOUT_INFLATER_SERVICE) as LayoutInflater
        //        LinearLayout editLayout = (LinearLayout) vi.inflate(R.layout.edit_dialog, null);
        val editLayout: LinearLayout = vi.inflate(R.layout.edit_dialog, null) as LinearLayout
        val displayname: EditText = editLayout.findViewById(R.id.edit_displayname)
        val contenttype: EditText = editLayout.findViewById(R.id.edit_contenttype)
        val modified: EditText = editLayout.findViewById(R.id.edit_modified)
        val relative: EditText = editLayout.findViewById(R.id.edit_relative)
        val pathdata: EditText = editLayout.findViewById(R.id.edit_pathdata)
        val datetaken: EditText = editLayout.findViewById(R.id.edit_datetaken)
        displayname.setText(item!!.mDISPLAY_NAME)
        contenttype.setText(item.mCONTENT_TYPE)
        modified.setText(item.mDATE_MODIFIED)
        relative.setText(item.mRELATIVE_PATH)
        pathdata.setText(item.mDATA)
        datetaken.setText(item.mDate)
        val newItem: PhotoItem? = item
        AlertDialog.Builder(this)
                .setTitle("사진정보")
                .setView(editLayout)
                .setNeutralButton("확인", object : DialogInterface.OnClickListener {
                    @RequiresApi(api = Build.VERSION_CODES.Q) // ***** contentresolver 수정 안되는 부분
                    override fun onClick(dialog: DialogInterface, which: Int) {
                        newItem!!.mDISPLAY_NAME = displayname.text.toString()
                        newItem.mCONTENT_TYPE = contenttype.text.toString()
                        newItem.mDATE_MODIFIED = modified.text.toString()
                        newItem.mRELATIVE_PATH = relative.text.toString()
                        newItem.mDATA = pathdata.text.toString()
                        newItem.mDate = datetaken.text.toString()
                        ////  사진자료 수정, newItem 자료를
                        val values = ContentValues()
                        values.put(MediaStore.Images.Media.IS_PENDING, 1)
                        values.put(MediaStore.Images.Media.DISPLAY_NAME, newItem.mDISPLAY_NAME)
                        values.put(MediaStore.Images.Media.MIME_TYPE, newItem.mCONTENT_TYPE)
                        values.put(MediaStore.Images.Media.RELATIVE_PATH, newItem.mRELATIVE_PATH)
                        try {
                            val dateModify: Date = SimpleDateFormat("yyyy.MM.dd", Locale.KOREA).parse(newItem.mDATE_MODIFIED)
                            values.put(MediaStore.Images.Media.DATE_MODIFIED, dateModify.time / 1000)
                        } catch (e: ParseException) {
                            values.put(MediaStore.Images.Media.DATE_MODIFIED, System.currentTimeMillis())
                            e.printStackTrace()
                        }
                        val resolver: ContentResolver = mContext!!.contentResolver
                        // RELATIVE_PATH/DISPLAY_NAME --> real path(DATA)
                        val uri: Uri = getUriFromPath(item.mDATA) // ** DISPLAY_NAME 에 따라 자동 바뀜 **
//                        val uri: Uri = getUriFromPath(newItem.mDATA) // ** DISPLAY_NAME 에 따라 자동 바뀜 **
                        // real path 계산 routine 필요
//-----------------------------------------------------------------------------------------------
//                        resolver.update(uri, values, null, null) // 수정 안됨 !!!!!!!
//-----------------------------------------------------------------------------------------------

                        try {
                            val dateTaken: Date = SimpleDateFormat("yyyy.MM.dd", Locale.KOREA).parse(newItem.mDate)
                            values.put(MediaStore.Images.Media.DATE_TAKEN, dateTaken.time)
                        } catch (e: ParseException) {
                            values.put(MediaStore.Images.Media.DATE_TAKEN, System.currentTimeMillis())
                            //                            e.printStackTrace();
                        }
                        values.put(MediaStore.Images.Media.IS_PENDING, 0)
                        Log.e("사진사건 수정", (uri.toString() + "\n" + newItem.mDATA
                                + "\n" + values.getAsString(MediaStore.Images.Media.DATE_TAKEN)))

//-----------------------------------------------------------------------------------------------
//                        resolver.update(uri, values, null, null) // 수정 안됨 !!!!!!!  --> 아래의 화일 수정?
//-----------------------------------------------------------------------------------------------

//                        ParcelFileDescriptor pfd = resolver.openFileDescriptor(uri,"w", null);
//                        FileOutputStream outStream = new FileOutputStream(pfd.getFileDescriptor());
//                        outStream.write(data);
//                        outStream.close();
//                        pfd.close();


////   등을 이용하여 MediaStore 자료 수정
                    }
                }).show()
    }

    override fun onClick(v: View) {}
    @SuppressLint("Recycle")
    fun getUriFromPath(filePath: String?): Uri {
        val cursor: Cursor? = contentResolver.query(MediaStore.Images.Media.EXTERNAL_CONTENT_URI,
                null, "_data = '$filePath'", null, null)
        cursor!!.moveToNext()
        val id: Int = cursor.getInt(cursor.getColumnIndex("_id"))
        return ContentUris.withAppendedId(MediaStore.Images.Media.EXTERNAL_CONTENT_URI, id.toLong())
    }

    @Suppress("DEPRECATION")
    fun getRealPathFromURI(contentUri: Uri?): String {
        val proj: Array<String> = arrayOf(MediaStore.Images.Media.DATA)
        val cursor: Cursor? = contentResolver.query((contentUri)!!, proj, null, null, null)
        cursor!!.moveToNext()
        val path: String = cursor.getString(cursor.getColumnIndex(MediaStore.MediaColumns.DATA))
        val uri: Uri = Uri.fromFile(File(path))
        cursor.close()
        return path
    }
}