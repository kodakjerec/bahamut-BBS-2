package com.kota.Bahamut.dialogs.uploadImgMethod

import android.content.Context
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.media.MediaCodec
import android.media.MediaCodecInfo
import android.media.MediaExtractor
import android.media.MediaFormat
import android.media.MediaMuxer
import android.net.Uri
import android.provider.OpenableColumns
import android.view.Surface
import com.google.gson.Gson
import okhttp3.*
import java.io.IOException
import java.io.ByteArrayOutputStream
import java.io.File
import okhttp3.MediaType.Companion.toMediaTypeOrNull
import okhttp3.RequestBody.Companion.toRequestBody

data class bahaImgResponse(
    val url: String
)

class UploaderBahaImg {

    private val client = OkHttpClient()
    private val gson = Gson()

    private val apiUrl = "https://img.kodakjerec.workers.dev/upload"
    
    // 文件大小限制 (單位: bytes)
    private val fileSizeLimits = mapOf(
        "image" to 10 * 1024 * 1024,  // 圖片: 10MB
        "video" to 20 * 1024 * 1024   // 影片: 20MB
    )
    
    // 支援的圖片和影片格式
    private val imageExtensions = setOf("jpg", "jpeg", "png", "gif", "webp")
    private val videoExtensions = setOf("mp4", "webm", "mov", "avi", "mkv", "flv", "wmv")

    /**
     * 判斷媒體類型 (圖片或影片)
     * @return "image" 或 "video" 或 null (不支援的格式)
     */
    private fun getMediaType(fileName: String): String? {
        val ext = fileName.substringAfterLast('.').lowercase()
        return when {
            imageExtensions.contains(ext) -> "image"
            videoExtensions.contains(ext) -> "video"
            else -> null
        }
    }

    /**
     * 獲取檔案大小限制
     */
    private fun getFileSizeLimit(mediaType: String): Int = fileSizeLimits[mediaType] ?: Int.MAX_VALUE

    /**
     * 壓縮圖片
     * @param context Context
     * @param imageUri 圖片 Uri
     * @return 壓縮後的位元組陣列
     */
    private fun compressImage(context: Context, imageUri: Uri): ByteArray? {
        return try {
            val inputStream = context.contentResolver.openInputStream(imageUri)
            val bitmap = BitmapFactory.decodeStream(inputStream)
            inputStream?.close()

            if (bitmap == null) return null

            // 計算縮放比例以符合 2K 解析度 (2560×1440)
            val maxWidth = 2560
            val maxHeight = 1440
            var width = bitmap.width
            var height = bitmap.height

            val scaleWidth = width.toFloat() / maxWidth
            val scaleHeight = height.toFloat() / maxHeight
            val scale = maxOf(scaleWidth, scaleHeight)

            // 如果圖片超過 2K，進行縮放
            val scaledBitmap = if (scale > 1) {
                val newWidth = (width / scale).toInt()
                val newHeight = (height / scale).toInt()
                Bitmap.createScaledBitmap(bitmap, newWidth, newHeight, true)
            } else {
                bitmap
            }

            // 將 Bitmap 壓縮為 JPEG (品質 85)
            val outputStream = ByteArrayOutputStream()
            scaledBitmap.compress(Bitmap.CompressFormat.JPEG, 85, outputStream)
            val compressedBytes = outputStream.toByteArray()
            outputStream.close()

            bitmap.recycle()
            if (scaledBitmap != bitmap) scaledBitmap.recycle()

            compressedBytes
        } catch (e: Exception) {
            null
        }
    }

