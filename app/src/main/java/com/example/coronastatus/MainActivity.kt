package com.example.coronastatus

import android.content.Intent
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.util.Log
import com.google.gson.GsonBuilder
import com.google.gson.JsonParser

import kotlinx.android.synthetic.main.activity_main.*
import okhttp3.*
import org.json.JSONObject
import java.io.IOException
import java.net.URL

private val TAG : String = MainActivity::class.java.simpleName
class MainActivity : AppCompatActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)
        fetchJson()

        map_btn.setOnClickListener {
            val intent = Intent(this, ScreeningClinicMap::class.java);
            startActivity(intent)
        }

        refresh.setOnClickListener {
            fetchJson()
        }

    }

    fun fetchJson(){
        val url = URL("https://www.portfoliobyteo.kro.kr/getjson.php")
        val request = Request.Builder().url(url).build()
        val client = OkHttpClient()

        client.newCall(request).enqueue(object : Callback{
            override fun onResponse(call: Call?, response: Response?) {
                //TODO("not implemented") //To change body of created functions use File | Settings | File Templates.
                val body = response?.body()?.string()
                println("Success to execute request! : $body")

                val jObject = JSONObject(body)
                val jArray = jObject.getJSONArray("board")

                for(i in 0 until jArray.length()){
                    val obj = jArray.getJSONObject(i)
                    val classification = obj.getString("classification")
                    Log.d(TAG, "classification($i) : $classification")
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

//TODO
//카카오맵 api 다운로드 후 적용해볼것
//run.js 응용해서 환자 수 크롤링 후 DB에 추가 -> 안드로이드에서 불러오기
