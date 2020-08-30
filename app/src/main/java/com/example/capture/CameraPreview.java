package com.example.capture;

import android.content.ContentResolver;
import android.content.ContentValues;
import android.content.Context;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.Bitmap.CompressFormat;
import android.graphics.BitmapFactory;
import android.graphics.Matrix;
import android.hardware.Camera;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Build;
import android.os.ParcelFileDescriptor;
import android.provider.MediaStore;
import android.util.AttributeSet;
import android.util.Log;
import android.view.Surface;
import android.view.SurfaceHolder;
import android.view.SurfaceView;
import android.view.View;
import android.view.ViewGroup;

import androidx.annotation.RequiresApi;
import androidx.appcompat.app.AppCompatActivity;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.List;

//// camera 예제
// https://webnautes.tistory.com/822
//
class CameraPreview extends ViewGroup implements SurfaceHolder.Callback {
    private final String TAG = "CameraPreview";

    private Context mContext;
    private AppCompatActivity mActivity;
    private Camera mCamera = null;
    private SurfaceView mSurfaceView;
    private SurfaceHolder mHolder;
    private int mCameraID;

    private Camera.CameraInfo mCameraInfo;
    private int mDisplayOrientation;

//    private List<Camera.Size> listPreviewSizes;
    private boolean isPreview = false;


    public CameraPreview(Context context) {
        super(context);
        this.mContext = context;
    }

    public CameraPreview(Context context, AttributeSet attrs) {
        super(context, attrs);
    }

    public CameraPreview(Context context, AppCompatActivity activity, int cameraID, SurfaceView surfaceView) {
        super(context);

        Log.d("@@@", "Preview 생성");

//        mCamera = MainActivity.getCamera();
//        if (mCamera == null){
//            mCamera = Camera.open();
//        }

        mCamera = Camera.open();

//        listPreviewSizes = mCamera.getParameters().getSupportedPreviewSizes();

        mActivity = activity;
        mCameraID = cameraID;
        mSurfaceView = surfaceView;
        mContext = context;

        mSurfaceView.setVisibility(View.VISIBLE);
        init();
    }

    private void init(){
        // SurfaceHolder.Callback를 등록하여 surface의 생성 및 해제 시점을 감지
        mHolder = mSurfaceView.getHolder();
        mHolder.addCallback(this);
        mHolder.setType(SurfaceHolder.SURFACE_TYPE_PUSH_BUFFERS); // 카메라가 SurfaceView를 독점
    }

    @Override
    public void surfaceCreated(SurfaceHolder holder) {
        // Open an instance of the camera
        try {
            if(mCamera == null){
                mCamera = Camera.open(mCameraID); // attempt to get a Camera instance
            }
        } catch (Exception e) {
            // Camera is not available (in use or does not exist)
            Log.e(TAG, "Camera " + mCameraID + " is not available: " + e.getMessage());
        }

        // retrieve camera's info.
        Camera.CameraInfo cameraInfo = new Camera.CameraInfo();
        Camera.getCameraInfo(mCameraID, cameraInfo);

        mCameraInfo = cameraInfo;
        mDisplayOrientation = mActivity.getWindowManager().getDefaultDisplay().getRotation();

        int orientation = calculatePreviewOrientation(mCameraInfo, mDisplayOrientation);
        mCamera.setDisplayOrientation(orientation);

//        listPreviewSizes = mCamera.getParameters().getSupportedPreviewSizes();
        requestLayout();

        // 카메라 설정
        Camera.Parameters parameters = mCamera.getParameters();

        List<String> focusModes = parameters.getSupportedFocusModes();
        if (focusModes.contains(Camera.Parameters.FOCUS_MODE_AUTO)) {
            // set the focus mode
            parameters.setFocusMode(Camera.Parameters.FOCUS_MODE_AUTO);
            // set Camera parameters
            mCamera.setParameters(parameters);
        }

        try {
            mCamera.setPreviewDisplay(mHolder);
            // Important: Call startPreview() to start updating the preview
            // surface. Preview must be started before you can take a picture.
            mCamera.startPreview();
            isPreview = true;
            Log.d(TAG, "Camera preview started.");
        } catch (IOException e) {
            Log.d(TAG, "Error setting camera preview: " + e.getMessage());
        }

    }

    @Override
    public void surfaceChanged(SurfaceHolder holder, int format, int width, int height) {

        // If your preview can change or rotate, take care of those events here.
        // Make sure to stop the preview before resizing or reformatting it.

        if (mHolder.getSurface() == null) {
            // preview surface does not exist
            Log.d(TAG, "Preview surface does not exist");
            return;
        }

        // 프리뷰를 다시 설정한다.
        // stop preview before making changes
        try {
            mCamera.stopPreview();
            Log.d(TAG, "Preview stopped.");
            int orientation = calculatePreviewOrientation(mCameraInfo, mDisplayOrientation);
            mCamera.setDisplayOrientation(orientation);

            // 새로 변경된 설정으로 프리뷰를 시작한다
            mCamera.setPreviewDisplay(mHolder);
            mCamera.startPreview();
            Log.d(TAG, "Camera preview REstarted.");

        } catch (Exception e){
            Log.d(TAG, "Error in surfaceChanging : " + e.getMessage());
        }
    }

