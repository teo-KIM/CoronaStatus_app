package com.teo.coronastatus

import android.app.Activity
import android.app.AlertDialog
import android.content.Intent
import android.graphics.Color
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.os.Handler
import android.util.Log
import android.view.View
import com.google.firebase.iid.FirebaseInstanceId
import kotlinx.android.synthetic.main.activity_main.*
import okhttp3.*
import org.json.JSONObject
import java.io.IOException
import java.lang.NullPointerException
import java.net.URL
import java.text.SimpleDateFormat
import java.util.*
import android.widget.Toast
import android.widget.Toast.makeText
import com.google.android.play.core.appupdate.AppUpdateInfo
import com.google.android.play.core.appupdate.AppUpdateManager
import com.google.android.play.core.appupdate.AppUpdateManagerFactory
import com.google.android.play.core.install.model.ActivityResult
import com.google.android.play.core.install.model.AppUpdateType
import com.google.android.play.core.install.model.UpdateAvailability


private val TAG: String = MainActivity::class.java.simpleName

class MainActivity : AppCompatActivity() {

    val MY_REQUEST_CODE = 301
    private lateinit var appUpdateManager: AppUpdateManager

    //back 버튼 누를 때 누른 시간을 담을 변수
    //onBackPressed() 에서 사용
    var second_time = 0L
    var first_time = 0L

    override fun onCreate(savedInstanceState: Bundle?) {
//        Log.d(TAG, "onCreate")
//        Log.d(TAG, "onCreate에서 function을 가지고 있는지 : " + intent.hasExtra("function"))
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        appUpdateManager = AppUpdateManagerFactory.create(this)

        appUpdateManager.appUpdateInfo.addOnSuccessListener {
            if (it.updateAvailability() == UpdateAvailability.UPDATE_AVAILABLE &&
                it.isUpdateTypeAllowed(AppUpdateType.IMMEDIATE)
            ) {   //  check for the type of update flow you want
                requestUpdate(appUpdateInfo = null)
            }
        }

        try {
            //FCM 사용을 위해 사용자 핸드폰의 토큰을 가져온다.
            //가져온 토큰을 사용하지는 않지만 해당 과정이 없으면 에러가 나는 경우가 있음.
            val token = FirebaseInstanceId.getInstance().token
//            Log.d(TAG, "device token : " + token)
        } catch (e: NullPointerException) {
            e.printStackTrace()
        }

        //알람을 눌러서 들어왔을 경우 해당 알람 내용을 다이얼로그로 한번 더 알려준다.
        //onCreate에 넣은 이뉴는 onResume, onStart의 경우 onDestroy 이전에 1회 이상 불러질 가능성이 있기 때문.
        //-> 1회 이상 불러지는 경우 불러질 때 마다 다이얼로그가 뜨게 된다.
        if (intent.hasExtra("function")) {
            val function: String? = intent.getStringExtra("function")

//            Log.d(TAG, "title : " + intent.getStringExtra("title"))
//            Log.d(TAG, "body : " + intent.getStringExtra("body"))
            when (function) {
                //알람을 눌러서 들어온 경우
                "activity_notification" -> {
//                    Log.d(TAG, "function : " + intent.getStringExtra("function"))
                    val builder = AlertDialog.Builder(this@MainActivity)

                    //이미 intent에 있는 function 값으로 알람을 눌러서 들어온 것이 확인되었기 때문에 body, title은 무조건 null이 아니라고 판단. !!를 추가한다.
                    builder.setMessage(intent.getStringExtra("body"))!!
                        //확인 이외에 다른 선택지는 필요 없기 때문에 positiveButton만 사용함. 확인 버튼 누를 경우 다이얼로그 없어지도록 함
                        .setPositiveButton("확인", { dialog, id -> dialog.cancel() })

                    val alert = builder.create()
                    alert.setTitle(intent.getStringExtra("title"))
                    alert.show()
                }
                else -> {
                    //다른 기능이 추가되면 필요함
                }
            }
        }

        //현재 MainActivity에 있다는 것을 알려주기 위해 바텀 네비게이션에 현황판 이미지를 바꿔준다.
        board_btn.setImageResource(R.drawable.board_click)
        board_tv.setTextColor(Color.parseColor("#0321C6"))

        //바텀 네비게이션 기능
        map_btn.setOnClickListener {
            val intent = Intent(this, ScreeningClinicMapActivity::class.java)
            intent.addFlags(Intent.FLAG_ACTIVITY_REORDER_TO_FRONT)
            startActivity(intent)
        }

        diagnose_btn.setOnClickListener {
            val intent = Intent(this, NotificationActivity::class.java)
            intent.addFlags(Intent.FLAG_ACTIVITY_REORDER_TO_FRONT)
            startActivity(intent)
        }

        refresh_lottie.setOnClickListener {
            //새로고침(로띠) 버튼 클릭 시 현황판을 업데이트 해주고 마지막 업데이트 시간으로 현재 시간을 나타내준다.
            fetchJson()
        }
    }

    fun setLastUpdateTime() {
        //새로고침 버튼을 누를 경우 마지막 업데이트(새로고침) 시간을 알려준다.
        second_time = System.currentTimeMillis()
        val date = Date(second_time)
        val dateNow = SimpleDateFormat("yyyy/MM/dd HH:mm:ss")
        val formatDate = dateNow.format(date)
        now_tv.text = formatDate
    }

