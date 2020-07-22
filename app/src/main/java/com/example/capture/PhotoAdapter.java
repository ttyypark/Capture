package com.example.capture;

import android.content.ContentUris;
import android.content.Context;
import android.content.res.Resources;
import android.database.Cursor;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.media.MediaMetadataRetriever;
import android.net.Uri;
import android.provider.BaseColumns;
import android.provider.MediaStore;
import android.text.TextUtils;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.RelativeLayout;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.bumptech.glide.Glide;

import java.util.ArrayList;

public class PhotoAdapter extends RecyclerView.Adapter<PhotoAdapter.PhotoViewHolder> {
    public interface onItemClickListener{
        void onItemClicked(PhotoItem item);
    }
    private onItemClickListener mListener;
    private String TAG = "포토어댑터";

    private Context mContext;
    private int itemLayout;
    public ArrayList<PhotoItem> mPhotoList;


    // 리스너 객체 전달함수
    public void setOnItemClickListener(onItemClickListener listener){
        this.mListener = listener;
    }

    public PhotoAdapter(onItemClickListener mListener) {
        this.mListener = mListener;
    }

    /**
     * 생성자
     * @param itemLayout        item layout
     */
    public PhotoAdapter(Context context, int itemLayout) {
        this.mContext = context;
        if(mPhotoList == null) this.mPhotoList = getPhotoList();
        this.itemLayout = itemLayout;
    }

    public void setmPhotoLists(ArrayList<PhotoItem> photoList){
        this.mPhotoList = photoList;
        notifyDataSetChanged();
    }

    public ArrayList<PhotoItem> getmPhotoList() {
        return mPhotoList;
    }

    public PhotoItem getItem(int position){
        return mPhotoList.get(position);
    }

    @Override
    public int getItemCount() {
        return mPhotoList.size();
    }

    @NonNull
    @Override
    public PhotoViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext())
                .inflate(itemLayout, parent, false);
//                .inflate(R.layout.photo_item, parent, false);
        final PhotoViewHolder viewHolder = new PhotoViewHolder(view);
            view.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    if(mListener != null){
                        final PhotoItem item = mPhotoList.get(viewHolder.getAdapterPosition());
                        mListener.onItemClicked(item);
                    }
                }
            });
        return viewHolder;
    }

    @Override
    public void onBindViewHolder(@NonNull PhotoViewHolder holder, int position) {
        Log.i(TAG, "BindViewHolder : " + position + "");
//        final PhotoItem item = mItems.get(position);
//        holder.setPhotoItem(item);

        PhotoItem item = mPhotoList.get(position);

        Glide.with(mContext)
                .load(item.getImgPath())
                .centerCrop()
//                .crossFade()
                .into(holder.mImageView);
        holder.mTxtTitle.setText(R.string.tempTitle);

//        final MediaMetadataRetriever retriever;
//        try {
//            retriever = new MediaMetadataRetriever();
//            retriever.setDataSource(mContext, mUri);
//            PhotoItem photoItem = PhotoItem.bindRetriever(retriever);
//            holder.setPhotoItem(photoItem);
//        } catch (Exception e) {
//            Log.d(TAG, "Retriever 오류! ");
//            e.printStackTrace();
//        }



//        holder.itemView.setOnClickListener(new View.OnClickListener() {
//            @Override
//            public void onClick(View v) {
//                if(mListener != null){
////                    final PhotoItem item = mItems.get(holder.getAdapterPosition());
//                    mListener.onItemClicked(item);
//                }
//            }
//        });

    }

    public static class PhotoViewHolder extends RecyclerView.ViewHolder {
        public ImageView mImageView;
        public RelativeLayout layoutSelect;
        private TextView mTxtTitle;

//            private View mView;
//            private int mPosition;
//            private onItemClickListener listener;

        // TODO: 2020-07-16
        // viewholder 재사용

        public PhotoViewHolder(@NonNull View itemView) {
            super(itemView);

//                mView = itemView;
            mImageView = itemView.findViewById(R.id.imagephotoView);
            mTxtTitle = itemView.findViewById(R.id.txt_title);

//            itemView.setOnClickListener(new View.OnClickListener() {
//                @Override
//                public void onClick(View v) {
//                    int position = getAdapterPosition();
//                    if (mListener != null){
//                        mListener.onItemClicked(mItems.get(position));
//                    }
//                }
//            });
        }

////        public void setPhotoItem(PhotoItem item){
//        public void setPhotoItem(PhotoItem item){
////                mPosition = Position;
//            if(item.mBitmap != null) {
//                mImageView.setImageBitmap(item.mBitmap);
//            } else {
//                mImageView.setImageResource(R.drawable.snow);
//            }
//        }
    }


    public ArrayList<PhotoItem> getPhotoList() {
        ArrayList<PhotoItem> photoList = new ArrayList<>();
        Uri uri;
        String[] projection;

//        uri = android.provider.MediaStore.Images.Media.EXTERNAL_CONTENT_URI;
        projection = new String[]{
                MediaStore.MediaColumns.DATA,
                MediaStore.MediaColumns.DATE_ADDED,
                MediaStore.MediaColumns.DISPLAY_NAME};
//      uri = MediaStore.Video.Media.EXTERNAL_CONTENT_URI;
//      projection = new String[]{MediaStore.Video.Media._ID, MediaStore.Video.Media.DISPLAY_NAME, MediaStore.Video.Media.DATA};

        Cursor cursor = mContext.getContentResolver()
                .query(MediaStore.Images.Media.EXTERNAL_CONTENT_URI,   // uri ?? 위와 비교
                null, null, null, MediaStore.MediaColumns.DATE_ADDED + " desc");
        int columnIndex = cursor.getColumnIndexOrThrow(MediaStore.MediaColumns.DATA);

        while (cursor.moveToNext())
        {
//            uri = ContentUris.withAppendedId(MediaStore.Images.Media.EXTERNAL_CONTENT_URI,
//                    cursor.getLong(
//                    cursor.getColumnIndexOrThrow(BaseColumns._ID)));
//            photoList.add(uri);

            String absolutePathOfImage = cursor.getString(columnIndex);
            PhotoItem photoItem = new PhotoItem(absolutePathOfImage, false);
            photoList.add(photoItem);
            Log.i(TAG, "Path : " + absolutePathOfImage + "");

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
        cursor.close();
        return photoList;
    }

    public static class PhotoItem {
        public String imgPath;
        private boolean selected;
        //        public Bitmap mBitmap;

        public PhotoItem(String imgPath, boolean selected) {
            this.imgPath = imgPath;
            this.selected = selected;
        }

        public String getImgPath() {
            return imgPath;
        }

        public void setImgPath(String imgPath) {
            this.imgPath = imgPath;
        }

        public boolean isSelected() {
            return selected;
        }

        public void setSelected(boolean selected) {
            this.selected = selected;
        }

//        public static PhotoItem bindRetriever(MediaMetadataRetriever retriever) {
//
//            Log.e("포토 바인드", "bindRetriever" + "");
//
//            PhotoItem photoItem = new PhotoItem();
//            Bitmap bitmap;
//            byte[] albumImage = retriever.getEmbeddedPicture();
//
//            if(albumImage == null) {
//                bitmap = BitmapFactory.decodeResource(Resources.getSystem(),  R.drawable.snow);
//            } else {
//                bitmap = BitmapFactory.decodeByteArray(albumImage, 0, albumImage.length);
//            }
//
//            photoItem.mBitmap = bitmap;
//            return photoItem;
//        }
    }
}
