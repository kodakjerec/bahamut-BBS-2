package com.kota.asFramework.dialog

import android.annotation.SuppressLint
import android.util.TypedValue
import android.view.Gravity
import android.view.View
import android.view.View.OnLongClickListener
import android.view.ViewGroup
import android.widget.Button
import android.widget.FrameLayout
import android.widget.LinearLayout
import android.widget.ScrollView
import android.widget.TextView
import com.kota.Bahamut.R
import java.util.Vector
import kotlin.math.ceil

/* loaded from: classes.dex */
class ASListDialog : ASDialog() {
    private var _content_view: LinearLayout? = null
    private var _item_block: LinearLayout? = null
    private var _listener: ASListDialogItemClickListener? = null
    private val _item_list: Vector<ASListDialogItem> = Vector<ASListDialogItem>()
    private var _title_label: TextView? = null
    private var _dialog_width = 280.0f
    private var _scroll_view: ScrollView? = null
    private var _item_text_size = 1

    private class ASListDialogItem {
        var button: Button? = null
        var title: String? = null
    }

    fun setItemTextSize(size: Int): ASListDialog {
        this._item_text_size = size
        return this
    }

    // com.kota.ASFramework.Dialog.ASDialog
    override fun getName(): String {
        return "ListDialog"
    }

    init {
        requestWindowFeature(1)
        setContentView(buildContentView())
        getWindow()!!.setBackgroundDrawable(null)
    }

    @SuppressLint("ResourceAsColor")
    private fun buildContentView(): View {
        val frame_padding = TypedValue.applyDimension(
            TypedValue.COMPLEX_UNIT_DIP,
            3.0f,
            getContext().getResources().getDisplayMetrics()
        ).toInt()
        val padding = TypedValue.applyDimension(
            TypedValue.COMPLEX_UNIT_DIP,
            5.0f,
            getContext().getResources().getDisplayMetrics()
        ).toInt()
        val frame = LinearLayout(getContext())
        frame.setBackgroundResource(R.color.dialog_border_color)
        frame.setPadding(frame_padding, frame_padding, frame_padding, frame_padding)
        val content_view = LinearLayout(getContext())
        content_view.setLayoutParams(
            LinearLayout.LayoutParams(
                ViewGroup.LayoutParams.WRAP_CONTENT,
                ViewGroup.LayoutParams.WRAP_CONTENT
            )
        )
        content_view.setBackgroundColor(View.MEASURED_STATE_MASK)
        frame.addView(content_view)
        content_view.setOrientation(LinearLayout.VERTICAL)
        val dialog_width = ((TypedValue.applyDimension(
            TypedValue.COMPLEX_UNIT_DIP,
            this._dialog_width,
            getContext().getResources().getDisplayMetrics()
        ).toInt()) / 2) * 2
        this._scroll_view = ScrollView(getContext())
        this._scroll_view!!.setLayoutParams(
            LinearLayout.LayoutParams(
                dialog_width,
                ViewGroup.LayoutParams.WRAP_CONTENT
            )
        )
        content_view.addView(this._scroll_view)
        this._content_view = LinearLayout(getContext())
        this._content_view!!.setLayoutParams(
            FrameLayout.LayoutParams(
                dialog_width,
                ViewGroup.LayoutParams.WRAP_CONTENT
            )
        )
        this._content_view!!.setOrientation(LinearLayout.VERTICAL)
        this._content_view!!.setGravity(Gravity.CENTER)
        this._scroll_view!!.addView(this._content_view)
        this._title_label = TextView(getContext())
        this._title_label!!.setLayoutParams(
            LinearLayout.LayoutParams(
                ViewGroup.LayoutParams.MATCH_PARENT,
                ViewGroup.LayoutParams.WRAP_CONTENT
            )
        )
        this._title_label!!.setPadding(padding, padding, padding, padding)
        this._title_label!!.setTextColor(-1)
        this._title_label!!.setTextSize(
            2,
            ASLayoutParams.Companion.getInstance().getTextSizeLarge()
        )
        this._title_label!!.setText("選項")
        this._title_label!!.setBackgroundColor(-14671840)
        this._title_label!!.setGravity(Gravity.CENTER)
        this._content_view!!.addView(this._title_label)
        this._item_block = LinearLayout(getContext())
        this._item_block!!.setLayoutParams(
            FrameLayout.LayoutParams(
                dialog_width,
                ViewGroup.LayoutParams.WRAP_CONTENT
            )
        )
        this._item_block!!.setOrientation(LinearLayout.VERTICAL)
        this._item_block!!.setGravity(Gravity.CENTER)
        this._content_view!!.addView(this._item_block)
        return frame
    }

