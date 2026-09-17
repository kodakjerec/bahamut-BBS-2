package com.kota.Bahamut.dialogs

import android.Manifest.permission.CAMERA
import android.annotation.SuppressLint
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.content.res.Configuration
import android.net.Uri
import android.os.Bundle
import android.os.Environment
import android.provider.MediaStore
import android.util.Log
import android.widget.ImageView
import android.widget.VideoView
import androidx.activity.compose.setContent
import androidx.activity.result.PickVisualMediaRequest
import androidx.activity.result.contract.ActivityResultContracts
import androidx.activity.result.contract.ActivityResultContracts.PickVisualMedia
import androidx.activity.result.contract.ActivityResultContracts.StartActivityForResult
import androidx.appcompat.app.AppCompatActivity
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.widthIn
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.viewinterop.AndroidView
import androidx.core.content.FileProvider
import com.bumptech.glide.Glide
import com.kota.Bahamut.PageContainer
import com.kota.Bahamut.R
import com.kota.Bahamut.dialogs.uploadImgMethod.UploaderBahaImg
import com.kota.Bahamut.pages.PostArticlePage
import com.kota.Bahamut.pages.messages.MessageSub
import com.kota.Bahamut.pages.theme.ThemeStore
import com.kota.Bahamut.service.CommonFunctions
import com.kota.Bahamut.service.CommonFunctions.getContextString
import com.kota.Bahamut.service.UserSettings
import com.kota.Bahamut.ui.components.BahaButton
import com.kota.Bahamut.ui.components.ButtonType
import com.kota.Bahamut.ui.dialogs.BahaAlertDialogContent
import com.kota.Bahamut.ui.dialogs.BahaDialogButton
import com.kota.Bahamut.ui.dialogs.BahaProcessingDialogContent
import com.kota.Bahamut.ui.theme.AppTheme
import com.kota.Bahamut.ui.theme.BahamutAppTheme
import com.kota.asFramework.pageController.ASNavigationController
import com.kota.asFramework.thread.ASCoroutine
import com.kota.asFramework.ui.ASToast
import java.io.File
import java.text.SimpleDateFormat
import java.util.Date

class DialogShortenImage : AppCompatActivity() {
    private var outputParam by mutableStateOf("")
    private var selectedImageUri by mutableStateOf<Uri?>(null)
    private var selectedVideoUri by mutableStateOf<Uri?>(null)
    private var isUploading by mutableStateOf(false)
    private var currentPhotoPath: String = ""

