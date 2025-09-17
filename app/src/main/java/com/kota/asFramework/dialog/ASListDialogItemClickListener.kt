package com.kota.asFramework.dialog

interface ASListDialogItemClickListener {
    fun onListDialogItemClicked(
        paramASListDialog: ASListDialog?,
        paramInt: Int,
        paramString: String?
    )

    fun onListDialogItemLongClicked(
        paramASListDialog: ASListDialog?,
        paramInt: Int,
        paramString: String?
    ): Boolean
}


