package com.kota.Bahamut.service

import android.util.Log
import androidx.appcompat.app.AppCompatDelegate
import com.google.gson.GsonBuilder
import com.google.gson.JsonPrimitive
import com.google.gson.JsonSerializer
import com.kota.Bahamut.R
import com.kota.Bahamut.service.NotificationSettings.getShowCloudSave
import com.kota.Bahamut.service.NotificationSettings.setShowCloudSave
import com.kota.asFramework.dialog.ASAlertDialog
import com.kota.asFramework.thread.ASCoroutine
import com.kota.asFramework.ui.ASToast
import okhttp3.MultipartBody
import okhttp3.OkHttpClient
import okhttp3.Request
import okhttp3.RequestBody
import org.json.JSONObject
import java.text.SimpleDateFormat
import java.util.Locale

/**
 * 雲端備份與還原管理類別 (`CloudBackup`)
 *
 * 負責處理使用者設定 (`UserSettings`) 與書籤資料 (`BookmarkStore`) 的雲端備份、下載還原、
 * 雲端狀態檢查以及詢問使用者的對話框流程。
 */
class CloudBackup {

    /**
     * 將日期時間字串解析轉換為毫秒時間戳 (Long)
     *
     * @param timeString 要解析的時間字串 (例如 "2026/10/07 23:50:00")
     * @return 解析成功傳回毫秒時間戳，若傳入 null、空字串或解析失敗則傳回 0L
     */
    fun parseTimeToLong(timeString: String?): Long {
        if (timeString.isNullOrEmpty()) return 0L

        /** 支援解析的日期時間格式清單 */
        val formats = listOf(
            "yyyy/MM/dd HH:mm:ss",
            "yyyy/M/d HH:mm:ss",
            "yyyy/MM/dd a hh:mm:ss",
            "yyyy/M/d a hh:mm:ss",
            "yyyy-MM-dd HH:mm:ss",
            "yyyy-M-d HH:mm:ss",
            "yyyy-MM-dd'T'HH:mm:ss"
        )
        for (format in formats) {
            try {
                /** 依特定格式建立的 SimpleDateFormat 解析器 */
                val sdf = SimpleDateFormat(format, Locale.getDefault())
                /** 解析後得到的 Date 物件 */
                val date = sdf.parse(timeString)
                if (date != null) return date.time
            } catch (_: Exception) {
            }
        }
        return 0L
    }

    /**
     * 向使用者詢問是否啟用雲端備份功能
     *
     * 若尚未詢問過，會彈出對話框讓使用者選擇；若已設定開啟，則直接進行雲端檢查。
     *
     * @return 傳回 `this` 實例以支援鏈式呼叫
     */
    fun askCloudSave(): CloudBackup {
        try {
            // 詢問是否啟用雲端備份
            when (getShowCloudSave()) {
                false -> {
                    ASCoroutine.ensureMainThread {
                        ASAlertDialog.createDialog()
                            .setTitle(CommonFunctions.getContextString(R.string.cloud_save))
                            .setMessage(CommonFunctions.getContextString(R.string.cloud_save_question))
                            .addButton(CommonFunctions.getContextString(R.string.cancel))
                            .addButton(CommonFunctions.getContextString(R.string.on))
                            .setDefaultButtonIndex(0)
                            .setListener { _, index ->
                                if (index == 1) {
                                    // 決定同步, 檢查雲端存檔是否存在
                                    NotificationSettings.setCloudSave(true)
                                    checkCloud()
                                } else {
                                    // 取消同步, 以本地端為主
                                    NotificationSettings.setCloudSave(false)
                                    final()
                                }
                            }.show()
                    }
                }
                else -> {
                    // 不用再次詢問，若已開啓雲端備份則直接檢查
                    if (NotificationSettings.getCloudSave()) {
                        checkCloud()
                    } else
                        final()
                }
            }
            setShowCloudSave(true)
        } catch (_: Exception) {
            final()
        }
        return this
    }

