package com.teo.coronastatus

import android.Manifest
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
import androidx.core.app.ActivityCompat
import android.content.pm.PackageManager
import android.app.AlertDialog
import android.content.DialogInterface
import android.util.Log
import androidx.core.content.ContextCompat
import kotlinx.android.synthetic.main.screening_clinic_map.view.*


class ScreeningClinicMap : AppCompatActivity(), MapView.CurrentLocationEventListener,
    MapView.POIItemEventListener {

    private val TAG: String = ScreeningClinicMap::class.java.simpleName

    lateinit var mapView: MapView

    //마커를 전역변수로 만들어서 동일한 아이템을 켰다 껐다 할 수 있도록 하기 위함
    lateinit var patients_location_markers: Array<MapPOIItem?>
    lateinit var patients_hospital_markers: Array<MapPOIItem?>

    //맵에 표시되는 이모티콘들이 눌려있는지 아닌지를 구분하기 위한 변수
    var patient_location_click = 0
    var patient_hospital_click = 0

    //내 위치 정보를 가져오기 위한 permission 관련 변수
    //checkRunTimePermission() 에서 사용
    val GPS_ENABLE_REQUEST_CODE = 201;
    val PERMISSIONS_REQUEST_CODE = 100;
    var REQUIRED_PERMISSIONS = arrayOf(Manifest.permission.ACCESS_FINE_LOCATION)

    //현재 위치(위도, 경도, 지도) 를 전역 변수로 저장 한 후 사용하기 위해 선언
    lateinit var currentMapPoint: MapPoint
    var mCurrentLat: Double = 0.0
    var mCurrentLng: Double = 0.0

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.screening_clinic_map)

        patient_location_click = 0
        patient_hospital_click = 0
