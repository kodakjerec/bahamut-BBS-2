package com.kota.telnetUI

import android.content.Context
import android.util.AttributeSet
import android.view.LayoutInflater
import android.view.View
import android.widget.ImageButton
import android.widget.LinearLayout
import android.widget.TextView
import com.kota.Bahamut.R
import com.kota.Bahamut.service.CommonFunctions.getContextColor
import com.kota.Bahamut.pages.theme.ThemeFunctions
import com.kota.Bahamut.service.UserSettings
import androidx.core.view.size

open class TelnetHeaderItemView : LinearLayout {
    protected var detail1: TextView? = null
    protected var detail2: TextView? = null
    protected var myTitle: TextView? = null
    protected var mMenuButton: ImageButton? = null
    protected var mMenuDivider: View? = null

    constructor(context: Context?) : super(context) {
        init()
    }

    constructor(context: Context?, attrs: AttributeSet?) : super(context, attrs) {
        init()
    }

    /** 提供子類別覆蓋 Layout ID */
    protected open fun getLayoutId(): Int {
        return R.layout.telnet_header_item_view
    }

    open fun init() {
        val inflater = context.getSystemService(Context.LAYOUT_INFLATER_SERVICE) as LayoutInflater
        inflater.inflate(getLayoutId(), this)
        myTitle = findViewById(R.id.title)
        detail1 = findViewById(R.id.detail_1)
        detail2 = findViewById(R.id.detail_2)
        mMenuDivider = findViewById(R.id.menu_divider)
        mMenuButton = findViewById(R.id.menu_button)

        // 側邊選單
        val location = UserSettings.propertiesDrawerLocation
        if (location == 1) {
            val headerItemView = findViewById<LinearLayout>(R.id.header_item_view)
            // 備份現在的view
            val alViews = ArrayList<View?>()
            for (i in headerItemView.size - 1 downTo 0) {
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

        // 套用主題顏色
        ThemeFunctions().applyThemeToHeaderItemView(this)
    }

    open fun setMenuButtonClickListener(aListener: OnClickListener?) {
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

    open fun setData(aTitle: String?, aDetail1: String?, aDetail2: String?) {
        setTitle(aTitle)
        setDetail1(aDetail1)
        setDetail2(aDetail2)
    }

    open fun setTitle(aTitle: String?) {
        if (myTitle != null) {
            myTitle?.text = aTitle
            if (aTitle != null && aTitle.contains("系統精靈送信來了")) {
                myTitle?.setTextColor(getContextColor(R.color.white))
                myTitle?.setBackgroundColor(getContextColor(R.color.red))
                val layoutParams = myTitle?.layoutParams
                layoutParams!!.width = LayoutParams.WRAP_CONTENT
                myTitle?.layoutParams = layoutParams
            }
        }
    }

    open fun setDetail1(aDetail1: String?) {
        if (detail1 != null) {
            detail1?.text = aDetail1
        }
    }

    /** 設定點擊功能 detail1  */
    open fun setDetail1ClickListener(aListener: OnClickListener?) {
        if (aListener != null) {
            detail1?.setOnClickListener(aListener)
        }
    }

    open fun setDetail2(aDetail2: String?) {
        if (detail2 != null) {
            detail2?.text = aDetail2
        }
    }
}
