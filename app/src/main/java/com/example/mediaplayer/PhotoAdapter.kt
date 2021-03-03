package com.example.mediaplayer

import android.content.ContentUris
import android.content.Context
import android.database.Cursor
import android.net.Uri
import android.os.Build
import android.os.Parcel
import android.os.Parcelable
import android.provider.BaseColumns
import android.provider.MediaStore
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.RelativeLayout
import android.widget.TextView
import androidx.annotation.RequiresApi
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import com.example.mediaplayer.PhotoAdapter.PhotoViewHolder
import java.io.Serializable
import java.text.SimpleDateFormat
import java.util.*

class PhotoAdapter : RecyclerView.Adapter<PhotoViewHolder> {
    interface onItemClickListener {
        fun onItemClicked(item: PhotoItem, view: View?)
    }

    private var mListener: onItemClickListener? = null
    private var mContext: Context? = null
    private var itemLayout: Int = 0
    var mPhotoList: ArrayList<PhotoItem>? = null

    companion object {
        private const val TAG: String = "포토어댑터"
    }

    // 리스너 객체 전달함수
    fun setOnItemClickListener(listener: onItemClickListener) {
        mListener = listener
    }

    constructor(mListener: onItemClickListener?) {
        this.mListener = mListener
    }

    /**
     * 생성자
     * @param itemLayout        item layout
     */
    constructor(context: Context?, itemLayout: Int) {
        mContext = context
        if (mPhotoList == null) mPhotoList = getPhotoList()
        this.itemLayout = itemLayout
    }

    fun setmPhotoLists(photoList: ArrayList<PhotoItem>?) {
        mPhotoList = photoList
        notifyDataSetChanged()
    }

    fun getmPhotoList(): ArrayList<PhotoItem>? {
        return mPhotoList
    }

    fun getItem(position: Int): PhotoItem {
        return mPhotoList!![position]
    }

    override fun getItemCount(): Int {
        return mPhotoList!!.size
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): PhotoViewHolder {
        val view: View = LayoutInflater.from(parent.context)
                .inflate(itemLayout, parent, false)
//                .inflate(R.layout.photo_item, parent, false);
        val viewHolder = PhotoViewHolder(view)
        view.setOnClickListener(object : View.OnClickListener {
            override fun onClick(v: View) {
                if (mListener != null) {
                    val item: PhotoItem = mPhotoList!![viewHolder.adapterPosition]
                    mListener!!.onItemClicked(item, v)
                }
            }
        })
        return viewHolder
    }

    override fun onBindViewHolder(holder: PhotoViewHolder, position: Int) {
        Log.i(Companion.TAG, "BindViewHolder : $position")
//        final PhotoItem item = mItems.get(position);
//        holder.setPhotoItem(item);
        val item: PhotoItem = mPhotoList!![position]
        Glide.with((mContext)!!)
                .load(item.getImgPath())
                .centerCrop() //                .crossFade()
                .into(holder.mImageView)
        holder.mTxtTitle.text = item.mDATE_MODIFIED
//        holder.mTxtTitle.setText(item.mDate);


//        holder.itemView.setOnClickListener(new View.OnClickListener() {
//            @Override
//            public void onClick(View v) {
//                if(mListener != null){
////                    final PhotoItem item = mItems.get(holder.getAdapterPosition());
//                    mListener.onItemClicked(item, v);
//                }
//            }
//        });
    }

    class PhotoViewHolder constructor(itemView: View) : RecyclerView.ViewHolder(itemView) {
        var mImageView: ImageView = itemView.findViewById(R.id.imagephotoView)
        var layoutSelect: RelativeLayout? = null
        val mTxtTitle: TextView = itemView.findViewById(R.id.txt_title)

    }

