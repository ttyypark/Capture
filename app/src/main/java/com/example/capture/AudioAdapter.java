package com.example.capture;

import android.content.ContentUris;
import android.content.Context;
import android.content.Intent;
import android.content.res.Resources;
import android.database.Cursor;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.drawable.Drawable;
import android.media.MediaMetadataRetriever;
import android.net.Uri;
import android.os.ParcelFileDescriptor;
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

import com.bumptech.glide.Glide;
import com.example.capture.adapters.CursorRecyclerViewAdapter;
import com.example.capture.services.MusicService;
import com.squareup.picasso.Picasso;

import org.greenrobot.eventbus.EventBus;

import java.io.FileDescriptor;
import java.util.ArrayList;
import java.util.List;

public class AudioAdapter extends CursorRecyclerViewAdapter<AudioAdapter.MyAudioViewHolder> {
    public interface onItemClickListener{
        void onItemClicked(Uri model);
    }
    private onItemClickListener mListener;
    private FragmentCallback callback;

    private String TAG = "오디오어댑터";
    private Context mContext;
    public ArrayList<Uri> mSongList;
    public ArrayList<Long> mSongID;
    private Uri mUri;

    // 리스너 객체 전달함수
    public void setOnItemClickListener(onItemClickListener listener){
        this.mListener = listener;
    }

    public AudioAdapter(onItemClickListener listener) {
        mListener = listener;
    }

