package com.example.mediaplayer

import android.content.*
import android.graphics.Bitmap
import android.graphics.Bitmap.CompressFormat
import android.graphics.BitmapFactory
import android.graphics.Matrix
import android.hardware.Camera
import android.hardware.Camera.*
import android.net.Uri
import android.os.AsyncTask
import android.os.Build
import android.os.ParcelFileDescriptor
import android.provider.MediaStore
import android.util.AttributeSet
import android.util.Log
import android.view.*
import androidx.annotation.RequiresApi
import androidx.appcompat.app.AppCompatActivity
import java.io.*

//// camera 예제
// https://webnautes.tistory.com/822
//
class CameraPreview : ViewGroup, SurfaceHolder.Callback {
    private val TAG: String = "CameraPreview"
    private var mContext: Context? = null
    private var mActivity: AppCompatActivity? = null
    private var mCamera: Camera? = null
    private var mSurfaceView: SurfaceView? = null
    private lateinit var mHolder: SurfaceHolder
    private var mCameraID: Int = 0
    private var mCameraInfo: CameraInfo? = null
    private var mDisplayOrientation: Int = 0

    //    private List<Camera.Size> listPreviewSizes;
    private var isPreview: Boolean = false

    constructor(context: Context?) : super(context) {
        mContext = context
    }

    constructor(context: Context?, attrs: AttributeSet?) : super(context, attrs) {}
    constructor(context: Context?, activity: AppCompatActivity?, cameraID: Int, surfaceView: SurfaceView?) : super(context) {
        Log.d("@@@", "Preview 생성")

//        mCamera = MainActivity.getCamera();
//        if (mCamera == null){
//            mCamera = Camera.open();
//        }
        mCamera = open()

//        listPreviewSizes = mCamera.getParameters().getSupportedPreviewSizes();
        mActivity = activity
        mCameraID = cameraID
        mSurfaceView = surfaceView
        mContext = context
        mSurfaceView!!.setVisibility(VISIBLE)
        init()
    }

    private fun init() {
        // SurfaceHolder.Callback를 등록하여 surface의 생성 및 해제 시점을 감지
        mHolder = mSurfaceView!!.holder
        mHolder.addCallback(this)
        mHolder.setType(SurfaceHolder.SURFACE_TYPE_PUSH_BUFFERS) // 카메라가 SurfaceView를 독점
    }

    public override fun surfaceCreated(holder: SurfaceHolder) {
        // Open an instance of the camera
        try {
            if (mCamera == null) {
                mCamera = open(mCameraID) // attempt to get a Camera instance
            }
        } catch (e: Exception) {
            // Camera is not available (in use or does not exist)
            Log.e(TAG, "Camera " + mCameraID + " is not available: " + e.message)
        }

        // retrieve camera's info.
        val cameraInfo: CameraInfo = CameraInfo()
        Camera.getCameraInfo(mCameraID, cameraInfo)
        mCameraInfo = cameraInfo
        mDisplayOrientation = mActivity!!.windowManager.defaultDisplay.rotation
        val orientation: Int = calculatePreviewOrientation(mCameraInfo, mDisplayOrientation)
        mCamera!!.setDisplayOrientation(orientation)

//        listPreviewSizes = mCamera.getParameters().getSupportedPreviewSizes();
        requestLayout()

        // 카메라 설정
        val parameters: Camera.Parameters = mCamera!!.getParameters()
        val focusModes: List<String> = parameters.getSupportedFocusModes()
        if (focusModes.contains(Camera.Parameters.FOCUS_MODE_AUTO)) {
            // set the focus mode
            parameters.setFocusMode(Camera.Parameters.FOCUS_MODE_AUTO)
            // set Camera parameters
            mCamera!!.setParameters(parameters)
        }
        try {
            mCamera!!.setPreviewDisplay(mHolder)
            // Important: Call startPreview() to start updating the preview
            // surface. Preview must be started before you can take a picture.
            mCamera!!.startPreview()
            isPreview = true
            Log.d(TAG, "Camera preview started.")
        } catch (e: IOException) {
            Log.d(TAG, "Error setting camera preview: " + e.message)
        }
    }

