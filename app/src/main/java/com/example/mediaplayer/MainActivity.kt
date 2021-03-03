package com.example.mediaplayer

import android.Manifest
import android.content.*
import android.os.Build
import android.os.Bundle
import android.os.StrictMode
import android.util.Log
import android.view.*
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.databinding.DataBindingUtil
import com.example.mediaplayer.databinding.ActivityMainBinding
import com.gun0912.tedpermission.PermissionListener
import com.gun0912.tedpermission.TedPermission

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
//        sendBroadcast(new Intent(BroadcastActions.PREPARED, // Widget쪽 // action
//                Uri.EMPTY,                                              // data
//                getApplicationContext(),                                // context
//                CaptureWidgetProvider.class));                          //class
//        --  Broadcast receiver 쪽 (Manifests에 receiver 정의하는 방법과 비교)
//            private BroadcastReceiver mBroadcastReceiver = new BroadcastReceiver() {
//                @Override
//                public void onReceive(Context context, Intent intent) {
//                        updateUI();
//                }
//            };
//            protected void onCreate(Bundle savedInstanceState){
//                registerBroadcast();
//            }
//            protected void onDestroy() {
//                unregisterBroadcast();
//            }
//            private void registerBroadcast() {
//                IntentFilter filter = new IntentFilter();
//                filter.addAction(BroadcastActions.PREPARED);
//                filter.addAction(BroadcastActions.PLAY_STATE_CHANGED);
//                registerReceiver(mBroadcastReceiver, filter);
//            }
//
//            private void unregisterBroadcast() {
//                unregisterReceiver(mBroadcastReceiver);
//            }
// -----------------------------------------------------------------------------------
// Adapter 사용법  -------------------------------------------------------------------
//  VideoPlayer는 CursorRecyclerViewAdapter - cursor - Array 사용안함
//  PhotoPlayer는 RecyclerViewAdapter - cursor - photoList Array(PhotoItem) 사용
//                                  interface onItemClickListener 사용
//  MusicPlayer는 RecyclerViewAdapter - retriever - songList Array (Uri) 사용
//                                  interface onItemClickListener 사용
// -----------------------------------------------------------------------------------
class MainActivity : AppCompatActivity(), View.OnClickListener {
    var getInstance: MainActivity? = null

    private lateinit var mainBinding: ActivityMainBinding

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        mainBinding = DataBindingUtil.setContentView(this, R.layout.activity_main)
        Foreground.init(application) // Foreground class, Background 체크
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            TedPermission.with(this)
                    .setPermissionListener(object : PermissionListener {
                        override fun onPermissionGranted() {
                            // 권한 요청 성공
                            Log.d(TAG, "권한 설정 완료") //PERMISSION_GRANTED
                            initView() ////
                        }

                        override fun onPermissionDenied(deniedPermissions: List<String>) {
                            // 권한 요청 실패
                            Log.d(TAG, "권한 요청 실패$deniedPermissions") //PERMISSION_Denied
                            Toast.makeText(this@MainActivity, "권한 허용을 하지 않으면 서비스를 이용할 수 없습니다.\n" +
                                    deniedPermissions.toString(), Toast.LENGTH_SHORT).show()
                        }
                    })
                    .setRationaleMessage(getResources().getString(R.string.permission_2))
                    .setDeniedMessage(getResources().getString(R.string.permission_1))
                    .setPermissions(Manifest.permission.WRITE_EXTERNAL_STORAGE,
                            Manifest.permission.READ_EXTERNAL_STORAGE,
                            Manifest.permission.CAMERA)
                    .check()
        } else {
            initView() /////
        }
    }

    private fun initView() {
        getInstance = this
        val context: Context = applicationContext

// 카메라 객체를 R.layout.activity_main의 레이아웃에 선언한 SurfaceView에서 먼저 정의해야 함으로 setContentView 보다 먼저 정의한다.
//        camera = Camera.open();
//        setContentView(R.layout.activity_main)

//----------------------------------------------------------------------------
// No Network Security Config specified, using platform default - Android Log
        val policy: StrictMode.ThreadPolicy = StrictMode.ThreadPolicy.Builder().permitAll().build()
        StrictMode.setThreadPolicy(policy)
        //----------------------------------------------------------------------------
//        findViewById<View>(R.id.camera_photo).setOnClickListener(this)
//        findViewById<View>(R.id.gallery).setOnClickListener(this)
//        findViewById<View>(R.id.record).setOnClickListener(this)
//        findViewById<View>(R.id.music).setOnClickListener(this)
//        findViewById<View>(R.id.video).setOnClickListener(this)

        mainBinding.cameraPhoto.setOnClickListener(this)
        mainBinding.gallery.setOnClickListener(this)
        mainBinding.record.setOnClickListener(this)
        mainBinding.music.setOnClickListener(this)
        mainBinding.video.setOnClickListener(this)
    }

    override fun onClick(v: View) {
        when (v.id) {
            R.id.camera_photo -> {
                val cameraIntent = Intent(applicationContext, CameraActivity::class.java)
                startActivity(cameraIntent)
            }
            R.id.gallery -> {
                val galleryIntent = Intent(applicationContext, PhotoGalleryActivity::class.java)
                startActivity(galleryIntent)
            }
            R.id.record -> {
                val recordIntent = Intent(applicationContext, AudioRecordActivity::class.java)
                startActivity(recordIntent)
            }
            R.id.music -> {
                val musicIntent = Intent(applicationContext, MusicPlayerActivity::class.java)
                startActivity(musicIntent)
            }
            R.id.video -> {
                val videoIntent = Intent(applicationContext, VideoPlayerActivity::class.java)
                startActivity(videoIntent)
            }
        }
    }

    companion object {
        private const val TAG: String = "권한 "
    }
}