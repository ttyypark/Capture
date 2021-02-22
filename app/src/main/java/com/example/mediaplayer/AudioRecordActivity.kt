package com.example.mediaplayer

import android.Manifest
import android.content.ContentValues
import android.content.Context
import android.content.pm.PackageManager
import android.media.AudioAttributes
import android.media.AudioManager
import android.media.MediaPlayer
import android.media.MediaRecorder
import android.net.Uri
import android.os.Build
import android.os.Bundle
import android.os.Environment
import android.provider.MediaStore
import android.util.Log
import android.view.View
import android.widget.Button
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.appcompat.widget.AppCompatButton
import androidx.core.app.ActivityCompat
import java.io.File
import java.io.IOException

class AudioRecordActivity constructor() : AppCompatActivity(), View.OnClickListener {
    private var context: Context? = null
    var getInstance: AudioRecordActivity? = null

    //    Music play
    private var position: Int = 0
    private val recordButton: RecordButton? = null
    private var recorder: MediaRecorder? = null
    private val playButton: PlayButton? = null
    private var player: MediaPlayer? = null

    // Requesting permission to RECORD_AUDIO
    private var permissionToRecordAccepted: Boolean = false
    private val permissions: Array<String> = arrayOf(Manifest.permission.RECORD_AUDIO)
    private var mStartRecording: Boolean = true
    private var mStartPlaying: Boolean = true
    private var rButton: Button? = null
    private var pButton: Button? = null
    public override fun onRequestPermissionsResult(requestCode: Int, permissions: Array<String>, grantResults: IntArray) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults)
        when (requestCode) {
            REQUEST_RECORD_AUDIO_PERMISSION -> permissionToRecordAccepted = grantResults.get(0) == PackageManager.PERMISSION_GRANTED
        }
        if (!permissionToRecordAccepted) finish()
    }

    internal inner class RecordButton constructor(ctx: Context?) : AppCompatButton((ctx)!!) {
        var mStartRecording: Boolean = true
        var clicker: OnClickListener = object : OnClickListener {
            public override fun onClick(v: View) {
                onRecord(mStartRecording)
                if (mStartRecording) {
                    setText("Stop recording")
                } else {
                    setText("Start recording")
                }
                mStartRecording = !mStartRecording
            }
        }

        init {
            setText("Start recording")
            setOnClickListener(clicker)
        }
    }

    private fun onRecord(start: Boolean) {
        if (start) {
            startRecording()
        } else {
            stopRecording()
        }
    }

    private fun startRecording() {
//        // Record to the external cache directory for visibility
//        fileName = getExternalCacheDir().getAbsolutePath();

//        File sdcard = Environment.getExternalStorageDirectory();
//        File file = new File(sdcard, "recorded.mp3");
//        fileName = file.getAbsolutePath();
        recorder = MediaRecorder()
        recorder!!.setAudioSource(MediaRecorder.AudioSource.MIC)
        //        recorder.setOutputFormat(MediaRecorder.OutputFormat.THREE_GPP);
//        recorder.setAudioEncoder(MediaRecorder.AudioEncoder.AMR_NB);
        recorder!!.setOutputFormat(MediaRecorder.OutputFormat.MPEG_4)
        recorder!!.setAudioEncoder(MediaRecorder.AudioEncoder.DEFAULT)
        recorder!!.setOutputFile(fileName)
        try {
            recorder!!.prepare()
        } catch (e: IOException) {
            Log.e(TAG, "Record prepare() failed")
        }
        recorder!!.start()
    }

    private fun stopRecording() {
        recorder!!.stop()
        recorder!!.release()
        recorder = null
        val values = ContentValues(10)
        values.put(MediaStore.MediaColumns.TITLE, "Recorded")
        values.put(MediaStore.Audio.Media.ALBUM, "Audio Album")
        values.put(MediaStore.Audio.Media.ARTIST, "Mike")
        values.put(MediaStore.Audio.Media.DISPLAY_NAME, "Recorded Audio")
        values.put(MediaStore.Audio.Media.IS_RINGTONE, 1)
        values.put(MediaStore.Audio.Media.IS_MUSIC, 1)
        values.put(MediaStore.MediaColumns.DATE_ADDED, System.currentTimeMillis() / 1000)
        values.put(MediaStore.MediaColumns.MIME_TYPE, "audio/mp4")
        values.put(MediaStore.Audio.Media.DATA, fileName)
        val audioUri: Uri? = contentResolver.insert(MediaStore.Audio.Media.EXTERNAL_CONTENT_URI, values)
        Log.e(TAG, "audioUri : $audioUri")
    }

    internal inner class PlayButton constructor(ctx: Context?) : AppCompatButton((ctx)!!) {
        var mStartPlaying: Boolean = true
        private var clicker: OnClickListener = object : OnClickListener {
            override fun onClick(v: View) {
                onPlay(mStartPlaying)
                if (mStartPlaying) {
                    text = "Stop playing"
                } else {
                    text = "Start playing"
                }
                mStartPlaying = !mStartPlaying
            }
        }

        init {
            text = "Start playing"
            setOnClickListener(clicker)
        }
    }

    private fun onPlay(start: Boolean) {
        if (start) {
            startPlaying()
        } else {
            stopPlaying()
        }
    }

    private fun startPlaying() {
        player = MediaPlayer()
        try {
            player!!.setDataSource(fileName)
            player!!.prepare()
            player!!.start()
        } catch (e: IOException) {
            Log.e(TAG, "Play prepare() failed")
        }
    }

    private fun stopPlaying() {
        player!!.release()
        player = null
    }

    public override fun onStop() {
        super.onStop()
        if (recorder != null) {
            recorder!!.release()
            recorder = null
        }
        if (player != null) {
            player!!.release()
            player = null
        }
    }

    // --------------------------------------------------------------- to
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        getInstance = this
        context = applicationContext

