package com.kota.Bahamut.DataModels
/*
  書籤元件 清單
 */

class BookmarkList(private val _board: String) {
    private val _bookmarks = mutableListOf<Bookmark>()
    private val _history_bookmarks = mutableListOf<Bookmark>()
    var limit = 40

    fun getBookmarkSize(): Int {
        return _bookmarks.size
    }

    fun getBookmark(index: Int): Bookmark {
        return _bookmarks[index]
    }

    fun addBookmark(bookmark: Bookmark) {
        _bookmarks.add(bookmark)
    }

    fun removeBookmark(index: Int) {
        _bookmarks.removeAt(index)
    }

    fun loadBookmarkList(aList: MutableList<Bookmark>) {
        aList.clear()
        aList.addAll(_bookmarks)
    }

    fun updateBookmark(index: Int, bookmark: Bookmark) {
        _bookmarks[index] = bookmark
    }

    fun clear() {
        _bookmarks.clear()
    }

    fun getHistoryBookmarkSize(): Int {
        return _history_bookmarks.size
    }

    fun getHistoryBookmark(index: Int): Bookmark {
        return _history_bookmarks[index]
    }

    /*
    傳入 string 新增 bookmark
     */
    fun addHistoryBookmark(_key_work: String) {
        var newBookmark: Bookmark? = null
        val it = _history_bookmarks.iterator()
        while (it.hasNext()) {
            val bookmark = it.next()
            if (bookmark.getKeyword() == _key_work) {
                newBookmark = bookmark
                it.remove()
                break
            }
        }
        if (newBookmark == null) {
            newBookmark = Bookmark()
            newBookmark.setBoard(_board)
            newBookmark.setKeyword(_key_work)
        }
        _history_bookmarks.add(0, newBookmark)
        while (_history_bookmarks.size > limit) {
            _history_bookmarks.removeAt(_history_bookmarks.size - 1)
        }
    }

    /*
    傳入 bookmark 新增 bookmark
     */
    fun addHistoryBookmark(aBookmark: Bookmark) {
        val it = _history_bookmarks.iterator()
        while (it.hasNext()) {
            val bookmark = it.next()
            if (bookmark.getKeyword() == aBookmark.getKeyword()) {
                it.remove()
                break
            }
        }
        _history_bookmarks.add(0, aBookmark)
        while (_history_bookmarks.size > limit) {
            _history_bookmarks.removeAt(_history_bookmarks.size - 1)
        }
    }

    fun removeHistoryBookmark(index: Int) {
        _history_bookmarks.removeAt(index)
    }

    fun loadHistoryList(aList: MutableList<Bookmark>) {
        aList.clear()
        aList.addAll(_history_bookmarks)
    }

    fun sort() {
        _bookmarks.sortWith { o1, o2 ->
            if (o1.index == 0 && o2.index == 0) {
                println("error")
            }
            o1.index.compareTo(o2.index)
        }
        _history_bookmarks.sortWith { o1, o2 ->
            if (o1.index == 0 && o2.index == 0) {
                println("error")
            }
            o1.index.compareTo(o2.index)
        }
    }
}
