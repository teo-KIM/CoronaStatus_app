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

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_notification)

        //알람 날짜와 내용을 db에서부터 가져온다.
        getNotification()

        //어답터 설정
        //TODO : 현재 데이터는 잘 가져오지만 가져온 데이터를 리스트뷰와 연결하지 못함. 해결 요망
        //웹 프론트에서 알람 날린거 그대로 저장 할 수 있도록 간단한 페이지 및 로직 구성할것
        val listView = findViewById<ListView>(R.id.notification_lv)
        listView.adapter = MyCustomAdapter(this, dateList, contentList)

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
    }

    override fun onResume() {
        super.onResume()
        //액티비티 이동 시 넘어가는 애니메이션 삭제
        overridePendingTransition(0, 0);
    }

    private class MyCustomAdapter(context: Context, dateList : MutableList<String>, contentList : MutableList<String>) : BaseAdapter() {
        private val mContext: Context

        //listView에 표현 해 줄 알람 온 날짜 리스트
        private val dates = dateList
        //listView에 표현 해 줄 알람 내용 리스트
        private val contents = contentList

        init {
            mContext = context
        }

        override fun getCount(): Int {
            return dates.size
        }

        override fun getItemId(position: Int): Long {
            return position.toLong()
        }

        override fun getItem(position: Int): Any {
            val selectItem = dates.get(position)
            return selectItem
        }

        override fun getView(position: Int, view: View?, viewGroup: ViewGroup?): View {
            val layoutInflater = LayoutInflater.from(mContext)
            val rowMain = layoutInflater.inflate(R.layout.item_notification, viewGroup, false)

            val datesTv = rowMain.findViewById<TextView>(R.id.date_tv)
            datesTv.text = dates.get(position)
            Log.d(TAG, "position : "+position)
            val contentsTv = rowMain.findViewById<TextView>(R.id.content_tv)
            contentsTv.text = contents.get(position)

            return rowMain
        }
    }
}