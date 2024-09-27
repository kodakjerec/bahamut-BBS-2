package com.kota.Bahamut.Service

import android.annotation.SuppressLint
import android.content.Context
import com.kota.Bahamut.DataModels.BookmarkStore
import com.kota.Bahamut.PageContainer

data class HeroStep (
    var authorNickname: String,
    var datetime: String,
    var content: String
)

/*
* 此處存放app執行階段使用的變數, 存放於記憶體, 關閉就消失
* */
object TempSettings {
    @JvmField
    var isUnderAutoToChat = false // 正在自動登入ing
    @JvmField
    var isFloatingInvisible = false // 浮動工具列是否正處於隱藏狀態
    private var boardFollowTitle = "" // 正在看的討論串標題
    private var _transportType = -1 // 網路狀況
    @JvmField
    var lastVisitBoard = "" // 最後離開的看板

    @SuppressLint("StaticFieldLeak")
    private var bookmarkStore: BookmarkStore? = null // 公用的bookmarkStore
    @SuppressLint("StaticFieldLeak")
    private var applicationContext: Context? = null // 公開的 applicationContext

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

        val page = PageContainer.getInstance().mainPage
        page.messageSmall.updateBadge(notReadMessageCount.toString())
    }
}
