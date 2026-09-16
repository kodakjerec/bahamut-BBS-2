package com.kota.Bahamut.pages.boardPage

import android.content.Context
import android.util.AttributeSet
import android.view.View
import android.widget.RelativeLayout
import android.widget.TextView
import com.kota.Bahamut.R
import com.kota.Bahamut.service.CommonFunctions.getContextString
import com.kota.Bahamut.service.CommonFunctions.getThemeColor
import com.kota.telnetUI.TelnetHeaderItemView
import com.kota.telnetUI.textView.TelnetTextViewSmall

class BoardHeaderView : TelnetHeaderItemView {
    private var detailVV: TextView? = null

    constructor(context: Context?) : super(context)

    constructor(context: Context?, attrs: AttributeSet?) : super(context, attrs)

    override fun createDetailsLayout(dp: (Float) -> Int): View {
        val relativeLayout = RelativeLayout(context).apply {
            id = R.id.relativeLayout
            layoutParams = LayoutParams(LayoutParams.MATCH_PARENT, LayoutParams.WRAP_CONTENT)
        }

        detail1 = TelnetTextViewSmall(context).apply {
            id = R.id.detail_1
            layoutParams = RelativeLayout.LayoutParams(
                RelativeLayout.LayoutParams.WRAP_CONTENT,
                RelativeLayout.LayoutParams.WRAP_CONTENT
            ).apply {
                addRule(RelativeLayout.ALIGN_PARENT_START)
            }
            ellipsize = android.text.TextUtils.TruncateAt.END
            isSingleLine = true
            setTextColor(getThemeColor(R.attr.bahamut_titleBarDetailColor))
        }
        relativeLayout.addView(detail1)

        detailVV = TelnetTextViewSmall(context).apply {
            id = R.id.detail_vV
            layoutParams = RelativeLayout.LayoutParams(
                RelativeLayout.LayoutParams.WRAP_CONTENT,
                RelativeLayout.LayoutParams.WRAP_CONTENT
            ).apply {
                addRule(RelativeLayout.END_OF, R.id.detail_1)
            }
            ellipsize = android.text.TextUtils.TruncateAt.END
            isSingleLine = true
            text = getContextString(R.string.board_main_vV)
            setTextColor(getThemeColor(R.attr.bahamut_titleBarDetail2Color))
            visibility = GONE
        }
        relativeLayout.addView(detailVV)

        detail2 = TelnetTextViewSmall(context).apply {
            id = R.id.detail_2
            layoutParams = RelativeLayout.LayoutParams(
                RelativeLayout.LayoutParams.WRAP_CONTENT,
                RelativeLayout.LayoutParams.WRAP_CONTENT
            ).apply {
                addRule(RelativeLayout.ALIGN_PARENT_END)
            }
            isSingleLine = true
            setTextColor(getThemeColor(R.attr.bahamut_titleBarDetail2Color))
        }
        relativeLayout.addView(detail2)

        return relativeLayout
    }

    /** 設定點擊功能 detail1  */
    override fun setDetail1ClickListener(aListener: OnClickListener?) {
        super.setDetail1ClickListener(aListener)
        if (aListener != null) {
            val v = detailVV ?: findViewById<TextView>(R.id.detail_vV)
            v?.visibility = VISIBLE
        }
    }
}
