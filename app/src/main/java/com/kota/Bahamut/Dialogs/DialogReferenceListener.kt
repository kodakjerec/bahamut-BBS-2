package com.kota.Bahamut.Dialogs

import com.kota.Bahamut.DataModels.ReferenceAuthor

interface DialogReferenceListener {
    fun onSelectAuthor(authors: List<ReferenceAuthor>)
}
