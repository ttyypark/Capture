package com.example.mediaplayer

import android.annotation.SuppressLint
import android.app.AlarmManager
import android.app.PendingIntent
import android.appwidget.AppWidgetManager
import android.appwidget.AppWidgetProvider
import android.content.*
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.graphics.Color
import android.net.Uri
import android.os.AsyncTask
import android.os.Build
import android.os.Bundle
import android.util.Log
import android.widget.RemoteViews
import android.widget.Toast
import java.io.InputStream
import java.net.URL
import java.text.SimpleDateFormat
import java.util.*

/**
 * Implementation of App Widget functionality.
 * App Widget Configuration implemented in [CaptureWidgetConfigureActivity][CaptureWidgetTestConfigure]
 */
class CaptureWidgetTest constructor() : AppWidgetProvider() {
    public override fun onUpdate(context: Context, appWidgetManager: AppWidgetManager, appWidgetIds: IntArray) {
        // There may be multiple widgets active, so update all of them
        for (appWidgetId: Int in appWidgetIds) {
            updateAppWidget(context, appWidgetManager, appWidgetId)
        }
    }

    public override fun onReceive(context: Context, intent: Intent) {
        super.onReceive(context, intent)
        val views: RemoteViews = RemoteViews(context.getPackageName(), R.layout.capture_widget_test)
        val appWidgetManager: AppWidgetManager = AppWidgetManager.getInstance(context)
        //        ComponentName thisAppWidget = new ComponentName(context.getPackageName(), CaptureWidget.class.getName());
        val thisAppWidget: ComponentName = ComponentName(context, CaptureWidgetTest::class.java)
        val appWidgets: IntArray = appWidgetManager.getAppWidgetIds(thisAppWidget)
        val action: String? = intent.getAction()
        assert(action != null)
        when (action) {
            AppWidgetManager.ACTION_APPWIDGET_UPDATE -> {
                ////          주기적 update
//            removePreviousAlarm();
//            long firstTime = System.currentTimeMillis() + WIDGET_UPDATE_INTERVAL;
//            mSender = PendingIntent.getBroadcast(context, 0, intent, 0);
//            mManager = (AlarmManager) context.getSystemService(Context.ALARM_SERVICE);
//            mManager.set(AlarmManager.RTC, firstTime, mSender);
////          주기적 update
                val extras: Bundle? = intent.getExtras()
                //Bundle 은 Key-Value 쌍으로 이루어진 일종의 해쉬맵 자료구조
                //한 Activity에서 Intent 에 putExtras로 Bundle 데이터를 넘겨주고,
                //다른 Activity에서 getExtras로 데이터를 참조하는 방식입니다.
                if (extras != null) {
                    val appWidgetIds: IntArray? = extras.getIntArray(AppWidgetManager.EXTRA_APPWIDGET_IDS)
                    if (appWidgetIds != null && appWidgetIds.size > 0) onUpdate(context, appWidgetManager, appWidgetIds)
                }
            }
            ACTION_BUTTON1 -> {
                Toast.makeText(context, "버튼을 클릭했어요.", Toast.LENGTH_LONG).show()
                onUpdate(context, appWidgetManager, appWidgets)
            }
            ACTION_BUTTON3 -> {
                Toast.makeText(context, "이미지를 교체 할께요.", Toast.LENGTH_SHORT).show()
                //AsyncTask를 이용해서 이미지를 가져와서 교체해 보자.
                val imgUrl: String = "https://img.fifa.com/image/upload/t_tc1/iimdtmzmdtirekoftruv.jpg"
                DownloadBitmap(views, appWidgets.get(0), appWidgetManager).execute(imgUrl) //AsyncTask 실행

//            appWidgetManager.updateAppWidget(new ComponentName(context, CaptureWidget.class), views);
                appWidgetManager.updateAppWidget(thisAppWidget, views)
            }
        }
        Log.e(TAG, "onReceive: " + action)
    }

    public override fun onDeleted(context: Context, appWidgetIds: IntArray) {
        // When the user deletes the widget, delete the preference associated with it.
        for (appWidgetId: Int in appWidgetIds) {
            CaptureWidgetTestConfigure.Companion.deleteTitlePref(context, appWidgetId)
        }
        removePreviousAlarm()
    }

    public override fun onEnabled(context: Context) {
        // Enter relevant functionality for when the first widget is created
    }

    public override fun onDisabled(context: Context) {
        // Enter relevant functionality for when the last widget is disabled
    }

