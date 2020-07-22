package com.example.capture.frags;

import android.app.Activity;
import android.content.ComponentName;
import android.content.ContentUris;
import android.content.Context;
import android.content.Intent;
import android.content.ServiceConnection;
import android.content.res.Resources;
import android.database.Cursor;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.media.Image;
import android.media.MediaMetadataRetriever;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.os.IBinder;
import android.provider.BaseColumns;
import android.provider.MediaStore;
import android.text.format.DateFormat;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentActivity;
import androidx.loader.app.LoaderManager;
import androidx.loader.content.CursorLoader;
import androidx.loader.content.Loader;
import androidx.recyclerview.widget.GridLayoutManager;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
//import androidx.recyclerview.widget.RecyclerView.ViewHolder;

//import com.squareup.picasso.Picasso;  // Glide 와 호환
import com.example.capture.AudioAdapter;
import com.example.capture.FragmentCallback;
import com.example.capture.MusicApplication;
import com.example.capture.MusicPlayerActivity;
import com.example.capture.R;
import com.example.capture.adapters.CursorRecyclerViewAdapter;
import com.example.capture.services.MusicService;

import org.greenrobot.eventbus.EventBus;

import java.util.ArrayList;

public class SongFragment extends Fragment {
    FragmentCallback callback;
    private boolean mBound = false;
    private Context mContext;
    private AudioAdapter mAdapter;

//    public static SongRecyclerAdapter mAdapter;   // **
    private MusicService mService;
    private static final String TAG = "SongFragment";

    public SongFragment() {
    }

    public SongFragment(int contentLayoutId) {
        super(contentLayoutId);
    }

    //  callBack 함수로 MusicPlayerActivity의 setPage(int pageNum) 호출
    @Override
    public void onAttach(@NonNull Context context) {
        super.onAttach(context);
        mContext = context;
        if (context instanceof FragmentCallback) {
            callback = (FragmentCallback) context;
        }
    }
    @Override
    public void onDetach() {
        super.onDetach();   // songFrag 없어짐?
        if(callback != null) callback = null;
    }

    @Override
    public void onStart() {
        super.onStart();

        Intent intent = new Intent(getActivity(), MusicService.class);
        getActivity().bindService(intent, mConnection, Context.BIND_AUTO_CREATE);
        Log.e(TAG , "서비스 연결");
    }