    /**
     * 使用 MediaCodec API 壓縮影片
     * 轉碼為 H.264 + AAC 格式，解析度最大 2K
     * @param context Context
     * @param videoUri 影片 Uri
     * @return 壓縮後的位元組陣列
     */
    private fun compressVideo(context: Context, videoUri: Uri): ByteArray? {
        return try {
            // 1. 讀取原始視頻檔案
            val inputStream = context.contentResolver.openInputStream(videoUri)
            val inputBytes = inputStream?.readBytes()
            inputStream?.close()

            if (inputBytes == null || inputBytes.isEmpty()) return null

            // 2. 建立臨時檔案
            val tempInputFile = File(context.cacheDir, "temp_input_video_${System.currentTimeMillis()}")
            val tempOutputFile = File(context.cacheDir, "temp_output_video_${System.currentTimeMillis()}.mp4")
            
            tempInputFile.writeBytes(inputBytes)

            // 3. 嘗試轉碼，轉碼失敗時回退到原始檔案
            return try {
                transcodeVideoWithMediaCodec(tempInputFile, tempOutputFile)
                
                // 4. 檢查輸出檔案是否有效
                if (tempOutputFile.exists() && tempOutputFile.length() > 0) {
                    val compressedBytes = tempOutputFile.readBytes()
                    
                    // 驗證轉碼後的大小是否有意義
                    if (compressedBytes.isNotEmpty()) {
                        compressedBytes
                    } else {
                        // 轉碼後無效，返回原始
                        if (inputBytes.size <= fileSizeLimits["video"]!!) inputBytes else null
                    }
                } else {
                    // 轉碼失敗，返回原始檔案（較小的情況下）
                    if (inputBytes.size <= fileSizeLimits["video"]!!) inputBytes else null
                }
            } catch (e: Exception) {
                e.printStackTrace()
                // 轉碼發生異常，返回原始檔案
                if (inputBytes.size <= fileSizeLimits["video"]!!) inputBytes else null
            } finally {
                // 5. 清理臨時檔案
                tempInputFile.delete()
                tempOutputFile.delete()
            }
        } catch (e: Exception) {
            e.printStackTrace()
            null
        }
    }