    @Override
    public void surfaceDestroyed(SurfaceHolder holder) {
        // Surface will be destroyed when we return, so stop the preview.
        // Release the camera for other applications.
        if (mCamera != null) {
            if (isPreview)
                mCamera.stopPreview();
            mCamera.release();
            mCamera = null;
            isPreview = false;
        }
        Log.d(TAG, "surfaceDestroyed");

    }

    @Override
    protected void onLayout(boolean changed, int l, int t, int r, int b) {

    }

    public boolean takePicture(Camera.PictureCallback handler) {
        if(mCamera != null) {
            // 셔터후 ?
            // Raw 이미지 생성후 ?
            // JPE 이미지 생성후 ?
            mCamera.takePicture(null, null, handler);
            return true;
        } else{
            return false;
        }
    }

//    public void takePicture(){
//        mCamera.takePicture(shutterCallback, rawCallback, jpegCallback);
//    }

    Camera.ShutterCallback shutterCallback = new Camera.ShutterCallback() {
        public void onShutter() {
        }
    };

    Camera.PictureCallback rawCallback = new Camera.PictureCallback() {
        public void onPictureTaken(byte[] data, Camera camera) {
        }
    };

    Camera.PictureCallback jpegCallback = new Camera.PictureCallback() {
        public void onPictureTaken(byte[] data, Camera camera) {
//            imageHandling(data, camera);
        }
    };

    public void imageHandling(byte[] data, Camera camera){
        //이미지의 너비와 높이 결정
        int w = camera.getParameters().getPictureSize().width;
        int h = camera.getParameters().getPictureSize().height;
        int orientation = calculatePreviewOrientation(mCameraInfo, mDisplayOrientation);

        //byte array를 bitmap으로 변환
        BitmapFactory.Options options = new BitmapFactory.Options();
        options.inPreferredConfig = Bitmap.Config.ARGB_8888;
        Bitmap bitmap = BitmapFactory.decodeByteArray( data, 0, data.length, options);

        //이미지를 디바이스 방향으로 회전
        Matrix matrix = new Matrix();
        matrix.postRotate(orientation);
        bitmap =  Bitmap.createBitmap(bitmap, 0, 0, w, h, matrix, true);

        //bitmap을 byte array로 변환
        ByteArrayOutputStream stream = new ByteArrayOutputStream();
        bitmap.compress(CompressFormat.JPEG, 100, stream);
        byte[] currentData = stream.toByteArray();

////            //파일로 저장
////            new SaveImageTask().execute(currentData);
//            Intent intent = new Intent();
//            intent.putExtra("camera", currentData);
//            setResult(RESULT_OK, intent);
//            finish();

        CameraActivity.imageView.setImageBitmap(bitmap);
        Log.d("직접사진기", "사진찍힘");

        new SaveImageTask().execute(currentData);

    }

    private class SaveImageTask extends AsyncTask<byte[], Void, Void> {
        File outputFile;

