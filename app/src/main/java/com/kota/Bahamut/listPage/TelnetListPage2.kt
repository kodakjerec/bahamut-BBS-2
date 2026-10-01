package com.kota.Bahamut.listPage

import android.view.ViewGroup
import android.widget.AbsListView
import android.widget.FrameLayout
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
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

    /** 內部使用的 RecyclerView Adapter */
    val recyclerViewAdapter: RecyclerView.Adapter<TelnetViewHolder> by lazy {
        TelnetListAdapter2()
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
     * 安全地在主執行緒刷新列表
     */
    override fun safeNotifyDataSetChanged() {
        ASCoroutine.ensureMainThread {
            recyclerViewAdapter.notifyDataSetChanged()
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
            }

            // 點擊事件監聽轉發
            holder.container.setOnClickListener {
                val currentPos = holder.bindingAdapterPosition
                if (currentPos != RecyclerView.NO_POSITION) {
                    onItemClick(null, childView ?: holder.container, currentPos, getItemId(currentPos))
                }
            }

            // 長按事件監聽轉發
            holder.container.setOnLongClickListener {
                val currentPos = holder.bindingAdapterPosition
                if (currentPos != RecyclerView.NO_POSITION) {
                    onListViewItemLongClicked(childView ?: holder.container, currentPos)
                } else {
                    false
                }
            }
        }
    }
}
