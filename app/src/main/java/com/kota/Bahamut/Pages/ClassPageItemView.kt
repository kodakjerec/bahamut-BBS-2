package com.kota.Bahamut.Pages

import android.content.Context
import android.util.AttributeSet
import android.view.LayoutInflater
import android.view.View
import android.widget.LinearLayout
import android.widget.TextView
import com.kota.Bahamut.Pages.Model.ClassPageItem
import com.kota.Bahamut.R
import com.kota.Bahamut.Service.CommonFunctions.getContextString
import java.util.Objects

class ClassPageItemView : LinearLayout {
    private var _board_manager_label: TextView? = null
    private var _board_name_label: TextView? = null
    private var _board_title_label: TextView? = null
    private var _divider_bottom: View? = null

    constructor(context: Context?) : super(context) {
        init()
    }

    constructor(context: Context?, attrs: AttributeSet?) : super(context, attrs) {
        init()
    }

    private fun init() {
        (getContext().getSystemService(Context.LAYOUT_INFLATER_SERVICE) as LayoutInflater).inflate(
            R.layout.class_page_item_view,
            this
        )
        this._board_title_label = findViewById<TextView?>(R.id.ClassPage_ItemView_classTitle)
        this._board_name_label = findViewById<TextView?>(R.id.ClassPage_ItemView_className)
        this._board_manager_label = findViewById<TextView?>(R.id.ClassPage_ItemView_classManager)
        this._divider_bottom = findViewById<View?>(R.id.ClassPage_ItemView_DividerBottom)
    }

    fun setDividerBottomVisible(visible: Boolean) {
        if (this._divider_bottom == null) {
            return
        }
        if (visible) {
            if (this._divider_bottom!!.getVisibility() != VISIBLE) {
                this._divider_bottom!!.setVisibility(VISIBLE)
            }
        } else if (this._divider_bottom!!.getVisibility() != GONE) {
            this._divider_bottom!!.setVisibility(GONE)
        }
    }

    fun setBoardTitleText(title: String?) {
        if (this._board_title_label == null) {
            return
        }
        this._board_title_label!!.setText(
            Objects.requireNonNullElse<String?>(
                title,
                getContextString(R.string.loading_)
            )
        )
    }

    fun setBoardNameText(boardName: String?) {
        if (this._board_name_label == null) {
            return
        }
        this._board_name_label!!.setText(
            Objects.requireNonNullElse<String?>(
                boardName,
                getContextString(R.string.loading)
            )
        )
    }

    fun setBoardManagerText(boardManager: String?) {
        if (this._board_manager_label == null) {
            return
        }
        this._board_manager_label!!.setText(
            Objects.requireNonNullElse<String?>(
                boardManager,
                getContextString(R.string.loading)
            )
        )
    }

    fun setItem(aItem: ClassPageItem?) {
        if (aItem != null) {
            setBoardTitleText(aItem.Title)
            setBoardNameText(aItem.Name)
            setBoardManagerText(aItem.Manager)
            return
        }
        clear()
    }

    fun clear() {
        setBoardTitleText(null)
        setBoardNameText(null)
        setBoardManagerText(null)
    }

    companion object {
        private const val _count = 0
    }
}
