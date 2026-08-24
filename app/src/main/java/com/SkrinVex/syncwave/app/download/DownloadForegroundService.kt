package com.SkrinVex.syncwave.app.download

import android.app.Notification
import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.PendingIntent
import android.app.Service
import android.content.Context
import android.content.Intent
import android.content.pm.ServiceInfo
import android.os.Build
import android.os.IBinder
import androidx.core.app.NotificationCompat
import com.SkrinVex.syncwave.app.MainActivity
import com.SkrinVex.syncwave.app.SyncWaveApplication
import com.SkrinVex.syncwave.app.domain.model.DownloadStatus
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.launch

class DownloadForegroundService : Service() {

    private val serviceScope = CoroutineScope(Dispatchers.Main + Job())
    private var observeJob: Job? = null
    private var lastNotificationUpdateTime = 0L

    companion object {
        const val CHANNEL_ID = "syncwave_download_channel"
        const val NOTIFICATION_ID = 2002
        const val ACTION_CANCEL_ALL = "com.SkrinVex.syncwave.app.ACTION_CANCEL_ALL_DOWNLOADS"

        fun start(context: Context) {
            try {
                val intent = Intent(context, DownloadForegroundService::class.java)
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                    context.startForegroundService(intent)
                } else {
                    context.startService(intent)
                }
            } catch (_: Exception) {}
        }

        fun stop(context: Context) {
            try {
                val intent = Intent(context, DownloadForegroundService::class.java)
                context.stopService(intent)
            } catch (_: Exception) {}
        }
    }

    override fun onCreate() {
        super.onCreate()
        createNotificationChannel()
        startForegroundSafely(buildInitialNotification())
        observeDownloads()
    }

    override fun onStartCommand(intent: Intent?, flags: Int, startId: Int): Int {
        // ALWAYS satisfy startForeground contract immediately in onStartCommand
        startForegroundSafely(buildInitialNotification())

        if (intent?.action == ACTION_CANCEL_ALL) {
            try {
                SyncWaveApplication.instance.container.downloadManager.cancelAll()
            } catch (_: Exception) {}
            stopForeground(STOP_FOREGROUND_REMOVE)
            stopSelf()
            return START_NOT_STICKY
        }
        return START_STICKY
    }

    private fun startForegroundSafely(notification: Notification) {
        try {
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
                startForeground(
                    NOTIFICATION_ID,
                    notification,
                    ServiceInfo.FOREGROUND_SERVICE_TYPE_DATA_SYNC
                )
            } else {
                startForeground(NOTIFICATION_ID, notification)
            }
        } catch (_: Exception) {}
    }

    private fun createNotificationChannel() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val channel = NotificationChannel(
                CHANNEL_ID,
                "Скачивание треков",
                NotificationManager.IMPORTANCE_LOW
            ).apply {
                description = "Отображение прогресса скачивания треков для оффлайн-прослушивания"
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
            .setContentTitle("Скачивание треков в память устройства")
            .setContentText("Подготовка к скачиванию...")
            .setSmallIcon(android.R.drawable.stat_sys_download)
            .setProgress(100, 0, true)
            .setOngoing(true)
            .setContentIntent(pendingIntent)
            .build()
    }

    private fun observeDownloads() {
        val downloadManager = SyncWaveApplication.instance.container.downloadManager
        observeJob?.cancel()
        observeJob = serviceScope.launch {
            downloadManager.tasks.collect { tasks ->
                val total = tasks.size
                if (total == 0) {
                    stopForeground(STOP_FOREGROUND_REMOVE)
                    stopSelf()
                    return@collect
                }

                val completed = tasks.count { it.status == DownloadStatus.COMPLETED }
                val failed = tasks.count { it.status == DownloadStatus.ERROR }
                val isDownloading = tasks.any {
                    it.status == DownloadStatus.PENDING || it.status == DownloadStatus.DOWNLOADING
                }

                val progress = downloadManager.overallProgress.value
                val notificationManager = getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
                val pendingIntent = PendingIntent.getActivity(
                    this@DownloadForegroundService,
                    0,
                    Intent(this@DownloadForegroundService, MainActivity::class.java).apply {
                        flags = Intent.FLAG_ACTIVITY_SINGLE_TOP or Intent.FLAG_ACTIVITY_CLEAR_TOP
                    },
                    PendingIntent.FLAG_IMMUTABLE or PendingIntent.FLAG_UPDATE_CURRENT
                )

                val now = System.currentTimeMillis()

                if (isDownloading) {
                    // Throttle notification updates to at most once per 800ms
                    if (now - lastNotificationUpdateTime > 800 || progress == 0 || progress == 100) {
                        lastNotificationUpdateTime = now

                        val activeTask = tasks.firstOrNull { it.status == DownloadStatus.DOWNLOADING }
                        val contentText = if (activeTask != null) {
                            val speedPart = if (activeTask.formattedSpeed.isNotBlank()) " • ${activeTask.formattedSpeed}" else ""
                            "${activeTask.title} • ${activeTask.downloadedMbFormatted} (${activeTask.progress}%)$speedPart"
                        } else {
                            "Скачано $completed из $total ($progress%)"
                        }

                        val notif = NotificationCompat.Builder(this@DownloadForegroundService, CHANNEL_ID)
                            .setContentTitle("Скачивание треков SyncWave ($completed/$total)")
                            .setContentText(contentText)
                            .setSmallIcon(android.R.drawable.stat_sys_download)
                            .setProgress(100, progress, false)
                            .setOngoing(true)
                            .setContentIntent(pendingIntent)
                            .build()

                        try {
                            notificationManager.notify(NOTIFICATION_ID, notif)
                        } catch (_: Exception) {}
                    }
                } else {
                    val title = if (failed == 0) "Скачивание треков завершено" else "Скачивание завершено с ошибками"
                    val text = if (failed == 0) {
                        "Сохранено на устройство $completed треков"
                    } else {
                        "Сохранено $completed, ошибок: $failed"
                    }

                    val notif = NotificationCompat.Builder(this@DownloadForegroundService, CHANNEL_ID)
                        .setContentTitle(title)
                        .setContentText(text)
                        .setSmallIcon(android.R.drawable.stat_sys_download_done)
                        .setProgress(0, 0, false)
                        .setOngoing(false)
                        .setAutoCancel(true)
                        .setContentIntent(pendingIntent)
                        .build()

                    try {
                        notificationManager.notify(NOTIFICATION_ID, notif)
                    } catch (_: Exception) {}

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
