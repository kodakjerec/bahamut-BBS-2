package com.kota.Bahamut.command

import com.kota.Bahamut.listPage.TelnetListPage
import com.kota.Bahamut.listPage.TelnetListPageBlock

/**
 * [TelnetCommand] - Telnet 命令抽象基類。
 *
 * 職責：
 * 1. 封裝傳送至 BBS 伺服器的非同步操作指令。
 * 2. 透過 [execute] 發送 BBS 控制碼與鍵盤指令，並於 [executeFinished] 處理伺服器回應封包。
 */
abstract class TelnetCommand : BahamutCommandDef {
    /** 指令動作類型 (對應 [BahamutCommandDef] 常數) */
    @JvmField
    var action: Int = BahamutCommandDef.LOAD_BLOCK

    /** 指令是否執行完成 */
    var isDone: Boolean = false

    /** 是否記錄發送時間 */
    @JvmField
    var recordTime: Boolean = true

    /**
     * 執行命令邏輯，向 BBS 發送 Telnet 鍵盤/字串指令
     *
     * @param telnetListPage 當前發起指令的列表頁面
     */
    abstract fun execute(telnetListPage: TelnetListPage)

    /**
     * 命令執行完成回呼，解析 BBS 回傳區塊資料
     *
     * @param telnetListPage 當前列表頁面
     * @param telnetListPageBlock BBS 回傳解析之區塊資料
     */
    abstract fun executeFinished(
        telnetListPage: TelnetListPage,
        telnetListPageBlock: TelnetListPageBlock?
    )

    /** 是否為操作型指令 (預設為 true) */
    open val isOperationCommand: Boolean
        get() = true
}
