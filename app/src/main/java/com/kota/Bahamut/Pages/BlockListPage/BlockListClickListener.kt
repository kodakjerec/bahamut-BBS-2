package com.kota.Bahamut.Pages.BlockListPage

interface BlockListClickListener {
    fun onBlockListPage_ItemView_clicked(blockListPage_ItemView: BlockListViewHolder?)

    fun onBlockListPage_ItemView_delete_clicked(blockListPage_ItemView: BlockListViewHolder?)
}
