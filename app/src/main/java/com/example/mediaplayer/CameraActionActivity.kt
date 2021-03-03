@file:Suppress("DEPRECATION")

package com.example.mediaplayer

import android.annotation.SuppressLint
import android.content.Intent
import android.hardware.Camera
import android.hardware.Camera.CameraInfo
import android.hardware.Camera.PictureCallback
import android.os.Bundle
import android.view.SurfaceHolder
import android.view.SurfaceView
import android.view.View
import android.view.WindowManager
import android.widget.Button
import androidx.appcompat.app.AppCompatActivity

class CameraActionActivity : AppCompatActivity() {
    private lateinit var surfaceView: SurfaceView
    private val callback: PictureCallback? = null
    private val holder: SurfaceHolder? = null

    @Suppress("DEPRECATION")
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        // 상태바를 안보이도록 합니다.
        window.setFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN,
                WindowManager.LayoutParams.FLAG_FULLSCREEN)

        // 화면 켜진 상태를 유지합니다.
        window.setFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON,
                WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON)
        setContentView(R.layout.activity_camera_action)
        surfaceView = findViewById(R.id.preview)

        // 런타임 퍼미션 완료될때 까지 화면에서 보이지 않게 해야합니다.
        surfaceView.visibility = View.GONE
        val button: Button = findViewById(R.id.button_main_capture)
        button.setOnClickListener(object : View.OnClickListener {
            override fun onClick(v: View) {
                mCameraPreview!!.takePicture(object : PictureCallback {
                    override fun onPictureTaken(data: ByteArray, camera: Camera) {
                        //                // 돌아감?
                        mCameraPreview!!.imageHandling(data, camera)
                        val intent = Intent()
                        setResult(RESULT_OK, intent)
                        finish()
                    }
                })
            }
        })
        mCameraPreview = CameraPreview(this, this, CAMERA_FACING, surfaceView)
    }

    @Suppress("DEPRECATION")
    companion object {
        @SuppressLint("StaticFieldLeak")
        var mCameraPreview: CameraPreview? = null
        private const val CAMERA_FACING: Int = CameraInfo.CAMERA_FACING_BACK // Camera.CameraInfo.CAMERA_FACING_FRONT
    }
}