package com.kota.Bahamut.Dialogs

import android.Manifest.permission.CAMERA
import android.content.Context
import android.annotation.SuppressLint
import android.content.Intent
import android.content.pm.PackageManager
import android.content.res.Configuration
import android.graphics.ImageDecoder
import android.net.Uri
import android.os.Build
import android.os.Bundle
import android.os.Environment
import android.provider.MediaStore
import android.util.Base64
import android.util.Log
import android.view.View
import android.view.View.GONE
import android.view.View.INVISIBLE
import android.view.View.OnClickListener
import android.view.View.VISIBLE
import android.view.ViewGroup
import android.widget.Button
import android.widget.ImageView
import android.widget.LinearLayout
import android.widget.RelativeLayout
import android.widget.TextView
import android.widget.VideoView
import androidx.activity.result.PickVisualMediaRequest
import androidx.activity.result.contract.ActivityResultContracts
import androidx.activity.result.contract.ActivityResultContracts.PickVisualMedia
import androidx.activity.result.contract.ActivityResultContracts.StartActivityForResult
import androidx.appcompat.app.AppCompatActivity
import androidx.core.content.FileProvider
import com.bumptech.glide.Glide
import com.kota.ASFramework.Thread.ASRunner
import com.kota.ASFramework.UI.ASToast
import com.kota.Bahamut.PageContainer
import com.kota.Bahamut.R
import com.kota.Bahamut.Service.CommonFunctions.getContextString
import com.kota.Bahamut.Service.TempSettings
import com.kota.Bahamut.Service.UserSettings
import okhttp3.MultipartBody
import okhttp3.OkHttpClient
import okhttp3.Request
import okhttp3.RequestBody
import org.json.JSONObject
import java.io.BufferedReader
import java.io.ByteArrayOutputStream
import java.io.File
import java.io.IOException
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Objects

