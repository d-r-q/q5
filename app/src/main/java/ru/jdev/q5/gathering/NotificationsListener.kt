package ru.jdev.q5.gathering

import android.content.Intent
import android.os.Bundle
import android.os.IBinder
import android.service.notification.NotificationListenerService
import android.service.notification.StatusBarNotification
import android.util.Log
import ru.jdev.q5.gathering.patterns.CheckPatternsModule
import ru.jdev.q5.logTag
import java.util.*


class NotificationsListener : NotificationListenerService() {

    private val tag = logTag<NotificationsListener>()

    private lateinit var checkNotifier: CheckNotifier

    private val processed: MutableMap<Int, Int> = WeakHashMap()

    override fun onCreate() {
        super.onCreate()
        checkNotifier = CheckNotifier(this)
        Log.d(tag(), "onCreate")
    }

    override fun onListenerConnected() {
        Log.d(tag(), "onListenerConnected")
    }

    override fun onListenerDisconnected() {
        Log.d(tag(), "onListenerDisconnected")
    }

    override fun onBind(intent: Intent?): IBinder? {
        Log.d(tag(), "OnBind")
        return super.onBind(intent)
    }

    override fun onNotificationPosted(sbn: StatusBarNotification) {
        try {
            if (sbn.id in processed) {
                return
            }
            val extras: Bundle = sbn.notification.extras
            val pkg = sbn.packageName
            val text = extras.getCharSequence("android.text")?.toString() ?: "null"
            val title = extras.getString("android.title") ?: "null"
            if (pkg.contains("(alfa|sber|kukur|google)".toRegex())) {
                Log.i(tag(), "Check candidate: $title, $text")
            }

            val check = CheckParser().tryParse(CheckPatternsModule.checkPatterns.listPatterns(), "$title\n$text")
            if (check != null) {
                checkNotifier.handleCheck(check)
                processed[sbn.id] = sbn.id
            }
        } catch (t: Throwable) {
            Log.e(tag(), "Notification handling error", t)
        }
    }
}
