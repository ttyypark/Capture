package com.example.capture.frags;

import android.content.ContentResolver;
import android.content.ContentUris;
import android.content.Context;
import android.content.Intent;
import android.database.Cursor;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.media.Image;
import android.media.MediaMetadataRetriever;
import android.media.ThumbnailUtils;
import android.net.Uri;
import android.os.Bundle;
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
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.GridLayoutManager;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.example.capture.FragmentCallback;
import com.example.capture.R;
import com.example.capture.adapters.CursorRecyclerViewAdapter;
import com.example.capture.services.MusicService;

import org.greenrobot.eventbus.EventBus;

import static android.media.ThumbnailUtils.createVideoThumbnail;

public class VideoFragment extends Fragment {
    private static final String TAG = "비디오";
    FragmentCallback callback;

    public VideoFragment() {
    }

    @Override
    public void onAttach(@NonNull Context context) {
        super.onAttach(context);
        if (context instanceof FragmentCallback) {
            callback = (FragmentCallback) context;
        }
    }

    @Override
    public void onDetach() {
        super.onDetach();
        if(callback != null) callback = null;
    }

    @Override
    public View onCreateView(
            LayoutInflater inflater, ViewGroup container,
            Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        return inflater.inflate(R.layout.fragment_video, container, false);
    }

    public void onViewCreated(@NonNull View view, Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        RecyclerView recyclerView = view.findViewById(R.id.recycler_view);

//========================================================
//   cursor 로 데이터를 가져와서 처리
        Cursor cursor = getActivity().getContentResolver()
                .query(MediaStore.Video.Media.EXTERNAL_CONTENT_URI,
                null, null, null, null);
        recyclerView.setAdapter(new VideoRecyclerAdapter(getActivity(), cursor));

        // 추가
        GridLayoutManager layoutManager = new GridLayoutManager(getActivity(), 3);
        layoutManager.setOrientation(LinearLayoutManager.VERTICAL);
        recyclerView.setLayoutManager(layoutManager);


//        cursor.close();
    }

    public class VideoRecyclerAdapter extends CursorRecyclerViewAdapter<VideoViewHolder> {
        private Context mContext;
//        private ArrayList<Uri> mVideoList;

        public VideoRecyclerAdapter(Context context, Cursor cursor) {
            super(context, cursor);
            this.mContext = context;
        }

        @NonNull
        @Override
        public VideoViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
            View view = LayoutInflater.from(parent.getContext())
                    .inflate(R.layout.video_item, parent, false);
            return new VideoViewHolder(view);
        }

