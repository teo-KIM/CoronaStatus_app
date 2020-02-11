package com.teo.coronastatus

import android.content.Context
import android.content.Intent
import android.graphics.Color
import android.location.LocationManager
import android.os.Bundle
import android.os.Handler
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import kotlinx.android.synthetic.main.activity_main.board_btn
import kotlinx.android.synthetic.main.screening_clinic_map.*
import kotlinx.android.synthetic.main.screening_clinic_map.diagnose_btn
import net.daum.mf.map.api.MapPOIItem
import net.daum.mf.map.api.MapPoint
import net.daum.mf.map.api.MapView
import okhttp3.*
import org.json.JSONObject
import java.io.IOException
import java.net.URL


abstract class ScreeningClinicMap : AppCompatActivity(), MapView.CurrentLocationEventListener {
    private val TAG: String = MainActivity::class.java.simpleName

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.screening_clinic_map)

        //현위치 버튼이 MapView 위로 올라오도록 설정
        location_btn.bringToFront()

        //현재 ScreeningClinicMap에 있다는 것을 알려주기 위함
        map_btn.setImageResource(R.drawable.ic_map_click)
        map_tv.setTextColor(Color.parseColor("#0d64b2"))

        val mapView = MapView(this)
        val mapViewContainer = map_view as ViewGroup
        mapViewContainer.addView(mapView)

        mapView.setCurrentLocationEventListener(this)

        if(!checkLocationServicesStatus()){
//            showdialogForLocationServiceSetting()
        }else{
//            checkRunTimePermission()
        }

        //mapView에 현재 확진자가 지나다녔던 곳을 마커로 찍어주는 메소드
        placeMarker(mapView)

        board_btn.setOnClickListener {
            val intent = Intent(this, MainActivity::class.java);
            startActivity(intent)
        }

        diagnose_btn.setOnClickListener {
            val intent = Intent(this, CodeOfConduct::class.java);
            startActivity(intent)
        }

        //현위치 버튼 클릭 시 색이 칠해진 현위치_클릭 버튼으로 대체한다.
        location_btn.setOnClickListener {
            location_btn.visibility = View.GONE
            location_click_btn.visibility = View.VISIBLE
            Toast.makeText(this@ScreeningClinicMap, "실제 위치와 차이가 날 수 있습니다.", Toast.LENGTH_SHORT).show()
            //현위치를 가져오고 보여주는 메소드

        }
        location_click_btn.setOnClickListener {
            location_click_btn.visibility = View.GONE
            location_btn.visibility = View.VISIBLE
        }
    }

    fun checkLocationServicesStatus() : Boolean {
        val locationManager = getSystemService(Context.LOCATION_SERVICE) as LocationManager
        return locationManager.isProviderEnabled(LocationManager.GPS_PROVIDER)
                || locationManager.isProviderEnabled(LocationManager.NETWORK_PROVIDER)
    }

    fun placeMarker(mapView : MapView) {

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
                //("not implemented") //To change body of created functions use File | Settings | File Templates.

                val body = response?.body()?.string()
//                Log.d(TAG, "Success to execute request! : $body")

                val jObject = JSONObject(body)
                val jArray = jObject.getJSONArray("infected")

                for (i in 0 until jArray.length()) {
                    val obj = jArray.getJSONObject(i)

                    val name = obj.getString("name")
                    nameList.add(name)
                    //name = db에 저장된 감염자가 다녀간 곳 이름

                    val latlong = obj.getString("latlong")
                    latlongList.add(latlong)
                    //latlong = db에 저장된 경도,위도

                    val description = obj.getString("description")
                    descriptionList.add(description)

//                    Log.d(TAG, "latlong : "+ latlong)
//                    Log.d(TAG, "classification($i) : $classification")
                }

            }

            override fun onFailure(call: Call?, e: IOException?) {
                //("not implemented") //To change body of created functions use File | Settings | File Templates.
                println("Failed to execute request!")
            }
        })

        val marker = MapPOIItem()

        Handler().postDelayed({
            for (i in 0 until nameList.size) {

                var latlong = latlongList.get(i).split(", ")

                var longitude = latlong[0]
                var latitude = latlong[1].trim()

//                Log.d(TAG, nameList.get(i))
//                Log.d(TAG, longitude+", "+latitude)

                marker.itemName = nameList.get(i)
                marker.tag = 0
                marker.mapPoint =
                    MapPoint.mapPointWithGeoCoord(longitude.toDouble(), latitude.toDouble())
                marker.markerType = MapPOIItem.MarkerType.RedPin
                marker.selectedMarkerType = MapPOIItem.MarkerType.RedPin
                mapView.addPOIItem(marker)

//                Log.d(TAG, "마커 표시 완료")

            }
        }, 4000)


    }

    //back 버튼 누를 시 현황판 (Main) 액티비티로 이동
    override fun onBackPressed() {
        super.onBackPressed()
        val intent = Intent(this, MainActivity::class.java);
        startActivity(intent)
    }
}