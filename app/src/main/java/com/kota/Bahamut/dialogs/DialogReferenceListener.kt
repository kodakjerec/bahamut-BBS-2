package com.kota.Bahamut.dialogs

import com.kota.Bahamut.dataModels.ReferenceAuthor

interface DialogReferenceListener {
    fun onSelectAuthor(authors: List<ReferenceAuthor>)
}
