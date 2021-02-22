package com.example.mediaplayer

import android.annotation.SuppressLint
import android.content.ContentUris
import android.content.Context
import android.content.res.Resources
import android.database.Cursor
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.net.Uri
import android.os.Build
import android.os.ParcelFileDescriptor
import android.provider.BaseColumns
import android.provider.MediaStore
import android.text.format.DateFormat
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.TextView
import androidx.annotation.RequiresApi
import androidx.recyclerview.widget.RecyclerView
import com.example.mediaplayer.adapters.CursorRecyclerViewAdapter
import com.squareup.picasso.Picasso
import java.io.FileDescriptor
import java.util.*

class AudioAdapter : CursorRecyclerViewAdapter<AudioAdapter.MyAudioViewHolder> {
    interface onItemClickListener {
        fun onItemClicked(model: Uri)
    }

    private var mListener: onItemClickListener? = null
    private val callback: FragmentCallback? = null
    private val TAG: String = "오디오어댑터"
    private var mContext: Context? = null
    var mSongList: ArrayList<Uri>? = null
    var mSongID: ArrayList<Long>? = null
    private val mUri: Uri? = null

    // 리스너 객체 전달함수
    fun setOnItemClickListener(listener: onItemClickListener) {
        mListener = listener
    }

    constructor(listener: onItemClickListener?) {
        mListener = listener
    }

    constructor(context: Context?, cursor: Cursor?) : super(context, cursor) {
        this.mContext = context
        if (mSongList == null) mSongList = getSongList()
    }

    fun setSongs(Songs: ArrayList<Uri>?) {       // ?? 사용처 확인
        mSongList = Songs
        notifyDataSetChanged()
    }

