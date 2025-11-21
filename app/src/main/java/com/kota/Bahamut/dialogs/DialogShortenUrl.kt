package com.kota.Bahamut.dialogs

import android.content.ClipboardManager
import android.content.Context
import android.content.res.Configuration
import android.text.style.URLSpan
import android.text.util.Linkify
import android.util.Log
import android.view.OrientationEventListener
import android.view.View
import android.view.View.OnClickListener
import android.view.ViewGroup
import android.widget.Button
import android.widget.CheckBox
import android.widget.EditText
import android.widget.RelativeLayout
import android.widget.TextView
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.kota.Bahamut.R
import com.kota.Bahamut.dataModels.UrlDatabase
import com.kota.Bahamut.pages.theme.ThemeFunctions
import com.kota.Bahamut.service.CommonFunctions.getContextString
import com.kota.Bahamut.service.UserSettings
import com.kota.asFramework.dialog.ASDialog
import com.kota.asFramework.dialog.ASProcessingDialog
import com.kota.asFramework.thread.ASRunner
import com.kota.asFramework.ui.ASToast
import okhttp3.MultipartBody
import okhttp3.OkHttpClient
import okhttp3.Request
import okhttp3.RequestBody
import org.json.JSONObject

class DialogShortenUrl : ASDialog(), OnClickListener,DialogShortenUrlItemViewListener {
    private var mainLayout: RelativeLayout
    private var editText: EditText
    private var sampleTextView: TextView
    private var sendButton: Button? = null
    private var outputParam: String = ""
    private lateinit var shortenUrlListener: DialogShortenUrlListener
    private lateinit var dialogShortenUrlItemViewAdapter: DialogShortenUrlItemViewAdapter
    private val urlDatabase = UrlDatabase(context)
    private var isTransfer: Boolean = true
    private var mOrientationEventListener: OrientationEventListener

    override val name: String?
        get() = "BahamutShortenUrlDialog"

    /** 轉檔 */
    private var transferListener = OnClickListener {
        var shortenTimes: Int = UserSettings.propertiesNoVipShortenTimes
        if (!UserSettings.propertiesVIP && shortenTimes>30) {
            ASToast.showLongToast(getContextString(R.string.vip_only_message))
            return@OnClickListener
        }

        if (editText.text.isEmpty()) {
            ASToast.showShortToast(getContextString(R.string.keyword_hint))
            return@OnClickListener
        }
        // 擷取文章內的所有連結
        val textView = TextView(context)
        textView.text = editText.text
        Linkify.addLinks(textView, Linkify.WEB_URLS)
        val urls = textView.urls
        if (urls.isEmpty()) {
            ASToast.showShortToast(getContextString(R.string.no_url))
            return@OnClickListener
        }

        val targetUrl = urls[0].url

        // 找歷史檔案
        val historyItem = urlDatabase.getShortenUrl(targetUrl)
        if (!historyItem.isEmpty()) {
            val shortUrl = historyItem[0]?.shortenUrl
            changeFrontend(shortUrl!!)
            UserSettings.propertiesNoVipShortenTimes = ++shortenTimes
            ASToast.showShortToast(getContextString(R.string.dialog_shorten_url_same_url))
            return@OnClickListener
        }

        ASProcessingDialog.showProcessingDialog(getContextString(R.string.dialog_shorten_url_under_transfer))
        val apiUrl = "https://worker-short-url.kodakjerec.workers.dev/"
        val client = OkHttpClient()
        val body: RequestBody = MultipartBody.Builder().setType(MultipartBody.FORM)
            .addFormDataPart("type", "shortenurl")
            .addFormDataPart("url", targetUrl)
            .build()
        val request: Request = Request.Builder()
            .url(apiUrl)
            .post(body)
            .build()
        ASRunner.runInNewThread {
            try{
                client.newCall(request).execute().use { response->
                    val data = response.body.string()
                    val jsonObject = JSONObject(data)
                    val status = jsonObject.optString("res")
                    if (status.isNotEmpty()) {
                        val shortUrl = jsonObject.getString("short_url")
                        val title = jsonObject.getString("title")
                        val description = jsonObject.getString("description")

                        object : ASRunner() {
                            override fun run() {
                                editText.setText(targetUrl)
                                changeFrontend(shortUrl)
                                UserSettings.propertiesNoVipShortenTimes = ++shortenTimes
                            }
                        }.runInMainThread()

                        // 網址存進資料庫
                        urlDatabase.addShortenUrl(targetUrl, title, description, shortUrl)
                        DialogShortenUrlItemViewAdapter(urlDatabase.shortenUrls)

                    } else {
                        val msg = jsonObject.getString("msg")
                        ASToast.showLongToast(msg)
                    }
                }
            } catch (e: Exception) {
                ASToast.showLongToast(e.message.toString())
                Log.e(javaClass.simpleName, e.message.toString())
            } finally {
                ASProcessingDialog.dismissProcessingDialog()
            }
        }
    }
    /** 變更畫面上選項 */
    fun changeFrontend(shortUrl: String) {
        sampleTextView.text = shortUrl
        outputParam = sampleTextView.text.toString()
        sendButton?.isEnabled = true
    }

