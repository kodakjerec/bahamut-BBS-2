package com.kota.ASFramework.Dialog

import android.annotation.SuppressLint
import android.util.TypedValue
import android.view.Gravity
import android.view.View
import android.view.ViewGroup
import android.widget.*
import com.kota.Bahamut.R
import java.util.*

class ASListDialog : ASDialog() {
    companion object {
        const val SIZE_NORMAL = 0
        const val SIZE_LARGE = 1
        
        fun createDialog(): ASListDialog {
            return ASListDialog()
        }
    }
    
    private data class ASListDialogItem(
        var button: Button? = null,
        var title: String? = null
    )
    
    private var _content_view: LinearLayout? = null
    private var _item_block: LinearLayout? = null
    private var _listener: ASListDialogItemClickListener? = null
    private val _item_list = Vector<ASListDialogItem>()
    private var _title_label: TextView? = null
    private var _dialog_width = 280.0f
    private var _scroll_view: ScrollView? = null
    private var _item_text_size = 1
    
    override fun getName(): String {
        return "ListDialog"
    }
    
    init {
        requestWindowFeature(1)
        setContentView(buildContentView())
        window?.setBackgroundDrawable(null)
    }
    
    @SuppressLint("ResourceAsColor")
    private fun buildContentView(): View {
        val displayMetrics = context.resources.displayMetrics
        
        val framePadding = TypedValue.applyDimension(
            TypedValue.COMPLEX_UNIT_DIP, 
            3.0f, 
            displayMetrics
        ).toInt()
        
        val padding = TypedValue.applyDimension(
            TypedValue.COMPLEX_UNIT_DIP, 
            5.0f, 
            displayMetrics
        ).toInt()
        
        // 外框
        val frame = LinearLayout(context).apply {
            setBackgroundResource(R.color.dialog_border_color)
            setPadding(framePadding, framePadding, framePadding, framePadding)
        }
        
        // 內容視圖
        val contentView = LinearLayout(context).apply {
            layoutParams = LinearLayout.LayoutParams(
                ViewGroup.LayoutParams.WRAP_CONTENT, 
                ViewGroup.LayoutParams.WRAP_CONTENT
            )
            setBackgroundColor(View.MEASURED_STATE_MASK)
            orientation = LinearLayout.VERTICAL
        }
        frame.addView(contentView)
        
        val dialogWidth = ((TypedValue.applyDimension(
            TypedValue.COMPLEX_UNIT_DIP, 
            _dialog_width, 
            displayMetrics
        ).toInt()) / 2) * 2
        
        // 滾動視圖
        _scroll_view = ScrollView(context).apply {
            layoutParams = LinearLayout.LayoutParams(dialogWidth, ViewGroup.LayoutParams.WRAP_CONTENT)
        }
        contentView.addView(_scroll_view)
        
        // 內容容器
        _content_view = LinearLayout(context).apply {
            layoutParams = FrameLayout.LayoutParams(dialogWidth, ViewGroup.LayoutParams.WRAP_CONTENT)
            orientation = LinearLayout.VERTICAL
            gravity = Gravity.CENTER
        }
        _scroll_view?.addView(_content_view)
        
        // 標題
        _title_label = TextView(context).apply {
            layoutParams = LinearLayout.LayoutParams(
                ViewGroup.LayoutParams.MATCH_PARENT, 
                ViewGroup.LayoutParams.WRAP_CONTENT
            )
            setPadding(padding, padding, padding, padding)
            setTextColor(-1) // 白色
            setTextSize(TypedValue.COMPLEX_UNIT_SP, ASLayoutParams.getInstance().getTextSizeLarge())
            text = "選項"
            setBackgroundColor(-14671840)
            gravity = Gravity.CENTER
        }
        _content_view?.addView(_title_label)
        
        // 項目區塊
        _item_block = LinearLayout(context).apply {
            layoutParams = FrameLayout.LayoutParams(dialogWidth, ViewGroup.LayoutParams.WRAP_CONTENT)
            orientation = LinearLayout.VERTICAL
            gravity = Gravity.CENTER
        }
        _content_view?.addView(_item_block)
        
        return frame
    }
    
