package com.kota.Bahamut.command

import com.kota.Bahamut.listPage.TelnetListPage
import com.kota.Bahamut.listPage.TelnetListPageBlock
import com.kota.Bahamut.service.EditFromLinkedState
import com.kota.Bahamut.service.EditFromLinkedStep
import com.kota.Bahamut.service.TempSettings
import com.kota.telnet.TelnetArticle
import com.kota.telnet.TelnetClient
import com.kota.telnet.reference.TelnetKeyboard

/**
 * [BahamutCommandLocateArticle] - 從串接頁 (BoardLinkPage / BoardSearchPage) 定位目標文章的 Telnet 命令。
 *
 * 職責：
 * 當使用者在同主題串接頁點擊「編輯文章」時，因串接頁上的編號與主看板之真正版面文章編號 (boardNumber) 不同，
 * 此命令初始化 [EditFromLinkedState] 狀態機，並依據邊界特徵 (區塊邊界、頁面第一篇、最後一篇等) 發送初始 BBS 按鍵。
 *
 * @property targetArticle 要定位編輯的目標文章
 * @property isFirstInPage 當前文章是否位於串接頁該頁面的第一筆 (用於處理 20 的倍數或頁面第一筆之例外判定)
 */
class BahamutCommandLocateArticle(
    private val targetArticle: TelnetArticle? = null,
    private val isFirstInPage: Boolean = false
) : TelnetCommand() {

    init {
        this.action = BahamutCommandDef.LOCATE_ARTICLE
    }

    override fun execute(telnetListPage: TelnetListPage) {
        if (this.targetArticle != null) {
            // 建立狀態機，由 StateHandler 處理後續流程
            val state = EditFromLinkedState(targetArticle)
            state.isFirstInPage = isFirstInPage

            // 1. 例外流程 1: 區塊邊界 (20 的倍數)，先往上移，再送 "t"
            if (state.isBlockBoundary) {
                state.step = EditFromLinkedStep.MOVE_UP_FOR_BOUNDARY
                TelnetClient.myInstance!!.sendKeyboardInputToServer(TelnetKeyboard.UP_ARROW)
            }
            // 2. 例外流程 3: 全看板第一篇文章 (articleNumber == 1)
            else if (targetArticle.articleNumber == 1) {
                state.isFirst = true
                state.step = EditFromLinkedStep.LEAVING_LINKED_PAGE
                TelnetClient.myInstance!!.sendKeyboardInputToServer(TelnetKeyboard.LEFT_ARROW)
            }
            // 3. 例外流程 2: 頁面第一篇且文章編號大於 1，往上移兩筆再處理
            else if (state.isFirstInPage && targetArticle.articleNumber > 1) {
                state.step = EditFromLinkedStep.MOVE_UP_FOR_BOUNDARY
                TelnetClient.myInstance!!.sendKeyboardInputToServer(TelnetKeyboard.UP_ARROW)
                TelnetClient.myInstance!!.sendKeyboardInputToServer(TelnetKeyboard.UP_ARROW)
            }
            // 4. 例外流程 4: 全串接清單最後一篇文章
            else if (targetArticle.articleNumber == telnetListPage.getItemSize()) {
                state.isLast = true
                state.step = EditFromLinkedStep.LEAVING_LINKED_PAGE
                TelnetClient.myInstance!!.sendKeyboardInputToServer(TelnetKeyboard.LEFT_ARROW)
            }
            // 5. 正常流程: 直接傳送 "t" 鍵查詢版面文章編號 (boardNumber)
            else {
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
