package com.teo.coronastatus

import android.annotation.SuppressLint
import android.app.NotificationManager
import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import android.media.RingtoneManager
import android.util.Log
import androidx.core.app.NotificationCompat
import com.google.firebase.messaging.FirebaseMessagingService
import com.google.firebase.messaging.RemoteMessage
import android.app.NotificationChannel
import android.graphics.Color

import android.os.Build



class MyFirebaseMessagingService : FirebaseMessagingService() {
    //firebase에서 메시지를 받으면 디바이스에 알람이 뜨게 하는 class
    private val TAG: String = MyFirebaseMessagingService::class.java.simpleName

    override fun onNewToken(token: String) {
//        Log.d("New_Token", token)
    }

    @SuppressLint("LongLogTag")

    override fun onMessageReceived(remoteMessage: RemoteMessage) {
        //받은 메시지에서 remoteMessage를 추출한다.
        super.onMessageReceived(remoteMessage)
        Log.d(TAG, "from : ${remoteMessage.from}")

        if (remoteMessage.notification != null) {
            Log.d(TAG, "Message body : ${remoteMessage.notification?.body}")
            sendNotification(remoteMessage)
        }
    }

    private fun sendNotification(remoteMessage: RemoteMessage) {
        //추출해온 remoteMessage에서 body, title을 추출해서 디바이스로 알림을 전송한다.

        val intent = Intent(this, MainActivity::class.java)
        intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP)
        intent.putExtra("function", "notification")
        intent.putExtra("title", remoteMessage.notification?.title)
        intent.putExtra("body", remoteMessage.notification?.body)

        val pendingIntent = PendingIntent.getActivity(this@MyFirebaseMessagingService, 0, intent, PendingIntent.FLAG_ONE_SHOT)

        val channelId : String = getString(R.string.default_notification_channel_id)
        val soundUri = RingtoneManager.getDefaultUri(RingtoneManager.TYPE_NOTIFICATION)
        val color = Color.RED

        val notificationBuilder = NotificationCompat.Builder(this, channelId)
            .setSmallIcon(R.drawable.virus)
            .setColor(color)
            .setContentTitle(remoteMessage.notification?.title)
            .setContentText(remoteMessage.notification?.body)
            .setAutoCancel(true)
            .setSound(soundUri)
            .setContentIntent(pendingIntent)

        val notificationManager =
            getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager

        //API 26, 즉 Oreo 이상의 버전에서는 Notification Channel을 설정해주지 않으면 알림 바가 실행되지 않음
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val channelName = getString(R.string.default_notification_channel_name)
            val channel =
                NotificationChannel(channelId, channelName, NotificationManager.IMPORTANCE_HIGH) as NotificationChannel
            notificationManager.createNotificationChannel(channel)
        }

        notificationManager.notify(0, notificationBuilder.build())

    }

}