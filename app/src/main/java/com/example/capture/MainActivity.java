package com.example.capture;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.content.FileProvider;

import android.Manifest;
import android.app.Activity;
import android.content.ContentProvider;
import android.content.ContentResolver;
import android.content.ContentUris;
import android.content.ContentValues;
import android.content.Context;
import android.content.Intent;
import android.database.Cursor;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.hardware.Camera;
import android.media.AudioAttributes;
import android.media.AudioManager;
import android.media.MediaPlayer;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.os.Environment;
import android.os.StrictMode;
import android.provider.BaseColumns;
import android.provider.MediaStore;
import android.util.Log;
import android.view.SurfaceHolder;
import android.view.SurfaceView;
import android.view.View;
import android.widget.ImageView;
import android.widget.Toast;

import com.gun0912.tedpermission.PermissionListener;
import com.gun0912.tedpermission.TedPermission;

import java.io.File;
import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.List;
import java.util.Locale;

//import static android.os.Environment.getExternalStoragePublicDirectory;

public class MainActivity extends AppCompatActivity implements View.OnClickListener {

    private static final String TAG = "권한 ";
    private Context context;
    public MainActivity getInstance;

//    Music play
    private int position = 0;
    MediaPlayer player;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        Foreground.init(this.getApplication()); // Foreground class, Background 체크

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            TedPermission.with(this)
                    .setPermissionListener(new PermissionListener() {
                        @Override
                        public void onPermissionGranted() {
                            // 권한 요청 성공
                            Log.d(TAG, "권한 설정 완료"); //PERMISSION_GRANTED
                            initView(); ////
                        }

                        @Override
                        public void onPermissionDenied(List<String> deniedPermissions) {
                            // 권한 요청 실패
                            Log.d(TAG, "권한 요청 실패" + deniedPermissions.toString()); //PERMISSION_GRANTED
                            Toast.makeText(MainActivity.this, "권한 허용을 하지 않으면 서비스를 이용할 수 없습니다.\n" +
                                    deniedPermissions.toString(), Toast.LENGTH_SHORT).show();
                        }
                    })
                    .setRationaleMessage(getResources().getString(R.string.permission_2))
                    .setDeniedMessage(getResources().getString(R.string.permission_1))
                    .setPermissions(Manifest.permission.WRITE_EXTERNAL_STORAGE,
                            Manifest.permission.READ_EXTERNAL_STORAGE,
                            Manifest.permission.CAMERA)
                    .check();
        } else {
            initView();  /////
        }

    }

    private void initView(){
        getInstance = this;
        context = getApplicationContext();

        // 카메라 객체를 R.layout.activity_main의 레이아웃에 선언한 SurfaceView에서 먼저 정의해야 함으로 setContentView 보다 먼저 정의한다.
//        camera = Camera.open();

        setContentView(R.layout.activity_main);

//----------------------------------------------------------------------------
// No Network Security Config specified, using platform default - Android Log
        if (android.os.Build.VERSION.SDK_INT > 9)
        {
            StrictMode.ThreadPolicy policy = new
                    StrictMode.ThreadPolicy.Builder().permitAll().build();
            StrictMode.setThreadPolicy(policy);
        }
//----------------------------------------------------------------------------
        findViewById(R.id.camera_photo).setOnClickListener(this);
        findViewById((R.id.music)).setOnClickListener(this);
        findViewById((R.id.video)).setOnClickListener(this);
        findViewById((R.id.music1)).setOnClickListener(this);
        findViewById((R.id.music2)).setOnClickListener(this);
        findViewById((R.id.music3)).setOnClickListener(this);
        findViewById((R.id.music4)).setOnClickListener(this);

    }

    @Override
    public void onClick(View v) {
        switch (v.getId()) {
            case R.id.camera_photo: // 사진찍기
                Intent cameraIntent = new Intent(getApplicationContext(), CameraPhoto.class);
                startActivity(cameraIntent);
                break;
            case R.id.music: // Music play
                Intent musicIntent = new Intent(getApplicationContext(), MusicPlayerActivity.class);
                startActivity(musicIntent);
                break;
            case R.id.video: // Video play
                Intent videoIntent = new Intent(getApplicationContext(), VideoPlayerActivity.class);
                startActivity(videoIntent);
                break;
            case R.id.music1: //재생
                playAudio();
                break;
            case R.id.music2: //일시정지
                pauseAudio();
                break;
            case R.id.music3: //재시작
                resumeAudio();
                break;
            case R.id.music4: //정지
                stopAudio();
                break;

        }
    }

    public long getID(Cursor cursor) {
        long thisId = 0;
        String thisTitle = "";

//        ContentResolver contentResolver = getContentResolver();
//        Uri uri = MediaStore.Audio.Media.EXTERNAL_CONTENT_URI;
////        Uri uri = android.provider.MediaStore.Audio.Media.EXTERNAL_CONTENT_URI;
//        // androidx.core.content.FileProvider
//        Cursor cursor = contentResolver.query(uri, null, null, null, null);
////        Cursor cursor = contentResolver.query(MyProvider.CONTENT_URI, null, null, null, null);
        if (cursor == null) {
            // 쿼리 실패, 에러 처리
            return 0;
        } else if (!cursor.moveToFirst()) {
            // 미디어가 장치에 없음
            return 0;
        } else {
            int titleColumn = cursor.getColumnIndex(android.provider.MediaStore.Audio.Media.TITLE);
            int idColumn = cursor.getColumnIndex(android.provider.MediaStore.Audio.Media._ID);
            do {
                thisId = cursor.getLong(idColumn);
                thisTitle = cursor.getString(titleColumn);
                // 목록을 처리
            } while (cursor.moveToNext());
            return thisId;
        }
    }