//        Log.d(TAG, "onCreate")

        //아이콘들이 mapView 최상위로 올라오도록 설정
        location_btn.bringToFront()
        patient_hospital_btn.bringToFront()
        patient_location_btn.bringToFront()

        //현재 ScreeningClinicMap에 있다는 것을 알려주기 위함
        map_btn.setImageResource(R.drawable.ic_map_click)
        map_tv.setTextColor(Color.parseColor("#0d64b2"))

        //화면에 맵뷰를 보여준다
        mapView = MapView(this@ScreeningClinicMap)
        val mapViewContainer = map_view as ViewGroup
        mapViewContainer.addView(mapView)
        mapView.setZoomLevel(8, true);

        mapView.setCurrentLocationEventListener(this)

        if (!checkLocationServicesStatus()) {
            showDialogForLocationServiceSetting()
        } else {
            checkRunTimePermission()
        }

        //디폴드 값이 모든 마커를 찍어주도록 변경한다
        patientHospitalMarker(mapView)
        patient_hospital_click = 1

        patient_hospital_btn.visibility = View.GONE
        hospital_simple_loader.playAnimation()
        Handler().postDelayed({
            patient_hospital_btn.setBackgroundColor(Color.BLACK)
            patient_hospital_btn.visibility = View.VISIBLE
        }, 1000)

        patientPlaceMarker(mapView)
        patient_location_click = 1

        patient_location_btn.visibility = View.GONE
        location_simple_loader.playAnimation()
        Handler().postDelayed({
            patient_location_btn.setBackgroundColor(Color.BLACK)
            patient_location_btn.visibility = View.VISIBLE
        }, 1000)


        patient_location_btn.setOnClickListener {

            //확진자가 돌아다닌 위치를 나타내주도록 하는 아이콘 클릭시
            if (patient_location_click == 0) {
                patientPlaceMarker(mapView)
                patient_location_click = 1
                Toast.makeText(this@ScreeningClinicMap, "확진자 방문지에 마커를 표시합니다.", Toast.LENGTH_SHORT)
                    .show()
                //클릭한 경우 아이콘을 GONE으로 변화시키고 로딩중인 Lottie 표시
                patient_location_btn.visibility = View.GONE
                location_simple_loader.playAnimation()

                Handler().postDelayed({
                    patient_location_btn.setBackgroundColor(Color.BLACK)
                    patient_location_btn.visibility = View.VISIBLE
                }, 1000)

            } else {
                patient_location_click = 0
                Toast.makeText(this@ScreeningClinicMap, "확진자 방문지에 마커를 지웁니다.", Toast.LENGTH_SHORT)
                    .show()
                mapView.removePOIItems(patients_location_markers)
                patient_location_btn.setBackgroundColor(Color.WHITE)
            }
        }
        //확진자가 입원해 있는 병원 위치를 나타내주도록 하는 아이콘 클릭시
        patient_hospital_btn.setOnClickListener {
            if (patient_hospital_click == 0) {
                patient_hospital_click = 1
                patientHospitalMarker(mapView)
                Toast.makeText(this@ScreeningClinicMap, "확진자 입원 병원에 마커를 표시합니다.", Toast.LENGTH_SHORT)
                    .show()

                //클릭한 경우 아이콘을 GONE으로 변화시키고 로딩중인 Lottie 표시
                patient_hospital_btn.visibility = View.GONE
                hospital_simple_loader.playAnimation()

                Handler().postDelayed({
                    patient_hospital_btn.setBackgroundColor(Color.BLACK)
                    patient_hospital_btn.visibility = View.VISIBLE
                }, 1000)


            } else {
                patient_hospital_click = 0
                Toast.makeText(this@ScreeningClinicMap, "확진자 입원 병원에 마커를 지웁니다.", Toast.LENGTH_SHORT)
                    .show()
                mapView.removePOIItems(patients_hospital_markers)
                patient_hospital_btn.setBackgroundColor(Color.WHITE)
            }
        }

        board_btn.setOnClickListener {
            val intent = Intent(this, MainActivity::class.java);
            intent.addFlags(Intent.FLAG_ACTIVITY_REORDER_TO_FRONT)
            startActivity(intent)
        }

        diagnose_btn.setOnClickListener {
            val intent = Intent(this, CodeOfConduct::class.java);
            intent.addFlags(Intent.FLAG_ACTIVITY_REORDER_TO_FRONT)
            startActivity(intent)
        }

        //현위치 버튼 클릭 시 색이 칠해진 현위치_클릭 버튼으로 대체한다.
        location_btn.setOnClickListener {
            Toast.makeText(this@ScreeningClinicMap, "실제 위치와 차이가 날 수 있습니다.", Toast.LENGTH_SHORT)
                .show()
        }
    }

    //MapView.CurrentLocationEventListener implement하기 위함
    override fun onCurrentLocationUpdateFailed(p0: MapView?) {
        //To change body of created functions use File | Settings | File Templates.
    }

    override fun onCurrentLocationUpdateCancelled(p0: MapView?) {
        //To change body of created functions use File | Settings | File Templates.
    }

    override fun onCurrentLocationDeviceHeadingUpdate(p0: MapView?, p1: Float) {
        //To change body of created functions use File | Settings | File Templates.
    }


    //MapView.POIItemEventListener implement하기 위함
    override fun onCalloutBalloonOfPOIItemTouched(p0: MapView?, p1: MapPOIItem?) {
        //("not implemented") //To change body of created functions use File | Settings | File Templates.
    }

    override fun onCalloutBalloonOfPOIItemTouched(
        p0: MapView?,
        p1: MapPOIItem?,
        p2: MapPOIItem.CalloutBalloonButtonType?
    ) {
        //("not implemented") //To change body of created functions use File | Settings | File Templates.
    }

    override fun onDraggablePOIItemMoved(p0: MapView?, p1: MapPOIItem?, p2: MapPoint?) {
        //("not implemented") //To change body of created functions use File | Settings | File Templates.
    }

    override fun onPOIItemSelected(p0: MapView?, p1: MapPOIItem?) {
        //("not implemented") //To change body of created functions use File | Settings | File Templates.
    }


    //위치 서비스가 비활성화 되어있을 경우 다이얼로그를 띄워서 활성화 할 수 있도록 하는 메소드
    fun showDialogForLocationServiceSetting() {

        val builder = AlertDialog.Builder(this@ScreeningClinicMap)

        builder.setTitle("위치 서비스 비활성화");
        builder.setMessage(
            "앱을 사용하기 위해서는 위치 서비스가 필요합니다.\n"
                    + "위치 설정을 수정하시겠습니까?"
        );
        builder.setCancelable(true);

        builder.setPositiveButton("설정") { dialog: DialogInterface?, which: Int ->
            val callGPSSettingIntent =
                Intent(android.provider.Settings.ACTION_LOCATION_SOURCE_SETTINGS)
            startActivityForResult(callGPSSettingIntent, GPS_ENABLE_REQUEST_CODE)
        }

        builder.setNegativeButton("취소") { dialog: DialogInterface?, which: Int ->
            dialog?.cancel()
        }

        builder.create().show();
    }


    override fun onCurrentLocationUpdate(p0: MapView?, p1: MapPoint?, p2: Float) {
//        ("not implemented") //To change body of created functions use File | Settings | File Templates.

        val mapPointGeo = p1?.getMapPointGeoCoord() as MapPoint.GeoCoordinate

        Log.i(
            TAG,
            String.format(
                "MapView onCurrentLocationUpdate (%f,%f) accuracy (%f)",
                mapPointGeo.latitude,
                mapPointGeo.longitude,
                p2
            )
        )

        currentMapPoint =
            MapPoint.mapPointWithGeoCoord(mapPointGeo.latitude, mapPointGeo.longitude);
        //이 좌표로 지도 중심 이동
        mapView.setMapCenterPoint(currentMapPoint, true);
        //전역변수로 현재 좌표 저장
        mCurrentLat = mapPointGeo.latitude;
        mCurrentLng = mapPointGeo.longitude;
//        Log.d(TAG, "현재위치 => " + mCurrentLat + "  " + mCurrentLng);

        //트래킹 모드가 아닌 단순 현재위치 업데이트일 경우, 한번만 위치 업데이트하고 트래킹을 중단시키기 위한 로직
//        if (!isTrackingMode) {
//            mMapView.setCurrentLocationTrackingMode(MapView.CurrentLocationTrackingMode.TrackingModeOff);
//        }
    }


    override fun onRequestPermissionsResult(
        requestCode: Int,
        permissions: Array<out String>,
        grantResults: IntArray
    ) {

//        super.onRequestPermissionsResult(requestCode, permissions, grantResults)
        if (requestCode == PERMISSIONS_REQUEST_CODE && grantResults.size == REQUIRED_PERMISSIONS.size) {

            // 요청 코드가 PERMISSIONS_REQUEST_CODE 이고, 요청한 퍼미션 개수만큼 수신되었다면
            var check_result = true

            // 모든 퍼미션을 허용했는지 체크한다.
            for (result in grantResults) {
                if (result != PackageManager.PERMISSION_GRANTED) {
                    check_result = false
                    break
                }
            }

            if (check_result) {
                Log.d(TAG, "onRequestPermissionsResult() 메소드 실행")
                //위치 값을 가져올 수 있음
                mapView.setCurrentLocationTrackingMode(MapView.CurrentLocationTrackingMode.TrackingModeOnWithoutHeadingWithoutMapMoving)
            } else {
                // 거부한 퍼미션이 있다면 앱을 사용할 수 없는 이유를 설명해주고 앱을 종료한다. 2 가지 경우가 있다.
                if (ActivityCompat.shouldShowRequestPermissionRationale(
                        this,
                        REQUIRED_PERMISSIONS[0]
                    )
                ) {

                    Toast.makeText(
                        this@ScreeningClinicMap,
                        "퍼미션이 거부되었습니다. 앱을 다시 실행하여 퍼미션을 허용해주세요.",
                        Toast.LENGTH_LONG
                    ).show()
                    finish()

                } else {

                    Toast.makeText(
                        this@ScreeningClinicMap,
                        "퍼미션이 거부되었습니다. 설정(앱 정보)에서 퍼미션을 허용해야 합니다. ",
                        Toast.LENGTH_LONG
                    ).show()
                }
            }
        }
    }

    fun checkRunTimePermission() {
        //런타임 퍼미션 처리
        // 1. 위치 퍼미션 가지고 있는지 확인
        val hasFineLocationPermission = ContextCompat.checkSelfPermission(
            this@ScreeningClinicMap,
            Manifest.permission.ACCESS_FINE_LOCATION
        )

        if (hasFineLocationPermission == PackageManager.PERMISSION_GRANTED) {
            // 2. 이미 퍼미션을 가지고 있다면 위치 값을 가져올 수 있음
            mapView.setCurrentLocationTrackingMode(MapView.CurrentLocationTrackingMode.TrackingModeOnWithoutHeadingWithoutMapMoving)

        } else {  //2. 퍼미션 요청을 허용한 적이 없다면 퍼미션 요청이 필요. 2가지 경우(3-1, 4-1)가 있다.

            // 3-1. 사용자가 퍼미션 거부를 한 적이 있는 경우
            if (ActivityCompat.shouldShowRequestPermissionRationale(
                    this@ScreeningClinicMap,
                    REQUIRED_PERMISSIONS[0]
                )
            ) {

                // 3-2. 요청을 진행하기 전에 사용자가에게 퍼미션이 필요한 이유를 설명해줄 필요가 있습니다.
                Toast.makeText(
                    this@ScreeningClinicMap,
                    "이 앱을 실행하려면 위치 접근 권한이 필요합니다.",
                    Toast.LENGTH_LONG
                )
                    .show()
                // 3-3. 사용자게에 퍼미션 요청을 합니다. 요청 결과는 onRequestPermissionResult에서 수신됩니다.
                ActivityCompat.requestPermissions(
                    this@ScreeningClinicMap, REQUIRED_PERMISSIONS,
                    PERMISSIONS_REQUEST_CODE
                )

            } else {
                // 4-1. 사용자가 퍼미션 거부를 한 적이 없는 경우에는 퍼미션 요청을 바로 합니다.
                // 요청 결과는 onRequestPermissionResult에서 수신됩니다.
                ActivityCompat.requestPermissions(
                    this@ScreeningClinicMap, REQUIRED_PERMISSIONS,
                    PERMISSIONS_REQUEST_CODE
                )
            }

        }
    }

    //현재 위치 서비스가 켜져있는지 확인하는 메소드
    fun checkLocationServicesStatus(): Boolean {
        val locationManager = getSystemService(Context.LOCATION_SERVICE) as LocationManager
        return locationManager.isProviderEnabled(LocationManager.GPS_PROVIDER)
                || locationManager.isProviderEnabled(LocationManager.NETWORK_PROVIDER)
    }

    //--------------------------------마커 찍는 메소드 시작

    //확진자가 입원중인 병원에 마커를 찍어주는 메소드
    fun patientHospitalMarker(mapView: MapView) {
        val url = URL("https://www.portfoliobyteo.kro.kr/getPatientHospital.php")
        val request = Request.Builder().url(url).build()
        val client = OkHttpClient()

        //db에서 가져올 데이터를 담을 List
        val nameList = mutableListOf<String>()
        val latlongList = mutableListOf<String>()

        client.newCall(request).enqueue(object : Callback {
            override fun onResponse(call: Call, response: Response?) {
//                Log.d(TAG, "onResponse 시작 : "+System.currentTimeMillis())
                //응답이 있을 경우 call은 무조건 null이 아니므로 ?를 쓰지 않는다.
                //json 형식으로 받아온 데이터를 until_yesterday, today 배열에 저장하고 해당하는 textview에 값을 넣어준다.
                //("not implemented") //To change body of created functions use File | Settings | File Templates.

                val body = response?.body()?.string()
//                Log.d(TAG, "Success to execute request! : $body")

                val jObject = JSONObject(body)
                val jArray = jObject.getJSONArray("patientHospital")

                for (i in 0 until jArray.length()) {
                    val obj = jArray.getJSONObject(i)

                    val name = obj.getString("name")
                    nameList.add(name)
                    //name = db에 저장된 확진자가 입원한 병원

                    val latlong = obj.getString("latlong")
                    latlongList.add(latlong)
                    //latlong = db에 저장된 경도,위도


                }
//                Log.d(TAG, "onResponse 끝 : "+System.currentTimeMillis())

            }

            override fun onFailure(call: Call?, e: IOException?) {
                //("not implemented") //To change body of created functions use File | Settings | File Templates.
                println("확진자가 입원한 병원의 위치 가져오기를 실패했습니다!")
            }
        })


        Handler().postDelayed({
            //            Log.d(TAG, "handler 시작 : "+System.currentTimeMillis())
            patients_hospital_markers = arrayOfNulls<MapPOIItem>(nameList.size)
            for (i in 0 until nameList.size) {

                var latlong = latlongList.get(i).split(", ")

//                Log.d(TAG, "latlongList : "+latlongList.get(i))
                var longitude = latlong[0]
                var latitude = latlong[1].trim()

//                Log.d(TAG, nameList.get(i))
//                Log.d(TAG, longitude+", "+latitude)

//                Log.d(TAG, "nameList : " +nameList.get(i))

                val marker = MapPOIItem()
                marker.itemName = nameList.get(i)
                marker.tag = 0
                marker.mapPoint =
                    MapPoint.mapPointWithGeoCoord(longitude.toDouble(), latitude.toDouble())
                marker.markerType = MapPOIItem.MarkerType.BluePin
                marker.selectedMarkerType = MapPOIItem.MarkerType.BluePin

                patients_hospital_markers[i] = marker
            }
            mapView.addPOIItems(patients_hospital_markers)


//                mapView.addPOIItem(marker)
//                Log.d(TAG, nameList.get(i))
//                Log.d(TAG, longitude+", "+latitude)
//                Log.d(TAG, "마커 표시 완료")
//                Log.d(TAG, "마커 표시 완료")
//            Log.d(TAG, "handler 끝 : "+System.currentTimeMillis())
        }, 1000)

    }


    //확진자가 있었던 곳에 핀을 찍어주는 메소드
    fun patientPlaceMarker(mapView: MapView) {

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

        Handler().postDelayed({

            patients_location_markers = arrayOfNulls<MapPOIItem>(nameList.size)
            for (i in 0 until nameList.size) {

                var latlong = latlongList.get(i).split(", ")

                var longitude = latlong[0]
                var latitude = latlong[1].trim()

                val marker = MapPOIItem()

                marker.itemName = nameList.get(i)
                marker.tag = 0
                marker.mapPoint =
                    MapPoint.mapPointWithGeoCoord(longitude.toDouble(), latitude.toDouble())
                marker.markerType = MapPOIItem.MarkerType.RedPin
                marker.selectedMarkerType = MapPOIItem.MarkerType.RedPin
//                mapView.addPOIItem(marker)
                patients_location_markers[i] = marker
            }
            mapView.addPOIItems(patients_location_markers)

        }, 1000)

    }


    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        when (requestCode) {

            GPS_ENABLE_REQUEST_CODE ->

                //사용자가 GPS 활성 시켰는지 검사
                if (checkLocationServicesStatus()) {
                    if (checkLocationServicesStatus()) {

                        Log.d(TAG, "onActivityResult : GPS 활성화 되있음")
                        checkRunTimePermission()
                        return
                    }
                }
        }
    }

    //back 버튼 누를 시 현황판 (Main) 액티비티로 이동
    override fun onBackPressed() {
        super.onBackPressed()
        val intent = Intent(this, MainActivity::class.java);
        intent.addFlags(Intent.FLAG_ACTIVITY_REORDER_TO_FRONT)
        startActivity(intent)
    }

    override fun onDestroy() {
        super.onDestroy()
        mapView.currentLocationTrackingMode = MapView.CurrentLocationTrackingMode.TrackingModeOff
        mapView.setShowCurrentLocationMarker(false)
//        Log.d(TAG, "onDestroy")

    }

    /*override fun onStop() {
        super.onStop()
        Log.d(TAG, "onStop")
    }
    override fun onResume() {
        super.onResume()
        Log.d(TAG, "onResume")
    }*/

}