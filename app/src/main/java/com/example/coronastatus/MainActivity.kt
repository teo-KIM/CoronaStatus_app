package com.example.coronastatus

import android.content.Intent
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import kotlinx.android.synthetic.main.activity_main.*

class MainActivity : AppCompatActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        map_btn.setOnClickListener {
            val intent = Intent(this, ScreeningClinicMap::class.java);
        }
    }

}

//TODO
//카카오맵 api 다운로드 후 적용해볼것
//run.js 응용해서 환자 수 크롤링 후 DB에 추가 -> 안드로이드에서 불러오기
