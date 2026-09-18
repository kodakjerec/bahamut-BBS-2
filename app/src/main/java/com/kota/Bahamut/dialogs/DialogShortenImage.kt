package com.kota.Bahamut.dialogs

import android.Manifest.permission.CAMERA
import android.annotation.SuppressLint
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.content.res.Configuration
import android.graphics.drawable.ColorDrawable
import android.net.Uri
import android.os.Bundle
import android.os.Environment
import android.provider.MediaStore
import android.util.Log
import android.view.ViewGroup
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
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.widthIn
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.style.TextOverflow
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
import com.kota.Bahamut.service.UserSettings
import com.kota.Bahamut.ui.components.BahaButton
import com.kota.Bahamut.ui.components.BahaTextSize
import com.kota.Bahamut.ui.components.ButtonType
import com.kota.Bahamut.ui.dialogs.BahaAlertDialogContent
import com.kota.Bahamut.ui.dialogs.BahaDialogButton
import com.kota.Bahamut.ui.dialogs.BahaProcessingDialog
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
        window.setBackgroundDrawable(ColorDrawable(android.graphics.Color.TRANSPARENT))
        window.decorView.setBackgroundColor(android.graphics.Color.TRANSPARENT)
        window.setLayout(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.MATCH_PARENT)

        setContent {
            BahamutAppTheme {
                Content()
            }
        }
    }

    @Composable
    private fun Content() {
        val colors = AppTheme.colors

        Box(
            modifier = Modifier
                .fillMaxSize()
                .clickable(
                    interactionSource = remember { MutableInteractionSource() },
                    indication = null
                ) { finish() },
            contentAlignment = Alignment.Center
        ) {
            Box(
                modifier = Modifier.clickable(
                    interactionSource = remember { MutableInteractionSource() },
                    indication = null
                ) { /* 點擊對話框本體不關閉 */ }
            ) {
                BahaAlertDialogContent(
                    modifier = Modifier.widthIn(min = 280.dp, max = 340.dp),
                    title = stringResource(R.string.dialog_shorten_img_title),
                    contentPadding = PaddingValues(0.dp),
                    buttons = listOf(
                        BahaDialogButton(
                            text = stringResource(R.string.cancel),
                            onClick = { finish() }
                        ),
                        BahaDialogButton(
                            text = stringResource(R.string.send),
                            enabled = outputParam.isNotEmpty(),
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
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        // 預覽區 (圖片 / 影片 / 空黑底)
                        Box(
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(230.dp)
                                .background(colors.pageBackground),
                            contentAlignment = Alignment.Center
                        ) {
                            if (selectedImageUri != null) {
                                AndroidView(
                                    modifier = Modifier.fillMaxSize(),
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
                                    modifier = Modifier.fillMaxSize(),
                                    factory = { ctx ->
                                        VideoView(ctx).apply {
                                            setVideoURI(selectedVideoUri)
                                            start()
                                        }
                                    }
                                )
                            }
                        }

                        // 分隔線
                        Box(
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(1.dp)
                                .background(colors.divider)
                        )

                        // 選擇來源按鈕列 (相簿 | 相機 | 錄影)
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(48.dp)
                        ) {
                            BahaButton(
                                text = stringResource(R.string.dialog_shorten_img_album),
                                type = ButtonType.NORMAL,
                                fontSize = BahaTextSize.TITLE,
                                onClick = {
                                    pickMediaLauncher.launch(PickVisualMediaRequest(PickVisualMedia.ImageAndVideo))
                                },
                                modifier = Modifier
                                    .weight(1f)
                                    .fillMaxHeight(),
                                minHeight = 48.dp
                            )
                            Box(
                                modifier = Modifier
                                    .width(1.dp)
                                    .fillMaxHeight()
                                    .background(colors.toolbarDivider)
                            )
                            BahaButton(
                                text = stringResource(R.string.dialog_shorten_img_camera),
                                type = ButtonType.NORMAL,
                                fontSize = BahaTextSize.TITLE,
                                onClick = {
                                    if (checkSelfPermission(CAMERA) == PackageManager.PERMISSION_GRANTED) {
                                        openCameraIntent()
                                    } else {
                                        permissionLauncher.launch(CAMERA)
                                    }
                                },
                                modifier = Modifier
                                    .weight(1f)
                                    .fillMaxHeight(),
                                minHeight = 48.dp
                            )
                            Box(
                                modifier = Modifier
                                    .width(1.dp)
                                    .fillMaxHeight()
                                    .background(colors.toolbarDivider)
                            )
                            BahaButton(
                                text = stringResource(R.string.dialog_shorten_img_video),
                                type = ButtonType.NORMAL,
                                fontSize = BahaTextSize.TITLE,
                                onClick = {
                                    if (checkSelfPermission(CAMERA) == PackageManager.PERMISSION_GRANTED) {
                                        openVideoIntent()
                                    } else {
                                        permissionVideoLauncher.launch(CAMERA)
                                    }
                                },
                                modifier = Modifier
                                    .weight(1f)
                                    .fillMaxHeight(),
                                minHeight = 48.dp
                            )
                        }

                        // 分隔線
                        Box(
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(1.dp)
                                .background(colors.divider)
                        )

                        // 縮址預覽 / 範例文字列
                        Box(
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(38.dp)
                                .background(colors.pageBackground)
                                .padding(horizontal = 8.dp),
                            contentAlignment = Alignment.Center
                        ) {
                            Text(
                                text = outputParam.ifEmpty {
                                    stringResource(R.string.dialog_paint_color_sample_ch)
                                },
                                color = if (outputParam.isNotEmpty()) colors.bbsAuthor0 else colors.textPrimary,
                                fontSize = 15.sp,
                                maxLines = 1,
                                overflow = TextOverflow.Ellipsis
                            )
                        }
                    }
                }
            }

            if (isUploading) {
                Box(
                    modifier = Modifier
                        .fillMaxSize()
                        .background(colors.pageBackground.copy(alpha = 0.5f))
                        .clickable { isUploading = false },
                    contentAlignment = Alignment.Center
                ) {
                    BahaProcessingDialog(message = stringResource(R.string.dialog_shorten_url_under_transfer))
                }
            }
        }
    }

    private fun startTransfer() {
        val finalUri = selectedImageUri ?: selectedVideoUri
        if (finalUri == null) {
            ASToast.showShortToast(CommonFunctions.getContextString(R.string.dialog_shorten_image_error02))
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
                        ASToast.showShortToast(CommonFunctions.getContextString(R.string.dialog_shorten_image_error03) + " " + message)
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
                val uri = FileProvider.getUriForFile(this, "${packageName}.fileprovider", it)
                selectedImageUri = uri
                selectedVideoUri = null
                intent.putExtra(MediaStore.EXTRA_OUTPUT, uri)
            }
        } catch (e: Exception) {
            Log.d(javaClass.simpleName, e.message.toString())
            ASToast.showShortToast(CommonFunctions.getContextString(R.string.dialog_shorten_image_error04))
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
