package com.kota.Bahamut.Pages.Messages

import android.view.View
import android.view.ViewGroup
import android.widget.BaseAdapter
import com.kota.Bahamut.Service.TempSettings

class MessageMainAdapter(list: MutableList<BahaMessageSummarize>): BaseAdapter() {
    private var myList: MutableList<BahaMessageSummarize> = ArrayList()

    init {
        myList = list
    }

    override fun getCount(): Int {
        return myList.size
    }

    override fun getItem(index: Int): BahaMessageSummarize {
        return myList[index]
    }

    override fun getItemId(index: Int): Long {
        return index.toLong()
    }

    override fun getView(index: Int, convertView: View?, viewGroup: ViewGroup?): View {
        var myView: View? = convertView

        myView = MessageMainItem(TempSettings.myContext!!)
        myView.setContent(getItem(index))

        return myView
    }

    // 新增資料
    fun addItem(item:BahaMessageSummarize) {
        myList.add(item)
        notifyDataSetChanged()
    }
}