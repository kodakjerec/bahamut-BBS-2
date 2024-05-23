package com.kota.Bahamut.Service

import android.util.Log
import com.google.gson.GsonBuilder
import com.google.gson.JsonPrimitive
import com.google.gson.JsonSerializer
import com.kota.ASFramework.Thread.ASRunner
import com.kota.ASFramework.UI.ASToast
import com.kota.Bahamut.Service.TempSettings.getBookmarkStore
import okhttp3.MultipartBody
import okhttp3.OkHttpClient
import okhttp3.Request
import okhttp3.RequestBody
import org.json.JSONObject

object CloudBackup {
    fun checkCloud(): Boolean {
        // encrypt
        val userId = AESCrypt.encrypt(UserSettings.getPropertiesUsername())
        val apiUrl = "https://cloud-backup-lqeallcr2q-de.a.run.app"
        val client = OkHttpClient()
        val body: RequestBody = MultipartBody.Builder().setType(MultipartBody.FORM)
            .addFormDataPart("userId", userId)
            .build()
        val request: Request = Request.Builder()
            .url(apiUrl)
            .post(body)
            .build()

        client.newCall(request).execute().use { response ->
            val data = response.body!!.string()
            val jsonObject = JSONObject(data)
            val status = jsonObject.optString("error")
            if (status.isNotEmpty()) {
                ASToast.showShortToast("雲端備份失敗：$status")
            } else {
                return jsonObject.getBoolean("isCloudExist")
            }
            return false
        }
    }

    // 備份所有設定
    fun backup() {
        try {
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
            jsonObject.put("bookmark", getBookmarkStore()!!.exportToJSON().toString())
            // get user_settings
            jsonObject.put("user_settings", UserSettings._sharedPref.all)
            // encrypt
            val userId = AESCrypt.encrypt(UserSettings.getPropertiesUsername())
            val jsonDataString = AESCrypt.encrypt(gson.toJson(jsonObject))
            // send data
            val jsonData = JSONObject()
            jsonData.put("jsonData", jsonDataString)
            val apiUrl = "https://cloud-backup-lqeallcr2q-de.a.run.app"
            val client = OkHttpClient()
            val body: RequestBody = MultipartBody.Builder().setType(MultipartBody.FORM)
                .addFormDataPart("userId", userId)
                .addFormDataPart("jsonData", jsonData.toString())
                .build()
            val request: Request = Request.Builder()
                .url(apiUrl)
                .post(body)
                .build()
            ASRunner.runInNewThread {
                    client.newCall(request).execute().use { response->
                        val data = response.body!!.string()
                        val fromJsonObject = JSONObject(data)
                        val status = fromJsonObject.optString("error")
                        if (status.isNotEmpty()) {
                            ASToast.showShortToast("雲端備份失敗：$status")
                        }
                    }
            }

        } catch (e: Exception) {
            Log.d("SharedPrefBackup", e.toString())
        }
    }

    // 還原設定
    fun restore() {
        try {
            // encrypt
            val userId = AESCrypt.encrypt(UserSettings.getPropertiesUsername())
            val apiUrl = "https://cloud-restore-lqeallcr2q-de.a.run.app"
            val client = OkHttpClient()
            val body: RequestBody = MultipartBody.Builder().setType(MultipartBody.FORM)
                .addFormDataPart("userId", userId)
                .build()
            val request: Request = Request.Builder()
                .url(apiUrl)
                .post(body)
                .build()
            ASRunner.runInNewThread {
                client.newCall(request).execute().use { response->
                    val data = response.body!!.string()
                    val jsonObject = JSONObject(data)
                    val status = jsonObject.optString("error")
                    if (status.isNotEmpty()) {
                        ASToast.showShortToast("雲端備份失敗：$status")
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

                        val jsonDataString = jsonObject.getString("jsonData")
                        val fromJsonObject = gson.fromJson(AESCrypt.decrypt(jsonDataString), JSONObject::class.java)
                        val userSettings = fromJsonObject["user_settings"] as Map<*,*>
                        // set user_settings
                        userSettings.forEach { (keyObject, value) ->
                            val key = keyObject.toString()
                            if (value != null) {
                                when (value.javaClass) {
                                    String::class.java -> {
                                        UserSettings._editor.putString(key, value as String)
                                    }
                                    Float::class.java -> {
                                        UserSettings._editor.putFloat(key, (value as Float))
                                    }
                                    Int::class.java -> {
                                        UserSettings._editor.putInt(key, (value as Int))
                                    }
                                    Boolean::class.java -> {
                                        UserSettings._editor.putBoolean(key, (value as Boolean))
                                    }
                                }
                            }
                        }
                        UserSettings._editor.commit()

                        // set bookmark
                        val bookmark = JSONObject((fromJsonObject["bookmark"] as String))
                        getBookmarkStore().importFromJSON(bookmark)
                    }
                }
            }

        } catch (e: java.lang.Exception) {
            Log.d("SharedPrefBackup", e.toString())
        }
    }
}