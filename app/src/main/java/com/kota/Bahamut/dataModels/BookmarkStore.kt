package com.kota.Bahamut.dataModels

import android.content.Context
import android.content.SharedPreferences
import android.util.Log
import com.kota.Bahamut.service.CloudBackup
import com.kota.Bahamut.service.NotificationSettings.getCloudSave
import com.kota.Bahamut.service.TempSettings
import org.json.JSONArray
import org.json.JSONException
import org.json.JSONObject
import java.io.File
import java.io.FileInputStream
import java.io.IOException
import java.io.InputStream
import java.io.ObjectInputStream
import java.util.Vector

/*
 書籤清單的儲存
*/

class BookmarkStore(val _context: Context?, var _file_path: String?) {
    val _bookmarks: MutableMap<String?, BookmarkList?> = HashMap<String?, BookmarkList?>()
    val _global_bookmarks: BookmarkList = BookmarkList("")
    var _owner: String? = ""
    var _version: Int = 1

    init {
        if (this._context == null) {
            println("Bookmark context can't null.")
        }
    }

    fun getBookmarkList(aBoardName: String?): BookmarkList {
        if (this._bookmarks.containsKey(aBoardName)) {
            val item = this._bookmarks.get(aBoardName)
            if (item != null) return item
        }
        val list = BookmarkList(aBoardName)
        this._bookmarks.put(aBoardName, list)
        return list
    }

    val totalBookmarkList: Vector<Bookmark>
        get() {
            val total_list =
                Vector<Bookmark>()
            for (key in this._bookmarks.keys) {
                val bookmark_list = getBookmarkList(key)
                for (i in 0..<bookmark_list.getBookmarkSize()) {
                    val bookmark =
                        bookmark_list.getBookmark(i)
                    bookmark.index = i
                    bookmark.optional =
                        Bookmark.Companion.OPTIONAL_BOOKMARK
                    total_list.add(bookmark)
                }
                for (i2 in 0..<bookmark_list.getHistoryBookmarkSize()) {
                    val bookmark2 =
                        bookmark_list.getHistoryBookmark(i2)
                    bookmark2.index = i2
                    bookmark2.optional =
                        Bookmark.Companion.OPTIONAL_STORY
                    total_list.add(bookmark2)
                }
            }
            for (i3 in 0..<this._global_bookmarks.getBookmarkSize()) {
                val bookmark3 =
                    this._global_bookmarks.getBookmark(i3)
                bookmark3.index = i3
                total_list.add(bookmark3)
            }
            return total_list
        }

    fun cleanBookmark() {
        this._bookmarks.clear()
        this._global_bookmarks.clear()
    }

    fun addBookmark(aBookmark: Bookmark) {
        val board_name = aBookmark.getBoard().trim { it <= ' ' }
        if (board_name.length == 0) {
            this._global_bookmarks.addBookmark(aBookmark)
        } else if (aBookmark.optional == Bookmark.Companion.OPTIONAL_STORY) {
            getBookmarkList(board_name).addHistoryBookmark(aBookmark)
        } else {
            getBookmarkList(board_name).addBookmark(aBookmark)
        }
    }

    /** 儲存書籤  */
    fun store() {
        val obj: JSONObject?
        println("save bookmark store to file")
        if (this._context != null && (exportToJSON().also { obj = it }) != null) {
            this._context.getSharedPreferences("bookmark", 0).edit()
                .putString("save_data", obj.toString()).commit()
        }

        // 雲端備份
        if (getCloudSave()) {
            val cloudBackup = CloudBackup()
            cloudBackup.backup()
        }
    }

    /** 儲存書籤, 但是不通知雲端  */
    fun storeWithoutCloud() {
        val obj: JSONObject?
        println("save bookmark store to file")
        if (this._context != null && (exportToJSON().also { obj = it }) != null) {
            this._context.getSharedPreferences("bookmark", 0).edit()
                .putString("save_data", obj.toString()).commit()
        }
    }

    private fun load(): BookmarkStore {
        val perf: SharedPreferences?
        val save_data: String?
        println("load bookmark store from file")
        var load_file = false
        if (this._file_path == null || this._file_path!!.length == 0) {
            println("bookmark file not exists")
        } else {
            val file = File(this._file_path)
            if (file.exists()) {
                println("bookmark file exists")
                try {
                    val file_input_stream: InputStream = FileInputStream(file)
                    val input_stream = ObjectInputStream(file_input_stream)
                    importFromStream(input_stream)
                    input_stream.close()
                    file_input_stream.close()
                } catch (e: IOException) {
                    Log.e(javaClass.simpleName, (if (e.message != null) e.message else "")!!)
                }
                file.delete()
                load_file = true
            }
        }
        if (load_file) {
            store()
        }
        if (!load_file && this._context != null && (this._context.getSharedPreferences(
                "bookmark",
                0
            ).also { perf = it }) != null && (perf!!.getString("save_data", "")
                .also { save_data = it }) != null && save_data!!.length > 0
        ) {
            try {
                importFromJSON(JSONObject(save_data))
            } catch (e2: JSONException) {
                e2.printStackTrace()
            }
        }
        return this
    }

    @Throws(IOException::class)
    fun importFromStream(aStream: ObjectInputStream) {
        cleanBookmark()
        this._version = aStream.readInt()
        this._owner = aStream.readUTF()
        val size = aStream.readInt()
        for (i in 0..<size) {
            addBookmark(Bookmark(aStream))
        }
        val _ext_data = ByteArray(aStream.readInt())
        aStream.read(_ext_data)
    }

    fun importFromJSON(obj: JSONObject) {
        cleanBookmark()
        try {
            this._version = obj.getInt("version")
            this._owner = obj.getString("owner")
            val data = obj.getJSONArray("data")
            for (i in 0..<data.length()) {
                addBookmark(Bookmark(data.getJSONObject(i)))
            }
        } catch (e: Exception) {
            Log.e(javaClass.simpleName, (if (e.message != null) e.message else "")!!)
        }
        sortBookmarks()
        println("load " + this.totalBookmarkList.size + " bookmarks.")
    }

    private fun sortBookmarks() {
        for (key in this._bookmarks.keys) {
            getBookmarkList(key).sort()
        }
        this._global_bookmarks.sort()
    }

    fun exportToJSON(): JSONObject {
        val obj = JSONObject()
        try {
            obj.put("version", this._version)
            obj.put("owner", this._owner)
            val data = JSONArray()
            for (bookmark in this.totalBookmarkList) {
                data.put(bookmark.exportToJSON())
            }
            obj.put("data", data)
        } catch (e: Exception) {
            Log.e(javaClass.simpleName, (if (e.message != null) e.message else "")!!)
        }
        return obj
    }

    companion object {
        @JvmStatic
        fun upgrade(context: Context?, aFilePath: String?) {
            TempSettings.bookmarkStore = BookmarkStore(context, aFilePath).load()
        }
    }
}
