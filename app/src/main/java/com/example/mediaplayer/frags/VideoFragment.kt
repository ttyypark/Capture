package com.example.mediaplayer.frags

import android.content.ContentUris
import android.content.Context
import android.database.Cursor
import android.graphics.Bitmap
import android.media.MediaMetadataRetriever
import android.media.ThumbnailUtils
import android.net.Uri
import android.os.Build
import android.os.Bundle
import android.provider.BaseColumns
import android.provider.MediaStore
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.TextView
import androidx.fragment.app.Fragment
import androidx.recyclerview.widget.RecyclerView
import androidx.recyclerview.widget.StaggeredGridLayoutManager
import com.example.mediaplayer.FragmentCallback
import com.example.mediaplayer.R
import com.example.mediaplayer.adapters.CursorRecyclerViewAdapter
import org.greenrobot.eventbus.EventBus
import java.io.File
import java.text.SimpleDateFormat
import java.util.*

class VideoFragment : Fragment() {
    var callback: FragmentCallback? = null
    var videoRecyclerAdapter: VideoRecyclerAdapter? = null
    override fun onAttach(context: Context) {
        super.onAttach(context)
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
        return inflater.inflate(R.layout.fragment_video, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        val recyclerView: RecyclerView = view.findViewById(R.id.recycler_view)

//========================================================
//   cursor 로 데이터를 가져와서 처리
        val cursor = requireActivity().contentResolver
                .query(MediaStore.Video.Media.EXTERNAL_CONTENT_URI,
                        null, null, null,
                        MediaStore.MediaColumns.DATE_MODIFIED + " desc")
        //        null, null, null, null);
        videoRecyclerAdapter = VideoRecyclerAdapter(activity, cursor)
        recyclerView.adapter = videoRecyclerAdapter
        Log.i(TAG, "비디오 수 : " + videoRecyclerAdapter!!.itemCount)

        // 추가
        val layoutManager = StaggeredGridLayoutManager(
                3, StaggeredGridLayoutManager.VERTICAL)
        recyclerView.layoutManager = layoutManager

//        cursor.close();
    }

    inner class VideoRecyclerAdapter     //        private ArrayList<Uri> mVideoList;
    (private val mContext: Context?, cursor: Cursor?) : CursorRecyclerViewAdapter<VideoViewHolder>(mContext, cursor) {
        override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): VideoViewHolder {
            val view = LayoutInflater.from(parent.context)
                    .inflate(R.layout.video_item, parent, false)
            return VideoViewHolder(view)
        }

        override fun onBindViewHolder(viewHolder: VideoViewHolder, cursor: Cursor?) {
//          http://commondatastorage.googleapis.com/gtv-videos-bucket/sample/BigBuckBunny.mp4
//            String url = "https://sites.google.com/site/ubiaccessmobile/sample_video.mp4";
//            final Uri uri = Uri.parse(url);
            val uri = ContentUris.withAppendedId(MediaStore.Video.Media.EXTERNAL_CONTENT_URI,  // 84);
                    cursor!!.getLong(cursor.getColumnIndexOrThrow(BaseColumns._ID)))
            val title = cursor.getString(cursor.getColumnIndex(MediaStore.Video.VideoColumns.DISPLAY_NAME))

//          입력받은 cursor를 이용하여 데이터 처리 =======================
            val videoItem = VideoItem.bindCursor(cursor, uri, mContext) ?: return
            viewHolder.setVideoItem(videoItem, cursor.position)
            //            Log.d(TAG, "Bind Title:" + videoItem.mTitle + ", 촬영날자:" + videoItem.mDate);

//            //        MediaMetadataRetriever 를 이용하여 데이터 처리 ================
//            final MediaMetadataRetriever retriever;
//            try {
//                retriever = new MediaMetadataRetriever();
//                retriever.setDataSource(mContext, uri);
//                VideoItem videoItem = VideoItem.bindRetriever(retriever, uri);
//                viewHolder.setVideoItem(videoItem, cursor.getPosition());
//                Log.d(TAG, "Title:" + videoItem.mTitle + ", Artist:" + videoItem.mArtist);
//                retriever.release();
//
//            } catch (Exception e) {
//                Log.d("VideoFrag", "Retriever 오류! ");
//                e.printStackTrace();
//            }
            viewHolder.itemView.setOnClickListener(View.OnClickListener {
                Log.d(TAG, "item 클릭 Uri : $uri")
                Log.d(TAG, "title : $title")

                //  EventBus로 VideoPlayerFragment#playVideo 사용
                /**
                 * 비디오 보기
                 * [com.example.mediaplayer.frags.VideoPlayerFragment.playVideo]
                 */
                /**
                 * 비디오 보기
                 * [com.example.mediaplayer.frags.VideoPlayerFragment.playVideo]
                 */
                EventBus.getDefault().post(uri)
                if (callback != null) callback!!.setPage(1)
            })
        }
    }

    class VideoViewHolder(view: View) : RecyclerView.ViewHolder(view) {
        //        TextView titleTextView;
        //        TextView artistTextView;
        //        private final Uri artworkUri = Uri.parse("content://media/external/audio/albumart");
        private val mImgAlbumArt: ImageView
        private val mTxtTitle: TextView
        private val mSubTitle: TextView? = null
        fun setVideoItem(item: VideoItem, position: Int) {
//            mItem = item;
            mTxtTitle.text = item.mDate
            //            mSubTitle.setText(DateFormat.format("mm:ss", item.mDuration));

//            mTxtDuration.setText(DateFormat.format("mm:ss", item.mDuration));
//            Uri albumArtUri = ContentUris.withAppendedId(artworkUri, item.mAlbumId);
//            Glide.with(itemView.getContext()).load(albumArtUri).error(R.drawable.snow).into(mImgAlbumArt);
//            Picasso.get().load(albumArtUri).error(R.drawable.empty_albumart).into(mImgAlbumArt);
            if (item.mBitmap != null) {
                mImgAlbumArt.setImageBitmap(item.mBitmap)
            } else {
                mImgAlbumArt.setImageResource(R.drawable.snow)
            }
        }

