# Bahamut/listPage - 列表頁面基礎架構（核心模組）

**applyto**: `app/src/main/java/com/kota/Bahamut/listPage/**/*.kt`

## 📋 模組概述

listPage 是所有列表頁面的基礎架構，實現 **20 項/區塊的分頁載入機制**。這是 BBS 客戶端最關鍵的效能優化架構，支援區塊載入、物件池、狀態保存、自動刷新等功能。

**所有文章列表、看板列表、信箱列表都基於此模組構建。**

**技術棧**: Kotlin, Android ListView, Object Pooling  
**設計模式**: 模板方法模式, 物件池模式, 適配器模式  
**關鍵機制**: 20 項/區塊分頁

---

## 📂 核心類別

### 1️⃣ `TelnetListPage.kt` - 列表頁面基類

所有列表頁面的抽象基類，實現區塊式載入。

#### 核心概念：20 項/區塊

```kotlin
// 區塊計算公式
fun getBlockIndex(itemIndex: Int): Int {
    return itemIndex / 20  // 每 20 項為一個區塊
}

fun getIndexInBlock(itemIndex: Int): Int {
    return itemIndex % 20  // 項目在區塊中的位置
}

// 範例：
// itemIndex = 0-19   -> blockIndex = 0
// itemIndex = 20-39  -> blockIndex = 1  
// itemIndex = 40-59  -> blockIndex = 2
```

#### 主要屬性和方法

```kotlin
abstract class TelnetListPage : TelnetPage(), ListAdapter {
    
    // ===== 區塊管理 =====
    
    private val blockList: MutableMap<Int, TelnetListPageBlock> = HashMap()
    
    /**
     * 設定區塊
     */
    fun setBlock(blockIndex: Int, block: TelnetListPageBlock?) {
        synchronized(blockList) {
            if (block != null) {
                blockList[blockIndex] = block
                itemSize = max(itemSize, block.maximumItemNumber)
            } else {
                blockList.remove(blockIndex)
            }
        }
    }
    
    /**
     * 獲取區塊
     */
    fun getBlock(blockIndex: Int): TelnetListPageBlock? {
        synchronized(blockList) {
            return blockList[blockIndex]
        }
    }
    
    /**
     * 移除區塊
     */
    private fun removeBlock(blockIndex: Int) {
        val block = blockList.remove(blockIndex)
        block?.let {
            // 回收區塊中的所有項目
            for (i in 0 until 20) {
                val item = it.getItem(i)
                if (item != null) {
                    item.clear()
                    recycleItem(item)
                }
            }
            it.clear()
            recycleBlock(it)
        }
    }
    
    // ===== 命令管理 =====
    
    private val operationCommandStack = Vector<TelnetCommand>()
    private var executingCommand: TelnetCommand? = null
    
    /**
     * 推送命令到執行佇列
     */
    fun pushCommand(command: TelnetCommand) {
        synchronized(operationCommandStack) {
            operationCommandStack.add(command)
        }
        executeCommand()
    }
    
    /**
     * 執行下一個命令
     */
    private fun executeCommand() {
        if (executingCommand != null) return
        
        synchronized(operationCommandStack) {
            if (operationCommandStack.isEmpty()) return
            
            executingCommand = operationCommandStack.removeAt(0)
            executingCommand?.execute(this)
        }
    }
    
    /**
     * 命令執行完成回呼
     */
    fun executeCommandFinished(block: TelnetListPageBlock?) {
        executingCommand?.executeFinished(this, block)
        executingCommand = null
        executeCommand() // 執行下一個命令
    }
    
    // ===== ListAdapter 實作 =====
    
    override fun getCount(): Int {
        synchronized(countLock) {
            return listCount
        }
    }
    
    override fun getItem(position: Int): Any? {
        val blockIndex = getBlockIndex(position)
        val indexInBlock = getIndexInBlock(position)
        return getBlock(blockIndex)?.getItem(indexInBlock)
    }
    
    override fun getItemId(position: Int): Long {
        return position.toLong()
    }
    
    abstract override fun getView(position: Int, convertView: View?, parent: ViewGroup?): View?
    
    /**
     * 安全的 notifyDataSetChanged（必須使用）
     */
    fun safeNotifyDataSetChanged() {
        ASCoroutine.ensureMainThread {
            mDataSetObservable.notifyChanged()
            
            if (listView?.adapter == null) {
                listView?.invalidateViews()
            }
        }
    }
    
    // ===== 抽象方法（子類必須實作） =====
    
    /**
     * 載入頁面資料
     * @return 載入的區塊資料
     */
    abstract fun loadPage(): TelnetListPageBlock?
    
    /**
     * 是否啟用自動載入
     */
    abstract val isAutoLoadEnable: Boolean
    
    /**
     * 回收區塊（物件池）
     */
    abstract fun recycleBlock(block: TelnetListPageBlock)
    
    /**
     * 回收項目（物件池）
     */
    abstract fun recycleItem(item: TelnetListPageItem)
    
    // ===== 自動刷新 =====
    
    private var autoLoadJob: Job? = null
    
    fun startAutoLoad() {
        if (!isAutoLoadEnable) return
        stopAutoLoad()
        
        autoLoadJob = CoroutineScope(Dispatchers.IO).launch {
            delay(10000) // 初始延遲 10 秒
            
            while (isActive) {
                if (shouldAutoLoad()) {
                    loadLastBlock()
                }
                delay(1000) // 每秒檢查一次
            }
        }
    }
    
    fun stopAutoLoad() {
        autoLoadJob?.cancel()
        autoLoadJob = null
    }
    
    private fun shouldAutoLoad(): Boolean {
        // 檢查是否需要自動載入
        return isPageAppeared && 
               System.currentTimeMillis() - lastLoadTime > 30000
    }
}
```

