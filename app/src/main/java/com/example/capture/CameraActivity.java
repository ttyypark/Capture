package com.example.capture;

import android.content.Context;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.hardware.Camera;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;

import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.core.content.FileProvider;

import android.os.Environment;
import android.provider.MediaStore;
import android.util.Log;
import android.view.SurfaceHolder;
import android.view.SurfaceView;
import android.view.View;
import android.widget.ImageView;
import android.widget.Toast;

import java.io.File;
import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Locale;

public class CameraActivity extends AppCompatActivity implements View.OnClickListener {
    private static final int REQUEST_GIVEN_IMAGE_CATURE = 101;
    private static final int REQUEST_MY_IMAGE_CATURE = 102;
    private static final String TAG = "카메라";
    private static final int CAMERA_FACING = Camera.CameraInfo.CAMERA_FACING_BACK; // Camera.CameraInfo.CAMERA_FACING_FRONT
    private static Camera camera;
    private File photoFile;
    private Uri photoUri;

    //    private CameraPreview mCameraPreview;
    private SurfaceView surfaceView;
    private SurfaceHolder holder;
    private Context context;
    public static CameraActivity getInstance;

    public static ImageView imageView;
    public static String currentPhotoPath;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_camera);
        getInstance = this;
        context = getApplicationContext();

        Toolbar toolbar = findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);

        imageView = findViewById(R.id.imagePhotoView);

        findViewById(R.id.button).setOnClickListener(this);
        findViewById(R.id.button2).setOnClickListener(this);

    }


    @Override
    public void onClick(View v) {
        switch (v.getId()) {
            case R.id.button:  //사진앱으로 찍기
                takePicture();
//                mCameraPreview.setVisibility(View.INVISIBLE);
                break;
            case R.id.button2: //직접 찍기
                Intent intent = new Intent(getApplicationContext(), CameraActionActivity.class);
                startActivityForResult(intent, REQUEST_MY_IMAGE_CATURE);
                break;

        }
    }

    private void takePicture(){

        String state = Environment.getExternalStorageState();

        if(Environment.MEDIA_MOUNTED.equals(state)) {
            Intent intent = new Intent(android.provider.MediaStore.ACTION_IMAGE_CAPTURE);
            // Ensure that there's a camera activity to handle the intent
            if(intent.resolveActivity(getPackageManager()) != null){
                photoFile = null;
                try{
                    photoFile = createImageFile();
                } catch (IOException ex){
                    // error
                    Log.v("알림", "이미지 생성 오류! createImagefile.");
                    Toast.makeText(this, "이미지 생성 오류! createImagefile.", Toast.LENGTH_SHORT).show();
                    ex.printStackTrace();
//                    finish();
                }

                if (photoFile != null){

                    if(Build.VERSION.SDK_INT >= Build.VERSION_CODES.N){     // 임시화일
                        photoUri = FileProvider.getUriForFile(this,
                                getPackageName(),
//                            "com.example.mycapture.provider",
                                photoFile);
                    } else {
                        photoUri = Uri.fromFile(photoFile);
                    }
                    // content://com.example.mycapture/external_file/Pictures/JPEG_20200208_031050_7007720671382549777.jpg
                    // paths.xml 에 있는 <external-files-path
                    //        name="external_file"
                    //        path="/" /> <!-- Context.getExternalFilesDir() 외부 저장소-->
                    // 내용을 받아옴

//                    imgUri = photoUri;
                    intent.putExtra(MediaStore.EXTRA_OUTPUT, photoUri);

//                    intent.putExtra(MediaStore.EXTRA_OUTPUT, Uri.fromFile(photoFile)); // 추가

                    intent.addFlags(Intent.FLAG_GRANT_WRITE_URI_PERMISSION); // set flag to permission
                    intent.addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION); // set flag to permission
                    grantUriPermission(getPackageName(), photoUri, Intent.FLAG_GRANT_WRITE_URI_PERMISSION |
                            Intent.FLAG_GRANT_READ_URI_PERMISSION);

                    startActivityForResult(intent, REQUEST_GIVEN_IMAGE_CATURE);
                }
            }
        } else {
            Log.v("알림", "저장공간에 접근 불가능");
        }
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data); // data로 icon 크기 사진 반환

        if (requestCode == REQUEST_GIVEN_IMAGE_CATURE && resultCode == RESULT_OK) {
            // 갤러리에 반영
            Intent mediaScanIntent = new Intent(Intent.ACTION_MEDIA_SCANNER_SCAN_FILE);
            mediaScanIntent.setData(Uri.fromFile(photoFile));
//            getContext().sendBroadcast(mediaScanIntent);
            context.sendBroadcast(mediaScanIntent);

            Log.d("사진앱", "사진 저장됨 : " + currentPhotoPath);
            setImage();

//            // Uri 로 받음
//            imageView.setImageURI(photoUri);

//            // data로 받음. capture()함수 사용시.....
//            Bundle extras = data.getExtras();
//            Bitmap imageBitmap = (Bitmap) extras.get("data");
//            imageView.setImageBitmap(imageBitmap);
        } else if (requestCode == REQUEST_MY_IMAGE_CATURE && resultCode == RESULT_OK) {

        }
    }

    private void setImage() {
        BitmapFactory.Options option = new BitmapFactory.Options();
        option.inSampleSize = 8;  // 1/8로 줄임
        Bitmap bitmap = BitmapFactory.decodeFile(currentPhotoPath, option);

        imageView.setImageBitmap(bitmap);

    }


    public static Camera getCamera(){
        return camera;
    }

    public File createImageFile() throws IOException {
        File image;
        // create an image file name
        String timeStamp = new SimpleDateFormat("yyMMdd_HHmmss", Locale.KOREA).format(new Date());
        String imageFileName = "JPEG_" + timeStamp;

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
        File storageDir = new File(Environment.getExternalStorageDirectory() + "/camtest");

//        // 특정 폴더 아닌 메모리 최상에 위치
//        File storageDir = Environment.getExternalStorageDirectory();
// ===================================================================================


        if (!storageDir.exists()) {
            boolean mkdirs = storageDir.mkdirs();
            if( !mkdirs) {
                Log.e("Error : ", "storageDir.mkdirs ");
            }
        }

        image = new File(storageDir, imageFileName + ".jpg");
//        image = File.createTempFile(
//                imageFileName, /* prefix */
//                ".jpg",   /* suffix */
//                storageDir);   /* directory */

        // Save a file: path for use with ACTION_VIEW intents
        currentPhotoPath = image.getAbsolutePath();

//        image.deleteOnExit();   // 종료와 동시 삭제됨.
        return image;
    }

}
