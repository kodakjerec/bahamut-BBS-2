package com.kota.Bahamut.pages.messages

import android.view.Gravity
import android.view.View
import android.view.ViewGroup
import android.widget.BaseAdapter
import com.kota.Bahamut.service.TempSettings.myContext

class MessageSubAdapter(list: MutableList<BahaMessage>): BaseAdapter() {
    private var myList: MutableList<BahaMessage> = ArrayList()

    init {
        myList = list
    }

    override fun getCount(): Int {
        return myList.size
    }

    override fun getItem(index: Int): BahaMessage {
        return myList[index]
    }

    override fun getItemId(index: Int): Long {
        return index.toLong()
    }

    override fun getView(index: Int, convertView: View?, viewGroup: ViewGroup?): View {
        var myView: View? = convertView
        val item = getItem(index)
        if (item.type==0) {
            myView = MessageSubReceive(myContext!!)
            myView.setContent(item)
            myView.gravity = Gravity.START
        } else {
            myView = MessageSubSend(myContext!!)
            myView.setContent(item)
            myView.gravity = Gravity.END
        }

        return myView
    }

    // 新增資料
    fun addItem(item:BahaMessage) {
        myList.add(item)
        notifyDataSetChanged()
    }
}