    /** 擷取剪貼簿 */
    var fromClipData = ""
    private fun catchClipBoard() {
            val clipboardManager =
                context.getSystemService(Context.CLIPBOARD_SERVICE) as ClipboardManager
            val clipData = clipboardManager.primaryClip
            if (clipData != null && clipData.itemCount>0) {
                val clipDataIndex0 = clipData.getItemAt(0)
                if (clipDataIndex0 != null && clipDataIndex0.text!=null) {
                    fromClipData = clipDataIndex0.text.toString()
                    editText.setText(fromClipData)
                    urlRemoveId()
                }
            }
    }

    /** 切換去識別化 */
    private val changeNonIdListenerForCheckbox = OnClickListener { _ ->
        val checkbox = mainLayout.findViewById<CheckBox>(R.id.dialog_shorten_url_non_id_checkbox)
        UserSettings.setPropertiesShortUrlNonId(checkbox.isChecked)
        urlRemoveId()
    }
    private val changeNonIdListenerForLabel = OnClickListener { _ ->
        val checkbox = mainLayout.findViewById<CheckBox>(R.id.dialog_shorten_url_non_id_checkbox)
        checkbox.isChecked = !checkbox.isChecked
        UserSettings.setPropertiesShortUrlNonId(checkbox.isChecked)
        urlRemoveId()
    }

    /** 清除內容 */
    private val resetListener = OnClickListener {_ ->
        editText.setText("")
        sampleTextView.text = getContextString(R.string.dialog_paint_color_sample_ch)
        outputParam = ""
    }

    /** 切換設定 */
    private val changeModeListener = OnClickListener { _->
        if (isTransfer) {
            // 從轉檔切換到紀錄
            isTransfer = false
            findViewById<Button>(R.id.dialog_shorten_url_change_mode).text = getContextString(R.string.dialog_shorten_url_transfer)
            findViewById<View>(R.id.dialog_shorten_url_layout_transfer).visibility = View.GONE
            findViewById<View>(R.id.dialog_shorten_url_layout_recycleView).visibility = View.VISIBLE

            val recyclerView = findViewById<RecyclerView>(R.id.dialog_shorten_url_layout_recycleView)
            recyclerView.layoutManager = LinearLayoutManager(context)

            dialogShortenUrlItemViewAdapter = DialogShortenUrlItemViewAdapter(urlDatabase.shortenUrls)
            recyclerView.adapter = dialogShortenUrlItemViewAdapter
            dialogShortenUrlItemViewAdapter.setOnItemClickListener(this)
        } else {
            isTransfer = true
            findViewById<Button>(R.id.dialog_shorten_url_change_mode).text = getContextString(R.string.record)
            findViewById<View>(R.id.dialog_shorten_url_middle_linear_layout).visibility = View.VISIBLE
            findViewById<View>(R.id.dialog_shorten_url_layout_recycleView).visibility = View.GONE
        }
    }

