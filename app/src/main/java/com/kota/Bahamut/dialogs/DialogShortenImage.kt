package com.kota.Bahamut.dialogs

import android.Manifest.permission.CAMERA
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
import com.kota.Bahamut.PageContainer
import com.kota.Bahamut.R
import com.kota.Bahamut.dialogs.uploadImgMethod.UploaderLitterCatBox
import com.kota.Bahamut.pages.PostArticlePage
import com.kota.Bahamut.pages.messages.MessageSub
import com.kota.Bahamut.pages.theme.ThemeFunctions
import com.kota.Bahamut.service.CommonFunctions.getContextString
import com.kota.Bahamut.service.UserSettings
import com.kota.asFramework.pageController.ASNavigationController
import com.kota.asFramework.thread.ASRunner
import com.kota.asFramework.ui.ASToast
import java.io.File
import java.io.IOException
import java.text.SimpleDateFormat
import java.util.Date

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
        window?.setBackgroundDrawable(null)
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
        transferButton?.setOnClickListener(transferListener)
        sendButton = mainLayout.findViewById(R.id.send)
        sendButton?.setOnClickListener(this)
        sendButton?.isEnabled = false
        mainLayout.findViewById<Button>(R.id.dialog_shorten_image_reset).setOnClickListener(resetListener)
        mainLayout.findViewById<Button>(R.id.cancel).setOnClickListener(this)

        // 預設高度
        changeDialogHeight(resources.configuration)
        setDialogWidth()

        // 替換外觀
        ThemeFunctions().layoutReplaceTheme(mainLayout)
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
        showProcessingDialog()

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
        ASRunner.runInNewThread {
            try {
                val uploaderObj = UploaderLitterCatBox()
                // 本地檔案上傳
                val link = uploaderObj.postImage( finalUri )
                if (link.startsWith("http")) {
                    object : ASRunner() {
                        override fun run() {
                            sampleTextView?.text = link
                            outputParam = sampleTextView?.text.toString()
                            sendButton?.isEnabled = true
                            transferButton?.isEnabled = false
                            var shortenTimes: Int = UserSettings.propertiesNoVipShortenTimes
                            UserSettings.propertiesNoVipShortenTimes = ++shortenTimes
                        }
                    }.runInMainThread()
                } else {
                    throw Exception("")
                }
            } catch (e: Exception) {
                ASToast.showShortToast(getContextString(R.string.dialog_shorten_image_error03) + " " + e.message)
                Log.e(javaClass.simpleName, e.message.toString())
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
        sampleTextView?.text = getContextString(R.string.dialog_paint_color_sample_ch)
        outputParam = ""
        selectedImageUri = null
        selectedVideoUri = null
        sendButton?.isEnabled = false
        transferButton?.isEnabled = true
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
                transferButton?.performClick()
            } catch (e:Exception) {
                Log.d(javaClass.simpleName, e.message.toString())
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
                val uri: Uri? = result.data?.data
                selectedVideoUri = uri
                videoView.setVideoURI(uri)
                videoView.start()
                videoView.requestFocus()
                transferButton?.performClick()
            } catch (e:Exception) {
                Log.d(javaClass.simpleName, e.message.toString())
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
                    transferButton?.performClick()
                } else {
                    // 影片
                    imageView.visibility = GONE
                    videoView.visibility = VISIBLE
                    selectedVideoUri = uri
                    videoView.setVideoURI(uri)
                    videoView.start()
                    videoView.requestFocus()
                    transferButton?.performClick()
                }
            } catch (_: IOException) {
                Log.e(javaClass.simpleName, "Error loading image/video")
            }
        } else {
            Log.d(javaClass.simpleName, "No media selected")
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
            Log.d(javaClass.simpleName, e.message.toString())
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
        val topPage = ASNavigationController.currentController?.topController
        if ((topPage as Any).javaClass == PostArticlePage::class.java) {
            val aPage = PageContainer.instance!!.postArticlePage
            aPage.insertString(str)
        } else if (topPage.javaClass == MessageSub::class.java) {
            val aPage = PageContainer.instance!!.getMessageSub()
            aPage.insertString(str)
        }
    }

    override fun onConfigurationChanged(newConfig: Configuration) {
        super.onConfigurationChanged(newConfig)
        changeDialogHeight(newConfig)
    }
    private fun changeDialogHeight(newConfig: Configuration) {
        val layoutParams : ViewGroup.LayoutParams? = mainLayout.layoutParams
        if (newConfig.orientation == Configuration.ORIENTATION_LANDSCAPE) {
            layoutParams?.height = ViewGroup.LayoutParams.MATCH_PARENT
        } else {
            val factor = applicationContext.resources.displayMetrics.density
            layoutParams?.height = (500 * factor).toInt()
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
