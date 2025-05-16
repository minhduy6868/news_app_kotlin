package com.gk.news_pro.page.utils.service

import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import android.os.Build
import android.util.Log
import androidx.core.app.NotificationCompat
import com.google.firebase.messaging.FirebaseMessagingService
import com.google.firebase.messaging.RemoteMessage
import com.gk.news_pro.MainActivity
import com.gk.news_pro.R

class MyFirebaseMessagingService : FirebaseMessagingService() {

    override fun onMessageReceived(remoteMessage: RemoteMessage) {
        Log.d("FCMService", "Nhận được thông báo: ${remoteMessage.notification?.title}, ${remoteMessage.notification?.body}")
        super.onMessageReceived(remoteMessage)
        remoteMessage.notification?.let {
            showNotification(
                it.title ?: "Video Tóm Tắt Sẵn Sàng",
                it.body ?: "Video tóm tắt tin tức mới đã được tạo!"
            )
        }
    }

    override fun onNewToken(token: String) {
        super.onNewToken(token)
        Log.d("FCMService", "FCM Token mới: $token")
        // Gửi token này đến server của bạn (nếu cần trong tương lai)
    }

    private fun showNotification(title: String, message: String) {
        Log.d("FCMService", "Tạo thông báo: title=$title, message=$message")
        val notificationManager = getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
        val channelId = "video_notification_channel"

        // Tạo Notification Channel cho Android 8.0 trở lên
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val channel = NotificationChannel(
                channelId,
                "Thông Báo Video",
                NotificationManager.IMPORTANCE_HIGH
            ).apply {
                description = "Thông báo khi video tóm tắt hoàn tất"
            }
            notificationManager.createNotificationChannel(channel)
            Log.d("FCMService", "Đã tạo Notification Channel: $channelId")
        }

        // Tạo Intent để mở ứng dụng khi người dùng nhấn vào thông báo
        val intent = Intent(this, MainActivity::class.java).apply {
            flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
        }
        val pendingIntent = PendingIntent.getActivity(
            this,
            0,
            intent,
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        )

        // Tạo thông báo
        val notification = NotificationCompat.Builder(this, channelId)
            .setSmallIcon(R.drawable.logo) // Đảm bảo logo tồn tại
            .setContentTitle(title)
            .setContentText(message)
            .setPriority(NotificationCompat.PRIORITY_HIGH)
            .setContentIntent(pendingIntent)
            .setAutoCancel(true)
            .build()

        // Hiển thị thông báo
        try {
            notificationManager.notify(1, notification)
            Log.d("FCMService", "Thông báo đã được hiển thị với ID=1")
        } catch (e: Exception) {
            Log.e("FCMService", "Lỗi khi hiển thị thông báo: ${e.message}", e)
        }
    }
}