    private var oldOrientation: Int = 1
    init {
        val layoutId = R.layout.dialog_shorten_url
        requestWindowFeature(1)
        setContentView(layoutId)
        window?.setBackgroundDrawable(null)
        setTitle(context.getString(R.string.dialog_shorten_url_title))
        mainLayout = findViewById(R.id.dialog_shorten_url_layout)
        editText = mainLayout.findViewById(R.id.dialog_shorten_url_content)
        sampleTextView = mainLayout.findViewById(R.id.dialog_shorten_url_sample)
        catchClipBoard()

        // 按鈕
        mainLayout.findViewById<Button>(R.id.dialog_shorten_url_transfer).setOnClickListener(transferListener)
        sendButton = mainLayout.findViewById(R.id.send)
        sendButton?.setOnClickListener(this)
        mainLayout.findViewById<Button>(R.id.cancel).setOnClickListener(this)
        mainLayout.findViewById<Button>(R.id.dialog_shorten_url_reset).setOnClickListener(resetListener)
        mainLayout.findViewById<Button>(R.id.dialog_shorten_url_change_mode).setOnClickListener(changeModeListener)
        val checkbox = mainLayout.findViewById<CheckBox>(R.id.dialog_shorten_url_non_id_checkbox)
        checkbox.isChecked = UserSettings.shortUrlNonId
        checkbox.setOnClickListener(changeNonIdListenerForCheckbox)
        mainLayout.findViewById<TextView>(R.id.dialog_shorten_url_non_id_label).setOnClickListener(changeNonIdListenerForLabel)

        // 監聽轉動事件, 變更視窗大小
        mOrientationEventListener = object : OrientationEventListener(context) {
            override fun onOrientationChanged(orientation: Int) {
                val nowOrientation: Int = context.resources.configuration.orientation

                if (nowOrientation!=oldOrientation) {
                    val layoutParams : ViewGroup.LayoutParams? = mainLayout.layoutParams
                    if (nowOrientation == Configuration.ORIENTATION_LANDSCAPE) {
                        layoutParams?.height = ViewGroup.LayoutParams.WRAP_CONTENT
                        oldOrientation = nowOrientation
                    } else {
                        val factor = context.resources.displayMetrics.density
                        layoutParams?.height = (500 * factor).toInt()
                        oldOrientation = nowOrientation
                    }
                    mainLayout.layoutParams = layoutParams
                }
            }
        }
        setDialogWidth()

        // 替換外觀
        ThemeFunctions().layoutReplaceTheme(mainLayout)
    }

    override fun onClick(view: View) {
        if (view === sendButton) {
            if (outputParam.isEmpty())
                shortenUrlListener.onShortenUrlDone(editText.text.toString())
            else
                shortenUrlListener.onShortenUrlDone(outputParam)
        }
        dismiss()
    }

    fun setListener(listener: DialogShortenUrlListener) {
        shortenUrlListener = listener
    }

    override fun onDialogShortenUrlItemViewClicked(dialogShortenUrlItemView: DialogShortenUrlViewHolder) {
        val shortUrl = dialogShortenUrlItemViewAdapter.getItem(dialogShortenUrlItemView.layoutPosition).shortenUrl
        changeFrontend(shortUrl!!)
    }

    override fun onStart() {
        super.onStart()
        mOrientationEventListener.enable()
    }

    override fun onStop() {
        super.onStop()
        mOrientationEventListener.disable()
    }

    /** 變更dialog寬度 */
    private fun setDialogWidth() {
        val screenWidth = context.resources.displayMetrics.widthPixels
        val dialogWidth = (screenWidth * 0.7).toInt()
        val oldLayoutParams = mainLayout.layoutParams
        oldLayoutParams.width = dialogWidth
        mainLayout.layoutParams = oldLayoutParams
    }

    /** 去識別化 */
    private fun urlRemoveId() {
        val filterString = if (UserSettings.shortUrlNonId) {
            var returnString = editText.text.toString()
            val textView = TextView(context)
            textView.text = returnString
            Linkify.addLinks(textView, Linkify.WEB_URLS)

            val urls: Array<URLSpan> = textView.urls
            if (urls.isNotEmpty()) {
                // 針對網址處理

                val firstUrl = urls[0].url
                val splits = firstUrl.split("?")
                if (splits.size>=2) {
                    // 有參數

                    // 先指定給前面位址
                    returnString = splits[0]

                    // 特定網址參數例外處理
                    // www.facebook.com , 只保留 fbid=1234
                    // www.youtube.com , 只保留 v=1234
                    if (splits[0].indexOf("www.youtube.com")>0 || splits[0].indexOf("www.facebook.com")>0) {
                        val reserveKeys = arrayOf("v","fbid")
                        val params = splits[1].split("&")
                        if (params.isNotEmpty()) {
                            for (param in params) {
                                val paramPair = param.split("=")
                                if (reserveKeys.contains(paramPair[0])) {
                                    returnString+= "?$param"
                                }
                            }
                        } else {
                            returnString += splits[1]
                        }
                    }
                } else {
                    // 沒有參數
                    returnString = firstUrl
                }
            }

            returnString
        } else {
            // 不啟動去識別化
            fromClipData
        }

        editText.setText(filterString)
    }
}
