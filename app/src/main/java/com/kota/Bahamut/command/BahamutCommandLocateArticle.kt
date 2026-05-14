package com.kota.Bahamut.command

import com.kota.Bahamut.BahamutStateHandler
import com.kota.Bahamut.listPage.TelnetListPage
import com.kota.Bahamut.listPage.TelnetListPageBlock
import com.kota.Bahamut.service.EditFromLinkedState
import com.kota.Bahamut.service.EditFromLinkedStep
import com.kota.Bahamut.service.TempSettings
import com.kota.telnet.TelnetArticle
import com.kota.telnet.TelnetClient
import com.kota.telnet.reference.TelnetKeyboard

class BahamutCommandLocateArticle(
    private val targetArticle: TelnetArticle? = null,
    private val isFirstInPage: Boolean = false
) : TelnetCommand() {

    init {
        this.action = BahamutCommandDef.Companion.LOCATE_ARTICLE
    }

    override fun execute(telnetListPage: TelnetListPage) {
        if (this.targetArticle != null) {
            // 建立狀態機，讓 StateHandler 處理後續流程
            val state = EditFromLinkedState(targetArticle)
            state.isFirstInPage = isFirstInPage
            
            // 判斷是否為區塊邊界（20的倍數）
            if (state.isBlockBoundary) {
                // 例外流程1: 先往上移，再送 "t"
                state.step = EditFromLinkedStep.MOVE_UP_FOR_BOUNDARY
                TelnetClient.myInstance!!.sendKeyboardInputToServer(TelnetKeyboard.UP_ARROW)
            } else if (targetArticle.articleNumber == 1) {
                // 例外流程3: 第一篇, 完整的第一篇. 必須比例外流程2更前面判斷
                state.isFirst = true
                state.step = EditFromLinkedStep.LEAVING_LINKED_PAGE
                TelnetClient.myInstance!!.sendKeyboardInputToServer(TelnetKeyboard.LEFT_ARROW)
            } else if (state.isFirstInPage && targetArticle.articleNumber > 1) {
                // 例外流程2: 剛好是第一篇, 送兩次 "t"
                state.step = EditFromLinkedStep.MOVE_UP_FOR_BOUNDARY
                TelnetClient.myInstance!!.sendKeyboardInputToServer(TelnetKeyboard.UP_ARROW)
                TelnetClient.myInstance!!.sendKeyboardInputToServer(TelnetKeyboard.UP_ARROW)
            }  else if (targetArticle.articleNumber == telnetListPage.getItemSize()) {
                state.isLast = true
                state.step = EditFromLinkedStep.LEAVING_LINKED_PAGE
                TelnetClient.myInstance!!.sendKeyboardInputToServer(TelnetKeyboard.LEFT_ARROW)
            } else {
                // 正常流程: 直接送 "t"
                state.step = EditFromLinkedStep.SENT_T
                TelnetClient.myInstance!!.sendKeyboardInputToServer(TelnetKeyboard.SMALL_T)
            }
            
            TempSettings.editFromLinkedState = state
        }
    }

    override fun executeFinished(telnetListPage: TelnetListPage, telnetListPageBlock: TelnetListPageBlock?) {
        isDone = true
    }

    override fun toString(): String {
        return "[LocateArticle][author=${targetArticle?.author}, title=${targetArticle?.title}, datetime=${targetArticle?.dateTime}]"
    }
}