    companion object {
        private const val REQUEST_CODE_FLEXI_UPDATE = 173
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        if (requestCode == REQUEST_CODE_FLEXI_UPDATE) {
            when (resultCode) {
                Activity.RESULT_OK -> { //  handle user's approval

                }
                Activity.RESULT_CANCELED -> { //  handle user's rejection
                    Log.d(TAG, "Update flow canceled! Result code: $resultCode ")

                }
                ActivityResult.RESULT_IN_APP_UPDATE_FAILED -> {  //  handle update failure
                    Log.d(TAG, "Update flow failed! Result code: $resultCode ")
                    requestUpdate(appUpdateInfo = null)
                }

            }
        }
    }


    private fun requestUpdate(appUpdateInfo: AppUpdateInfo?) {
        appUpdateManager.startUpdateFlowForResult(
            appUpdateInfo,
            AppUpdateType.IMMEDIATE, //  HERE specify the type of update flow you want
            this,   //  the instance of an activity
            REQUEST_CODE_FLEXI_UPDATE
        )
    }

    override fun onResume() {
//        Log.d(TAG, "onResume")
        super.onResume()
        overridePendingTransition(0, 0);

        //DB에서 현재 국내 코로나 현황을 가져오는 메소드
        //onCreate가 아닌 onResume인 이유는 1회만 실행하는 게 아닌 다른 액티비티로부터 넘어왔을때도 자동으로 새로고침을 해주기 위해서
        fetchJson()

        appUpdateManager.appUpdateInfo.addOnSuccessListener {
            if (it.updateAvailability() == UpdateAvailability.DEVELOPER_TRIGGERED_UPDATE_IN_PROGRESS) {
                appUpdateManager.startUpdateFlowForResult(
                    it,
                    AppUpdateType.IMMEDIATE,
                    this,
                    REQUEST_CODE_FLEXI_UPDATE
                )
            }
        }

    }

    fun fetchJson() {
        //코로나 바이러스 국내 현황을 업데이트 해주는 메소드
        //새로고침 로띠를 실행해주고 데이터를 불러오는 1초동안 현황을 나타내주는 숫자(TextView) 대신 로딩 로띠를 띄워준다. -> Visibility로 관리한다.
        refresh_lottie.playAnimation()

        definite.visibility = View.GONE
        recovery.visibility = View.GONE
        death.visibility = View.GONE
        isolated.visibility = View.GONE

        loader_red.visibility = View.VISIBLE
        loader_red.playAnimation()
        loader_green.visibility = View.VISIBLE
        loader_green.playAnimation()
        loader_black.visibility = View.VISIBLE
        loader_black.playAnimation()
        loader_yellow.visibility = View.VISIBLE
        loader_yellow.playAnimation()

        val url = URL(getString(R.string.status))
        val request = Request.Builder().url(url).build()
        val client = OkHttpClient()

        //db에서 가져올 데이터를 담을 배열
        var until_yesterday_array = Array(6, { "" })
        var today_array = Array(6, { "" })

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

                    val until_yesterday = obj.getString("until_yesterday")
                    //until_yesterday = db에 저장된 여태까지의 누계 인원수

                    val today = obj.getString("today")
                    //today = db에 저장된 오늘 최종 인원수

                    until_yesterday_array[i] = until_yesterday
                    today_array[i] = today

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
                //("not implemented") //To change body of created functions use File | Settings | File Templates.
                println("Failed to get status Data from MainActivity")
            }
        })

        Handler().postDelayed({
            for (i in 0 until today_array.size) {
                when (i) {
                    0 -> definite.text = today_array[i].toString()
                    1 -> death.text = today_array[i].toString()
                    2 -> recovery.text = today_array[i].toString()
                    3 -> isolated.text = today_array[i].toString()
                    4 -> released.text = today_array[i].toString()
                    5 -> symptom.text = today_array[i].toString()
                }
            }

            loader_red.visibility = View.GONE
            loader_green.visibility = View.GONE
            loader_black.visibility = View.GONE
            loader_yellow.visibility = View.GONE

            definite.visibility = View.VISIBLE
            recovery.visibility = View.VISIBLE
            death.visibility = View.VISIBLE
            isolated.visibility = View.VISIBLE

            setLastUpdateTime()

        }, 1000)

    }

    override fun onBackPressed() {
        //toast.cancel()이 있는데도 불구하고 토스트 메세지가 바로 없어지지 않음. 수정 요망
        second_time = System.currentTimeMillis()
        val toast = makeText(this@MainActivity, "한 번 더 누르시면 종료됩니다.", Toast.LENGTH_SHORT)
        toast.show()
        if (second_time - first_time < 2000) {
            super.onBackPressed()
            toast.cancel()
            finishAffinity()
        }
        first_time = System.currentTimeMillis()
    }

}


//data class 생성 후 전체 json 데이터를 한번에 파싱 하려고 했으나 데이터를 가져올 때 바로 파싱하는 것으로 로직 변경
//data class JsonObj(val result : List<StatusData>)
//data class StatusData (val idx : String, val classification : String, val until_yesterday : Int, val today : Int)
