package com.kota.TelnetUI

import android.content.Context
import android.util.AttributeSet
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageButton
import android.widget.LinearLayout
import android.widget.TextView
import com.kota.Bahamut.R
import com.kota.Bahamut.Service.CommonFunctions.getContextColor
import com.kota.Bahamut.Service.UserSettings

class TelnetHeaderItemView @JvmOverloads constructor(
    context: Context,
    attrs: AttributeSet? = null
) : LinearLayout(context, attrs) {
    
    private var detail1: TextView? = null
    private var detail2: TextView? = null
    private var title: TextView? = null
    private lateinit var menuButton: ImageButton
    private lateinit var menuDivider: View
    
    init {
        init()
    }
    
    private fun init() {
        val inflater = context.getSystemService(Context.LAYOUT_INFLATER_SERVICE) as LayoutInflater
        inflater.inflate(R.layout.telnet_header_item_view, this)
        
        title = findViewById(R.id.title)
        detail1 = findViewById(R.id.detail_1)
        detail2 = findViewById(R.id.detail_2)
        menuDivider = findViewById(R.id.menu_divider)
        menuButton = findViewById(R.id.menu_button)
        
        // 側邊選單
        val location = UserSettings.getPropertiesDrawerLocation()
        if (location == 1) {
            val headerItemView = findViewById<LinearLayout>(R.id.header_item_view)
            
            // 備份現在的view
            val views = mutableListOf<View>()
            for (i in headerItemView.childCount - 1 downTo 0) {
                val view = headerItemView.getChildAt(i)
                views.add(view)
            }
            
            // 刪除所有child-view
            headerItemView.removeAllViews()
            
            // 回填
            views.forEach { view ->
                headerItemView.addView(view)
            }
        }
    }
    
    fun setMenuButtonClickListener(listener: OnClickListener?) {
        if (listener == null) {
            menuDivider.visibility = GONE
            menuButton.visibility = GONE
            menuButton.setOnClickListener(null)
            return
        }
        menuDivider.visibility = VISIBLE
        menuButton.visibility = VISIBLE
        menuButton.setOnClickListener(listener)
    }
    
    fun setData(title: String?, detail1: String?, detail2: String?) {
        setTitle(title)
        setDetail1(detail1)
        setDetail2(detail2)
    }
    
    fun setTitle(titleText: String?) {
        title?.let { titleView ->
            titleView.text = titleText
            if (titleText?.contains("系統精靈送信來了") == true) {
                titleView.setTextColor(getContextColor(R.color.white))
                titleView.setBackgroundColor(getContextColor(R.color.red))
                val layoutParams = titleView.layoutParams
                layoutParams.width = ViewGroup.LayoutParams.WRAP_CONTENT
                titleView.layoutParams = layoutParams
            }
        }
    }
    
    fun setDetail1(detail1Text: String?) {
        detail1?.text = detail1Text
    }
    
    /** 設定點擊功能 detail1 */
    fun setDetail1ClickListener(listener: OnClickListener?) {
        listener?.let { detail1?.setOnClickListener(it) }
    }
    
    fun setDetail2(detail2Text: String?) {
        detail2?.text = detail2Text
    }
}
