package com.kota.asFramework.dialog

/**
 * [ASAlertDialog] 點擊對話框按鈕與關閉事件的回呼介面
 */
fun interface ASAlertDialogListener {
    /**
     * 當對話框關閉並回傳被點擊的按鈕索引時呼叫
     *
     * @param paramASAlertDialog 觸發事件的 [ASAlertDialog] 實牌
     * @param paramInt 被點擊按鈕的索引位置 (從 0 開始)
     */
    fun onAlertDialogDismissWithButtonIndex(paramASAlertDialog: ASAlertDialog, paramInt: Int)
}