    // onCreateViewHolder() - 아이템 뷰를 위한 뷰홀더 객체 생성하여 리턴.
    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): MyAudioViewHolder {
        val view: View = LayoutInflater.from(parent.context)
                .inflate(R.layout.listitem_audio, parent, false)
        //        View view = ((LayoutInflater)parent.getContext().getSystemService(Context.LAYOUT_INFLATER_SERVICE)).inflate(R.layout.listitem_audio, parent, false) ;
        val viewHolder = MyAudioViewHolder(view)
//  onCreateViewHolder에서 setOnClickListener사용.
//          ViewHolder에서 사용할 수도 있고
//          onBindViewHolder 에서도 가능
        view.setOnClickListener(object : View.OnClickListener {
            override fun onClick(v: View) {
                if (mListener != null) {
                    val item: Uri = mSongList!![viewHolder.adapterPosition]
                    mListener!!.onItemClicked(item)
                    Log.e(TAG, "Playing Title:" + viewHolder.holderTitle)
                }
            }
        })
        return viewHolder
    }

    @RequiresApi(Build.VERSION_CODES.Q)
    override fun onBindViewHolder(viewHolder: MyAudioViewHolder, cursor: Cursor?) {
        val audioItem: AudioItem = AudioItem.bindCursor(mContext, cursor)
        viewHolder.setAudioItem(audioItem, cursor!!.position)
        Log.e(TAG, "onBinding Title:" + audioItem.mTitle + ", Artist:" + audioItem.mArtist)
    }

    //    @Override
    //    public void onBindViewHolder(@NonNull MyAudioViewHolder viewHolder, int position) {
    ////        AudioItem audioItem = AudioItem.bindCursor(mSongCursorList.get(position));  // bindCursor
    ////        ((MyAudioViewHolder) viewHolder).setAudioItem(audioItem, position);         // 활용
    ////        Log.e(TAG, "onBinding Title:" + audioItem.mTitle + ", Artist:" + audioItem.mArtist);
    //
    ////==============================================================
    //        mUri = mSongList.get(position);
    //        Log.i(TAG, "BindViewHolder : " + position + "");
    ////        MediaMetadataRetriever 를 이용하여 데이터 처리 ================
    //        final MediaMetadataRetriever retriever;
    //        try {
    //            retriever = new MediaMetadataRetriever();
    //            retriever.setDataSource(mContext, mUri);
    //            AudioItem audioItem = AudioItem.bindRetriever(retriever);
    //            ((MyAudioViewHolder) viewHolder).setAudioItem(audioItem, position);
    //            Log.e(TAG, "onBinding Title:" + audioItem.mTitle + ", Artist:" + audioItem.mArtist
    //                    + ", uri:" + mUri);
    //
    //        } catch (Exception e) {
    //            Log.d(TAG, "Retriever 오류! ");
    //            e.printStackTrace();
    //        }
    //    }

    override fun getItemCount(): Int {
        return mSongList!!.size
    }

    @SuppressLint("Recycle")
    private fun getSongList(): ArrayList<Uri> {
        val songList: ArrayList<Uri> = ArrayList()
        mSongID = ArrayList()
        val selection: String = MediaStore.Audio.Media.IS_MUSIC + " = 1"
        val sortOrder: String = MediaStore.Audio.Media.TITLE + " COLLATE LOCALIZED ASC"
        val cursor: Cursor? = mContext!!.contentResolver.query(MediaStore.Audio.Media.EXTERNAL_CONTENT_URI,
                null,
                selection,
                null,
                null)
//        sortOrder);
        if (cursor != null && cursor.count > 0) {
            while (cursor.moveToNext()) {
                val songID: Long = cursor.getLong(cursor.getColumnIndexOrThrow(BaseColumns._ID))
                mSongID!!.add(songID)
                val uri: Uri = ContentUris.withAppendedId(MediaStore.Audio.Media.EXTERNAL_CONTENT_URI,
                        cursor.getLong(
                                cursor.getColumnIndexOrThrow(BaseColumns._ID)))
                songList.add(uri)
//                Log.d(TAG, "getSongList Title:" + cursor.getString(cursor.getColumnIndex(MediaStore.Audio.Media.TITLE)));
            }
        }
        cursor!!.close()
        return songList
    }

    // 아이템 뷰를 저장하는 뷰홀더 클래스.
    class MyAudioViewHolder constructor(view: View) : RecyclerView.ViewHolder(view) {
        private val artworkUri: Uri = Uri.parse("content://media/external/audio/albumart")

//        private final Uri artworkUri = MediaStore.Audio.Albums.EXTERNAL_CONTENT_URI;
        private val mImgAlbumArt: ImageView
        private val mTxtTitle: TextView
        private val mTxtSubTitle: TextView
        private val mTxtDuration: TextView
        private var mPosition: Int = 0
        var holderTitle: String? = null

        @SuppressLint("SetTextI18n")
        fun setAudioItem(item: AudioItem, position: Int) {
//            mItem = item;
            mPosition = position
            holderTitle = item.mTitle // temp
            mTxtTitle.text = item.mTitle
            mTxtSubTitle.text = item.mArtist + "(" + item.mAlbum + ")"
            mTxtDuration.text = DateFormat.format("mm:ss", item.mDuration)
            val albumArtUri: Uri = ContentUris.withAppendedId(
                    Uri.parse("content://media/external/audio/albumart"), item.mAlbumId)
            Picasso.get().load(albumArtUri).error(R.drawable.snow).into(mImgAlbumArt)
//            Glide.with(itemView.getContext()).load(albumArtUri).error(R.drawable.snow).into(mImgAlbumArt);

//                Drawable image = Drawable.createFromPath(item.mAlbum);
//                mImgAlbumArt.setImageDrawable(image);

//            if(item.mBitmap != null) {
//                mImgAlbumArt.setImageBitmap(item.mBitmap);
//            } else {
//                mImgAlbumArt.setImageResource(R.drawable.snow);
//            }
        }

        init {
            // 뷰 객체에 대한 참조. (hold strong reference)
            mImgAlbumArt = view.findViewById<View>(R.id.img_albumart) as ImageView
            mTxtTitle = view.findViewById<View>(R.id.txt_title) as TextView
            mTxtSubTitle = view.findViewById<View>(R.id.txt_sub_title) as TextView
            mTxtDuration = view.findViewById<View>(R.id.txt_duration) as TextView
            //            view.setOnClickListener(new View.OnClickListener() {
//                @Override
//                public void onClick(View v) {
//                    // play(mPosition)  .....
//                }
//            });
        }
    }

    class AudioItem {
        var mId // 오디오 고유 ID
                : Long = 0
        var mAlbumId // 오디오 앨범아트 ID
                : Long = 0
        var mTitle // 타이틀 정보
                : String? = null
        var mArtist // 아티스트 정보
                : String? = null
        var mAlbum // 앨범 정보
                : String? = null
        var mDuration // 재생시간
                : Long = 0
        var mDataPath // 실제 데이터위치
                : String? = null
        var mBitmap: Bitmap? = null

        companion object {
            @RequiresApi(Build.VERSION_CODES.Q)
            fun bindCursor(context: Context?, cursor: Cursor?): AudioItem {
                val audioItem = AudioItem()
                audioItem.mId = cursor!!.getLong(cursor.getColumnIndex(MediaStore.Audio.Media._ID))
                audioItem.mAlbumId = cursor.getLong(cursor.getColumnIndex(MediaStore.Audio.Media.ALBUM_ID))
                audioItem.mTitle = cursor.getString(cursor.getColumnIndex(MediaStore.Audio.Media.TITLE))
                audioItem.mArtist = cursor.getString(cursor.getColumnIndex(MediaStore.Audio.Media.ARTIST))
                audioItem.mAlbum = cursor.getString(cursor.getColumnIndex(MediaStore.Audio.Media.ALBUM))
                audioItem.mDuration = cursor.getLong(cursor.getColumnIndex(MediaStore.Audio.Media.DURATION))
                audioItem.mDataPath = cursor.getString(cursor.getColumnIndex(MediaStore.Audio.Media.DATA))
                audioItem.mBitmap = getAlbumart(context, audioItem.mAlbumId)

////            audioItem.mDataPath = cursor.getString(cursor.getColumnIndex(MediaStore.Audio.AudioColumns.DATA));  // AudioColumns ==> Media 로 !!
//            Cursor cursorAlbum = context.getContentResolver().query(MediaStore.Audio.Albums.EXTERNAL_CONTENT_URI,
//                    new String[]{MediaStore.Audio.Albums._ID, MediaStore.Audio.Albums.ALBUM_ART},
//                    MediaStore.Audio.Albums._ID + "=?",
//                    new String[] {String.valueOf(audioItem.mAlbumId)}, null);
////            Cursor cursorAlbum = context.getContentResolver().query(MediaStore.Audio.Albums.EXTERNAL_CONTENT_URI,
////                    new String[]{MediaStore.Audio.Albums._ID, MediaStore.Audio.Albums.ALBUM_ART},
////                    MediaStore.Audio.Albums._ID + "=" + audioItem.mAlbumId, null, null);
//
//            if(cursorAlbum != null && cursorAlbum.moveToFirst()){
//                audioItem.mAlbum = cursorAlbum.getString(cursorAlbum.getColumnIndex(MediaStore.Audio.Albums.ALBUM_ART));
//                cursorAlbum.close();
//            }
                Log.d("AudioItem bindCursor", " Title:" + audioItem.mTitle + ", Artist:" + audioItem.mArtist)
                return audioItem
            }
        }
    }

    companion object {
        fun getAlbumart(context: Context?, album_id: Long?): Bitmap? {
            var albumArtBitMap: Bitmap? = null
            val options: BitmapFactory.Options = BitmapFactory.Options()
            try {
                val sArtworkUri: Uri = Uri
                        .parse("content://media/external/audio/albumart")
                val uri: Uri = ContentUris.withAppendedId(sArtworkUri, (album_id)!!)
                val pfd: ParcelFileDescriptor? = context!!.contentResolver
                        .openFileDescriptor(uri, "r")
                if (pfd != null) {
                    val fd: FileDescriptor? = pfd.fileDescriptor
                    albumArtBitMap = BitmapFactory.decodeFileDescriptor(fd, null,
                            options)
//                    pfd = null
//                    fd = null
                }
            } catch (ee: Error) {
            } catch (e: Exception) {
            }
            if (null != albumArtBitMap) {
                return albumArtBitMap
            }
            return getDefaultAlbumArtEfficiently(context!!.resources)
        }

        private fun getDefaultAlbumArtEfficiently(resource: Resources?): Bitmap? {
            var defaultBitmapArt: Bitmap? = null
//        if (defaultBitmapArt == null) {
            defaultBitmapArt = BitmapFactory.decodeResource(resource, R.drawable.snow)
//            defaultBitmapArt = decodeSampledBitmapFromResource(resource,
//                    R.drawable.snow, UtilFunctions
//                            .getUtilFunctions().dpToPixels(85, resource),
//                    UtilFunctions.getUtilFunctions().dpToPixels(85, resource));
//        }
            return defaultBitmapArt
        }
    }
}