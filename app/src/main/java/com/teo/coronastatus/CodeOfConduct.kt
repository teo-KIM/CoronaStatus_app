package com.teo.coronastatus

import android.content.Intent
import android.graphics.Color
import android.os.Build
import android.os.Bundle
import android.os.Handler
import android.webkit.WebView
import androidx.appcompat.app.AppCompatActivity
import kotlinx.android.synthetic.main.code_of_conduct.*
import okhttp3.*
import org.json.JSONObject
import java.io.IOException
import java.net.URL

class CodeOfConduct : AppCompatActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.code_of_conduct)

        //현재 CodeOfConduct에 있다는 것을 알려주기 위함
        diagnose_btn.setImageResource(R.drawable.doctor_click)
        diagnose_tv.setTextColor(Color.parseColor("#0321C6"))

        val myWebView = web_view as WebView
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.KITKAT) {
            WebView.setWebContentsDebuggingEnabled(true)
        }
        val settings = myWebView.settings
        settings.javaScriptEnabled = true
        settings.domStorageEnabled = true


        getUrl(myWebView)

        board_btn.setOnClickListener {
            val intent = Intent(this, MainActivity::class.java);
            intent.addFlags(Intent.FLAG_ACTIVITY_REORDER_TO_FRONT)
            startActivity(intent)
        }


        map_btn.setOnClickListener {
            val intent = Intent(this, ScreeningClinicMap::class.java);
            intent.addFlags(Intent.FLAG_ACTIVITY_REORDER_TO_FRONT)
            startActivity(intent)
        }

    }

    fun getUrl(myWebView: WebView) {

        val url = URL(getString(R.string.url))
        val request = Request.Builder().url(url).build()
        val client = OkHttpClient()

        var infoUrl: String = "https://www.google.com"

        client.newCall(request).enqueue(object : Callback {
            override fun onResponse(call: Call, response: Response?) {
                //응답이 있을 경우 call은 무조건 null이 아니므로 ?를 쓰지 않는다.
                //json 형식으로 받아온 데이터를 until_yesterday, today 배열에 저장하고 해당하는 textview에 값을 넣어준다.
                //("not implemented") //To change body of created functions use File | Settings | File Templates.

                val body = response?.body()?.string()
//                Log.d(TAG, "Success to execute request! : $body")

                val jObject = JSONObject(body)
                val jArray = jObject.getJSONArray("info_url")

                for (i in 0 until jArray.length()) {
                    val obj = jArray.getJSONObject(i)

                    infoUrl = obj.getString("url")

                }

            }

            override fun onFailure(call: Call?, e: IOException?) {
                //("not implemented") //To change body of created functions use File | Settings | File Templates.
                println("Failed to execute request!")
            }
        })

        Handler().postDelayed({
            myWebView.loadUrl(infoUrl)
        }, 300)


    }

    //back 버튼 누를 시 현황판 (Main) 액티비티로 이동
    override fun onBackPressed() {
        super.onBackPressed()
        val intent = Intent(this, MainActivity::class.java);
        intent.addFlags(Intent.FLAG_ACTIVITY_REORDER_TO_FRONT)
        startActivity(intent)
    }
}
