package com.kota.asFramework.dialog

/**
 * [ASListDialog] 選項列表項目點擊與長按事件的回呼介面
 */
interface ASListDialogItemClickListener {
    /**
     * 當點擊對話框中的選項項目時呼叫
     *
     * @param paramASListDialog 觸發事件的 [ASListDialog] 實例
     * @param index 被點擊項目的索引位置 (從 0 開始)
     * @param title 被點擊項目的標題文字
     */
    fun onListDialogItemClicked(
        paramASListDialog: ASListDialog?,
        index: Int,
        title: String?
    )

    /**
     * 當長按對話框中的選項項目時呼叫
     *
     * @param paramASListDialog 觸發事件的 [ASListDialog] 實例
     * @param index 被長按項目的索引位置 (從 0 開始)
     * @param title 被長按項目的標題文字
     * @return 傳回 true 表示已處理長按事件，對話框將會關閉
     */
    fun onListDialogItemLongClicked(
        paramASListDialog: ASListDialog?,
        index: Int,
        title: String?
    ): Boolean
}
