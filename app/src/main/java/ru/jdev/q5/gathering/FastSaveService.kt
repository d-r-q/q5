package ru.jdev.q5.gathering

import android.app.IntentService
import android.app.NotificationManager
import android.content.Context
import android.content.Intent
import android.os.Handler
import android.util.Log
import org.jetbrains.anko.toast
import ru.jdev.q5.Transaction
import ru.jdev.q5.TransactionLog
import ru.jdev.q5.logTag


class FastSaveService : IntentService("FastSaveService") {

    private val tag = logTag<FastSaveService>()

    private val trxLog = TransactionLog(this)
    private lateinit var mHandler: Handler

    override fun onCreate() {
        super.onCreate()
        mHandler = Handler()
    }

    override fun onHandleIntent(intent: Intent?) {
        Log.d(tag(), "FastSaveService started")
        if (intent != null) {
            Log.d(tag(), "intent is not null")
            val manager = getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
            val nId = intent.getIntExtra("notificationId", -1)
            Log.d(tag(), nId.toString())
            manager.cancel(nId)
            val trx = intent.getSerializableExtra("trx") as Transaction
            if (!trxLog.storeTrx(trx)) {
                mHandler.post { toast("Внешнее хранилище недоступно") }
            } else {
                mHandler.post { toast("Транзакция сохранена") }
            }
        }
    }
}
