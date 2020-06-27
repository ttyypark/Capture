package com.example.capture;

import android.app.AlarmManager;
import android.app.PendingIntent;
import android.appwidget.AppWidgetManager;
import android.appwidget.AppWidgetProvider;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Color;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Build;
import android.os.Bundle;
import android.provider.MediaStore;
import android.util.Log;
import android.widget.RemoteViews;
import android.widget.Toast;

import java.io.InputStream;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Random;

/**
 * Implementation of App Widget functionality.
 * App Widget Configuration implemented in {@link CaptureWidgetConfigureActivity CaptureWidgetConfigureActivity}
 */
public class CaptureWidget extends AppWidgetProvider {
    private static final String ACTION_BUTTON1 = "BUTTON1";
    private static final String ACTION_BUTTON3 = "BUTTON3";
    private static final String TAG = "위젯";
    private static final int WIDGET_UPDATE_INTERVAL = 5000;
    private static PendingIntent mSender;
    private static AlarmManager mManager;

    static void updateAppWidget(Context context, AppWidgetManager appWidgetManager,
                                int appWidgetId) {

        CharSequence widgetText = CaptureWidgetConfigureActivity.loadTitlePref(context, appWidgetId);
        // Construct the RemoteViews object
        RemoteViews views = new RemoteViews(context.getPackageName(), R.layout.capture_widget);

        String time = getDate("time");
//        String day = getDate("day");

        views.setTextColor(R.id.appwidget_text, Color.WHITE);
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.JELLY_BEAN) {
            views.setViewPadding(R.id.appwidget_text, 8, 8, 8, 8);
        }
        views.setTextViewText(R.id.appwidget_text, widgetText);
//        views.setTextViewText(R.id.appwidget_text, time);

        //랜덤 값을 만들어 화면에 출력해 보기
        int number = (new Random().nextInt(100));
//        views.setViewPadding(R.id.message_text, 0, 8,0,8);
        views.setTextColor(R.id.message_text, Color.YELLOW);
        views.setTextViewText(R.id.message_text, String.valueOf(number));

        //시작되면서 지정 이미지로 교체
        views.setImageViewResource(R.id.imageView, R.drawable.snow);


        //버튼1 클릭 : 클릭 성공 메세지 출력!
//        Intent intent1 = new Intent(ACTION_BUTTON1);
        Intent intent1 = new Intent(context, CaptureWidget.class); // Broadcast를 받을 class 지정 필요
        intent1.setAction(ACTION_BUTTON1);
        intent1.putExtra("viewID", R.id.button1);
//        PendingIntent pendingIntent1 = PendingIntent.getBroadcast(context, 0, intent1, PendingIntent.FLAG_CANCEL_CURRENT);
        PendingIntent pendingIntent1 = PendingIntent.getBroadcast(context, 0, intent1, PendingIntent.FLAG_UPDATE_CURRENT);
        views.setOnClickPendingIntent(R.id.button1, pendingIntent1);

        //버튼2 클릭 : 클릭하면 웹브라우저를 열어서 지정된 사이트를 보내 준다.
        Intent intent2 = new Intent(Intent.ACTION_VIEW, Uri.parse("http://google.com"));
        PendingIntent pendingIntent2 = PendingIntent.getActivity(context, 0, intent2, 0);
        views.setOnClickPendingIntent(R.id.button2, pendingIntent2);

        //버튼3 클릭 : 이미지뷰에 비트맵 이미지를 교체해준다.
        Intent intent3 = new Intent(context, CaptureWidget.class); // Broadcast를 받을 class 지정 필요
        intent3.setAction(ACTION_BUTTON3);
        PendingIntent pendingIntent3 = PendingIntent.getBroadcast(context, 0, intent3, PendingIntent.FLAG_UPDATE_CURRENT);
        views.setOnClickPendingIntent(R.id.button3, pendingIntent3);