    override fun attachBaseContext(newBase: Context) {
        UserSettings(newBase)
        if (!UserSettings.propertiesFollowSystemDarkMode) {
            val config = Configuration(newBase.resources.configuration)
            config.uiMode = (config.uiMode and Configuration.UI_MODE_NIGHT_MASK.inv()) or Configuration.UI_MODE_NIGHT_NO
            val context = newBase.createConfigurationContext(config)
            super.attachBaseContext(context)
        } else {
            super.attachBaseContext(newBase)
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        setTheme(ThemeStore.getDialogThemeResId())
        super.onCreate(savedInstanceState)
        window.setBackgroundDrawable(null)

        setContent {
            BahamutAppTheme {
                Content()
            }
        }
    }

    @Composable
    private fun Content() {
        val colors = AppTheme.colors
        val scrollState = rememberScrollState()

        Box(
            modifier = Modifier.fillMaxSize(),
            contentAlignment = Alignment.Center
        ) {
            BahaAlertDialogContent(
                modifier = Modifier.widthIn(min = 280.dp, max = 360.dp),
                title = CommonFunctions.getContextString(R.string.dialog_shorten_img_title),
                buttons = listOf(
                    BahaDialogButton(
                        text = CommonFunctions.getContextString(R.string.cancel),
                        type = ButtonType.SECONDARY,
                        onClick = { finish() }
                    ),
                    BahaDialogButton(
                        text = CommonFunctions.getContextString(R.string.send),
                        type = ButtonType.NORMAL,
                        onClick = {
                            if (outputParam.isNotEmpty()) {
                                postUrl(outputParam)
                            }
                            finish()
                        }
                    )
                )
            ) {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .verticalScroll(scrollState),
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    if (selectedImageUri == null && selectedVideoUri == null) {
                        Text(
                            text = CommonFunctions.getContextString(R.string.dialog_shorten_img_hint),
                            color = colors.textSecondary,
                            fontSize = 14.sp,
                            modifier = Modifier.padding(vertical = 12.dp)
                        )

                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.spacedBy(8.dp)
                        ) {
                            BahaButton(
                                text = CommonFunctions.getContextString(R.string.dialog_shorten_img_album),
                                type = ButtonType.NORMAL,
                                onClick = {
                                    pickMediaLauncher.launch(PickVisualMediaRequest(PickVisualMedia.ImageAndVideo))
                                },
                                modifier = Modifier.weight(1f),
                                minHeight = 36.dp
                            )
                            BahaButton(
                                text = CommonFunctions.getContextString(R.string.dialog_shorten_img_camera),
                                type = ButtonType.NORMAL,
                                onClick = {
                                    if (checkSelfPermission(CAMERA) == PackageManager.PERMISSION_GRANTED) {
                                        openCameraIntent()
                                    } else {
                                        permissionLauncher.launch(CAMERA)
                                    }
                                },
                                modifier = Modifier.weight(1f),
                                minHeight = 36.dp
                            )
                            BahaButton(
                                text = CommonFunctions.getContextString(R.string.dialog_shorten_img_video),
                                type = ButtonType.NORMAL,
                                onClick = {
                                    if (checkSelfPermission(CAMERA) == PackageManager.PERMISSION_GRANTED) {
                                        openVideoIntent()
                                    } else {
                                        permissionVideoLauncher.launch(CAMERA)
                                    }
                                },
                                modifier = Modifier.weight(1f),
                                minHeight = 36.dp
                            )
                        }
                    } else {
                        // 顯示預覽
                        if (selectedImageUri != null) {
                            AndroidView(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .height(200.dp)
                                    .clip(RoundedCornerShape(4.dp)),
                                factory = { ctx ->
                                    ImageView(ctx).apply {
                                        scaleType = ImageView.ScaleType.FIT_CENTER
                                        Glide.with(ctx).load(selectedImageUri).into(this)
                                    }
                                },
                                update = { imageView ->
                                    Glide.with(imageView.context).load(selectedImageUri).into(imageView)
                                }
                            )
                        } else if (selectedVideoUri != null) {
                            AndroidView(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .height(200.dp)
                                    .clip(RoundedCornerShape(4.dp)),
                                factory = { ctx ->
                                    VideoView(ctx).apply {
                                        setVideoURI(selectedVideoUri)
                                        start()
                                    }
                                }
                            )
                        }

                        Spacer(modifier = Modifier.height(10.dp))

                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.spacedBy(8.dp)
                        ) {
                            BahaButton(
                                text = CommonFunctions.getContextString(R.string.reset),
                                type = ButtonType.SECONDARY,
                                onClick = { resetState() },
                                modifier = Modifier.weight(1f),
                                minHeight = 36.dp
                            )
                            BahaButton(
                                text = CommonFunctions.getContextString(R.string.dialog_shorten_img_title),
                                type = ButtonType.NORMAL,
                                onClick = { startTransfer() },
                                modifier = Modifier.weight(1f),
                                minHeight = 36.dp
                            )
                        }
                    }

                    if (outputParam.isNotEmpty()) {
                        Spacer(modifier = Modifier.height(12.dp))
                        Box(
                            modifier = Modifier
                                .fillMaxWidth()
                                .clip(RoundedCornerShape(4.dp))
                                .background(colors.dialogBlockBackground)
                                .padding(10.dp)
                        ) {
                            Text(
                                text = outputParam,
                                color = colors.bbsAuthor0,
                                fontSize = 14.sp
                            )
                        }
                    }
                }
            }