    class DownloadBitmap constructor(
            private val views: RemoteViews,
            private val widgetID: Int,
            private val appWidgetManager: AppWidgetManager) : AsyncTask<String?, Void?, Bitmap?>() {

        override fun doInBackground(vararg params: String?): Bitmap? {
            //다운로드 받을 이미지 주소
            val url: String = params[0].toString()
            try {
                val `in`: InputStream = URL(url).openStream()
                val bitmap: Bitmap = BitmapFactory.decodeStream(`in`)
                Log.e("ImageDownload", "Download succeeded! " + params.get(0))
                return bitmap
            } catch (e: Exception) {
                Log.e("ImageDownload", "Download failed: " + e.message)
            }
            return null
        }

        override fun onPostExecute(bitmap: Bitmap?) {
            //그 결과를 가지고 화면에 출력
            var bitmap: Bitmap? = bitmap
            if (isCancelled()) {
                bitmap = null
            }
            views.setImageViewBitmap(R.id.imagePhotoView, bitmap)
            appWidgetManager.updateAppWidget(widgetID, views)
        }


    }

    fun removePreviousAlarm() {
        if (mManager != null && mSender != null) {
            mSender.cancel()
            mManager.cancel(mSender)
        }
    }

    companion object {
        private val ACTION_BUTTON1: String = "BUTTON1"
        private val ACTION_BUTTON3: String = "BUTTON3"
        private val TAG: String = "위젯"
        private val WIDGET_UPDATE_INTERVAL: Int = 5000
        private val mSender: PendingIntent? = null
        private val mManager: AlarmManager? = null
        fun updateAppWidget(context: Context, appWidgetManager: AppWidgetManager,
                            appWidgetId: Int) {
            val widgetText: CharSequence = CaptureWidgetTestConfigure.Companion.loadTitlePref(context, appWidgetId)
            // Construct the RemoteViews object
            val views: RemoteViews = RemoteViews(context.getPackageName(), R.layout.capture_widget_test)
            val time: String? = getDate("time")
            //        String day = getDate("day");
            views.setTextColor(R.id.appwidget_text, Color.WHITE)
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.JELLY_BEAN) {
                views.setViewPadding(R.id.appwidget_text, 8, 8, 8, 8)
            }
            views.setTextViewText(R.id.appwidget_text, widgetText)
            //        views.setTextViewText(R.id.appwidget_text, time);

            //랜덤 값을 만들어 화면에 출력해 보기
            val number: Int = (Random().nextInt(100))
            //        views.setViewPadding(R.id.message_text, 0, 8,0,8);
            views.setTextColor(R.id.message_text, Color.YELLOW)
            views.setTextViewText(R.id.message_text, number.toString())

            //시작되면서 지정 이미지로 교체
            views.setImageViewResource(R.id.imagePhotoView, R.drawable.snow)


            //버튼1 클릭 : 랜덤문자
            val intent1: Intent = Intent(context, CaptureWidgetTest::class.java) // Broadcast를 받을 class 지정 필요
            intent1.setAction(ACTION_BUTTON1)
            intent1.putExtra("viewID", R.id.button1)
            //버튼2 클릭 : 클릭하면 웹브라우저를 열어서 지정된 사이트를 보내 준다.
            val intent2: Intent = Intent(Intent.ACTION_VIEW, Uri.parse("http://m.welfare.mil.kr/main/index.html"))
            //        Intent intent2 = new Intent(Intent.ACTION_VIEW, Uri.parse("http://google.com"));
            //버튼3 클릭 : 이미지뷰에 비트맵 이미지를 교체해준다.
            val intent3: Intent = Intent(context, CaptureWidgetTest::class.java) // Broadcast를 받을 class 지정 필요
            intent3.setAction(ACTION_BUTTON3)
            val pendingIntent1: PendingIntent = PendingIntent.getBroadcast(context, 0, intent1, PendingIntent.FLAG_UPDATE_CURRENT)
            val pendingIntent2: PendingIntent = PendingIntent.getActivity(context, 0, intent2, 0)
            val pendingIntent3: PendingIntent = PendingIntent.getBroadcast(context, 0, intent3, PendingIntent.FLAG_UPDATE_CURRENT)
            views.setOnClickPendingIntent(R.id.button1, pendingIntent1)
            views.setOnClickPendingIntent(R.id.button2, pendingIntent2)
            views.setOnClickPendingIntent(R.id.button3, pendingIntent3)

            // Instruct the widget manager to update the widget
            appWidgetManager.updateAppWidget(appWidgetId, views)
            Log.d(TAG, "Update widget ID: " + appWidgetId)
        }

        @SuppressLint("SimpleDateFormat")
        private fun getDate(mode: String): String? {
            val mFormat: SimpleDateFormat
            if ((mode == "day")) {
                mFormat = SimpleDateFormat("yyyy/MM/dd")
                val now: Long = System.currentTimeMillis()
                val date: Date = Date(now)
                return mFormat.format(date)
            } else if ((mode == "time")) {
                mFormat = SimpleDateFormat("HH:mm")
                //            long now = System.currentTimeMillis();
                val date: Date = Date()
                return mFormat.format(date)
            }
            return null
        }
    }
}