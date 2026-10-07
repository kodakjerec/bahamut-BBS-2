package com.kota.asFramework.dialog

import android.graphics.Typeface
import android.util.TypedValue
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.LinearLayout
import android.widget.TextView
import androidx.core.content.ContextCompat
import com.kota.asFramework.pageController.ASViewController
import com.kota.Bahamut.R
import com.kota.Bahamut.service.CommonFunctions
import java.util.Vector
import kotlin.math.ceil

/**
 * [ASAlertDialog] - 自訂對話框類別
 *
 * 職責：
 * 提供應用程式內統一風格的對話框，支援自訂標題、訊息內容、多個按鈕以及尺寸調整。
 */
class ASAlertDialog : ASDialog, View.OnClickListener {

    /** 對話框的唯一識別碼，用於管理多個對話框的顯示與隱藏 */
    private var alertId: String? = null

    /** 儲存對話框下方按鈕的集合 */
    private val itemList = Vector<Button?>()

    /** 對話框按鈕點擊事件的監聽器 */
    private var listener: ASAlertDialogListener? = null

    /** 顯示對話框訊息內容的 TextView */
    private var messageLabel: TextView? = null

    /** 顯示對話框標題的 TextView */
    private var titleLabel: TextView? = null

    /** 標題與訊息內容之間的分隔線 */
    private var titleDivider: View? = null

    /** 放置按鈕的底部工具列 (LinearLayout) */
    private var toolbar: LinearLayout? = null

    /** 預設觸發的按鈕索引，當使用者取消或未點擊按鈕時返回此值 */
    private var defaultIndex = -1

    /** 預設建構子，初始化對話框 */
    constructor() {
        initial()
    }

    /**
     * 帶有識別碼的建構子
     *
     * @param paramString 對話框的唯一識別碼
     */
    constructor(paramString: String?) {
        initial()
        alertId = paramString
    }

    /**
     * 建立水平分隔線視圖
     *
     * @return 分隔線的 View
     */
    private fun createHorizontalDivider(): View {
        val view = View(context)
        val dividerHeight = ceil(
            TypedValue.applyDimension(
                TypedValue.COMPLEX_UNIT_DIP,
                1.0f,
                context.resources.displayMetrics
            ).toDouble()
        ).toInt()
        view.layoutParams = LinearLayout.LayoutParams(
            ViewGroup.LayoutParams.MATCH_PARENT,
            dividerHeight
        )
        view.setBackgroundColor(CommonFunctions.getThemeColor(R.attr.bahamut_dividerColor))
        return view
    }

