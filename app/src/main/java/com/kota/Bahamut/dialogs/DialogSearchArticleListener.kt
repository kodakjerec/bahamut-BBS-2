package com.kota.Bahamut.dialogs

import java.util.Vector

interface DialogSearchArticleListener {
    fun onSearchDialogSearchButtonClickedWithValues(vector: Vector<String>)
    fun onSearchDialogCancelButtonClicked()
}
