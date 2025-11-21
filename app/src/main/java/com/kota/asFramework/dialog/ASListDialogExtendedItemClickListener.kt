package com.kota.asFramework.dialog

interface ASListDialogExtendedItemClickListener {
    fun onListDialogExtendedItemClicked(paramASListDialog: ASListDialog?, paramInt: Int)

    fun onListDialogExtendedItemLongClicked(
        paramASListDialog: ASListDialog?,
        paramInt: Int
    ): Boolean
}

