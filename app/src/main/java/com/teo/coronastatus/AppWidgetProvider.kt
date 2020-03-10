package com.teo.coronastatus

import android.app.PendingIntent
import android.appwidget.AppWidgetManager
import android.appwidget.AppWidgetProvider
import android.content.ComponentName
import android.content.Context
import android.content.Intent
import android.net.Uri
import android.os.Handler
import android.util.Log
import android.widget.RemoteViews
import androidx.appcompat.app.AppCompatActivity
import okhttp3.*
import org.json.JSONObject
import java.io.IOException
import java.net.URL
import java.text.SimpleDateFormat
import java.util.*

private val TAG: String = com.teo.coronastatus.AppWidgetProvider::class.java.simpleName

class AppWidgetProvider : AppWidgetProvider() {

    //db에서 가져올 데이터를 담을 배열
    var today_array = Array(6, { "" })

    //코로나 관련 현황을 DB에서 가져올 메소드
    fun getStatus(context : Context?){

        //getString 메소드는 Context를 상속받기 때문에 Activity에서는 사용이 가능하다.
        //즉 현재 클래스에서 사용하기 위해서는 Context.getString() 형태로 사용해야 하기 때문에 context를 받아와서 사용한다.
        val url = URL(context!!.getString(R.string.status))
        val request = Request.Builder().url(url).build()
        val client = OkHttpClient()

        client.newCall(request).enqueue(object : Callback {
            override fun onResponse(call: Call, response: Response?) {
                //응답이 있을 경우 call은 무조건 null이 아니므로 ?를 쓰지 않는다.
                //json 형식으로 받아온 데이터를 until_yesterday, today 배열에 저장하고 해당하는 textview에 값을 넣어준다.
                //("not implemented") //To change body of created functions use File | Settings | File Templates.

                val body = response?.body()?.string()
//                Log.d(TAG, "Success to execute request! : $body")

                val jObject = JSONObject(body)
                val jArray = jObject.getJSONArray("board")

                for (i in 0 until jArray.length()) {
                    val obj = jArray.getJSONObject(i)

                    val today = obj.getString("today")
                    //today = db에 저장된 오늘 최종 인원수

                    today_array[i] = today

                    Log.d(TAG, "onResponse today_array[$i] : " + today_array[i])
//                    Log.d(TAG, "classification($i) : $classification")
                }

            }

            override fun onFailure(call: Call?, e: IOException?) {
                //("not implemented") //To change body of created functions use File | Settings | File Templates.
                println("Failed to get status Data from MainActivity")
            }
        })

    }

    //위젯에 있는 TextView들을 업데이트 해주는 메소드
    private fun updateAppWidgetTV(
        context: Context?,
        appWidgetManager: AppWidgetManager,
        appWidgetIds: IntArray?
    ) {

        //Calendar 객체 사용해서 현재시간 가져옴
        val mCalendar = Calendar.getInstance();
        val mFormat = SimpleDateFormat("yyyy-MM-dd HH:mm", Locale.KOREA);

        //RemoteViews 사용해서 현재시간(now_tv) 변경
        val updateViews = RemoteViews(context!!.getPackageName(), R.layout.widget_board);
        updateViews.setTextViewText(R.id.now_tv, mFormat.format(mCalendar.getTime()));

        //현재 코로나 현황 값 Db에서 가져오기
        getStatus(context)

        //클릭 시 setOnClickPendingIntent를 사용해서 MainActivity를 켜준다.
        val intent = Intent(context, MainActivity::class.java);
        val pendingIntent = PendingIntent.getActivity(context, 0, intent, 0);
        updateViews.setOnClickPendingIntent(R.id.widget_layout, pendingIntent);

        //핸들러를 사용하는 이유는 today_array 값을 가져올 때 getStatus() 메소드가 끝날때까지 기다렸다가 가져와야 빈 값이 들어있지 않기 때문
        Handler().postDelayed({

//            for (i in 0 until today_array.size)
//                Log.d(TAG, "inside handler today_array[$i] : " + today_array[i])

            //위젯에 넣을 데이터들 입력
            updateViews.setTextViewText(R.id.definite_num, today_array[0]);
            updateViews.setTextViewText(R.id.death_num, today_array[1]);
            updateViews.setTextViewText(R.id.recovery_num, today_array[2]);
            updateViews.setTextViewText(R.id.checked_num, today_array[3]);

            //위젯 업데이트
            appWidgetManager.updateAppWidget(appWidgetIds, updateViews);
        }, 1000)

    }

    override fun onReceive(context: Context?, intent: Intent?) {
        //브로드캐스트 리시버와 동일
        super.onReceive(context, intent)
    }

    override fun onUpdate(
        //위젯 갱신 주기에 따라 위젯 업데이트 시 사용
        //**처음 만들어질 때 사용되지 않으며 업데이트 시에만 사용된다.
        //현재 주기 30분
        context: Context,
        appWidgetManager: AppWidgetManager,
        appWidgetIds: IntArray
    ) {

        val appWidgetIds = appWidgetManager.getAppWidgetIds(ComponentName(context, javaClass));
        updateAppWidgetTV(context, appWidgetManager, appWidgetIds);

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