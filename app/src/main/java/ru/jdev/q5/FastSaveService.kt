package ru.jdev.q5

import android.app.IntentService
import android.app.NotificationManager
import android.content.Context
import android.content.Intent
import android.os.Handler
import android.util.Log
import org.jetbrains.anko.toast


class FastSaveService : IntentService("FastSaveService") {

    private lateinit var mHandler: Handler

    override fun onCreate() {
        super.onCreate()
        mHandler = Handler()
    }

    override fun onHandleIntent(intent: Intent?) {
        if (intent != null) {
            val manager = getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
            val nId = intent.getIntExtra("notificationId", -1)
            Log.d("FastSaveService", nId.toString())
            manager.cancel(nId)
            val trx = intent.getSerializableExtra("trx") as Transaction
            if (!TransactionLog.storeTrx(this, trx)) {
                mHandler.post { toast("Внешнее хранилище недоступно") }
            } else {
                mHandler.post { toast("Транзакция сохранена") }
            }
        }
    }
}
