package com.kota.Bahamut.pages.blockListPage

interface BlockListClickListener {
    fun onBlockListPage_ItemView_clicked(blockListPage_ItemView: BlockListViewHolder?)

    fun onBlockListPage_ItemView_delete_clicked(blockListPage_ItemView: BlockListViewHolder?)
}
