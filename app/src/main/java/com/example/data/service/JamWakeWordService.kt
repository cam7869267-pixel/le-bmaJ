package com.example.data.service

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
import android.util.Log
import androidx.core.app.NotificationCompat
import androidx.core.app.ServiceCompat
import com.example.MainActivity
import com.example.R
import com.example.data.wakeword.JamWakeWordEngine
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow

class JamWakeWordService : Service() {

    private lateinit var wakeWordEngine: JamWakeWordEngine

    override fun onCreate() {
        super.onCreate()
        wakeWordEngine = JamWakeWordEngine.getInstance(applicationContext)
        createNotificationChannels()
    }

    override fun onStartCommand(intent: Intent?, flags: Int, startId: Int): Int {
        val action = intent?.action

        if (action == ACTION_STOP_SERVICE) {
            stopForegroundService()
            return START_NOT_STICKY
        }

        startForegroundWithNotification()
        startListening()

        return START_STICKY
    }

    private fun startForegroundWithNotification() {
        val notification = buildPersistentNotification()
        try {
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
                ServiceCompat.startForeground(
                    this,
                    NOTIFICATION_ID,
                    notification,
                    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.R) {
                        ServiceInfo.FOREGROUND_SERVICE_TYPE_MICROPHONE
                    } else {
                        0
                    }
                )
            } else {
                startForeground(NOTIFICATION_ID, notification)
            }
            _isServiceRunning.value = true
        } catch (e: Exception) {
            Log.e(TAG, "Error starting foreground service: ${e.message}")
            try {
                startForeground(NOTIFICATION_ID, notification)
                _isServiceRunning.value = true
            } catch (inner: Exception) {
                Log.e(TAG, "Fallback startForeground failed: ${inner.message}")
            }
        }
    }

    private fun startListening() {
        wakeWordEngine.setOnWakeWordListener { keyword ->
            handleWakeWordDetected(keyword)
        }
        wakeWordEngine.startListening()
    }

    private fun handleWakeWordDetected(keyword: String) {
        Log.i(TAG, "Wake word '$keyword' detected in background service! Launching Jumble P...")

        // 1. Launch / Bring MainActivity to front
        try {
            val launchIntent = Intent(applicationContext, MainActivity::class.java).apply {
                action = ACTION_WAKE_WORD_TRIGGERED
                putExtra(EXTRA_WAKE_WORD, keyword)
                putExtra(EXTRA_TRIGGER_JAM, true)
                flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_SINGLE_TOP
            }
            startActivity(launchIntent)
        } catch (e: Exception) {
            Log.e(TAG, "Error launching activity from service: ${e.message}")
        }

        // 2. Also show an instant Heads-up Notification for quick tap & visual confirmation
        postWakeWordAlertNotification(keyword)
    }

    private fun buildPersistentNotification(): Notification {
        val openAppIntent = Intent(this, MainActivity::class.java).apply {
            action = Intent.ACTION_MAIN
            flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_SINGLE_TOP
        }
        val openAppPendingIntent = PendingIntent.getActivity(
            this,
            101,
            openAppIntent,
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        )

        val stopIntent = Intent(this, JamWakeWordService::class.java).apply {
            action = ACTION_STOP_SERVICE
        }
        val stopPendingIntent = PendingIntent.getService(
            this,
            102,
            stopIntent,
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        )

        return NotificationCompat.Builder(this, CHANNEL_ID)
            .setContentTitle("Jumble P Voice Listener")
            .setContentText("🎙️ Listening for wake word 'Jam'...")
            .setSmallIcon(R.mipmap.ic_launcher)
            .setOngoing(true)
            .setPriority(NotificationCompat.PRIORITY_LOW)
            .setContentIntent(openAppPendingIntent)
            .addAction(
                android.R.drawable.ic_menu_camera,
                "Open App",
                openAppPendingIntent
            )
            .addAction(
                android.R.drawable.ic_menu_close_clear_cancel,
                "Stop Listening",
                stopPendingIntent
            )
            .build()
    }

    private fun postWakeWordAlertNotification(keyword: String) {
        val launchIntent = Intent(this, MainActivity::class.java).apply {
            action = ACTION_WAKE_WORD_TRIGGERED
            putExtra(EXTRA_WAKE_WORD, keyword)
            putExtra(EXTRA_TRIGGER_JAM, true)
            flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_SINGLE_TOP
        }
        val pendingIntent = PendingIntent.getActivity(
            this,
            103,
            launchIntent,
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        )

        val alertNotification = NotificationCompat.Builder(this, ALERT_CHANNEL_ID)
            .setContentTitle("⚡ '$keyword' Wake Word Detected!")
            .setContentText("Jumble P AI interface activated. Tap to interact.")
            .setSmallIcon(R.mipmap.ic_launcher)
            .setPriority(NotificationCompat.PRIORITY_HIGH)
            .setDefaults(NotificationCompat.DEFAULT_ALL)
            .setAutoCancel(true)
            .setContentIntent(pendingIntent)
            .build()

        val notificationManager = getSystemService(Context.NOTIFICATION_SERVICE) as? NotificationManager
        notificationManager?.notify(ALERT_NOTIFICATION_ID, alertNotification)
    }

    private fun createNotificationChannels() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val notificationManager = getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager

            val persistentChannel = NotificationChannel(
                CHANNEL_ID,
                "Jumble P Wake Word Listener",
                NotificationManager.IMPORTANCE_LOW
            ).apply {
                description = "Shows status while continuously listening for the 'Jam' wake word"
                setShowBadge(false)
            }

            val alertChannel = NotificationChannel(
                ALERT_CHANNEL_ID,
                "Jumble P Wake Word Alerts",
                NotificationManager.IMPORTANCE_HIGH
            ).apply {
                description = "Alerts triggered when 'Jam' is detected in the background"
                enableVibration(true)
                setShowBadge(true)
            }

            notificationManager.createNotificationChannel(persistentChannel)
            notificationManager.createNotificationChannel(alertChannel)
        }
    }

    private fun stopForegroundService() {
        wakeWordEngine.stopListening()
        _isServiceRunning.value = false
        stopForeground(STOP_FOREGROUND_REMOVE)
        stopSelf()
    }

    override fun onDestroy() {
        super.onDestroy()
        wakeWordEngine.stopListening()
        _isServiceRunning.value = false
    }

    override fun onBind(intent: Intent?): IBinder? = null

    companion object {
        private const val TAG = "JamWakeWordService"
        const val CHANNEL_ID = "jumblep_wakeword_channel"
        const val ALERT_CHANNEL_ID = "jumblep_wakeword_alerts"
        const val NOTIFICATION_ID = 2001
        const val ALERT_NOTIFICATION_ID = 2002

        const val ACTION_START_SERVICE = "com.example.action.START_WAKE_WORD_SERVICE"
        const val ACTION_STOP_SERVICE = "com.example.action.STOP_WAKE_WORD_SERVICE"
        const val ACTION_WAKE_WORD_TRIGGERED = "com.example.action.WAKE_WORD_TRIGGERED"
        const val EXTRA_WAKE_WORD = "extra_wake_word"
        const val EXTRA_TRIGGER_JAM = "extra_trigger_jam"

        private val _isServiceRunning = MutableStateFlow(false)
        val isServiceRunning: StateFlow<Boolean> = _isServiceRunning.asStateFlow()

        fun start(context: Context) {
            val intent = Intent(context, JamWakeWordService::class.java).apply {
                action = ACTION_START_SERVICE
            }
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                context.startForegroundService(intent)
            } else {
                context.startService(intent)
            }
            _isServiceRunning.value = true
        }

        fun stop(context: Context) {
            val intent = Intent(context, JamWakeWordService::class.java).apply {
                action = ACTION_STOP_SERVICE
            }
            context.startService(intent)
            _isServiceRunning.value = false
        }
    }
}
