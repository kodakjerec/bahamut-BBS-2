package com.kota.Bahamut.DataModels;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.Iterator;
import java.util.List;

public class BookmarkList {
    private String _board = "";
    private List<Bookmark> _bookmarks = new ArrayList();
    private List<Bookmark> _history_bookmarks = new ArrayList();
    public int limit = 20;

    public BookmarkList(String aBoardName) {
        this._board = aBoardName;
    }

    public int getBookmarkSize() {
        return this._bookmarks.size();
    }

    public Bookmark getBookmark(int index) {
        return this._bookmarks.get(index);
    }

    public void addBookmark(Bookmark bookmark) {
        this._bookmarks.add(bookmark);
    }

    public Bookmark removeBookmark(int index) {
        return this._bookmarks.remove(index);
    }

    public void clear() {
        this._bookmarks.clear();
    }

    public int getHistoryBookmarkSize() {
        return this._history_bookmarks.size();
    }

    public Bookmark getHistoryBookmark(int index) {
        return this._history_bookmarks.get(index);
    }

    public void addHistoryBookmark(String aKeywork) {
        Bookmark new_bookmark = null;
        Iterator<Bookmark> it = this._history_bookmarks.iterator();
        while (true) {
            if (!it.hasNext()) {
                break;
            }
            Bookmark bookmark = it.next();
            if (bookmark.getKeyword().equals(aKeywork)) {
                new_bookmark = bookmark;
                this._history_bookmarks.remove(bookmark);
                break;
            }
        }
        if (new_bookmark == null) {
            new_bookmark = new Bookmark();
            new_bookmark.setBoard(this._board);
            new_bookmark.setKeyword(aKeywork);
        }
        this._history_bookmarks.add(0, new_bookmark);
        while (this._history_bookmarks.size() > this.limit) {
            this._history_bookmarks.remove(this._history_bookmarks.get(this._history_bookmarks.size() - 1));
        }
    }

    public void addHistoryBookmark(Bookmark aBookmark) {
        Iterator<Bookmark> it = this._history_bookmarks.iterator();
        while (true) {
            if (!it.hasNext()) {
                break;
            }
            Bookmark bookmark = it.next();
            if (bookmark.getKeyword().equals(aBookmark.getKeyword())) {
                this._history_bookmarks.remove(bookmark);
                break;
            }
        }
        this._history_bookmarks.add(0, aBookmark);
        while (this._history_bookmarks.size() > this.limit) {
            this._history_bookmarks.remove(this._history_bookmarks.get(this._history_bookmarks.size() - 1));
        }
    }

    public Bookmark removeHistoryBookmark(int index) {
        return this._history_bookmarks.remove(index);
    }

    public void clearHistoryBookmark() {
        this._history_bookmarks.clear();
    }

    public void printHistoryBookmark() {
        for (int i = 0; i < this._history_bookmarks.size(); i++) {
            System.out.println((i + 1) + ". " + this._history_bookmarks.get(i).getTitle());
        }
    }

    public void loadTitleList(List<Bookmark> aList) {
        aList.clear();
        aList.addAll(this._bookmarks);
    }

    public void loadHistoryList(List<Bookmark> aList) {
        aList.clear();
        aList.addAll(this._history_bookmarks);
    }

    /* access modifiers changed from: package-private */
    public void sort() {
        Collections.sort(this._bookmarks, new Comparator<Bookmark>() {
            public int compare(Bookmark o1, Bookmark o2) {
                if (o1.index == 0 && o2.index == 0) {
                    System.out.println("error");
                }
                if (o1.index < o2.index) {
                    return -1;
                }
                if (o1.index > o2.index) {
                    return 1;
                }
                return 0;
            }
        });
        Collections.sort(this._history_bookmarks, new Comparator<Bookmark>() {
            public int compare(Bookmark o1, Bookmark o2) {
                if (o1.index == 0 && o2.index == 0) {
                    System.out.println("error");
                }
                if (o1.index < o2.index) {
                    return -1;
                }
                if (o1.index > o2.index) {
                    return 1;
                }
                return 0;
            }
        });
    }
}