    /**
     * 向雲端伺服器查詢目前使用者是否已存在雲端備份檔
     */
    private fun checkCloud() {
        /** 經過 AES 加密的使用者帳號標識 */
        val userId = AESCrypt.encrypt(UserSettings.propertiesUsername)
        /** 查詢雲端備份狀態的 API 網址 */
        val apiUrl = "https://cloud-restore.kodakjerec.work/"
        /** 用於發送 HTTP 請求的 OkHttpClient 實例 */
        val client = OkHttpClient()
        /** 包含 userId 與查詢型態 (check) 的 POST 請求表單內容 */
        val body: RequestBody = MultipartBody.Builder().setType(MultipartBody.FORM)
            .addFormDataPart("userId", userId)
            .addFormDataPart("queryType", "check")
            .build()
        /** 建立的 POST HTTP 請求物件 */
        val request: Request = Request.Builder()
            .url(apiUrl)
            .post(body)
            .build()

        ASCoroutine.runInNewCoroutine {
            client.newCall(request).execute().use { response ->
                /** 伺服器傳回的原始回應字串 */
                val data = response.body.string()
                /** 解析後的 JSON 回應物件 */
                val jsonObject = JSONObject(data)
                /** 伺服器傳回的錯誤訊息 (若無錯誤則為空字串) */
                val status = jsonObject.optString("error")
                if (status.isNotEmpty()) {
                    ASToast.showShortToast("雲端備份失敗：$status")
                } else {
                    /** 伺服器傳回的上一次雲端備份時間字串 */
                    val lastTime = jsonObject.getString("lastTime")
                    if (lastTime.isEmpty()) {
                        ASToast.showShortToast(CommonFunctions.getContextString(R.string.cloud_save_result1))
                        // 沒有雲端備份, 直接以本地端備份覆蓋雲端
                        backup()
                        final()
                    } else {
                        // 已有雲端備份，彈出第二個對話框讓使用者選擇備份來源
                        askCloudSave2(lastTime)
                    }
                }
            }
        }
    }

    /**
     * 當雲端已存在備份時，彈出對話框讓使用者選擇採用「本地存檔」覆蓋雲端，或是「雲端存檔」覆蓋本地
     *
     * @param lastTime 雲端備份檔的最後更新時間字串
     */
    private fun askCloudSave2(lastTime: String) {
        // 雲端存在, 選擇本地或雲端
        ASCoroutine.ensureMainThread {
            ASAlertDialog.createDialog()
                .setTitle(CommonFunctions.getContextString(R.string.cloud_save))
                .setMessage("已有雲端備份：\n$lastTime\n採用 本地存檔\n或 雲端存檔？")
                .addButton(CommonFunctions.getContextString(R.string.cloud_save_local))
                .addButton(CommonFunctions.getContextString(R.string.cloud_save))
                .setDefaultButtonIndex(0)
                .setListener { _: ASAlertDialog?, index: Int ->
                    if (index == 0) {
                        // 選擇本地 => 以本地端資料覆蓋雲端
                        backup()
                        ASToast.showShortToast(CommonFunctions.getContextString(R.string.cloud_save_result1))
                    } else {
                        // 選擇雲端 => 以雲端資料覆蓋本地
                        restore()
                        ASToast.showShortToast(CommonFunctions.getContextString(R.string.cloud_save_result2))
                    }
                }.show()
        }
    }