    fun setItemTextSize(size: Int): ASListDialog {
        _item_text_size = size
        return this
    }
    
    fun setListener(listener: ASListDialogItemClickListener?): ASListDialog {
        _listener = listener
        return this
    }
    
    fun setTitle(title: String): ASListDialog {
        _title_label?.text = title
        return this
    }
    
    fun addItems(itemList: Array<String>): ASListDialog {
        itemList.forEach { addItem(it) }
        return this
    }
    
    fun addItem(itemTitle: String?): ASListDialog {
        val button = createButton()
        button.setOnClickListener { onItemClicked(it as Button) }
        button.setOnLongClickListener { onItemLongClicked(it as Button) }
        
        if (itemTitle == null) {
            button.visibility = View.GONE
        } else {
            if (_item_list.size > 0) {
                _item_block?.addView(createDivider())
            }
            button.text = itemTitle
        }
        
        _item_block?.addView(button)
        
        val item = ASListDialogItem(button, itemTitle)
        _item_list.add(item)
        return this
    }
    
    fun createDivider(): View {
        val dividerHeight = TypedValue.applyDimension(
            TypedValue.COMPLEX_UNIT_DIP, 
            1.0f, 
            context.resources.displayMetrics
        ).toInt()
        
        return View(context).apply {
            layoutParams = LinearLayout.LayoutParams(-1, dividerHeight)
            setBackgroundColor(-2130706433)
        }
    }
    
    private fun createButton(): Button {
        val layoutParams = ASLayoutParams.getInstance()
        val minHeight = TypedValue.applyDimension(
            TypedValue.COMPLEX_UNIT_DIP, 
            60.0f, 
            context.resources.displayMetrics
        ).toInt()
        
        return Button(context).apply {
            this.layoutParams = LinearLayout.LayoutParams(
                ViewGroup.LayoutParams.MATCH_PARENT, 
                ViewGroup.LayoutParams.WRAP_CONTENT
            )
            minimumHeight = minHeight
            gravity = Gravity.CENTER
            
            val textSize = when (_item_text_size) {
                SIZE_NORMAL -> layoutParams.getTextSizeNormal()
                SIZE_LARGE -> layoutParams.getTextSizeLarge()
                2 -> layoutParams.getTextSizeUltraLarge()
                else -> layoutParams.getTextSizeNormal()
            }
            setTextSize(TypedValue.COMPLEX_UNIT_SP, textSize)
            
            background = layoutParams.getListItemBackgroundDrawable()
            setTextColor(layoutParams.getListItemTextColor())
            isSingleLine = true
        }
    }
    
    private fun indexOfButton(button: Button): Int {
        for (i in _item_list.indices) {
            if (_item_list[i].button == button) {
                return i
            }
        }
        return -1
    }
    
    private fun onItemClicked(button: Button) {
        _listener?.let { listener ->
            val index = indexOfButton(button)
            if (index != -1) {
                listener.onListDialogItemClicked(this, index, _item_list[index].title)
            }
            dismiss()
        }
    }
    
    private fun onItemLongClicked(button: Button): Boolean {
        var result = false
        _listener?.let { listener ->
            val index = indexOfButton(button)
            if (index != -1) {
                result = listener.onListDialogItemLongClicked(this, index, _item_list[index].title)
            }
        }
        if (result) {
            dismiss()
        }
        return result
    }
    
    fun setDialogWidth(width: Float): ASListDialog {
        _dialog_width = width
        val dialogWidth = ((TypedValue.applyDimension(
            TypedValue.COMPLEX_UNIT_DIP, 
            _dialog_width, 
            context.resources.displayMetrics
        ).toInt()) / 2) * 2
        
        _scroll_view?.layoutParams = LinearLayout.LayoutParams(
            dialogWidth, 
            ViewGroup.LayoutParams.WRAP_CONTENT
        )
        _content_view?.layoutParams = FrameLayout.LayoutParams(
            dialogWidth, 
            ViewGroup.LayoutParams.WRAP_CONTENT
        )
        return this
    }
}
