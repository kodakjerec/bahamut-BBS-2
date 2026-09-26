package com.kota.Bahamut.pages.messages

import android.view.View
import android.view.ViewGroup
import android.widget.BaseAdapter
import com.kota.Bahamut.service.TempSettings

class MessageMainListAdapter(list: MutableList<MessageMainListItemStructure> = ArrayList()): BaseAdapter() {
    private var myList: MutableList<MessageMainListItemStructure> = ArrayList()

    init {
        myList.addAll(list)
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
        val myView: MessageMainListItem = if (convertView is MessageMainListItem) {
            convertView
        } else {
            MessageMainListItem(TempSettings.myContext!!)
        }
        myView.setContent(getItem(index))
        return myView
    }

    // 批次追加資料並去重
    fun addItems(newItems: List<MessageMainListItemStructure>) {
        var hasChanged = false
        for (newItem in newItems) {
            if (newItem.senderName.isEmpty()) continue
            val existingIndex = myList.indexOfFirst {
                it.senderName.equals(newItem.senderName, ignoreCase = true)
            }
            if (existingIndex != -1) {
                myList[existingIndex] = newItem
                hasChanged = true
            } else {
                myList.add(newItem)
                hasChanged = true
            }
        }
        if (hasChanged) {
            notifyDataSetChanged()
        }
    }

    // 新增單筆資料
    fun addItem(item: MessageMainListItemStructure) {
        addItems(listOf(item))
    }

    // 清空列表
    fun clear() {
        myList.clear()
        notifyDataSetChanged()
    }
}