    /**
     * 將本地端資料（包含書籤與使用者設定）打包加密後上傳備份至雲端伺服器
     *
     * @param callback 備份完成後的回呼函式，傳入 `(isSuccess: Boolean, errorMsg: String?)`
     */
    fun backup(callback: ((Boolean, String?) -> Unit)? = null) {
        try {
            /** 經過 AES 加密的使用者帳號標識 */
            val userId = AESCrypt.encrypt(UserSettings.propertiesUsername)
            if (userId.isEmpty()) {
                callback?.invoke(false, "User ID is empty")
                final()
                return
            }

            /** 包含所有備份資料 (書籤、設定) 的核心 JSON 物件 */
            val jsonObject = JSONObject()
            /** 用於 JSON 序列化/反序列化的 Gson 實例 */
            val gson = GsonBuilder()
                .registerTypeAdapter(
                    Double::class.java,
                    JsonSerializer<Double> { src, _, _ ->
                        if (src == 0.0) JsonPrimitive(
                            "0.0"
                        ) else JsonPrimitive(src)
                    })
                .create()

            // 1. 打包書籤資料 (BookmarkStore)
            jsonObject.put("bookmark", TempSettings.bookmarkStore?.exportToJSON().toString())

            // 2. 打包使用者設定 (UserSettings)，排除不同步至雲端的本地設定 (參考 UserSettings.IGNORED_KEYS)
            val filteredSettings = UserSettings.mySharedPref?.all?.filterKeys { key ->
                UserSettings.IGNORED_KEYS.none { it.equals(key, ignoreCase = true) }
            }
            jsonObject.put("user_settings", filteredSettings)

            // 3. 將整體 JSON 資料進行 AES 加密
            val jsonDataString = AESCrypt.encrypt(gson.toJson(jsonObject))

            // 4. 發送 HTTP POST 請求上傳至雲端伺服器
            val apiUrl = "https://cloud-backup.kodakjerec.work/"
            val client = OkHttpClient()
            val body: RequestBody = MultipartBody.Builder().setType(MultipartBody.FORM)
                .addFormDataPart("userId", userId)
                .addFormDataPart("jsonData", jsonDataString)
                .build()
            val request: Request = Request.Builder()
                .url(apiUrl)
                .post(body)
                .build()

            ASCoroutine.runInNewCoroutine {
                /** 標記備份任務是否成功執行 */
                var isSuccess = false
                /** 記錄備份失敗時的錯誤訊息 */
                var errorMsg: String? = null
                try {
                    client.newCall(request).execute().use { response ->
                        /** 伺服器傳回的回應內容字串 */
                        val data = response.body.string()
                        /** 解析後的伺服器回應 JSON 物件 */
                        val fromJsonObject = JSONObject(data)
                        /** 伺服器傳回的錯誤代碼/訊息 */
                        val error = fromJsonObject.optString("error")
                        if (error.isNotEmpty()) {
                            errorMsg = error
                            ASToast.showShortToast("雲端備份失敗：$error")
                        } else {
                            // 記錄雲端備份成功的時間戳記
                            val lastTimeLong = parseTimeToLong(fromJsonObject.optString("lastTime", ""))
                            TempSettings.cloudSaveLastTime = lastTimeLong
                            NotificationSettings.setCloudSaveLastTime(lastTimeLong)
                            isSuccess = true
                        }
                    }
                } catch (e: Exception) {
                    Log.d(javaClass.simpleName, e.toString())
                    errorMsg = e.message
                } finally {
                    callback?.invoke(isSuccess, errorMsg)
                    final()
                }
            }
        } catch (e: Exception) {
            Log.d(javaClass.simpleName, e.toString())
            callback?.invoke(false, e.message)
            final()
        }
    }

