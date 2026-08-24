package com.SkrinVex.syncwave.app.upload

import android.app.Notification
import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.PendingIntent
import android.app.Service
import android.content.Context
import android.content.Intent
import android.os.Build
import android.os.IBinder
import androidx.core.app.NotificationCompat
import com.SkrinVex.syncwave.app.MainActivity
import com.SkrinVex.syncwave.app.R
import com.SkrinVex.syncwave.app.SyncWaveApplication
import com.SkrinVex.syncwave.app.domain.model.UploadStatus
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.launch

class UploadForegroundService : Service() {

    private val serviceScope = CoroutineScope(Dispatchers.Main + Job())
    private var observeJob: Job? = null

    companion object {
        const val CHANNEL_ID = "syncwave_upload_channel"
        const val NOTIFICATION_ID = 2001

        fun start(context: Context) {
            val intent = Intent(context, UploadForegroundService::class.java)
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                context.startForegroundService(intent)
            } else {
                context.startService(intent)
            }
        }

        fun stop(context: Context) {
            val intent = Intent(context, UploadForegroundService::class.java)
            context.stopService(intent)
        }
    }

    override fun onCreate() {
        super.onCreate()
        createNotificationChannel()
        startForeground(NOTIFICATION_ID, buildInitialNotification())
        observeUploads()
    }

    private fun createNotificationChannel() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val channel = NotificationChannel(
                CHANNEL_ID,
                "Загрузка аудиофайлов",
                NotificationManager.IMPORTANCE_LOW
            ).apply {
                description = "Отображение прогресса загрузки ручных аудиофайлов в SyncWave"
                setShowBadge(false)
            }
            val manager = getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
            manager.createNotificationChannel(channel)
        }
    }

    private fun buildInitialNotification(): Notification {
        val pendingIntent = PendingIntent.getActivity(
            this,
            0,
            Intent(this, MainActivity::class.java).apply {
                flags = Intent.FLAG_ACTIVITY_SINGLE_TOP or Intent.FLAG_ACTIVITY_CLEAR_TOP
            },
            PendingIntent.FLAG_IMMUTABLE or PendingIntent.FLAG_UPDATE_CURRENT
        )

        return NotificationCompat.Builder(this, CHANNEL_ID)
            .setContentTitle("Загрузка треков в SyncWave")
            .setContentText("Подготовка к отправке...")
            .setSmallIcon(android.R.drawable.stat_sys_upload)
            .setProgress(100, 0, true)
            .setOngoing(true)
            .setContentIntent(pendingIntent)
            .build()
    }

    private fun observeUploads() {
        val uploadManager = SyncWaveApplication.instance.container.uploadManager
        observeJob = serviceScope.launch {
            uploadManager.tasks.collect { tasks ->
                val total = tasks.size
                if (total == 0) {
                    stopForeground(STOP_FOREGROUND_REMOVE)
                    stopSelf()
                    return@collect
                }

                val completed = tasks.count { it.status == UploadStatus.DONE }
                val failed = tasks.count { it.status == UploadStatus.ERROR }
                val isUploading = tasks.any {
                    it.status == UploadStatus.PENDING ||
                    it.status == UploadStatus.UPLOADING ||
                    it.status == UploadStatus.PROCESSING
                }

                val progress = uploadManager.overallProgress.value

                val notificationManager = getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
                val pendingIntent = PendingIntent.getActivity(
                    this@UploadForegroundService,
                    0,
                    Intent(this@UploadForegroundService, MainActivity::class.java).apply {
                        flags = Intent.FLAG_ACTIVITY_SINGLE_TOP or Intent.FLAG_ACTIVITY_CLEAR_TOP
                    },
                    PendingIntent.FLAG_IMMUTABLE or PendingIntent.FLAG_UPDATE_CURRENT
                )

                if (isUploading) {
                    val activeTask = tasks.firstOrNull { it.status == UploadStatus.UPLOADING || it.status == UploadStatus.PROCESSING }
                    val contentText = if (activeTask != null) {
                        "${activeTask.name} ($completed/$total)"
                    } else {
                        "Загружено $completed из $total ($progress%)"
                    }

                    val notif = NotificationCompat.Builder(this@UploadForegroundService, CHANNEL_ID)
                        .setContentTitle("Загрузка треков в SyncWave ($completed/$total)")
                        .setContentText(contentText)
                        .setSmallIcon(android.R.drawable.stat_sys_upload)
                        .setProgress(100, progress, false)
                        .setOngoing(true)
                        .setContentIntent(pendingIntent)
                        .build()

                    notificationManager.notify(NOTIFICATION_ID, notif)
                } else {
                    // Uploads finished
                    val title = if (failed == 0) "Загрузка треков завершена" else "Загрузка завершена с ошибками"
                    val text = if (failed == 0) {
                        "Успешно загружено $completed треков"
                    } else {
                        "Загружено $completed, ошибок: $failed"
                    }

                    val notif = NotificationCompat.Builder(this@UploadForegroundService, CHANNEL_ID)
                        .setContentTitle(title)
                        .setContentText(text)
                        .setSmallIcon(android.R.drawable.stat_sys_upload_done)
                        .setProgress(0, 0, false)
                        .setOngoing(false)
                        .setAutoCancel(true)
                        .setContentIntent(pendingIntent)
                        .build()

                    notificationManager.notify(NOTIFICATION_ID, notif)
                    stopForeground(STOP_FOREGROUND_DETACH)
                    stopSelf()
                }
            }
        }
    }

    override fun onBind(intent: Intent?): IBinder? = null

    override fun onDestroy() {
        super.onDestroy()
        observeJob?.cancel()
    }
}

