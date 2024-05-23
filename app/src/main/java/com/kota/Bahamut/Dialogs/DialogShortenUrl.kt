package com.kota.Bahamut.Dialogs

import android.content.ClipboardManager
import android.content.Context
import android.text.util.Linkify
import android.util.Log
import android.view.View
import android.view.View.GONE
import android.view.View.OnClickListener
import android.view.View.VISIBLE
import android.widget.Button
import android.widget.EditText
import android.widget.LinearLayout
import android.widget.ScrollView
import android.widget.TextView
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.kota.ASFramework.Dialog.ASDialog
import com.kota.ASFramework.Dialog.ASProcessingDialog
import com.kota.ASFramework.Thread.ASRunner
import com.kota.ASFramework.UI.ASToast
import com.kota.Bahamut.DataModels.ShortenUrl
import com.kota.Bahamut.DataModels.UrlDatabase
import com.kota.Bahamut.R
import com.kota.Bahamut.Service.CommonFunctions.getContextString
import com.kota.Bahamut.Service.UserSettings
import okhttp3.MultipartBody
import okhttp3.OkHttpClient
import okhttp3.Request
import okhttp3.RequestBody
import org.json.JSONObject
import java.util.Objects
import java.util.Vector

class DialogShortenUrl : ASDialog(), OnClickListener,DialogShortenUrlItemViewListener {
    private var editText: EditText? = null
    private var sampleTextView: TextView? = null
    private var sendButton: Button? = null
    private var outputParam: String? = null
    private var shortenUrlListener: DialogShortenUrlListener? = null
    private var dialogShortenUrlItemViewAdapter: DialogShortenUrlItemViewAdapter? = null
    private val urlDatabase = UrlDatabase(context)
    private var isTransfer: Boolean = true

    override fun getName(): String {
        return "BahamutShortenUrlDialog"
    }

