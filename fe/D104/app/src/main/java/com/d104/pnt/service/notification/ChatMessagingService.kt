package com.d104.pnt.service.notification

import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.PendingIntent
import android.content.Intent
import android.os.Build
import androidx.core.app.NotificationCompat
import com.d104.pnt.MainActivity
import com.d104.pnt.R
import com.d104.pnt.base.Constants
import com.d104.pnt.data.repository.AuthRepository
import com.google.firebase.messaging.FirebaseMessagingService
import com.google.firebase.messaging.RemoteMessage
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.launch
import javax.inject.Inject

@AndroidEntryPoint
class ChatMessagingService : FirebaseMessagingService() {

    @Inject
    lateinit var authRepository: AuthRepository

    override fun onNewToken(token: String) {
        super.onNewToken(token)

        val scope = CoroutineScope(Dispatchers.IO + SupervisorJob())
        scope.launch {
            authRepository.isLoggedIn().collect { isLoggedIn ->
                if (isLoggedIn) {
                    authRepository.sendFcmToken(true)
                }
            }
        }
    }

    // 메시지 수신
    override fun onMessageReceived(message: RemoteMessage) {
        super.onMessageReceived(message)

        if (message.data.isNotEmpty()) {

            val title = message.data["title"] ?: "새 메시지"
            val body = message.data["body"] ?: ""
            val roomId = message.data["chatRoomId"] ?: ""

            if (roomId.isNotEmpty()) {
                sendNotification(title, body, roomId)
            } else {

            }
        } else {

        }
    }

    private fun sendNotification(title: String, body: String, roomId: String) {
        val notificationManager = getSystemService(NOTIFICATION_SERVICE) as NotificationManager
        val channelId = Constants.CHANNEL_CHAT_ROOM

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val existingChannel = notificationManager.getNotificationChannel(channelId)
            if (existingChannel == null) {
                val channel = NotificationChannel(
                    channelId,
                    "채팅 알림",
                    NotificationManager.IMPORTANCE_HIGH
                ).apply {
                    description = "채팅방 메시지 알림"
                    enableVibration(true)
                    enableLights(true)
                }
                notificationManager.createNotificationChannel(channel)
            }
        }

        val intent = Intent(this, MainActivity::class.java).apply {
            flags = Intent.FLAG_ACTIVITY_SINGLE_TOP or Intent.FLAG_ACTIVITY_CLEAR_TOP

            putExtra("roomId", roomId)

            putExtra("from_notification", true)
        }

        val pendingIntent = PendingIntent.getActivity(
            this,
            roomId.hashCode(),
            intent,
            PendingIntent.FLAG_IMMUTABLE or PendingIntent.FLAG_UPDATE_CURRENT
        )

        // 알림 빌드
        val notificationBuilder = NotificationCompat.Builder(this, channelId)
            .setSmallIcon(R.drawable.ic_logo)
            .setContentTitle(title)
            .setContentText(body)
            .setAutoCancel(true)
            .setContentIntent(pendingIntent)
            .setPriority(NotificationCompat.PRIORITY_HIGH)
            .setDefaults(NotificationCompat.DEFAULT_ALL)
            .setVisibility(NotificationCompat.VISIBILITY_PUBLIC)

        val notificationId = roomId.hashCode()
        notificationManager.notify(notificationId, notificationBuilder.build())
    }
}