    /**
     * 從雲端伺服器下載最新的備份資料並還原覆蓋至本地端（書籤與使用者設定）
     *
     * @param callback 還原完成後的回呼函式，傳入 `(isSuccess: Boolean, errorMsg: String?)`
     */
    fun restore(callback: ((Boolean, String?) -> Unit)? = null) {
        try {
            /** 經過 AES 加密的使用者帳號標識 */
            val userId = AESCrypt.encrypt(UserSettings.propertiesUsername)
            if (userId.isEmpty()) {
                callback?.invoke(false, "User ID is empty")
                final()
                return
            }

            /** 下載雲端還原資料的 API 網址 */
            val apiUrl = "https://cloud-restore.kodakjerec.work/"
            /** 用於發送 HTTP 請求的 OkHttpClient 實例 */
            val client = OkHttpClient()
            /** 包含 userId 與查詢類別 (all) 的 POST 請求表單內容 */
            val body: RequestBody = MultipartBody.Builder().setType(MultipartBody.FORM)
                .addFormDataPart("userId", userId)
                .addFormDataPart("queryType", "all")
                .build()
            /** 建立的 POST HTTP 請求物件 */
            val request: Request = Request.Builder()
                .url(apiUrl)
                .post(body)
                .build()

            ASCoroutine.runInNewCoroutine {
                /** 標記還原任務是否成功執行 */
                var isSuccess = false
                /** 記錄還原失敗時的錯誤訊息 */
                var errorMsg: String? = null
                try {
                    client.newCall(request).execute().use { response ->
                        /** 伺服器傳回的回應內容字串 */
                        val data = response.body.string()
                        /** 解析後的伺服器回應 JSON 物件 */
                        val jsonObject = JSONObject(data)
                        /** 伺服器傳回的錯誤狀態訊息 */
                        val error = jsonObject.optString("error")
                        if (error.isNotEmpty()) {
                            errorMsg = error
                            ASToast.showShortToast("雲端備份失敗：$error")
                        } else {
                            /** 用於 JSON 序列化/反序列化的 Gson 實例 */
                            val gson = GsonBuilder()
                                .registerTypeAdapter(
                                    Double::class.java,
                                    JsonSerializer<Double> { src, _, _ ->
                                        if (src == 0.0) JsonPrimitive(
                                            "0.0"
                                        ) else JsonPrimitive(src)
                                    })
                                .create()

                            // 更新本地記錄的雲端最後備份時間
                            val lastTimeLong = parseTimeToLong(jsonObject.optString("lastTime", ""))
                            TempSettings.cloudSaveLastTime = lastTimeLong
                            NotificationSettings.setCloudSaveLastTime(lastTimeLong)

                            /** 經過加密的雲端備份 JSON 資料字串 */
                            val jsonDataString = jsonObject.getString("jsonData")
                            /** 解密並解析後的雲端備份核心 JSON 物件 */
                            val fromJsonObject = gson.fromJson(
                                AESCrypt.decrypt(jsonDataString),
                                JSONObject::class.java
                            )
                            /** 雲端 JSON 中的 user_settings 設定 Key-Value 字典 */
                            val userSettings = fromJsonObject["user_settings"] as Map<*, *>

                            // 1. 還原使用者設定 (UserSettings)，排除不進行還原的本地豁免 Key (參考 UserSettings.IGNORED_KEYS)
                            userSettings.forEach { (keyObject, value) ->
                                val key = keyObject.toString()
                                if (value != null && UserSettings.IGNORED_KEYS.none { it.equals(key, ignoreCase = true) }) {
                                    when (value) {
                                        is String ->
                                            UserSettings.myEditor?.putString(key, value)

                                        is Float ->
                                            UserSettings.myEditor?.putFloat(key, value)

                                        is Double -> {
                                            /** 取得本地端對應 Key 的原本資料型態，避免將 Float 設定誤寫入為 Int */
                                            val existing = UserSettings.mySharedPref?.all?.get(key)
                                            if (existing is Float) {
                                                UserSettings.myEditor?.putFloat(key, value.toFloat())
                                            } else {
                                                val insertValue: Int = value.toInt()
                                                UserSettings.myEditor?.putInt(key, insertValue)
                                            }
                                        }

                                        is Int ->
                                            UserSettings.myEditor?.putInt(key, value)

                                        is Boolean ->
                                            UserSettings.myEditor?.putBoolean(key, value)

                                    }
                                }
                            }
                            UserSettings.myEditor?.apply()

                            // 2. 還原書籤資料 (BookmarkStore)
                            val bookmark = JSONObject((fromJsonObject["bookmark"] as String))
                            TempSettings.bookmarkStore?.importFromJSON(bookmark)

                            // 3. 若雲端還原改變了深色模式設定，同步套用到 AppCompatDelegate
                            ASCoroutine.ensureMainThread {
                                val targetNightMode = if (UserSettings.propertiesFollowSystemDarkMode) {
                                    AppCompatDelegate.MODE_NIGHT_FOLLOW_SYSTEM
                                } else {
                                    AppCompatDelegate.MODE_NIGHT_NO
                                }
                                if (AppCompatDelegate.getDefaultNightMode() != targetNightMode) {
                                    AppCompatDelegate.setDefaultNightMode(targetNightMode)
                                }
                            }

                            isSuccess = true
                        }
                    }
                } catch (e: Exception) {
                    Log.d(javaClass.simpleName, e.toString())
                    errorMsg = e.message
                } finally {
                    callback?.invoke(isSuccess, errorMsg)
                    final()
                }
            }

        } catch (e: java.lang.Exception) {
            Log.d(javaClass.simpleName, e.toString())
            callback?.invoke(false, e.message)
            final()
        }
    }

    /** 雲端備份事件監聽器實例 */
    private var myListener: CloudBackupListener? = null

    /**
     * 設定雲端備份事件監聽器
     *
     * @param listener 實作 CloudBackupListener 的監聽器實例
     * @return 傳回 `this` 實例以支援鏈式呼叫
     */
    fun setListener(listener: CloudBackupListener): CloudBackup {
        myListener = listener
        return this
    }

    /**
     * 觸發雲端備份流程結束通知
     */
    fun final() {
        myListener?.onFinal()
    }
}
