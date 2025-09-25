package com.kota.Bahamut.pages.blockListPage

interface BlockListClickListener {
    fun onBlockListPageItemViewClicked(blockListPageItemView: BlockListViewHolder)

    fun onBlockListPageItemViewDeleteClicked(blockListPageItemView: BlockListViewHolder)
}
