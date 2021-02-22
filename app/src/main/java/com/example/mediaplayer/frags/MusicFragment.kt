package com.example.mediaplayer.frags

import android.content.*
import android.database.Cursor
import android.net.Uri
import android.os.Bundle
import android.os.IBinder
import android.provider.MediaStore
import android.util.Log
import android.view.*
import androidx.fragment.app.Fragment
import androidx.loader.app.LoaderManager
import androidx.loader.content.CursorLoader
import androidx.loader.content.Loader
import androidx.recyclerview.widget.GridLayoutManager
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.example.mediaplayer.AudioAdapter
import com.example.mediaplayer.AudioAdapter.onItemClickListener
import com.example.mediaplayer.FragmentCallback
import com.example.mediaplayer.MusicApplication
import com.example.mediaplayer.R
import com.example.mediaplayer.services.MusicService
import com.example.mediaplayer.services.MusicService.LocalBinder
import org.greenrobot.eventbus.EventBus

//import androidx.recyclerview.widget.RecyclerView.ViewHolder;
//import com.squareup.picasso.Picasso;  // Glide 와 호환

class MusicFragment : Fragment {
    var callback: FragmentCallback? = null
    private var mBound = false
    private var mContext: Context? = null
    private var mAdapter: AudioAdapter? = null

    //    public static SongRecyclerAdapter mAdapter;   // **
    private var mService: MusicService? = null

    constructor()
    constructor(contentLayoutId: Int) : super(contentLayoutId)

    //  callBack 함수로 MusicPlayerActivity의 setPage(int pageNum) 호출
    override fun onAttach(context: Context) {
        super.onAttach(context)
        mContext = context
        if (context is FragmentCallback) {
            callback = context
        }
    }

    override fun onDetach() {
        super.onDetach() // songFrag 없어짐?
        if (callback != null) callback = null
    }

    override fun onStart() {
        super.onStart()
        val intent = Intent(activity, MusicService::class.java)
        requireActivity().bindService(intent, mConnection, Context.BIND_AUTO_CREATE)
        Log.e(TAG, "서비스 연결")
    }

    override fun onStop() {
        super.onStop()
        if (mBound) {
            requireActivity().unbindService(mConnection)
            mBound = false
            Log.e(TAG, "서비스 연결 해제")
        }
    }

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        return inflater.inflate(R.layout.fragment_song, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        Log.d("노래목록", "시작됨")
        val recyclerView: RecyclerView = view.findViewById(R.id.recycler_view)

//========================================================
//        getAudioListFromMediaDatabase();
        val selection = MediaStore.Audio.Media.IS_MUSIC + " = 1"
        val sortOrder = MediaStore.Audio.Media.TITLE + " COLLATE LOCALIZED ASC"
        val cursor = requireActivity().contentResolver
                .query(MediaStore.Audio.Media.EXTERNAL_CONTENT_URI,
                        null, selection, null,
                        null)
        mAdapter = AudioAdapter(context, cursor)


        //       item click interface
        mAdapter!!.setOnItemClickListener (object : onItemClickListener {
            override fun onItemClicked(mUri: Uri) {
                //**********************
                Log.d("AudioAdapter", "item 클릭 Uri : $mUri")

//                //  startService로 MusicService#playMusic 사용 ------------------대체
//                /**
//                 * 음악 틀기
//                 * {@link com.example.capture.services.MusicService#playMusic(Uri)}
//                 */
//                Intent intent = new Intent(mContext, MusicService.class);
//                intent.setAction(MusicService.ACTION_PLAY);
//                intent.putExtra("uri", mUri);
//                mContext.startService(intent);
                MusicApplication.getInstance()!!.getServiceInterface()!!.play(mUri)

                // fragment 옮기기 -> player fragment
                /**
                 * Activity 로 정보 쏘기
                 * [com.example.mediaplayer.MusicPlayerActivity.setPage]
                 */
                // UI 갱신 ;
                if (callback != null) callback!!.setPage(1)
//                int event = 1;
//                EventBus.getDefault().post(event);  // ** Boolean 으로
//**********************
            }
        })
        Log.d("노래개수 : ", mAdapter!!.mSongList!!.size.toString())
        recyclerView.adapter = mAdapter

////========================================================
////   cursor 로 데이터를 가져와서 처리
//        Cursor cursor = getActivity().getContentResolver()
//                .query(MediaStore.Audio.Media.EXTERNAL_CONTENT_URI,
//                null, null, null, null);
//        recyclerView.setAdapter(new SongRecyclerAdapter(getActivity(), cursor));
////        cursor.close();

////========================================================
////   Loader를 사용하여 처리
//        getAudioListFromMediaDatabase();
//        recyclerView.setAdapter(new SongRecyclerAdapter(getActivity(), null));

//========================================================
        // GridLayout 기능추가
        val layoutManager = GridLayoutManager(activity, 1)
        layoutManager.orientation = LinearLayoutManager.VERTICAL
        recyclerView.layoutManager = layoutManager
    }

    val audioListFromMediaDatabase: Unit
        get() {
//   Loader를 이용하여  전체 cursor data를 얻어 adapter에 연결
            LoaderManager.getInstance(this).initLoader(LOADER_ID, null, object : LoaderManager.LoaderCallbacks<Cursor?> {
//        getLoaderManager().initLoader(101, null, new LoaderManager.LoaderCallbacks<Cursor>() {
                override fun onCreateLoader(id: Int, args: Bundle?): Loader<Cursor?> {
                    val uri = MediaStore.Audio.Media.EXTERNAL_CONTENT_URI
                    val projection = arrayOf(
                            MediaStore.Audio.Media._ID,
                            MediaStore.Audio.Media.TITLE,
                            MediaStore.Audio.Media.ARTIST,
                            MediaStore.Audio.Media.ALBUM,
                            MediaStore.Audio.Media.ALBUM_ID,
                            MediaStore.Audio.Media.DURATION,
                            MediaStore.Audio.Media.DATA
                    )
                    val selection = MediaStore.Audio.Media.IS_MUSIC + " = 1"
                    val sortOrder = MediaStore.Audio.Media.TITLE + " COLLATE LOCALIZED ASC"
                    //                return new CursorLoader(getApplicationContext(), uri, projection, selection, null, sortOrder);
                    return CursorLoader(context!!, uri, projection, selection, null, sortOrder)
                }

                override fun onLoadFinished(loader: Loader<Cursor?>, data: Cursor?) {
                    if (data != null && data.count > 0) {
                        while (data.moveToNext()) {
                            Log.d(TAG, "Title:" + data.getString(data.getColumnIndex(MediaStore.Audio.Media.TITLE)))
                        }
                    }
                    mAdapter!!.swapCursor(data!!)
                }

                override fun onLoaderReset(loader: Loader<Cursor?>) {
                    mAdapter!!.swapCursor(null)
                }
            })
        }
    private val mConnection: ServiceConnection = object : ServiceConnection {
        override fun onServiceConnected(className: ComponentName,
                                        service: IBinder) {
            // We've bound to LocalService, cast the IBinder and get LocalService instance
            val binder = service as LocalBinder
            mService = binder.getService()
            mBound = true
            /**
             * [MusicControllerFragment.updateUI]
             */
            /**
             * [MusicPlayerFragment.updateUI]
             */
            EventBus.getDefault().post(mService!!.isPlaying()) //???
        }

        override fun onServiceDisconnected(arg0: ComponentName) {
            mBound = false
        }
    }

    companion object {
        private const val LOADER_ID = 0x001
        private const val TAG = "SongFragment"
    }
}