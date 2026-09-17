package com.kota.telnetUI

import android.content.Context
import android.util.AttributeSet
import android.view.View
import android.widget.ImageButton
import android.widget.LinearLayout
import android.widget.TextView
import androidx.core.content.ContextCompat
import androidx.core.view.size
import com.kota.Bahamut.R
import com.kota.Bahamut.service.CommonFunctions.getContextColor
import com.kota.Bahamut.service.CommonFunctions.getThemeColor
import com.kota.Bahamut.service.UserSettings
import com.kota.telnetUI.textView.TelnetTextViewNormal
import com.kota.telnetUI.textView.TelnetTextViewSmall

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

    /** 提供子類別覆蓋詳細資訊區塊 */
    protected open fun createDetailsLayout(dp: (Float) -> Int): View {
        val detailsRow = LinearLayout(context).apply {
            orientation = HORIZONTAL
            layoutParams = LayoutParams(LayoutParams.MATCH_PARENT, LayoutParams.WRAP_CONTENT)
        }

        detail1 = TelnetTextViewSmall(context).apply {
            id = R.id.detail_1
            layoutParams = LayoutParams(0, LayoutParams.WRAP_CONTENT, 1f)
            ellipsize = android.text.TextUtils.TruncateAt.END
            isSingleLine = true
            setTextColor(getThemeColor(R.attr.bahamut_titleBarDetailColor))
        }
        detailsRow.addView(detail1)

        detail2 = TelnetTextViewSmall(context).apply {
            id = R.id.detail_2
            layoutParams = LayoutParams(LayoutParams.WRAP_CONTENT, LayoutParams.WRAP_CONTENT)
            isSingleLine = true
            setTextColor(getThemeColor(R.attr.bahamut_titleBarDetail2Color))
        }
        detailsRow.addView(detail2)

        return detailsRow
    }

    open fun init() {
        val density = context.resources.displayMetrics.density
        fun dp(value: Float): Int = (value * density + 0.5f).toInt()

        orientation = VERTICAL
        layoutParams = LayoutParams(LayoutParams.MATCH_PARENT, LayoutParams.WRAP_CONTENT)

        val headerItemView = LinearLayout(context).apply {
            id = R.id.header_item_view
            orientation = HORIZONTAL
            layoutParams = LayoutParams(LayoutParams.MATCH_PARENT, LayoutParams.WRAP_CONTENT)
            setBackgroundColor(getThemeColor(R.attr.bahamut_titleBarBackground))
        }

        val textBlock = LinearLayout(context).apply {
            orientation = VERTICAL
            layoutParams = LayoutParams(0, LayoutParams.WRAP_CONTENT, 1f)
            setBackgroundColor(getThemeColor(R.attr.bahamut_titleBarBackground))
            setPadding(dp(10f), dp(6f), dp(10f), dp(6f))
        }

        myTitle = TelnetTextViewNormal(context).apply {
            id = R.id.title
            layoutParams = LayoutParams(LayoutParams.MATCH_PARENT, LayoutParams.WRAP_CONTENT)
            setTextIsSelectable(true)
            setTextColor(getThemeColor(R.attr.bahamut_titleBarTitleColor))
        }
        textBlock.addView(myTitle)

        val detailsLayout = createDetailsLayout(::dp)
        textBlock.addView(detailsLayout)

        headerItemView.addView(textBlock)

        mMenuButton = ImageButton(context).apply {
            id = R.id.menu_button
            layoutParams = LayoutParams(dp(60f), LayoutParams.MATCH_PARENT)
            setImageDrawable(ContextCompat.getDrawable(context, R.drawable.menu_icon))
            imageTintList = android.content.res.ColorStateList.valueOf(getThemeColor(R.attr.bahamut_defaultTextColor))
            setBackgroundColor(getThemeColor(R.attr.bahamut_titleBarMenuIconBackground))
            contentDescription = com.kota.Bahamut.service.CommonFunctions.getContextString(R.string.zero_word)
            visibility = GONE
        }
        headerItemView.addView(mMenuButton)

        addView(headerItemView)

        val divider = View(context).apply {
            layoutParams = LayoutParams(LayoutParams.MATCH_PARENT, dp(1f))
            setBackgroundColor(getThemeColor(R.attr.bahamut_dividerColor))
        }
        addView(divider)

        // 側邊選單
        val location = UserSettings.propertiesDrawerLocation
        if (location == 1) {
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
        updateThemeColors()
    }

    open fun updateThemeColors() {
        val titleColor = getThemeColor(R.attr.bahamut_titleBarTitleColor)
        val detail1Color = getThemeColor(R.attr.bahamut_titleBarDetailColor)
        val detail2Color = getThemeColor(R.attr.bahamut_titleBarDetail2Color)
        val headerBg = getThemeColor(R.attr.bahamut_titleBarBackground)

        if (myTitle?.text?.contains("系統精靈送信來了") != true) {
            myTitle?.setTextColor(titleColor)
        }
        detail1?.setTextColor(detail1Color)
        detail2?.setTextColor(detail2Color)

        findViewById<View>(R.id.header_item_view)?.setBackgroundColor(headerBg)
        findViewById<View>(R.id.title)?.parent?.let { parentView ->
            if (parentView is View) {
                parentView.setBackgroundColor(headerBg)
            }
        }
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
        updateThemeColors()
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
