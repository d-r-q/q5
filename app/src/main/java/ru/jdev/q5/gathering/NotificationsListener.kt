package ru.jdev.q5.gathering

import android.os.Bundle
import android.service.notification.NotificationListenerService
import android.service.notification.StatusBarNotification
import ru.jdev.q5.logTag
import java.util.*


class NotificationsListener : NotificationListenerService() {

    private val tag = logTag<NotificationsListener>()

    private lateinit var checkNotifier: CheckNotifier

    private val processed: MutableMap<Int, Int> = WeakHashMap<Int, Int>()

    override fun onCreate() {
        super.onCreate()
        checkNotifier = CheckNotifier(this)
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
