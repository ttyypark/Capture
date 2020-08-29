package com.example.capture;

import android.appwidget.AppWidgetManager;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.net.Uri;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.util.TypedValue;
import android.view.View;
import android.widget.SeekBar;
import android.widget.TextView;

import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;

import com.example.capture.services.MusicService;

public class CaptureWidgetConfigure extends AppCompatActivity implements View.OnClickListener {
    private TextView mTxtFontSize;
    private SeekBar mSeekFontSize;
    private int mAppWidgetId;
    private static int mDefaultFontSize;

    private static final String PREFS_NAME = "com.example.capture.CaptureWidgetProvider";
    private static final String PREF_PREFIX_KEY = "appwidget_";

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.appwidget_configure);
        findViewById(R.id.btn_confirm).setOnClickListener(this);
        mTxtFontSize = (TextView) findViewById(R.id.txt_size);
        mSeekFontSize = ((SeekBar) findViewById(R.id.seek_size));
        mSeekFontSize.setOnSeekBarChangeListener(new SeekBar.OnSeekBarChangeListener() {
            @Override
            public void onProgressChanged(SeekBar seekBar, int progress, boolean fromUser) {
                mTxtFontSize.setTextSize(TypedValue.COMPLEX_UNIT_SP, mDefaultFontSize + progress);
            }

            @Override
            public void onStartTrackingTouch(SeekBar seekBar) {

            }

            @Override
            public void onStopTrackingTouch(SeekBar seekBar) {
                int size = seekBar.getProgress() + mDefaultFontSize;
                saveTextSizePref(getApplicationContext(), size);
//                SharedPreferences.Editor editor = PreferenceManager.getDefaultSharedPreferences(getApplicationContext()).edit();
//                editor.putInt("font_size", seekBar.getProgress() + mDefaultFontSize);
//                editor.apply();
            }
        });

        mDefaultFontSize = 14;
        mSeekFontSize.setMax(10);
        mSeekFontSize.setProgress(0);
        mAppWidgetId = getIntent().getIntExtra(AppWidgetManager.EXTRA_APPWIDGET_ID, AppWidgetManager.INVALID_APPWIDGET_ID);
    }

    static void saveTextSizePref(Context context, int size) {
        SharedPreferences.Editor editor = PreferenceManager.getDefaultSharedPreferences(context).edit();
        editor.putInt("font_size", size);
        editor.apply();
    }

    static void deleteTextSizePref(Context context) {
        SharedPreferences.Editor prefs = PreferenceManager.getDefaultSharedPreferences(context).edit();
        prefs.clear();
        prefs.apply();
    }

    static int loadTextSizePref(Context context) {
        SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(context);
        return prefs.getInt("font_size", mDefaultFontSize);
    }

    @Override
    public void onClick(View v) {
        sendBroadcast(new Intent(MusicService.BroadcastActions.PLAY_STATE_CHANGED,   // action
                Uri.EMPTY,                                              // data
                getApplicationContext(),                                // context
                CaptureWidgetProvider.class));                          //class

        Intent resultValue = new Intent();
        resultValue.putExtra(AppWidgetManager.EXTRA_APPWIDGET_ID, mAppWidgetId);
        setResult(RESULT_OK, resultValue);
        finish();
    }
}

