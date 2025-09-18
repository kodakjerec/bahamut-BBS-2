package com.kota.Bahamut.dialogs

import android.util.TypedValue
import android.view.Gravity
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.LinearLayout
import android.widget.ScrollView
import android.widget.TextView
import com.kota.asFramework.dialog.ASDialog
import com.kota.asFramework.dialog.ASLayoutParams.Companion.instance
import com.kota.Bahamut.R
import java.util.Vector
import kotlin.math.ceil

class DialogInsertExpression : ASDialog() {
    var mainView: ScrollView
    var titleLabel: TextView?
    var itemBlock: LinearLayout
    var listener: DialogInsertExpressionListener? = null
    val itemList: Vector<DialogItem> = Vector<DialogItem>()

    class DialogItem {
        var button: Button? = null
        var title: String? = null
    }

    var settingListener: View.OnClickListener = View.OnClickListener { view: View? ->
        this.listener!!.onListDialogSettingClicked()
        dismiss()
    }

    init {
        requestWindowFeature(1)
        setContentView(R.layout.dialog_insert_expressions)
        if (window != null) window!!.setBackgroundDrawable(null)

        mainView = findViewById<ScrollView>(R.id.dialog_insert_expressions_scrollView)
        titleLabel = findViewById<TextView?>(R.id.dialog_insert_expressions_title)
        itemBlock = findViewById<LinearLayout>(R.id.dialog_insert_expressions_content)

        findViewById<View>(R.id.dialog_insert_expressions_setting).setOnClickListener(
            settingListener
        )
        setDialogWidth(mainView)
    }

    fun setListener(aListener: DialogInsertExpressionListener?): DialogInsertExpression {
        listener = aListener
        return this
    }

    fun setTitle(aTitle: String?): DialogInsertExpression {
        if (this.titleLabel != null) {
            this.titleLabel!!.text = aTitle
        }
        return this
    }

    fun addItems(aItemList: Array<String?>): DialogInsertExpression {
        for (itemTitle in aItemList) {
            addItem(itemTitle)
        }
        return this
    }

    fun addItem(aItemTitle: String?): DialogInsertExpression {
        val button = createButton()

        button.setOnClickListener { v: View? ->
            this@DialogInsertExpression.onItemClicked(
                v as Button?
            )
        }

        if (aItemTitle == null) {
            button.visibility = View.GONE
        } else {
            if (this.itemList.isNotEmpty()) {
                this.itemBlock.addView(createDivider())
            }
            button.text = aItemTitle
        }
        this.itemBlock.addView(button)
        val item = DialogItem()
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
        button.setTextSize(2, instance.textSizeNormal)
        button.background = instance.listItemBackgroundDrawable
        button.setTextColor(instance.listItemTextColor)
        button.isSingleLine = true
        return button
    }

    private fun indexOfButton(aButton: Button?): Int {
        for (i in this.itemList.indices) {
            val item = this.itemList[i]
            if (item.button === aButton) {
                return i
            }
        }
        return -1
    }

    private fun onItemClicked(button: Button?) {
        if (this.listener != null) {
            val index = indexOfButton(button)
            if (index != -1) {
                this.listener!!.onListDialogItemClicked(
                    this,
                    index,
                    this.itemList[index].title!!
                )
            }
            dismiss()
        }
    }

    companion object {
        @JvmStatic
        fun createDialog(): DialogInsertExpression {
            return DialogInsertExpression()
        }
    }
}