//========================================================================
    public void playAudio(){

        killPlayer();

// 로컬의 resource의 자체음원 사용시
//            player = MediaPlayer.create(context, R.raw.music1);  // prepare() 까지 필요없음
//            player.start();
//        Toast.makeText(this, "재생 시작됨", Toast.LENGTH_LONG).show();

        try {
////  URI를 통해서 로컬 시스템의 음원 사용시
//            ContentResolver contentResolver = getContentResolver();
//            Cursor cursor = contentResolver.query(MediaStore.Audio.Media.EXTERNAL_CONTENT_URI,
//                    null, null, null, null);
//            long id = getID(cursor);
//            cursor.moveToFirst();  // ***
////            Uri myUri = ContentUris.withAppendedId(MediaStore.Audio.Media.EXTERNAL_CONTENT_URI, id);
//
//            final Uri myUri = ContentUris.withAppendedId(MediaStore.Audio.Media.EXTERNAL_CONTENT_URI,
//                    cursor.getLong(
//                    cursor.getColumnIndexOrThrow(BaseColumns._ID)));
//
//            player = new MediaPlayer();
//            player.setAudioStreamType(AudioManager.STREAM_MUSIC);
//            player.setDataSource(getApplicationContext(), myUri);

// HTTP스트리밍을 통한 원격의 URL에서 재생을 하는 경우
            String url = "https://sites.google.com/site/ubiaccessmobile/sample_audio.amr";
            player = new MediaPlayer();
            player.setAudioStreamType(AudioManager.STREAM_MUSIC);
            player.setDataSource(getApplicationContext(), Uri.parse(url));

            player.prepare();
            player.start();
            Toast.makeText(this, "재생 시작됨", Toast.LENGTH_LONG).show();
            Log.d("음악", "재생 시작됨");

        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public void pauseAudio(){
        if(player != null) {
            position = player.getCurrentPosition();
            player.pause();
            Toast.makeText(this, "일시 정지됨", Toast.LENGTH_LONG).show();
            Log.d("음악", "일시 정지됨");
        }
    }

    public void resumeAudio(){
        if(player != null && !player.isPlaying()) {
            player.seekTo(position);
            player.start();
            Toast.makeText(this, "재시작됨", Toast.LENGTH_LONG).show();
            Log.d("음악", "재시작됨");
        }
    }

    public void stopAudio(){
        if(player != null && player.isPlaying()) {
            player.stop();
            killPlayer();
            Toast.makeText(this, "중지됨", Toast.LENGTH_LONG).show();
            Log.d("음악", "중지됨");
        }
    }

    public void killPlayer(){
        if(player != null){
            player.release();
            player = null;
        }
    }
    @Override
    public void onDestroy() {
        super.onDestroy();
        if (player != null) killPlayer();
    }
}