    @Override
    public void onStop() {
        super.onStop();

        if (mBound) {
            getActivity().unbindService(mConnection);
            mBound = false;
            Log.e(TAG , "서비스 연결 해제");
        }
    }

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {

        return inflater.inflate(R.layout.fragment_song, container, false);
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        Log.d("노래목록" , "시작됨");
        RecyclerView recyclerView = view.findViewById(R.id.recycler_view);

//========================================================
        mAdapter = new AudioAdapter(mContext);
        mAdapter.setOnItemClickListener(new AudioAdapter.onItemClickListener() {
            @Override
            public void onItemClicked(Uri mUri) {
                //**********************
                Log.d("AudioAdapter", "item 클릭 Uri : " + mUri);

                //  startService로 MusicService#playMusic 사용 ------------------대체
                /**
                 * 음악 틀기
                 * {@link com.example.capture.services.MusicService#playMusic(Uri)}
                 */
                Intent intent = new Intent(mContext, MusicService.class);
                intent.setAction(MusicService.ACTION_PLAY);
                intent.putExtra("uri", mUri);
                mContext.startService(intent);

                // fragment 옮기기 -> player fragment
                /**
                 * Activity 로 정보 쏘기
                 *  {@link com.example.capture.MusicPlayerActivity#setPage(int)}
                 */
                // UI 갱신 ;
                if (callback != null) callback.setPage(1);
//                int event = 1;
//                EventBus.getDefault().post(event);  // ** Boolean 으로
                //**********************

            }
        });

//        MusicPlayerActivity.mSongList = mAdapter.mSongList;  // ?
//        makeSongList();
        Log.d("노래개수 : " , String.valueOf(mAdapter.mSongList.size()));

// onStart 와 중복?
//        // MusicPlayerActivity.mSongList 만든 후 서비스 시작
//        Intent intent = new Intent(getActivity(), MusicService.class);
//        getActivity().bindService(intent, mConnection, Context.BIND_AUTO_CREATE);

        recyclerView.setAdapter(mAdapter);
//        recyclerView.setAdapter(new AudioAdapter(getActivity(), MusicPlayerActivity.mSongList));
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
        GridLayoutManager layoutManager = new GridLayoutManager(getActivity(), 1);
        layoutManager.setOrientation(LinearLayoutManager.VERTICAL);
        recyclerView.setLayoutManager(layoutManager);
    }




////====================================================================
////  사용 안함  from
//// ***** CursorRecyclerViewAdapter 사용하기
//    public static class SongRecyclerAdapter extends CursorRecyclerViewAdapter<AudioViewHolder> {
//
//        private Context mContext;
//
//        public SongRecyclerAdapter(Context context, Cursor cursor) {
//            // cursor 는 전체 data
//            super(context, cursor);
//            mContext = context;
////            mAdapter = this;   // **
////            mAdapter.swapCursor(cursor);  // ** 전체 data 연결
//        }
//
//        @NonNull
//        @Override
//        public AudioViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
//            View v = LayoutInflater.from(parent.getContext()).inflate(R.layout.listitem_audio, parent, false);
//            return new AudioViewHolder(v);
//        }
//
////        @RequiresApi(api = Build.VERSION_CODES.KITKAT)  // 필요????
//        @Override
//        public void onBindViewHolder(AudioViewHolder viewHolder, Cursor cursor) {
//            // content://audio/media/1      ------> 하나의 item씩 처리 *******
////            content://media/external/audio/albumart/1
//
//            final Uri uri = ContentUris.withAppendedId(MediaStore.Audio.Media.EXTERNAL_CONTENT_URI,  // 84);
//                    cursor.getLong(cursor.getColumnIndexOrThrow(BaseColumns._ID)));
//
//            final String title = cursor.getString(cursor.getColumnIndex(MediaStore.Audio.AudioColumns.TITLE));
//
////          입력받은 cursor를 이용하여 데이터 처리 =======================
//            AudioItem audioItem = AudioItem.bindCursor(cursor);
//            if (audioItem == null) return;
////            Image data 설정 미흡
//            viewHolder.setAudioItem(audioItem, cursor.getPosition());
////            Log.d(TAG, "Title:" + audioItem.mTitle + ", Artist:" + audioItem.mArtist);
//
//////        MediaMetadataRetriever 를 이용하여 데이터 처리 ================
////            final MediaMetadataRetriever retriever;
//////            try {
////                retriever = new MediaMetadataRetriever();
////                retriever.setDataSource(mContext, uri);
////                AudioItem audioItem = AudioItem.bindRetriever(retriever);
////                viewHolder.setAudioItem(audioItem, cursor.getPosition());
//////                Log.d(TAG, "Title:" + audioItem.mTitle + ", Artist:" + audioItem.mArtist);
//
////            } catch (Exception e) {
////                Log.d("SongFrag", "Retriever 오류! ");
////                e.printStackTrace();
////            }
//
//            viewHolder.itemView.setOnClickListener(new View.OnClickListener() {
//                @Override
//                public void onClick(View v) {
//
//                    Log.d(TAG, "item 클릭 Uri : " + uri);
//                    Log.d(TAG, "title : " + title);
//
////                    //  EventBus로 MusicPlayerActivity#playMusic 사용
////                    /**
////                     * 음악 틀기
////                     * {@link com.example.capture.MusicPlayerActivity#playMusic(Uri)}
////                     */
////                    EventBus.getDefault().post(uri);
//
////                    /**
////                     * 아래쪽 프래그먼트로 정보 쏘기
////                     *  {@link com.example.capture.frags.MusicControllerFragment#updateUI(MediaMetadataRetriever)}
////                     */
////                    EventBus.getDefault().post(retriever);
//
//                    //  Service로 MusicService#playMusic 사용
//                    /**
//                      * 음악 틀기
//                      * {@link MusicService#playMusic(Uri)}
//                      */
//                    Intent intent = new Intent(mContext, MusicService.class);
//                    intent.setAction(MusicService.ACTION_PLAY);
//                    intent.putExtra("uri", uri);
//
//                    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
//                        mContext.startForegroundService(intent);
//                    } else{
//                        mContext.startService(intent);
//                    }
//                }
//            });
//        }
//
////   필요 없음?
////        private class SongClickEvent {
////            public Uri uri;
////            public SongClickEvent(Uri uri) {
////                this.uri = uri;
////            }
////        }
//    }
////====================================================================
////  사용 안함  to


//    public class SongListRecyclerAdapter extends RecyclerView.Adapter<AudioViewHolder> {
//
//        private Context mContext;
//        private ArrayList<Uri> mSongList;
//
//        public SongListRecyclerAdapter(Context context, ArrayList<Uri> SongList) {
////            super(context, mSongList);
//            this.mContext = context;
//            this.mSongList = SongList;
////            mAdapter = this;   // **
////            mAdapter.swapCursor(cursor);  // ** 전체 data 연결
//        }
//
//        @NonNull
//        @Override
//        public AudioViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
//            View v = LayoutInflater.from(parent.getContext()).inflate(R.layout.listitem_audio, parent, false);
//            return new AudioViewHolder(v);
//        }
//
//        @Override
//        public int getItemCount() {
//            return mSongList.size();
//        }
//
//        @Override
//        public void onBindViewHolder(AudioViewHolder viewHolder, int position) {
//            // content://audio/media/1      ------> 하나의 item씩 처리 *******
////            content://media/external/audio/albumart/1
//
//            final Uri uri = mSongList.get(position);
//
////        MediaMetadataRetriever 를 이용하여 데이터 처리 ================
//            final MediaMetadataRetriever retriever;
//            try {
//                retriever = new MediaMetadataRetriever();
//                retriever.setDataSource(mContext, uri);
//                AudioItem audioItem = AudioItem.bindRetriever(retriever);
//                viewHolder.setAudioItem(audioItem, position);
////                Log.d(TAG, "Title:" + audioItem.mTitle + ", Artist:" + audioItem.mArtist);
//
//            } catch (Exception e) {
//                Log.d("SongFrag", "Retriever 오류! ");
//                e.printStackTrace();
//            }
//
//            viewHolder.itemView.setOnClickListener(new View.OnClickListener() {
//                @Override
//                public void onClick(View v) {
//
//                    Log.d(TAG, "item 클릭 Uri : " + uri);
////                    Log.d(TAG, "title : " + title);
//
//                    //  startService로 MusicService#playMusic 사용 ------------------대체
//                    /**
//                     * 음악 틀기
//                     * {@link com.example.capture.services.MusicService#playMusic(Uri)}
//                     */
//                    Intent intent = new Intent(mContext, MusicService.class);
//                    intent.setAction(MusicService.ACTION_PLAY);
//                    intent.putExtra("uri", uri);
//                    mContext.startService(intent);
//
//////                    //  Service로 MusicService#playMusic 사용 ------------------대체
////                    // mConnection 후 직접 접근
////                    Intent intent = new Intent(getActivity(), MusicService.class);
////                    getActivity().bindService(intent, mConnection, Context.BIND_AUTO_CREATE);
////                    mService.playMusic(uri);
//
//                    // fragment 옮기기 -> player fragment
//                    /**
//                     * Axtivity 로 정보 쏘기
//                     *  {@link com.example.capture.MusicPlayerActivity#setPage(int)}
//                     */
//                    // UI 갱신 ;
//                    int event = 1;
////                    EventBus.getDefault().post(event);  // ** 동작하지 않음 => callback 으로?
//
//                    if (callback != null) callback.setPage(event);
//
//                }
//            });
//        }
//
//    }
//
//    public static class AudioItem{
//        public long mId; // 오디오 고유 ID
//        public long mAlbumId; // 오디오 앨범아트 ID
//        public String mTitle; // 타이틀 정보
//        public String mArtist; // 아티스트 정보
//        public String mAlbum; // 앨범 정보
//        public long mDuration; // 재생시간
//        public String mDataPath; // 실제 데이터위치
//        public Bitmap mBitmap = null;
//
//        public static AudioItem bindCursor(Cursor cursor) {
//            // 개별 item, cursor
//            AudioItem audioItem = new AudioItem();
//            //  Audio Image -----
//            int column_index = cursor.getColumnIndexOrThrow(MediaStore.Audio.Media.DATA);
//            String pathId = cursor.getString(column_index);
//            if(pathId == null) return null;
//
//            MediaMetadataRetriever mediaMetadataRetriever = new MediaMetadataRetriever();
//            try {
//                mediaMetadataRetriever.setDataSource(pathId);
//                byte[] albumImage = mediaMetadataRetriever.getEmbeddedPicture();
//                if (null != albumImage) {
//                    audioItem.mBitmap = BitmapFactory.decodeByteArray(albumImage, 0, albumImage.length);
//                } else {
//                audioItem.mBitmap = BitmapFactory.decodeResource(Resources.getSystem(),  R.drawable.music_circle);
//                }
//            } catch (Exception e){
//                return null;
//            }
//            mediaMetadataRetriever.release();
//
//            //-------------------
//            audioItem.mId = cursor.getLong(cursor.getColumnIndex(MediaStore.Audio.AudioColumns._ID));
//            audioItem.mAlbumId = cursor.getLong(cursor.getColumnIndex(MediaStore.Audio.AudioColumns.ALBUM_ID));
//            audioItem.mTitle = cursor.getString(cursor.getColumnIndex(MediaStore.Audio.AudioColumns.TITLE));
//            audioItem.mArtist = cursor.getString(cursor.getColumnIndex(MediaStore.Audio.AudioColumns.ARTIST));
//            audioItem.mAlbum = cursor.getString(cursor.getColumnIndex(MediaStore.Audio.AudioColumns.ALBUM));
//            audioItem.mDuration = cursor.getLong(cursor.getColumnIndex(MediaStore.Audio.AudioColumns.DURATION));
//            audioItem.mDataPath = cursor.getString(cursor.getColumnIndex(MediaStore.Audio.AudioColumns.DATA));
//
//            Log.d(TAG, "bindCursor Title:" + audioItem.mTitle + ", Artist:" + audioItem.mArtist);
//            return audioItem;
//        }
//
//        public static AudioItem bindRetriever(MediaMetadataRetriever retriever) {
//            // 개별 item, retriever
//            AudioItem audioItem = new AudioItem();
////            audioItem.mId = cursor.getLong(cursor.getColumnIndex(MediaStore.Audio.AudioColumns._ID));
////            audioItem.mAlbumId = cursor.getLong(cursor.getColumnIndex(MediaStore.Audio.AudioColumns.ALBUM_ID));
//            audioItem.mTitle = retriever.extractMetadata(MediaMetadataRetriever.METADATA_KEY_TITLE);
//            audioItem.mArtist = retriever.extractMetadata(MediaMetadataRetriever.METADATA_KEY_ARTIST);
//            audioItem.mAlbum = retriever.extractMetadata(MediaMetadataRetriever.METADATA_KEY_ALBUM);
//            audioItem.mDuration = Long.parseLong(retriever.extractMetadata(MediaMetadataRetriever.METADATA_KEY_DURATION));
////            String duration = retriever.extractMetadata((MediaMetadataRetriever.METADATA_KEY_DURATION));
////            audioItem.mDataPath = cursor.getString(cursor.getColumnIndex(MediaStore.Audio.AudioColumns.DATA));
//            //             오디오 앨범 자켓 이미지
//            Bitmap bitmap;
//            byte[] albumImage = retriever.getEmbeddedPicture();
//
//            if(albumImage == null) {
//                bitmap = BitmapFactory.decodeResource(Resources.getSystem(),  R.drawable.snow);
//            } else {
//                bitmap = BitmapFactory.decodeByteArray(albumImage, 0, albumImage.length);
//            }
//
////            try {
////            } catch (Exception e){
//////                Log.e(TAG, "getEmbeddedPicture error, snow 로 대체: " + e);
//////            } finally {
//////                if(bitmap == null) bitmap = BitmapFactory.decodeResource(Resources.getSystem(),  R.drawable.snow);
////            }
//            audioItem.mBitmap = bitmap;
//
//            Log.d(TAG, "bindRetriever Title:" + audioItem.mTitle + ", Artist:" + audioItem.mArtist);
//            return audioItem;
//        }
//    }
//
//    public static class AudioViewHolder extends RecyclerView.ViewHolder {
////        TextView titleTextView;
////        TextView artistTextView;
//
//        private final Uri artworkUri = Uri.parse("content://media/external/audio/albumart");
//        private ImageView mImgAlbumArt;
//        private TextView mTxtTitle;
//        private TextView mTxtSubTitle;
//        private TextView mTxtDuration;
//        private AudioItem mItem;
//        private int mPosition;
//
//        public AudioViewHolder(View view) {
//            super(view);
////            titleTextView = (TextView) itemView.findViewById(android.R.id.text1);
////            artistTextView = (TextView) itemView.findViewById(android.R.id.text2);
//
//            mImgAlbumArt = (ImageView) view.findViewById(R.id.img_albumart);
//            mTxtTitle = (TextView) view.findViewById(R.id.txt_title);
//            mTxtSubTitle = (TextView) view.findViewById(R.id.txt_sub_title);
//            mTxtDuration = (TextView) view.findViewById(R.id.txt_duration);
//            view.setOnClickListener(new View.OnClickListener() {
//                @Override
//                public void onClick(View v) {
//                    // ..
//                }
//            });
//        }
//
//        public void setAudioItem(AudioItem item, int position) {
//            mItem = item;
//            mPosition = position;
//            mTxtTitle.setText(item.mTitle);
//            mTxtSubTitle.setText(item.mArtist + "(" + item.mAlbum + ")");
//            mTxtDuration.setText(DateFormat.format("mm:ss", item.mDuration));
////            Uri albumArtUri = ContentUris.withAppendedId(artworkUri, item.mAlbumId);
////            Glide.with(itemView.getContext()).load(albumArtUri).error(R.drawable.snow).into(mImgAlbumArt);
////            Picasso.get().load(albumArtUri).error(R.drawable.empty_albumart).into(mImgAlbumArt);
//            if(item.mBitmap != null) {
//                mImgAlbumArt.setImageBitmap(item.mBitmap);
//            } else {
//                mImgAlbumArt.setImageResource(R.drawable.snow);
//            }
//        }
//    }  // AudioHolder
//
////    // 사용 안함
////    private void getAudioListFromMediaDatabase() {
//////   Loader를 이용하여  전체 cursor data를 얻어 adapter에 연결
//////        getSupportLoaderManager().initLoader(LOADER_ID, null, new LoaderManager.LoaderCallbacks<Cursor>() {
////        getLoaderManager().initLoader(101, null, new LoaderManager.LoaderCallbacks<Cursor>() {
////            @Override
////            public Loader<Cursor> onCreateLoader(int id, Bundle args) {
////                Uri uri = MediaStore.Audio.Media.EXTERNAL_CONTENT_URI;
////                String[] projection = new String[]{
////                        MediaStore.Audio.Media._ID,
////                        MediaStore.Audio.Media.TITLE,
////                        MediaStore.Audio.Media.ARTIST,
////                        MediaStore.Audio.Media.ALBUM,
////                        MediaStore.Audio.Media.ALBUM_ID,
////                        MediaStore.Audio.Media.DURATION,
////                        MediaStore.Audio.Media.DATA
////                };
////                String selection = MediaStore.Audio.Media.IS_MUSIC + " = 1";
////                String sortOrder = MediaStore.Audio.Media.TITLE + " COLLATE LOCALIZED ASC";
//////                return new CursorLoader(getApplicationContext(), uri, projection, selection, null, sortOrder);
////                return new CursorLoader(getContext(), uri, projection, selection, null, sortOrder);
////            }
////
////            @Override
////            public void onLoadFinished(Loader<Cursor> loader, Cursor data) {
////                if (data != null && data.getCount() > 0) {
////                    while (data.moveToNext()) {
////                        Log.d(TAG, "Title:" + data.getString(data.getColumnIndex(MediaStore.Audio.Media.TITLE)));
////                    }
////                }
////
//////                mAdapter.swapCursor(data);
////                SongFragment.mAdapter.swapCursor(data);
////            }
////
////            @Override
////            public void onLoaderReset(Loader<Cursor> loader) {
////                SongFragment.mAdapter.swapCursor(null);
////            }
////        });
////    }