    public AudioAdapter(Context context, Cursor cursor) {
        super(context, cursor);
        this.mContext = context;
        if(mSongList == null) this.mSongList = getSongList();
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
//  onCreateViewHolder에서 setOnClickListener사용.
//          ViewHolder에서 사용할 수도 있고
//          onBindViewHolder 에서도 가능
        view.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if(mListener != null){
                    final Uri item = mSongList.get(viewHolder.getAdapterPosition());
                    mListener.onItemClicked(item);
                    Log.e(TAG, "Playing Title:" + viewHolder.holderTitle);
                }
            }
        });
        return viewHolder;
    }

    @Override
    public void onBindViewHolder(MyAudioViewHolder viewHolder, Cursor cursor) {
        AudioItem audioItem = AudioItem.bindCursor(mContext, cursor);
        ((MyAudioViewHolder) viewHolder).setAudioItem(audioItem, cursor.getPosition());
        Log.e(TAG, "onBinding Title:" + audioItem.mTitle + ", Artist:" + audioItem.mArtist);
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

    @Override
    public int getItemCount() {
        return mSongList.size();
    }

    public ArrayList<Uri> getSongList(){
        ArrayList<Uri> songList = new ArrayList<>();
        mSongID = new ArrayList<>();
        String selection = MediaStore.Audio.Media.IS_MUSIC + " = 1";
        String sortOrder = MediaStore.Audio.Media.TITLE + " COLLATE LOCALIZED ASC";

        Cursor cursor = mContext.getContentResolver().query(MediaStore.Audio.Media.EXTERNAL_CONTENT_URI,
                null,
                selection,
                null,
                null);
//        sortOrder);
        if (cursor != null && cursor.getCount() > 0) {
            while (cursor.moveToNext()) {
                long songID = cursor.getLong(cursor.getColumnIndexOrThrow(BaseColumns._ID));
                mSongID.add(songID);
                Uri uri = ContentUris.withAppendedId(MediaStore.Audio.Media.EXTERNAL_CONTENT_URI,
                        cursor.getLong(
                        cursor.getColumnIndexOrThrow(BaseColumns._ID)));
                songList.add(uri);
//                Log.d(TAG, "getSongList Title:" + cursor.getString(cursor.getColumnIndex(MediaStore.Audio.Media.TITLE)));
            }
        }
        cursor.close();
        return songList;
    }

    // 아이템 뷰를 저장하는 뷰홀더 클래스.
    public static class MyAudioViewHolder extends RecyclerView.ViewHolder{
        private final Uri artworkUri = Uri.parse("content://media/external/audio/albumart");
//        private final Uri artworkUri = MediaStore.Audio.Albums.EXTERNAL_CONTENT_URI;
        private ImageView mImgAlbumArt;
        private TextView mTxtTitle;
        private TextView mTxtSubTitle;
        private TextView mTxtDuration;
        private int mPosition;
        private String holderTitle;


        public MyAudioViewHolder(@NonNull View view) {
            super(view);
            // 뷰 객체에 대한 참조. (hold strong reference)
            mImgAlbumArt = (ImageView) view.findViewById(R.id.img_albumart);
            mTxtTitle = (TextView) view.findViewById(R.id.txt_title);
            mTxtSubTitle = (TextView) view.findViewById(R.id.txt_sub_title);
            mTxtDuration = (TextView) view.findViewById(R.id.txt_duration);
//            view.setOnClickListener(new View.OnClickListener() {
//                @Override
//                public void onClick(View v) {
//                    // play(mPosition)  .....
//                }
//            });
        }

        public void setAudioItem(AudioItem item, int position) {
//            mItem = item;
            mPosition = position;
            holderTitle = item.mTitle;      // temp
            mTxtTitle.setText(item.mTitle);
            mTxtSubTitle.setText(item.mArtist + "(" + item.mAlbum + ")");
            mTxtDuration.setText(DateFormat.format("mm:ss", item.mDuration));
            Uri albumArtUri = ContentUris.withAppendedId(
                    Uri.parse("content://media/external/audio/albumart"), item.mAlbumId);
            Picasso.get().load(albumArtUri).error(R.drawable.snow).into(mImgAlbumArt);
//            Glide.with(itemView.getContext()).load(albumArtUri).error(R.drawable.snow).into(mImgAlbumArt);

//                Drawable image = Drawable.createFromPath(item.mAlbum);
//                mImgAlbumArt.setImageDrawable(image);

//            if(item.mBitmap != null) {
//                mImgAlbumArt.setImageBitmap(item.mBitmap);
//            } else {
//                mImgAlbumArt.setImageResource(R.drawable.snow);
//            }
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

        // 사용 안함
//        public static AudioItem bindRetriever(MediaMetadataRetriever retriever) {
//            // 개별 item, retriever
//            AudioItem audioItem = new AudioItem();
//            audioItem.mTitle = retriever.extractMetadata(MediaMetadataRetriever.METADATA_KEY_TITLE);
//            audioItem.mArtist = retriever.extractMetadata(MediaMetadataRetriever.METADATA_KEY_ARTIST);
//            audioItem.mAlbum = retriever.extractMetadata(MediaMetadataRetriever.METADATA_KEY_ALBUM);
//            audioItem.mDuration = Long.parseLong(retriever.extractMetadata(MediaMetadataRetriever.METADATA_KEY_DURATION));
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
//            audioItem.mBitmap = bitmap;
//
//            Log.d("AudioItem bindRetriever", " Title:" + audioItem.mTitle + ", Artist:" + audioItem.mArtist);
//            return audioItem;
//        }

        public static AudioItem bindCursor(Context context, Cursor cursor) {
            AudioItem audioItem = new AudioItem();
            audioItem.mId = cursor.getLong(cursor.getColumnIndex(MediaStore.Audio.Media._ID));
            audioItem.mAlbumId = cursor.getLong(cursor.getColumnIndex(MediaStore.Audio.Media.ALBUM_ID));
            audioItem.mTitle = cursor.getString(cursor.getColumnIndex(MediaStore.Audio.Media.TITLE));
            audioItem.mArtist = cursor.getString(cursor.getColumnIndex(MediaStore.Audio.Media.ARTIST));
            audioItem.mAlbum = cursor.getString(cursor.getColumnIndex(MediaStore.Audio.Media.ALBUM));
            audioItem.mDuration = cursor.getLong(cursor.getColumnIndex(MediaStore.Audio.Media.DURATION));
            audioItem.mDataPath = cursor.getString(cursor.getColumnIndex(MediaStore.Audio.Media.DATA));
            audioItem.mBitmap = getAlbumart(context, audioItem.mAlbumId);

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

            Log.d("AudioItem bindCursor", " Title:" + audioItem.mTitle + ", Artist:" + audioItem.mArtist);
            return audioItem;
        }
    }

    public static Bitmap getAlbumart(Context context, Long album_id) {
        Bitmap albumArtBitMap = null;
        BitmapFactory.Options options = new BitmapFactory.Options();
        try {
            final Uri sArtworkUri = Uri
                    .parse("content://media/external/audio/albumart");
            Uri uri = ContentUris.withAppendedId(sArtworkUri, album_id);
            ParcelFileDescriptor pfd = context.getContentResolver()
                    .openFileDescriptor(uri, "r");

            if (pfd != null) {
                FileDescriptor fd = pfd.getFileDescriptor();
                albumArtBitMap = BitmapFactory.decodeFileDescriptor(fd, null,
                        options);
                pfd = null;
                fd = null;
            }
        } catch (Error ee) {
        } catch (Exception e) {
        }

        if (null != albumArtBitMap) {
            return albumArtBitMap;
        }
        return getDefaultAlbumArtEfficiently(context.getResources());
    }

    public static Bitmap getDefaultAlbumArtEfficiently(Resources resource) {
        Bitmap defaultBitmapArt = null;
//        if (defaultBitmapArt == null) {
            defaultBitmapArt = BitmapFactory.decodeResource(resource, R.drawable.snow);
//            defaultBitmapArt = decodeSampledBitmapFromResource(resource,
//                    R.drawable.snow, UtilFunctions
//                            .getUtilFunctions().dpToPixels(85, resource),
//                    UtilFunctions.getUtilFunctions().dpToPixels(85, resource));
//        }
        return defaultBitmapArt;
    }
}