@Suppress("DEPRECATION")
class DialogShortenImage : AppCompatActivity(), OnClickListener {
    private lateinit var mainLayout: RelativeLayout
    private lateinit var textView: TextView
    private lateinit var imageView: ImageView
    private lateinit var videoView: VideoView
    private lateinit var middleToolbar: LinearLayout
    private lateinit var middleToolbar2: LinearLayout
    private lateinit var processingDialog: LinearLayout
    private var transferButton: Button? = null
    private var sendButton: Button? = null
    private var outputParam: String = ""
    private var sampleTextView: TextView? = null


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        init()
    }

    fun init() {
        val layoutId = R.layout.dialog_shorten_image
        requestWindowFeature(1)
        setContentView(layoutId)
        Objects.requireNonNull(window)!!.setBackgroundDrawable(null)
        mainLayout = findViewById(R.id.dialog_shorten_image_layout)
        mainLayout.setOnClickListener { _ -> closeProcessingDialog() }
        textView = mainLayout.findViewById(R.id.dialog_shorten_image_hint)
        imageView = mainLayout.findViewById(R.id.dialog_shorten_image_image)
        videoView = mainLayout.findViewById(R.id.dialog_shorten_image_video)
        middleToolbar = mainLayout.findViewById(R.id.dialog_shorten_image_middle_toolbar)
        middleToolbar2 = mainLayout.findViewById(R.id.dialog_shorten_image_middle_toolbar2)
        sampleTextView = mainLayout.findViewById(R.id.dialog_shorten_image_sample)
        processingDialog = mainLayout.findViewById(R.id.dialog_shorten_image_processing_dialog)

        mainLayout.findViewById<Button>(R.id.dialog_shorten_image_album).setOnClickListener(selectImageListener)
        mainLayout.findViewById<Button>(R.id.dialog_shorten_image_camera_shot).setOnClickListener(selectCameraListener)
        mainLayout.findViewById<Button>(R.id.dialog_shorten_image_camera_video).setOnClickListener(selectVideoListener)
        transferButton = mainLayout.findViewById(R.id.dialog_shorten_image_transfer)
        transferButton!!.setOnClickListener(transferListener)
        sendButton = mainLayout.findViewById(R.id.send)
        sendButton!!.setOnClickListener(this)
        sendButton!!.isEnabled = false
        mainLayout.findViewById<Button>(R.id.dialog_shorten_image_reset).setOnClickListener(resetListener)
        mainLayout.findViewById<Button>(R.id.cancel).setOnClickListener(this)

        // 預設高度
        changeDialogHeight(resources.configuration)
        setDialogWidth()
    }

    /** 選擇相簿 */
    private val selectImageListener = OnClickListener { _->
        pickMediaLauncher.launch(PickVisualMediaRequest(PickVisualMedia.ImageAndVideo))
    }
    /** 選擇相機 */
    private val selectCameraListener = OnClickListener { _->
        if (checkSelfPermission(CAMERA) == PackageManager.PERMISSION_GRANTED){
            openCameraIntent()
        } else {
            permissionLauncher.launch(CAMERA)
        }
    }
    private val selectVideoListener = OnClickListener { _->
        if (checkSelfPermission(CAMERA) == PackageManager.PERMISSION_GRANTED){
            openVideoIntent()
        } else {
            permissionVideoLauncher.launch(CAMERA)
        }
    }

    /** 按下送出或取消 */
    override fun onClick(view: View) {
        postUrl(outputParam)
        finish()
    }

    /** 轉檔 */
    @SuppressLint("SimpleDateFormat")
    private var transferListener = OnClickListener {
        var shortenTimes: Int = UserSettings.getPropertiesNoVipShortenTimes()
        if (!UserSettings.getPropertiesVIP() && shortenTimes>30) {
            ASToast.showLongToast(getContextString(R.string.vip_only_message))
            return@OnClickListener
        }

        showProcessingDialog()

        val encodedBase64: String
        val byteArrayOutputStream = ByteArrayOutputStream()
        var finalUri: Uri? = null
        if (selectedImageUri != null) {
            finalUri = selectedImageUri
        } else if (selectedVideoUri != null) {
            finalUri = selectedVideoUri
        }
        if (finalUri == null) {
            ASToast.showShortToast(getContextString(R.string.dialog_shorten_image_error02))
            closeProcessingDialog()
            return@OnClickListener
        }
        val inputStream = contentResolver.openInputStream(finalUri)
        BufferedReader(inputStream!!.reader())
        val buffer = ByteArray(1024)
        var len: Int
        while ((inputStream.read(buffer).also { len = it }) !=-1) {
            byteArrayOutputStream.write(buffer, 0, len)
        }
        val byteArray = byteArrayOutputStream.toByteArray()
        encodedBase64 = Base64.encodeToString(byteArray, Base64.DEFAULT)

        // error handle
        if (encodedBase64.isEmpty()) {
            ASToast.showShortToast(getContextString(R.string.dialog_shorten_image_error02))
            closeProcessingDialog()
            return@OnClickListener
        }
        val accessToken = TempSettings.getImgurToken()
        val albumHash = TempSettings.getImgurAlbum()
        if (accessToken.isEmpty()) {
            ASToast.showShortToast(getContextString(R.string.dialog_shorten_image_error01))
            closeProcessingDialog()
            return@OnClickListener
        }

        val apiUrl = "https://api.imgur.com/3/image"
        val client = OkHttpClient()
        val body: RequestBody = MultipartBody.Builder().setType(MultipartBody.FORM)
            .addFormDataPart("image", encodedBase64)
            .addFormDataPart("type", "base64")
            .addFormDataPart("title", "bahamutBBS Upload " + SimpleDateFormat("yyyy-MM-dd").format(Date()))
            .addFormDataPart("description", UserSettings.getPropertiesUsername())
            .build()
        val request: Request = Request.Builder()
            .url(apiUrl)
            .addHeader("Authorization", "Bearer $accessToken")
            .post(body)
            .build()
        ASRunner.runInNewThread {
            try {
                client.newCall(request).execute().use { response ->
                    val data = response.body!!.string()
                    val jsonObject = JSONObject(data)
                    val status = jsonObject.getInt("status")
                    if (status == 200) {
                        val link = jsonObject.getJSONObject("data").getString("link")
                        val imageHash = jsonObject.getJSONObject("data").getString("id")
                        object : ASRunner() {
                            override fun run() {
                                sampleTextView!!.text = link
                                outputParam = sampleTextView!!.text.toString()
                                sendButton!!.isEnabled = true
                                transferButton!!.isEnabled = false
                                UserSettings.setPropertiesNoVipShortenTimes(++shortenTimes)
                            }
                        }.runInMainThread()

                        // 把圖片加進相簿
                        val apiUrlAlbum = "https://api.imgur.com/3/album/$albumHash/add"
                        val bodyAlbum: RequestBody = MultipartBody.Builder().setType(MultipartBody.FORM)
                            .addFormDataPart("ids[]", imageHash)
                            .build()
                        val requestAlbum: Request = Request.Builder()
                            .url(apiUrlAlbum)
                            .addHeader("Authorization", "Bearer $accessToken")
                            .post(bodyAlbum)
                            .build()
                        ASRunner.runInNewThread {
                            try {
                                client.newCall(requestAlbum).execute().use { }
                            } catch (e: Exception) {
                                Log.e("ShortenImage", e.printStackTrace().toString())
                            }
                        }
                    } else {
                        val error = jsonObject.getJSONObject("data").getString("error")
                        ASToast.showShortToast(getContextString(R.string.dialog_shorten_image_error03)+ " " + error)
                    }
                }
            } catch (e: Exception) {
                ASToast.showShortToast(getContextString(R.string.dialog_shorten_image_error03)+ " " + e.message)
                Log.e("ShortenImage", e.printStackTrace().toString())
            } finally {
                closeProcessingDialog()
            }
        }
    }

    /** 清除內容 */
    private val resetListener = OnClickListener {_ ->
        middleToolbar.visibility = VISIBLE
        middleToolbar2.visibility = GONE
        textView.visibility = VISIBLE
        imageView.setImageBitmap(null)
        imageView.visibility = GONE
        videoView.stopPlayback()
        videoView.clearAnimation()
        videoView.suspend()
        videoView.setVideoURI(null)
        videoView.visibility = GONE
        sampleTextView!!.text = getContextString(R.string.dialog_paint_color_sample)
        outputParam = ""
        selectedImageUri = null
        selectedVideoUri = null
        sendButton!!.isEnabled = false
        transferButton!!.isEnabled = true
    }
    /** 註冊 intent */
    private val intentCameraLauncher = registerForActivityResult(StartActivityForResult()) { result ->
        if (result.resultCode == RESULT_OK) {
            try {
                middleToolbar.visibility = GONE
                middleToolbar2.visibility = VISIBLE
                textView.visibility = GONE
                imageView.visibility = VISIBLE
                videoView.visibility = GONE
                if (Build.VERSION.SDK_INT>=29) {
                    val source = ImageDecoder.createSource(contentResolver, selectedImageUri!!)
                    val bitmap = ImageDecoder.decodeBitmap(source) { decoder, _, _->
                        decoder.setTargetSampleSize(1)
                        decoder.isMutableRequired = true
                    }
                    Glide.with(this).load(bitmap).into(imageView)
                } else {
                    val bitmap = MediaStore.Images.Media.getBitmap(contentResolver, selectedImageUri)
                    Glide.with(this).load(bitmap).into(imageView)
                }
                transferButton!!.performClick()
            } catch (e:Exception) {
                Log.d("DialogShortenImage", e.printStackTrace().toString())
                ASToast.showShortToast(getContextString(R.string.dialog_shorten_image_error02))
            }
        }
    }
    private val intentVideoLauncher = registerForActivityResult(StartActivityForResult()) { result ->
        if (result.resultCode == RESULT_OK) {
            try {
                middleToolbar.visibility = GONE
                middleToolbar2.visibility = VISIBLE
                textView.visibility = GONE
                imageView.visibility = GONE
                videoView.visibility = VISIBLE
                val uri: Uri? = result.data!!.data
                selectedVideoUri = uri
                videoView.setVideoURI(uri)
                videoView.start()
                videoView.requestFocus()
                transferButton!!.performClick()
            } catch (e:Exception) {
                Log.d("DialogShortenImage", e.printStackTrace().toString())
            }
        }
    }
    /** 註冊 相簿回傳相片或影片 */
    private var selectedImageUri: Uri? = null
    private var selectedVideoUri: Uri? = null
    private val pickMediaLauncher = registerForActivityResult(PickVisualMedia()) { uri ->
        if (uri != null) {
            try {
                // Check if it's an image
                middleToolbar.visibility = GONE
                middleToolbar2.visibility = VISIBLE
                textView.visibility = GONE
                val uriType = contentResolver.getType(uri)
                if (uriType?.startsWith("image/") == true) {
                    // 影像
                    imageView.visibility = VISIBLE
                    videoView.visibility = GONE
                    if (Build.VERSION.SDK_INT>= 29) {
                        val source = ImageDecoder.createSource(contentResolver, uri)
                        val bitmap = ImageDecoder.decodeBitmap(source) { decoder, _, _->
                            decoder.setTargetSampleSize(1)
                            decoder.isMutableRequired = true
                        }
                        selectedImageUri = uri
                        Glide.with(this).load(bitmap).into(imageView)
                    } else {
                        val bitmap = MediaStore.Images.Media.getBitmap(contentResolver, uri)
                        selectedImageUri = uri
                        Glide.with(this).load(bitmap).into(imageView)
                    }
                    transferButton!!.performClick()
                } else {
                    // 影片
                    imageView.visibility = GONE
                    videoView.visibility = VISIBLE
                    selectedVideoUri = uri
                    videoView.setVideoURI(uri)
                    videoView.start()
                    videoView.requestFocus()
                    transferButton!!.performClick()
                }
            } catch (e: IOException) {
                Log.e("PhotoPicker", "Error loading image/video", e)
            }
        } else {
            Log.d("PhotoPicker", "No media selected")
        }
    }

    /** 註冊 確認權限 */
    private val permissionLauncher = registerForActivityResult(ActivityResultContracts.RequestPermission()) { isGranted ->
        if (isGranted) {
            openCameraIntent()
        }
    }
    private val permissionVideoLauncher = registerForActivityResult(ActivityResultContracts.RequestPermission()) { isGranted ->
        if (isGranted) {
            openVideoIntent()
        }
    }
    /** 開啟相機 */
    private lateinit var currentPhotoPath: String

    @SuppressLint("SimpleDateFormat")
    private fun openCameraIntent() {
        val intent = Intent(MediaStore.ACTION_IMAGE_CAPTURE)
        val timeStamp: String = SimpleDateFormat("yyyyMMddHHmmss").format(Date())
        val storageDir: File? = getExternalFilesDir(Environment.DIRECTORY_PICTURES)
        try {
            val photoFile: File? =
                File.createTempFile(
                    "babamutBBS_${timeStamp}_", /* prefix */
                    ".png", /* suffix */
                    storageDir /* directory */
                ).apply {
                    // Save a file: path for use with ACTION_VIEW intents
                    currentPhotoPath = absolutePath
                }
            photoFile?.also {
                selectedImageUri = FileProvider.getUriForFile(this, "com.kota.Bahamut.fileprovider", it)
                intent.putExtra(MediaStore.EXTRA_OUTPUT, selectedImageUri)
            }
        } catch (e: Exception) {
            Log.d("DialogShortenImage", e.printStackTrace().toString())
            ASToast.showShortToast(getContextString(R.string.dialog_shorten_image_error04))
            return
        }
        intentCameraLauncher.launch(intent)
    }
    private fun openVideoIntent() {
        val intent = Intent(MediaStore.ACTION_VIDEO_CAPTURE)
        intentVideoLauncher.launch(intent)
    }

    private fun showProcessingDialog() {
        object: ASRunner() {
            override fun run() {
                processingDialog.visibility = VISIBLE
            }
        }.runInMainThread()
    }
    private  fun closeProcessingDialog() {
        object: ASRunner() {
            override fun run() {
                processingDialog.visibility = INVISIBLE
            }
        }.runInMainThread()
    }

    private fun postUrl(str:String) {
        // 最上層是 發文 或 看板
        val page = PageContainer.getInstance().postArticlePage
        page.insertString(str)
    }

    override fun onConfigurationChanged(newConfig: Configuration) {
        super.onConfigurationChanged(newConfig)
        changeDialogHeight(newConfig)
    }
    private fun changeDialogHeight(newConfig: Configuration) {
        val layoutParams : ViewGroup.LayoutParams? = mainLayout.layoutParams
        if (newConfig.orientation == Configuration.ORIENTATION_LANDSCAPE) {
            layoutParams!!.height = ViewGroup.LayoutParams.MATCH_PARENT
        } else {
            val factor = applicationContext.resources.displayMetrics.density
            layoutParams!!.height = (500 * factor).toInt()
        }
        mainLayout.layoutParams = layoutParams
    }

    // 變更dialog寬度
    private fun setDialogWidth() {
        val screenWidth = resources.displayMetrics.widthPixels
        val dialogWidth = (screenWidth * 0.7).toInt()
        val oldLayoutParams = mainLayout.layoutParams
        oldLayoutParams.width = dialogWidth
        mainLayout.layoutParams = oldLayoutParams
    }
}
