package com.kota.Bahamut.DataModels

/*
 書籤元件 清單
*/
class BookmarkList(private val _board: String?) {
    private val _bookmarks: MutableList<Bookmark?> = ArrayList<Bookmark?>()
    private val _history_bookmarks: MutableList<Bookmark?> = ArrayList<Bookmark?>()
    var limit: Int = 40

    val bookmarkSize: Int
        get() = _bookmarks.size

    fun getBookmark(index: Int): Bookmark? {
        return _bookmarks.get(index)
    }

    fun addBookmark(bookmark: Bookmark?) {
        _bookmarks.add(bookmark)
    }

    fun removeBookmark(index: Int) {
        _bookmarks.removeAt(index)
    }

    fun loadBookmarkList(aList: MutableList<Bookmark?>) {
        aList.clear()
        aList.addAll(_bookmarks)
    }

    fun updateBookmark(index: Int, bookmark: Bookmark?) {
        _bookmarks.set(index, bookmark)
    }

    fun clear() {
        _bookmarks.clear()
    }

    val historyBookmarkSize: Int
        get() = _history_bookmarks.size

    fun getHistoryBookmark(index: Int): Bookmark? {
        return _history_bookmarks.get(index)
    }

    /*
    傳入 string 新增 bookmark
     */
    fun addHistoryBookmark(_key_work: String?) {
        var new_bookmark: Bookmark? = null
        val it = _history_bookmarks.iterator()
        while (true) {
            if (!it.hasNext()) {
                break
            }
            val bookmark = it.next()
            if (bookmark.getKeyword() == _key_work) {
                new_bookmark = bookmark
                _history_bookmarks.remove(bookmark)
                break
            }
        }
        if (new_bookmark == null) {
            new_bookmark = Bookmark()
            new_bookmark.setBoard(_board)
            new_bookmark.setKeyword(_key_work)
        }
        _history_bookmarks.add(0, new_bookmark)
        while (_history_bookmarks.size > limit) {
            _history_bookmarks.remove(_history_bookmarks.get(_history_bookmarks.size - 1))
        }
    }

    /*
    傳入 bookmark 新增 bookmark
     */
    fun addHistoryBookmark(aBookmark: Bookmark) {
        val it = _history_bookmarks.iterator()
        while (true) {
            if (!it.hasNext()) {
                break
            }
            val bookmark = it.next()
            if (bookmark.getKeyword() == aBookmark.getKeyword()) {
                _history_bookmarks.remove(bookmark)
                break
            }
        }
        _history_bookmarks.add(0, aBookmark)
        while (_history_bookmarks.size > limit) {
            _history_bookmarks.remove(_history_bookmarks.get(_history_bookmarks.size - 1))
        }
    }

    fun removeHistoryBookmark(index: Int) {
        _history_bookmarks.removeAt(index)
    }

    fun loadHistoryList(aList: MutableList<Bookmark?>) {
        aList.clear()
        aList.addAll(_history_bookmarks)
    }

    fun sort() {
        _bookmarks.sort(Comparator { o1: Bookmark?, o2: Bookmark? ->
            if (o1!!.index == 0 && o2!!.index == 0) {
                println("error")
            }
            Integer.compare(o1.index, o2!!.index)
        })
        _history_bookmarks.sort(Comparator { o1: Bookmark?, o2: Bookmark? ->
            if (o1!!.index == 0 && o2!!.index == 0) {
                println("error")
            }
            Integer.compare(o1.index, o2!!.index)
        })
    }
}
