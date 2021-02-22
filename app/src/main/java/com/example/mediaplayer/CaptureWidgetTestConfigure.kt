package com.example.mediaplayer

import android.app.Activity
import android.appwidget.AppWidgetManager
import android.content.*
import android.os.Bundle
import android.view.*
import android.widget.EditText
import com.example.mediaplayer.CaptureWidgetTestConfigure

/**
 * The configuration screen for the [CaptureWidget][CaptureWidgetTest] AppWidget.
 */
class CaptureWidgetTestConfigure constructor() : Activity() {
    var mAppWidgetId: Int = AppWidgetManager.INVALID_APPWIDGET_ID
    var mAppWidgetText: EditText? = null
    var mOnClickListener: View.OnClickListener = object : View.OnClickListener {
        public override fun onClick(v: View) {
            val context: Context = this@CaptureWidgetTestConfigure

            // When the button is clicked, store the string locally
            val widgetText: String = mAppWidgetText!!.getText().toString()
            saveTitlePref(context, mAppWidgetId, widgetText)

            // It is the responsibility of the configuration activity to update the app widget
            val appWidgetManager: AppWidgetManager = AppWidgetManager.getInstance(context)
            CaptureWidgetTest.Companion.updateAppWidget(context, appWidgetManager, mAppWidgetId)

            // Make sure we pass back the original appWidgetId
            val resultValue: Intent = Intent()
            resultValue.putExtra(AppWidgetManager.EXTRA_APPWIDGET_ID, mAppWidgetId)
            setResult(RESULT_OK, resultValue)
            finish()
        }
    }

    public override fun onCreate(icicle: Bundle?) {
        super.onCreate(icicle)

        // Set the result to CANCELED.  This will cause the widget host to cancel
        // out of the widget placement if the user presses the back button.
        setResult(RESULT_CANCELED)
        setContentView(R.layout.capture_widget_test_configure)
        mAppWidgetText = findViewById<View>(R.id.appwidget_text) as EditText?
        findViewById<View>(R.id.add_button).setOnClickListener(mOnClickListener)

        // Find the widget id from the intent.
        val intent: Intent = getIntent()
        val extras: Bundle? = intent.getExtras()
        if (extras != null) {
            mAppWidgetId = extras.getInt(
                    AppWidgetManager.EXTRA_APPWIDGET_ID, AppWidgetManager.INVALID_APPWIDGET_ID)
        }

        // If this activity was started with an intent without an app widget ID, finish with an error.
        if (mAppWidgetId == AppWidgetManager.INVALID_APPWIDGET_ID) {
            finish()
            return
        }
        mAppWidgetText!!.setText(loadTitlePref(this@CaptureWidgetTestConfigure, mAppWidgetId))
    }

    companion object {
        private val PREFS_NAME: String = "com.example.capture.CaptureWidgetTest"
        private val PREF_PREFIX_KEY: String = "appwidget_"

        // Write the prefix to the SharedPreferences object for this widget
        fun saveTitlePref(context: Context, appWidgetId: Int, text: String?) {
            val prefs: SharedPreferences.Editor = context.getSharedPreferences(PREFS_NAME, 0).edit()
            prefs.putString(PREF_PREFIX_KEY + appWidgetId, text)
            prefs.apply()
        }

        // Read the prefix from the SharedPreferences object for this widget.
        // If there is no preference saved, get the default from a resource
        fun loadTitlePref(context: Context, appWidgetId: Int): String {
            val prefs: SharedPreferences = context.getSharedPreferences(PREFS_NAME, 0)
            val titleValue: String? = prefs.getString(PREF_PREFIX_KEY + appWidgetId, null)
            if (titleValue != null) {
                return titleValue
            } else {
                return context.getString(R.string.appwidget_text)
            }
        }

        fun deleteTitlePref(context: Context, appWidgetId: Int) {
            val prefs: SharedPreferences.Editor = context.getSharedPreferences(PREFS_NAME, 0).edit()
            prefs.remove(PREF_PREFIX_KEY + appWidgetId)
            prefs.apply()
        }
    }
}