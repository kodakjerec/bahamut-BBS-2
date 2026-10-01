package com.kota.Bahamut.listPage

import android.annotation.SuppressLint
import android.view.ViewGroup
import android.widget.AbsListView
import android.widget.FrameLayout
import androidx.recyclerview.widget.ItemTouchHelper
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.kota.Bahamut.R
import com.kota.asFramework.thread.ASCoroutine

/**
 * [TelnetListPage2] - 以 [RecyclerView] 為基準的 BBS 列表頁面抽象基類。
 *
 * 繼承自 [TelnetListPage]，維持與既有 [com.kota.Bahamut.command.TelnetCommand] 體系 100% 相容，
 * 並將底層 UI 控制從 [android.widget.ListView] 無縫轉移至 [RecyclerView]。
 */
abstract class TelnetListPage2 : TelnetListPage() {

    var recyclerView: RecyclerView? = null
        private set

    var emptyView: android.view.View? = null

    var itemTouchHelper: ItemTouchHelper? = null
        private set

    /** 內部使用的 RecyclerView Adapter */
    val recyclerViewAdapter: RecyclerView.Adapter<TelnetViewHolder> by lazy {
        TelnetListAdapter2()
    }

    /** 預設 ItemTouchHelper Callback，封裝拖曳處理邏輯 */
    private val defaultItemTouchHelperCallback = object : ItemTouchHelper.SimpleCallback(
        ItemTouchHelper.UP or ItemTouchHelper.DOWN, 0
    ) {
        var startPos: Int = -1
        var targetPos: Int = -1

        override fun isLongPressDragEnabled(): Boolean = false
        override fun isItemViewSwipeEnabled(): Boolean = false

        override fun onMove(
            recyclerView: RecyclerView,
            viewHolder: RecyclerView.ViewHolder,
            target: RecyclerView.ViewHolder
        ): Boolean {
            val from = viewHolder.bindingAdapterPosition
            val to = target.bindingAdapterPosition
            if (from != RecyclerView.NO_POSITION && to != RecyclerView.NO_POSITION) {
                if (startPos == -1) startPos = from
                targetPos = to
                return onItemMove(from, to)
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
                if (startPos != -1 && targetPos != -1 && startPos != targetPos) {
                    onItemDrop(startPos, targetPos)
                }
                startPos = -1
                targetPos = -1
            }
        }
    }

    /**
     * 當 Item 正在拖曳移動時呼叫 (畫面或資料交換)
     *
     * @param fromPosition 原始位置 (0-based)
     * @param toPosition 目標位置 (0-based)
     * @return 是否允許移動
     */
    open fun onItemMove(fromPosition: Int, toPosition: Int): Boolean {
        recyclerViewAdapter.notifyItemMoved(fromPosition, toPosition)
        return true
    }

    /**
     * 當 Item 拖曳放開 (完成拖曳) 時呼叫
     *
     * @param fromPosition 起始位置 (0-based)
     * @param toPosition 最終放置位置 (0-based)
     */
    open fun onItemDrop(fromPosition: Int, toPosition: Int) {
        // 預設留空，由子類別 (如 ClassPage) 覆寫處理 (如發送 Telnet 移動指令)
    }

    /**
     * 設定自訂 ItemTouchHelper 供拖曳排序使用
     */
    fun setupItemTouchHelper(callback: ItemTouchHelper.Callback) {
        itemTouchHelper = ItemTouchHelper(callback)
        if (recyclerView != null) {
            itemTouchHelper?.attachToRecyclerView(recyclerView)
        }
    }

    /**
     * 綁定 [RecyclerView] 元件並設定 LayoutManager 與 Adapter
     */
    fun bindRecyclerView(aRecyclerView: RecyclerView) {
        recyclerView = aRecyclerView
        if (recyclerView?.layoutManager == null) {
            recyclerView?.layoutManager = LinearLayoutManager(recyclerView?.context)
        }
        recyclerView?.adapter = recyclerViewAdapter

        if (itemTouchHelper == null) {
            itemTouchHelper = ItemTouchHelper(defaultItemTouchHelperCallback)
        }
        itemTouchHelper?.attachToRecyclerView(recyclerView)
    }

    /**
     * 移動列表至指定位置
     */
    override fun setListViewSelection(selection: Int) {
        if (recyclerView != null) {
            ASCoroutine.ensureMainThread {
                val targetIndex = if (selection == -1) count - 1 else selection
                if (targetIndex in 0 until count) {
                    recyclerView?.scrollToPosition(targetIndex)
                }
            }
        }
        // 同時相容舊有 listView 呼叫
        super.setListViewSelection(selection)
    }

    /**
     * 移動列表至指定位置並帶有頂部偏移量 (Top Offset)
     */
    override fun setListViewSelectionFromTop(selection: Int, top: Int) {
        if (recyclerView != null) {
            ASCoroutine.ensureMainThread {
                val targetIndex = if (selection == -1) count - 1 else selection
                if (targetIndex in 0 until count) {
                    val layoutManager = recyclerView?.layoutManager as? LinearLayoutManager
                    layoutManager?.scrollToPositionWithOffset(targetIndex, top)
                }
            }
        }
        // 同時相容舊有 listView 呼叫
        super.setListViewSelectionFromTop(selection, top)
    }

    /**
     * 取得第一個可見區塊索引
     */
    override val firstVisibleBlockIndex: Int
        get() {
            if (recyclerView != null) {
                val layoutManager = recyclerView?.layoutManager as? LinearLayoutManager
                val firstPos = layoutManager?.findFirstVisibleItemPosition() ?: -1
                return if (firstPos >= 0) getBlockIndex(firstPos) else -1
            }
            return super.firstVisibleBlockIndex
        }

