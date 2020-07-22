package com.example.capture;

import android.content.ContentUris;
import android.content.Context;
import android.content.Intent;
import android.content.res.Resources;
import android.database.Cursor;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.media.MediaMetadataRetriever;
import android.net.Uri;
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
import androidx.recyclerview.widget.RecyclerView;

import com.example.capture.services.MusicService;

import org.greenrobot.eventbus.EventBus;

import java.util.ArrayList;
import java.util.List;

public class AudioAdapter extends RecyclerView.Adapter <AudioAdapter.MyAudioViewHolder> {
    public interface onItemClickListener{
        void onItemClicked(Uri model);
    }
    private onItemClickListener mListener;
    private FragmentCallback callback;

    private String TAG = "오디오어댑터";
    private Context mContext;
    public ArrayList<Uri> mSongList;
    private Uri mUri;

    // 리스너 객체 전달함수
    public void setOnItemClickListener(onItemClickListener listener){
        this.mListener = listener;
    }

    public AudioAdapter() {}

    public AudioAdapter(onItemClickListener listener) {
        mListener = listener;
    }

    public AudioAdapter(Context context) {
        this.mContext = context;
        if(mSongList == null) this.mSongList = getSongList();
        if (context instanceof FragmentCallback) {
            callback = (FragmentCallback) context;
        }
    }

    public void setSongs(ArrayList <Uri> Songs) {       // ?? 사용처 확인
        this.mSongList = Songs;
        notifyDataSetChanged();
    }

    // onCreateViewHolder() - 아이템 뷰를 위한 뷰홀더 객체 생성하여 리턴.
    @NonNull
    @Override
    public MyAudioViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.listitem_audio, parent, false);
