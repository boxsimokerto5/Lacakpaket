package com.example.notification

import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.os.Build
import androidx.core.app.NotificationCompat
import androidx.core.app.NotificationManagerCompat
import androidx.core.content.ContextCompat
import com.example.MainActivity
import com.example.R

object NotificationHelper {
    const val CHANNEL_ID = "channel_package_updates"
    private const val CHANNEL_NAME = "Pembaruan Paket"
    private const val CHANNEL_DESC = "Notifikasi status perjalanan dan kedatangan paket"

    fun createNotificationChannel(context: Context) {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val importance = NotificationManager.IMPORTANCE_HIGH
            val channel = NotificationChannel(CHANNEL_ID, CHANNEL_NAME, importance).apply {
                description = CHANNEL_DESC
                enableVibration(true)
            }
            val notificationManager =
                context.getSystemService(Context.NOTIFICATION_SERVICE) as? NotificationManager
            notificationManager?.createNotificationChannel(channel)
        }
    }

    fun hasNotificationPermission(context: Context): Boolean {
        return if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            ContextCompat.checkSelfPermission(
                context,
                android.Manifest.permission.POST_NOTIFICATIONS
            ) == PackageManager.PERMISSION_GRANTED
        } else {
            NotificationManagerCompat.from(context).areNotificationsEnabled()
        }
    }

    fun sendTestNotification(context: Context) {
        if (!hasNotificationPermission(context)) return

        createNotificationChannel(context)

        val intent = Intent(context, MainActivity::class.java).apply {
            flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
        }
        val pendingIntent = PendingIntent.getActivity(
            context,
            1001,
            intent,
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        )

        val notification = NotificationCompat.Builder(context, CHANNEL_ID)
            .setSmallIcon(R.mipmap.ic_launcher)
            .setContentTitle("📦 Uji Coba Notifikasi LacakPaket")
            .setContentText("Notifikasi real-time aktif! Anda akan mendapatkan pembaruan status resi secara otomatis.")
            .setStyle(
                NotificationCompat.BigTextStyle()
                    .bigText("Notifikasi real-time aktif! Anda akan mendapatkan pembaruan otomatis setiap kali ada pergerakan status dari kurir logistik.")
            )
            .setPriority(NotificationCompat.PRIORITY_HIGH)
            .setAutoCancel(true)
            .setContentIntent(pendingIntent)
            .build()

        try {
            NotificationManagerCompat.from(context).notify(1001, notification)
        } catch (_: SecurityException) {
            // Permission not granted
        }
    }

    fun sendPackageStatusNotification(
        context: Context,
        packageTitle: String,
        waybill: String,
        courierName: String,
        statusDesc: String,
        isDelivered: Boolean,
        notificationId: Int = (waybill.hashCode() and 0x7FFFFFFF)
    ) {
        if (!hasNotificationPermission(context)) return

        createNotificationChannel(context)

        val intent = Intent(context, MainActivity::class.java).apply {
            flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
        }
        val pendingIntent = PendingIntent.getActivity(
            context,
            notificationId,
            intent,
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        )

        val title = if (isDelivered) {
            "🎉 Paket Tiba: ${packageTitle.ifBlank { waybill }} ($courierName)"
        } else {
            "🚚 Pembaruan Paket: ${packageTitle.ifBlank { waybill }} ($courierName)"
        }

        val bigText = buildString {
            append("Resi: $waybill\n")
            append("Status: $statusDesc\n")
            if (isDelivered) {
                append("Status akhir: PAKET TELAH DITERIMA")
            } else {
                append("Kurir: $courierName sedang memproses pengiriman")
            }
        }

        val notification = NotificationCompat.Builder(context, CHANNEL_ID)
            .setSmallIcon(R.mipmap.ic_launcher)
            .setContentTitle(title)
            .setContentText(statusDesc)
            .setStyle(NotificationCompat.BigTextStyle().bigText(bigText))
            .setPriority(NotificationCompat.PRIORITY_HIGH)
            .setAutoCancel(true)
            .setContentIntent(pendingIntent)
            .build()

        try {
            NotificationManagerCompat.from(context).notify(notificationId, notification)
        } catch (_: SecurityException) {
            // Ignore if permission denied
        }
    }
}
