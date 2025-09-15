package com.kota.Bahamut.DataModels
/*
  書籤清單的儲存
 */

import android.content.Context
import android.content.SharedPreferences
import android.util.Log
import com.kota.Bahamut.Service.CloudBackup
import com.kota.Bahamut.Service.NotificationSettings
import com.kota.Bahamut.Service.TempSettings
import org.json.JSONArray
import org.json.JSONException
import org.json.JSONObject
import java.io.File
import java.io.FileInputStream
import java.io.IOException
import java.io.InputStream
import java.io.ObjectInputStream
import java.util.Vector

class BookmarkStore(private var _file_path: String?, private val _context: Context?) {
    private val _bookmarks = mutableMapOf<String, BookmarkList>()
    private val _global_bookmarks = BookmarkList("")
    private var _owner = ""
    private var _version = 1

    init {
        if (_context == null) {
            println("Bookmark context can't null.")
        }
    }

    fun getBookmarkList(aBoardName: String): BookmarkList {
        return _bookmarks[aBoardName] ?: run {
            val list = BookmarkList(aBoardName)
            _bookmarks[aBoardName] = list
            list
        }
    }

    fun getTotalBookmarkList(): Vector<Bookmark> {
        val totalList = Vector<Bookmark>()
        for (key in _bookmarks.keys) {
            val bookmarkList = getBookmarkList(key)
            for (i in 0 until bookmarkList.getBookmarkSize()) {
                val bookmark = bookmarkList.getBookmark(i)
                bookmark.index = i
                bookmark.optional = Bookmark.OPTIONAL_BOOKMARK
                totalList.add(bookmark)
            }
            for (i2 in 0 until bookmarkList.getHistoryBookmarkSize()) {
                val bookmark2 = bookmarkList.getHistoryBookmark(i2)
                bookmark2.index = i2
                bookmark2.optional = Bookmark.OPTIONAL_STORY
                totalList.add(bookmark2)
            }
        }
        for (i3 in 0 until _global_bookmarks.getBookmarkSize()) {
            val bookmark3 = _global_bookmarks.getBookmark(i3)
            bookmark3.index = i3
            totalList.add(bookmark3)
        }
        return totalList
    }

    fun cleanBookmark() {
        _bookmarks.clear()
        _global_bookmarks.clear()
    }

    fun addBookmark(aBookmark: Bookmark) {
        val boardName = aBookmark.getBoard().trim()
        when {
            boardName.isEmpty() -> _global_bookmarks.addBookmark(aBookmark)
            aBookmark.optional == Bookmark.OPTIONAL_STORY -> getBookmarkList(boardName).addHistoryBookmark(aBookmark)
            else -> getBookmarkList(boardName).addBookmark(aBookmark)
        }
    }

    /** 儲存書籤 */
    fun store() {
        println("save bookmark store to file")
        if (_context != null) {
            val obj = exportToJSON()
            if (obj != null) {
                _context.getSharedPreferences("bookmark", 0).edit().putString("save_data", obj.toString()).commit()
            }
        }

        // 雲端備份
        if (NotificationSettings.getCloudSave()) {
            val cloudBackup = CloudBackup()
            cloudBackup.backup()
        }
    }

    /** 儲存書籤, 但是不通知雲端 */
    fun storeWithoutCloud() {
        println("save bookmark store to file")
        if (_context != null) {
            val obj = exportToJSON()
            if (obj != null) {
                _context.getSharedPreferences("bookmark", 0).edit().putString("save_data", obj.toString()).commit()
            }
        }
    }

    private fun load(): BookmarkStore {
        println("load bookmark store from file")
        var loadFile = false
        if (_file_path.isNullOrEmpty()) {
            println("bookmark file not exists")
        } else {
            val file = File(_file_path!!)
            if (file.exists()) {
                println("bookmark file exists")
                try {
                    val fileInputStream: InputStream = FileInputStream(file)
                    val inputStream = ObjectInputStream(fileInputStream)
                    importFromStream(inputStream)
                    inputStream.close()
                    fileInputStream.close()
                } catch (e: IOException) {
                    Log.e(javaClass.simpleName, e.message ?: "")
                }
                file.delete()
                loadFile = true
            }
        }
        if (loadFile) {
            store()
        }
        if (!loadFile && _context != null) {
            val perf: SharedPreferences? = _context.getSharedPreferences("bookmark", 0)
            val saveData = perf?.getString("save_data", "")
            if (!saveData.isNullOrEmpty()) {
                try {
                    importFromJSON(JSONObject(saveData))
                } catch (e2: JSONException) {
                    e2.printStackTrace()
                }
            }
        }
        return this
    }

    @Throws(IOException::class)
    fun importFromStream(aStream: ObjectInputStream) {
        cleanBookmark()
        _version = aStream.readInt()
        _owner = aStream.readUTF()
        val size = aStream.readInt()
        for (i in 0 until size) {
            addBookmark(Bookmark(aStream))
        }
        val extData = ByteArray(aStream.readInt())
        aStream.read(extData)
    }

    fun importFromJSON(obj: JSONObject) {
        cleanBookmark()
        try {
            _version = obj.getInt("version")
            _owner = obj.getString("owner")
            val data = obj.getJSONArray("data")
            for (i in 0 until data.length()) {
                addBookmark(Bookmark(data.getJSONObject(i)))
            }
        } catch (e: Exception) {
            Log.e(javaClass.simpleName, e.message ?: "")
        }
        sortBookmarks()
        println("load " + getTotalBookmarkList().size + " bookmarks.")
    }

    private fun sortBookmarks() {
        for (key in _bookmarks.keys) {
            getBookmarkList(key).sort()
        }
        _global_bookmarks.sort()
    }

    fun exportToJSON(): JSONObject? {
        val obj = JSONObject()
        return try {
            obj.put("version", _version)
            obj.put("owner", _owner)
            val data = JSONArray()
            for (bookmark in getTotalBookmarkList()) {
                data.put(bookmark.exportToJSON())
            }
            obj.put("data", data)
            obj
        } catch (e: Exception) {
            Log.e(javaClass.simpleName, e.message ?: "")
            null
        }
    }

    companion object {
        fun upgrade(context: Context, aFilePath: String) {
            TempSettings.bookmarkStore = BookmarkStore(aFilePath, context).load()
        }
    }
}
