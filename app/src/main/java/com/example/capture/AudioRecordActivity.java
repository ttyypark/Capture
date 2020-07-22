package com.example.capture;

import android.Manifest;
import android.content.ContentValues;
import android.content.Context;
import android.content.pm.PackageManager;
import android.media.AudioAttributes;
import android.media.AudioManager;
import android.media.MediaPlayer;
import android.media.MediaRecorder;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.os.Environment;
import android.provider.MediaStore;
import android.util.Log;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.LinearLayout;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;

import java.io.File;
import java.io.IOException;

public class AudioRecordActivity extends AppCompatActivity implements View.OnClickListener{

    private static final String TAG = "녹음 ";
    private Context context;
    public AudioRecordActivity getInstance;

    //    Music play
    private int position = 0;

    private static String fileName = null;
    private static final int REQUEST_RECORD_AUDIO_PERMISSION = 200;

    private RecordButton recordButton = null;
    private MediaRecorder recorder = null;

    private PlayButton playButton = null;
    private MediaPlayer player = null;

    // Requesting permission to RECORD_AUDIO
    private boolean permissionToRecordAccepted = false;
    private String [] permissions = {Manifest.permission.RECORD_AUDIO};

    private boolean mStartRecording = true;
    private boolean mStartPlaying = true;
    private Button rButton;
    private Button pButton;


    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        switch (requestCode){
            case REQUEST_RECORD_AUDIO_PERMISSION:
                permissionToRecordAccepted  = grantResults[0] == PackageManager.PERMISSION_GRANTED;
                break;
        }
        if (!permissionToRecordAccepted ) finish();

    }

    class RecordButton extends androidx.appcompat.widget.AppCompatButton {
        boolean mStartRecording = true;

        OnClickListener clicker = new OnClickListener() {
            public void onClick(View v) {
                onRecord(mStartRecording);
                if (mStartRecording) {
                    setText("Stop recording");
                } else {
                    setText("Start recording");
                }
                mStartRecording = !mStartRecording;
            }
        };

        public RecordButton(Context ctx) {
            super(ctx);
            setText("Start recording");
            setOnClickListener(clicker);
        }
    }

    private void onRecord(boolean start) {
        if (start) {
            startRecording();
        } else {
            stopRecording();
        }
    }

    private void startRecording() {
//        // Record to the external cache directory for visibility
//        fileName = getExternalCacheDir().getAbsolutePath();

//        File sdcard = Environment.getExternalStorageDirectory();
//        File file = new File(sdcard, "recorded.mp3");
//        fileName = file.getAbsolutePath();

        recorder = new MediaRecorder();
        recorder.setAudioSource(MediaRecorder.AudioSource.MIC);
//        recorder.setOutputFormat(MediaRecorder.OutputFormat.THREE_GPP);
//        recorder.setAudioEncoder(MediaRecorder.AudioEncoder.AMR_NB);
        recorder.setOutputFormat(MediaRecorder.OutputFormat.MPEG_4);
        recorder.setAudioEncoder(MediaRecorder.AudioEncoder.DEFAULT);
        recorder.setOutputFile(fileName);

        try {
            recorder.prepare();
        } catch (IOException e) {
            Log.e(TAG, "Record prepare() failed");
        }

        recorder.start();

    }

    private void stopRecording() {
        recorder.stop();
        recorder.release();
        recorder = null;

        ContentValues values = new ContentValues(10);
        values.put(MediaStore.MediaColumns.TITLE, "Recorded");
        values.put(MediaStore.Audio.Media.ALBUM, "Audio Album");
        values.put(MediaStore.Audio.Media.ARTIST, "Mike");
        values.put(MediaStore.Audio.Media.DISPLAY_NAME, "Recorded Audio");
        values.put(MediaStore.Audio.Media.IS_RINGTONE, 1);
        values.put(MediaStore.Audio.Media.IS_MUSIC, 1);
        values.put(MediaStore.MediaColumns.DATE_ADDED, System.currentTimeMillis()/1000);
        values.put(MediaStore.MediaColumns.MIME_TYPE, "audio/mp4");
        values.put(MediaStore.Audio.Media.DATA, fileName);

        Uri audioUri = getContentResolver().insert(MediaStore.Audio.Media.EXTERNAL_CONTENT_URI, values);
        Log.e(TAG, "audioUri : " + audioUri);
    }

    class PlayButton extends androidx.appcompat.widget.AppCompatButton {
        boolean mStartPlaying = true;

        OnClickListener clicker = new OnClickListener() {
            public void onClick(View v) {
                onPlay(mStartPlaying);
                if (mStartPlaying) {
                    setText("Stop playing");
                } else {
                    setText("Start playing");
                }
                mStartPlaying = !mStartPlaying;
            }
        };

        public PlayButton(Context ctx) {
            super(ctx);
            setText("Start playing");
            setOnClickListener(clicker);
        }
    }

    private void onPlay(boolean start) {
        if (start) {
            startPlaying();
        } else {
            stopPlaying();
        }
    }

    private void startPlaying() {
        player = new MediaPlayer();
        try {
            player.setDataSource(fileName);
            player.prepare();
            player.start();
        } catch (IOException e) {
            Log.e(TAG, "Play prepare() failed");
        }
    }

    private void stopPlaying() {
        player.release();
        player = null;
    }

    @Override
    public void onStop() {
        super.onStop();
        if (recorder != null) {
            recorder.release();
            recorder = null;
        }

        if (player != null) {
            player.release();
            player = null;
        }
    }