        @Override
        public void onBindViewHolder(@NonNull VideoViewHolder viewHolder, Cursor cursor) {
//          http://commondatastorage.googleapis.com/gtv-videos-bucket/sample/BigBuckBunny.mp4
//            String url = "https://sites.google.com/site/ubiaccessmobile/sample_video.mp4";
//            final Uri uri = Uri.parse(url);
            final Uri uri = ContentUris.withAppendedId(MediaStore.Video.Media.EXTERNAL_CONTENT_URI,  // 84);
                    cursor.getLong(cursor.getColumnIndexOrThrow(BaseColumns._ID)));

            final String title = cursor.getString(cursor.getColumnIndex(MediaStore.Video.VideoColumns.DISPLAY_NAME));

//          입력받은 cursor를 이용하여 데이터 처리 =======================
            VideoItem videoItem = VideoItem.bindCursor(cursor);
            if (videoItem == null) return;
            viewHolder.setVideoItem(videoItem, cursor.getPosition());
            Log.d(TAG, "Title:" + videoItem.mTitle + ", Artist:" + videoItem.mArtist);

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

            viewHolder.itemView.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {

                    Log.d(TAG, "item 클릭 Uri : " + uri);
                    Log.d(TAG, "title : " + title);

                    //  EventBus로 VideoPlayerFragment#playVideo 사용
                    /**
                     * 비디오 보기
                     * {@link com.example.capture.frags.VideoPlayerFragment#playVideo(Uri)}
                     */
                    EventBus.getDefault().post(uri);
                    if (callback != null) callback.setPage(1);

                }
            });

        }

    }

    public static class VideoViewHolder extends RecyclerView.ViewHolder {
//        TextView titleTextView;
//        TextView artistTextView;

//        private final Uri artworkUri = Uri.parse("content://media/external/audio/albumart");
        private ImageView mImgAlbumArt;
        private TextView mTxtTitle;
        private TextView mSubTitle;
        private int mPosition;

        public VideoViewHolder(View view) {
            super(view);

            mImgAlbumArt = (ImageView) view.findViewById(R.id.imageView);
            mTxtTitle = (TextView) view.findViewById(R.id.txt_title);
            mSubTitle = (TextView) view.findViewById(R.id.txt_sub_title);
            view.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    // ..
                }
            });
        }

        public void setVideoItem(VideoItem item, int position) {
//            mItem = item;
            mPosition = position;
            mTxtTitle.setText(item.mDataPath);
            mSubTitle.setText(DateFormat.format("mm:ss", item.mDuration));
//            mTxtDuration.setText(DateFormat.format("mm:ss", item.mDuration));
//            Uri albumArtUri = ContentUris.withAppendedId(artworkUri, item.mAlbumId);
//            Glide.with(itemView.getContext()).load(albumArtUri).error(R.drawable.snow).into(mImgAlbumArt);
//            Picasso.get().load(albumArtUri).error(R.drawable.empty_albumart).into(mImgAlbumArt);
            if(item.mBitmap != null) {
                mImgAlbumArt.setImageBitmap(item.mBitmap);
            } else {
                mImgAlbumArt.setImageResource(R.drawable.snow);
            }
        }
    }  // AudioHolder

    public static class VideoItem{
        public long mId; // 오디오 고유 ID
//        public long mAlbumId; // 오디오 앨범아트 ID
        public String mTitle; // 타이틀 정보
        public String mArtist; // 아티스트 정보
        public String mAlbum; // 앨범 정보
        public long mDuration; // 재생시간
        public String mDataPath; // 실제 데이터위치
        public Bitmap mBitmap;

        public static VideoItem bindCursor(Cursor cursor) {
            // 개별 item, cursor
            VideoItem videoItem = new VideoItem();
            //  Audio Image -----
            int column_index = cursor.getColumnIndexOrThrow(MediaStore.Video.Media.DATA);
            String pathId = cursor.getString(column_index);
            if(pathId == null) return null;
            videoItem.mBitmap = ThumbnailUtils.createVideoThumbnail(pathId, MediaStore.Video.Thumbnails.MINI_KIND);
            //-------------------
            videoItem.mId = cursor.getLong(cursor.getColumnIndex(MediaStore.Video.VideoColumns._ID));
            videoItem.mTitle = cursor.getString(cursor.getColumnIndex(MediaStore.Video.VideoColumns.TITLE));
            videoItem.mArtist = cursor.getString(cursor.getColumnIndex(MediaStore.Video.VideoColumns.ARTIST));
            videoItem.mAlbum = cursor.getString(cursor.getColumnIndex(MediaStore.Video.VideoColumns.ALBUM));
            videoItem.mDuration = cursor.getLong(cursor.getColumnIndex(MediaStore.Video.VideoColumns.SIZE));

            String path = cursor.getString(cursor.getColumnIndex(MediaStore.Video.VideoColumns.DATA));
            String[] pathArr = path.split("/");
            videoItem.mDataPath = pathArr[pathArr.length-1];

            Log.d(TAG, "Title:" + videoItem.mTitle + ", Artist:" + videoItem.mArtist);
            return videoItem;
        }

        public static VideoItem bindRetriever(MediaMetadataRetriever retriever, Uri uri) {
            // 개별 item, retriever
            VideoItem videoItem = new VideoItem();
//            audioItem.mId = cursor.getLong(cursor.getColumnIndex(MediaStore.Audio.AudioColumns._ID));
//            audioItem.mAlbumId = cursor.getLong(cursor.getColumnIndex(MediaStore.Audio.AudioColumns.ALBUM_ID));
            videoItem.mTitle = retriever.extractMetadata(MediaMetadataRetriever.METADATA_KEY_TITLE);
            videoItem.mArtist = retriever.extractMetadata(MediaMetadataRetriever.METADATA_KEY_ARTIST);
            videoItem.mAlbum = retriever.extractMetadata(MediaMetadataRetriever.METADATA_KEY_ALBUM);
            videoItem.mDuration = Long.parseLong(retriever.extractMetadata(MediaMetadataRetriever.METADATA_KEY_DURATION));
//            String duration = retriever.extractMetadata((MediaMetadataRetriever.METADATA_KEY_DURATION));
//            audioItem.mDataPath = cursor.getString(cursor.getColumnIndex(MediaStore.Audio.AudioColumns.DATA));
//          오디오 앨범 자켓 이미지
            videoItem.mBitmap = retriever.getFrameAtTime(0, MediaMetadataRetriever.OPTION_CLOSEST);
            Log.d(TAG, "Title:" + videoItem.mTitle + ", Album:" + videoItem.mAlbum);
            return videoItem;
        }
    }

}
