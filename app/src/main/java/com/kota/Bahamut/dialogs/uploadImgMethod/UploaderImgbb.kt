package com.kota.Bahamut.dialogs.uploadImgMethod

import android.content.Context
import android.net.Uri
import com.google.gson.Gson
import okhttp3.*
import java.io.IOException
import com.google.gson.annotations.SerializedName
import okhttp3.MediaType.Companion.toMediaTypeOrNull
import okhttp3.RequestBody.Companion.toRequestBody

// ImgBB API 響應外層
data class ImgbbResponse(
    val data: ImgbbData?,
    val success: Boolean,
    val status: Int
)

// 主要資料內容
data class ImgbbData(
    val id: String,
    val title: String,
    @SerializedName("url_viewer") val urlViewer: String,
    val url: String, // 這是圖片上傳後的直接連結 (Direct Link)
    @SerializedName("display_url") val displayUrl: String,
    val size: String,
    val time: String,
    val expiration: String,
    val image: ImgbbDetails,
    val thumb: ImgbbDetails,
    @SerializedName("delete_url") val deleteUrl: String
)

// 圖片詳細細節
data class ImgbbDetails(
    val filename: String,
    val name: String,
    val mime: String,
    val extension: String,
    val url: String
)

class UploaderImgbb {

    private val client = OkHttpClient()
    private val gson = Gson()

    // 您的 ImgBB API KEY
    private val apiKey = "b4df258e90214da1939268e7357180ab"
    private val apiUrl = "https://api.imgbb.com/1/upload"

    /**
     * 上傳圖片 (處理 Android Uri)
     * @param context 用於獲取 contentResolver
     * @param imageUri 圖片的 Uri (來自相簿或相機)
     * @param callback 回傳結果的介面
     */
    fun uploadImage(context: Context, imageUri: Uri, callback: UploadCallback) {
        try {
            // 1. 從 Uri 讀取圖片位元組
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
                .addFormDataPart("key", apiKey)
                // 將圖片位元組包裝成 RequestBody 上傳，檔名預設為 upload.jpg
                .addFormDataPart(
                    "image",
                    "upload.jpg",
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
                            val result = gson.fromJson(responseBody, ImgbbResponse::class.java)
                            if (result.success && result.data != null) {
                                // 成功：回傳 ImgBB 託管後的直接圖片網址
                                callback.onSuccess(result.data.url)
                            } else {
                                callback.onError("ImgBB API 錯誤: ${result.status}")
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