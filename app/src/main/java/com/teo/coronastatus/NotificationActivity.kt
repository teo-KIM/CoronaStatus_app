package com.teo.coronastatus

import android.content.Context
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.AdapterView
import android.widget.BaseAdapter
import android.widget.ListView
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity

private val TAG: String = MainActivity::class.java.simpleName

class NotificationActivity : AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_notification)

        val listView = findViewById<ListView>(R.id.notification_lv)

        //어답터 설정
        listView.adapter = MyCustomAdapter(this)

        //아이템 클릭 리스너
        /*listView.onItemClickListener =
            AdapterView.OnItemClickListener { parent, view, position, id ->
                val selectItem = parent.getItemAtPosition(position) as String
                selectName.text = selectItem
                //Toast.makeText(this, selectItem, Toast.LENGTH_SHORT).show()
            }*/
    }

    private class MyCustomAdapter(context: Context) : BaseAdapter() {
        private val mContext: Context

        //데이터 어레이
        private val names = arrayListOf<String>(
            "이순신", "강감찬", "연개소문", "김유신", "을지문덕", "링컨", "워싱턴", "태종", "박혁거세", "광개토대왕"
        )

        init {
            mContext = context
        }

        override fun getCount(): Int {
            return names.size
        }

        override fun getItemId(position: Int): Long {
            return position.toLong()
        }

        override fun getItem(position: Int): Any {
            val selectItem = names.get(position)
            return selectItem
        }

        override fun getView(position: Int, view: View?, viewGroup: ViewGroup?): View {
            val layoutInflater = LayoutInflater.from(mContext)
            val rowMain = layoutInflater.inflate(R.layout.item_notification, viewGroup, false)

            val nameTextView = rowMain.findViewById<TextView>(R.id.date_tv)
            nameTextView.text = names.get(position)
            val positionTextView = rowMain.findViewById<TextView>(R.id.content_tv)
            positionTextView.text = "순서: " + position

            return rowMain
        }
    }


}