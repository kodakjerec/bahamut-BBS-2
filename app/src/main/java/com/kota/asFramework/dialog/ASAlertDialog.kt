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
    private var _alert_id: String? = null

    private val _item_list = Vector<Button?>()

    private var _listener: ASAlertDialogListener? = null

    private var _message_label: TextView? = null

    private var _title_label: TextView? = null

    private var _toolbar: LinearLayout? = null
    private var _default_index = -1

    constructor() {
        initial()
    }

    constructor(paramString: String?) {
        initial()
        _alert_id = paramString
    }

    private fun buildContentView(): View {
        val n = TypedValue.applyDimension(
            TypedValue.COMPLEX_UNIT_DIP,
            ASLayoutParams.Companion.getInstance().getDialogWidthNormal(),
            getContext().getResources().getDisplayMetrics()
        ).toInt()
        val i = TypedValue.applyDimension(
            TypedValue.COMPLEX_UNIT_DIP,
            100.0f,
            getContext().getResources().getDisplayMetrics()
        ).toInt()
        val k = ceil(
            TypedValue.applyDimension(
                TypedValue.COMPLEX_UNIT_DIP,
                6.0f,
                getContext().getResources().getDisplayMetrics()
            ).toDouble()
        ).toInt()
        val j = ceil(
            TypedValue.applyDimension(
                TypedValue.COMPLEX_UNIT_DIP,
                3.0f,
                getContext().getResources().getDisplayMetrics()
            ).toDouble()
        ).toInt()
        val m = ceil(
            TypedValue.applyDimension(
                TypedValue.COMPLEX_UNIT_DIP,
                1.0f,
                getContext().getResources().getDisplayMetrics()
            ).toDouble()
        ).toInt()
        val linearLayout2 = LinearLayout(getContext())
        linearLayout2.setOrientation(LinearLayout.VERTICAL)
        linearLayout2.setPadding(j, j, j, j)
        linearLayout2.setBackgroundResource(R.color.dialog_border_color)
        val linearLayout1 = LinearLayout(getContext())
        linearLayout1.setOrientation(LinearLayout.VERTICAL)
        linearLayout1.setPadding(m, m, m, m)
        linearLayout1.setBackgroundColor(-16777216)
        linearLayout2.addView(linearLayout1 as View)
        _title_label = TextView(getContext())
        _title_label!!.setLayoutParams(
            LinearLayout.LayoutParams(
                n,
                ViewGroup.LayoutParams.WRAP_CONTENT
            ) as ViewGroup.LayoutParams
        )
        _title_label!!.setPadding(k, k, k, k)
        _title_label!!.setTextSize(
            2,
            ASLayoutParams.Companion.getInstance().getTextSizeUltraLarge()
        )
        _title_label!!.setTextColor(-1)
        _title_label!!.setTypeface(_title_label!!.getTypeface(), Typeface.BOLD)
        _title_label!!.setVisibility(View.GONE)
        _title_label!!.setBackgroundColor(-15724528)
        _title_label!!.setSingleLine(true)
        linearLayout1.addView(_title_label as View?)
        _message_label = TextView(getContext())
        _message_label!!.setLayoutParams(
            LinearLayout.LayoutParams(
                n,
                ViewGroup.LayoutParams.WRAP_CONTENT
            ) as ViewGroup.LayoutParams
        )
        _message_label!!.setPadding(k, k, k, k)
        _message_label!!.setTextSize(2, ASLayoutParams.Companion.getInstance().getTextSizeLarge())
        _message_label!!.setMinimumHeight(i)
        _message_label!!.setTextColor(-1)
        _message_label!!.setVisibility(View.GONE)
        _message_label!!.setBackgroundColor(-16777216)
        linearLayout1.addView(_message_label as View?)
        _toolbar = LinearLayout(getContext())
        _toolbar!!.setLayoutParams(
            LinearLayout.LayoutParams(
                n,
                ViewGroup.LayoutParams.WRAP_CONTENT
            ) as ViewGroup.LayoutParams
        )
        _toolbar!!.setGravity(17)
        _toolbar!!.setOrientation(LinearLayout.HORIZONTAL)
        linearLayout1.addView(_toolbar as View?)
        return linearLayout2 as View
    }

    private fun clear() {
        if (_message_label != null) _message_label!!.setText("")
        if (_title_label != null) _title_label!!.setText("")
        _toolbar!!.removeAllViews()
        _item_list.clear()
    }

    private fun createButton(): Button {
        val button = Button(getContext())
        button.setLayoutParams(
            LinearLayout.LayoutParams(
                ViewGroup.LayoutParams.MATCH_PARENT,
                ViewGroup.LayoutParams.WRAP_CONTENT,
                1.0f
            ) as ViewGroup.LayoutParams
        )
        val j = TypedValue.applyDimension(
            TypedValue.COMPLEX_UNIT_DIP,
            3.0f,
            getContext().getResources().getDisplayMetrics()
        ).toInt()
        val i = TypedValue.applyDimension(
            TypedValue.COMPLEX_UNIT_DIP,
            5.0f,
            getContext().getResources().getDisplayMetrics()
        ).toInt()
        button.setPadding(i, j, i, j)
        button.setTextSize(2, ASLayoutParams.Companion.getInstance().getTextSizeLarge())
        button.setMinimumHeight(
            TypedValue.applyDimension(
                TypedValue.COMPLEX_UNIT_DIP,
                ASLayoutParams.Companion.getInstance().getDefaultTouchBlockHeight(),
                getContext().getResources().getDisplayMetrics()
            ).toInt()
        )
        button.setGravity(17)
        button.setOnClickListener(this)
        button.setBackground(
            ASLayoutParams.Companion.getInstance().getAlertItemBackgroundDrawable()
        )
        button.setSingleLine(false)
        button.setTextColor(ASLayoutParams.Companion.getInstance().getAlertItemTextColor())
        return button
    }

    private fun initial() {
        requestWindowFeature(1)
        setContentView(buildContentView())
        getWindow()!!.setBackgroundDrawable(null)
    }

    fun addButton(paramString: String?): ASAlertDialog {
        if (paramString != null) {
            if (_item_list.size > 0) _toolbar!!.addView(createDivider())
            val button = createButton()
            _toolbar!!.addView(button as View)
            button.setText(paramString)
            if (paramString.length < 4) {
                button.setTextSize(2, ASLayoutParams.Companion.getInstance().getTextSizeLarge())
            } else {
                button.setTextSize(2, ASLayoutParams.Companion.getInstance().getTextSizeNormal())
            }
            button.setOnClickListener(this)
            _item_list.add(button)
        }
        return this
    }

    fun createDivider(): View {
        val view = View(getContext())
        view.setLayoutParams(
            LinearLayout.LayoutParams(
                ceil(
                    TypedValue.applyDimension(
                        TypedValue.COMPLEX_UNIT_DIP,
                        1.0f,
                        getContext().getResources().getDisplayMetrics()
                    ).toDouble()
                ).toInt(), ViewGroup.LayoutParams.MATCH_PARENT
            ) as ViewGroup.LayoutParams
        )
        view.setBackgroundColor(-16777216)
        return view
    }

    override fun dismiss() {
        if (_alert_id != null) _alerts.remove(_alert_id)
        super.dismiss()
    }

    override fun onClick(paramView: View?) {
        if (_listener != null) {
            val i = _item_list.indexOf(paramView)
            _listener!!.onAlertDialogDismissWithButtonIndex(this, i)
        }
        dismiss()
    }

    fun setItemTitle(paramInt: Int, paramString: String?): ASAlertDialog {
        if (paramInt >= 0 && paramInt < _item_list.size) (_item_list.get(paramInt) as Button).setText(
            paramString
        )
        return this
    }

    fun setListener(paramASAlertDialogListener: ASAlertDialogListener?): ASAlertDialog {
        _listener = paramASAlertDialogListener
        return this
    }

    fun setMessage(paramString: String?): ASAlertDialog {
        if (paramString == null) {
            _message_label!!.setVisibility(View.GONE)
            return this
        }
        _message_label!!.setVisibility(View.VISIBLE)
        _message_label!!.setText(paramString)
        return this
    }

    fun setTitle(paramString: String?): ASAlertDialog {
        if (paramString == null) {
            _title_label!!.setVisibility(View.GONE)
            return this
        }
        _title_label!!.setVisibility(View.VISIBLE)
        _title_label!!.setText(paramString)
        return this
    }

    override fun show() {
        if (_alert_id != null) {
            val aSAlertDialog: ASAlertDialog? = _alerts.get(_alert_id)
            if (aSAlertDialog != null && aSAlertDialog.isShowing()) aSAlertDialog.dismiss()
            _alerts.put(_alert_id, this)
        }
        super.show()
    }

    // 設定都不按的時候, 是否傳回預設值
    // 預設不傳
    fun setDefaultButtonIndex(i: Int): ASAlertDialog {
        _default_index = i
        return this
    }

    override fun cancel() {
        if (_default_index > -1) {
            _listener!!.onAlertDialogDismissWithButtonIndex(this, _default_index)
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
            val aSAlertDialog: ASAlertDialog? = _alerts.get(paramString)
            if (aSAlertDialog == null) return ASAlertDialog(paramString)
            aSAlertDialog.clear()
            return aSAlertDialog
        }

        @JvmStatic
        fun createDialog(): ASAlertDialog {
            return ASAlertDialog()
        }

        fun hideAlert(paramString: String?) {
            val aSAlertDialog: ASAlertDialog? = _alerts.get(paramString)
            if (aSAlertDialog != null) aSAlertDialog.dismiss()
        }

        @JvmStatic
        fun showErrorDialog(err_message: String?, bahamutController: ASViewController?) {
            createDialog()
                .setTitle("錯誤")
                .setMessage(err_message)
                .addButton("確定")
                .setListener(ASAlertDialogListener { aDialog: ASAlertDialog?, index: Int -> aDialog!!.dismiss() })
                .scheduleDismissOnPageDisappear(bahamutController)
                .show()
        }
    }
} /* Location:              C:\Users\kodak\Downloads\反編譯\dex-tools-v2.4\classes-dex2jar.jar!\com\kumi\ASFramework\Dialog\ASAlertDialog.class
 * Java compiler version: 6 (50.0)
 * JD-Core Version:       1.1.3
 */


