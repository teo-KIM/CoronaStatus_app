package com.teo.coronastatus

import android.content.Context
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.BaseAdapter
import android.widget.TextView

private val TAG: String = MyCustomAdapter::class.java.simpleName

class MyCustomAdapter(context: Context, dateList : MutableList<String>, contentList : MutableList<String>) : BaseAdapter() {
    private val mContext: Context

    //listView에 표현 해 줄 알람 온 날짜 리스트
    private val dates = dateList
    //listView에 표현 해 줄 알람 내용 리스트
    private val contents = contentList



    init {
        mContext = context
    }

    override fun getCount(): Int {
        return dates.size
    }

    override fun getItemId(position: Int): Long {
        return position.toLong()
    }

    override fun getItem(position: Int): Any {
        val selectItem = dates.get(position)
        return selectItem
    }

    override fun getView(position: Int, view: View?, viewGroup: ViewGroup?): View {

        val layoutInflater = LayoutInflater.from(mContext)
        val rowMain = layoutInflater.inflate(R.layout.item_notification, viewGroup, false)

        val datesTv = rowMain.findViewById<TextView>(R.id.date_tv)
        datesTv.text = dates.get(position)
        Log.d(TAG, "position : "+position)
        val contentsTv = rowMain.findViewById<TextView>(R.id.content_tv)
        contentsTv.text = contents.get(position)

        return rowMain
    }
}