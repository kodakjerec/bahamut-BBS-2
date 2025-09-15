package com.kota.ASFramework.Dialog

import android.graphics.Typeface
import android.util.TypedValue
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.LinearLayout
import android.widget.TextView
import com.kota.ASFramework.PageController.ASViewController
import com.kota.Bahamut.R
import java.util.*

class ASAlertDialog : ASDialog, View.OnClickListener {
    companion object {
        private val _alerts = HashMap<String, ASAlertDialog>()
        
        fun containsAlert(alertId: String?): Boolean {
            return alertId?.let { _alerts.containsKey(it) } ?: false
        }
        
        fun create(alertId: String): ASAlertDialog {
            val existingAlert = _alerts[alertId]
            return if (existingAlert == null) {
                ASAlertDialog(alertId)
            } else {
                existingAlert.clear()
                existingAlert
            }
        }
        
        fun createDialog(): ASAlertDialog {
            return ASAlertDialog()
        }
        
        fun hideAlert(alertId: String) {
            _alerts[alertId]?.dismiss()
        }
        
        fun showErrorDialog(errMessage: String, bahamutController: ASViewController) {
            createDialog()
                .setTitle("錯誤")
                .setMessage(errMessage)
                .addButton("確定")
                .setListener { dialog, _ -> dialog.dismiss() }
                .scheduleDismissOnPageDisappear(bahamutController)
                .show()
        }
    }
    
    private var _alert_id: String? = null
    private val _item_list = Vector<Button>()
    private var _listener: ASAlertDialogListener? = null
    private var _message_label: TextView? = null
    private var _title_label: TextView? = null
    private lateinit var _toolbar: LinearLayout
    private var _default_index = -1
    
    constructor() {
        initial()
    }
    
    constructor(alertId: String) {
        initial()
        _alert_id = alertId
    }
    
    private fun buildContentView(): View {
        val displayMetrics = context.resources.displayMetrics
        val layoutParams = ASLayoutParams.getInstance()
        
        val width = TypedValue.applyDimension(
            TypedValue.COMPLEX_UNIT_DIP, 
            layoutParams.getDialogWidthNormal(), 
            displayMetrics
        ).toInt()
        
        val minHeight = TypedValue.applyDimension(
            TypedValue.COMPLEX_UNIT_DIP, 
            100.0f, 
            displayMetrics
        ).toInt()
        
        val largePadding = TypedValue.applyDimension(
            TypedValue.COMPLEX_UNIT_DIP, 
            6.0f, 
            displayMetrics
        ).toInt()
        
        val mediumPadding = TypedValue.applyDimension(
            TypedValue.COMPLEX_UNIT_DIP, 
            3.0f, 
            displayMetrics
        ).toInt()
        
        val smallPadding = TypedValue.applyDimension(
            TypedValue.COMPLEX_UNIT_DIP, 
            1.0f, 
            displayMetrics
        ).toInt()
        
        // 外層容器
        val outerLayout = LinearLayout(context).apply {
            orientation = LinearLayout.VERTICAL
            setPadding(mediumPadding, mediumPadding, mediumPadding, mediumPadding)
            setBackgroundResource(R.color.dialog_border_color)
        }
        
        // 內層容器
        val innerLayout = LinearLayout(context).apply {
            orientation = LinearLayout.VERTICAL
            setPadding(smallPadding, smallPadding, smallPadding, smallPadding)
            setBackgroundColor(-16777216) // 黑色背景
        }
        outerLayout.addView(innerLayout)
        
        // 標題
        _title_label = TextView(context).apply {
            layoutParams = LinearLayout.LayoutParams(width, ViewGroup.LayoutParams.WRAP_CONTENT)
            setPadding(largePadding, largePadding, largePadding, largePadding)
            setTextSize(TypedValue.COMPLEX_UNIT_SP, layoutParams.getTextSizeUltraLarge())
            setTextColor(-1) // 白色文字
            setTypeface(typeface, Typeface.BOLD)
            visibility = View.GONE
            setBackgroundColor(-15724528)
            isSingleLine = true
        }
        innerLayout.addView(_title_label)
        
        // 訊息
        _message_label = TextView(context).apply {
            layoutParams = LinearLayout.LayoutParams(width, ViewGroup.LayoutParams.WRAP_CONTENT)
            setPadding(largePadding, largePadding, largePadding, largePadding)
            setTextSize(TypedValue.COMPLEX_UNIT_SP, layoutParams.getTextSizeLarge())
            minimumHeight = minHeight
            setTextColor(-1) // 白色文字
            visibility = View.GONE
            setBackgroundColor(-16777216) // 黑色背景
        }
        innerLayout.addView(_message_label)
        
        // 工具列
        _toolbar = LinearLayout(context).apply {
            layoutParams = LinearLayout.LayoutParams(width, ViewGroup.LayoutParams.WRAP_CONTENT)
            gravity = 17 // CENTER
            orientation = LinearLayout.HORIZONTAL
        }
        innerLayout.addView(_toolbar)
        
        return outerLayout
    }
    
