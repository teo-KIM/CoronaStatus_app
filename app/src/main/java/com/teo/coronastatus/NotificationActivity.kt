package com.teo.coronastatus

import android.content.Context
import android.content.Intent
import android.graphics.Color
import android.os.Bundle
import android.os.Handler
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.BaseAdapter
import android.widget.ListView
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity
import kotlinx.android.synthetic.main.activity_notification.*
import okhttp3.*
import org.json.JSONObject
import java.io.IOException
import java.net.URL

private val TAG: String = NotificationActivity::class.java.simpleName

class NotificationActivity : AppCompatActivity() {

    //db에서 가져올 데이터를 담을 배열
    //getNotification()에서 사용한다.
    val dateList = mutableListOf<String>()
    val contentList = mutableListOf<String>()

    lateinit var listView : ListView

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_notification)

        listView = findViewById(R.id.notification_lv)

        //알람 날짜와 내용을 db에서부터 가져온다.
        getNotification()

//        Log.d(TAG, "dateList : "+dateList)
//        Log.d(TAG, "contentList : "+contentList)


        //아이템 클릭 리스너
        /*listView.onItemClickListener =
            AdapterView.OnItemClickListener { parent, view, position, id ->
                val selectItem = parent.getItemAtPosition(position) as String
                selectName.text = selectItem
                //Toast.makeText(this, selectItem, Toast.LENGTH_SHORT).show()
            }*/

        //바텀 네비게이션 기능
        board_btn.setOnClickListener {
            val intent = Intent(this, MainActivity::class.java)
            intent.addFlags(Intent.FLAG_ACTIVITY_REORDER_TO_FRONT)
            startActivity(intent)
        }

        map_btn.setOnClickListener {
            val intent = Intent(this, ScreeningClinicMapActivity::class.java)
            intent.addFlags(Intent.FLAG_ACTIVITY_REORDER_TO_FRONT)
            startActivity(intent)
        }

        //현재 MainActivity에 있다는 것을 알려주기 위해 바텀 네비게이션에 현황판 이미지를 바꿔준다.
        diagnose_btn.setImageResource(R.drawable.ic_notifications_click_24dp)
        diagnose_tv.setTextColor(Color.parseColor("#0321C6"))
    }

    fun updateNotification(){
        //TODO 알람 내용 업데이트 될 경우 onResume 에서 실행 될 수 있도록 하는 메소드
    }



    fun getNotification() {
        val url = URL(getString(R.string.notification))
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
                val jArray = jObject.getJSONArray("notification")

                for (i in 0 until jArray.length()) {
                    val obj = jArray.getJSONObject(i)

                    val date = obj.getString("date")
                    //date = db에 저장된 알람 보낸 날짜 / 시간

                    val content = obj.getString("content")
                    //content = db에 저장된 알람 내용

                    dateList.add(date)
                    contentList.add(content)

//                    Log.d(TAG, "dateList[$i] : "+ dateList[i])
//                    Log.d(TAG, "contentList($i) : " + contentList[i])
                }


            }

            override fun onFailure(call: Call?, e: IOException?) {
                //("not implemented") //To change body of created functions use File | Settings | File Templates.
                println("Failed to get notification from NotificationActivity.kt")
            }


        })
        Handler().postDelayed({
            listView.adapter = MyCustomAdapter(this@NotificationActivity, dateList, contentList)
        },500)

    }

    override fun onResume() {
        super.onResume()
        //액티비티 이동 시 넘어가는 애니메이션 삭제
        overridePendingTransition(0, 0);

        updateNotification()
    }


}