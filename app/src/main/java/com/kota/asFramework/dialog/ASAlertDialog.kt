package com.kota.asFramework.dialog

import android.graphics.Typeface
import android.util.TypedValue
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.LinearLayout
import android.widget.TextView
import com.kota.asFramework.pageController.ASViewController
import com.kota.Bahamut.R
import java.util.Vector
import kotlin.math.ceil

class ASAlertDialog : ASDialog, View.OnClickListener {
    private var alertId: String? = null

    private val itemList = Vector<Button?>()

    private var listener: ASAlertDialogListener? = null

    private var messageLabel: TextView? = null

    private var titleLabel: TextView? = null

    private var toolbar: LinearLayout? = null
    private var defaultIndex = -1

    constructor() {
        initial()
    }

    constructor(paramString: String?) {
        initial()
        alertId = paramString
    }

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
        linearLayout2.setBackgroundResource(R.color.dialog_border_color)
        val linearLayout1 = LinearLayout(context)
        linearLayout1.orientation = LinearLayout.VERTICAL
        linearLayout1.setPadding(m, m, m, m)
        linearLayout1.setBackgroundColor(-16777216)
        linearLayout2.addView(linearLayout1 as View)
        titleLabel = TextView(context)
        titleLabel!!.layoutParams = LinearLayout.LayoutParams(
            n,
            ViewGroup.LayoutParams.WRAP_CONTENT
        ) as ViewGroup.LayoutParams
        titleLabel!!.setPadding(k, k, k, k)
        titleLabel!!.setTextSize(
            2,
            ASLayoutParams.instance.textSizeUltraLarge
        )
        titleLabel!!.setTextColor(-1)
        titleLabel!!.setTypeface(titleLabel!!.typeface, Typeface.BOLD)
        titleLabel!!.visibility = View.GONE
        titleLabel!!.setBackgroundColor(-15724528)
        titleLabel!!.isSingleLine = true
        linearLayout1.addView(titleLabel as View?)
        messageLabel = TextView(context)
        messageLabel!!.layoutParams = LinearLayout.LayoutParams(
            n,
            ViewGroup.LayoutParams.WRAP_CONTENT
        ) as ViewGroup.LayoutParams
        messageLabel!!.setPadding(k, k, k, k)
        messageLabel!!.setTextSize(2, ASLayoutParams.instance.textSizeLarge)
        messageLabel!!.minimumHeight = i
        messageLabel!!.setTextColor(-1)
        messageLabel!!.visibility = View.GONE
        messageLabel!!.setBackgroundColor(-16777216)
        linearLayout1.addView(messageLabel as View?)
        toolbar = LinearLayout(context)
        toolbar!!.layoutParams = LinearLayout.LayoutParams(
            n,
            ViewGroup.LayoutParams.WRAP_CONTENT
        ) as ViewGroup.LayoutParams
        toolbar!!.gravity = 17
        toolbar!!.orientation = LinearLayout.HORIZONTAL
        linearLayout1.addView(toolbar as View?)
        return linearLayout2 as View
    }

    private fun clear() {
        if (messageLabel != null) messageLabel!!.text = ""
        if (titleLabel != null) titleLabel!!.text = ""
        toolbar!!.removeAllViews()
        itemList.clear()
    }

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
        button.background = ASLayoutParams.instance.alertItemBackgroundDrawable
        button.isSingleLine = false
        button.setTextColor(ASLayoutParams.instance.alertItemTextColor)
        return button
    }

    private fun initial() {
        requestWindowFeature(1)
        setContentView(buildContentView())
        window!!.setBackgroundDrawable(null)
    }

    fun addButton(paramString: String?): ASAlertDialog {
        if (paramString != null) {
            if (itemList.isNotEmpty()) toolbar!!.addView(createDivider())
            val button = createButton()
            toolbar!!.addView(button as View)
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
        view.setBackgroundColor(-16777216)
        return view
    }

    override fun dismiss() {
        if (alertId != null) _alerts.remove(alertId)
        super.dismiss()
    }

    override fun onClick(paramView: View?) {
        if (listener != null) {
            val i = itemList.indexOf(paramView)
            listener!!.onAlertDialogDismissWithButtonIndex(this, i)
        }
        dismiss()
    }

    fun setItemTitle(paramInt: Int, paramString: String?): ASAlertDialog {
        if (paramInt >= 0 && paramInt < itemList.size) (itemList[paramInt] as Button).text =
            paramString
        return this
    }

    fun setListener(paramASAlertDialogListener: ASAlertDialogListener): ASAlertDialog {
        listener = paramASAlertDialogListener
        return this
    }

    fun setMessage(paramString: String?): ASAlertDialog {
        if (paramString == null) {
            messageLabel!!.visibility = View.GONE
            return this
        }
        messageLabel!!.visibility = View.VISIBLE
        messageLabel!!.text = paramString
        return this
    }

    fun setTitle(paramString: String?): ASAlertDialog {
        if (paramString == null) {
            titleLabel!!.visibility = View.GONE
            return this
        }
        titleLabel!!.visibility = View.VISIBLE
        titleLabel!!.text = paramString
        return this
    }

    override fun show() {
        if (alertId != null) {
            val aSAlertDialog: ASAlertDialog? = _alerts[alertId]
            if (aSAlertDialog != null && aSAlertDialog.isShowing) aSAlertDialog.dismiss()
            _alerts.put(alertId, this)
        }
        super.show()
    }

    // 設定都不按的時候, 是否傳回預設值
    // 預設不傳
    fun setDefaultButtonIndex(i: Int): ASAlertDialog {
        defaultIndex = i
        return this
    }

    override fun cancel() {
        if (defaultIndex > -1) {
            listener!!.onAlertDialogDismissWithButtonIndex(this, defaultIndex)
        }
        super.cancel()
    }

    companion object {
        private val _alerts: MutableMap<String?, ASAlertDialog?> =
            HashMap<String?, ASAlertDialog?>()

        fun containsAlert(paramString: String?): Boolean {
            var bool = false
            if (paramString != null) bool = _alerts.containsKey(paramString)
            return bool
        }

        fun create(paramString: String?): ASAlertDialog {
            val aSAlertDialog: ASAlertDialog? = _alerts[paramString]
            if (aSAlertDialog == null) return ASAlertDialog(paramString)
            aSAlertDialog.clear()
            return aSAlertDialog
        }

        @JvmStatic
        fun createDialog(): ASAlertDialog {
            return ASAlertDialog()
        }

        fun hideAlert(paramString: String?) {
            val aSAlertDialog: ASAlertDialog? = _alerts[paramString]
            aSAlertDialog?.dismiss()
        }

        @JvmStatic
        fun showErrorDialog(errMessage: String?, aSViewController: ASViewController?) {
            createDialog()
                .setTitle("錯誤")
                .setMessage(errMessage)
                .addButton("確定")
                .setListener { aDialog: ASAlertDialog?, index: Int -> aDialog!!.dismiss() }
                .scheduleDismissOnPageDisappear(aSViewController)
                .show()
        }
    }
} /* Location:              C:\Users\kodak\Downloads\反編譯\dex-tools-v2.4\classes-dex2jar.jar!\com\kumi\ASFramework\Dialog\ASAlertDialog.class
 * Java compiler version: 6 (50.0)
 * JD-Core Version:       1.1.3
 */