    /**
     * 建立對話框的主要內容視圖 (包含標題、訊息、按鈕列)
     *
     * @return 內容的 View
     */
    private fun buildContentView(): View {
        val n = TypedValue.applyDimension(
            TypedValue.COMPLEX_UNIT_DIP,
            ASLayoutParams.instance.dialogWidthNormal,
            context.resources.displayMetrics
        ).toInt()
        val i = TypedValue.applyDimension(
            TypedValue.COMPLEX_UNIT_DIP,
            100.0f,
            context.resources.displayMetrics
        ).toInt()
        val k = ceil(
            TypedValue.applyDimension(
                TypedValue.COMPLEX_UNIT_DIP,
                6.0f,
                context.resources.displayMetrics
            ).toDouble()
        ).toInt()
        val j = ceil(
            TypedValue.applyDimension(
                TypedValue.COMPLEX_UNIT_DIP,
                3.0f,
                context.resources.displayMetrics
            ).toDouble()
        ).toInt()
        val m = ceil(
            TypedValue.applyDimension(
                TypedValue.COMPLEX_UNIT_DIP,
                1.0f,
                context.resources.displayMetrics
            ).toDouble()
        ).toInt()
        val linearLayout2 = LinearLayout(context)
        linearLayout2.orientation = LinearLayout.VERTICAL
        linearLayout2.setPadding(j, j, j, j)
        linearLayout2.setBackgroundColor(CommonFunctions.getThemeColor(R.attr.bahamut_dialogBorderColor))
        
        val linearLayout1 = LinearLayout(context)
        linearLayout1.orientation = LinearLayout.VERTICAL
        linearLayout1.setPadding(m, m, m, m)
        linearLayout1.setBackgroundColor(CommonFunctions.getThemeColor(R.attr.bahamut_pageBackground))
        linearLayout2.addView(linearLayout1 as View)
        
        titleLabel = TextView(context)
        titleLabel?.layoutParams = LinearLayout.LayoutParams(
            ViewGroup.LayoutParams.MATCH_PARENT,
            ViewGroup.LayoutParams.WRAP_CONTENT
        ) as ViewGroup.LayoutParams
        titleLabel?.setPadding(k, k, k, k)
        titleLabel?.setTextSize(
            2,
            ASLayoutParams.instance.textSizeUltraLarge
        )
        titleLabel?.setTextColor(CommonFunctions.getThemeColor(R.attr.bahamut_dialogTitleTextColor))
        titleLabel?.setTypeface(titleLabel?.typeface, Typeface.BOLD)
        titleLabel?.visibility = View.GONE
        titleLabel?.setBackgroundColor(CommonFunctions.getThemeColor(R.attr.bahamut_dialogTitleBackground))
        titleLabel?.isSingleLine = true
        linearLayout1.addView(titleLabel as View?)

        titleDivider = createHorizontalDivider()
        titleDivider?.visibility = View.GONE
        linearLayout1.addView(titleDivider as View?)

        messageLabel = TextView(context)
        messageLabel?.layoutParams = LinearLayout.LayoutParams(
            ViewGroup.LayoutParams.MATCH_PARENT,
            ViewGroup.LayoutParams.WRAP_CONTENT
        ) as ViewGroup.LayoutParams
        messageLabel?.setPadding(k, k, k, k)
        messageLabel?.setTextSize(2, ASLayoutParams.instance.textSizeLarge)
        messageLabel?.minimumHeight = i
        messageLabel?.setTextColor(CommonFunctions.getThemeColor(R.attr.bahamut_defaultTextColor))
        messageLabel?.visibility = View.GONE
        messageLabel?.setBackgroundColor(CommonFunctions.getThemeColor(R.attr.bahamut_pageBackground))
        linearLayout1.addView(messageLabel as View?)

        val toolbarTopDivider = createHorizontalDivider()
        linearLayout1.addView(toolbarTopDivider as View)

        toolbar = LinearLayout(context)
        toolbar?.layoutParams = LinearLayout.LayoutParams(
            ViewGroup.LayoutParams.MATCH_PARENT,
            ViewGroup.LayoutParams.WRAP_CONTENT
        ) as ViewGroup.LayoutParams
        toolbar?.gravity = 17
        toolbar?.orientation = LinearLayout.HORIZONTAL
        linearLayout1.addView(toolbar as View?)
        
        return linearLayout2 as View
    }

    /**
     * 清除對話框的內容與按鈕
     */
    private fun clear() {
        if (messageLabel != null) messageLabel?.text = ""
        if (titleLabel != null) {
            titleLabel?.text = ""
            titleLabel?.visibility = View.GONE
            titleDivider?.visibility = View.GONE
        }
        toolbar?.removeAllViews()
        itemList.clear()
    }