    public override fun surfaceChanged(holder: SurfaceHolder, format: Int, width: Int, height: Int) {

        // If your preview can change or rotate, take care of those events here.
        // Make sure to stop the preview before resizing or reformatting it.
        if (mHolder!!.getSurface() == null) {
            // preview surface does not exist
            Log.d(TAG, "Preview surface does not exist")
            return
        }

        // 프리뷰를 다시 설정한다.
        // stop preview before making changes
        try {
            mCamera!!.stopPreview()
            Log.d(TAG, "Preview stopped.")
            val orientation: Int = calculatePreviewOrientation(mCameraInfo, mDisplayOrientation)
            mCamera!!.setDisplayOrientation(orientation)

            // 새로 변경된 설정으로 프리뷰를 시작한다
            mCamera!!.setPreviewDisplay(mHolder)
            mCamera!!.startPreview()
            Log.d(TAG, "Camera preview REstarted.")
        } catch (e: Exception) {
            Log.d(TAG, "Error in surfaceChanging : " + e.message)
        }
    }

    public override fun surfaceDestroyed(holder: SurfaceHolder) {
        // Surface will be destroyed when we return, so stop the preview.
        // Release the camera for other applications.
        if (mCamera != null) {
            if (isPreview) mCamera!!.stopPreview()
            mCamera!!.release()
            mCamera = null
            isPreview = false
        }
        Log.d(TAG, "surfaceDestroyed")
    }

    override fun onLayout(changed: Boolean, l: Int, t: Int, r: Int, b: Int) {}
    fun takePicture(handler: PictureCallback?): Boolean {
        if (mCamera != null) {
            // 셔터후 ?
            // Raw 이미지 생성후 ?
            // JPE 이미지 생성후 ?
            mCamera!!.takePicture(null, null, handler)
            return true
        } else {
            return false
        }
    }

    //    public void takePicture(){
    //        mCamera.takePicture(shutterCallback, rawCallback, jpegCallback);
    //    }
    var shutterCallback: ShutterCallback = object : ShutterCallback {
        public override fun onShutter() {}
    }
    var rawCallback: PictureCallback = object : PictureCallback {
        public override fun onPictureTaken(data: ByteArray, camera: Camera) {}
    }
    var jpegCallback: PictureCallback = object : PictureCallback {
        public override fun onPictureTaken(data: ByteArray, camera: Camera) {
//            imageHandling(data, camera);
        }
    }

    fun imageHandling(data: ByteArray, camera: Camera) {
        //이미지의 너비와 높이 결정
        val w: Int = camera.getParameters().getPictureSize().width
        val h: Int = camera.getParameters().getPictureSize().height
        val orientation: Int = calculatePreviewOrientation(mCameraInfo, mDisplayOrientation)

        //byte array를 bitmap으로 변환
        val options: BitmapFactory.Options = BitmapFactory.Options()
        options.inPreferredConfig = Bitmap.Config.ARGB_8888
        var bitmap: Bitmap = BitmapFactory.decodeByteArray(data, 0, data.size, options)

        //이미지를 디바이스 방향으로 회전
        val matrix: Matrix = Matrix()
        matrix.postRotate(orientation.toFloat())
        bitmap = Bitmap.createBitmap(bitmap, 0, 0, w, h, matrix, true)

        //bitmap을 byte array로 변환
        val stream: ByteArrayOutputStream = ByteArrayOutputStream()
        bitmap.compress(CompressFormat.JPEG, 100, stream)
        val currentData: ByteArray = stream.toByteArray()

////            //파일로 저장
////            new SaveImageTask().execute(currentData);
//            Intent intent = new Intent();
//            intent.putExtra("camera", currentData);
//            setResult(RESULT_OK, intent);
//            finish();
        CameraActivity.Companion.imageView!!.setImageBitmap(bitmap)
        Log.d("직접사진기", "사진찍힘")
        SaveImageTask().execute(currentData)
    }

