package ru.jdev.q5.gathering

import android.os.Bundle
import android.service.notification.NotificationListenerService
import android.service.notification.StatusBarNotification
import ru.jdev.q5.Log


class NotificationsListener : NotificationListenerService() {

    lateinit var log: Log
    lateinit var checkNotifier: CheckNotifier

    override fun onCreate() {
        super.onCreate()
        log = Log(this)
        checkNotifier = CheckNotifier(this)
    }

    override fun onNotificationPosted(sbn: StatusBarNotification) {
        val extras: Bundle = sbn.notification.extras
        val text = extras.getCharSequence("android.text")?.toString() ?: "null"
        val title = extras.getString("android.title") ?: "null"

        log.print(sbn.packageName)
        log.print("title: $title")
        log.print("text: $text")

        val check = parseNotification(title, text)
        if (check != null) {
            checkNotifier.handleCheck(check)
        }
    }
}
