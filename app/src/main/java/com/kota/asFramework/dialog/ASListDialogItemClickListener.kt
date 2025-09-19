package com.kota.asFramework.dialog

interface ASListDialogItemClickListener {
    fun onListDialogItemClicked(
        paramASListDialog: ASListDialog?,
        index: Int,
        title: String?
    )

    fun onListDialogItemLongClicked(
        paramASListDialog: ASListDialog?,
        index: Int,
        title: String?
    ): Boolean
}


