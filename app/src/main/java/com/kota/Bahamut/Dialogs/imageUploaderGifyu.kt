package com.kota.Bahamut.Dialogs

import kotlinx.coroutines.runBlocking
import okhttp3.*
import okhttp3.MediaType.Companion.toMediaTypeOrNull
import okhttp3.RequestBody.Companion.asRequestBody
import org.json.JSONObject
import java.io.File

class GifyuUploader {
    private val client = OkHttpClient()
    private val baseURL = "https://gifyu.com"

    data class Credential(val authToken: String, val cookie: String)

    // 取得認證資訊
    fun getCredential(): Credential {
        val request = Request.Builder()
            .url("$baseURL/")
            .get()
            .build()
        client.newCall(request).execute().use { response ->
            val body = response.body?.string() ?: throw Exception("No body")
            val authTokenRegex = Regex("""PF\.obj\.config\.auth_token\s*=\s*"([^"]+)"""")
            val authToken = authTokenRegex.find(body)?.groupValues?.get(1)
                ?: throw Exception("No auth_token found")
            val cookie = response.headers("Set-Cookie").firstOrNull()?.split(";")?.get(0)
                ?: throw Exception("No cookie found")
            return Credential(authToken, cookie)
        }
    }

    // 上傳單張圖片 (本地檔案或網址)
    fun postImage(
        credential: Credential,
        source: String,
        filename: String? = null,
        description: String? = null
    ): JSONObject {
        val isUrl = source.startsWith("http://") || source.startsWith("https://")
        val formBuilder = MultipartBody.Builder().setType(MultipartBody.FORM)
            .addFormDataPart("nsfw", "0")
            .addFormDataPart("action", "upload")
            .addFormDataPart("expiration", "0")
            .addFormDataPart("timestamp", System.currentTimeMillis().toString())
            .addFormDataPart("type", if (isUrl) "url" else "file")
            .addFormDataPart("auth_token", credential.authToken)

        filename?.let { formBuilder.addFormDataPart("title", it) }
        description?.let { formBuilder.addFormDataPart("description", it) }
        if (isUrl) {
            formBuilder.addFormDataPart("source", source)
        } else {
            val file = File(source)
            require(file.exists()) { "File not found: $source" }
            formBuilder.addFormDataPart(
                "source", file.name,
                file.asRequestBody("image/*".toMediaTypeOrNull())
            )
        }

        val request = Request.Builder()
            .url("$baseURL/json")
            .post(formBuilder.build())
            .addHeader("Cookie", credential.cookie)
            .addHeader("Connection", "keep-alive")
            .addHeader("Accept", "application/json")
            .addHeader("Origin", "https://gifyu.com")
            .addHeader("Referer", "https://gifyu.com/")
            .addHeader("sec-fetch-mode", "cors")
            .addHeader("X-Requested-With", "XMLHttpRequest")
            .addHeader("Accept-Encoding", "gzip, deflate, br")
            .addHeader("Accept-Language", "en-US,en;q=0.9")
            .addHeader(
                "User-Agent",
                "Mozilla/5.0 (Windows NT 6.1; Win64; x64) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/72.0.3626.119 Safari/537.36"
            )
            .build()

        client.newCall(request).execute().use { response ->
            val responseStr = response.body?.string() ?: throw Exception("No response body")
            return JSONObject(responseStr)
        }
    }
}

// --- 使用範例 ---
fun main() = runBlocking {
    val gifyu = GifyuUploader()
    val credential = gifyu.getCredential()
    // 本地檔案上傳
    val result = gifyu.postImage(credential, "/path/to/file.jpg", filename = "測試圖片", description = "說明")
    println(result.toString(2))

    // 或者網址上傳
    // val result = gifyu.postImage(credential, "https://example.com/test.jpg")
    // println(result.toString(2))
}