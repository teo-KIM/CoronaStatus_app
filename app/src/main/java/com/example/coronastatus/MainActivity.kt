package com.example.coronastatus

import android.content.Intent
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.util.Log
import kotlinx.android.synthetic.main.activity_main.*
import okhttp3.*
import org.json.JSONObject
import java.io.IOException
import java.net.URL

private val TAG: String = MainActivity::class.java.simpleName

class MainActivity : AppCompatActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)
        fetchJson()

        map_btn.setOnClickListener {
            val intent = Intent(this, ScreeningClinicMap::class.java);
            startActivity(intent)
        }

        refresh_lottie.setOnClickListener {
            fetchJson()
            refresh_lottie.playAnimation()
        }

    }

    fun fetchJson() {

        val url = URL("https://www.portfoliobyteo.kro.kr/getjson.php")
        val request = Request.Builder().url(url).build()
        val client = OkHttpClient()

        client.newCall(request).enqueue(object : Callback {
            override fun onResponse(call: Call, response: Response?) {
                //응답이 있을 경우 call은 무조건 null이 아니므로 ?를 쓰지 않는다.
                //json 형식으로 받아온 데이터를 until_yesterday와 today 배열에 저장하고 해당하는 textview에 값을 넣어준다.
                //TODO("not implemented") //To change body of created functions use File | Settings | File Templates.

                var until_yesterday_array = Array(6, { 0 })
                var today_array = Array(6, { 0 })

                val body = response?.body()?.string()
//                Log.d(TAG, "Success to execute request! : $body")

                val jObject = JSONObject(body)
                val jArray = jObject.getJSONArray("board")

                for (i in 0 until jArray.length()) {
                    val obj = jArray.getJSONObject(i)
                    val until_yesterday = obj.getInt("until_yesterday")
                    val today = obj.getInt("today")
//                    when (i) {
//                        0 -> definite.setText(today)
//                    }

                    val until_yesterday_int = obj.getInt("until_yesterday")
                    //until_yesterday = db에 저장된 여태까지의 누계 인원수

                    val today_int = obj.getInt("today")
                    //today = db에 저장된 오늘 최종 인원수

                    until_yesterday_array.set(i, until_yesterday_int)
                    today_array.set(i, today_int)
//                    Log.d(TAG, "classification($i) : $classification")
                }
                for (i in 0 until until_yesterday_array.size) {
                    println("어제까지 : " + until_yesterday_array[i])
                    println("오늘 최종 : " + today_array[i])
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
    }

}


//data class 생성 후 전체 json 데이터를 한번에 파싱 하려고 했으나 데이터를 가져올 때 바로 파싱하는 것으로 로직 변경
//data class JsonObj(val result : List<StatusData>)
//data class StatusData (val idx : String, val classification : String, val until_yesterday : Int, val today : Int)
