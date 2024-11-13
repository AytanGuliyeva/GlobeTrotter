package com.example.globetrotter
import android.app.NotificationChannel
import android.app.NotificationManager
import android.content.Context
import android.graphics.Color
import android.os.Build
import android.util.Log
import androidx.annotation.RequiresApi
import androidx.core.app.NotificationCompat
import androidx.work.CoroutineWorker
import androidx.work.WorkerParameters
import com.google.firebase.firestore.FirebaseFirestore
import kotlinx.coroutines.tasks.await
import java.util.Random
import java.time.LocalDate

class SpecialDayWorker(appContext: Context, workerParams: WorkerParameters) : CoroutineWorker(appContext, workerParams) {

    private val ADMIN_CHANNEL_ID = "special_day_channel"

    @RequiresApi(Build.VERSION_CODES.O)
    override suspend fun doWork(): Result {
        val today = LocalDate.now().toString() // Bugünün tarihini alıyoruz
        val db = FirebaseFirestore.getInstance()

        return try {
            val documents = db.collection("specialDays")
                .whereEqualTo("date", today) // Bugünün tarihiyle eşleşen özel günleri al
                .get()
                .await()

            Log.d("SpecialDayWorker", "Fetched documents count: ${documents.size()}")
            documents.forEach { document ->
                val title = document.getString("country")
                val message = document.getString("message")
                Log.d("SpecialDayWorker", "Notification - Title: $title, Message: $message")
                sendNotification(title, message)
            }

            Result.success()
        } catch (e: Exception) {
            Result.retry() // Hata durumunda tekrar deneyin
        }
    }

    private fun sendNotification(title: String?, message: String?) {
        val notificationManager = applicationContext.getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
        val notificationID = Random().nextInt(3000)

        // Bildirim kanalını oluşturuyoruz
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            setupChannels(notificationManager)
        }

        val notificationBuilder = NotificationCompat.Builder(applicationContext, ADMIN_CHANNEL_ID)
            .setSmallIcon(R.drawable.app_icon_3)
            .setContentTitle(title)
            .setContentText(message)
            .setAutoCancel(true)
            .setPriority(NotificationCompat.PRIORITY_HIGH)

        notificationManager.notify(notificationID, notificationBuilder.build())
    }

    @RequiresApi(Build.VERSION_CODES.O)
    private fun setupChannels(notificationManager: NotificationManager?) {
        val adminChannelName = "Special Day Notifications"
        val adminChannelDescription = "Notifications for special days"

        val adminChannel = NotificationChannel(ADMIN_CHANNEL_ID, adminChannelName, NotificationManager.IMPORTANCE_HIGH)
        adminChannel.description = adminChannelDescription
        adminChannel.enableLights(true)
        adminChannel.lightColor = Color.RED
        adminChannel.enableVibration(true)
        notificationManager?.createNotificationChannel(adminChannel)
    }
}