//        View view = ((LayoutInflater)parent.getContext().getSystemService(Context.LAYOUT_INFLATER_SERVICE)).inflate(R.layout.listitem_audio, parent, false) ;
        final MyAudioViewHolder viewHolder = new MyAudioViewHolder(view);
        view.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if(mListener != null){
                    final Uri item = mSongList.get(viewHolder.getAdapterPosition());
                    mListener.onItemClicked(item);
                }
            }
        });
        return viewHolder;
    }

    // onBindViewHolder() - position에 해당하는 데이터를 뷰홀더의 아이템뷰에 표시.
    @Override
    public void onBindViewHolder(@NonNull MyAudioViewHolder viewHolder, int position) {
        mUri = mSongList.get(position);

        Log.i(TAG, "BindViewHolder : " + position + "");
//        MediaMetadataRetriever 를 이용하여 데이터 처리 ================
        final MediaMetadataRetriever retriever;
        try {
            retriever = new MediaMetadataRetriever();
            retriever.setDataSource(mContext, mUri);
            AudioItem audioItem = AudioItem.bindRetriever(retriever);
            ((MyAudioViewHolder) viewHolder).setAudioItem(audioItem, position);
            Log.e(TAG, "onBinding Title:" + audioItem.mTitle + ", Artist:" + audioItem.mArtist + mUri);

        } catch (Exception e) {
            Log.d(TAG, "Retriever 오류! ");
            e.printStackTrace();
        }

//        viewHolder.mView.setOnClickListener(new View.OnClickListener() {
//            @Override
//            public void onClick(View v) {
//                if(mListener != null){
//                    final Uri item = mSongList.get(viewHolder.getAdapterPosition());
//                    mListener.onItemClicked(item);
//                }
//
//                Log.d(TAG, "item 클릭 Uri : " + mUri);
////                    Log.d(TAG, "title : " + title);
//
//                //  startService로 MusicService#playMusic 사용 ------------------대체
//                /**
//                 * 음악 틀기
//                 * {@link com.example.capture.services.MusicService#playMusic(Uri)}
//                 */
//                Intent intent = new Intent(mContext, MusicService.class);
//                intent.setAction(MusicService.ACTION_PLAY);
//                intent.putExtra("uri", mUri);
//                mContext.startService(intent);
//
//                // fragment 옮기기 -> player fragment
//                /**
//                 * Axtivity 로 정보 쏘기
//                 *  {@link com.example.capture.MusicPlayerActivity#setPage(int)}
//                 */
//
//                if (callback != null) callback.setPage(1);
//            }
//        });
    }

    @Override
    public int getItemCount() {
        return mSongList.size();
//        return mItems.size();
    }

    public ArrayList<Uri> getSongList(){
        ArrayList<Uri> songList = new ArrayList<>();
        String selection = MediaStore.Audio.Media.IS_MUSIC + " = 1";
        String sortOrder = MediaStore.Audio.Media.TITLE + " COLLATE LOCALIZED ASC";

        Cursor cursor = mContext.getContentResolver().query(MediaStore.Audio.Media.EXTERNAL_CONTENT_URI,
                null,
                selection,
                null,
                null);
//        sortOrder);
        if (cursor != null) {
            while (cursor.moveToNext()) {
                Uri uri = ContentUris.withAppendedId(MediaStore.Audio.Media.EXTERNAL_CONTENT_URI,
                        cursor.getLong(
                        cursor.getColumnIndexOrThrow(BaseColumns._ID)));
                songList.add(uri);
                Log.d(TAG, "getSongList Title:" + cursor.getString(cursor.getColumnIndex(MediaStore.Audio.Media.TITLE)));
            }
        }
        mSongList = songList;  // ??
        return songList;
    }

    // 아이템 뷰를 저장하는 뷰홀더 클래스.
    public class MyAudioViewHolder extends RecyclerView.ViewHolder{
        private final Uri artworkUri = Uri.parse("content://media/external/audio/albumart");
        private ImageView mImgAlbumArt;
        private TextView mTxtTitle;
        private TextView mTxtSubTitle;
        private TextView mTxtDuration;
//        private AudioItem mItem;
//        private int mPosition;
//        private View mView;

        // TODO: 2020-07-16
        // viewholder 재사용

        public MyAudioViewHolder(@NonNull View view) {
            super(view);
            // 뷰 객체에 대한 참조. (hold strong reference)
//            mView = view;
            mImgAlbumArt = (ImageView) view.findViewById(R.id.img_albumart);
            mTxtTitle = (TextView) view.findViewById(R.id.txt_title);
            mTxtSubTitle = (TextView) view.findViewById(R.id.txt_sub_title);
            mTxtDuration = (TextView) view.findViewById(R.id.txt_duration);
//            view.setOnClickListener(new View.OnClickListener() {
//                @Override
//                public void onClick(View v) {
//                    // ..
//                    if(mListener != null){
//                        mListener.onItemClicked(mUri);
//                    }
////                    // mSongList 초기화 ??
////                    MusicApplication.getInstance().getServiceInterface().setmSongList(getSongList());
////
//                    Log.d("AudioAdapter", "item 클릭 Uri : " + mUri);
////                    Log.d(TAG, "title : " + title);
//
//                    //  startService로 MusicService#playMusic 사용 ------------------대체
//                    /**
//                     * 음악 틀기
//                     * {@link com.example.capture.services.MusicService#playMusic(Uri)}
//                     */
//                    Intent intent = new Intent(mContext, MusicService.class);
//                    intent.setAction(MusicService.ACTION_PLAY);
//                    intent.putExtra("uri", mUri);
//                    mContext.startService(intent);
//
//                    // fragment 옮기기 -> player fragment
//                    /**
//                     * Activity 로 정보 쏘기
//                     *  {@link com.example.capture.MusicPlayerActivity#setPage(int)}
//                     */
//                     if (callback != null) callback.setPage(1);
//                }
//            });
        }

        public void setAudioItem(AudioItem item, int position) {
//            mItem = item;
//            mPosition = position;
            mTxtTitle.setText(item.mTitle);
            mTxtSubTitle.setText(item.mArtist + "(" + item.mAlbum + ")");
            mTxtDuration.setText(DateFormat.format("mm:ss", item.mDuration));
//            Uri albumArtUri = ContentUris.withAppendedId(artworkUri, item.mAlbumId);
//            Glide.with(itemView.getContext()).load(albumArtUri).error(R.drawable.snow).into(mImgAlbumArt);
//            Picasso.get().load(albumArtUri).error(R.drawable.empty_albumart).into(mImgAlbumArt);
            if(item.mBitmap != null) {
                mImgAlbumArt.setImageBitmap(item.mBitmap);
            } else {
                mImgAlbumArt.setImageResource(R.drawable.snow);
            }
        }

    }

    public static class AudioItem {
        public long mId; // 오디오 고유 ID
        public long mAlbumId; // 오디오 앨범아트 ID
        public String mTitle; // 타이틀 정보
        public String mArtist; // 아티스트 정보
        public String mAlbum; // 앨범 정보
        public long mDuration; // 재생시간
        public String mDataPath; // 실제 데이터위치
        public Bitmap mBitmap = null;

        public static AudioItem bindRetriever(MediaMetadataRetriever retriever) {
            // 개별 item, retriever
            AudioItem audioItem = new AudioItem();
//            audioItem.mId = cursor.getLong(cursor.getColumnIndex(MediaStore.Audio.AudioColumns._ID));
//            audioItem.mAlbumId = cursor.getLong(cursor.getColumnIndex(MediaStore.Audio.AudioColumns.ALBUM_ID));
            audioItem.mTitle = retriever.extractMetadata(MediaMetadataRetriever.METADATA_KEY_TITLE);
            audioItem.mArtist = retriever.extractMetadata(MediaMetadataRetriever.METADATA_KEY_ARTIST);
            audioItem.mAlbum = retriever.extractMetadata(MediaMetadataRetriever.METADATA_KEY_ALBUM);
            audioItem.mDuration = Long.parseLong(retriever.extractMetadata(MediaMetadataRetriever.METADATA_KEY_DURATION));
//            String duration = retriever.extractMetadata((MediaMetadataRetriever.METADATA_KEY_DURATION));
//            audioItem.mDataPath = cursor.getString(cursor.getColumnIndex(MediaStore.Audio.AudioColumns.DATA));
            //             오디오 앨범 자켓 이미지
            Bitmap bitmap;
            byte[] albumImage = retriever.getEmbeddedPicture();

            if(albumImage == null) {
                bitmap = BitmapFactory.decodeResource(Resources.getSystem(),  R.drawable.snow);
            } else {
                bitmap = BitmapFactory.decodeByteArray(albumImage, 0, albumImage.length);
            }

            audioItem.mBitmap = bitmap;

            Log.d("AudioItem bindRetriever", " Title:" + audioItem.mTitle + ", Artist:" + audioItem.mArtist);
            return audioItem;
        }

    }
}