    private inner class SaveImageTask : AsyncTask<ByteArray?, Void?, Void?>() {
        var outputFile: File? = null

        @RequiresApi(api = Build.VERSION_CODES.KITKAT)
        override fun doInBackground(vararg data: ByteArray?): Void? {
//            FileOutputStream outStream ;
            try {
                outputFile = CameraActivity.Companion.getInstance!!.createImageFile()
//// =============================================================================
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
                    afterQ(data[0], CameraActivity.getInstance!!.createFileName())
                }
//// =============================================================================
            } catch (e: FileNotFoundException) {
                Log.d("사진기", ("File NotFound : " + data.size + " to "
                        + outputFile!!.absolutePath))
                e.printStackTrace()
            } catch (e: IOException) {
                e.printStackTrace()
            }
            return null
        }
    }

    //    private void afterQ(byte[] data, File outputFile) {
    @RequiresApi(api = Build.VERSION_CODES.Q)
    private fun afterQ(data: ByteArray?, outputFile: String) {
        val outStream: FileOutputStream
        val resolver: ContentResolver

// Insert my file to MediaStore
        val values: ContentValues = ContentValues()
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
            values.put(MediaStore.Audio.Media.RELATIVE_PATH, "Pictures/camtest") // 다른 path..
        }
        values.put(MediaStore.Images.Media.DISPLAY_NAME, outputFile)
        values.put(MediaStore.Images.Media.MIME_TYPE, "image/jpeg")
        values.put(MediaStore.Images.Media.DATE_TAKEN, System.currentTimeMillis())
        //        values.put(MediaStore.Images.Media.DATE_TAKEN, System.currentTimeMillis());
//      file을 write한 다음에 DATE_TAKEN이 설정됨. 그 전에는 null로 setting 됨. IS_PENDING이 1일 경우
        // 파일을 write중이라면 다른곳에서 데이터요구를 무시하겠다는 의미
        values.put(MediaStore.Images.Media.IS_PENDING, 1)
        resolver = mContext!!.getContentResolver()
        // ContentResolver을 통해 insert를 해주고 해당 insert가 되는 위치의 Uri를 리턴받는다.
        // 이후로는 해당 Uri를 통해 파일 관리를 해줄 수 있다.
        val uri: Uri? = resolver.insert(MediaStore.Images.Media.EXTERNAL_CONTENT_URI, values)

//  Uri(item) 위치에 화일 생성
        try {
            var pfd: ParcelFileDescriptor? = null
            //            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.KITKAT) {
            pfd = resolver.openFileDescriptor((uri)!!, "w", null)
            //            }
            if (pfd == null) {
                // ...
            } else {
                outStream = FileOutputStream(pfd.getFileDescriptor())
                //                outStream.write(data[0]);
                outStream.write(data)
                outStream.close()
                pfd.close()
                resolver.update((uri)!!, values, null, null)
            }
        } catch (e: FileNotFoundException) {
            e.printStackTrace()
        } catch (e: IOException) {
            e.printStackTrace()
        }

        // 파일을 모두 write하고 다른곳에서 사용할 수 있도록 0으로 업데이트를 해줍니다.
        values.clear()
        values.put(MediaStore.Images.Media.DATE_TAKEN, System.currentTimeMillis())
        values.put(MediaStore.Images.Media.IS_PENDING, 0)
        resolver.update((uri)!!, values, null, null)

//          //      For deletion, use:
//                getContentResolver().delete(uriOfMediaFileDeteled, null, null);
        Log.d("직접사진기", ("저장됨: " + data!!.size + " to "
                + outputFile
                + "\n" + values.getAsString(MediaStore.Images.Media.DATE_TAKEN)))
    }

    companion object {
        /**
         * 안드로이드 디바이스 방향에 맞는 카메라 프리뷰를 화면에 보여주기 위해 계산합니다.
         */
        private fun calculatePreviewOrientation(info: CameraInfo?, rotation: Int): Int {
            var degrees: Int = 0
            when (rotation) {
                Surface.ROTATION_0 -> degrees = 0
                Surface.ROTATION_90 -> degrees = 90
                Surface.ROTATION_180 -> degrees = 180
                Surface.ROTATION_270 -> degrees = 270
            }
            var result: Int
            if (info!!.facing == CameraInfo.CAMERA_FACING_FRONT) {
                result = (info.orientation + degrees) % 360
                result = (360 - result) % 360 // compensate the mirror
            } else {  // back-facing
                result = (info.orientation - degrees + 360) % 360
            }
            return result
        }
    }
}