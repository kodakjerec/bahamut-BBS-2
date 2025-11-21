package com.kota.asFramework.dialog

import android.annotation.SuppressLint
import android.util.TypedValue
import android.view.Gravity
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.FrameLayout
import android.widget.LinearLayout
import android.widget.ScrollView
import android.widget.TextView
import com.kota.Bahamut.R
import java.util.Vector
import kotlin.math.ceil

class ASListDialog : ASDialog() {
    private var contentView: LinearLayout? = null
    private var itemBlock: LinearLayout? = null
    private var listener: ASListDialogItemClickListener? = null
    private val itemList: Vector<ASListDialogItem> = Vector<ASListDialogItem>()
    private var titleLabel: TextView? = null
    private var dialogWidth = 280.0f
    private var scrollView: ScrollView? = null
    private var itemTextSize = 1

    private class ASListDialogItem {
        var button: Button? = null
        var title: String? = null
    }

    fun setItemTextSize(size: Int): ASListDialog {
        this.itemTextSize = size
        return this
    }

    override val name: String?
        get() = "ListDialog"

    init {
        requestWindowFeature(1)
        setContentView(buildContentView())
        window?.setBackgroundDrawable(null)
    }

    @SuppressLint("ResourceAsColor")
    private fun buildContentView(): View {
        val framePadding = TypedValue.applyDimension(
            TypedValue.COMPLEX_UNIT_DIP,
            3.0f,
            context.resources.displayMetrics
        ).toInt()
        val padding = TypedValue.applyDimension(
            TypedValue.COMPLEX_UNIT_DIP,
            5.0f,
            context.resources.displayMetrics
        ).toInt()
        val frame = LinearLayout(context)
        frame.setBackgroundResource(R.color.dialog_border_color)
        frame.setPadding(framePadding, framePadding, framePadding, framePadding)
        val contentView = LinearLayout(context)
        contentView.layoutParams = LinearLayout.LayoutParams(
            ViewGroup.LayoutParams.WRAP_CONTENT,
            ViewGroup.LayoutParams.WRAP_CONTENT
        )
        contentView.setBackgroundColor(View.MEASURED_STATE_MASK)
        frame.addView(contentView)
        contentView.orientation = LinearLayout.VERTICAL
        val dialogWidth = ((TypedValue.applyDimension(
            TypedValue.COMPLEX_UNIT_DIP,
            this.dialogWidth,
            context.resources.displayMetrics
        ).toInt()) / 2) * 2
        this.scrollView = ScrollView(context)
        this.scrollView?.layoutParams = LinearLayout.LayoutParams(
            dialogWidth,
            ViewGroup.LayoutParams.WRAP_CONTENT
        )
        contentView.addView(this.scrollView)
        this.contentView = LinearLayout(context)
        this.contentView?.layoutParams = FrameLayout.LayoutParams(
            dialogWidth,
            ViewGroup.LayoutParams.WRAP_CONTENT
        )
        this.contentView?.orientation = LinearLayout.VERTICAL
        this.contentView?.gravity = Gravity.CENTER
        this.scrollView?.addView(this.contentView)
        this.titleLabel = TextView(context)
        this.titleLabel?.layoutParams = LinearLayout.LayoutParams(
            ViewGroup.LayoutParams.MATCH_PARENT,
            ViewGroup.LayoutParams.WRAP_CONTENT
        )
        this.titleLabel?.setPadding(padding, padding, padding, padding)
        this.titleLabel?.setTextColor(-1)
        this.titleLabel?.setTextSize(
            2,
            ASLayoutParams.instance.textSizeLarge
        )
        this.titleLabel?.text = "選項"
        this.titleLabel?.setBackgroundColor(-14671840)
        this.titleLabel?.gravity = Gravity.CENTER
        this.contentView?.addView(this.titleLabel)
        this.itemBlock = LinearLayout(context)
        this.itemBlock?.layoutParams = FrameLayout.LayoutParams(
            dialogWidth,
            ViewGroup.LayoutParams.WRAP_CONTENT
        )
        this.itemBlock?.orientation = LinearLayout.VERTICAL
        this.itemBlock?.gravity = Gravity.CENTER
        this.contentView?.addView(this.itemBlock)
        return frame
    }

    fun setListener(aListener: ASListDialogItemClickListener?): ASListDialog {
        this.listener = aListener
        return this
    }