    /**
     * 建立對話框底部的按鈕
     *
     * @return 建立完成的 Button
     */
    private fun createButton(): Button {
        val button = Button(context)
        button.layoutParams = LinearLayout.LayoutParams(
            ViewGroup.LayoutParams.MATCH_PARENT,
            ViewGroup.LayoutParams.WRAP_CONTENT,
            1.0f
        ) as ViewGroup.LayoutParams
        val j = TypedValue.applyDimension(
            TypedValue.COMPLEX_UNIT_DIP,
            3.0f,
            context.resources.displayMetrics
        ).toInt()
        val i = TypedValue.applyDimension(
            TypedValue.COMPLEX_UNIT_DIP,
            5.0f,
            context.resources.displayMetrics
        ).toInt()
        button.setPadding(i, j, i, j)
        button.setTextSize(2, ASLayoutParams.instance.textSizeLarge)
        button.minimumHeight = TypedValue.applyDimension(
            TypedValue.COMPLEX_UNIT_DIP,
            ASLayoutParams.instance.defaultTouchBlockHeight,
            context.resources.displayMetrics
        ).toInt()
        button.gravity = 17
        button.setOnClickListener(this)
        val bgRes = CommonFunctions.getThemeResourceId(R.attr.bahamut_dialogItemBackground)
        if (bgRes != 0) {
            button.setBackgroundResource(bgRes)
        } else {
            button.background = ASLayoutParams.instance.alertItemBackgroundDrawable
        }
        val textColorRes = CommonFunctions.getThemeResourceId(R.attr.bahamut_buttonTextColor)
        if (textColorRes != 0) {
            button.setTextColor(ContextCompat.getColorStateList(context, textColorRes))
        } else {
            button.setTextColor(ASLayoutParams.instance.alertItemTextColor)
        }
        button.isSingleLine = false
        return button
    }

    /**
     * 初始化對話框，設定無標題視窗及背景
     */
    private fun initial() {
        requestWindowFeature(1)
        setContentView(buildContentView())
        window?.setBackgroundDrawable(null)
        setSize(currentDialogSize)
    }

    /**
     * 加入按鈕到對話框底部
     *
     * @param paramString 按鈕上顯示的文字
     * @return 回傳 ASAlertDialog 本身，以支援鏈式呼叫
     */
    fun addButton(paramString: String?): ASAlertDialog {
        if (paramString != null) {
            if (itemList.isNotEmpty()) toolbar?.addView(createDivider())
            val button = createButton()
            toolbar?.addView(button as View)
            button.text = paramString
            if (paramString.length < 4) {
                button.setTextSize(2, ASLayoutParams.instance.textSizeLarge)
            } else {
                button.setTextSize(2, ASLayoutParams.instance.textSizeNormal)
            }
            button.setOnClickListener(this)
            itemList.add(button)
        }
        return this
    }

    /**
     * 建立按鈕之間的垂直分隔線
     *
     * @return 垂直分隔線的 View
     */
    fun createDivider(): View {
        val view = View(context)
        view.layoutParams = LinearLayout.LayoutParams(
            ceil(
                TypedValue.applyDimension(
                    TypedValue.COMPLEX_UNIT_DIP,
                    1.0f,
                    context.resources.displayMetrics
                ).toDouble()
            ).toInt(), ViewGroup.LayoutParams.MATCH_PARENT
        ) as ViewGroup.LayoutParams
        view.setBackgroundColor(CommonFunctions.getThemeColor(R.attr.bahamut_toolbarDivider))
        return view
    }

    /**
     * 關閉對話框，並從管理清單中移除
     */
    override fun dismiss() {
        if (alertId != null) _alerts.remove(alertId)
        super.dismiss()
    }

    /**
     * 處理按鈕的點擊事件
     *
     * @param paramView 被點擊的按鈕 View
     */
    override fun onClick(paramView: View?) {
        if (listener != null) {
            val i = itemList.indexOf(paramView)
            listener?.onAlertDialogDismissWithButtonIndex(this, i)
        }
        dismiss()
    }

    /**
     * 設定特定按鈕的文字
     *
     * @param paramInt 按鈕的索引值
     * @param paramString 新的文字內容
     * @return 回傳 ASAlertDialog 本身，以支援鏈式呼叫
     */
    fun setItemTitle(paramInt: Int, paramString: String?): ASAlertDialog {
        if (paramInt >= 0 && paramInt < itemList.size) (itemList[paramInt] as Button).text =
            paramString
        return this
    }

    /**
     * 設定對話框的監聽器
     *
     * @param paramASAlertDialogListener 監聽器實例
     * @return 回傳 ASAlertDialog 本身，以支援鏈式呼叫
     */
    fun setListener(paramASAlertDialogListener: ASAlertDialogListener): ASAlertDialog {
        listener = paramASAlertDialogListener
        return this
    }

