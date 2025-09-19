package com.kota.Bahamut.dialogs

import com.kota.Bahamut.dataModels.ReferenceAuthor

fun interface DialogReferenceListener {
    fun onSelectAuthor(authors: MutableList<ReferenceAuthor>)
}