    private ServiceConnection mConnection = new ServiceConnection() {

        @Override
        public void onServiceConnected(ComponentName className,
                                       IBinder service) {
            // We've bound to LocalService, cast the IBinder and get LocalService instance
            MusicService.LocalBinder binder = (MusicService.LocalBinder) service;
            mService = binder.getService();
            mBound = true;

            /**
             * {@link MusicControllerFragment#updateUI(Boolean)}
             */
            /**
             * {@link PlayerFragment#updateUI(Boolean)}
             */
            EventBus.getDefault().post(mService.isPlaying());
        }

        @Override
        public void onServiceDisconnected(ComponentName arg0) {
            mBound = false;
        }
    };

//    public void makeSongList(){
//        ArrayList<Uri> mSongList = new ArrayList<>();
//        String selection = MediaStore.Audio.Media.IS_MUSIC + " = 1";
//        String sortOrder = MediaStore.Audio.Media.TITLE + " COLLATE LOCALIZED ASC";
//
//        Cursor cursor = getActivity().getContentResolver().query(MediaStore.Audio.Media.EXTERNAL_CONTENT_URI,
//                null,
//                selection,
//                null,
//                null);
////        sortOrder);
//        if (cursor != null) {
//            while (cursor.moveToNext()) {
//                Uri uri = ContentUris.withAppendedId(MediaStore.Audio.Media.EXTERNAL_CONTENT_URI, cursor.getLong(
//                        cursor.getColumnIndexOrThrow(BaseColumns._ID)));
//                mSongList.add(uri);
//                Log.d(TAG, "Title:" + cursor.getString(cursor.getColumnIndex(MediaStore.Audio.Media.TITLE)));
//            }
//            MusicPlayerActivity.mSongList = mSongList;
//        }
//    }

}
