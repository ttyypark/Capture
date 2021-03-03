package com.example.mediaplayer

import android.annotation.SuppressLint
import android.content.Context
import android.content.Intent
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.hardware.Camera
import android.hardware.Camera.CameraInfo
import android.net.Uri
import android.os.Build
import android.os.Bundle
import android.os.Environment
import android.provider.MediaStore
import android.util.Log
import android.view.SurfaceHolder
import android.view.SurfaceView
import android.view.View
import android.widget.ImageView
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.appcompat.widget.Toolbar
import androidx.core.content.FileProvider
import androidx.core.content.FileProvider.getUriForFile
import androidx.databinding.DataBindingUtil
import com.example.mediaplayer.databinding.ActivityCameraBinding
import java.io.File
import java.io.IOException
import java.text.SimpleDateFormat
import java.util.*

class CameraActivity : AppCompatActivity(), View.OnClickListener {
    private var photoFile: File? = null
    private var photoUri: Uri? = null

    //    private CameraPreview mCameraPreview;
    private val surfaceView: SurfaceView? = null
    private val holder: SurfaceHolder? = null
    private var context: Context? = null
    private lateinit var cameraBinding: ActivityCameraBinding

    @Suppress("DEPRECATION")
    companion object {
        private const val REQUEST_GIVEN_IMAGE_CAPTURE: Int = 101
        private const val REQUEST_MY_IMAGE_CAPTURE: Int = 102
        private const val TAG: String = "카메라"
        private const val CAMERA_FACING: Int = CameraInfo.CAMERA_FACING_BACK // Camera.CameraInfo.CAMERA_FACING_FRONT
        private val camera: Camera? = null
        var currentPhotoPath: String? = null

        @SuppressLint("StaticFieldLeak")
        var imageView: ImageView? = null
        @SuppressLint("StaticFieldLeak")
        var getInstance: CameraActivity? = null

        fun getCamera(): Camera? {
            return camera
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
//        setContentView(R.layout.activity_camera)
        cameraBinding = DataBindingUtil.setContentView(this, R.layout.activity_camera)

        getInstance = this
        context = applicationContext
        val toolbar: Toolbar = findViewById(R.id.toolbar)
        setSupportActionBar(toolbar)
        imageView = cameraBinding.imagePhotoView

//        findViewById<View>(R.id.button).setOnClickListener(this)
        cameraBinding.takePicture.setOnClickListener(this)
        cameraBinding.imageCapture.setOnClickListener(this)
        cameraBinding.gotoGallery.setOnClickListener(this)
    }

    override fun onClick(v: View) {
        when (v.id) {
            R.id.take_picture -> takePicture()
            R.id.image_capture -> {
                val intent = Intent(applicationContext, CameraActionActivity::class.java)
                startActivityForResult(intent, REQUEST_MY_IMAGE_CAPTURE)
            }
            R.id.goto_gallery -> {
                val galleryIntent = Intent(applicationContext, PhotoGalleryActivity::class.java)
                startActivity(galleryIntent)
            }
        }
    }

    @Suppress("UNREACHABLE_CODE", "CAST_NEVER_SUCCEEDS")
    @SuppressLint("QueryPermissionsNeeded")
    private fun takePicture() {
        val state: String = Environment.getExternalStorageState()
        if ((Environment.MEDIA_MOUNTED == state)) {
            val intent = Intent(MediaStore.ACTION_IMAGE_CAPTURE)
            // Ensure that there's a camera activity to handle the intent
            if (intent.resolveActivity(packageManager) != null) {
                photoFile = null
                try {
                    photoFile = createImageFile()
                } catch (ex: IOException) {
                    // error
                    Log.v("알림", "이미지 생성 오류! createImagefile.")
                    Toast.makeText(this, "이미지 생성 오류! createImagefile.", Toast.LENGTH_SHORT).show()
                    ex.printStackTrace()
                    //                    finish();
                }
                if (photoFile != null) {
                    photoUri = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.N) {     // 임시화일
                        getUriForFile(this,
                                packageName,  //             "com.example.mycapture.provider",
                                photoFile!!)
                    } else {
                        Uri.fromFile(photoFile)
                    }

                    // content://com.example.mycapture/external_file/Pictures/JPEG_20200208_031050_7007720671382549777.jpg
                    // paths.xml 에 있는 <external-files-path
                    //        name="external_file"
                    //        path="/" /> <!-- Context.getExternalFilesDir() 외부 저장소-->
                    // 내용을 받아옴

//                    imgUri = photoUri;
                    intent.putExtra(MediaStore.EXTRA_OUTPUT, photoUri)

//                    intent.putExtra(MediaStore.EXTRA_OUTPUT, Uri.fromFile(photoFile)); // 추가
                    intent.addFlags(Intent.FLAG_GRANT_WRITE_URI_PERMISSION) // set flag to permission
                    intent.addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION) // set flag to permission
                    grantUriPermission(packageName, photoUri, Intent.FLAG_GRANT_WRITE_URI_PERMISSION or
                            Intent.FLAG_GRANT_READ_URI_PERMISSION)
                    startActivityForResult(intent, REQUEST_GIVEN_IMAGE_CAPTURE)
                }
            }
        } else {
            Log.v("알림", "저장공간에 접근 불가능")
        }
    }

    @Suppress("DEPRECATION")
    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data) // data로 icon 크기 사진 반환
        if (requestCode == REQUEST_GIVEN_IMAGE_CAPTURE && resultCode == RESULT_OK) {
            // 갤러리에 반영
            val mediaScanIntent = Intent(Intent.ACTION_MEDIA_SCANNER_SCAN_FILE)
            mediaScanIntent.data = Uri.fromFile(photoFile)
            //            getContext().sendBroadcast(mediaScanIntent);
            context!!.sendBroadcast(mediaScanIntent)
            Log.d("사진앱", "사진 저장됨 : $currentPhotoPath")
            setImage()

//            // Uri 로 받음
//            imageView.setImageURI(photoUri);

//            // data로 받음. capture()함수 사용시.....
//            Bundle extras = data.getExtras();
//            Bitmap imageBitmap = (Bitmap) extras.get("data");
//            imageView.setImageBitmap(imageBitmap);
        } else if (requestCode == REQUEST_MY_IMAGE_CAPTURE && resultCode == RESULT_OK) {
        }
    }

    private fun setImage() {
        val option: BitmapFactory.Options = BitmapFactory.Options()
        option.inSampleSize = 8 // 1/8로 줄임
        val bitmap: Bitmap = BitmapFactory.decodeFile(currentPhotoPath, option)
        imageView!!.setImageBitmap(bitmap)
    }

    @Suppress("DEPRECATION")
    @Throws(IOException::class)
    fun createImageFile(): File {
        val image: File
        // create an image file name
        val timeStamp: String = SimpleDateFormat("yyMMdd_HHmmss", Locale.KOREA).format(Date())
        val imageFileName: String = "JPEG_$timeStamp"

//        //앱 전용 화일 directory       내부저장소
//        File storageDir = context.getFilesDir();     // OK

//        // 외부저장소 Environment.DIRECTORY_PICTURES  /files/Pictures
//        File storageDir = context.getExternalFilesDir(Environment.DIRECTORY_PICTURES);  // OK

// ===================================================================================
//   Andtoid 10의 외부 저장소는 Scoped Storage이라는 모드가 적용
//   FileNotFoundException이 발생하거나, 권한이 없다는 에러가 발생.
//   manifest 화일의 <application에  아래 내용을 삽입하여 한시적으로 허용
//        android:requestLegacyExternalStorage="true"
// ------------------------------------------------------------------------------
        // 이미지가 저장될 폴더 이름 ( camera )
        val storageDir = File(Environment.getExternalStorageDirectory().toString() + "/camtest")

//        // 특정 폴더 아닌 메모리 최상에 위치
//        File storageDir = Environment.getExternalStorageDirectory();
// ===================================================================================
        if (!storageDir.exists()) {
            val mkdirs: Boolean = storageDir.mkdirs()
            if (!mkdirs) {
                Log.e("Error : ", "storageDir.mkdirs ")
            }
        }
        image = File(storageDir, "$imageFileName.jpg")
        //        image = File.createTempFile(
//                imageFileName, /* prefix */
//                ".jpg",   /* suffix */
//                storageDir);   /* directory */

        // Save a file: path for use with ACTION_VIEW intents
        currentPhotoPath = image.absolutePath

//        image.deleteOnExit();   // 종료와 동시 삭제됨.
        return image
    }

    @Throws(IOException::class)
    fun createFileName(): String {
        val timeStamp: String = SimpleDateFormat("yyMMdd_HHmmss", Locale.KOREA).format(Date())
        return "JPEG_$timeStamp.jpg"
    }

}