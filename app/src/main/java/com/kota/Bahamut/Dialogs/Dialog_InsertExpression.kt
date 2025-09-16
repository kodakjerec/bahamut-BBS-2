package com.kota.Bahamut.Dialogs

import android.util.TypedValue
import android.view.Gravity
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.LinearLayout
import android.widget.ScrollView
import android.widget.TextView
import com.kota.ASFramework.Dialog.ASDialog
import com.kota.ASFramework.Dialog.ASLayoutParams.Companion.instance
import com.kota.Bahamut.R
import java.util.Vector
import kotlin.math.ceil

class Dialog_InsertExpression : ASDialog() {
    var _scroll_view: ScrollView
    var _title_label: TextView?
    var _item_block: LinearLayout
    var _listener: Dialog_InsertExpression_Listener? = null
    val _item_list: Vector<DialogItem> = Vector<DialogItem>()

    private class DialogItem {
        var button: Button? = null
        var title: String? = null
    }

    var _setting_listener: View.OnClickListener = View.OnClickListener { view: View? ->
        this._listener!!.onListDialogSettingClicked()
        dismiss()
    }

    init {
        requestWindowFeature(1)
        setContentView(R.layout.dialog_insert_expressions)
        if (getWindow() != null) getWindow()!!.setBackgroundDrawable(null)

        _scroll_view = findViewById<ScrollView>(R.id.dialog_insert_expressions_scrollView)
        _title_label = findViewById<TextView?>(R.id.dialog_insert_expressions_title)
        _item_block = findViewById<LinearLayout>(R.id.dialog_insert_expressions_content)

        findViewById<View?>(R.id.dialog_insert_expressions_setting).setOnClickListener(
            _setting_listener
        )
        setDialogWidth()
    }

    fun setListener(aListener: Dialog_InsertExpression_Listener?): Dialog_InsertExpression {
        _listener = aListener
        return this
    }

    fun setTitle(aTitle: String?): Dialog_InsertExpression {
        if (this._title_label != null) {
            this._title_label!!.setText(aTitle)
        }
        return this
    }

    fun addItems(aItemList: Array<String?>): Dialog_InsertExpression {
        for (item_title in aItemList) {
            addItem(item_title)
        }
        return this
    }

    fun addItem(aItemTitle: String?): Dialog_InsertExpression {
        val button = createButton()

        button.setOnClickListener(View.OnClickListener { v: View? ->
            this@Dialog_InsertExpression.onItemClicked(
                v as Button?
            )
        })

        if (aItemTitle == null) {
            button.setVisibility(View.GONE)
        } else {
            if (this._item_list.size > 0) {
                this._item_block.addView(createDivider())
            }
            button.setText(aItemTitle)
        }
        this._item_block.addView(button)
        val item = DialogItem()
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
        button.setTextSize(2, instance.textSizeNormal)
        button.setBackground(instance.listItemBackgroundDrawable)
        button.setTextColor(instance.listItemTextColor)
        button.setSingleLine(true)
        return button
    }

    private fun indexOfButton(aButton: Button?): Int {
        for (i in this._item_list.indices) {
            val item = this._item_list.get(i)
            if (item.button === aButton) {
                return i
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

    // 變更dialog寬度
    fun setDialogWidth() {
        val screenHeight = getContext().getResources().getDisplayMetrics().heightPixels
        val screenWidth = getContext().getResources().getDisplayMetrics().widthPixels
        val dialog_height = (screenHeight * 0.7).toInt()
        val dialog_width = (screenWidth * 0.7).toInt()
        val oldLayoutParams = _scroll_view.getLayoutParams()
        oldLayoutParams.width = dialog_width
        oldLayoutParams.height = dialog_height
        _scroll_view.setLayoutParams(oldLayoutParams)
    }

    companion object {
        @JvmStatic
        fun createDialog(): Dialog_InsertExpression {
            return Dialog_InsertExpression()
        }
    }
}
