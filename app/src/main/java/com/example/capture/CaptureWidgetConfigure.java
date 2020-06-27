package com.example.capture;

import android.appwidget.AppWidgetManager;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.util.TypedValue;
import android.view.View;
import android.widget.SeekBar;
import android.widget.TextView;

import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;

public class CaptureWidgetConfigure extends AppCompatActivity implements View.OnClickListener {
    private TextView mTxtFontSize;
    private SeekBar mSeekFontSize;
    private int mAppWidgetId;
    private int mDefaultFontSize;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_appwidget_configure);
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
                SharedPreferences.Editor editor = PreferenceManager.getDefaultSharedPreferences(getApplicationContext()).edit();
                editor.putInt("font_size", seekBar.getProgress() + mDefaultFontSize);
                editor.apply();
            }
        });

        mDefaultFontSize = 14;
        mSeekFontSize.setMax(10);
        mSeekFontSize.setProgress(0);
        mAppWidgetId = getIntent().getIntExtra(AppWidgetManager.EXTRA_APPWIDGET_ID, AppWidgetManager.INVALID_APPWIDGET_ID);
    }

    @Override
    public void onClick(View v) {
        Intent resultValue = new Intent();
        resultValue.putExtra(AppWidgetManager.EXTRA_APPWIDGET_ID, mAppWidgetId);
        setResult(RESULT_OK, resultValue);
        finish();
    }
}

