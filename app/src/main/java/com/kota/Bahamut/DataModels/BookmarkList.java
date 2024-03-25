package com.kota.Bahamut.DataModels;
/*
  書籤元件 清單
 */
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

public class BookmarkList {
    private final String _board;
    private final List<Bookmark> _bookmarks = new ArrayList<>();
    private final List<Bookmark> _history_bookmarks = new ArrayList<>();
    public int limit = 20;

    public BookmarkList(String aBoardName) {
        _board = aBoardName;
    }

    public int getBookmarkSize() {
        return _bookmarks.size();
    }

    public Bookmark getBookmark(int index) {
        return _bookmarks.get(index);
    }

    public void addBookmark(Bookmark bookmark) {
        _bookmarks.add(bookmark);
    }

    public Bookmark removeBookmark(int index) {
        return _bookmarks.remove(index);
    }
    public void updateBookmark(int index, Bookmark bookmark) {
        _bookmarks.set(index, bookmark);
    }

    public void clear() {
        _bookmarks.clear();
    }

    public int getHistoryBookmarkSize() {
        return _history_bookmarks.size();
    }

    public Bookmark getHistoryBookmark(int index) {
        return _history_bookmarks.get(index);
    }

    public void addHistoryBookmark(String _key_work) {
        Bookmark new_bookmark = null;
        Iterator<Bookmark> it = _history_bookmarks.iterator();
        while (true) {
            if (!it.hasNext()) {
                break;
            }
            Bookmark bookmark = it.next();
            if (bookmark.getKeyword().equals(_key_work)) {
                new_bookmark = bookmark;
                _history_bookmarks.remove(bookmark);
                break;
            }
        }
        if (new_bookmark == null) {
            new_bookmark = new Bookmark();
            new_bookmark.setBoard(_board);
            new_bookmark.setKeyword(_key_work);
        }
        _history_bookmarks.add(0, new_bookmark);
        while (_history_bookmarks.size() > limit) {
            _history_bookmarks.remove(_history_bookmarks.get(_history_bookmarks.size() - 1));
        }
    }

    public void addHistoryBookmark(Bookmark aBookmark) {
        Iterator<Bookmark> it = _history_bookmarks.iterator();
        while (true) {
            if (!it.hasNext()) {
                break;
            }
            Bookmark bookmark = it.next();
            if (bookmark.getKeyword().equals(aBookmark.getKeyword())) {
                _history_bookmarks.remove(bookmark);
                break;
            }
        }
        _history_bookmarks.add(0, aBookmark);
        while (_history_bookmarks.size() > limit) {
            _history_bookmarks.remove(_history_bookmarks.get(_history_bookmarks.size() - 1));
        }
    }

    public Bookmark removeHistoryBookmark(int index) {
        return _history_bookmarks.remove(index);
    }

    public void clearHistoryBookmark() {
        _history_bookmarks.clear();
    }

    public void printHistoryBookmark() {
        for (int i = 0; i < _history_bookmarks.size(); i++) {
            System.out.println((i + 1) + ". " + _history_bookmarks.get(i).getTitle());
        }
    }

    public void loadTitleList(List<Bookmark> aList) {
        aList.clear();
        aList.addAll(_bookmarks);
    }

    public void loadHistoryList(List<Bookmark> aList) {
        aList.clear();
        aList.addAll(_history_bookmarks);
    }

    public void sort() {
        _bookmarks.sort((o1, o2) -> {
            if (o1.index == 0 && o2.index == 0) {
                System.out.println("error");
            }
            return Integer.compare(o1.index, o2.index);
        });
        _history_bookmarks.sort((o1, o2) -> {
            if (o1.index == 0 && o2.index == 0) {
                System.out.println("error");
            }
            return Integer.compare(o1.index, o2.index);
        });
    }
}
