package com.kota.Bahamut.service

import android.util.Log
import com.kota.Bahamut.service.NotificationSettings.getCloudSave
import com.kota.Bahamut.service.NotificationSettings.isCloudDirty
import com.kota.Bahamut.service.NotificationSettings.setCloudDirty
import com.kota.asFramework.thread.ASCoroutine
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.delay
import kotlinx.coroutines.isActive
import kotlinx.coroutines.launch

/**
 * 雲端同步管理器 (SyncManager)
 * 負責協調雲端備份與還原、異動標記 (isDirty)、10分鐘定期輪巡、變更防抖 (Debounce)、登出/離開前等待同步以及登入後下載。
 */
object SyncManager {
    private const val TAG = "SyncManager"
    private const val PERIODIC_INTERVAL_MS = 10 * 60 * 1000L // 10 分鐘
    private const val DEBOUNCE_DELAY_MS = 5 * 1000L // 5 秒防抖

    private val syncScope = CoroutineScope(Dispatchers.IO + SupervisorJob())
    private var periodicJob: Job? = null
    private var debounceJob: Job? = null
    private var isSyncing = false

    /** 資料還原完成的監聽器 (通知 UI 刷新) */
    var onDataRestoredListener: (() -> Unit)? = null

    /**
     * 標記資料已變更 (Dirty)
     * 當書籤、偏好設定等被修改時呼叫此方法。
     */
    fun markDirty() {
        if (!getCloudSave()) return
        setCloudDirty(true)
        Log.d(TAG, "markDirty: 資料已標記為有變更 (isDirty = true)")
        scheduleDebounceUpload()
    }

    /**
     * 排程防抖上傳
     * 變更後 5 秒內無新動作則自動背景上傳，避免每次微小異動都打 API。
     */
    private fun scheduleDebounceUpload() {
        debounceJob?.cancel()
        debounceJob = syncScope.launch {
            delay(DEBOUNCE_DELAY_MS)
            if (isCloudDirty() && getCloudSave()) {
                Log.d(TAG, "防抖計時器到期，執行背景上傳")
                uploadInternal()
            }
        }
    }

    /**
     * 啟動 10 分鐘定時輪巡上傳
     * 建議在連線成功或登入成功後啟動。
     */
    fun startPeriodicSync() {
        if (periodicJob?.isActive == true) return
        Log.d(TAG, "啟動 10 分鐘定時輪巡")
        periodicJob = syncScope.launch {
            while (isActive) {
                delay(PERIODIC_INTERVAL_MS)
                if (isCloudDirty() && getCloudSave()) {
                    Log.d(TAG, "10 分鐘定時檢查：偵測到未同步變更，執行上傳")
                    uploadInternal()
                }
            }
        }
    }

    /**
     * 停止定時輪巡與防抖任務
     */
    fun stopPeriodicSync() {
        periodicJob?.cancel()
        periodicJob = null
        debounceJob?.cancel()
        debounceJob = null
        Log.d(TAG, "已停止定時輪巡與防抖任務")
    }

    /**
     * 執行上傳內部實作
     * @param callback 回呼告知上傳是否成功
     */
    @Synchronized
    fun uploadInternal(callback: ((Boolean) -> Unit)? = null) {
        if (!getCloudSave()) {
            callback?.invoke(false)
            return
        }
        if (isSyncing) {
            Log.d(TAG, "已有同步任務進行中，略過本次上傳請求")
            callback?.invoke(false)
            return
        }

        isSyncing = true
        val cloudBackup = CloudBackup()
        cloudBackup.backup { success, errorMsg ->
            isSyncing = false
            if (success) {
                setCloudDirty(false)
                Log.d(TAG, "雲端備份成功，清除 isDirty 標記")
            } else {
                Log.w(TAG, "雲端備份失敗 ($errorMsg)，保留 isDirty 標記待下次重試")
            }
            callback?.invoke(success)
        }
    }

    /**
     * 登出離開前同步上傳
     * 當使用者主動按下「登出」或「離開 App」時呼叫。
     * 改為純背景靜默執行，不給使用者看到任何視窗，直接放行離開流程。
     */
    fun uploadBeforeExit(onComplete: () -> Unit) {
        if (getCloudSave() && isCloudDirty()) {
            Log.d(TAG, "登出/離開：背景靜默執行雲端上傳")
            uploadInternal()
        }
        onComplete()
    }

    /**
     * 斷線時處理
     * 停止定時輪巡；若有未同步變更嘗試最後背景上傳（若無網路或失敗則保留 isDirty）。
     */
    fun onConnectionClosed() {
        stopPeriodicSync()
        if (getCloudSave() && isCloudDirty()) {
            Log.d(TAG, "連線中斷，嘗試背景同步未完成之變更")
            uploadInternal()
        }
    }

    /**
     * 登入成功後的同步流程
     * 1. 啟動 10 分鐘定時輪巡
     * 2. 若本地有未同步修改 (isDirty == true)，優先補傳上雲端，避免被舊雲端覆蓋
     * 3. 若本地無未同步修改，執行 restore 下載最新雲端設定，並通知 UI 刷新
     */
    fun performLoginSync(onComplete: (() -> Unit)? = null) {
        startPeriodicSync()

        if (!getCloudSave()) {
            onComplete?.invoke()
            return
        }

        if (isCloudDirty()) {
            Log.d(TAG, "登入同步：本地有未同步變更，優先補傳至雲端")
            uploadInternal { _ ->
                onComplete?.invoke()
            }
        } else {
            Log.d(TAG, "登入同步：本地無未同步變更，執行雲端下載 (Restore)")
            val cloudBackup = CloudBackup()
            cloudBackup.restore { success, errorMsg ->
                if (success) {
                    Log.d(TAG, "登入還原成功，通知 UI 刷新")
                    ASCoroutine.ensureMainThread {
                        onDataRestoredListener?.invoke()
                    }
                } else {
                    Log.w(TAG, "登入還原失敗：$errorMsg")
                }
                onComplete?.invoke()
            }
        }
    }

    /**
     * 清理資源
     */
    fun cleanup() {
        stopPeriodicSync()
        onDataRestoredListener = null
    }
}

