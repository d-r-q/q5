package ru.jdev.q5.gathering

import android.os.Bundle
import android.service.notification.NotificationListenerService
import android.service.notification.StatusBarNotification
import ru.jdev.q5.Log
import java.util.WeakHashMap


class NotificationsListener : NotificationListenerService() {

    lateinit var log: Log
    lateinit var checkNotifier: CheckNotifier
    private val processed: MutableMap<Int, Int> = WeakHashMap<Int, Int>()

    override fun onCreate() {
        super.onCreate()
        log = Log(this)
        checkNotifier = CheckNotifier(this)
    }

    override fun onNotificationPosted(sbn: StatusBarNotification) {
        if (sbn.packageName.startsWith("org.kde") ||
                sbn.packageName.startsWith("android") ||
                sbn.id in processed) {
            return
        }
        val extras: Bundle = sbn.notification.extras
        val text = extras.getCharSequence("android.text")?.toString() ?: "null"
        val title = extras.getString("android.title") ?: "null"

        log.print(sbn.packageName)
        log.print("title: $title")
        log.print("text: $text")

        val check = parseNotification(title, text)
        if (check != null) {
            checkNotifier.handleCheck(check)
            processed[sbn.id] = sbn.id
        }
    }
}
