package com.kota.Bahamut.pages.bookmarkPage

import android.view.View

interface BookmarkClickListener {
    fun onItemClick(view: View?, position: Int)
    fun onEditClick(view: View?, position: Int)
    fun onDeleteClick(view: View?, position: Int)
}
