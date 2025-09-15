package com.kota.ASFramework.Dialog

interface ASListDialogExtendedItemClickListener {
    fun onListDialogExtendedItemClicked(dialog: ASListDialog, index: Int)
    fun onListDialogExtendedItemLongClicked(dialog: ASListDialog, index: Int): Boolean
}