        // Instruct the widget manager to update the widget
        appWidgetManager.updateAppWidget(appWidgetId, views);
        Log.d(TAG, "Update widget ID: " + String.valueOf(appWidgetId));
    }

    @Override
    public void onUpdate(Context context, AppWidgetManager appWidgetManager, int[] appWidgetIds) {
        // There may be multiple widgets active, so update all of them
        for (int appWidgetId : appWidgetIds) {
            updateAppWidget(context, appWidgetManager, appWidgetId);
        }
    }

    @Override
    public void onReceive(Context context, Intent intent) {
        super.onReceive(context, intent);

        RemoteViews views = new RemoteViews(context.getPackageName(), R.layout.capture_widget);
        AppWidgetManager appWidgetManager = AppWidgetManager.getInstance(context);
//        ComponentName thisAppWidget = new ComponentName(context.getPackageName(), CaptureWidget.class.getName());
        ComponentName thisAppWidget = new ComponentName(context, CaptureWidget.class);
        int[] appWidgets = appWidgetManager.getAppWidgetIds(thisAppWidget);

        final String action = intent.getAction();
        if(action.equals(AppWidgetManager.ACTION_APPWIDGET_UPDATE)) {
////          주기적 update
//            removePreviousAlarm();
//            long firstTime = System.currentTimeMillis() + WIDGET_UPDATE_INTERVAL;
//            mSender = PendingIntent.getBroadcast(context, 0, intent, 0);
//            mManager = (AlarmManager) context.getSystemService(Context.ALARM_SERVICE);
//            mManager.set(AlarmManager.RTC, firstTime, mSender);
////          주기적 update

            Bundle extras = intent.getExtras();
            //Bundle 은 Key-Value 쌍으로 이루어진 일종의 해쉬맵 자료구조
            //한 Activity에서 Intent 에 putExtras로 Bundle 데이터를 넘겨주고,
            //다른 Activity에서 getExtras로 데이터를 참조하는 방식입니다.
            if(extras!=null)
            {
                int [] appWidgetIds = extras.getIntArray(AppWidgetManager.EXTRA_APPWIDGET_IDS);
                if(appWidgetIds!=null && appWidgetIds.length>0)
                    this.onUpdate(context, appWidgetManager, appWidgetIds);
            }
        }
        else if(action.equals(ACTION_BUTTON1)) {

            Toast.makeText(context, "버튼을 클릭했어요.", Toast.LENGTH_LONG).show();
            onUpdate(context, appWidgetManager, appWidgets);
        }
        else if(action.equals(ACTION_BUTTON3)){
            Toast.makeText(context, "이미지를 교체 할께요.", Toast.LENGTH_SHORT).show();
            //AsyncTask를 이용해서 이미지를 가져와서 교체해 보자.
            String imgUrl = "https://img.fifa.com/image/upload/t_tc1/iimdtmzmdtirekoftruv.jpg";
            new DownloadBitmap(views, appWidgets[0], appWidgetManager).execute(imgUrl); //AsyncTask 실행
//            appWidgetManager.updateAppWidget(new ComponentName(context, CaptureWidget.class), views);
            appWidgetManager.updateAppWidget(thisAppWidget, views);
        }
        Log.e(TAG, "onReceive: " + action);
    }

    @Override
    public void onDeleted(Context context, int[] appWidgetIds) {
        // When the user deletes the widget, delete the preference associated with it.
        for (int appWidgetId : appWidgetIds) {
            CaptureWidgetConfigureActivity.deleteTitlePref(context, appWidgetId);
        }
        removePreviousAlarm();
    }

    @Override
    public void onEnabled(Context context) {
        // Enter relevant functionality for when the first widget is created
    }

    @Override
    public void onDisabled(Context context) {
        // Enter relevant functionality for when the last widget is disabled
    }

    public class DownloadBitmap extends AsyncTask<String, Void, Bitmap> {

        private RemoteViews views;
        private int widgetID;
        private AppWidgetManager appWidgetManager;

        public DownloadBitmap(RemoteViews views, int appWidgetID, AppWidgetManager appWidgetManager) {
            this.views = views;
            this.widgetID = appWidgetID;
            this.appWidgetManager = appWidgetManager;
        }

        @Override
        protected void onPreExecute() {
            super.onPreExecute();
        }

        @Override
        protected Bitmap doInBackground(String... params) {
            //다운로드 받을 이미지 주소
            String url = params[0];

            try {
                InputStream in = new java.net.URL(url).openStream();
                Bitmap bitmap = BitmapFactory.decodeStream(in);
                Log.e("ImageDownload", "Download succeeded! " + params[0]);
                return bitmap;
            } catch (Exception e) {
                Log.e("ImageDownload", "Download failed: " + e.getMessage());
            }
            return null;
        }

        @Override
        protected void onPostExecute(Bitmap bitmap) {
            //그 결과를 가지고 화면에 출력
            if(isCancelled()) {
                bitmap = null;
            }
            views.setImageViewBitmap(R.id.imageView, bitmap);
            appWidgetManager.updateAppWidget(widgetID, views);

        }
    }

    static private String getDate(String mode){
        SimpleDateFormat mFormat;
        if(mode == "day") {
            mFormat = new SimpleDateFormat("yyyy/MM/dd");
            long now = System.currentTimeMillis();
            Date date = new Date(now);
            return mFormat.format(date);
        } else if (mode == "time") {
            mFormat = new SimpleDateFormat("HH:mm");
//            long now = System.currentTimeMillis();
            Date date = new Date();
            return mFormat.format(date);
        }
        return null;
    }

    public void removePreviousAlarm()
    {
        if(mManager != null && mSender != null)
        {
            mSender.cancel();
            mManager.cancel(mSender);
        }
    }
}

