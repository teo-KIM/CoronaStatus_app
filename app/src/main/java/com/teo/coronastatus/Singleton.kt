package com.teo.coronastatus

import android.content.Context
import android.widget.Toast

object Singleton{

    fun showToast(context: Context, text: String, duration: Int = Toast.LENGTH_LONG) {
        Toast.makeText(context, text, duration).show()
    }

}