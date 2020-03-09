package com.teo.coronastatus

import android.app.PendingIntent
import android.appwidget.AppWidgetManager
import android.appwidget.AppWidgetProvider
import android.content.Context
import android.content.Intent
import android.widget.RemoteViews

class AppWidgetProvider : AppWidgetProvider() {

    /*fun updateAppWidget(
        context: Context, appWidgetManager: AppWidgetManager,
        appWidgetId: Int
    ) {
        val widgetText: CharSequence = context.getString(R.string.appwidget_text)
        // Construct the RemoteViews object
        val views = RemoteViews(context.packageName, R.layout.widget_board)
        views.setTextViewText(R.id.appwidget_text, widgetText)
        // Instruct the widget manager to update the widget
        appWidgetManager.updateAppWidget(appWidgetId, views)
    }*/

    override fun onReceive(context: Context?, intent: Intent?) {
        //브로드캐스트 리시버와 동일
        super.onReceive(context, intent)
    }
    override fun onUpdate(
        //위젯 갱신 주기에 따라 위젯 업데이트 시 사용
        //**처음 만들어질 때 사용되지 않으며 업데이트 시에만 사용된다.
        context: Context,
        appWidgetManager: AppWidgetManager,
        appWidgetIds: IntArray
    ) {
        // Perform this loop procedure for each App Widget that belongs to this provider
        appWidgetIds.forEach { appWidgetId ->
            // Create an Intent to launch MainActivity
            val pendingIntent: PendingIntent = Intent(context, MainActivity::class.java)
                .let { intent ->
                    PendingIntent.getActivity(context, 0, intent, 0)
                }

            // Get the layout for the App Widget and attach an on-click listener
            // to the button
            val views: RemoteViews = RemoteViews(
                context.packageName,
                R.layout.widget_board
            ).apply {
//                setOnClickPendingIntent(R.id.refresh_IB, pendingIntent)
            }

            // Tell the AppWidgetManager to perform an update on the current app widget
            appWidgetManager.updateAppWidget(appWidgetId, views)
        }
    }

    override fun onEnabled(context: Context?) {
        //위젯이 처음 생성될 때 사용된다.
        super.onEnabled(context)
    }

    override fun onDisabled(context: Context?) {
        //위젯의 마지막 인스턴스가 제거될 때 사용된다.
        super.onDisabled(context)
    }

    override fun onDeleted(context: Context?, appWidgetIds: IntArray?) {
        //위젯이 사용자에 의해 제거될 때 호출된다.
        super.onDeleted(context, appWidgetIds)
    }
}