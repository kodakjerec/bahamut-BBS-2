package com.kota.Bahamut.pages.boardPage

import android.content.Context
import android.util.AttributeSet
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageButton
import android.widget.LinearLayout
import android.widget.TextView
import com.kota.Bahamut.R
import com.kota.Bahamut.service.CommonFunctions.getContextColor
import com.kota.Bahamut.service.UserSettings

class BoardHeaderView : LinearLayout {
    private var detail1: TextView? = null
    private var detail2: TextView? = null
    private var myTitle: TextView? = null
    private var mMenuButton: ImageButton? = null
    private var mMenuDivider: View? = null

    constructor(context: Context?) : super(context) {
        init()
    }

    constructor(context: Context?, attrs: AttributeSet?) : super(context, attrs) {
        init()
    }

    fun init() {
        (context.getSystemService(Context.LAYOUT_INFLATER_SERVICE) as LayoutInflater).inflate(
            R.layout.board_header_view,
            this
        )
        myTitle = findViewById(R.id.title)
        detail1 = findViewById(R.id.detail_1)
        detail2 = findViewById(R.id.detail_2)
        mMenuDivider = findViewById(R.id.menu_divider)
        mMenuButton = findViewById(R.id.menu_button)

        // 側邊選單
        val location = UserSettings.getPropertiesDrawerLocation()
        if (location == 1) {
            val headerItemView = findViewById<LinearLayout>(R.id.header_item_view)
            // 備份現在的view
            val alViews = ArrayList<View>()
            for (i in headerItemView.childCount - 1 downTo 0) {
                val view = headerItemView.getChildAt(i)
                alViews.add(view)
            }
            // 刪除所有child-view
            headerItemView.removeAllViews()
            // 回填
            for (j in alViews.indices) {
                headerItemView.addView(alViews[j])
            }
        }
    }

    fun setMenuButtonClickListener(aListener: OnClickListener?) {
        if (aListener == null) {
            mMenuDivider?.visibility = GONE
            mMenuButton?.visibility = GONE
            mMenuButton?.setOnClickListener(null)
            return
        }
        mMenuDivider?.visibility = VISIBLE
        mMenuButton?.visibility = VISIBLE
        mMenuButton?.setOnClickListener(aListener)
    }

    fun setData(aTitle: String?, aDetail1: String?, aDetail2: String?) {
        setTitle(aTitle)
        setDetail1(aDetail1)
        setDetail2(aDetail2)
    }

    fun setTitle(aTitle: String?) {
        if (myTitle != null) {
            myTitle?.text = aTitle
            if (aTitle != null && aTitle.contains("系統精靈送信來了")) {
                myTitle?.setTextColor(getContextColor(R.color.white))
                myTitle?.setBackgroundColor(getContextColor(R.color.red))
                val layoutParams = myTitle?.layoutParams
                layoutParams.width = ViewGroup.LayoutParams.WRAP_CONTENT
                myTitle?.layoutParams = layoutParams
            }
        }
    }

    private fun setDetail1(aDetail1: String?) {
        if (detail1 != null) {
            detail1?.text = aDetail1
        }
    }

    /** 設定點擊功能 detail1  */
    fun setDetail1ClickListener(aListener: OnClickListener?) {
        if (aListener != null) {
            detail1?.setOnClickListener(aListener)
            val detailvV = findViewById<TextView>(R.id.detail_vV)
            detailvV.visibility = VISIBLE
        }
    }

    private fun setDetail2(aDetail2: String?) {
        if (detail2 != null) {
            detail2?.text = aDetail2
        }
    }
}