        @RequiresApi(api = Build.VERSION_CODES.KITKAT)
        @Override
        protected Void doInBackground(byte[]... data) {
//            FileOutputStream outStream ;

            try {
                outputFile = CameraActivity.getInstance.createImageFile();

//// ----------------------------------------------------------------------
//                beforeQ(data[0], outputFile);
//// ----------------------------------------------------------------------
//                outStream = new FileOutputStream(outputFile);
//                outStream.write(data[0]);
//                outStream.flush();
//                outStream.close();
//
//                // 갤러리에 반영
//                Intent mediaScanIntent = new Intent(Intent.ACTION_MEDIA_SCANNER_SCAN_FILE);
//                mediaScanIntent.setData(Uri.fromFile(outputFile));
//                mContext.sendBroadcast(mediaScanIntent);
//// ----------------------------------------------------------------------

//// =============================================================================
//                afterQ(data[0], outputFile);
                afterQ(data[0], CameraActivity.getInstance.createFileName());
//// =============================================================================
//// Insert my file to MediaStore
//                ContentValues values = new ContentValues();
////                values.put(MediaStore.Audio.Media.RELATIVE_PATH, "DCIM/My Images"); // 다른 path..
//                values.put(MediaStore.Images.Media.DISPLAY_NAME, String.valueOf(outputFile));
//                values.put(MediaStore.Images.Media.MIME_TYPE, "image/*");
//                values.put(MediaStore.Images.Media.DATE_TAKEN, System.currentTimeMillis());
//                values.put(MediaStore.Images.Media.DATE_ADDED, System.currentTimeMillis());
//                // 파일을 write중이라면 다른곳에서 데이터요구를 무시하겠다는 의미
//                values.put(MediaStore.Images.Media.IS_PENDING, 1);
//
//                ContentResolver resolver = mContext.getContentResolver();
//                Uri collection = MediaStore.Images.Media.getContentUri(MediaStore.VOLUME_EXTERNAL_PRIMARY);
//                // ContentResolver을 통해 insert를 해주고 해당 insert가 되는 위치의 Uri를 리턴받는다.
//                // 이후로는 해당 Uri를 통해 파일 관리를 해줄 수 있다.
//                Uri item = resolver.insert(collection,values);
//
////  Uri(item) 위치에 화일 생성
//                try {
//                    ParcelFileDescriptor pfd = resolver.openFileDescriptor(item,"w", null);
//                    if(pfd == null){
//
//                    } else {
//                        outStream = new FileOutputStream(pfd.getFileDescriptor());
//                        outStream.write(data[0]);
//                        outStream.close();
//                        pfd.close();
//                        resolver.update(item, values, null, null);
//                    }
//                } catch (FileNotFoundException e) {
//                    e.printStackTrace();
//                } catch (IOException e){
//                    e.printStackTrace();
//                }
//
//                values.clear();
//                // 파일을 모두 write하고 다른곳에서 사용할 수 있도록 0으로 업데이트를 해줍니다.
//                values.put(MediaStore.Images.Media.IS_PENDING, 0);
//                resolver.update(item, values, null, null);
//
//                Log.d("직접사진기", "저장됨: " + data.length + " to "
//                        + outputFile.getAbsolutePath());
//// =============================================================================


            } catch (FileNotFoundException e) {
                Log.d("사진기", "File NotFound : " + data.length + " to "
                        + outputFile.getAbsolutePath());
                e.printStackTrace();
            } catch (IOException e) {
                e.printStackTrace();
            }

            return null;
        }
    }


//    private void afterQ(byte[] data, File outputFile) {
    @RequiresApi(api = Build.VERSION_CODES.Q)
    private void afterQ(byte[] data, String outputFile) {
        FileOutputStream outStream ;
        ContentResolver resolver;

// Insert my file to MediaStore
        ContentValues values = new ContentValues();
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
            values.put(MediaStore.Audio.Media.RELATIVE_PATH, "Pictures/camtest"); // 다른 path..
        }
        values.put(MediaStore.Images.Media.DISPLAY_NAME, outputFile);
        values.put(MediaStore.Images.Media.MIME_TYPE, "image/jpeg");
        values.put(MediaStore.Images.Media.DATE_TAKEN, System.currentTimeMillis());
//        values.put(MediaStore.Images.Media.DATE_TAKEN, System.currentTimeMillis());
//      file을 write한 다음에 DATE_TAKEN이 설정됨. 그 전에는 null로 setting 됨. IS_PENDING이 1일 경우
        // 파일을 write중이라면 다른곳에서 데이터요구를 무시하겠다는 의미
        values.put(MediaStore.Images.Media.IS_PENDING, 1);

        resolver = mContext.getContentResolver();
        // ContentResolver을 통해 insert를 해주고 해당 insert가 되는 위치의 Uri를 리턴받는다.
        // 이후로는 해당 Uri를 통해 파일 관리를 해줄 수 있다.
        Uri uri = resolver.insert(MediaStore.Images.Media.EXTERNAL_CONTENT_URI, values);

//  Uri(item) 위치에 화일 생성
        try {
            ParcelFileDescriptor pfd = null;
//            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.KITKAT) {
                pfd = resolver.openFileDescriptor(uri,"w", null);
//            }
            if (pfd == null){
                // ...
            } else {
                outStream = new FileOutputStream(pfd.getFileDescriptor());
//                outStream.write(data[0]);
                outStream.write(data);
                outStream.close();
                pfd.close();
                resolver.update(uri, values, null, null);
            }
        } catch (FileNotFoundException e) {
            e.printStackTrace();
        } catch (IOException e){
            e.printStackTrace();
        }

        // 파일을 모두 write하고 다른곳에서 사용할 수 있도록 0으로 업데이트를 해줍니다.
        values.clear();
        values.put(MediaStore.Images.Media.DATE_TAKEN, System.currentTimeMillis());
        values.put(MediaStore.Images.Media.IS_PENDING, 0);
        resolver.update(uri, values, null, null);

//          //      For deletion, use:
//                getContentResolver().delete(uriOfMediaFileDeteled, null, null);

        Log.d("직접사진기", "저장됨: " + data.length + " to "
                + outputFile
                + "\n" + values.getAsString(MediaStore.Images.Media.DATE_TAKEN));


    }

     /**
     * 안드로이드 디바이스 방향에 맞는 카메라 프리뷰를 화면에 보여주기 위해 계산합니다.
     */
    private static int calculatePreviewOrientation(Camera.CameraInfo info, int rotation) {
        int degrees = 0;

        switch (rotation) {
            case Surface.ROTATION_0:
                degrees = 0;
                break;
            case Surface.ROTATION_90:
                degrees = 90;
                break;
            case Surface.ROTATION_180:
                degrees = 180;
                break;
            case Surface.ROTATION_270:
                degrees = 270;
                break;
        }

        int result;
        if (info.facing == Camera.CameraInfo.CAMERA_FACING_FRONT) {
            result = (info.orientation + degrees) % 360;
            result = (360 - result) % 360;  // compensate the mirror
        } else {  // back-facing
            result = (info.orientation - degrees + 360) % 360;
        }

        return result;
    }

}