            if (isUploading) {
                Box(
                    modifier = Modifier
                        .fillMaxSize()
                        .background(colors.surface.copy(alpha = 0.5f))
                        .clickable { isUploading = false },
                    contentAlignment = Alignment.Center
                ) {
                    BahaProcessingDialogContent(message = CommonFunctions.getContextString(R.string.dialog_shorten_url_under_transfer))
                }
            }
        }
    }

    private fun resetState() {
        outputParam = ""
        selectedImageUri = null
        selectedVideoUri = null
        isUploading = false
    }

    private fun startTransfer() {
        val finalUri = selectedImageUri ?: selectedVideoUri
        if (finalUri == null) {
            ASToast.showShortToast(getContextString(R.string.dialog_shorten_image_error02))
            return
        }

        isUploading = true
        ASCoroutine.runInNewCoroutine {
            val uploader = UploaderBahaImg()
            uploader.uploadImage(applicationContext, finalUri, object : UploaderBahaImg.UploadCallback {
                override fun onSuccess(imageUrl: String) {
                    ASCoroutine.ensureMainThread {
                        outputParam = imageUrl
                        var shortenTimes = UserSettings.propertiesNoVipShortenTimes
                        UserSettings.propertiesNoVipShortenTimes = ++shortenTimes
                        isUploading = false
                    }
                }

                override fun onError(message: String) {
                    ASCoroutine.ensureMainThread {
                        ASToast.showShortToast(getContextString(R.string.dialog_shorten_image_error03) + " " + message)
                        isUploading = false
                    }
                }
            })
        }
    }

    private val intentCameraLauncher = registerForActivityResult(StartActivityForResult()) { result ->
        if (result.resultCode == RESULT_OK) {
            startTransfer()
        }
    }

    private val intentVideoLauncher = registerForActivityResult(StartActivityForResult()) { result ->
        if (result.resultCode == RESULT_OK) {
            val uri: Uri? = result.data?.data
            selectedVideoUri = uri
            startTransfer()
        }
    }

    private val pickMediaLauncher = registerForActivityResult(PickVisualMedia()) { uri ->
        if (uri != null) {
            val uriType = contentResolver.getType(uri)
            if (uriType?.startsWith("image/") == true) {
                selectedImageUri = uri
                selectedVideoUri = null
            } else {
                selectedVideoUri = uri
                selectedImageUri = null
            }
            startTransfer()
        }
    }

    private val permissionLauncher = registerForActivityResult(ActivityResultContracts.RequestPermission()) { isGranted ->
        if (isGranted) openCameraIntent()
    }

    private val permissionVideoLauncher = registerForActivityResult(ActivityResultContracts.RequestPermission()) { isGranted ->
        if (isGranted) openVideoIntent()
    }

    @SuppressLint("SimpleDateFormat")
    private fun openCameraIntent() {
        val intent = Intent(MediaStore.ACTION_IMAGE_CAPTURE)
        val timeStamp: String = SimpleDateFormat("yyyyMMddHHmmss").format(Date())
        val storageDir: File? = getExternalFilesDir(Environment.DIRECTORY_PICTURES)
        try {
            val photoFile: File? = File.createTempFile("babamutBBS_${timeStamp}_", ".png", storageDir).apply {
                currentPhotoPath = absolutePath
            }
            photoFile?.also {
                val uri = FileProvider.getUriForFile(this, "com.kota.Bahamut.fileprovider", it)
                selectedImageUri = uri
                intent.putExtra(MediaStore.EXTRA_OUTPUT, uri)
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

    private fun postUrl(str: String) {
        val topPage = ASNavigationController.currentController?.topController
        if ((topPage as Any).javaClass == PostArticlePage::class.java) {
            val aPage = PageContainer.instance!!.postArticlePage
            aPage.insertString(str)
        } else if (topPage.javaClass == MessageSub::class.java) {
            val aPage = PageContainer.instance!!.getMessageSub()
            aPage.insertString(str)
        }
    }
}
