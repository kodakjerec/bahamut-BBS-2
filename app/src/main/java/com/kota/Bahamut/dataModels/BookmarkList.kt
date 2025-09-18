package com.kota.Bahamut.dataModels

/*
 書籤元件 清單
*/
class BookmarkList(private val myBoard: String?) {
    private val bookmarks: MutableList<Bookmark?> = ArrayList()
    private val historyBookmarks: MutableList<Bookmark?> = ArrayList()
    var limit: Int = 40

    val bookmarkSize: Int
        get() = bookmarks.size

    fun getBookmark(index: Int): Bookmark? {
        return bookmarks[index]
    }

    fun addBookmark(bookmark: Bookmark?) {
        bookmarks.add(bookmark)
    }

    fun removeBookmark(index: Int) {
        bookmarks.removeAt(index)
    }

    fun loadBookmarkList(aList: MutableList<Bookmark?>) {
        aList.clear()
        aList.addAll(bookmarks)
    }

    fun updateBookmark(index: Int, bookmark: Bookmark?) {
        bookmarks[index] = bookmark
    }

    fun clear() {
        bookmarks.clear()
    }

    val historyBookmarkSize: Int
        get() = historyBookmarks.size

    fun getHistoryBookmark(index: Int): Bookmark? {
        return historyBookmarks[index]
    }

    /*
    傳入 string 新增 bookmark
     */
    fun addHistoryBookmark(keyWord: String?) {
        var newBookmark: Bookmark? = null
        val it = historyBookmarks.iterator()
        while (true) {
            if (!it.hasNext()) {
                break
            }
            val bookmark = it.next()
            if (bookmark?.keyword == keyWord) {
                newBookmark = bookmark
                historyBookmarks.remove(bookmark)
                break
            }
        }
        if (newBookmark == null) {
            newBookmark = Bookmark()
            newBookmark.board = myBoard
            newBookmark.keyword = keyWord
        }
        historyBookmarks.add(0, newBookmark)
        while (historyBookmarks.size > limit) {
            historyBookmarks.remove(historyBookmarks[historyBookmarks.size - 1])
        }
    }

    /*
    傳入 bookmark 新增 bookmark
     */
    fun addHistoryBookmark(aBookmark: Bookmark) {
        val it = historyBookmarks.iterator()
        while (true) {
            if (!it.hasNext()) {
                break
            }
            val bookmark = it.next()
            if (bookmark?.keyword == aBookmark.keyword) {
                historyBookmarks.remove(bookmark)
                break
            }
        }
        historyBookmarks.add(0, aBookmark)
        while (historyBookmarks.size > limit) {
            historyBookmarks.remove(historyBookmarks[historyBookmarks.size - 1])
        }
    }

    fun removeHistoryBookmark(index: Int) {
        historyBookmarks.removeAt(index)
    }

    fun loadHistoryList(aList: MutableList<Bookmark?>) {
        aList.clear()
        aList.addAll(historyBookmarks)
    }

    fun sort() {
        bookmarks.sortWith(Comparator { o1: Bookmark?, o2: Bookmark? ->
                    if (o1!!.index == 0 && o2!!.index == 0) {
                        println("error")
                    }
                    o1.index.compareTo(o2!!.index)
                })
        historyBookmarks.sortWith(Comparator { o1: Bookmark?, o2: Bookmark? ->
                    if (o1!!.index == 0 && o2!!.index == 0) {
                        println("error")
                    }
                    o1.index.compareTo(o2!!.index)
                })
    }
}
