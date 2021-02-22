package com.example.mediaplayer

import android.appwidget.AppWidgetManager
import android.content.Context
import android.content.Intent
import android.content.SharedPreferences
import android.net.Uri
import android.os.Bundle
import android.preference.PreferenceManager
import android.util.TypedValue
import android.view.View
import android.widget.SeekBar
import android.widget.SeekBar.OnSeekBarChangeListener
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity
import com.example.mediaplayer.services.MusicService.BroadcastActions

class CaptureWidgetConfigure constructor() : AppCompatActivity(), View.OnClickListener {
    private var mTxtFontSize: TextView? = null
    private var mSeekFontSize: SeekBar? = null
    private var mAppWidgetId: Int = 0
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.appwidget_configure)
        findViewById<View>(R.id.btn_confirm).setOnClickListener(this)
        mTxtFontSize = findViewById<View>(R.id.txt_size) as TextView?
        mSeekFontSize = (findViewById<View>(R.id.seek_size) as SeekBar?)
        mSeekFontSize!!.setOnSeekBarChangeListener(object : OnSeekBarChangeListener {
            public override fun onProgressChanged(seekBar: SeekBar, progress: Int, fromUser: Boolean) {
                mTxtFontSize!!.setTextSize(TypedValue.COMPLEX_UNIT_SP, (mDefaultFontSize + progress).toFloat())
            }

            public override fun onStartTrackingTouch(seekBar: SeekBar) {}
            public override fun onStopTrackingTouch(seekBar: SeekBar) {
                val size: Int = seekBar.getProgress() + mDefaultFontSize
                saveTextSizePref(getApplicationContext(), size)
                //                SharedPreferences.Editor editor = PreferenceManager.getDefaultSharedPreferences(getApplicationContext()).edit();
//                editor.putInt("font_size", seekBar.getProgress() + mDefaultFontSize);
//                editor.apply();
            }
        })
        mDefaultFontSize = 14
        mSeekFontSize!!.setMax(10)
        mSeekFontSize!!.setProgress(0)
        mAppWidgetId = getIntent().getIntExtra(AppWidgetManager.EXTRA_APPWIDGET_ID, AppWidgetManager.INVALID_APPWIDGET_ID)
    }

    public override fun onClick(v: View) {
        sendBroadcast(Intent(BroadcastActions.PLAY_STATE_CHANGED,  // action
                Uri.EMPTY,  // data
                getApplicationContext(),  // context
                CaptureWidgetProvider::class.java)) //class
        val resultValue: Intent = Intent()
        resultValue.putExtra(AppWidgetManager.EXTRA_APPWIDGET_ID, mAppWidgetId)
        setResult(RESULT_OK, resultValue)
        finish()
    }

    companion object {
        private var mDefaultFontSize: Int = 0
        private val PREFS_NAME: String = "com.example.capture.CaptureWidgetProvider"
        private val PREF_PREFIX_KEY: String = "appwidget_"
        fun saveTextSizePref(context: Context?, size: Int) {
            val editor: SharedPreferences.Editor = PreferenceManager.getDefaultSharedPreferences(context).edit()
            editor.putInt("font_size", size)
            editor.apply()
        }

        fun deleteTextSizePref(context: Context?) {
            val prefs: SharedPreferences.Editor = PreferenceManager.getDefaultSharedPreferences(context).edit()
            prefs.clear()
            prefs.apply()
        }

        fun loadTextSizePref(context: Context?): Int {
            val prefs: SharedPreferences = PreferenceManager.getDefaultSharedPreferences(context)
            return prefs.getInt("font_size", mDefaultFontSize)
        }
    }
}