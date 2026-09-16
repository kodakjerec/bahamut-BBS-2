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
import java.util.Locale.getDefault

class CloudBackup {
    // 文字轉為 long
    fun parseTimeToLong(timeString: String?): Long {
        if (timeString.isNullOrEmpty()) return 0L
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
                val sdf = SimpleDateFormat(format, Locale.getDefault())
                val date = sdf.parse(timeString)
                if (date != null) return date.time
            } catch (_: Exception) {
            }
        }
        return 0L
    }

    // 詢問雲端
    fun askCloudSave(): CloudBackup {
        // debug
//        setShowCloudSave(false)

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
                    // 不用再次詢問
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

    // 檢查雲端
    private fun checkCloud() {
        // encrypt
        val userId = AESCrypt.encrypt(UserSettings.propertiesUsername)
        val apiUrl = "https://cloud-restore.kodakjerec.work/"
        val client = OkHttpClient()
        val body: RequestBody = MultipartBody.Builder().setType(MultipartBody.FORM)
            .addFormDataPart("userId", userId)
            .addFormDataPart("queryType", "check")
            .build()
        val request: Request = Request.Builder()
            .url(apiUrl)
            .post(body)
            .build()

        ASCoroutine.runInNewCoroutine {
            client.newCall(request).execute().use { response ->
                val data = response.body.string()
                val jsonObject = JSONObject(data)
                val status = jsonObject.optString("error")
                if (status.isNotEmpty()) {
                    ASToast.showShortToast("雲端備份失敗：$status")
                } else {
                    val lastTime = jsonObject.getString("lastTime")
                    if (lastTime.isEmpty()) {
                        ASToast.showShortToast(CommonFunctions.getContextString(R.string.cloud_save_result1))
                        // 沒有雲端, 本地=>雲端
                        backup()
                        final()
                    } else {
                        // 有雲端, 第二次詢問
                        askCloudSave2(lastTime)
                    }
                }
            }
        }
    }

    private fun askCloudSave2(lastTime: String) {
        // 雲端存在, 選擇本地或雲端
        ASCoroutine.ensureMainThread {
            ASAlertDialog.createDialog()
                .setTitle(CommonFunctions.getContextString(R.string.cloud_save))
                .setMessage("已有雲端備份：\n$lastTime\n採用 本地存檔\n或 雲端存檔？")
                .addButton(CommonFunctions.getContextString(R.string.cloud_save_local))
                .addButton(CommonFunctions.getContextString(R.string.cloud_save_cloud))
                .setDefaultButtonIndex(0)
                .setListener { _: ASAlertDialog?, index: Int ->
                    if (index == 0) {
                        // 選擇本地=>覆蓋雲端
                        backup()
                        ASToast.showShortToast(CommonFunctions.getContextString(R.string.cloud_save_result1))
                    } else {
                        // 選擇雲端=>覆蓋本地
                        restore()
                        ASToast.showShortToast(CommonFunctions.getContextString(R.string.cloud_save_result2))
                    }
                }.show()
        }
    }

    // 備份所有設定
    fun backup(callback: ((Boolean, String?) -> Unit)? = null) {
        try {
            val userId = AESCrypt.encrypt(UserSettings.propertiesUsername)
            if (userId.isEmpty()) {
                callback?.invoke(false, "User ID is empty")
                final()
                return
            }
            val jsonObject = JSONObject()
            val gson = GsonBuilder()
                .registerTypeAdapter(
                    Double::class.java,
                    JsonSerializer<Double> { src, _, _ ->
                        if (src == 0.0) JsonPrimitive(
                            "0.0"
                        ) else JsonPrimitive(src)
                    })
                .create()

            // get bookmark
            jsonObject.put("bookmark", TempSettings.bookmarkStore?.exportToJSON().toString())
            // get user_settings (排除不要同步到雲端的本地設定: 帳號密碼、記住登入、Web自動簽到與 Web帳號密碼)
            val notBackupKeys = listOf("username", "password", "savelogonuser", "websignin", "webusername", "webpassword")
            val filteredSettings = UserSettings.mySharedPref?.all?.filterKeys { key ->
                !notBackupKeys.contains(key.lowercase())
            }
            jsonObject.put("user_settings", filteredSettings)
            // encrypt
            val jsonDataString = AESCrypt.encrypt(gson.toJson(jsonObject))
            // send data
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
                var isSuccess = false
                var errorMsg: String? = null
                try {
                    client.newCall(request).execute().use { response ->
                        val data = response.body.string()
                        val fromJsonObject = JSONObject(data)
                        val error = fromJsonObject.optString("error")
                        if (error.isNotEmpty()) {
                            errorMsg = error
                            ASToast.showShortToast("雲端備份失敗：$error")
                        } else {
                            // 雲端備份的時間
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

    // 還原設定
    fun restore(callback: ((Boolean, String?) -> Unit)? = null) {
        try {
            // encrypt
            val userId = AESCrypt.encrypt(UserSettings.propertiesUsername)
            if (userId.isEmpty()) {
                callback?.invoke(false, "User ID is empty")
                final()
                return
            }
            val apiUrl = "https://cloud-restore.kodakjerec.work/"
            val client = OkHttpClient()
            val body: RequestBody = MultipartBody.Builder().setType(MultipartBody.FORM)
                .addFormDataPart("userId", userId)
                .addFormDataPart("queryType", "all")
                .build()
            val request: Request = Request.Builder()
                .url(apiUrl)
                .post(body)
                .build()
            ASCoroutine.runInNewCoroutine {
                var isSuccess = false
                var errorMsg: String? = null
                try {
                    client.newCall(request).execute().use { response ->
                        val data = response.body.string()
                        val jsonObject = JSONObject(data)
                        val error = jsonObject.optString("error")
                        if (error.isNotEmpty()) {
                            errorMsg = error
                            ASToast.showShortToast("雲端備份失敗：$error")
                        } else {
                            val gson = GsonBuilder()
                                .registerTypeAdapter(
                                    Double::class.java,
                                    JsonSerializer<Double> { src, _, _ ->
                                        if (src == 0.0) JsonPrimitive(
                                            "0.0"
                                        ) else JsonPrimitive(src)
                                    })
                                .create()

                            // 雲端備份的時間
                            val lastTimeLong = parseTimeToLong(jsonObject.optString("lastTime", ""))
                            TempSettings.cloudSaveLastTime = lastTimeLong
                            NotificationSettings.setCloudSaveLastTime(lastTimeLong)

                            val jsonDataString = jsonObject.getString("jsonData")
                            val fromJsonObject = gson.fromJson(
                                AESCrypt.decrypt(jsonDataString),
                                JSONObject::class.java
                            )
                            val userSettings = fromJsonObject["user_settings"] as Map<*, *>
                            // set user_settings
                            // 不要還原的key: 使用者帳密, 在登入前的設定, 以及 Web 帳密與自動簽到
                            val notRestoreKeys: List<String> =
                                listOf("username", "password", "savelogonuser", "websignin", "webusername", "webpassword")
                            userSettings.forEach { (keyObject, value) ->
                                val key = keyObject.toString()
                                if (value != null && !notRestoreKeys.contains(key.lowercase())) {
                                    when (value) {
                                        is String ->
                                            UserSettings.myEditor?.putString(key, value)

                                        is Float ->
                                            UserSettings.myEditor?.putFloat(key, value)

                                        is Double -> {
                                            val insertValue: Int = value.toInt()
                                            UserSettings.myEditor?.putInt(key, insertValue)
                                        }
                                        is Int ->
                                            UserSettings.myEditor?.putInt(key, value)

                                        is Boolean ->
                                            UserSettings.myEditor?.putBoolean(key, value)

                                    }
                                }
                            }
                            UserSettings.myEditor?.apply()

                            // set bookmark
                            val bookmark = JSONObject((fromJsonObject["bookmark"] as String))
                            TempSettings.bookmarkStore?.importFromJSON(bookmark)

                            // 若雲端還原改變了深色模式設定，同步套用到 AppCompatDelegate
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
    private var myListener: CloudBackupListener? = null
    fun setListener(listener: CloudBackupListener): CloudBackup {
        myListener = listener
        return this
    }

    fun final() {
        myListener?.onFinal()
    }
}