    /**
     * 使用 MediaCodec 進行視頻轉碼（解碼→編碼流水線）
     * @param inputFile 輸入視頻檔案
     * @param outputFile 輸出視頻檔案
     */
    private fun transcodeVideoWithMediaCodec(inputFile: File, outputFile: File) {
        val extractor = MediaExtractor()
        var muxer: MediaMuxer? = null
        var decoder: MediaCodec? = null
        var encoder: MediaCodec? = null
        var encoderInputSurface: Surface? = null
        
        try {
            extractor.setDataSource(inputFile.absolutePath)

            var videoTrackIndex = -1
            var audioTrackIndex = -1
            var videoFormat: MediaFormat? = null
            var audioFormat: MediaFormat? = null

            // 1. 找到視頻和音頻軌道
            for (i in 0 until extractor.trackCount) {
                val format = extractor.getTrackFormat(i)
                val mime = format.getString(MediaFormat.KEY_MIME) ?: ""

                when {
                    mime.startsWith("video/") && videoTrackIndex < 0 -> {
                        videoTrackIndex = i
                        videoFormat = format
                    }
                    mime.startsWith("audio/") && audioTrackIndex < 0 -> {
                        audioTrackIndex = i
                        audioFormat = format
                    }
                }
            }

            if (videoFormat == null) {
                return
            }

            val inputMime = videoFormat.getString(MediaFormat.KEY_MIME) ?: "video/avc"
            val inputWidth = videoFormat.getInteger(MediaFormat.KEY_WIDTH)
            val inputHeight = videoFormat.getInteger(MediaFormat.KEY_HEIGHT)
            val outputWidth = getScaledWidth(videoFormat)
            val outputHeight = getScaledHeight(videoFormat)
            
            // 獲取旋轉角度（直立影片通常有 90 或 270 度旋轉）
            val rotation = if (videoFormat.containsKey(MediaFormat.KEY_ROTATION)) {
                videoFormat.getInteger(MediaFormat.KEY_ROTATION)
            } else {
                0
            }

            // 2. 建立 Muxer
            muxer = MediaMuxer(outputFile.absolutePath, MediaMuxer.OutputFormat.MUXER_OUTPUT_MPEG_4)
            
            // 設置輸出視頻的旋轉角度
            if (rotation != 0) {
                muxer.setOrientationHint(rotation)
            }
            
            // 3. 配置編碼器（先創建編碼器以獲取輸入 Surface）
            val encoderFormat = MediaFormat.createVideoFormat("video/avc", outputWidth, outputHeight).apply {
                setInteger(MediaFormat.KEY_COLOR_FORMAT, MediaCodecInfo.CodecCapabilities.COLOR_FormatSurface)
                setInteger(MediaFormat.KEY_BIT_RATE, 2_500_000)
                setInteger(MediaFormat.KEY_FRAME_RATE, 30)
                setInteger(MediaFormat.KEY_I_FRAME_INTERVAL, 1)
            }
            
            encoder = MediaCodec.createEncoderByType("video/avc")
            encoder.configure(encoderFormat, null, null, MediaCodec.CONFIGURE_FLAG_ENCODE)
            encoderInputSurface = encoder.createInputSurface()
            encoder.start()

            // 4. 配置解碼器（輸出到編碼器的 Surface）
            decoder = MediaCodec.createDecoderByType(inputMime)
            decoder.configure(videoFormat, encoderInputSurface, null, 0)
            decoder.start()

            // 5. 選擇視頻軌道
            extractor.selectTrack(videoTrackIndex)

            // 6. 轉碼視頻
            var videoMuxerTrackIndex = -1
            var muxerStarted = false
            var inputDone = false
            var decoderDone = false
            var encoderDone = false
            
            val decoderBufferInfo = MediaCodec.BufferInfo()
            val encoderBufferInfo = MediaCodec.BufferInfo()

            while (!encoderDone) {
                // 送入解碼器
                if (!inputDone) {
                    val inputIndex = decoder.dequeueInputBuffer(10000)
                    if (inputIndex >= 0) {
                        val inputBuffer = decoder.getInputBuffer(inputIndex)!!
                        val sampleSize = extractor.readSampleData(inputBuffer, 0)
                        
                        if (sampleSize < 0) {
                            decoder.queueInputBuffer(inputIndex, 0, 0, 0, MediaCodec.BUFFER_FLAG_END_OF_STREAM)
                            inputDone = true
                        } else {
                            decoder.queueInputBuffer(inputIndex, 0, sampleSize, extractor.sampleTime, 0)
                            extractor.advance()
                        }
                    }
                }

                // 從解碼器獲取輸出並渲染到 Surface
                if (!decoderDone) {
                    val outputIndex = decoder.dequeueOutputBuffer(decoderBufferInfo, 10000)
                    if (outputIndex >= 0) {
                        val render = decoderBufferInfo.size > 0
                        decoder.releaseOutputBuffer(outputIndex, render) // render = true 會渲染到 Surface
                        
                        if (decoderBufferInfo.flags and MediaCodec.BUFFER_FLAG_END_OF_STREAM != 0) {
                            encoder.signalEndOfInputStream()
                            decoderDone = true
                        }
                    }
                }

                // 從編碼器獲取輸出
                val encoderOutputIndex = encoder.dequeueOutputBuffer(encoderBufferInfo, 10000)
                when {
                    encoderOutputIndex == MediaCodec.INFO_OUTPUT_FORMAT_CHANGED -> {
                        videoMuxerTrackIndex = muxer.addTrack(encoder.outputFormat)
                        muxer.start()
                        muxerStarted = true
                    }
                    encoderOutputIndex >= 0 -> {
                        val encodedData = encoder.getOutputBuffer(encoderOutputIndex)!!
                        
                        if (encoderBufferInfo.size > 0 && muxerStarted) {
                            encodedData.position(encoderBufferInfo.offset)
                            encodedData.limit(encoderBufferInfo.offset + encoderBufferInfo.size)
                            muxer.writeSampleData(videoMuxerTrackIndex, encodedData, encoderBufferInfo)
                        }
                        
                        encoder.releaseOutputBuffer(encoderOutputIndex, false)
                        
                        if (encoderBufferInfo.flags and MediaCodec.BUFFER_FLAG_END_OF_STREAM != 0) {
                            encoderDone = true
                        }
                    }
                }
            }

            // 7. 複製音頻軌道
            if (audioTrackIndex >= 0 && audioFormat != null) {
                // 需要重新創建 extractor 來讀取音頻
                val audioExtractor = MediaExtractor()
                audioExtractor.setDataSource(inputFile.absolutePath)
                audioExtractor.selectTrack(audioTrackIndex)
                
                val audioMuxerTrackIndex = muxer.addTrack(audioFormat)
                
                copyAudioTrack(audioExtractor, muxer, audioMuxerTrackIndex)
                audioExtractor.release()
            }

            // 8. 停止 Muxer
            if (muxerStarted) {
                muxer.stop()
            }
        } catch (e: Exception) {
            e.printStackTrace()
        } finally {
            decoder?.stop()
            decoder?.release()
            encoder?.stop()
            encoder?.release()
            encoderInputSurface?.release()
            extractor.release()
            muxer?.release()
        }
    }

    /**
     * 複製音頻軌道（無需轉碼）
     */
    private fun copyAudioTrack(
        extractor: MediaExtractor,
        muxer: MediaMuxer,
        audioMuxerTrackIndex: Int
    ) {
        try {
            val bufferInfo = MediaCodec.BufferInfo()
            val buffer = java.nio.ByteBuffer.allocate(256 * 1024)

            while (true) {
                val sampleSize = extractor.readSampleData(buffer, 0)
                if (sampleSize < 0) break

                bufferInfo.apply {
                    offset = 0
                    size = sampleSize
                    presentationTimeUs = extractor.sampleTime
                    flags = 0
                }

                muxer.writeSampleData(audioMuxerTrackIndex, buffer, bufferInfo)
                extractor.advance()
            }
        } catch (e: Exception) {
            e.printStackTrace()
        }
    }

