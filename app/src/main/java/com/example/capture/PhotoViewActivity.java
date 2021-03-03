package com.example.capture;

import android.content.ContentResolver;
import android.content.ContentUris;
import android.content.ContentValues;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.database.Cursor;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.os.ParcelFileDescriptor;
import android.provider.MediaStore;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.MotionEvent;
import android.view.View;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.LinearLayout;

import androidx.annotation.Nullable;
import androidx.annotation.RequiresApi;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;

import com.bumptech.glide.Glide;
import com.github.chrisbanes.photoview.PhotoView;

import java.io.File;
import java.io.FileOutputStream;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Locale;

public class PhotoViewActivity extends AppCompatActivity implements View.OnClickListener {
    private Context mContext;
    private PhotoAdapter.PhotoItem mItem;
    private String mPath;
    private ImageView mImageView;

    @Override
    public void onEnterAnimationComplete() {
        super.onEnterAnimationComplete();
        setInit();
    }

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_photoview);

        mContext = this;
        getData();
    }

    private void getData(){
        Intent intent = getIntent();
        mPath = intent.getStringExtra("path");
        mItem = (PhotoAdapter.PhotoItem) intent.getExtras().get("photoItem");
    }

    private void setInit(){
        PhotoView mImageView = (PhotoView) findViewById(R.id.photoview);
        Glide.with(mContext).load(mPath).into(mImageView);

        mImageView.setOnLongClickListener(new View.OnLongClickListener() {
            @Override
            public boolean onLongClick(View v) {
                Log.d("사진사건", "onLongClick");
//                PhotoGalleryActivity.dataModifyDialog()  ==> eventBus로도?
                editDialog(mItem);
                return false;
            }
        });

        mImageView.setOnGenericMotionListener(new View.OnGenericMotionListener() {
            @Override
            public boolean onGenericMotion(View v, MotionEvent event) {
                Log.d("사진사건", "onGenericMotion");
                return false;
            }
        });
    }

    private void editDialog(final PhotoAdapter.PhotoItem item){
        LayoutInflater vi = (LayoutInflater) getSystemService(Context.LAYOUT_INFLATER_SERVICE);
//        LinearLayout editLayout = (LinearLayout) vi.inflate(R.layout.edit_dialog, null);
        LinearLayout editLayout = (LinearLayout) vi.inflate(R.layout.edit_dialog, null);

        final EditText displayname = editLayout.findViewById(R.id.edit_displayname);
        final EditText contenttype = editLayout.findViewById(R.id.edit_contenttype);
        final EditText modified = editLayout.findViewById(R.id.edit_modified);
        final EditText relative = editLayout.findViewById(R.id.edit_relative);
        final EditText pathdata = editLayout.findViewById(R.id.edit_pathdata);
        final EditText datetaken = editLayout.findViewById(R.id.edit_datetaken);

        displayname.setText(item.mDISPLAY_NAME);
        contenttype.setText(item.mCONTENT_TYPE);
        modified.setText(item.mDATE_MODIFIED);
        relative.setText(item.mRELATIVE_PATH);
        pathdata.setText(item.mDATA);
        datetaken.setText(item.mDate);

        final PhotoAdapter.PhotoItem newItem = item;
        new AlertDialog.Builder(this)
                .setTitle("사진정보")
                .setView(editLayout)
                .setNeutralButton("확인", new DialogInterface.OnClickListener() {
                    @RequiresApi(api = Build.VERSION_CODES.Q)    // ***** contentresolver 수정 안되는 부분
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        newItem.mDISPLAY_NAME = displayname.getText().toString();
                        newItem.mCONTENT_TYPE = contenttype.getText().toString();
                        newItem.mDATE_MODIFIED = modified.getText().toString();
                        newItem.mRELATIVE_PATH = relative.getText().toString();
                        newItem.mDATA = pathdata.getText().toString();
                        newItem.mDate = datetaken.getText().toString();
////  사진자료 수정, newItem 자료를
                        ContentValues values = new ContentValues();
                        values.put(MediaStore.Images.Media.IS_PENDING, 1);
                        values.put(MediaStore.Images.Media.DISPLAY_NAME, newItem.mDISPLAY_NAME);
                        values.put(MediaStore.Images.Media.MIME_TYPE, newItem.mCONTENT_TYPE);
                        values.put(MediaStore.Images.Media.RELATIVE_PATH, newItem.mRELATIVE_PATH);
                        try {
                            Date dateModify = new SimpleDateFormat("yyyy.MM.dd", Locale.KOREA).parse(newItem.mDATE_MODIFIED);
                            values.put(MediaStore.Images.Media.DATE_MODIFIED, dateModify.getTime()/1000);
                        } catch (ParseException e) {
                            values.put(MediaStore.Images.Media.DATE_MODIFIED, System.currentTimeMillis());
                            e.printStackTrace();
                        }

                        ContentResolver resolver = mContext.getContentResolver();
                                                                    // RELATIVE_PATH/DISPLAY_NAME --> real path(DATA)
                        Uri uri = getUriFromPath(newItem.mDATA);    // ** DISPLAY_NAME 에 따라 자동 바뀜 **
                                                                    // real path 계산 routine 필요
                        resolver.update(uri, values, null, null);  // 수정 안됨 !!!!!!!

                        try {
                            Date dateTaken = new SimpleDateFormat("yyyy.MM.dd", Locale.KOREA).parse(newItem.mDate);
                            values.put(MediaStore.Images.Media.DATE_TAKEN, dateTaken.getTime());
                        } catch (ParseException e) {
                            values.put(MediaStore.Images.Media.DATE_TAKEN, System.currentTimeMillis());
//                            e.printStackTrace();
                        }

                        values.put(MediaStore.Images.Media.IS_PENDING, 0);
                        resolver.update(uri, values, null, null);  // 수정 안됨 !!!!!!!

                        Log.e("사진사건 수정", uri.toString() + "\n" + newItem.mDATA
                        + "\n" + values.getAsString(MediaStore.Images.Media.DATE_TAKEN));

//                        ParcelFileDescriptor pfd = resolver.openFileDescriptor(uri,"w", null);
//                        FileOutputStream outStream = new FileOutputStream(pfd.getFileDescriptor());
//                        outStream.write(data);
//                        outStream.close();
//                        pfd.close();


////   등을 이용하여 MediaStore 자료 수정
                    }
                }).show();
    }

    @Override
    public void onClick(View v) {

    }

    public Uri getUriFromPath(String filePath) {
        Cursor cursor = getContentResolver().query(MediaStore.Images.Media.EXTERNAL_CONTENT_URI,
                null, "_data = '" + filePath + "'", null, null);

        cursor.moveToNext();
        int id = cursor.getInt(cursor.getColumnIndex("_id"));
        Uri uri = ContentUris.withAppendedId(MediaStore.Images.Media.EXTERNAL_CONTENT_URI, id);

        return uri;
    }

    public String getRealPathFromURI(Uri contentUri) {

        String[] proj = { MediaStore.Images.Media.DATA };

        Cursor cursor = getContentResolver().query(contentUri, proj, null, null, null);
        cursor.moveToNext();
        String path = cursor.getString(cursor.getColumnIndex(MediaStore.MediaColumns.DATA));
        Uri uri = Uri.fromFile(new File(path));

        cursor.close();
        return path;
    }
}
