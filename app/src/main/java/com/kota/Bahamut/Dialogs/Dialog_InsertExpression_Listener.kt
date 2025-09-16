package com.kota.Bahamut.Dialogs

interface Dialog_InsertExpression_Listener {
    fun onListDialogItemClicked(
        paramASListDialog: Dialog_InsertExpression?,
        paramInt: Int,
        paramString: String?
    )

    fun onListDialogSettingClicked()
}