    /**
     * 計算縮放後的寬度 (限制 2K)
     */
    private fun getScaledWidth(format: MediaFormat): Int {
        val width = format.getInteger(MediaFormat.KEY_WIDTH)
        val height = format.getInteger(MediaFormat.KEY_HEIGHT)
        val maxWidth = 2560

        return if (width > maxWidth) {
            (maxWidth * height / width).coerceAtLeast(2)
        } else {
            width
        }
    }

    /**
     * 計算縮放後的高度 (限制 2K)
     */
    private fun getScaledHeight(format: MediaFormat): Int {
        val width = format.getInteger(MediaFormat.KEY_WIDTH)
        val height = format.getInteger(MediaFormat.KEY_HEIGHT)
        val maxHeight = 1440

        return if (height > maxHeight) {
            (maxHeight * width / height).coerceAtLeast(2)
        } else {
            height
        }
    }

    /**
     * 根據媒體類型壓縮檔案
     * @param context Context
     * @param uri 媒體 Uri
     * @param mediaType "image" 或 "video"
     * @return 壓縮後的位元組陣列
     */
    private fun compressMedia(context: Context, uri: Uri, mediaType: String): ByteArray? {
        return when (mediaType) {
            "image" -> compressImage(context, uri)
            "video" -> compressVideo(context, uri)
            else -> null
        }
    }

    /**
     * 檢查和獲取媒體位元組，先驗證原始大小再決定是否壓縮
     * @param context Context
     * @param uri 媒體 Uri
     * @param mediaType "image" 或 "video"
     * @return Pair<位元組陣列, 是否已壓縮>
     */
    private fun getMediaBytesWithCompressionCheck(
        context: Context, 
        uri: Uri, 
        mediaType: String
    ): Pair<ByteArray?, Boolean> {
        return try {
            // 1. 先讀取原始位元組
            val inputStream = context.contentResolver.openInputStream(uri)
            val originalBytes = inputStream?.readBytes()
            inputStream?.close()

            if (originalBytes == null || originalBytes.isEmpty()) {
                return Pair(null, false)
            }

            // 2. 檢查原始大小是否超過限制
            val sizeLimit = getFileSizeLimit(mediaType)
            if (originalBytes.size <= sizeLimit) {
                // 原始檔案已符合限制，無需壓縮
                return Pair(originalBytes, false)
            }

            // 3. 原始檔案超過限制，進行壓縮
            val compressedBytes = compressMedia(context, uri, mediaType)
            
            if (compressedBytes == null) {
                // 壓縮失敗
                return Pair(null, false)
            }

            // 4. 檢查壓縮後的大小
            if (compressedBytes.size <= sizeLimit) {
                // 壓縮成功且符合限制
                return Pair(compressedBytes, true)
            }

            // 5. 壓縮後仍超過限制
            return Pair(null, false)
        } catch (e: Exception) {
            Pair(null, false)
        }
    }

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

            // 2. 判斷媒體類型
            val mediaType = getMediaType(fileName)
            if (mediaType == null) {
                callback.onError("不支援的檔案格式: $fileName")
                return
            }

            // 3. 先檢查原始大小，決定是否需要壓縮
            val (mediaBytes, wasCompressed) = getMediaBytesWithCompressionCheck(context, imageUri, mediaType)
            if (mediaBytes == null) {
                val sizeLimit = getFileSizeLimit(mediaType)
                val limitMB = sizeLimit / (1024 * 1024)
                callback.onError("檔案超過限制 (${mediaType} 限制: ${limitMB}MB)，壓縮後仍未能符合要求")
                return
            }

            // 4. 建立 Multipart 表單內容
            val mimeType = if (mediaType == "image") "image/*" else "video/*"
            val requestBody = MultipartBody.Builder()
                .setType(MultipartBody.FORM)
                .addFormDataPart(
                    "image",
                    fileName,
                    mediaBytes.toRequestBody(mimeType.toMediaTypeOrNull())
                )
                .build()

            val request = Request.Builder()
                .url(apiUrl)
                .post(requestBody)
                .build()

            // 6. 執行非同步請求
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