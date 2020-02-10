package com.teo.coronastatus

import android.content.Intent
import android.graphics.Color
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.os.Handler
import com.google.firebase.iid.FirebaseInstanceId
import kotlinx.android.synthetic.main.activity_main.*
import okhttp3.*
import org.json.JSONObject
import java.io.IOException
import java.lang.NullPointerException
import java.net.URL
import java.text.SimpleDateFormat
import java.util.*

private val TAG: String = MainActivity::class.java.simpleName

class MainActivity : AppCompatActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        try{
            val token = FirebaseInstanceId.getInstance().getToken()
//            Log.d(TAG, "device token : " + token)

        }catch (e:NullPointerException){
            e.printStackTrace()
        }
        val now = System.currentTimeMillis()
        val date = Date(now)
        val dateNow = SimpleDateFormat("yyyy/MM/dd HH:mm:ss")
        val formatDate = dateNow.format(date)
        now_tv.setText(formatDate)

        fetchJson()

        //현재 MainActivity에 있다는 것을 알려주기 위함
        board_btn.setImageResource(R.drawable.board_click)
        board_tv.setTextColor(Color.parseColor("#0d64b2"))

        map_btn.setOnClickListener {
            val intent = Intent(this, ScreeningClinicMap::class.java);
            startActivity(intent)
        }

        diagnose_btn.setOnClickListener {
            val intent = Intent(this, CodeOfConduct::class.java);
            startActivity(intent)
        }

        refresh_lottie.setOnClickListener {
            fetchJson()
            refresh_lottie.playAnimation()
            val now = System.currentTimeMillis()
            val date = Date(now)
            val dateNow = SimpleDateFormat("yyyy/MM/dd HH:mm:ss")
            val formatDate = dateNow.format(date)
            now_tv.setText(formatDate)
        }

    }

    fun fetchJson() {

        val url = URL("https://www.portfoliobyteo.kro.kr/getjson.php")
        val request = Request.Builder().url(url).build()
        val client = OkHttpClient()

        //db에서 가져올 데이터를 담을 배열
        var until_yesterday_array = Array(6, { 0 })
        var today_array = Array(6, { 0 })

        client.newCall(request).enqueue(object : Callback {
            override fun onResponse(call: Call, response: Response?) {
                //응답이 있을 경우 call은 무조건 null이 아니므로 ?를 쓰지 않는다.
                //json 형식으로 받아온 데이터를 until_yesterday, today 배열에 저장하고 해당하는 textview에 값을 넣어준다.
                //TODO("not implemented") //To change body of created functions use File | Settings | File Templates.

                val body = response?.body()?.string()
//                Log.d(TAG, "Success to execute request! : $body")

                val jObject = JSONObject(body)
                val jArray = jObject.getJSONArray("board")

                for (i in 0 until jArray.length()) {
                    val obj = jArray.getJSONObject(i)

                    val until_yesterday = obj.getInt("until_yesterday")
                    //until_yesterday = db에 저장된 여태까지의 누계 인원수

                    val today = obj.getInt("today")
                    //today = db에 저장된 오늘 최종 인원수

                    until_yesterday_array[i]=until_yesterday
                    today_array[i]=today

//                    Log.d(TAG, "today_array[$i] : "+ today_array[i])
//                    Log.d(TAG, "classification($i) : $classification")
                }

                //data class 생성 후 전체 json 데이터를 한번에 파싱 하려고 했으나 데이터를 가져올 때 바로 파싱하는 것으로 로직 변경
                //Gson으로 파싱
//                val gson = GsonBuilder().create()
//                val list = gson.fromJson(body, JsonObj::class.java)

                //결과가 나오면 바로 텍스트뷰에 넣어야됨
//                Log.d(TAG, list.result[0].classification)
//                Log.d(TAG, "$list")

            }

            override fun onFailure(call: Call?, e: IOException?) {
                //TODO("not implemented") //To change body of created functions use File | Settings | File Templates.
                println("Failed to execute request!")
            }
        })

            Handler().postDelayed({
                for (i in 0 until today_array.size) {
                    when (i) {
                        0 -> definite.setText(today_array[i].toString())
                        1 -> death.setText(today_array[i].toString())
                        2 -> recovery.setText(today_array[i].toString())
                        3 -> isolated.setText(today_array[i].toString())
                        4 -> released.setText(today_array[i].toString())
                        5 -> symptom.setText(today_array[i].toString())
                    }
                }
            }, 400)



    }

}



//data class 생성 후 전체 json 데이터를 한번에 파싱 하려고 했으나 데이터를 가져올 때 바로 파싱하는 것으로 로직 변경
//data class JsonObj(val result : List<StatusData>)
//data class StatusData (val idx : String, val classification : String, val until_yesterday : Int, val today : Int)
