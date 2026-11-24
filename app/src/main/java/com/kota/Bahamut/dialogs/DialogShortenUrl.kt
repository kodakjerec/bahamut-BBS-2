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
import androidx.core.net.toUri
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.kota.Bahamut.R
import com.kota.Bahamut.dataModels.UrlDatabase
import com.kota.Bahamut.service.CommonFunctions.getContextString
import com.kota.Bahamut.service.UserSettings
import com.kota.asFramework.dialog.ASDialog
import com.kota.asFramework.dialog.ASProcessingDialog
import com.kota.asFramework.thread.ASCoroutine
import com.kota.asFramework.ui.ASToast
import fe.clearurlskt.ClearUrlSettings
import fe.clearurlskt.ClearUrls
import fe.clearurlskt.Resource
import fe.clearurlskt.config.loader.BundledClearURLConfigLoader
import fe.std.result.isSuccess
import okhttp3.MultipartBody
import okhttp3.OkHttpClient
import okhttp3.Request
import okhttp3.RequestBody
import org.json.JSONObject

class DialogShortenUrl : ASDialog(), OnClickListener,DialogShortenUrlItemViewListener {
    /** 對話框根部主要容器 Layout */
    private var mainLayout: RelativeLayout
    /** 供使用者輸入或貼上原始網址的文字編輯框 */
    private var editText: EditText
    /** 顯示轉換後的短網址預覽文字框 */
    private var sampleTextView: TextView
    /** 送出按鈕，將最終網址回傳至發文頁面 */
    private var sendButton: Button? = null
    /** 儲存目前已轉換完成的短網址字串（若未縮址則為空字串） */
    private var outputParam: String = ""
    /** 縮網址完成後的回呼監聽器 */
    private lateinit var shortenUrlListener: DialogShortenUrlListener
    /** 歷史縮網址紀錄列表的 RecyclerView Adapter */
    private lateinit var dialogShortenUrlItemViewAdapter: DialogShortenUrlItemViewAdapter
    /** 本地網址資料庫操作實例，用於讀寫歷史縮址紀錄 */
    private val urlDatabase = UrlDatabase(context)
    /** 目前介面模式旗標：true 代表「縮址轉檔模式」，false 代表「歷史紀錄列表模式」 */
    private var isTransfer: Boolean = true
    /** 螢幕旋轉方向監聽器，用於動態調整對話框高度 */
    private var mOrientationEventListener: OrientationEventListener

    /**
     * 去識別化規則引擎 (ClearURLs)：
     * 延遲載入套件內建的 clearurls.json 規則集，若載入成功則取出所有網站的規則清單，
     * 失敗則退回空清單，避免初始化例外。
     */
    private val clearUrls by lazy {
        val result = BundledClearURLConfigLoader(Resource::class.java).load()
        val providers = if (result.isSuccess()) result.value.orEmpty() else emptyList()
        ClearUrls(providers)
    }

    /**
     * 追蹤轉址專用 OkHttpClient：
     * 刻意將 followRedirects 與 followSslRedirects 設為 false，
     * 由程式手動逐層解析 HTTP 3xx 的 Location 標頭，以便在跳入 Facebook 登入牆前及時攔截真實貼文網址。
     */
    private val redirectClient by lazy {
        OkHttpClient.Builder()
            .followRedirects(false)
            .followSslRedirects(false)
            .build()
    }

    /** 快取上一次進行還原的原始網址，避免重複對同一網址發送網路請求 */
    private var cachedOriginUrl: String = ""
    /** 快取上一次成功還原後的目標真實網址 */
    private var cachedRestoredUrl: String = ""

    /** 對話框識別名稱 */
    override val name: String?
        get() = "BahamutShortenUrlDialog"