---

### 2️⃣ `TelnetListPageBlock.kt` - 資料區塊

表示 20 個項目的資料區塊。

```kotlin
class TelnetListPageBlock {
    
    private val items = arrayOfNulls<TelnetListPageItem>(20)
    
    var minimumItemNumber: Int = 0  // 最小項目編號
    var maximumItemNumber: Int = 0  // 最大項目編號
    
    /**
     * 設定項目
     */
    fun setItem(index: Int, item: TelnetListPageItem?) {
        if (index in 0..19) {
            items[index] = item
        }
    }
    
    /**
     * 獲取項目
     */
    fun getItem(index: Int): TelnetListPageItem? {
        return if (index in 0..19) items[index] else null
    }
    
    /**
     * 清空區塊
     */
    fun clear() {
        items.fill(null)
        minimumItemNumber = 0
        maximumItemNumber = 0
    }
    
    companion object {
        // 物件池
        private val pool = Stack<TelnetListPageBlock>()
        
        /**
         * 從物件池獲取或建立新區塊
         */
        fun create(): TelnetListPageBlock {
            synchronized(pool) {
                return if (pool.isNotEmpty()) {
                    pool.pop()
                } else {
                    TelnetListPageBlock()
                }
            }
        }
        
        /**
         * 回收到物件池
         */
        fun recycle(block: TelnetListPageBlock) {
            synchronized(pool) {
                block.clear()
                pool.push(block)
            }
        }
    }
}
```

---

### 3️⃣ `TelnetListPageItem.kt` - 列表項目

表示單一列表項目。

```kotlin
abstract class TelnetListPageItem {
    
    var itemIndex: Int = 0
    var isRead: Boolean = false
    
    /**
     * 清空項目資料
     */
    abstract fun clear()
    
    companion object {
        // 每個子類應該有自己的物件池
    }
}
```

---

### 4️⃣ 狀態管理

#### `ListState.kt` - 列表狀態
```kotlin
data class ListState(
    var position: Int = 0,       // 捲動位置
    var topOffset: Int = 0,      // 頂部偏移
    var blockIndex: Int = 0,     // 當前區塊
    var timestamp: Long = 0L     // 儲存時間
)
```

#### `ListStateStore.kt` - 狀態儲存器
```kotlin
object ListStateStore {
    
    private val states = mutableMapOf<String, ListState>()
    
    fun save(key: String, state: ListState) {
        states[key] = state
    }
    
    fun load(key: String): ListState? {
        return states[key]
    }
    
    fun remove(key: String) {
        states.remove(key)
    }
}
```

#### 使用狀態保存
```kotlin
// 在 TelnetListPage 中
fun saveListState() {
    if (listView == null) return
    
    val firstVisiblePosition = listView!!.firstVisiblePosition
    val topView = listView!!.getChildAt(0)
    val topOffset = topView?.top ?: 0
    
    val state = ListState(
        position = firstVisiblePosition,
        topOffset = topOffset,
        blockIndex = currentBlock,
        timestamp = System.currentTimeMillis()
    )
    
    ListStateStore.save(listName, state)
}

fun loadListState() {
    val state = ListStateStore.load(listName) ?: return
    
    // 恢復捲動位置
    object : ASRunner() {
        override fun run() {
            listView?.setSelectionFromTop(state.position, state.topOffset)
        }
    }.runInMainThread()
}
```

---

## 🎯 完整使用範例

### 實作看板文章列表

