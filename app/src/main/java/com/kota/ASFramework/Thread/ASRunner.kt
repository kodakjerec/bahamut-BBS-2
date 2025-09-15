package com.kota.ASFramework.Thread

import android.annotation.SuppressLint
import android.os.Build
import android.os.Handler
import android.os.Looper
import android.os.Message
import java.util.concurrent.atomic.AtomicInteger

abstract class ASRunner {
    companion object {
        private val mainLooper = Looper.getMainLooper()
        private var _main_thread: Thread? = null
        private var mainHandler: Handler? = null
        private val tokenGenerator = AtomicInteger()
        
        @SuppressLint("HandlerLeak")
        fun construct() {
            _main_thread = Thread.currentThread()
            mainHandler = object : Handler(mainLooper) {
                override fun handleMessage(message: Message) {
                    val runner = message.obj as ASRunner
                    runner.run()
                }
            }
        }
        
        fun isMainThread(): Boolean {
            return Thread.currentThread() == _main_thread
        }
        
        /** 在新執行序內執行 */
        fun runInNewThread(runnable: Runnable) {
            val thread = Thread(runnable)
            thread.start()
        }
    }
    
    private var token = 0
    private var runnable: Runnable? = null
    
    abstract fun run()
    
    /** 在主執行序內執行 */
    fun runInMainThread(): ASRunner {
        if (Thread.currentThread() == _main_thread) {
            run()
        } else {
            val message = Message().apply {
                obj = this@ASRunner
            }
            mainHandler?.sendMessage(message)
        }
        return this
    }
    
    /** 延遲執行 */
    fun postDelayed(delayMillis: Int) {
        // 先取消之前的任務
        cancel()
        
        token = tokenGenerator.incrementAndGet()
        
        runnable = Runnable { run() }
        
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.P) {
            mainHandler?.postDelayed(runnable!!, token.toLong(), delayMillis.toLong())
        } else {
            mainHandler?.postDelayed(runnable!!, delayMillis.toLong())
        }
    }
    
    /** 取消執行 */
    fun cancel() {
        runnable?.let { r ->
            mainHandler?.removeCallbacks(r, token)
        }
        runnable = null
    }
    
    /** 釋放資源 */
    fun release() {
        cancel()
    }
}