    /**
     * 點擊「縮址」按鈕時的轉檔監聽器：
     * 1. 檢查非 VIP 使用者的免費縮址次數上限（上限 30 次）。
     * 2. 使用 Linkify 從輸入框文字中擷取第一筆網址。
     * 3. 先查詢本地 SQLite 資料庫 (`urlDatabase`) 是否已有相同網址的歷史縮址紀錄；若有則直接取用，省去 API 請求。
     * 4. 若無歷史紀錄，則向 Cloudflare Worker (`worker-short-url`) 發送 POST 請求產生短網址並寫入本地資料庫。
     */
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
        val apiUrl = "https://worker-short-url.kodakjerec.work/"
        val client = OkHttpClient()
        val body: RequestBody = MultipartBody.Builder().setType(MultipartBody.FORM)
            .addFormDataPart("type", "shortenurl")
            .addFormDataPart("url", targetUrl)
            .build()
        val request: Request = Request.Builder()
            .url(apiUrl)
            .post(body)
            .build()
        ASCoroutine.runInNewCoroutine {
            try{
                client.newCall(request).execute().use { response->
                    val data = response.body.string()
                    val jsonObject = JSONObject(data)
                    val status = jsonObject.optString("res")
                    if (status.isNotEmpty()) {
                        val shortUrl = jsonObject.getString("short_url")
                        val title = jsonObject.getString("title")
                        val description = jsonObject.getString("description")

                        ASCoroutine.ensureMainThread {
                            editText.setText(targetUrl)
                            changeFrontend(shortUrl)
                            UserSettings.propertiesNoVipShortenTimes = ++shortenTimes
                        }

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
    /**
     * 變更畫面上預覽區的短網址內容，並啟用「送出」按鈕。
     * @param shortUrl 轉換後的短網址字串
     */
    fun changeFrontend(shortUrl: String) {
        sampleTextView.text = shortUrl
        outputParam = sampleTextView.text.toString()
        sendButton?.isEnabled = true
    }

    /** 儲存從系統剪貼簿原始擷取到的文字內容（作為還原與去識別化切換時的基準字串） */
    var fromClipData = ""

    /**
     * 從系統剪貼簿擷取第一筆文字內容並填入輸入框，
     * 隨後依據使用者目前的勾選狀態自動執行「還原網址」與「去識別化」。
     */
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

    /** 點擊「去識別化」核取方塊時的監聽器：更新偏好設定並重新處理網址 */
    private val changeNonIdListenerForCheckbox = OnClickListener { _ ->
        val checkbox = mainLayout.findViewById<CheckBox>(R.id.dialog_shorten_url_non_id_checkbox)
        UserSettings.setPropertiesShortUrlNonId(checkbox.isChecked)
        urlRemoveId()
    }
    /** 點擊「去識別化」文字標籤時的監聽器：反向切換核取方塊狀態、更新偏好設定並重新處理網址 */
    private val changeNonIdListenerForLabel = OnClickListener { _ ->
        val checkbox = mainLayout.findViewById<CheckBox>(R.id.dialog_shorten_url_non_id_checkbox)
        checkbox.isChecked = !checkbox.isChecked
        UserSettings.setPropertiesShortUrlNonId(checkbox.isChecked)
        urlRemoveId()
    }

    /** 點擊「還原網址」核取方塊時的監聽器：更新偏好設定並重新處理網址 */
    private val changeRestoreListenerForCheckbox = OnClickListener { _ ->
        val checkbox = mainLayout.findViewById<CheckBox>(R.id.dialog_shorten_url_restore_checkbox)
        UserSettings.setPropertiesShortUrlRestore(checkbox.isChecked)
        urlRemoveId()
    }
    /** 點擊「還原網址」文字標籤時的監聽器：反向切換核取方塊狀態、更新偏好設定並重新處理網址 */
    private val changeRestoreListenerForLabel = OnClickListener { _ ->
        val checkbox = mainLayout.findViewById<CheckBox>(R.id.dialog_shorten_url_restore_checkbox)
        checkbox.isChecked = !checkbox.isChecked
        UserSettings.setPropertiesShortUrlRestore(checkbox.isChecked)
        urlRemoveId()
    }

    /** 點擊「清除」按鈕時的監聽器：清空輸入框、剪貼簿暫存、轉址快取與預覽結果 */
    private val resetListener = OnClickListener {_ ->
        editText.setText("")
        fromClipData = ""
        cachedOriginUrl = ""
        cachedRestoredUrl = ""
        sampleTextView.text = getContextString(R.string.dialog_paint_color_sample_ch)
        outputParam = ""
    }

    /** 點擊右上角「紀錄 / 縮址」模式切換按鈕時的監聽器 */
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

    /** 紀錄上一次的螢幕方向（預設 1 為直向 Configuration.ORIENTATION_PORTRAIT），避免相同方向重複重設 Layout */
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

        val restoreCheckbox = mainLayout.findViewById<CheckBox>(R.id.dialog_shorten_url_restore_checkbox)
        restoreCheckbox.isChecked = UserSettings.shortUrlRestore
        restoreCheckbox.setOnClickListener(changeRestoreListenerForCheckbox)
        mainLayout.findViewById<TextView>(R.id.dialog_shorten_url_restore_label).setOnClickListener(changeRestoreListenerForLabel)

        // 監聽轉動事件, 變更視窗大小
        mOrientationEventListener = object : OrientationEventListener(context) {
            override fun onOrientationChanged(orientation: Int) {
                val nowOrientation: Int = context.resources.configuration.orientation

                if (nowOrientation!=oldOrientation) {
                    val layoutParams : ViewGroup.LayoutParams? = mainLayout.layoutParams
                    if (nowOrientation == Configuration.ORIENTATION_LANDSCAPE) {
                        // 橫向模式時高度設為 WRAP_CONTENT，避免超出螢幕高度
                        layoutParams?.height = ViewGroup.LayoutParams.WRAP_CONTENT
                        oldOrientation = nowOrientation
                    } else {
                        // 直向模式時將 500dp 乘上螢幕像素密度 (density) 換算為實際 px 高度
                        val factor = context.resources.displayMetrics.density
                        layoutParams?.height = (500 * factor).toInt()
                        oldOrientation = nowOrientation
                    }
                    mainLayout.layoutParams = layoutParams
                }
            }
        }
        setDialogWidth()
    }

    /**
     * 處理底部「送出」與「取消」按鈕點擊事件：
     * 若點擊「送出」，優先回傳已縮址的短網址 (outputParam)；若尚未縮址則直接回傳輸入框內的網址。
     */
    override fun onClick(view: View) {
        if (view === sendButton) {
            if (outputParam.isEmpty())
                shortenUrlListener.onShortenUrlDone(editText.text.toString())
            else
                shortenUrlListener.onShortenUrlDone(outputParam)
        }
        dismiss()
    }

    /** 設定縮網址完成後的回呼監聽器 */
    fun setListener(listener: DialogShortenUrlListener) {
        shortenUrlListener = listener
    }

    /** 點擊歷史紀錄列表項目時，將該筆短網址填入預覽區 */
    override fun onDialogShortenUrlItemViewClicked(dialogShortenUrlItemView: DialogShortenUrlViewHolder) {
        val shortUrl = dialogShortenUrlItemViewAdapter.getItem(dialogShortenUrlItemView.layoutPosition).shortenUrl
        changeFrontend(shortUrl!!)
    }

    /** 對話框顯示時啟動螢幕旋轉監聽 */
    override fun onStart() {
        super.onStart()
        mOrientationEventListener.enable()
    }

    /** 對話框關閉時停用螢幕旋轉監聽以節省資源 */
    override fun onStop() {
        super.onStop()
        mOrientationEventListener.disable()
    }

    /**
     * 清除網址中的追蹤與識別參數（去識別化）：
     * 1. 先透過 ClearURLs 規則庫移除各平台常見的追蹤參數（如 utm_*、mibextid、__cft__ 等）。
     * 2. 針對在「還原網址」主機清單內且轉址後額外附加 `rdid` 與 `share_url` 參數的網址（ClearURLs 舊規則未涵蓋），
     *    拆解 Uri 的 Query 參數逐一過濾並重新組裝，同時保留 `story_fbid`、`id`、`fbid` 等必要貼文參數。
     *
     * @param url 待清理的網址字串
     * @return 移除追蹤參數後的乾淨網址
     */
    private fun cleanUrlParams(url: String): String {
        // 步驟 1：使用 ClearURLs 規則進行初步清理
        val (cleanedUrl, _) = clearUrls.clearUrl(url, ClearUrlSettings(skipUnchanged = true))
        return try {
            val uri = cleanedUrl.toUri()
            // 步驟 2：若屬於還原網址主機清單網域且含有 rdid 或 share_url 追蹤參數，重建 Query 部分並剔除這兩個參數
            if (isMatchedRestoreHost(cleanedUrl) &&
                (uri.getQueryParameter("rdid") != null || uri.getQueryParameter("share_url") != null)
            ) {
                val builder = uri.buildUpon().clearQuery()
                for (name in uri.queryParameterNames) {
                    if (name != "rdid" && name != "share_url") {
                        // 同名參數可能有多個值，逐一加回 Builder
                        for (value in uri.getQueryParameters(name)) {
                            builder.appendQueryParameter(name, value)
                        }
                    }
                }
                builder.build().toString()
            } else {
                cleanedUrl
            }
        } catch (_: Exception) {
            cleanedUrl
        }
    }

    /**
     * 檢查網址的網域 (Host) 是否符合 SharedPreferences 中儲存的「還原網址」主機清單 (`UserSettings.restoreUrlsList`)。
     *
     * 比對邏輯說明：
     * - 從 `UserSettings.restoreUrlsList` 讀取雲端同步下來的主機清單（例如 `facebook.com`、`fb.watch`、`fb.me`、`tw.shp.ee`）。
     * - 解析目標網址的 `host` 並轉為小寫，逐一比對是否與清單中的網域完全相同（`host == cleanDomain`），
     *   或是屬於該網域的子網域（`host.endsWith(".$cleanDomain")`，例如 `www.facebook.com` 或 `m.facebook.com`）。
     *
     * @param url 待檢查的網址字串
     * @return 若網址主機在還原網址主機清單內則回傳 true
     */
    private fun isMatchedRestoreHost(url: String): Boolean {
        return try {
            val uri = url.toUri()
            val host = uri.host?.lowercase() ?: return false
            UserSettings.restoreUrlsList.any { domain ->
                val cleanDomain = domain.trim().lowercase()
                cleanDomain.isNotEmpty() && (host == cleanDomain || host.endsWith(".$cleanDomain"))
            }
        } catch (_: Exception) {
            false
        }
    }

    /**
     * 判斷網址是否仍處於「尚未還原完成」的短網址或分享網址狀態：
     * - 首先必須符合 `isMatchedRestoreHost(url)`（網域位於 `UserSettings.restoreUrlsList` 清單內）。
     * - 若網域本身是主站網域（含有 `/share/` 路徑的分享連結，或是清單內的其他獨立短網址網域但尚未跳出短網址），
     *   當路徑為 `/share/` 開頭、或整個網域本身就是短網址專用網域（路徑非一般長貼文路徑）時回傳 true。
     *   一旦含有 `/share/` 的分享網址成功轉址為一般貼文路徑（不再以 `/share/` 開頭），即視為已脫離短網址狀態。
     *
     * @param url 待檢查的網址字串
     * @param initialWasSharePath 原始網址是否為 `/share/` 結構的分享網址
     * @return 若仍為待還原的短網址狀態則回傳 true
     */
    private fun isPendingShortUrl(url: String, initialWasSharePath: Boolean = false): Boolean {
        if (!isMatchedRestoreHost(url)) return false
        return try {
            val uri = url.toUri()
            val path = uri.path ?: ""
            if (initialWasSharePath) {
                path.startsWith("/share/")
            } else {
                true
            }
        } catch (_: Exception) {
            false
        }
    }

    /**
     * 當伺服器未透過 HTTP 3xx 轉址、而是直接回傳 HTTP 200 HTML 頁面時，
     * 使用正則表達式從 HTML 內容中提取 `<meta property="og:url">` 或 `<link rel="canonical">` 的真實網址。
     *
     * 計算與解析說明：
     * - HTML 標籤屬性順序不固定（例如 `property="og:url" content="..."` 或 `content="..." property="og:url"`），
     *   因此正則表達式使用 `|` 同時匹配兩種屬性排列順序，並從對應的捕獲群組 (group 1 或 group 2) 取出網址。
     * - 取出的網址可能含有 HTML 實體編碼（例如 `&amp;`），需透過 `Html.fromHtml` 解碼還原為一般 `&` 符號。
     *
     * @param html 網頁原始 HTML 字串
     * @param initialWasSharePath 原始網址是否為 `/share/` 路徑結構
     * @return 解析出的真實貼文網址；若未找到或仍是未還原短網址則回傳 null
     */
    private fun extractCanonicalOrOgUrl(html: String, initialWasSharePath: Boolean): String? {
        // 優先比對 OpenGraph 的 og:url 標籤
        val ogMatch = Regex(
            """<meta[^>]+property=["']og:url["'][^>]+content=["']([^"']+)["']|<meta[^>]+content=["']([^"']+)["'][^>]+property=["']og:url["']""",
            RegexOption.IGNORE_CASE
        ).find(html)
        val ogUrl = ogMatch?.groupValues?.getOrNull(1)?.ifEmpty { ogMatch.groupValues.getOrNull(2) ?: "" }
        if (!ogUrl.isNullOrEmpty() && !isPendingShortUrl(ogUrl, initialWasSharePath)) {
            return android.text.Html.fromHtml(ogUrl, android.text.Html.FROM_HTML_MODE_LEGACY).toString()
        }

        // 次要備援：比對標準 canonical 連結標籤
        val canonicalMatch = Regex(
            """<link[^>]+rel=["']canonical["'][^>]+href=["']([^"']+)["']|<link[^>]+href=["']([^"']+)["'][^>]+rel=["']canonical["']""",
            RegexOption.IGNORE_CASE
        ).find(html)
        val canonicalUrl = canonicalMatch?.groupValues?.getOrNull(1)?.ifEmpty { canonicalMatch.groupValues.getOrNull(2) ?: "" }
        if (!canonicalUrl.isNullOrEmpty() && !isPendingShortUrl(canonicalUrl, initialWasSharePath)) {
            return android.text.Html.fromHtml(canonicalUrl, android.text.Html.FROM_HTML_MODE_LEGACY).toString()
        }
        return null
    }

    /**
     * 透過逐層追蹤 HTTP 轉址，將符合 `UserSettings.restoreUrlsList` 清單的短網址或分享網址還原為真實目標網址。
     *
     * 關鍵處理流程說明：
     * 1. 清單檢查與快取檢查：若目標網址不在 `UserSettings.restoreUrlsList` 主機清單內，直接回傳原網址；
     *    若與上次成功還原的 `cachedOriginUrl` 相同，直接回傳快取結果，省去網路請求。
     * 2. User-Agent 策略：針對清單內的網域（如 Facebook 等）使用 `PostmanRuntime/2.10.1`，
     *    避免桌面版 Chrome User-Agent 因缺少 `Sec-Fetch-*` 標頭被伺服器拒絕 (`HTTP 400 Bad Request`)。
     * 3. 逐層轉址追蹤（最多 5 層，防止無限迴圈）：
     *    - 若回傳 HTTP 300..399，讀取 `Location` 標頭並透過 `request.url.resolve(location)` 將相對路徑轉為完整絕對網址。
     *    - 若回傳 HTTP 200 且仍是未還原的短網址，則改由 HTML 內文解析 `og:url` 或 `canonical`。
     * 4. 登入牆攔截：若伺服器在跳轉過程導向 `/login/?next=...`，
     *    直接從 `next` 查詢參數取出已解碼的真實網址並立即停止追蹤。
     * 5. 提前終止條件：一旦轉址後的網址已脫離待還原狀態（例如已離開短網址網域，或 `/share/` 已展開為真實貼文路徑），
     *    就立刻 `break` 停止，避免再對真實貼文發送第二次請求而被導向登入頁面。
     *
     * @param targetUrl 欲還原的原始短網址或分享網址
     * @return 還原後的真實網址（若未在清單內、未發生轉址或發生錯誤則回傳原網址）
     */
    private fun resolveRedirectUrl(targetUrl: String): String {
        // 0. 若網址不在還原網址主機清單內，不需發送轉址請求
        if (!isMatchedRestoreHost(targetUrl)) {
            return targetUrl
        }
        // 1. 檢查快取是否命中
        if (targetUrl == cachedOriginUrl && cachedRestoredUrl.isNotEmpty() && cachedRestoredUrl != targetUrl) {
            return cachedRestoredUrl
        }
        /** 目前追蹤層級的網址，隨每次轉址更新 */
        var currentUrl = targetUrl
        /** 標記原始網址是否為 /share/ 路徑結構（用於判斷同網域下是否已脫離分享短網址） */
        val initialWasSharePath = try {
            (targetUrl.toUri().path ?: "").startsWith("/share/")
        } catch (_: Exception) {
            false
        }
        try {
            // 最多追蹤 5 層轉址
            for (hop in 0 until 5) {
                val isInRestoreList = isMatchedRestoreHost(currentUrl)
                val requestBuilder = Request.Builder()
                    .url(currentUrl)
                    .get()
                    .header(
                        "User-Agent",
                        if (isInRestoreList) {
                            "PostmanRuntime/2.10.1"
                        } else {
                            "Mozilla/5.0 (Windows NT 10.0; Win64; x64) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/125.0.0.0 Safari/537.36"
                        }
                    )
                    .header("Accept", "text/html,application/xhtml+xml,application/xml;q=0.9,*/*;q=0.8")

                val request = requestBuilder.build()

                /** 保存從 HTTP 200 HTML meta 中備援解析出的真實網址 */
                var resolvedFromHtml: String? = null
                /** 保存從 HTTP 3xx Location 標頭中解析出的下一層轉址網址 */
                val nextUrl = redirectClient.newCall(request).execute().use { response ->
                    if (response.code in 300..399) {
                        val location = response.header("Location")
                        if (!location.isNullOrEmpty()) {
                            // 使用當前請求網址作為基底，將可能為相對路徑的 Location 解析為完整網址
                            response.request.url.resolve(location)?.toString()
                        } else {
                            null
                        }
                    } else if (response.code == 200 && isPendingShortUrl(currentUrl, initialWasSharePath)) {
                        // 備援：若未發生 3xx 轉址而是回傳 200 HTML，嘗試從 og:url / canonical 提取
                        val bodyStr = response.body.string()
                        resolvedFromHtml = extractCanonicalOrOgUrl(bodyStr, initialWasSharePath)
                        null
                    } else {
                        null
                    }
                }

                // 若從 HTML meta 成功提取出真實網址，直接採用並結束迴圈
                if (!resolvedFromHtml.isNullOrEmpty()) {
                    currentUrl = resolvedFromHtml
                    break
                }

                // 若無下一層轉址網址，代表已抵達最終網址
                if (nextUrl == null) break

                // 檢查下一層是否被導向登入頁面 (例如 /login/ 或 /login.php?next=...)
                val nextUri = nextUrl.toUri()
                val nextPath = nextUri.path ?: ""
                if (nextPath.startsWith("/login")) {
                    val nextParam = nextUri.getQueryParameter("next")
                    if (!nextParam.isNullOrEmpty() && !isPendingShortUrl(nextParam, initialWasSharePath)) {
                        currentUrl = nextParam
                    }
                    break
                }

                currentUrl = nextUrl

                // 一旦成功脫離待還原的短網址狀態（例如跳出短網址網域或跳出 /share/ 路徑），即停止追蹤以避免跳入登入牆
                if (!isPendingShortUrl(currentUrl, initialWasSharePath)) {
                    break
                }
            }
        } catch (e: Exception) {
            Log.e(javaClass.simpleName, "resolveRedirectUrl failed: ${e.message}")
        }
        // 僅在確實成功還原出不同網址時寫入快取
        if (currentUrl != targetUrl) {
            cachedOriginUrl = targetUrl
            cachedRestoredUrl = currentUrl
        }
        return currentUrl
    }

    /**
     * 依據「還原網址」與「去識別化」兩個核取方塊的勾選狀態，對輸入框中的網址進行處理：
     * - 若兩者皆未勾選：還原為最原始的剪貼簿/輸入字串 (`sourceText`)。
     * - 若勾選「還原網址」且網址主機在 `UserSettings.restoreUrlsList` 清單內：
     *   在背景協程中發送網路請求追蹤轉址，若同時勾選「去識別化」則接著清理追蹤參數，最後切回主執行緒更新輸入框。
     * - 若僅勾選「去識別化」（或網址不在還原清單內）：直接在主執行緒以離線規則清理網址追蹤參數並更新輸入框。
     */
    private fun urlRemoveId() {
        val currentText = editText.text.toString()
        // 若剪貼簿基準字串為空（例如按下清除後手動貼上新網址），以目前輸入框內容作為基準
        if (fromClipData.isEmpty() && currentText.isNotEmpty()) {
            fromClipData = currentText
        }
        val sourceText = fromClipData.ifEmpty { currentText }
        if (sourceText.isEmpty()) return

        val enableRestore = UserSettings.shortUrlRestore
        val enableNonId = UserSettings.shortUrlNonId

        // 兩個選項都未勾選時，直接恢復原始網址文字
        if (!enableRestore && !enableNonId) {
            editText.setText(sourceText)
            return
        }

        // 利用 Linkify 自動辨識文字中的所有網址Span
        val textView = TextView(context)
        textView.text = sourceText
        Linkify.addLinks(textView, Linkify.WEB_URLS)
        val urls: Array<URLSpan> = textView.urls
        if (urls.isEmpty()) {
            editText.setText(sourceText)
            return
        }

        /** 擷取出的第一筆目標網址 */
        val firstUrl = urls[0].url

        if (enableRestore && isMatchedRestoreHost(firstUrl)) {
            // 網路請求不可在主執行緒執行，啟動背景協程進行轉址還原
            ASCoroutine.runInNewCoroutine {
                var processedUrl = resolveRedirectUrl(firstUrl)
                if (enableNonId) {
                    processedUrl = cleanUrlParams(processedUrl)
                }
                ASCoroutine.ensureMainThread {
                    editText.setText(processedUrl)
                }
            }
        } else if (enableNonId) {
            // 僅執行離線去識別化參數清理
            val processedUrl = cleanUrlParams(firstUrl)
            editText.setText(processedUrl)
        } else {
            editText.setText(sourceText)
        }
    }
}