// --------------------------------------------------------------- to

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        getInstance = this;
        context = getApplicationContext();

//   Permissions ----------------------
        ActivityCompat.requestPermissions(this, permissions,
                REQUEST_RECORD_AUDIO_PERMISSION);

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

        File sdcard = Environment.getExternalStorageDirectory();
        File file = new File(sdcard, "recorded.mp3");
        fileName = file.getAbsolutePath();

        setContentView(R.layout.activity_audio_record);
        mStartRecording = true;
        mStartPlaying = true;

        rButton = findViewById(R.id.start_record);
        pButton = findViewById(R.id.stop_record);

        findViewById(R.id.start_record).setOnClickListener(this);
        findViewById(R.id.stop_record).setOnClickListener(this);

        findViewById((R.id.music1)).setOnClickListener(this);
        findViewById((R.id.music2)).setOnClickListener(this);
        findViewById((R.id.music3)).setOnClickListener(this);
        findViewById((R.id.music4)).setOnClickListener(this);

    }


    @Override
    protected void onDestroy() {
        super.onDestroy();
    }

    @Override
    public void onClick(View v) {
        switch (v.getId()) {
            case R.id.start_record: //재생
//                startRecord();
                if (mStartRecording) {
                    startRecording();
                    rButton.setText("녹음종료");
                } else {
                    stopRecording();
                    rButton.setText("녹음시작");
                }
                mStartRecording = !mStartRecording;
                break;
            case R.id.stop_record: //재생
//                stopRecord();
                if (mStartPlaying) {
                    startPlaying();
                    pButton.setText("재생종료");
                } else {
                    stopPlaying();
                    pButton.setText("재생시작");
                }
                mStartPlaying = !mStartPlaying;
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

    public void stopRecord() {
        if (recorder != null) {
            recorder.stop();
            recorder.release();
            recorder = null;
        }

        Log.d("TAG", "녹음 종료됨 :" + fileName);
        Toast.makeText(context, "녹음 종료됨", Toast.LENGTH_SHORT).show();
    }

    public void startRecord() {

        File sdcard = Environment.getExternalStorageDirectory();
        File file = new File(sdcard, "recorded.mp4");
        fileName = file.getAbsolutePath();

        if(recorder == null) recorder = new MediaRecorder();

        ContentValues values = new ContentValues(10);
        values.put(MediaStore.MediaColumns.TITLE, "Recorded");
        values.put(MediaStore.Audio.Media.ALBUM, "Audio Album");
        values.put(MediaStore.Audio.Media.ARTIST, "Mike");
        values.put(MediaStore.Audio.Media.DISPLAY_NAME, "Recorded Audio");
        values.put(MediaStore.Audio.Media.IS_RINGTONE, 1);
        values.put(MediaStore.Audio.Media.IS_MUSIC, 1);
        values.put(MediaStore.MediaColumns.DATE_ADDED, System.currentTimeMillis()/1000);
        values.put(MediaStore.MediaColumns.MIME_TYPE, "audio/mp4");
        values.put(MediaStore.Audio.Media.DATA, fileName);
        Uri audioUri = getContentResolver().insert(MediaStore.Audio.Media.EXTERNAL_CONTENT_URI, values);

        recorder.setAudioSource(MediaRecorder.AudioSource.MIC);
        recorder.setOutputFormat(MediaRecorder.OutputFormat.MPEG_4);
        recorder.setAudioEncoder(MediaRecorder.AudioEncoder.DEFAULT);

        recorder.setOutputFile(fileName);

        try {
            recorder.prepare();
            recorder.start();
        } catch (Exception e){
            e.printStackTrace();
        }

        Log.d("TAG", "녹음 시작됨 :" + fileName);
        Toast.makeText(context, "녹음 시작됨", Toast.LENGTH_SHORT).show();
    }


    public void playAudio(){

        killPlayer();

//        try {
        // HTTP스트리밍을 통한 원격의 URL에서 재생을 하는 경우
        String url = "https://sites.google.com/site/ubiaccessmobile/sample_audio.amr";
        player = new MediaPlayer();

//            player.setAudioStreamType(AudioManager.STREAM_MUSIC);
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
            player.setAudioAttributes(
                    new AudioAttributes
                            .Builder()
                            .setContentType(AudioAttributes.CONTENT_TYPE_MUSIC)
                            .build());
        } else {
            player.setAudioStreamType(AudioManager.STREAM_MUSIC);
        }

        try {
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

}
