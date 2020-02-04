package com.example.coronastatus

import android.content.Intent
import android.os.Bundle
import android.view.ViewGroup
import androidx.appcompat.app.AppCompatActivity
import kotlinx.android.synthetic.main.activity_main.*
import kotlinx.android.synthetic.main.activity_main.board_btn
import kotlinx.android.synthetic.main.screening_clinic_map.*
import net.daum.mf.map.api.MapPOIItem
import net.daum.mf.map.api.MapPoint
import net.daum.mf.map.api.MapView


class ScreeningClinicMap : AppCompatActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.screening_clinic_map)

        val mapView = MapView(this)

        val mapViewContainer = map_view as ViewGroup

        mapViewContainer.addView(mapView)



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
}