    /**
     * 取得最後一個可見區塊索引
     */
    override val lastVisibleBlockIndex: Int
        get() {
            if (recyclerView != null) {
                val layoutManager = recyclerView?.layoutManager as? LinearLayoutManager
                val lastPos = layoutManager?.findLastVisibleItemPosition() ?: -1
                return if (lastPos >= 0) getBlockIndex(lastPos) else -1
            }
            return super.lastVisibleBlockIndex
        }

    /**
     * 當 Telnet 頁面收到刷新通知時呼叫，同步更新 RecyclerView
     */
    override fun onPageRefresh() {
        super.onPageRefresh()
        if (recyclerView != null) {
            safeNotifyDataSetChanged()
        }
    }

    /**
     * 清除列表項目時同步刷新 RecyclerView
     */
    override fun clear() {
        super.clear()
        if (recyclerView != null) {
            safeNotifyDataSetChanged()
        }
    }

    /**
     * 安全地在主執行緒刷新列表
     */
    override fun safeNotifyDataSetChanged() {
        ASCoroutine.ensureMainThread {
            recyclerViewAdapter.notifyDataSetChanged()
            if (emptyView != null) {
                if (isEmpty()) {
                    emptyView?.visibility = android.view.View.VISIBLE
                    recyclerView?.visibility = android.view.View.GONE
                } else {
                    emptyView?.visibility = android.view.View.GONE
                    recyclerView?.visibility = android.view.View.VISIBLE
                }
            }
            super.safeNotifyDataSetChanged()
        }
    }

    /**
     * 儲存列表捲動位置狀態
     */
    override fun saveListState() {
        if (recyclerView != null) {
            val state: ListState = ListStateStore.instance.getState(this.listId)
            val layoutManager = recyclerView?.layoutManager as? LinearLayoutManager
            if (layoutManager != null) {
                val firstPos = layoutManager.findFirstVisibleItemPosition()
                if (firstPos != RecyclerView.NO_POSITION) {
                    state.position = firstPos
                    val firstView = layoutManager.findViewByPosition(firstPos)
                    state.top = firstView?.top ?: 0
                }
            }
        } else {
            super.saveListState()
        }
    }

    /**
     * 載入並還原列表捲動位置狀態
     */
    override fun loadListState() {
        if (recyclerView != null) {
            val state: ListState = ListStateStore.instance.getState(this.listId)
            setListViewSelectionFromTop(state.position, state.top)
        } else {
            super.loadListState()
        }
    }

    /**
     * [RecyclerView.ViewHolder] 的容器包裝類別
     */
    class TelnetViewHolder(val container: FrameLayout) : RecyclerView.ViewHolder(container)

    /**
     * 專用 [RecyclerView.Adapter] 轉接器
     */
    private inner class TelnetListAdapter2 : RecyclerView.Adapter<TelnetViewHolder>() {

        override fun getItemCount(): Int {
            return this@TelnetListPage2.count
        }

        override fun getItemViewType(position: Int): Int {
            return this@TelnetListPage2.getItemViewType(position)
        }

        override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): TelnetViewHolder {
            val container = FrameLayout(parent.context).apply {
                layoutParams = RecyclerView.LayoutParams(
                    ViewGroup.LayoutParams.MATCH_PARENT,
                    ViewGroup.LayoutParams.WRAP_CONTENT
                )
            }
            return TelnetViewHolder(container)
        }

        @SuppressLint("ClickableViewAccessibility")
        override fun onBindViewHolder(holder: TelnetViewHolder, position: Int) {
            val adapterPos = holder.bindingAdapterPosition
            if (adapterPos == RecyclerView.NO_POSITION) return

            val convertView = if (holder.container.childCount > 0) holder.container.getChildAt(0) else null
            val childView = getView(adapterPos, convertView, holder.container)

            if (childView != null) {
                // 防護：若子類別 getView 返回的 View LayoutParams 為 AbsListView.LayoutParams，轉換為 FrameLayout.LayoutParams
                if (childView.layoutParams is AbsListView.LayoutParams || childView.layoutParams == null) {
                    childView.layoutParams = FrameLayout.LayoutParams(
                        ViewGroup.LayoutParams.MATCH_PARENT,
                        ViewGroup.LayoutParams.WRAP_CONTENT
                    )
                }

                if (childView.parent != holder.container) {
                    (childView.parent as? ViewGroup)?.removeView(childView)
                    holder.container.removeAllViews()
                    holder.container.addView(childView)
                }

                val dragHandle = childView.findViewById<android.view.View>(R.id.ClassPage_ItemView_DragHandle)
                dragHandle?.setOnTouchListener { _, event ->
                    if (event.actionMasked == android.view.MotionEvent.ACTION_DOWN) {
                        itemTouchHelper?.startDrag(holder)
                    }
                    false
                }
            }

            val targetView = childView ?: holder.container

            // 點擊事件監聽轉發
            targetView.setOnClickListener {
                val currentPos = holder.bindingAdapterPosition
                if (currentPos != RecyclerView.NO_POSITION) {
                    onItemClick(null, targetView, currentPos, getItemId(currentPos))
                }
            }

            // 長按事件監聽轉發
            targetView.setOnLongClickListener {
                val currentPos = holder.bindingAdapterPosition
                if (currentPos != RecyclerView.NO_POSITION) {
                    onListViewItemLongClicked(targetView, currentPos)
                } else {
                    false
                }
            }
        }
    }
}