//   Permissions ----------------------
        ActivityCompat.requestPermissions(this, permissions,
                REQUEST_RECORD_AUDIO_PERMISSION)

////   가상 layout, button ----- from
//        LinearLayout linearLayout = new LinearLayout(this);
//        recordButton = new RecordButton(this);
//        linearLayout.addView(recordButton,
//                new LinearLayout.LayoutParams(
//                        ViewGroup.LayoutParams.WRAP_CONTENT,
//                        ViewGroup.LayoutParams.WRAP_CONTENT,
//                        0));
//        playButton = new PlayButton(this);
//        linearLayout.addView(playButton,
//                new LinearLayout.LayoutParams(
//                        ViewGroup.LayoutParams.WRAP_CONTENT,
//                        ViewGroup.LayoutParams.WRAP_CONTENT,
//                        0));
//        setContentView(linearLayout);
////   가상 layout, button ----- to
        val sdcard: File = Environment.getExternalStorageDirectory()
        val file = File(sdcard, "recorded.mp3")
        fileName = file.absolutePath
        setContentView(R.layout.activity_audio_record)
        mStartRecording = true
        mStartPlaying = true
        rButton = findViewById(R.id.start_record)
        pButton = findViewById(R.id.stop_record)
        findViewById<View>(R.id.start_record).setOnClickListener(this)
        findViewById<View>(R.id.stop_record).setOnClickListener(this)
        findViewById<View>((R.id.music1)).setOnClickListener(this)
        findViewById<View>((R.id.music2)).setOnClickListener(this)
        findViewById<View>((R.id.music3)).setOnClickListener(this)
        findViewById<View>((R.id.music4)).setOnClickListener(this)
    }

    override fun onDestroy() {
        super.onDestroy()
    }

    override fun onClick(v: View) {
        when (v.id) {
            R.id.start_record -> {
                //                startRecord();
                if (mStartRecording) {
                    startRecording()
                    rButton!!.text = "녹음종료"
                } else {
                    stopRecording()
                    rButton!!.text = "녹음시작"
                }
                mStartRecording = !mStartRecording
            }
            R.id.stop_record -> {
                //                stopRecord();
                if (mStartPlaying) {
                    startPlaying()
                    pButton!!.text = "재생종료"
                } else {
                    stopPlaying()
                    pButton!!.text = "재생시작"
                }
                mStartPlaying = !mStartPlaying
            }
            R.id.music1 -> playAudio()
            R.id.music2 -> pauseAudio()
            R.id.music3 -> resumeAudio()
            R.id.music4 -> stopAudio()
        }
    }

    fun stopRecord() {
        if (recorder != null) {
            recorder!!.stop()
            recorder!!.release()
            recorder = null
        }
        Log.d("TAG", "녹음 종료됨 :$fileName")
        Toast.makeText(context, "녹음 종료됨", Toast.LENGTH_SHORT).show()
    }

    fun startRecord() {
        val sdcard: File = Environment.getExternalStorageDirectory()
        val file: File = File(sdcard, "recorded.mp4")
        fileName = file.getAbsolutePath()
        if (recorder == null) recorder = MediaRecorder()
        val values: ContentValues = ContentValues(10)
        values.put(MediaStore.MediaColumns.TITLE, "Recorded")
        values.put(MediaStore.Audio.Media.ALBUM, "Audio Album")
        values.put(MediaStore.Audio.Media.ARTIST, "Mike")
        values.put(MediaStore.Audio.Media.DISPLAY_NAME, "Recorded Audio")
        values.put(MediaStore.Audio.Media.IS_RINGTONE, 1)
        values.put(MediaStore.Audio.Media.IS_MUSIC, 1)
        values.put(MediaStore.MediaColumns.DATE_ADDED, System.currentTimeMillis() / 1000)
        values.put(MediaStore.MediaColumns.MIME_TYPE, "audio/mp4")
        values.put(MediaStore.Audio.Media.DATA, fileName)
        val audioUri: Uri? = contentResolver.insert(MediaStore.Audio.Media.EXTERNAL_CONTENT_URI, values)
        recorder!!.setAudioSource(MediaRecorder.AudioSource.MIC)
        recorder!!.setOutputFormat(MediaRecorder.OutputFormat.MPEG_4)
        recorder!!.setAudioEncoder(MediaRecorder.AudioEncoder.DEFAULT)
        recorder!!.setOutputFile(fileName)
        try {
            recorder!!.prepare()
            recorder!!.start()
        } catch (e: Exception) {
            e.printStackTrace()
        }
        Log.d("TAG", "녹음 시작됨 :$fileName")
        Toast.makeText(context, "녹음 시작됨", Toast.LENGTH_SHORT).show()
    }

    private fun playAudio() {
        killPlayer()

//        try {
        // HTTP스트리밍을 통한 원격의 URL에서 재생을 하는 경우
        val url = "https://sites.google.com/site/ubiaccessmobile/sample_audio.amr"
        player = MediaPlayer()

//            player.setAudioStreamType(AudioManager.STREAM_MUSIC);
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
            player!!.setAudioAttributes(
                    AudioAttributes.Builder()
                            .setContentType(AudioAttributes.CONTENT_TYPE_MUSIC)
                            .build())
        } else {
            player!!.setAudioStreamType(AudioManager.STREAM_MUSIC)
        }
        try {
            player!!.setDataSource(applicationContext, Uri.parse(url))
            player!!.prepare()
            player!!.start()
            Toast.makeText(this, "재생 시작됨", Toast.LENGTH_LONG).show()
            Log.d("음악", "재생 시작됨")
        } catch (e: Exception) {
            e.printStackTrace()
        }
    }

    private fun pauseAudio() {
        if (player != null) {
            position = player!!.currentPosition
            player!!.pause()
            Toast.makeText(this, "일시 정지됨", Toast.LENGTH_LONG).show()
            Log.d("음악", "일시 정지됨")
        }
    }

    private fun resumeAudio() {
        if (player != null && !player!!.isPlaying) {
            player!!.seekTo(position)
            player!!.start()
            Toast.makeText(this, "재시작됨", Toast.LENGTH_LONG).show()
            Log.d("음악", "재시작됨")
        }
    }

    private fun stopAudio() {
        if (player != null && player!!.isPlaying) {
            player!!.stop()
            killPlayer()
            Toast.makeText(this, "중지됨", Toast.LENGTH_LONG).show()
            Log.d("음악", "중지됨")
        }
    }

    private fun killPlayer() {
        if (player != null) {
            player!!.release()
            player = null
        }
    }

    companion object {
        private const val TAG: String = "녹음 "
        private var fileName: String? = null
        private const val REQUEST_RECORD_AUDIO_PERMISSION: Int = 200
    }
}