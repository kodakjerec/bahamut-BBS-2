package com.kota.TelnetUI

import android.content.Context
import android.util.AttributeSet
import android.view.LayoutInflater
import android.view.View
import android.widget.ImageButton
import android.widget.LinearLayout
import android.widget.TextView
import com.kota.Bahamut.R
import com.kota.Bahamut.Service.CommonFunctions.getContextColor
import com.kota.Bahamut.Service.UserSettings

open class TelnetHeaderItemView : LinearLayout {
    private var _detail_1: TextView? = null
    private var _detail_2: TextView? = null
    private var _title: TextView? = null
    private var mMenuButton: ImageButton? = null
    private var mMenuDivider: View? = null

    constructor(context: Context?) : super(context) {
        init()
    }

    constructor(context: Context?, attrs: AttributeSet?) : super(context, attrs) {
        init()
    }

    fun init() {
        (getContext().getSystemService(Context.LAYOUT_INFLATER_SERVICE) as LayoutInflater).inflate(
            R.layout.telnet_header_item_view,
            this
        )
        _title = findViewById<TextView?>(R.id.title)
        _detail_1 = findViewById<TextView?>(R.id.detail_1)
        _detail_2 = findViewById<TextView?>(R.id.detail_2)
        mMenuDivider = findViewById<View>(R.id.menu_divider)
        mMenuButton = findViewById<ImageButton>(R.id.menu_button)

        // 側邊選單
        val location = UserSettings.getPropertiesDrawerLocation()
        if (location == 1) {
            val headerItemView = findViewById<LinearLayout>(R.id.header_item_view)
            // 備份現在的view
            val alViews = ArrayList<View?>()
            for (i in headerItemView.getChildCount() - 1 downTo 0) {
                val view = headerItemView.getChildAt(i)
                alViews.add(view)
            }
            // 刪除所有child-view
            headerItemView.removeAllViews()
            // 回填
            for (j in alViews.indices) {
                headerItemView.addView(alViews.get(j))
            }
        }
    }

    fun setMenuButtonClickListener(aListener: OnClickListener?) {
        if (aListener == null) {
            mMenuDivider!!.setVisibility(GONE)
            mMenuButton!!.setVisibility(GONE)
            mMenuButton!!.setOnClickListener(null)
            return
        }
        mMenuDivider!!.setVisibility(VISIBLE)
        mMenuButton!!.setVisibility(VISIBLE)
        mMenuButton!!.setOnClickListener(aListener)
    }

    fun setData(aTitle: String?, aDetail1: String?, aDetail2: String?) {
        setTitle(aTitle)
        setDetail1(aDetail1)
        setDetail2(aDetail2)
    }

    fun setTitle(aTitle: String?) {
        if (_title != null) {
            _title!!.setText(aTitle)
            if (aTitle != null && aTitle.contains("系統精靈送信來了")) {
                _title!!.setTextColor(getContextColor(R.color.white))
                _title!!.setBackgroundColor(getContextColor(R.color.red))
                val layoutParams = _title!!.getLayoutParams()
                layoutParams.width = LayoutParams.WRAP_CONTENT
                _title!!.setLayoutParams(layoutParams)
            }
        }
    }

    fun setDetail1(aDetail1: String?) {
        if (_detail_1 != null) {
            _detail_1!!.setText(aDetail1)
        }
    }

    /** 設定點擊功能 detail1  */
    fun setDetail1ClickListener(aListener: OnClickListener?) {
        if (aListener != null) {
            _detail_1!!.setOnClickListener(aListener)
        }
    }

    fun setDetail2(aDetail2: String?) {
        if (_detail_2 != null) {
            _detail_2!!.setText(aDetail2)
        }
    }
}
