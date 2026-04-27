package com.kota.Bahamut.dialogs.uploadImgMethod

import android.content.Context
import android.net.Uri
import android.provider.OpenableColumns
import com.google.gson.Gson
import okhttp3.*
import java.io.IOException
import okhttp3.MediaType.Companion.toMediaTypeOrNull
import okhttp3.RequestBody.Companion.toRequestBody

data class bahaImgResponse(
    val url: String
)

class UploaderBahaImg {

    private val client = OkHttpClient()
    private val gson = Gson()

    private val apiUrl = "https://img.kodakjerec.workers.dev/upload"

    /**
     * 取得 Uri 的真實檔案名稱
     */
    private fun getFileName(context: Context, uri: Uri): String {
        var result: String? = null
        if (uri.scheme == "content") {
            val cursor = context.contentResolver.query(uri, null, null, null, null)
            try {
                if (cursor != null && cursor.moveToFirst()) {
                    val index = cursor.getColumnIndex(OpenableColumns.DISPLAY_NAME)
                    if (index != -1) {
                        result = cursor.getString(index)
                    }
                }
            } finally {
                cursor?.close()
            }
        }
        if (result == null) {
            result = uri.path
            val cut = result?.lastIndexOf('/') ?: -1
            if (cut != -1) {
                result = result?.substring(cut + 1)
            }
        }
        return result ?: "upload.jpg" // 最終保險回傳值
    }

    /**
     * 上傳圖片 (處理 Android Uri)
     * @param context 用於獲取 contentResolver
     * @param imageUri 圖片的 Uri (來自相簿或相機)
     * @param callback 回傳結果的介面
     */
    fun uploadImage(context: Context, imageUri: Uri, callback: UploadCallback) {
        try {
            // 1. 取得真實檔案名稱
            val fileName = getFileName(context, imageUri)

            // 2. 從 Uri 讀取圖片位元組
            val inputStream = context.contentResolver.openInputStream(imageUri)
            val bytes = inputStream?.readBytes()
            inputStream?.close()

            if (bytes == null) {
                callback.onError("無法讀取圖片內容")
                return
            }

            // 2. 建立 Multipart 表單內容
            val requestBody = MultipartBody.Builder()
                .setType(MultipartBody.FORM)
                // 將圖片位元組包裝成 RequestBody 上傳，檔名預設為 upload.jpg
                .addFormDataPart(
                    "image",
                    fileName,
                    bytes.toRequestBody("image/*".toMediaTypeOrNull())
                )
                .build()

            val request = Request.Builder()
                .url(apiUrl)
                .post(requestBody)
                .build()

            // 3. 執行非同步請求
            client.newCall(request).enqueue(object : Callback {
                override fun onFailure(call: Call, e: IOException) {
                    callback.onError(e.message ?: "網路連線錯誤")
                }

                override fun onResponse(call: Call, response: Response) {
                    val responseBody = response.body?.string()

                    if (response.isSuccessful && responseBody != null) {
                        try {
                            val result = gson.fromJson(responseBody, bahaImgResponse::class.java)
                            if (result.url.isNotEmpty()) {
                                // 成功：回傳 bahaImg 託管後的直接圖片網址
                                callback.onSuccess(result.url)
                            } else {
                                callback.onError("bahaImg API 錯誤")
                            }
                        } catch (e: Exception) {
                            callback.onError("解析 JSON 失敗: ${e.message}")
                        }
                    } else {
                        callback.onError("伺服器錯誤代碼: ${response.code}")
                    }
                }
            })
        } catch (e: Exception) {
            callback.onError("讀取檔案失敗: ${e.message}")
        }
    }

    // 定義 Callback 介面
    interface UploadCallback {
        fun onSuccess(imageUrl: String)
        fun onError(message: String)
    }
}