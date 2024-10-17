package com.kota.Bahamut.Service

import android.annotation.SuppressLint
import android.app.Activity
import android.content.Context
import com.kota.Bahamut.DataModels.BookmarkStore
import com.kota.Bahamut.PageContainer
import com.kota.Bahamut.Pages.Messages.MessageSmall

data class HeroStep (
    var authorNickname: String,
    var datetime: String,
    var content: String
)

/**
* 此處存放app執行階段使用的變數, 存放於記憶體, 關閉就消失
* */
@SuppressLint("StaticFieldLeak")
object TempSettings {
    @JvmField
    var isUnderAutoToChat = false // 正在自動登入ing
    @JvmField
    var isFloatingInvisible = false // 浮動工具列是否正處於隱藏狀態
    @JvmField
    var lastVisitBoard = "" // 最後離開的看板
    @JvmField
    var lastReceivedMessage = "" // 最後收到的訊息
    @JvmField
    var isSyncMessageMain = false // 是否已經同步訊息

    private var bookmarkStore: BookmarkStore? = null // 公用的bookmarkStore
    private var applicationContext: Context? = null // 公開的 applicationContext
    private var myContext: Context? = null
    private var myActivity: Activity? = null
    private var messageSmall: MessageSmall? = null // 聊天小視窗

    @JvmStatic
    fun initialCFContext(fromContext: Context) {
        myContext = fromContext
    }
    @JvmStatic
    fun getMyContext(): Context? {
        return myContext
    }

    @JvmStatic
    fun initialCFActivity(fromActivity: Activity) {
        myActivity = fromActivity
    }

    @JvmStatic
    fun getActivity(): Activity? {
        return myActivity
    }

    private var boardFollowTitle = "" // 正在看的討論串標題
    private var _transportType = -1 // 網路狀況
    private var imgurAccessToken: String = "" // 上傳 imgur 的 token
    private var imgurAlbum: String = "" // 上傳 imgur 的 album
    private var cloudSaveLastTime: String = "" // 雲端備份最後時間
    private var heroStepList: MutableList<HeroStep> = mutableListOf()
    private var notReadMessageCount: Int = 0 // 尚未讀取的訊息量

    // 清空數據, 不用清除的不要列進去
    @JvmStatic
    fun clearTempSettings() {
        isUnderAutoToChat = false
        isFloatingInvisible = false
        lastReceivedMessage = ""
        setBoardFollowTitle("")
        setCloudSaveLastTime("")
        heroStepList = mutableListOf()
    }

    @JvmStatic
    fun setBoardFollowTitle(title: String) {
        boardFollowTitle = title
    }

    @JvmStatic
    fun isBoardFollowTitle(readTitle: String): Boolean {
        return boardFollowTitle == readTitle
    }

    @JvmStatic
    fun setTransportType(fromTransportType: Int) {
        _transportType = fromTransportType
    }

    @JvmStatic
    fun getTransportType(): Int {
        return _transportType
    }

    @JvmStatic
    fun setBookmarkStore(fromBookmarkStore: BookmarkStore?) {
        bookmarkStore = fromBookmarkStore
    }

    @JvmStatic
    fun getBookmarkStore(): BookmarkStore {
        return bookmarkStore!!
    }
    @JvmStatic
    fun setApplicationContext(fromContext: Context) {
        applicationContext = fromContext
    }

    @JvmStatic
    fun getApplicationContext(): Context {
        return applicationContext!!
    }

    @JvmStatic
    fun getImgurToken(): String {
        return imgurAccessToken
    }
    @JvmStatic
    fun setImgurToken(token:String) {
        imgurAccessToken = token
    }
    @JvmStatic
    fun getImgurAlbum(): String {
        return imgurAlbum
    }
    @JvmStatic
    fun setImgurAlbum(album:String) {
        imgurAlbum = album
    }
    @JvmStatic
    fun getCloudSaveLastTime(): String {
        return cloudSaveLastTime
    }
    @JvmStatic
    fun setCloudSaveLastTime(lastTime:String) {
        cloudSaveLastTime = lastTime
    }

    @JvmStatic
    fun getHeroStepList(): List<HeroStep> {
        return heroStepList
    }

    @JvmStatic
    fun setHeroStep(heroStep: HeroStep) {
        heroStepList.add(heroStep)
    }

    @JvmStatic
    fun getNotReadMessageCount(): Int {
        return notReadMessageCount
    }

    @JvmStatic
    fun setNotReadMessageCount(aCount: Int) {
        notReadMessageCount = aCount

        messageSmall?.updateBadge(notReadMessageCount.toString())
    }

    /** 聊天小視窗 */
    @JvmStatic
    fun setMessageSmall(view: MessageSmall?) {
        messageSmall = view
    }
    @JvmStatic
    fun getMessageSmall(): MessageSmall? {
        return messageSmall
    }
}
