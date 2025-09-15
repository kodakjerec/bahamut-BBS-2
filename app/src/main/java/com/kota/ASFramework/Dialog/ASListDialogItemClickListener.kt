package com.kota.ASFramework.Dialog

interface ASListDialogItemClickListener {
    fun onListDialogItemClicked(dialog: ASListDialog, index: Int, item: String)
    fun onListDialogItemLongClicked(dialog: ASListDialog, index: Int, item: String): Boolean
}
