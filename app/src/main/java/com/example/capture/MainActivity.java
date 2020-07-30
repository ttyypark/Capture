package com.example.capture;

import androidx.appcompat.app.AppCompatActivity;

import android.Manifest;
import android.content.Context;
import android.content.Intent;
import android.os.Build;
import android.os.Bundle;
import android.os.StrictMode;
import android.util.Log;
import android.view.View;
import android.widget.Toast;

import com.gun0912.tedpermission.PermissionListener;
import com.gun0912.tedpermission.TedPermission;

import java.util.List;

//import static android.os.Environment.getExternalStoragePublicDirectory;

// Data 전달 -------------------------------------------------------------------------
//        FragmentCallback 함수 - VideoPlayerActivity / MusicPlayerActivity의 ViewPager
//                  페이지 전환(setPage) 함수 호출에 사용
//        EventBus.getDefault().post(isPlaying());    // fragment쪽 UI
//               post(isPlaying())  -  Boolean
//               post(uri)  -  Uri
//               post(event)  -  Integer   MusicPlayerActivity.stopPlayer()
//               post(retriver)  -  Retriver
//        updateNotificationPlayer();                 // Notification쪽 UI
//        sendBroadcast(new Intent(BroadcastActions.PREPARED,  // Widget쪽
// -----------------------------------------------------------------------------------

public class MainActivity extends AppCompatActivity implements View.OnClickListener {

    private static final String TAG = "권한 ";
    public MainActivity getInstance;

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
        Context context = getApplicationContext();

// 카메라 객체를 R.layout.activity_main의 레이아웃에 선언한 SurfaceView에서 먼저 정의해야 함으로 setContentView 보다 먼저 정의한다.
//        camera = Camera.open();

        setContentView(R.layout.activity_main);

//----------------------------------------------------------------------------
// No Network Security Config specified, using platform default - Android Log
        StrictMode.ThreadPolicy policy = new
                StrictMode.ThreadPolicy.Builder().permitAll().build();
        StrictMode.setThreadPolicy(policy);
//----------------------------------------------------------------------------
        findViewById(R.id.camera_photo).setOnClickListener(this);
        findViewById(R.id.gallery).setOnClickListener(this);
        findViewById(R.id.record).setOnClickListener(this);
        findViewById(R.id.music).setOnClickListener(this);
        findViewById(R.id.video).setOnClickListener(this);

    }

    @Override
    public void onClick(View v) {
        switch (v.getId()) {
            case R.id.camera_photo: // 사진찍기
                Intent cameraIntent = new Intent(getApplicationContext(), CameraActivity.class);
                startActivity(cameraIntent);
                break;
            case R.id.gallery: // 사진찍기
                Intent galleryIntent = new Intent(getApplicationContext(), PhotoGalleryActivity.class);
                startActivity(galleryIntent);
                break;
            case R.id.record: // Audio Record
                Intent recordIntent = new Intent(getApplicationContext(), AudioRecordActivity.class);
                startActivity(recordIntent);
                break;
            case R.id.music: // Music play
                Intent musicIntent = new Intent(getApplicationContext(), MusicPlayerActivity.class);
                startActivity(musicIntent);
                break;
            case R.id.video: // Video play
                Intent videoIntent = new Intent(getApplicationContext(), VideoPlayerActivity.class);
                startActivity(videoIntent);
                break;
        }
    }
}

