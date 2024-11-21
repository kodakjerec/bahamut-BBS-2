package com.kota.Bahamut.DataModels;
/*
  書籤清單的儲存
 */

import android.content.Context;
import android.content.SharedPreferences;
import android.util.Log;

import androidx.annotation.NonNull;

import com.kota.Bahamut.Service.CloudBackup;
import com.kota.Bahamut.Service.NotificationSettings;
import com.kota.Bahamut.Service.TempSettings;

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
import java.util.Objects;
import java.util.Vector;

public class BookmarkStore {
    final Map<String, BookmarkList> _bookmarks = new HashMap<>();
    final Context _context;
    String _file_path;
    final BookmarkList _global_bookmarks = new BookmarkList("");
    String _owner = "";
    int _version = 1;

    public BookmarkStore(Context context, String aFilePath) {
        this._file_path = aFilePath;
        this._context = context;
        if (this._context == null) {
            System.out.println("Bookmark context can't null.");
        }
    }

    public static void upgrade(Context context, String aFilePath) {
        TempSettings.bookmarkStore = new BookmarkStore(context, aFilePath).load();
    }

    @NonNull
    public BookmarkList getBookmarkList(String aBoardName) {
        if (this._bookmarks.containsKey(aBoardName)) {
            return Objects.requireNonNull(this._bookmarks.get(aBoardName));
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
                bookmark2.optional = Bookmark.OPTIONAL_STORY;
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
        } else if (aBookmark.optional.equals(Bookmark.OPTIONAL_STORY)) {
            getBookmarkList(board_name).addHistoryBookmark(aBookmark);
        } else {
            getBookmarkList(board_name).addBookmark(aBookmark);
        }
    }

    /** 儲存書籤 */
    public void store() {
        JSONObject obj;
        System.out.println("save bookmark store to file");
        if (this._context != null && (obj = exportToJSON()) != null) {
            this._context.getSharedPreferences("bookmark", 0).edit().putString("save_data", obj.toString()).commit();
        }

        // 雲端備份
        if (NotificationSettings.getCloudSave()) {
            CloudBackup cloudBackup = new CloudBackup();
            cloudBackup.backup();
        }
    }

    /** 儲存書籤, 但是不通知雲端 */
    public void storeWithoutCloud() {
        JSONObject obj;
        System.out.println("save bookmark store to file");
        if (this._context != null && (obj = exportToJSON()) != null) {
            this._context.getSharedPreferences("bookmark", 0).edit().putString("save_data", obj.toString()).commit();
        }
    }

    private BookmarkStore load() {
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
                    Log.e(getClass().getSimpleName(), Objects.requireNonNull(e.getMessage()));
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
        return this;
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
            Log.e(getClass().getSimpleName(), Objects.requireNonNull(e.getMessage()));
        }
        sortBookmarks();
        System.out.println("load " + getTotalBookmarkList().size() + " bookmarks.");
    }

    private void sortBookmarks() {
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
            Log.e(getClass().getSimpleName(), Objects.requireNonNull(e.getMessage()));
        }
        return obj;
    }
}
