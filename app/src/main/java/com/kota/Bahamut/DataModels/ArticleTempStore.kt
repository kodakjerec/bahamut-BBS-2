package com.kota.Bahamut.DataModels

import android.content.Context
import android.content.SharedPreferences
import android.util.Log
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
    戰巴哈專用暫存檔
    0~4 文章使用
    8  mail
    9  發文
 */
class ArticleTempStore {
    private val _context: Context?
    private var _file_path: String? = null
    var articles = Vector<ArticleTemp>()

    constructor(context: Context, aFilePath: String) {
        _context = context
        _file_path = aFilePath
        for (i in 0..9) {
            articles.add(ArticleTemp())
        }
    }

    constructor(context: Context) {
        _context = context
        for (i in 0..9) {
            articles.add(ArticleTemp())
        }
        load()
    }

    fun load() {
        println("load article store from file")
        var loadFromFile = false
        if (!_file_path.isNullOrEmpty()) {
            val file = File(_file_path!!)
            if (file.exists()) {
                try {
                    val fileInputStream: InputStream = FileInputStream(file)
                    val inputStream = ObjectInputStream(fileInputStream)
                    importFromStream(inputStream)
                    inputStream.close()
                    fileInputStream.close()
                    file.delete()
                    loadFromFile = true
                } catch (e: IOException) {
                    Log.e(javaClass.simpleName, e.message ?: "")
                }
            }
        }
        if (loadFromFile) {
            store()
        }
        if (!loadFromFile && _context != null) {
            try {
                val saveData = _context.getSharedPreferences("article_temp", 0).getString("save_data", "")
                if (!saveData.isNullOrEmpty()) {
                    importFromJSON(JSONObject(saveData))
                }
            } catch (e2: Exception) {
                e2.printStackTrace()
            }
        }
    }

    fun store() {
        if (_context != null) {
            try {
                val perf: SharedPreferences = _context.getSharedPreferences("article_temp", 0)
                perf.edit().putString("save_data", exportToJSON().toString()).commit()
            } catch (e: Exception) {
                Log.e(javaClass.simpleName, e.message ?: "")
            }
        }
    }

    @Throws(JSONException::class)
    fun importFromJSON(obj: JSONObject) {
        val data = obj.getJSONArray("data")
        articles.clear()
        for (i in 0 until data.length()) {
            val itemData = data.getJSONObject(i)
            val temp = ArticleTemp()
            temp.importFromJSON(itemData)
            articles.add(temp)
        }
    }

    @Throws(JSONException::class)
    fun exportToJSON(): JSONObject {
        val obj = JSONObject()
        val saveData = JSONArray()
        for (article in articles) {
            saveData.put(article.exportToJSON())
        }
        obj.put("data", saveData)
        return obj
    }

    @Throws(IOException::class)
    fun importFromStream(aStream: ObjectInputStream) {
        aStream.readInt()
        val size = aStream.readInt()
        for (i in 0 until size) {
            val article = ArticleTemp()
            article.importFromStream(aStream)
            articles.add(article)
        }
    }

    companion object {
        const val version = 1

        fun upgrade(context: Context, aFilePath: String) {
            ArticleTempStore(context, aFilePath).load()
        }
    }
}
