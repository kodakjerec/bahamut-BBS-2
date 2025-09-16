package com.kota.Bahamut.pages.messages

import android.view.View
import android.view.ViewGroup
import android.widget.BaseAdapter
import com.kota.Bahamut.service.TempSettings

class MessageMainListAdapter(list: MutableList<MessageMainListItemStructure>): BaseAdapter() {
    private var myList: MutableList<MessageMainListItemStructure> = ArrayList()

    init {
        myList = list
    }

    override fun getCount(): Int {
        return myList.size
    }

    override fun getItem(index: Int): MessageMainListItemStructure {
        return myList[index]
    }

    override fun getItemId(index: Int): Long {
        return index.toLong()
    }

    override fun getView(index: Int, convertView: View?, viewGroup: ViewGroup?): View {
        var myView: View? = convertView

        myView = MessageMainListItem(TempSettings.myContext!!)
        myView.setContent(getItem(index))

        return myView
    }

    // 新增資料
    fun addItem(item:MessageMainListItemStructure) {
        myList.add(item)
        notifyDataSetChanged()
    }
}