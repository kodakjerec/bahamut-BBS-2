package com.kota.Bahamut.pages.boardPage

import android.app.Activity
import android.content.Context
import android.util.AttributeSet
import android.util.Log
import android.util.TypedValue
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageButton
import android.widget.LinearLayout
import android.widget.TextView
import com.kota.Bahamut.R
import com.kota.Bahamut.service.CommonFunctions.getContextColor
import com.kota.Bahamut.service.UserSettings
import com.kota.asFramework.ui.ResponsiveLayoutHelper
import com.kota.asFramework.ui.ResponsiveLayoutHelper.isSmallWindow

class BoardHeaderView : LinearLayout {
    private var detail1: TextView? = null
    private var detail2: TextView? = null
    private var myTitle: TextView? = null
    private var mMenuButton: ImageButton? = null
    private var mMenuDivider: View? = null

    // 修改成員變數
    private var defaultHeaderHeightPx = -1
    private var isDefaultHeightCaptured = false
    private var originalHeightParam = LayoutParams.WRAP_CONTENT // 紀錄原始 LayoutParams 設定

    // 用於儲存內部 TextView 的原始文字大小
    private val originalTextSizes = mutableMapOf<Int, Float>()

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

        // 儲存所有 TextView 的原始文字大小
        fun storeOriginalTextSizes(viewGroup: ViewGroup) {
            for (i in 0 until viewGroup.childCount) {
                val child = viewGroup.getChildAt(i)
                if (child is TextView && child.id != View.NO_ID) {
                    originalTextSizes[child.id] = child.textSize / resources.displayMetrics.scaledDensity
                } else if (child is ViewGroup) {
                    storeOriginalTextSizes(child)
                }
            }
        }
        storeOriginalTextSizes(this)

        // 側邊選單
        val location = UserSettings.propertiesDrawerLocation
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

        // 加入監聽器，當佈局變動時檢查是否需要回應式調整
        this.addOnLayoutChangeListener { _, left, _, right, _, _, _, _, _ ->
            val currentWidth = right - left
            // 如果還沒記錄到真正的測量高度，且現在已經有高度了，就記錄下來
            if (!isDefaultHeightCaptured && height > 0) {
                defaultHeaderHeightPx = height
                isDefaultHeightCaptured = true
                
                // 同時在此時紀錄原始的 LayoutParams 設定，因為此時 layoutParams 必不為空
                if (layoutParams != null) {
                    originalHeightParam = layoutParams.height
                }
                
                Log.d("BoardHeaderView", "捕捉到自然高度: $defaultHeaderHeightPx px, 原始參數: $originalHeightParam")
            }

            val activity = context as? Activity
            if (activity != null && currentWidth > 0) {
                // 使用 post 確保在佈局完成後才執行調整，避免資訊同步落後的問題
                post {
                    applyResponsiveHeaderLayout(activity)
                }
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
                val layoutParams = myTitle?.layoutParams!!
                layoutParams.width = LayoutParams.WRAP_CONTENT
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
            val detailVV = findViewById<TextView>(R.id.detail_vV)
            detailVV.visibility = VISIBLE
        }
    }

    private fun setDetail2(aDetail2: String?) {
        if (detail2 != null) {
            detail2?.text = aDetail2
        }
    }

    /**
     * 應用響應式標題列佈局調整。
     * 改為：小視窗模式固定高度為 40dp，寬螢幕模式還原原始設定。
     */
    fun applyResponsiveHeaderLayout(activity: Activity) {
        val layoutParams = this.layoutParams
        if (layoutParams == null) {
            Log.e("BoardHeaderView", "BoardHeaderView 的 layoutParams 為空，無法應用響應式佈局。")
            return
        }

        if (isSmallWindow(activity)) {
            // 使用 TypedValue 將 40dp 轉換為 px
            val newHeight = TypedValue.applyDimension(
                TypedValue.COMPLEX_UNIT_DIP,
                40f,
                resources.displayMetrics
            ).toInt()

            if (layoutParams.height != newHeight) {
                layoutParams.height = newHeight
                this.layoutParams = layoutParams
                Log.d("BoardHeaderView", "小視窗模式：固定高度設為 40dp ($newHeight px)")
            }
        } else {
            // 其餘還原：如果原本是 wrap_content，就還原成 wrap_content 讓內容自然撐開
            if (isDefaultHeightCaptured && layoutParams.height != originalHeightParam) {
                layoutParams.height = originalHeightParam
                this.layoutParams = layoutParams
                Log.d("BoardHeaderView", "寬螢幕模式：還原原始高度設定: $originalHeightParam")
            }
        }
    }
}