```kotlin
class BoardMainPage : TelnetListPage() {
    
    override val pageLayout = R.layout.board_main_page_layout
    
    // 每個子類有自己的物件池
    companion object {
        private val blockPool = Stack<BoardPageBlock>()
        private val itemPool = Stack<BoardPageItem>()
        
        fun createBlock(): BoardPageBlock {
            synchronized(blockPool) {
                return if (blockPool.isNotEmpty()) blockPool.pop() 
                       else BoardPageBlock()
            }
        }
        
        fun recycleBlockInternal(block: BoardPageBlock) {
            synchronized(blockPool) {
                block.clear()
                blockPool.push(block)
            }
        }
        
        fun createItem(): BoardPageItem {
            synchronized(itemPool) {
                return if (itemPool.isNotEmpty()) itemPool.pop() 
                       else BoardPageItem()
            }
        }
        
        fun recycleItemInternal(item: BoardPageItem) {
            synchronized(itemPool) {
                item.clear()
                itemPool.push(item)
            }
        }
    }
    
    override val isAutoLoadEnable = true
    
    override fun loadPage(): TelnetListPageBlock? {
        // 解析 Telnet 回傳的資料
        val block = createBlock()
        
        // 解析每一行，建立項目
        for (i in 0 until 20) {
            val item = createItem()
            item.itemIndex = currentBlock * 20 + i
            item.title = parseTitle(i)
            item.author = parseAuthor(i)
            item.date = parseDate(i)
            
            block.setItem(i, item)
        }
        
        block.minimumItemNumber = currentBlock * 20 + 1
        block.maximumItemNumber = currentBlock * 20 + 20
        
        return block
    }
    
    override fun recycleBlock(block: TelnetListPageBlock) {
        recycleBlockInternal(block as BoardPageBlock)
    }
    
    override fun recycleItem(item: TelnetListPageItem) {
        recycleItemInternal(item as BoardPageItem)
    }
    
    override fun getView(position: Int, convertView: View?, parent: ViewGroup?): View {
        val item = getItem(position) as? BoardPageItem
        val view = convertView as? BoardItemView ?: BoardItemView(context)
        
        item?.let {
            view.setTitle(it.title)
            view.setAuthor(it.author)
            view.setDate(it.date)
            view.setRead(it.isRead)
        }
        
        return view
    }
    
    override fun onItemClick(parent: AdapterView<*>?, view: View?, position: Int, id: Long) {
        // 載入文章
        loadItemAtIndex(position)
    }
    
    fun loadFirstBlock() {
        val command = BahamutCommandLoadFirstBlock()
        pushCommand(command)
    }
    
    fun loadLastBlock() {
        val command = BahamutCommandLoadLastBlock()
        pushCommand(command)
    }
    
    fun loadBlock(blockIndex: Int) {
        val command = BahamutCommandLoadBlock(blockIndex)
        pushCommand(command)
    }
}

// 自訂項目資料模型
class BoardPageItem : TelnetListPageItem() {
    var title: String = ""
    var author: String = ""
    var date: String = ""
    var pushCount: Int = 0
    
    override fun clear() {
        title = ""
        author = ""
        date = ""
        pushCount = 0
        isRead = false
    }
}

// 自訂區塊
class BoardPageBlock : TelnetListPageBlock()
```

---

## ⚠️ 重要注意事項

### 1. Adapter 更新崩潰問題（重要！）

**🔴 這是已知的最高頻崩潰點！**

```kotlin
// ❌ 錯誤：多次連續呼叫會導致 IllegalStateException
adapter.notifyDataSetChanged()
safeNotifyDataSetChanged()
listView.invalidateViews()

// ✅ 正確：只呼叫一次，且包裹在 ASRunner 中
object : ASRunner() {
    override fun run() {
        safeNotifyDataSetChanged()
    }
}.runInMainThread()
```

### 2. 物件池使用

```kotlin
// ✅ 正確：使用物件池減少 GC
val item = BoardPageItem.create()  // 從池中取得或建立
// ... 使用 item
BoardPageItem.recycle(item)  // 回收到池中

// ❌ 錯誤：直接 new 造成頻繁 GC
val item = BoardPageItem()  // 每次都建立新物件
```

### 3. 區塊清理

```kotlin
// 自動清理遠離可見區域的區塊
private fun cleanDistantBlocks() {
    val firstVisible = firstVisibleBlockIndex
    val lastVisible = lastVisibleBlockIndex
    
    val keys = HashSet(blockList.keys)
    for (key in keys) {
        if (key > lastVisible + 3 || key < firstVisible - 3) {
            removeBlock(key)  // 移除並回收
        }
    }
}
```

---

## 📚 相關模組

- [asFramework-pageController](asFramework-pageController.md) - 頁面生命週期
- [asFramework-thread](asFramework-thread.md) - 執行緒管理
- [Bahamut-command](Bahamut-command.md) - 載入命令
- [Bahamut-pages](Bahamut-pages.md) - 具體頁面實作

---

## 📝 技術特點總結

1. **區塊式載入**: 20 項/區塊，減少記憶體使用
2. **物件池模式**: 重用物件減少 GC 壓力
3. **自動清理**: 清理不可見區塊釋放記憶體
4. **狀態保存**: 保存並恢復捲動位置
5. **命令佇列**: 依序執行載入命令
6. **自動刷新**: 背景自動載入最新內容
7. **執行緒安全**: 同步保護共用資料
