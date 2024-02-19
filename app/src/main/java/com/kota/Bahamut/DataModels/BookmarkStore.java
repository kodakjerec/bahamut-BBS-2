package com.kota.Bahamut.DataModels;
/**
 * 書籤清單的儲存
 */

import android.content.Context;
import android.content.SharedPreferences;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.ObjectInputStream;
import java.util.HashMap;
import java.util.Map;
import java.util.Vector;

public class BookmarkStore {
    private final Map<String, BookmarkList> _bookmarks = new HashMap();
    private final Context _context;
    private String _file_path = "";
    private final BookmarkList _global_bookmarks = new BookmarkList("");
    private String _owner = "";
    private int _version = 1;

    public BookmarkStore(Context context, String aFilePath) {
        this._file_path = aFilePath;
        this._context = context;
    }

    public BookmarkStore(Context context) {
        this._context = context;
        if (this._context == null) {
            System.out.println("Bookmark context can't null.");
        }
        load();
    }

    public static void upgrade(Context context, String aFilePath) {
        new BookmarkStore(context, aFilePath).load();
    }

    public BookmarkList getBookmarkList(String aBoardName) {
        if (this._bookmarks.containsKey(aBoardName)) {
            return this._bookmarks.get(aBoardName);
        }
        BookmarkList list = new BookmarkList(aBoardName);
        this._bookmarks.put(aBoardName, list);
        return list;
    }

    public Vector<Bookmark> getTotalBookmarkList() {
        Vector<Bookmark> total_list = new Vector<>();
        for (String key : this._bookmarks.keySet()) {
            BookmarkList bookmark_list = getBookmarkList(key);
            for (int i = 0; i < bookmark_list.getBookmarkSize(); i++) {
                Bookmark bookmark = bookmark_list.getBookmark(i);
                bookmark.index = i;
                bookmark.optional = Bookmark.OPTIONAL_BOOKMARK;
                total_list.add(bookmark);
            }
            for (int i2 = 0; i2 < bookmark_list.getHistoryBookmarkSize(); i2++) {
                Bookmark bookmark2 = bookmark_list.getHistoryBookmark(i2);
                bookmark2.index = i2;
                bookmark2.optional = Bookmark.OPTIONAL_HOSTORY;
                total_list.add(bookmark2);
            }
        }
        for (int i3 = 0; i3 < this._global_bookmarks.getBookmarkSize(); i3++) {
            Bookmark bookmark3 = this._global_bookmarks.getBookmark(i3);
            bookmark3.index = i3;
            total_list.add(bookmark3);
        }
        return total_list;
    }

    public void cleanBookmark() {
        this._bookmarks.clear();
        this._global_bookmarks.clear();
    }

    public void addBookmark(Bookmark aBookmark) {
        String board_name = aBookmark.getBoard().trim();
        if (board_name.length() == 0) {
            this._global_bookmarks.addBookmark(aBookmark);
        } else if (aBookmark.optional.equals(Bookmark.OPTIONAL_HOSTORY)) {
            getBookmarkList(board_name).addHistoryBookmark(aBookmark);
        } else {
            getBookmarkList(board_name).addBookmark(aBookmark);
        }
    }

    public void store() {
        JSONObject obj;
        System.out.println("save bookmark store to file");
        if (this._context != null && (obj = exportToJSON()) != null) {
            this._context.getSharedPreferences("bookmark", 0).edit().putString("save_data", obj.toString()).commit();
        }
    }

    private void load() {
        SharedPreferences perf;
        String save_data;
        System.out.println("load bookmark store from file");
        boolean load_file = false;
        if (this._file_path == null || this._file_path.length() == 0) {
            System.out.println("bookmark file not exists");
        } else {
            File file = new File(this._file_path);
            if (file.exists()) {
                System.out.println("bookmark file exists");
                try {
                    InputStream file_input_stream = new FileInputStream(file);
                    ObjectInputStream input_stream = new ObjectInputStream(file_input_stream);
                    importFromStream(input_stream);
                    input_stream.close();
                    file_input_stream.close();
                } catch (IOException e) {
                    e.printStackTrace();
                }
                file.delete();
                load_file = true;
            }
        }
        if (load_file) {
            store();
        }
        if (!load_file && this._context != null && (perf = this._context.getSharedPreferences("bookmark", 0)) != null && (save_data = perf.getString("save_data", "")) != null && save_data.length() > 0) {
            try {
                importFromJSON(new JSONObject(save_data));
            } catch (JSONException e2) {
                e2.printStackTrace();
            }
        }
    }

    public void importFromStream(ObjectInputStream aStream) throws IOException {
        cleanBookmark();
        this._version = aStream.readInt();
        this._owner = aStream.readUTF();
        int size = aStream.readInt();
        for (int i = 0; i < size; i++) {
            addBookmark(new Bookmark(aStream));
        }
        byte[] _ext_data = new byte[aStream.readInt()];
        aStream.read(_ext_data);
    }

    public void importFromJSON(JSONObject obj) {
        cleanBookmark();
        try {
            this._version = obj.getInt("version");
            this._owner = obj.getString("owner");
            JSONArray data = obj.getJSONArray("data");
            for (int i = 0; i < data.length(); i++) {
                addBookmark(new Bookmark(data.getJSONObject(i)));
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        sortBookmarks();
        System.out.println("load " + getTotalBookmarkList().size() + " bookmarks.");
    }

    /* access modifiers changed from: package-private */
    public void sortBookmarks() {
        for (String key : this._bookmarks.keySet()) {
            getBookmarkList(key).sort();
        }
        this._global_bookmarks.sort();
    }

    public JSONObject exportToJSON() {
        JSONObject obj = new JSONObject();
        try {
            obj.put("version", this._version);
            obj.put("owner", this._owner);
            JSONArray data = new JSONArray();
            for (Bookmark bookmark : getTotalBookmarkList()) {
                data.put(bookmark.exportToJSON());
            }
            obj.put("data", data);
        } catch (Exception e) {
            e.printStackTrace();
        }
        return obj;
    }
}
