package com.kota.Bahamut.service

import android.annotation.SuppressLint
import android.app.Activity
import android.content.Context
import com.bumptech.glide.Glide
import com.kota.Bahamut.dataModels.BookmarkStore
import com.kota.Bahamut.pages.messages.MessageSmall
import com.kota.asFramework.thread.ASCoroutine
import com.kota.telnet.TelnetArticle

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
    var lastVisitArticleNumber = 0 // 最後離開的文章編號
    @JvmField
    var lastReceivedMessage = "" // 最後收到的訊息
    @JvmField
    var isSyncMessageMain = false // 是否已經同步訊息
    @JvmField
    var boardFollowTitle = "" // 正在看的討論串標題
    @JvmField
    var cloudSaveLastTime: Long = 0L // 雲端備份最後時間
    @JvmField
    var webAutoLoginSuccessTime: Long = 0L // web自動簽到成功時間
    @JvmField
    var transportType = -1 // 網路狀況
    @JvmField
    var bookmarkStore: BookmarkStore? = null // 公用的 bookmarkStore
    @JvmField
    var applicationContext: Context? = null // 公開的 applicationContext
    @JvmField
    var myContext: Context? = null
    @JvmField
    var myActivity: Activity? = null
    @JvmField
    var editFromLinkedState: EditFromLinkedState? = null // 從串接頁編輯文章的狀態

    // 比較少用到的變數, 不影響效能
    private var messageSmall: MessageSmall? = null // 聊天小視窗
    private var heroStepList: MutableList<HeroStep> = mutableListOf()
    private var notReadMessageCount: Int = 0 // 尚未讀取的訊息量

    // 清空數據, 不用清除的不要列進去
    @JvmStatic
    fun clearTempSettings() {
        isUnderAutoToChat = false
        isFloatingInvisible = false
        isSyncMessageMain = false
        lastReceivedMessage = ""
        boardFollowTitle = ""
        cloudSaveLastTime = 0L
        heroStepList = mutableListOf()
        editFromLinkedState = null
        // 清除Glide
        Glide.get(applicationContext!!).clearMemory()
        ASCoroutine.runInNewCoroutine {
            Glide.get(applicationContext!!).clearDiskCache()
        }
    }

    @JvmStatic
    fun isBoardFollowTitle(readTitle: String): Boolean {
        return boardFollowTitle == readTitle
    }

    @JvmStatic
    fun getHeroStepList(): MutableList<HeroStep> {
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

    /** web自動簽到成功時間 */
    @JvmStatic
    fun setWebAutoLoginSuccessTime(time: Long) {
        webAutoLoginSuccessTime = time
    }

    @JvmStatic
    fun getWebAutoLoginSuccessTime(): Long {
        return webAutoLoginSuccessTime
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
