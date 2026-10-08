package com.kota.asFramework.thread

import android.os.Looper
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import kotlin.time.Duration.Companion.milliseconds

/**
 * [ASCoroutine] - 協程與線程調度工具抽象類別。
 *
 * 職責：
 * 1. 簡化主執行緒 (Main)、背景執行緒 (IO) 任務的派發與協程管理。
 * 2. 提供延遲執行 ([postDelayed]) 與取消任務 ([cancel]) 機制。
 * 3. 靜態提供 [ensureMainThread] 確保區塊代碼在 UI 主執行緒中安全執行。
 */
abstract class ASCoroutine {
    private var job: Job? = null

    /** 子類別實作的非同步執行區塊 */
    abstract suspend fun run()

    /**
     * 延遲指定毫秒後在主執行緒執行
     *
     * @param delayMillis 延遲時間 (毫秒)
     */
    fun postDelayed(delayMillis: Long) {
        cancel()
        job = CoroutineScope(Dispatchers.Main).launch {
            delay(delayMillis.milliseconds)
            run()
        }
    }

    /** 取消任務 */
    fun cancel() {
        job?.cancel()
        job = null
    }

    companion object {

        /** 判斷當前呼叫是否位於主執行緒 */
        @JvmStatic
        val isMainThread: Boolean
            get() = Looper.getMainLooper().thread == Thread.currentThread()

        /** 在新背景協程內執行指定區塊 */
        @JvmStatic
        fun runInNewCoroutine(block: suspend () -> Unit) {
            CoroutineScope(Dispatchers.IO).launch {
                block()
            }
        }

        /**
         * 確保指定區塊在主執行緒中執行
         *
         * @param block 要在主執行緒呼叫的程式碼區塊
         */
        @JvmStatic
        fun ensureMainThread(block: () -> Unit) {
            if (isMainThread) {
                block()
            } else {
                CoroutineScope(Dispatchers.Main).launch {
                    block()
                }
            }
        }
    }
}
