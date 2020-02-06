package com.example.coronastatus

import android.annotation.SuppressLint
import android.app.NotificationManager
import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import android.media.RingtoneManager
import android.util.Log
import androidx.core.app.NotificationCompat
import androidx.core.content.ContextCompat.getSystemService
import com.google.firebase.messaging.FirebaseMessagingService
import com.google.firebase.messaging.RemoteMessage

class MyFirebaseMessagingService : FirebaseMessagingService() {
    //푸시 메시지를 받았을 때 동작하는 서비스 설정
    private val TAG: String = MainActivity::class.java.simpleName

    override fun onNewToken(token: String) {
        Log.d("New_Token", token)
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

        val pendingIntent = PendingIntent.getActivity(this, 0, intent, PendingIntent.FLAG_ONE_SHOT)
        val soundUri = RingtoneManager.getDefaultUri(RingtoneManager.TYPE_NOTIFICATION)
        val notificationBuilder = NotificationCompat.Builder(this, "Notification")
            .setSmallIcon(R.mipmap.ic_launcher)
            .setContentTitle(remoteMessage.notification?.title)
            .setContentText(remoteMessage.notification?.body)
            .setAutoCancel(true)
            .setSound(soundUri)
            .setContentIntent(pendingIntent)

        val notificationManager =
            getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
        notificationManager.notify(0, notificationBuilder.build())

    }

}