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
import androidx.core.content.edit

/*
 書籤清單的儲存
*/

class BookmarkStore(val context: Context?, var filePath: String?) {
    val bookmarks: MutableMap<String?, BookmarkList?> = HashMap()
    val globalBookmarks: BookmarkList = BookmarkList("")
    var owner: String? = ""
    var version: Int = 1

    init {
        if (this.context == null) {
            println("Bookmark context can't null.")
        }
    }

    fun getBookmarkList(aBoardName: String?): BookmarkList {
        if (this.bookmarks.containsKey(aBoardName)) {
            val item = this.bookmarks[aBoardName]
            if (item != null) return item
        }
        val list = BookmarkList(aBoardName)
        this.bookmarks.put(aBoardName, list)
        return list
    }

    val totalBookmarkList: Vector<Bookmark>
        get() {
            val totalList =
                Vector<Bookmark>()
            for (key in this.bookmarks.keys) {
                val bookmarkList = getBookmarkList(key)
                for (i in 0..<bookmarkList.bookmarkSize) {
                    val bookmark =
                        bookmarkList.getBookmark(i)
                    bookmark?.index = i
                    bookmark?.optional =
                        Bookmark.Companion.OPTIONAL_BOOKMARK
                    totalList.add(bookmark)
                }
                for (i2 in 0..<bookmarkList.historyBookmarkSize) {
                    val bookmark2 =
                        bookmarkList.getHistoryBookmark(i2)
                    bookmark2?.index = i2
                    bookmark2?.optional =
                        Bookmark.Companion.OPTIONAL_STORY
                    totalList.add(bookmark2)
                }
            }
            for (i3 in 0..<this.globalBookmarks.bookmarkSize) {
                val bookmark3 =
                    this.globalBookmarks.getBookmark(i3)
                bookmark3?.index = i3
                totalList.add(bookmark3)
            }
            return totalList
        }

    fun cleanBookmark() {
        this.bookmarks.clear()
        this.globalBookmarks.clear()
    }

    fun addBookmark(aBookmark: Bookmark) {
        val boardName = aBookmark.board?.trim { it <= ' ' }
        if (boardName?.isEmpty() == true) {
            this.globalBookmarks.addBookmark(aBookmark)
        } else if (aBookmark.optional == Bookmark.Companion.OPTIONAL_STORY) {
            getBookmarkList(boardName).addHistoryBookmark(aBookmark)
        } else {
            getBookmarkList(boardName).addBookmark(aBookmark)
        }
    }

    /** 儲存書籤  */
    fun store() {
        val obj: JSONObject? = null
        println("save bookmark store to file")
        this.context?.getSharedPreferences("bookmark", 0)?.edit {
            putString("save_data", obj.toString())
        }

        // 雲端備份
        if (getCloudSave()) {
            val cloudBackup = CloudBackup()
            cloudBackup.backup()
        }
    }

    /** 儲存書籤, 但是不通知雲端  */
    fun storeWithoutCloud() {
        val obj: JSONObject? = null
        println("save bookmark store to file")
        this.context?.getSharedPreferences("bookmark", 0)?.edit {
            putString("save_data", obj.toString())
        }
    }

    private fun load(): BookmarkStore {
        var perf: SharedPreferences? = null
        var saveData: String? = ""
        println("load bookmark store from file")
        var loadFile = false
        if (this.filePath == null) {
            println("bookmark file not exists")
        } else {
            val file = File(this.filePath!!)
            if (file.exists()) {
                println("bookmark file exists")
                try {
                    val fileInputStream: InputStream = FileInputStream(file)
                    val inputStream = ObjectInputStream(fileInputStream)
                    importFromStream(inputStream)
                    inputStream.close()
                    fileInputStream.close()
                } catch (e: IOException) {
                    Log.e(javaClass.simpleName, (if (e.message != null) e.message else "")!!)
                }
                file.delete()
                loadFile = true
            }
        }
        if (loadFile) {
            store()
        }
        if (!loadFile && this.context != null && (this.context.getSharedPreferences(
                "bookmark",
                0
            ).also { perf = it }) != null && (perf?.getString("save_data", "")
                .also { saveData = it }) != null
        ) {
            try {
                importFromJSON(JSONObject(saveData!!))
            } catch (e2: JSONException) {
                e2.printStackTrace()
            }
        }
        return this
    }

    @Throws(IOException::class)
    fun importFromStream(aStream: ObjectInputStream) {
        cleanBookmark()
        this.version = aStream.readInt()
        this.owner = aStream.readUTF()
        val size = aStream.readInt()
        for (i in 0..<size) {
            addBookmark(Bookmark(aStream))
        }
        val extData = ByteArray(aStream.readInt())
        aStream.read(extData)
    }

    fun importFromJSON(obj: JSONObject) {
        cleanBookmark()
        try {
            this.version = obj.getInt("version")
            this.owner = obj.getString("owner")
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
        for (key in this.bookmarks.keys) {
            getBookmarkList(key).sort()
        }
        this.globalBookmarks.sort()
    }

    fun exportToJSON(): JSONObject {
        val obj = JSONObject()
        try {
            obj.put("version", this.version)
            obj.put("owner", this.owner)
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
