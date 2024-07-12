package com.kota.Bahamut.Dialogs

import java.util.Vector

interface DialogSearchArticleListener {
    fun onSearchDialogSearchButtonClickedWithValues(vector: Vector<String>)
    fun onSearchDialogCancelButtonClicked()
}