    fun setTitle(aTitle: String?): ASListDialog {
        if (this.titleLabel != null) {
            this.titleLabel?.text = aTitle
        }
        return this
    }

    fun addItems(aItemList: Array<String>): ASListDialog {
        for (itemTitle in aItemList) {
            addItem(itemTitle)
        }
        return this
    }

    fun addItem(aItemTitle: String?): ASListDialog {
        val button = createButton()
        button.setOnClickListener { v: View? ->
            this@ASListDialog.onItemClicked(
                v as Button?
            )
        }
        button.setOnLongClickListener { v: View? ->
            this@ASListDialog.onItemLongClicked(
                v as Button?
            )
        }
        if (aItemTitle == null) {
            button.visibility = View.GONE
        } else {
            if (this.itemList.isNotEmpty()) {
                this.itemBlock?.addView(createDivider())
            }
            button.text = aItemTitle
        }
        this.itemBlock?.addView(button)
        val item = ASListDialogItem()
        item.button = button
        item.title = aItemTitle
        this.itemList.add(item)
        return this
    }

    fun createDivider(): View {
        val divider = View(context)
        val dividerHeight = ceil(
            TypedValue.applyDimension(
                TypedValue.COMPLEX_UNIT_DIP,
                1.0f,
                context.resources.displayMetrics
            ).toDouble()
        ).toInt()
        divider.layoutParams = LinearLayout.LayoutParams(-1, dividerHeight)
        divider.setBackgroundColor(-2130706433)
        return divider
    }

    private fun createButton(): Button {
        val button = Button(context)
        button.layoutParams = LinearLayout.LayoutParams(
            ViewGroup.LayoutParams.MATCH_PARENT,
            ViewGroup.LayoutParams.WRAP_CONTENT
        )
        button.minimumHeight = TypedValue.applyDimension(
            TypedValue.COMPLEX_UNIT_DIP,
            60.0f,
            context.resources.displayMetrics
        ).toInt()
        button.gravity = Gravity.CENTER
        when (this.itemTextSize) {
            0 -> {
                button.setTextSize(2, ASLayoutParams.instance.textSizeNormal)
            }
            1 -> {
                button.setTextSize(2, ASLayoutParams.instance.textSizeLarge)
            }
            2 -> {
                button.setTextSize(2, ASLayoutParams.instance.textSizeUltraLarge)
            }
        }
        button.background = ASLayoutParams.instance .listItemBackgroundDrawable
        button.setTextColor(ASLayoutParams.instance.listItemTextColor)
        button.isSingleLine = true
        return button
    }

    private fun indexOfButton(aButton: Button?): Int {
        for (i in this.itemList.indices) {
            val item = this.itemList[i]
            if (item.button === aButton) {
                val index = i
                return index
            }
        }
        return -1
    }

    private fun onItemClicked(button: Button?) {
        if (this.listener != null) {
            val index = indexOfButton(button)
            if (index != -1) {
                this.listener?.onListDialogItemClicked(
                    this,
                    index,
                    this.itemList[index].title
                )
            }
            dismiss()
        }
    }

    private fun onItemLongClicked(button: Button?): Boolean {
        var index = 0
        var result = false
        if (this.listener != null && (indexOfButton(button).also { index = it }) != -1) {
            result = this.listener?.onListDialogItemLongClicked(
                this,
                index,
                this.itemList[index].title
            ) == true
        }
        if (result) {
            dismiss()
        }
        return result
    }

    fun setDialogWidth(width: Float): ASListDialog {
        this.dialogWidth = width
        val dialogWidth = ((TypedValue.applyDimension(
            TypedValue.COMPLEX_UNIT_DIP,
            this.dialogWidth,
            context.resources.displayMetrics
        ).toInt()) / 2) * 2
        this.scrollView?.layoutParams = LinearLayout.LayoutParams(
            dialogWidth,
            ViewGroup.LayoutParams.WRAP_CONTENT
        )
        this.contentView?.layoutParams = FrameLayout.LayoutParams(
            dialogWidth,
            ViewGroup.LayoutParams.WRAP_CONTENT
        )
        return this
    }

    companion object {
        const val SIZE_LARGE: Int = 1
        const val SIZE_NORMAL: Int = 0
        @JvmStatic
        fun createDialog(): ASListDialog {
            return ASListDialog()
        }
    }
}