package ru.jdev.q5.gathering

import android.content.Intent
import android.os.Bundle
import android.os.IBinder
import android.service.notification.NotificationListenerService
import android.service.notification.StatusBarNotification
import android.util.Log
import ru.jdev.q5.logTag
import java.util.*


class NotificationsListener : NotificationListenerService() {

    private val tag = logTag<NotificationsListener>()

    private lateinit var checkNotifier: CheckNotifier

    private val processed: MutableMap<Int, Int> = WeakHashMap<Int, Int>()

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
        if (sbn.id in processed) {
            return
        }
        val extras: Bundle = sbn.notification.extras
        val text = extras.getCharSequence("android.text")?.toString() ?: "null"
        val title = extras.getString("android.title") ?: "null"

        val check = parseNotification(title, text)
        if (check != null) {
            checkNotifier.handleCheck(check)
            processed[sbn.id] = sbn.id
        }
    }
}
