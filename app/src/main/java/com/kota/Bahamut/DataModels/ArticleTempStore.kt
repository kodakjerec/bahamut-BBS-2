package com.kota.Bahamut.DataModels

import android.content.Context
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
    @JvmField
    var articles: Vector<ArticleTemp> = Vector<ArticleTemp>()

    constructor(context: Context?, aFilePath: String?) {
        this._context = context
        this._file_path = aFilePath
        for (i in 0..9) {
            this.articles.add(ArticleTemp())
        }
    }

    constructor(context: Context?) {
        this._context = context
        for (i in 0..9) {
            this.articles.add(ArticleTemp())
        }
        load()
    }

    fun load() {
        println("load article store from file")
        var load_from_file = false
        if (this._file_path != null && this._file_path!!.length > 0) {
            val file = File(this._file_path)
            if (file.exists()) {
                try {
                    val file_input_stream: InputStream = FileInputStream(file)
                    val input_stream = ObjectInputStream(file_input_stream)
                    importFromStream(input_stream)
                    input_stream.close()
                    file_input_stream.close()
                    file.delete()
                    load_from_file = true
                } catch (e: IOException) {
                    Log.e(javaClass.getSimpleName(), (if (e.message != null) e.message else "")!!)
                }
            }
        }
        if (load_from_file) {
            store()
        }
        if (!load_from_file && this._context != null) {
            try {
                val save_data: String = this._context.getSharedPreferences("article_temp", 0)
                    .getString("save_data", "")!!
                if (save_data.length > 0) {
                    importFromJSON(JSONObject(save_data))
                }
            } catch (e2: Exception) {
                e2.printStackTrace()
            }
        }
    }

    fun store() {
        if (this._context != null) {
            try {
                val perf = this._context.getSharedPreferences("article_temp", 0)
                perf.edit().putString("save_data", exportToJSON().toString()).commit()
            } catch (e: Exception) {
                Log.e(javaClass.getSimpleName(), (if (e.message != null) e.message else "")!!)
            }
        }
    }

    @Throws(JSONException::class)
    fun importFromJSON(obj: JSONObject) {
        val data = obj.getJSONArray("data")
        this.articles.clear()
        for (i in 0..<data.length()) {
            val item_data = data.getJSONObject(i)
            val temp = ArticleTemp()
            temp.importFromJSON(item_data)
            this.articles.add(temp)
        }
    }

    @Throws(JSONException::class)
    fun exportToJSON(): JSONObject {
        val obj = JSONObject()
        val save_data = JSONArray()
        for (article in this.articles) {
            save_data.put(article.exportToJSON())
        }
        obj.put("data", save_data)
        return obj
    }

    @Throws(IOException::class)
    fun importFromStream(aStream: ObjectInputStream) {
        aStream.readInt()
        val size = aStream.readInt()
        for (i in 0..<size) {
            val article = ArticleTemp()
            article.importFromStream(aStream)
            this.articles.add(article)
        }
    }

    companion object {
        const val version: Int = 1
        @JvmStatic
        fun upgrade(context: Context?, aFilePath: String?) {
            ArticleTempStore(context, aFilePath).load()
        }
    }
}
