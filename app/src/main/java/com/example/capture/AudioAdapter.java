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
    interface onItemClickListener{
        void onItemClicked(Uri model);
//        void onItemClicked(AudioItem model);
    }

    private String TAG = "오디오어댑터";
    private Context mContext;
    private onItemClickListener mListener;
    public ArrayList<AudioItem> mItems;  // mSongList와 둘중 하나 선택
    public ArrayList<Uri> mSongList;
    private Uri mUri;

    public AudioAdapter() {}

    public AudioAdapter(onItemClickListener listener) {
        mListener = listener;
    }

    public AudioAdapter(Context context) {
//            super(context, mSongList);
        this.mContext = context;
        this.mSongList = getSongList();
    }

    public void setItems(ArrayList <AudioItem> items) {
        this.mItems = items;
        notifyDataSetChanged();
    }

    public void setSongs(ArrayList <Uri> Songs) {
        this.mSongList = Songs;
        notifyDataSetChanged();
    }

    @NonNull
    @Override
    public MyAudioViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.listitem_audio, parent, false);
        final MyAudioViewHolder viewHolder = new MyAudioViewHolder(view);
        view.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if(mListener != null){
                    final Uri item = mSongList.get(viewHolder.getAdapterPosition());
                    mListener.onItemClicked(item);
//                    final AudioItem item = mItems.get(viewHolder.getAdapterPosition());
//                    mListener.onItemClicked(item);
                }
            }
        });
        return viewHolder;
    }

    @Override
    public void onBindViewHolder(@NonNull MyAudioViewHolder viewHolder, int position) {
        mUri = mSongList.get(position);

//        MediaMetadataRetriever 를 이용하여 데이터 처리 ================
        final MediaMetadataRetriever retriever;
        try {
            retriever = new MediaMetadataRetriever();
            retriever.setDataSource(mContext, mUri);
            AudioItem audioItem = AudioItem.bindRetriever(retriever);
//            AudioItem item = mItems.get(position);
            ((MyAudioViewHolder) viewHolder).setAudioItem(audioItem, position);
            Log.e(TAG, "onBinding Title:" + audioItem.mTitle + ", Artist:" + audioItem.mArtist + mUri);

        } catch (Exception e) {
            Log.d(TAG, "Retriever 오류! ");
            e.printStackTrace();
        }

//        viewHolder.itemView.setOnClickListener(new View.OnClickListener() {
//            @Override
//            public void onClick(View v) {
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
//                // UI 갱신 ;
//                int event = 1;
//                EventBus.getDefault().post(event);  // ** 동작하지 않음 => callback 으로?
//
////                if (callback != null) callback.setPage(event);
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
                Uri uri = ContentUris.withAppendedId(MediaStore.Audio.Media.EXTERNAL_CONTENT_URI, cursor.getLong(
                        cursor.getColumnIndexOrThrow(BaseColumns._ID)));
                songList.add(uri);
                Log.d(TAG, "getSongList Title:" + cursor.getString(cursor.getColumnIndex(MediaStore.Audio.Media.TITLE)));
            }
        }
        mSongList = songList;  // ??
        return songList;
    }

    public class MyAudioViewHolder extends RecyclerView.ViewHolder{
        private final Uri artworkUri = Uri.parse("content://media/external/audio/albumart");
        private ImageView mImgAlbumArt;
        private TextView mTxtTitle;
        private TextView mTxtSubTitle;
        private TextView mTxtDuration;
        private AudioItem mItem;
        private int mPosition;

        public MyAudioViewHolder(@NonNull View view) {
            super(view);
            mImgAlbumArt = (ImageView) view.findViewById(R.id.img_albumart);
            mTxtTitle = (TextView) view.findViewById(R.id.txt_title);
            mTxtSubTitle = (TextView) view.findViewById(R.id.txt_sub_title);
            mTxtDuration = (TextView) view.findViewById(R.id.txt_duration);
//            view.setOnClickListener(new View.OnClickListener() {
//                @Override
//                public void onClick(View v) {
//                    // ..
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
//                    // UI 갱신 ;
//                    int event = 1;
//                    EventBus.getDefault().post(event);  // ** 동작하지 않음 => callback 으로?
//                }
//            });
        }

        public void setAudioItem(AudioItem item, int position) {
            mItem = item;
            mPosition = position;
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