    @RequiresApi(Build.VERSION_CODES.Q)
    @Suppress("DEPRECATION")
    private fun getPhotoList(): ArrayList<PhotoItem> {
        val photoList: ArrayList<PhotoItem> = ArrayList()
        var uri: Uri?
        val projection: Array<String>
        projection = arrayOf(
                MediaStore.MediaColumns.DATA,
                MediaStore.MediaColumns.DATE_ADDED,
                MediaStore.MediaColumns.DISPLAY_NAME)
        uri = MediaStore.Images.Media.EXTERNAL_CONTENT_URI
//      projection = new String[]{
//          MediaStore.Video.Media._ID,
//          MediaStore.Video.Media.DISPLAY_NAME,
//          MediaStore.Video.Media.DATA
//      };
        val cursor: Cursor? = mContext!!.contentResolver
                .query(MediaStore.Images.Media.EXTERNAL_CONTENT_URI,  // uri ?? 위와 비교
                        null, null, null, MediaStore.MediaColumns.DATE_MODIFIED + " desc")
        val columnIndex: Int = cursor!!.getColumnIndexOrThrow(MediaStore.MediaColumns.DATA)
//        int dateIndex = cursor.getColumnIndexOrThrow(MediaStore.MediaColumns.DATE_ADDED);
        while (cursor.moveToNext()) {
            val absolutePathOfImage: String = cursor.getString(columnIndex)
            val Sdate: Long = cursor.getLong(cursor.getColumnIndexOrThrow(MediaStore.Images.Media.DATE_TAKEN))
            var mDate: String?
//            if( Sdate != 0 ) {    // DATE_TAKEN 이 없는 경우 1970.0.0 ?
                mDate = SimpleDateFormat("yyyy.MM.dd", Locale.KOREA).format(Date(Sdate))
//            } else {
//                mDate = "Title";
//            }
            uri = ContentUris.withAppendedId(MediaStore.Images.Media.EXTERNAL_CONTENT_URI,
                    cursor.getLong(cursor.getColumnIndexOrThrow(BaseColumns._ID)))
            val photoItem = PhotoItem(absolutePathOfImage, mDate, false, uri)
            photoItem.mDISPLAY_NAME = cursor.getString(cursor.getColumnIndexOrThrow(MediaStore.Images.Media.DISPLAY_NAME))
            photoItem.mCONTENT_TYPE = cursor.getString(cursor.getColumnIndexOrThrow(MediaStore.Images.Media.MIME_TYPE))
            photoItem.mDATE_MODIFIED = SimpleDateFormat("yyyy.MM.dd", Locale.KOREA)
                    .format(Date(cursor.getLong(cursor.getColumnIndexOrThrow(MediaStore.Images.Media.DATE_MODIFIED)) * 1000))
            photoItem.mRELATIVE_PATH = cursor.getString(cursor.getColumnIndexOrThrow(MediaStore.Images.Media.RELATIVE_PATH))
            photoItem.mDATA = cursor.getString(cursor.getColumnIndexOrThrow(MediaStore.Images.Media.DATA))
            photoList.add(photoItem)
            Log.i(Companion.TAG, "Path : $uri$mDate*$Sdate");

//            if (!TextUtils.isEmpty(absolutePathOfImage))
//            {
//                BitmapFactory.Options bo = new BitmapFactory.Options();
//                bo.inSampleSize = 8;
//                Bitmap bmp = BitmapFactory.decodeFile(absolutePathOfImage, bo);
//                item.mBitmap = bmp;
//                result.add(item);
//                Log.i(TAG, "Path : " + absolutePathOfImage + "");
//            }
        }
        cursor.close()
        return photoList
    }

    class PhotoItem : Serializable, Parcelable {
        var imgPath: String? = null
        var mDate: String? = null
        private var selected: Boolean = false
        var mUri: Uri? = null

        var mDISPLAY_NAME: String? = null
        var mCONTENT_TYPE: String? = null
        var mDATE_MODIFIED: String? = null
        var mRELATIVE_PATH: String? = null
        var mDATA: String? = null

        companion object {
            @JvmField val CREATOR: Parcelable.Creator<PhotoItem?> = object : Parcelable.Creator<PhotoItem?> {
                override fun createFromParcel(`in`: Parcel): PhotoItem {
                    return PhotoItem(`in`)
                }

                override fun newArray(size: Int): Array<PhotoItem?> {
                    return arrayOfNulls(size)
                }
            }
        }


        constructor(imgPath: String?, date: String?, selected: Boolean, uri: Uri?) {
            this.imgPath = imgPath
            this.mDate = date
            this.selected = selected
            this.mUri = uri
        }

        // Parcelable 로 만들기 -----
        constructor(`in`: Parcel) {
            imgPath = `in`.readString()
            mDate = `in`.readString()
            selected = `in`.readByte().toInt() != 0
            mUri = `in`.readParcelable(Uri::class.java.classLoader)
            mDISPLAY_NAME = `in`.readString()
            mCONTENT_TYPE = `in`.readString()
            mDATE_MODIFIED = `in`.readString()
            mRELATIVE_PATH = `in`.readString()
            mDATA = `in`.readString()
        }

        // Parcelable 로 만들기 -----
        @JvmName("getImgPath1")
        fun getImgPath(): String? {
            return imgPath
        }

        fun getUri(): Uri? {
            return mUri
        }

        @JvmName("setImgPath1")
        fun setImgPath(imgPath: String?) {
            this.imgPath = imgPath as Nothing?
        }

        fun isSelected(): Boolean {
            return selected
        }

        fun setSelected(selected: Boolean) {
            this.selected = selected
        }

        // Parcelable 로 만들기 -----
        override fun describeContents(): Int {
            return 0
        }

        override fun writeToParcel(dest: Parcel, flags: Int) {
            dest.writeString(imgPath)
            dest.writeString(mDate)
            dest.writeByte((if (selected) 1 else 0).toByte())
            dest.writeParcelable(mUri, flags)
            dest.writeString(mDISPLAY_NAME)
            dest.writeString(mCONTENT_TYPE)
            dest.writeString(mDATE_MODIFIED)
            dest.writeString(mRELATIVE_PATH)
            dest.writeString(mDATA)
        } // Parcelable 로 만들기 -----

    }

}