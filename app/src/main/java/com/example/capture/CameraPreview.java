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
import android.os.Environment;
import android.provider.MediaStore;
import android.util.AttributeSet;
import android.util.Log;
import android.view.Surface;
import android.view.SurfaceHolder;
import android.view.SurfaceView;
import android.view.View;
import android.view.ViewGroup;

import androidx.appcompat.app.AppCompatActivity;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.List;

import static android.app.Activity.RESULT_OK;

class CameraPreview extends ViewGroup implements SurfaceHolder.Callback {
    private final String TAG = "CameraPreview";

    private Context context;
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

        CameraPhoto.imageView.setImageBitmap(bitmap);
        Log.d("직접사진기", "사진찍힘");

        new SaveImageTask().execute(currentData);

    }

    private class SaveImageTask extends AsyncTask<byte[], Void, Void> {
        File outputFile;
        @Override
        protected Void doInBackground(byte[]... data) {
            FileOutputStream outStream ;

            try {

//                File path = MainActivity.getInstance.getExternalFilesDir(Environment.DIRECTORY_PICTURES);
////                File path = new File(Environment.getExternalStorageDirectory().getAbsolutePath() + "/capture");
//                if (!path.exists()) {
//                    path.mkdirs();
//                }
//
//                String fileName = String.format("%d.jpg", System.currentTimeMillis());
//                File outputFile = new File(path, fileName);

                outputFile = CameraPhoto.getInstance.createImageFile();

                outStream = new FileOutputStream(outputFile);
//                outStream = new FileOutputStream(MainActivity.currentPhotoPath);
                outStream.write(data[0]);
                outStream.flush();
                outStream.close();

                Log.d("직접사진기", "저장됨: " + data.length + " to "
                        + outputFile.getAbsolutePath());

                // 갤러리에 반영
                Intent mediaScanIntent = new Intent(Intent.ACTION_MEDIA_SCANNER_SCAN_FILE);
                mediaScanIntent.setData(Uri.fromFile(outputFile));
                getContext().sendBroadcast(mediaScanIntent);

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