        init {
            mImgAlbumArt = view.findViewById(R.id.imageView)
            mTxtTitle = view.findViewById(R.id.txt_title)
            //            mSubTitle = view.findViewById(R.id.txt_sub_title);
            view.setOnClickListener(object : View.OnClickListener {
                override fun onClick(v: View) {
                    // ..
                }
            })
        }
    }

    class VideoItem {
        var mContext: Context? = null
        var mId // 오디오 고유 ID
                : Long = 0

        //        public long mAlbumId; // 오디오 앨범아트 ID
        lateinit var mTitle // 타이틀 정보
                : String
        var mDate //
                : String? = null
        var mAlbum // 앨범 정보
                : String? = null
        var mDuration // 재생시간
                : Long = 0
        var mDataPath // 실제 데이터위치
                : String? = null
        var mBitmap: Bitmap? = null

        companion object {
            fun bindCursor(cursor: Cursor?, uri: Uri?, mContext: Context?): VideoItem? {
                // 개별 item, cursor
                val videoItem = VideoItem()
                //  Audio Image -----
                val column_index = cursor!!.getColumnIndexOrThrow(MediaStore.Video.Media.DATA)
                val pathId = cursor.getString(column_index) ?: return null
                videoItem.mBitmap = ThumbnailUtils.createVideoThumbnail(pathId, MediaStore.Video.Thumbnails.MINI_KIND)
                //-------------------
                videoItem.mId = cursor.getLong(cursor.getColumnIndex(MediaStore.Video.VideoColumns._ID))
                videoItem.mTitle = cursor.getString(cursor.getColumnIndex(MediaStore.Video.VideoColumns.TITLE))
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
                    val Sdate = cursor.getString(cursor.getColumnIndex(MediaStore.Video.VideoColumns.DATE_TAKEN))
                    if (Sdate != null) {
                        val lDate = Sdate.toLong()
                        videoItem.mDate = SimpleDateFormat("yyyyMMdd", Locale.KOREA).format(Date(lDate))
                    } else {
                        // path 나누기 ----------------------
                        val pathArr = videoItem.mTitle.split("_").toTypedArray()
                        if ((pathArr[0] == "band")) {
                            videoItem.mDate = pathArr[0] + pathArr[2] + pathArr[3] + pathArr[4]
                        } else if ((pathArr[0] == "kakaotalk")) {
                            videoItem.mDate = pathArr[0] + pathArr[1]
                        } else {
                            videoItem.mDate = videoItem.mTitle
                        }
                    }
                }
                videoItem.mAlbum = cursor.getString(cursor.getColumnIndex(MediaStore.Video.VideoColumns.ALBUM))
                videoItem.mDuration = cursor.getLong(cursor.getColumnIndex(MediaStore.Video.VideoColumns.SIZE))
                val path = cursor.getString(cursor.getColumnIndex(MediaStore.MediaColumns.DATA))
                //            String path = cursor.getString(cursor.getColumnIndex(MediaStore.Video.VideoColumns.DATA));
//  MediaStore.Video.VideoColumns.DATA 으로 path 자료를 가져올수 없음. Title 이 들어있음.
//            ContentResolver.openFileDescriptor(Uri, String) 를 사용하라고 함
// path 나누기 ----------------------
                val pathArr = path.split("/").toTypedArray()
                videoItem.mDataPath = pathArr[pathArr.size - 1]
                Log.d(TAG, ("Title:" + videoItem.mTitle + ", Date:" + videoItem.mDate
                        + ", Path:" + videoItem.mDataPath))
                return videoItem
            }

            fun bindRetriever(retriever: MediaMetadataRetriever, uri: Uri?): VideoItem {
                // 개별 item, retriever
                val videoItem = VideoItem()
                videoItem.mTitle = retriever.extractMetadata(MediaMetadataRetriever.METADATA_KEY_TITLE)
                videoItem.mDate = retriever.extractMetadata(MediaMetadataRetriever.METADATA_KEY_DATE)
                videoItem.mAlbum = retriever.extractMetadata(MediaMetadataRetriever.METADATA_KEY_ALBUM)
                videoItem.mDuration = retriever.extractMetadata(MediaMetadataRetriever.METADATA_KEY_DURATION).toLong()
                videoItem.mBitmap = retriever.getFrameAtTime(0, MediaMetadataRetriever.OPTION_CLOSEST)
                Log.d(TAG, "Title:" + videoItem.mTitle + ", Album:" + videoItem.mAlbum)
                return videoItem
            }
        }
    }

    fun getRealPathFromURI(contentUri: Uri?): String {
        val proj = arrayOf(MediaStore.Video.Media.DATA)
        val cursor = requireActivity().contentResolver.query((contentUri)!!, proj, null, null, null)
        cursor!!.moveToNext()
        val path = cursor.getString(cursor.getColumnIndex(MediaStore.MediaColumns.DATA))
        val uri = Uri.fromFile(File(path))
        cursor.close()
        return path
    }

    companion object {
        private val TAG = "비디오"
    }
}