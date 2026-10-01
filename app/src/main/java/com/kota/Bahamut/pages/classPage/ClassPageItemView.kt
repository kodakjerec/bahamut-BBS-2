package com.kota.Bahamut.pages.classPage

import android.content.Context
import android.util.AttributeSet
import android.view.LayoutInflater
import android.view.View
import android.widget.Button
import android.widget.LinearLayout
import android.widget.TextView
import com.kota.Bahamut.R
import com.kota.Bahamut.pages.model.ClassPageItem
import com.kota.Bahamut.service.CommonFunctions.getContextString
import java.util.Objects

class ClassPageItemView : LinearLayout {
    private lateinit var boardManagerLabel: TextView
    private lateinit var boardNameLabel: TextView
    private lateinit var boardTitleLabel: TextView
    private lateinit var dividerBottom: View
    private lateinit var deleteButton: Button
    private lateinit var arrowView: View
    private var onDeleteClickListener: OnClickListener? = null

    constructor(context: Context?) : super(context) {
        init()
    }

    constructor(context: Context?, attrs: AttributeSet?) : super(context, attrs) {
        init()
    }

    private fun init() {
        (context.getSystemService(Context.LAYOUT_INFLATER_SERVICE) as LayoutInflater).inflate(
            R.layout.class_page_item_view,
            this
        )
        this.boardTitleLabel = findViewById(R.id.ClassPage_ItemView_classTitle)
        this.boardNameLabel = findViewById(R.id.ClassPage_ItemView_className)
        this.boardManagerLabel = findViewById(R.id.ClassPage_ItemView_classManager)
        this.dividerBottom = findViewById(R.id.ClassPage_ItemView_DividerBottom)
        this.deleteButton = findViewById(R.id.ClassPage_ItemView_DeleteButton)
        this.arrowView = findViewById(R.id.ListItem_ArrowView)

        this.deleteButton.setOnClickListener { v ->
            onDeleteClickListener?.onClick(v)
        }
    }

    fun setOnDeleteClickListener(listener: OnClickListener?) {
        this.onDeleteClickListener = listener
    }

    fun setDividerBottomVisible(visible: Boolean) {
        if (visible) {
            if (this.dividerBottom.visibility != VISIBLE) {
                this.dividerBottom.visibility = VISIBLE
            }
        } else if (this.dividerBottom.visibility != GONE) {
            this.dividerBottom.visibility = GONE
        }
    }

    fun setBoardTitleText(title: String?) {
        this.boardTitleLabel.text = Objects.requireNonNullElse<String?>(
            title,
            getContextString(R.string.loading_)
        )
    }

    fun setBoardNameText(boardName: String?) {
        this.boardNameLabel.text = Objects.requireNonNullElse<String?>(
            boardName,
            getContextString(R.string.loading)
        )
    }

    fun setBoardManagerText(boardManager: String?) {
        this.boardManagerLabel.text = Objects.requireNonNullElse<String?>(
            boardManager,
            getContextString(R.string.loading)
        )
    }

    fun setItem(aItem: ClassPageItem?, isFavoriteMode: Boolean = false, isDeleteManageMode: Boolean = false) {
        if (aItem != null) {
            setBoardTitleText(aItem.title)
            setBoardNameText(aItem.name)
            setBoardManagerText(aItem.manager)
        } else {
            clear()
            return
        }

        if (isFavoriteMode && isDeleteManageMode) {
            deleteButton.visibility = VISIBLE
            arrowView.visibility = GONE
        } else {
            deleteButton.visibility = GONE
            arrowView.visibility = VISIBLE
        }
    }

    fun clear() {
        setBoardTitleText(null)
        setBoardNameText(null)
        setBoardManagerText(null)
    }

    companion object {
        private const val COUNT = 0
    }
}