    fun setListener(aListener: ASListDialogItemClickListener?): ASListDialog {
        this._listener = aListener
        return this
    }

    fun setTitle(aTitle: String?): ASListDialog {
        if (this._title_label != null) {
            this._title_label!!.setText(aTitle)
        }
        return this
    }

    fun addItems(aItemList: Array<String?>): ASListDialog {
        for (item_title in aItemList) {
            addItem(item_title)
        }
        return this
    }

    fun addItem(aItemTitle: String?): ASListDialog {
        val button = createButton()
        button.setOnClickListener(View.OnClickListener { v: View? ->
            this@ASListDialog.onItemClicked(
                v as Button?
            )
        })
        button.setOnLongClickListener(OnLongClickListener { v: View? ->
            this@ASListDialog.onItemLongClicked(
                v as Button?
            )
        })
        if (aItemTitle == null) {
            button.setVisibility(View.GONE)
        } else {
            if (this._item_list.size > 0) {
                this._item_block!!.addView(createDivider())
            }
            button.setText(aItemTitle)
        }
        this._item_block!!.addView(button)
        val item = ASListDialogItem()
        item.button = button
        item.title = aItemTitle
        this._item_list.add(item)
        return this
    }

    fun createDivider(): View {
        val divider = View(getContext())
        val divider_height = ceil(
            TypedValue.applyDimension(
                TypedValue.COMPLEX_UNIT_DIP,
                1.0f,
                getContext().getResources().getDisplayMetrics()
            ).toDouble()
        ).toInt()
        divider.setLayoutParams(LinearLayout.LayoutParams(-1, divider_height))
        divider.setBackgroundColor(-2130706433)
        return divider
    }

    private fun createButton(): Button {
        val button = Button(getContext())
        button.setLayoutParams(
            LinearLayout.LayoutParams(
                ViewGroup.LayoutParams.MATCH_PARENT,
                ViewGroup.LayoutParams.WRAP_CONTENT
            )
        )
        button.setMinimumHeight(
            TypedValue.applyDimension(
                TypedValue.COMPLEX_UNIT_DIP,
                60.0f,
                getContext().getResources().getDisplayMetrics()
            ).toInt()
        )
        button.setGravity(Gravity.CENTER)
        if (this._item_text_size == 0) {
            button.setTextSize(2, ASLayoutParams.Companion.getInstance().getTextSizeNormal())
        } else if (this._item_text_size == 1) {
            button.setTextSize(2, ASLayoutParams.Companion.getInstance().getTextSizeLarge())
        } else if (this._item_text_size == 2) {
            button.setTextSize(2, ASLayoutParams.Companion.getInstance().getTextSizeUltraLarge())
        }
        button.setBackground(ASLayoutParams.Companion.getInstance().getListItemBackgroundDrawable())
        button.setTextColor(ASLayoutParams.Companion.getInstance().getListItemTextColor())
        button.setSingleLine(true)
        return button
    }

    private fun indexOfButton(aButton: Button?): Int {
        for (i in this._item_list.indices) {
            val item = this._item_list.get(i)
            if (item.button === aButton) {
                val index = i
                return index
            }
        }
        return -1
    }

    private fun onItemClicked(button: Button?) {
        if (this._listener != null) {
            val index = indexOfButton(button)
            if (index != -1) {
                this._listener!!.onListDialogItemClicked(
                    this,
                    index,
                    this._item_list.get(index).title
                )
            }
            dismiss()
        }
    }

    private fun onItemLongClicked(button: Button?): Boolean {
        val index: Int
        var result = false
        if (this._listener != null && (indexOfButton(button).also { index = it }) != -1) {
            result = this._listener!!.onListDialogItemLongClicked(
                this,
                index,
                this._item_list.get(index).title
            )
        }
        if (result) {
            dismiss()
        }
        return result
    }

    fun setDialogWidth(width: Float): ASListDialog {
        this._dialog_width = width
        val dialog_width = ((TypedValue.applyDimension(
            TypedValue.COMPLEX_UNIT_DIP,
            this._dialog_width,
            getContext().getResources().getDisplayMetrics()
        ).toInt()) / 2) * 2
        this._scroll_view!!.setLayoutParams(
            LinearLayout.LayoutParams(
                dialog_width,
                ViewGroup.LayoutParams.WRAP_CONTENT
            )
        )
        this._content_view!!.setLayoutParams(
            FrameLayout.LayoutParams(
                dialog_width,
                ViewGroup.LayoutParams.WRAP_CONTENT
            )
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