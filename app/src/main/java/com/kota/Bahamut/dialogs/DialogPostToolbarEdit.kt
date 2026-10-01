package com.kota.Bahamut.dialogs

import android.annotation.SuppressLint
import android.view.Gravity
import android.view.MotionEvent
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.ImageView
import android.widget.LinearLayout
import androidx.recyclerview.widget.ItemTouchHelper
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.kota.Bahamut.R
import com.kota.Bahamut.pages.PostArticlePage
import com.kota.Bahamut.pages.theme.ThemeStore
import com.kota.Bahamut.service.CommonFunctions.getThemeColor
import com.kota.Bahamut.service.UserSettings
import com.kota.asFramework.dialog.ASDialog
import com.kota.telnetUI.textView.TelnetTextViewLarge
import java.util.Collections

class DialogPostToolbarEdit(
    private val postArticlePage: PostArticlePage,
    theme: Int = ThemeStore.getDialogThemeResId()
) : ASDialog(theme) {

    private val recyclerView: RecyclerView
    private val keys: MutableList<String>
    private val adapter: ToolbarEditAdapter
    private val itemTouchHelper: ItemTouchHelper

    init {
        requestWindowFeature(1)
        setContentView(R.layout.dialog_post_toolbar_edit)
        if (window != null) window?.setBackgroundDrawable(null)

        val mainLayout = findViewById<LinearLayout>(R.id.dialog_post_toolbar_edit_content_view)!!
        recyclerView = findViewById(R.id.PostToolbarEdit_recyclerView)!!

        findViewById<Button>(R.id.PostToolbarEdit_Done)?.setOnClickListener {
            dismiss()
        }

        keys = UserSettings.propertiesPostToolbarOrder.split(",").toMutableList()
        adapter = ToolbarEditAdapter()

        recyclerView.layoutManager = LinearLayoutManager(context)
        recyclerView.adapter = adapter

        val callback = object : ItemTouchHelper.SimpleCallback(
            ItemTouchHelper.UP or ItemTouchHelper.DOWN, 0
        ) {
            var startDisplayPos: Int = -1
            var targetDisplayPos: Int = -1

            override fun isLongPressDragEnabled(): Boolean = false
            override fun isItemViewSwipeEnabled(): Boolean = false

            override fun canDropOver(
                recyclerView: RecyclerView,
                current: RecyclerView.ViewHolder,
                target: RecyclerView.ViewHolder
            ): Boolean {
                return target.itemViewType == TYPE_ITEM
            }

            override fun onMove(
                recyclerView: RecyclerView,
                viewHolder: RecyclerView.ViewHolder,
                target: RecyclerView.ViewHolder
            ): Boolean {
                val from = viewHolder.bindingAdapterPosition
                val to = target.bindingAdapterPosition
                if (from != RecyclerView.NO_POSITION && to != RecyclerView.NO_POSITION &&
                    adapter.getItemViewType(from) == TYPE_ITEM &&
                    adapter.getItemViewType(to) == TYPE_ITEM
                ) {
                    if (startDisplayPos == -1) startDisplayPos = from
                    targetDisplayPos = to

                    val fromKeyIndex = adapter.displayPosToKeyIndex(from)
                    val toKeyIndex = adapter.displayPosToKeyIndex(to)

                    if (fromKeyIndex in keys.indices && toKeyIndex in keys.indices) {
                        Collections.swap(keys, fromKeyIndex, toKeyIndex)
                        adapter.notifyItemMoved(from, to)
                        return true
                    }
                }
                return false
            }

            override fun onSwiped(viewHolder: RecyclerView.ViewHolder, direction: Int) {}

            override fun onSelectedChanged(viewHolder: RecyclerView.ViewHolder?, actionState: Int) {
                super.onSelectedChanged(viewHolder, actionState)
                if (actionState == ItemTouchHelper.ACTION_STATE_DRAG) {
                    viewHolder?.itemView?.alpha = 0.7f
                } else if (actionState == ItemTouchHelper.ACTION_STATE_IDLE) {
                    viewHolder?.itemView?.alpha = 1.0f
                    if (startDisplayPos != -1 && targetDisplayPos != -1 && startDisplayPos != targetDisplayPos) {
                        saveAndApply()
                    }
                    startDisplayPos = -1
                    targetDisplayPos = -1
                }
            }
        }

        itemTouchHelper = ItemTouchHelper(callback)
        itemTouchHelper.attachToRecyclerView(recyclerView)

        setDialogWidth(mainLayout)
    }

    private fun saveAndApply() {
        val newOrder = keys.joinToString(",")
        UserSettings.propertiesPostToolbarOrder = newOrder
        postArticlePage.applyToolbarOrder()
        adapter.notifyDataSetChanged()
    }

    private fun getButtonNameForKey(key: String): String {
        return when (key) {
            "REFERENCE" -> "引用"
            "SYMBOL" -> "符號"
            "FACE" -> "表情"
            "COLOR" -> "上色"
            "FILE" -> "檔案"
            "SHORTEN_URL" -> "短網址"
            "SHORTEN_IMAGE" -> "縮圖"
            else -> key
        }
    }

    private inner class ToolbarEditAdapter : RecyclerView.Adapter<RecyclerView.ViewHolder>() {

        override fun getItemCount(): Int {
            // 7 keys + 2 group headers = 9 items total
            return keys.size + 2
        }

        override fun getItemViewType(position: Int): Int {
            return if (position == 0 || position == 4) TYPE_HEADER else TYPE_ITEM
        }

        fun displayPosToKeyIndex(displayPos: Int): Int {
            return if (displayPos <= 3) displayPos - 1 else displayPos - 2
        }

        override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): RecyclerView.ViewHolder {
            val context = parent.context
            if (viewType == TYPE_HEADER) {
                val headerView = TelnetTextViewLarge(context).apply {
                    setTextColor(getThemeColor(context, R.attr.bahamut_chapterTextColor))
                    setBackgroundColor(getThemeColor(context, R.attr.bahamut_dialogTitleBackground))
                    setPadding(24, 12, 16, 12)
                    layoutParams = RecyclerView.LayoutParams(
                        ViewGroup.LayoutParams.MATCH_PARENT,
                        ViewGroup.LayoutParams.WRAP_CONTENT
                    )
                }
                return HeaderViewHolder(headerView)
            } else {
                val rowLayout = LinearLayout(context).apply {
                    orientation = LinearLayout.HORIZONTAL
                    gravity = Gravity.CENTER_VERTICAL
                    setPadding(16, 8, 16, 8)
                    layoutParams = RecyclerView.LayoutParams(
                        ViewGroup.LayoutParams.MATCH_PARENT,
                        ViewGroup.LayoutParams.WRAP_CONTENT
                    )
                }

                val nameTextView = TelnetTextViewLarge(context).apply {
                    setTextColor(getThemeColor(context, R.attr.bahamut_defaultTextColor))
                    layoutParams = LinearLayout.LayoutParams(0, LinearLayout.LayoutParams.WRAP_CONTENT, 1f)
                }

                val dragHandle = ImageView(context).apply {
                    setImageResource(R.drawable.ic_drag_handle)
                    scaleType = ImageView.ScaleType.CENTER_INSIDE
                    setPadding(12, 12, 12, 12)
                    layoutParams = LinearLayout.LayoutParams(
                        (44 * context.resources.displayMetrics.density).toInt(),
                        (44 * context.resources.displayMetrics.density).toInt()
                    )
                }

                rowLayout.addView(nameTextView)
                rowLayout.addView(dragHandle)

                return ItemViewHolder(rowLayout, nameTextView, dragHandle)
            }
        }

        @SuppressLint("ClickableViewAccessibility")
        override fun onBindViewHolder(holder: RecyclerView.ViewHolder, position: Int) {
            val context = holder.itemView.context
            if (holder is HeaderViewHolder) {
                val textView = holder.itemView as TelnetTextViewLarge
                if (position == 0) {
                    textView.text = context.getString(R.string.post_toolbar_row_1)
                } else {
                    textView.text = context.getString(R.string.post_toolbar_row_2)
                }
            } else if (holder is ItemViewHolder) {
                val keyIndex = displayPosToKeyIndex(position)
                if (keyIndex in keys.indices) {
                    val key = keys[keyIndex]
                    holder.nameTextView.text = getButtonNameForKey(key)

                    holder.dragHandle.setOnTouchListener { _, event ->
                        if (event.actionMasked == MotionEvent.ACTION_DOWN) {
                            itemTouchHelper.startDrag(holder)
                        }
                        false
                    }
                }
            }
        }
    }

    private class HeaderViewHolder(view: View) : RecyclerView.ViewHolder(view)

    private class ItemViewHolder(
        view: View,
        val nameTextView: TelnetTextViewLarge,
        val dragHandle: ImageView
    ) : RecyclerView.ViewHolder(view)

    companion object {
        const val TYPE_HEADER = 0
        const val TYPE_ITEM = 1
    }
}
