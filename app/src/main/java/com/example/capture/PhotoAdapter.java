package com.example.capture;

import android.content.ContentUris;
import android.content.Context;
import android.content.res.Resources;
import android.database.Cursor;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.media.MediaMetadataRetriever;
import android.net.Uri;
import android.os.Parcel;
import android.os.Parcelable;
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

import java.io.Serializable;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.Locale;

public class PhotoAdapter extends RecyclerView.Adapter<PhotoAdapter.PhotoViewHolder> {
    public interface onItemClickListener{
        void onItemClicked(PhotoItem item, View view);
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
                    mListener.onItemClicked(item, v);
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
        holder.mTxtTitle.setText(item.mDATE_MODIFIED);
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

    public static class PhotoViewHolder extends RecyclerView.ViewHolder {
        public ImageView mImageView;
        public RelativeLayout layoutSelect;
        private TextView mTxtTitle;

        // TODO: 2020-07-16
        // viewholder 재사용

        public PhotoViewHolder(@NonNull View itemView) {
            super(itemView);

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

        projection = new String[]{
                MediaStore.MediaColumns.DATA,
                MediaStore.MediaColumns.DATE_ADDED,
                MediaStore.MediaColumns.DISPLAY_NAME};
        uri = MediaStore.Images.Media.EXTERNAL_CONTENT_URI;
//      projection = new String[]{MediaStore.Video.Media._ID, MediaStore.Video.Media.DISPLAY_NAME, MediaStore.Video.Media.DATA};

        Cursor cursor = mContext.getContentResolver()
                .query(MediaStore.Images.Media.EXTERNAL_CONTENT_URI,   // uri ?? 위와 비교
                null, null, null, MediaStore.MediaColumns.DATE_MODIFIED + " desc");
        int columnIndex = cursor.getColumnIndexOrThrow(MediaStore.MediaColumns.DATA);
//        int dateIndex = cursor.getColumnIndexOrThrow(MediaStore.MediaColumns.DATE_ADDED);

        while (cursor.moveToNext())
        {
            String absolutePathOfImage = cursor.getString(columnIndex);

            long Sdate = cursor.getLong(cursor.getColumnIndexOrThrow(MediaStore.Images.Media.DATE_TAKEN));
            String mDate;
//            if( Sdate != 0 ) {    // DATE_TAKEN 이 없는 경우 1970.0.0 ?
                mDate = new SimpleDateFormat("yyyy.MM.dd", Locale.KOREA).format(new Date(Sdate));
//            } else {
//                mDate = "Title";
//            }
            uri = ContentUris.withAppendedId(MediaStore.Images.Media.EXTERNAL_CONTENT_URI,
                    cursor.getLong(cursor.getColumnIndexOrThrow(BaseColumns._ID)));

            PhotoItem photoItem = new PhotoItem(absolutePathOfImage, mDate, false, uri);
            photoItem.mDISPLAY_NAME = cursor.getString(cursor.getColumnIndexOrThrow(MediaStore.Images.Media.DISPLAY_NAME));
            photoItem.mCONTENT_TYPE = cursor.getString(cursor.getColumnIndexOrThrow(MediaStore.Images.Media.MIME_TYPE));
            photoItem.mDATE_MODIFIED = new SimpleDateFormat("yyyy.MM.dd", Locale.KOREA)
                .format(new Date(cursor.getLong(cursor.getColumnIndexOrThrow(MediaStore.Images.Media.DATE_MODIFIED))*1000));
            photoItem.mRELATIVE_PATH = cursor.getString(cursor.getColumnIndexOrThrow(MediaStore.Images.Media.RELATIVE_PATH));
            photoItem.mDATA = cursor.getString(cursor.getColumnIndexOrThrow(MediaStore.Images.Media.DATA));

            photoList.add(photoItem);
//            Log.i(TAG, "Path : " + uri + mDate + "*" + Sdate);

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

    public static class PhotoItem implements Serializable, Parcelable {
        public String imgPath;
        public String mDate;
        private boolean selected;
        public Uri mUri;

        public String mDISPLAY_NAME;
        public String mCONTENT_TYPE;
        public String mDATE_MODIFIED;
        public String mRELATIVE_PATH;
        public String mDATA;


        public PhotoItem(){
        }

        public PhotoItem(String imgPath, String date, boolean selected, Uri uri) {
            this.imgPath = imgPath;
            this.mDate = date;
            this.selected = selected;
            this.mUri = uri;
        }

// Parcelable 로 만들기 -----
        protected PhotoItem(Parcel in) {
            imgPath = in.readString();
            mDate = in.readString();
            selected = in.readByte() != 0;
            mUri = in.readParcelable(Uri.class.getClassLoader());
            mDISPLAY_NAME = in.readString();
            mCONTENT_TYPE = in.readString();
            mDATE_MODIFIED = in.readString();
            mRELATIVE_PATH = in.readString();
            mDATA = in.readString();
        }

        public static final Creator<PhotoItem> CREATOR = new Creator<PhotoItem>() {
            @Override
            public PhotoItem createFromParcel(Parcel in) {
                return new PhotoItem(in);
            }

            @Override
            public PhotoItem[] newArray(int size) {
                return new PhotoItem[size];
            }
        };
// Parcelable 로 만들기 -----

        public String getImgPath() {
            return imgPath;
        }

        public Uri getUri() {
            return mUri;
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

// Parcelable 로 만들기 -----
        @Override
        public int describeContents() {
            return 0;
        }

        @Override
        public void writeToParcel(Parcel dest, int flags) {
            dest.writeString(imgPath);
            dest.writeString(mDate);
            dest.writeByte((byte) (selected ? 1 : 0));
            dest.writeParcelable(mUri, flags);
            dest.writeString(mDISPLAY_NAME);
            dest.writeString(mCONTENT_TYPE);
            dest.writeString(mDATE_MODIFIED);
            dest.writeString(mRELATIVE_PATH);
            dest.writeString(mDATA);
        }
// Parcelable 로 만들기 -----

    }
}
