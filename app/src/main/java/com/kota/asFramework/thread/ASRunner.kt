package com.kota.asFramework.thread

import android.annotation.SuppressLint
import android.os.Build
import android.os.Handler
import android.os.Looper
import android.os.Message
import java.util.concurrent.atomic.AtomicInteger

abstract class ASRunner {
    private var token = 0
    private var runnable: Runnable? = null
    abstract fun run()

    /** 在主執行序內執行  */
    fun runInMainThread(): ASRunner {
        if (Thread.currentThread() === mainThread) {
            run()
        } else {
            val message = Message()
            message.obj = this
            mainHandler!!.sendMessage(message)
        }
        return this
    }

    /** 延遲執行  */
    fun postDelayed(delayMillis: Int) {
        // 先取消之前的任務
        cancel()

        token = tokenGenerator.incrementAndGet()

        runnable = Runnable { this.run() }
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.P) {
            mainHandler!!.postDelayed(runnable!!, token, delayMillis.toLong())
        } else {
            mainHandler!!.postDelayed(runnable!!, delayMillis.toLong())
        }
    }

    /** 取消執行  */
    fun cancel() {
        if (runnable != null) {
            mainHandler!!.removeCallbacks(runnable!!, token)
        }
        runnable = null
    }

    /** 釋放資源  */
    fun release() {
        cancel()
    }

    companion object {
        var mainLooper: Looper = Looper.getMainLooper()
        var mainThread: Thread? = null
        var mainHandler: Handler? = null
        private val tokenGenerator = AtomicInteger()

        @JvmStatic
        @SuppressLint("HandlerLeak")
        fun construct() {
            mainThread = Thread.currentThread()
            mainHandler = object : Handler(mainLooper) {
                // android.os.Handler
                override fun handleMessage(message: Message) {
                    val runner = message.obj as ASRunner
                    runner.run()
                }
            }
        }

        val isMainThread: Boolean
            get() = Thread.currentThread() === mainThread

        /** 在新執行序內執行  */
        @JvmStatic
        fun runInNewThread(runnable: Runnable?) {
            val thread = Thread(runnable)
            thread.start()
        }
    }
}