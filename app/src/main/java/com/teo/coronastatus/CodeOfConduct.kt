package com.teo.coronastatus

import android.content.Intent
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
            startActivity(intent)
        }


        map_btn.setOnClickListener {
            val intent = Intent(this, ScreeningClinicMap::class.java);
            startActivity(intent)
        }

    }

    fun getUrl(myWebView: WebView) {

        val url = URL("https://www.portfoliobyteo.kro.kr/getUrl.php")
        val request = Request.Builder().url(url).build()
        val client = OkHttpClient()

        var infoUrl: String = "https://www.google.com"

        client.newCall(request).enqueue(object : Callback {
            override fun onResponse(call: Call, response: Response?) {
                //응답이 있을 경우 call은 무조건 null이 아니므로 ?를 쓰지 않는다.
                //json 형식으로 받아온 데이터를 until_yesterday, today 배열에 저장하고 해당하는 textview에 값을 넣어준다.
                //TODO("not implemented") //To change body of created functions use File | Settings | File Templates.

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
                //TODO("not implemented") //To change body of created functions use File | Settings | File Templates.
                println("Failed to execute request!")
            }
        })

        Handler().postDelayed({
            myWebView.loadUrl(infoUrl)
        }, 300)


    }
}