    /**
     * 設定對話框的訊息內容
     *
     * @param paramString 訊息內容字串
     * @return 回傳 ASAlertDialog 本身，以支援鏈式呼叫
     */
    fun setMessage(paramString: String?): ASAlertDialog {
        if (paramString == null) {
            messageLabel?.visibility = View.GONE
            return this
        }
        messageLabel?.visibility = View.VISIBLE
        messageLabel?.text = paramString
        return this
    }

    /**
     * 設定對話框的標題
     *
     * @param paramString 標題字串
     * @return 回傳 ASAlertDialog 本身，以支援鏈式呼叫
     */
    fun setTitle(paramString: String?): ASAlertDialog {
        if (paramString == null) {
            titleLabel?.visibility = View.GONE
            titleDivider?.visibility = View.GONE
            return this
        }
        titleLabel?.visibility = View.VISIBLE
        titleDivider?.visibility = View.VISIBLE
        titleLabel?.text = paramString
        return this
    }

    /**
     * 顯示對話框，若已有相同 ID 的對話框存在則先將其關閉
     */
    override fun show() {
        if (alertId != null) {
            val aSAlertDialog: ASAlertDialog? = _alerts[alertId]
            if (aSAlertDialog != null && aSAlertDialog.isShowing) aSAlertDialog.dismiss()
            _alerts.put(alertId, this)
        }
        super.show()
    }

    /**
     * 設定都不按的時候, 是否傳回預設值 (預設不傳)
     *
     * @param i 預設按鈕的索引
     * @return 回傳 ASAlertDialog 本身，以支援鏈式呼叫
     */
    fun setDefaultButtonIndex(i: Int): ASAlertDialog {
        defaultIndex = i
        return this
    }

    /**
     * 取消對話框，若有設定預設索引則觸發監聽器
     */
    override fun cancel() {
        if (defaultIndex > -1) {
            listener?.onAlertDialogDismissWithButtonIndex(this, defaultIndex)
        }
        super.cancel()
    }

    companion object {
        /** 儲存所有具有 ID 的對話框的映射表 */
        private val _alerts: MutableMap<String?, ASAlertDialog?> =
            HashMap<String?, ASAlertDialog?>()

        /**
         * 關閉所有受到管理的對話框
         */
        @JvmStatic
        fun dismissAllAlerts() {
            for (alert in _alerts.values) {
                alert?.dismiss()
            }
            _alerts.clear()
        }

        /**
         * 檢查特定 ID 的對話框是否存在
         *
         * @param paramString 對話框識別碼
         * @return 是否存在
         */
        fun containsAlert(paramString: String?): Boolean {
            var bool = false
            if (paramString != null) bool = _alerts.containsKey(paramString)
            return bool
        }

        /**
         * 建立或取得指定 ID 的對話框
         *
         * @param paramString 對話框識別碼
         * @return 對話框實例
         */
        fun create(paramString: String?): ASAlertDialog {
            val aSAlertDialog: ASAlertDialog? = _alerts[paramString]
            if (aSAlertDialog == null) return ASAlertDialog(paramString)
            aSAlertDialog.clear()
            return aSAlertDialog
        }

        /**
         * 建立一個新的無 ID 對話框
         *
         * @return 對話框實例
         */
        @JvmStatic
        fun createDialog(): ASAlertDialog {
            return ASAlertDialog()
        }

        /**
         * 隱藏指定 ID 的對話框
         *
         * @param paramString 對話框識別碼
         */
        fun hideAlert(paramString: String?) {
            val aSAlertDialog: ASAlertDialog? = _alerts[paramString]
            aSAlertDialog?.dismiss()
        }

        /**
         * 顯示包含錯誤訊息的對話框，並在頁面消失時自動關閉
         *
         * @param errMessage 錯誤訊息字串
         * @param aSViewController 關聯的視圖控制器
         */
        @JvmStatic
        fun showErrorDialog(errMessage: String?, aSViewController: ASViewController?) {
            createDialog()
                .setTitle("錯誤")
                .setMessage(errMessage)
                .addButton("確定")
                .setListener { aDialog: ASAlertDialog?, index: Int -> aDialog?.dismiss() }
                .scheduleDismissOnPageDisappear(aSViewController)
                .show()
        }
    }
}
