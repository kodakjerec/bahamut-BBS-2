package com.kota.Bahamut.dialogs.uploadImgMethod

import android.annotation.SuppressLint
import android.net.Uri
import com.kota.Bahamut.service.TempSettings
import okhttp3.*
import okhttp3.MediaType.Companion.toMediaTypeOrNull
import okhttp3.RequestBody.Companion.toRequestBody
import java.io.InputStream

class UploaderLitterCatBox {
    private val client = OkHttpClient()
    private val baseURL = "https://litterbox.catbox.moe"

    // 上傳單張圖片 (本地檔案或網址)
    @SuppressLint("Recycle")
    fun postImage( source: Uri ): String {
        val formBuilder = MultipartBody.Builder().setType(MultipartBody.FORM)
            .addFormDataPart("reqtype", "fileupload")
            .addFormDataPart("time", "72h")

        val inputStream : InputStream = TempSettings.myContext?.contentResolver?.openInputStream(source)
            ?: error("找不到檔案")
        val byteArray = inputStream.readBytes()
        val binaryBody = byteArray.toRequestBody("image/*".toMediaTypeOrNull())
        formBuilder.addFormDataPart(
            "fileToUpload", "temp.png",
            binaryBody
        )

        val request = Request.Builder()
            .url("$baseURL/resources/internals/api.php")
            .post(formBuilder.build())
            .addHeader("Accept", "application/json")
            .build()

        client.newCall(request).execute().use { response ->
            return response.body?.string() ?: throw Exception("No response body")
        }
    }
}