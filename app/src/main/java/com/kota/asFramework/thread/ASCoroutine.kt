package com.kota.asFramework.thread

import android.os. Looper
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines. Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch

abstract class ASCoroutine {

    private var job: Job?  = null

    abstract suspend fun run()

    /** 在主執行緒內執行 */
    fun runInMainThread() {
        CoroutineScope(Dispatchers.Main).launch {
            run()
        }
    }

    /** 在背景執行緒執行 */
    fun runInBackground() {
        CoroutineScope(Dispatchers.IO).launch {
            run()
        }
    }

    /** 延遲執行 */
    fun postDelayed(delayMillis: Long) {
        cancel()
        job = CoroutineScope(Dispatchers.Main).launch {
            delay(delayMillis)
            run()
        }
    }

    /** 取消執行 */
    fun cancel() {
        job?.cancel()
        job = null
    }

    companion object {

        /** 判斷是否在主執行緒 */
        @JvmStatic
        val isMainThread: Boolean
            get() = Looper.getMainLooper().thread == Thread.currentThread()

        /** 在新協程內執行 */
        @JvmStatic
        fun runInNewCoroutine(block: suspend () -> Unit) {
            CoroutineScope(Dispatchers.IO).launch {
                block()
            }
        }

        /** 在主執行緒執行（靜態方法） */
        @JvmStatic
        fun runOnMain(block: () -> Unit): Job {
            return CoroutineScope(Dispatchers.Main).launch {
                block()
            }
        }

        /** 確保在主執行緒執行 */
        @JvmStatic
        fun ensureMainThread(block: () -> Unit) {
            if (isMainThread) {
                block()
            } else {
                runOnMain(block)
            }
        }
    }
}