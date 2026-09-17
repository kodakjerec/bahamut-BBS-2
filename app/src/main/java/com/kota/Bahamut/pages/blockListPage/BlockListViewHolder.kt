package com.kota.Bahamut.pages.blockListPage

import android.content.Context
import android.view.Gravity
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.LinearLayout
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.kota.Bahamut.R
import com.kota.Bahamut.service.CommonFunctions

class BlockListViewHolder(view: View, listener: BlockListClickListener?) :
    RecyclerView.ViewHolder(view), View.OnClickListener {
    private val nameLabel: TextView? = view.findViewById(R.id.BlockListItemView_Name)
    var index: Int = 0
    var mListener: BlockListClickListener?

    init {
        this.mListener = listener
        view.setOnClickListener(this)
        view.findViewById<Button>(R.id.BlockListItemView_Delete)?.setOnClickListener {
            mListener?.onBlockListPageItemViewDeleteClicked(this)
        }
    }

    fun setName(name: String?) {
        nameLabel?.text = name
        nameLabel?.contentDescription = "從名單中剔除$name"
    }

    override fun onClick(v: View?) {
        mListener?.onBlockListPageItemViewClicked(this)
    }

    companion object {
        fun createView(context: Context): View {
            val scale = context.resources.displayMetrics.density

            val root = LinearLayout(context).apply {
                layoutParams = ViewGroup.LayoutParams(
                    ViewGroup.LayoutParams.MATCH_PARENT,
                    ViewGroup.LayoutParams.WRAP_CONTENT
                )
                orientation = LinearLayout.VERTICAL
            }

            val row = LinearLayout(context).apply {
                layoutParams = LinearLayout.LayoutParams(
                    LinearLayout.LayoutParams.MATCH_PARENT,
                    (48 * scale).toInt()
                )
                orientation = LinearLayout.HORIZONTAL
                gravity = Gravity.CENTER_VERTICAL
                setPadding((12 * scale).toInt(), 0, 0, 0)
            }

            val nameLabel = TextView(context).apply {
                id = R.id.BlockListItemView_Name
                layoutParams = LinearLayout.LayoutParams(
                    0,
                    ViewGroup.LayoutParams.WRAP_CONTENT,
                    1.0f
                )
                textSize = 18f
                setTextColor(CommonFunctions.getThemeColor(R.attr.bahamut_defaultTextColor))
            }

            val deleteBtn = Button(context).apply {
                id = R.id.BlockListItemView_Delete
                layoutParams = LinearLayout.LayoutParams(
                    (80 * scale).toInt(),
                    ViewGroup.LayoutParams.MATCH_PARENT
                )
                text = CommonFunctions.getContextString(R.string.delete)
                textSize = 16f
                val bgRes = CommonFunctions.getThemeResourceId(R.attr.bahamut_toolbarItemBackground)
                if (bgRes != 0) {
                    setBackgroundResource(bgRes)
                }
                setTextColor(CommonFunctions.getThemeColor(R.attr.bahamut_buttonTextColor))
            }

            val divider = View(context).apply {
                id = R.id.BlockListPage_ItemView_DividerBottom
                layoutParams = LinearLayout.LayoutParams(
                    LinearLayout.LayoutParams.MATCH_PARENT,
                    1
                )
                setBackgroundColor(CommonFunctions.getThemeColor(R.attr.bahamut_dividerColor))
            }

            row.addView(nameLabel)
            row.addView(deleteBtn)
            root.addView(row)
            root.addView(divider)
            return root
        }
    }
}