    /** 轉檔 */
    private var transferListener = OnClickListener {
        var shortenTimes: Int = UserSettings.getPropertiesNoVipShortenTimes()
        if (!UserSettings.getPropertiesVIP() && shortenTimes>30) {
            ASToast.showLongToast(getContextString(R.string.vip_only_message))
            return@OnClickListener
        }

        if (editText!!.text.isEmpty()) {
            ASToast.showShortToast(getContextString(R.string.keyword_hint))
            return@OnClickListener
        }
        // 擷取文章內的所有連結
        val textView = TextView(context)
        textView.text = editText!!.text
        Linkify.addLinks(textView, Linkify.WEB_URLS)
        val urls = textView.urls
        if (urls.isEmpty()) {
            ASToast.showShortToast(getContextString(R.string.no_url))
            return@OnClickListener
        }

        val targetUrl = urls[0].url

        // 找歷史檔案
        val historyItem:Vector<ShortenUrl> = urlDatabase.getShortenUrl(targetUrl)
        if (!historyItem.isEmpty()) {
            val shortUrl = historyItem[0].shorten_url
            changeFrontend(shortUrl)
            UserSettings.setPropertiesNoVipShortenTimes(++shortenTimes)
            ASToast.showShortToast(getContextString(R.string.dialog_shorten_url_same_url))
            return@OnClickListener
        }

        ASProcessingDialog.showProcessingDialog(getContextString(R.string.dialog_shorten_url_under_transfer))
        val apiUrl = "https://short-url-lqeallcr2q-de.a.run.app"
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
                    val data = response.body!!.string()
                    val jsonObject = JSONObject(data)
                    val status = jsonObject.optString("res")
                    if (status.isNotEmpty()) {
                        val shortUrl = jsonObject.getString("short_url")
                        val title = jsonObject.getString("title")
                        val description = jsonObject.getString("description")

                        object : ASRunner() {
                            override fun run() {
                                editText!!.setText(targetUrl)
                                changeFrontend(shortUrl)
                                UserSettings.setPropertiesNoVipShortenTimes(++shortenTimes)
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
                ASToast.showLongToast(e.printStackTrace().toString())
                Log.e("ShortenUrl", e.printStackTrace().toString())
            } finally {
                ASProcessingDialog.dismissProcessingDialog()
            }
        }
    }
    /** 變更畫面上選項 */
    fun changeFrontend(shortUrl: String) {
        sampleTextView!!.text = shortUrl
        outputParam = sampleTextView!!.text.toString()
        sendButton!!.isEnabled = true
    }

    /** 擷取剪貼簿 */
    private fun catchClipBoard() {
            val clipboardManager =
                context.getSystemService(Context.CLIPBOARD_SERVICE) as ClipboardManager
            val clipData = clipboardManager.primaryClip
            if (clipData != null && clipData.itemCount>0) {
                val clipString = clipData.getItemAt(0).text
                if (clipString!=null)
                    editText!!.setText(clipString.toString())
            }
    }

    /** 清除內容 */
    private val resetListener = OnClickListener {_ ->
        editText!!.setText("")
        sampleTextView!!.text = getContextString(R.string.dialog_paint_color_sample)
        outputParam = ""
    }

    /** 切換設定 */
    private val changeModeListener = OnClickListener { _->
        if (isTransfer) {
            // 從轉檔切換到紀錄
            isTransfer = false
            findViewById<Button>(R.id.dialog_shorten_url_change_mode).text = getContextString(R.string.dialog_shorten_url_transfer)
            findViewById<ScrollView>(R.id.dialog_scrollView).visibility = GONE
            findViewById<ScrollView>(R.id.dialog_scrollView2).visibility = VISIBLE

            val recyclerView = findViewById<RecyclerView>(R.id.dialog_shorten_url_layout_recycleView)
            recyclerView.layoutManager = LinearLayoutManager(context)

            dialogShortenUrlItemViewAdapter = DialogShortenUrlItemViewAdapter(urlDatabase.shortenUrls)
            recyclerView.adapter = dialogShortenUrlItemViewAdapter
            dialogShortenUrlItemViewAdapter!!.setOnItemClickListener(this)
        } else {
            isTransfer = true
            findViewById<Button>(R.id.dialog_shorten_url_change_mode).text = getContextString(R.string.record)
            findViewById<ScrollView>(R.id.dialog_scrollView).visibility = VISIBLE
            findViewById<ScrollView>(R.id.dialog_scrollView2).visibility = GONE
        }
    }

    init {
        val layoutId = R.layout.dialog_shorten_url
        requestWindowFeature(1)
        setContentView(layoutId)
        Objects.requireNonNull(window)!!.setBackgroundDrawable(null)
        setTitle(context.getString(R.string.dialog_shorten_url_title))
        val layout = findViewById<LinearLayout>(R.id.dialog_shorten_url_layout)
        editText = layout.findViewById(R.id.dialog_shorten_url_content)
        sampleTextView = layout.findViewById(R.id.dialog_shorten_url_sample)
        catchClipBoard()

        // 按鈕
        layout.findViewById<Button>(R.id.dialog_shorten_url_transfer).setOnClickListener(transferListener)
        sendButton = layout.findViewById(R.id.send)
        sendButton!!.setOnClickListener(this)
        sendButton!!.isEnabled = false
        layout.findViewById<Button>(R.id.cancel).setOnClickListener(this)
        layout.findViewById<Button>(R.id.dialog_shorten_url_reset).setOnClickListener(resetListener)
        findViewById<Button>(R.id.dialog_shorten_url_change_mode).setOnClickListener(changeModeListener)
    }

    override fun onClick(view: View) {
        if (view === sendButton && shortenUrlListener != null) {
            shortenUrlListener!!.onShortenUrlDone(outputParam)
        }
        dismiss()
    }

    fun setListener(listener: DialogShortenUrlListener?) {
        shortenUrlListener = listener
    }

    override fun onDialogShortenUrlItemViewClicked(dialogShortenUrlItemView: DialogShortenUrlViewHolder?) {
        val shortUrl = dialogShortenUrlItemViewAdapter!!.getItem(dialogShortenUrlItemView!!.layoutPosition).shorten_url
        changeFrontend(shortUrl)
    }
}
