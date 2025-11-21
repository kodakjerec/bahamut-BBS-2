package com.kota.Bahamut.dialogs

interface DialogInsertExpressionListener {
    fun onListDialogItemClicked(
        paramASListDialog: DialogInsertExpression,
        paramInt: Int,
        paramString: String
    )

    fun onListDialogSettingClicked()
}
