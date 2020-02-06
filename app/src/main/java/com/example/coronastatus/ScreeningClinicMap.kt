package com.example.coronastatus

import android.content.Intent
import android.os.Bundle
import android.os.Handler
import android.util.Log
import android.view.ViewGroup
import androidx.appcompat.app.AppCompatActivity
import kotlinx.android.synthetic.main.activity_main.*
import kotlinx.android.synthetic.main.activity_main.board_btn
import kotlinx.android.synthetic.main.screening_clinic_map.*
import net.daum.mf.map.api.MapPOIItem
import net.daum.mf.map.api.MapPoint
import net.daum.mf.map.api.MapView
import okhttp3.*
import org.json.JSONObject
import java.io.IOException
import java.net.URL


class ScreeningClinicMap : AppCompatActivity() {
    private val TAG: String = MainActivity::class.java.simpleName

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.screening_clinic_map)

        val mapView = MapView(this)

        val mapViewContainer = map_view as ViewGroup

        mapViewContainer.addView(mapView)

        placeMarker()

        val marker = MapPOIItem()
        marker.itemName = "진료소"
        marker.tag = 0
        marker.mapPoint = MapPoint.mapPointWithGeoCoord(37.53737528.toDouble(), 127.00557633.toDouble())
        marker.markerType = MapPOIItem.MarkerType.BluePin
        marker.selectedMarkerType = MapPOIItem.MarkerType.RedPin
        mapView.addPOIItem(marker)

        marker.itemName = "진료소"
        marker.tag = 0
        marker.mapPoint = MapPoint.mapPointWithGeoCoord(37.500545.toDouble(), 126.985867.toDouble())
        marker.markerType = MapPOIItem.MarkerType.BluePin
        marker.selectedMarkerType = MapPOIItem.MarkerType.RedPin
        mapView.addPOIItem(marker)

        board_btn.setOnClickListener {
            val intent = Intent(this, MainActivity::class.java);
            startActivity(intent)
        }
    }

    fun placeMarker() {

        val url = URL("https://www.portfoliobyteo.kro.kr/getInfectedLocation.php")
        val request = Request.Builder().url(url).build()
        val client = OkHttpClient()

        //db에서 가져올 데이터를 담을 List
        val nameList = mutableListOf<String>()
        val latlongList = mutableListOf<String>()
        val descriptionList = mutableListOf<String>()

        client.newCall(request).enqueue(object : Callback {
            override fun onResponse(call: Call, response: Response?) {
                //응답이 있을 경우 call은 무조건 null이 아니므로 ?를 쓰지 않는다.
                //json 형식으로 받아온 데이터를 until_yesterday, today 배열에 저장하고 해당하는 textview에 값을 넣어준다.
                //TODO("not implemented") //To change body of created functions use File | Settings | File Templates.

                val body = response?.body()?.string()
                Log.d(TAG, "Success to execute request! : $body")

                val jObject = JSONObject(body)
                val jArray = jObject.getJSONArray("infected")

                for (i in 0 until jArray.length()) {
                    val obj = jArray.getJSONObject(i)

                    val name = obj.getString("name")
                    nameList.add(name)
                    //name = db에 저장된 감염자가 다녀간 곳 이름

                    val latlong = obj.getString("latlong")
                    latlongList.add(latlong)
                    //today = db에 저장된 경도,위도

                    val description = obj.getString("description")
                    descriptionList.add(description)

                    Log.d(TAG, "name : "+ name)
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


    }
}