    private fun clear() {
        _message_label?.text = ""
        _title_label?.text = ""
        _toolbar.removeAllViews()
        _item_list.clear()
    }
    
    private fun createButton(): Button {
        val displayMetrics = context.resources.displayMetrics
        val layoutParams = ASLayoutParams.getInstance()
        
        val smallPadding = TypedValue.applyDimension(
            TypedValue.COMPLEX_UNIT_DIP, 
            3.0f, 
            displayMetrics
        ).toInt()
        
        val mediumPadding = TypedValue.applyDimension(
            TypedValue.COMPLEX_UNIT_DIP, 
            5.0f, 
            displayMetrics
        ).toInt()
        
        val minHeight = TypedValue.applyDimension(
            TypedValue.COMPLEX_UNIT_DIP, 
            layoutParams.getDefaultTouchBlockHeight(), 
            displayMetrics
        ).toInt()
        
        return Button(context).apply {
            this.layoutParams = LinearLayout.LayoutParams(
                ViewGroup.LayoutParams.MATCH_PARENT,
                ViewGroup.LayoutParams.WRAP_CONTENT,
                1.0f
            )
            setPadding(mediumPadding, smallPadding, mediumPadding, smallPadding)
            setTextSize(TypedValue.COMPLEX_UNIT_SP, layoutParams.getTextSizeLarge())
            minimumHeight = minHeight
            gravity = 17 // CENTER
            setOnClickListener(this@ASAlertDialog)
            background = layoutParams.getAlertItemBackgroundDrawable()
            isSingleLine = false
            setTextColor(layoutParams.getAlertItemTextColor())
        }
    }
    
    fun createDivider(): View {
        val dividerWidth = TypedValue.applyDimension(
            TypedValue.COMPLEX_UNIT_DIP, 
            1.0f, 
            context.resources.displayMetrics
        ).toInt()
        
        return View(context).apply {
            layoutParams = LinearLayout.LayoutParams(
                dividerWidth, 
                ViewGroup.LayoutParams.MATCH_PARENT
            )
            setBackgroundColor(-16777216) // 黑色
        }
    }
    
    private fun initial() {
        requestWindowFeature(1)
        setContentView(buildContentView())
        window?.setBackgroundDrawable(null)
    }
    
    fun addButton(buttonText: String?): ASAlertDialog {
        buttonText?.let { text ->
            if (_item_list.size > 0) {
                _toolbar.addView(createDivider())
            }
            
            val button = createButton().apply {
                this.text = text
                val textSize = if (text.length < 4) {
                    ASLayoutParams.getInstance().getTextSizeLarge()
                } else {
                    ASLayoutParams.getInstance().getTextSizeNormal()
                }
                setTextSize(TypedValue.COMPLEX_UNIT_SP, textSize)
                setOnClickListener(this@ASAlertDialog)
            }
            
            _toolbar.addView(button)
            _item_list.add(button)
        }
        return this
    }
    
    override fun dismiss() {
        _alert_id?.let { _alerts.remove(it) }
        super.dismiss()
    }
    
    override fun onClick(view: View) {
        _listener?.let { listener ->
            val index = _item_list.indexOf(view)
            listener.onAlertDialogDismissWithButtonIndex(this, index)
        }
        dismiss()
    }
    
    fun setItemTitle(index: Int, title: String): ASAlertDialog {
        if (index >= 0 && index < _item_list.size) {
            _item_list[index].text = title
        }
        return this
    }
    
    fun setListener(listener: ASAlertDialogListener?): ASAlertDialog {
        _listener = listener
        return this
    }
    
    fun setMessage(message: String?): ASAlertDialog {
        if (message == null) {
            _message_label?.visibility = View.GONE
        } else {
            _message_label?.apply {
                visibility = View.VISIBLE
                text = message
            }
        }
        return this
    }
    
    fun setTitle(title: String?): ASAlertDialog {
        if (title == null) {
            _title_label?.visibility = View.GONE
        } else {
            _title_label?.apply {
                visibility = View.VISIBLE
                text = title
            }
        }
        return this
    }
    
    override fun show() {
        _alert_id?.let { alertId ->
            val existingAlert = _alerts[alertId]
            if (existingAlert?.isShowing == true) {
                existingAlert.dismiss()
            }
            _alerts[alertId] = this
        }
        super.show()
    }
    
    // 設定都不按的時候, 是否傳回預設值
    // 預設不傳
    fun setDefaultButtonIndex(index: Int): ASAlertDialog {
        _default_index = index
        return this
    }
    
    override fun cancel() {
        if (_default_index > -1) {
            _listener?.onAlertDialogDismissWithButtonIndex(this, _default_index)
        }
        super.cancel()
    }
}
