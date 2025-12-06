# Bahamut BBS Android Client - AI Coding Guidelines

## Project Overview
Android BBS client for bbs.gamer.com.tw using Telnet protocol. Decompiled from v1.6, mixed Kotlin/Java codebase.

**Tech Stack:** Kotlin 2.1.0, Android SDK 36, minSdk 26, Java 17

## Critical Architecture Patterns

### 1. **Threading Model - ASRunner**
**ALWAYS use ASRunner for UI updates, NEVER direct Handler/runOnUiThread**

```kotlin
// Main thread execution
object : ASRunner() {
    override fun run() {
        // UI updates here
    }
}.runInMainThread()

// Background execution
ASCoroutine.runInNewCoroutine {
    // Background work
}

// Delayed execution
val runner = object : ASRunner() {
    override fun run() { /* action */ }
}
runner.postDelayed(3000) // 3 seconds
runner.cancel() // Cancel if needed
```

**Check thread context:** `ASRunner.isMainThread`

### 2. **Block-Based Pagination (20 items/block)**
All list-based pages inherit from `TelnetListPage` using block loading:

```kotlin
// Block calculation
val blockIndex = itemIndex / 20  // getBlockIndex()
val indexInBlock = itemIndex % 20 // getIndexInBlock()

// Block management
setBlock(blockIndex, telnetListPageBlock)
getBlock(blockIndex)
removeBlock(blockIndex)
```

**Block lifecycle:** Load → Cache → Recycle (object pooling pattern)

### 3. **ListView Adapter Updates - CRITICAL**
**NEVER call multiple adapter updates in sequence. Use safeNotifyDataSetChanged() ONCE:**

```kotlin
// ❌ WRONG - Multiple updates cause crashes
adapter.notifyDataSetChanged()
safeNotifyDataSetChanged()
listView.invalidateViews()

// ✅ CORRECT - Single update wrapped in ASRunner
object : ASRunner() {
    override fun run() {
        safeNotifyDataSetChanged() // Calls mDataSetObservable.notifyChanged()
    }
}.runInMainThread()
```

**Known crash pattern:** Double notification in post-article workflow (`BoardMainPage.recoverPost()`, `finishPost()`)

### 4. **Page Navigation - ASNavigationController**
Custom iOS-like navigation stack:

```kotlin
// Page lifecycle
onPageDidLoad()       // Once, view created
onPageWillAppear()    // Before visible, load state
onPageDidAppear()     // Visible, start auto-refresh
onPageWillDisappear() // Before hidden, stop timers
onPageDidDisappear()  // Hidden, save state

// Navigation
navigationController.pushViewController(page)
navigationController.popViewController()
navigationController.popToViewController(page)
```

### 5. **Singleton Page Management - PageContainer**
Pages are cached singletons accessed via `PageContainer.instance`:

```kotlin
// Accessing pages
val boardPage = PageContainer.instance!!.boardPage  // Get/create
PageContainer.instance!!.cleanBoardPage()          // Destroy

// Stack-based pages (Class, BoardEssence)
PageContainer.instance!!.pushClassPage(name, title)
PageContainer.instance!!.popClassPage()
```

### 6. **Telnet Command Pattern**
All Telnet operations use command stacks:

```kotlin
class BahamutCommandLoadBlock : TelnetCommand() {
    override fun execute(page: TelnetListPage) {
        // Send Telnet commands
        TelnetOutputBuilder.create()
            .pushKey(TelnetKeyboard.CTRL_Z)
            .sendToServer()
    }
    
    override fun executeFinished(page: TelnetListPage, block: TelnetListPageBlock?) {
        // Process response
        page.setBlock(blockIndex, block)
    }
}

// Usage
pushCommand(BahamutCommandLoadBlock(blockIndex))
```

### 7. **Object Pooling for Performance**
**ALL page items and blocks use object pools:**

```kotlin
companion object {
    private val _pool = Stack<BoardPageItem>()
    
    fun create(): BoardPageItem {
        synchronized(_pool) {
            return if (_pool.isNotEmpty()) _pool.pop() 
                   else BoardPageItem()
        }
    }
    
    fun recycle(item: BoardPageItem) {
        synchronized(_pool) { _pool.push(item) }
    }
}
```

**Always recycle items when removing blocks:**
```kotlin
recycleItem(item)
recycleBlock(block)
```

### 8. **State Management - BahamutStateHandler**
Central Telnet state machine parsing server responses:

```kotlin
override fun handleState() {
    loadState() // Parse TelnetModel.frame
    
    // Detect page by cursor position and content
    if (rowString00.contains("文章選讀")) {
        handleBoardMainPage()
    }
}
```

**Critical:** State handler drives ALL page transitions, not user actions.

## Common Patterns

### Auto-Refresh Coroutines
```kotlin
private var autoLoadJob: Job? = null

fun startAutoLoad() {
    if (!isAutoLoadEnable) return
    stopAutoLoad()
    
    autoLoadJob = CoroutineScope(Dispatchers.IO).launch {
        delay(10000) // Initial delay
        while (isActive) {
            if (shouldAutoLoad()) loadLastBlock()
            delay(1000) // Check interval
        }
    }
}

fun stopAutoLoad() {
    autoLoadJob?.cancel()
    autoLoadJob = null
}
```

### List State Preservation
```kotlin
// Save before disappear
saveListState() // Saves position + top offset

// Restore on appear
loadListState() // Restores scroll position
```

### Error Handling with Dialogs
```kotlin
ASProcessingDialog.showProcessingDialog("載入中")
// ... async operation
ASProcessingDialog.dismissProcessingDialog()
```

## Key Files Reference
- `TelnetListPage.kt` - Base list page with block loading
- `BoardMainPage.kt` - Main board view (has known bugs)
- `ASRunner.kt` - Threading wrapper (line 1-100)
- `ASNavigationController.kt` - Page stack manager
- `BahamutStateHandler.kt` - Telnet response parser
- `PageContainer.kt` - Singleton page cache
- `TelnetClient.kt` - Telnet connection manager

## Common Pitfalls
1. **Multiple adapter updates** → `ListView.IllegalStateException`
2. **Missing ASRunner wrapper** → UI thread violations
3. **Forgetting object recycling** → Memory leaks
4. **Direct page instantiation** → Use PageContainer
5. **Sync operations on main thread** → Use `ASCoroutine.runInNewCoroutine`

## Testing Workflow Changes
After modifying list pages:
1. Navigate to board (e.g., C_Chat)
2. Post article → Check for crash
3. Scroll rapidly → Verify smooth loading
4. Background/foreground → Check state preservation
5. Monitor memory (object pools should prevent leaks)
