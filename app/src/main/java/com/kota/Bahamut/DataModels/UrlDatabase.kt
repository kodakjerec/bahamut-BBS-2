package com.kota.Bahamut.DataModels

import android.annotation.SuppressLint
import android.content.ContentValues
import android.content.Context
import android.database.Cursor
import android.database.SQLException
import android.database.sqlite.SQLiteDatabase
import android.database.sqlite.SQLiteOpenHelper
import android.util.Log
import com.kota.Telnet.TelnetClient
import java.util.Vector

class UrlDatabase(context: Context) : SQLiteOpenHelper(
    context,
    TelnetClient.getClient().username.lowercase().trim() + "_database",
    null,
    1
) {
    override fun onCreate(aDatabase: SQLiteDatabase) {
        try {
            var createTableQuery = """
                CREATE TABLE IF NOT EXISTS urls (
                    url TEXT PRIMARY KEY, 
                    title TEXT, 
                    description TEXT, 
                    imageUrl TEXT, 
                    isPic TEXT
                )
            """.trimIndent()

            aDatabase.execSQL(createTableQuery)
            createTableQuery = """
                CREATE TABLE IF NOT EXISTS shorten_urls (
                    shorten_url TEXT PRIMARY KEY, 
                    title TEXT, 
                    description TEXT, 
                    url TEXT
                )
            """.trimIndent()

            aDatabase.execSQL(createTableQuery)
        } catch (e: SQLException) {
            Log.e(javaClass.simpleName, e.message ?: "")
        }
    }

    override fun onUpgrade(sqLiteDatabase: SQLiteDatabase, i: Int, i1: Int) {
        // Not implemented
    }

    override fun onOpen(db: SQLiteDatabase) {
        onCreate(db)
    }

    fun addUrl(url: String, title: String, description: String, imageUrl: String, isPic: Boolean) {
        if (title.isEmpty() && description.isEmpty()) return
        
        val values = ContentValues().apply {
            put("url", url)
            put("title", title)
            put("description", description)
            put("imageUrl", imageUrl)
            put("isPic", isPic)
        }
        
        try {
            val db = writableDatabase
            db.insertWithOnConflict("urls", null, values, SQLiteDatabase.CONFLICT_IGNORE)
            db.close()
        } catch (ignored: Exception) {
        }
    }

    @SuppressLint("Range")
    fun getUrl(url: String): Vector<String>? {
        return try {
            val db = readableDatabase
            val columns = arrayOf("url", "title", "description", "imageUrl", "isPic")
            val selection = "url = ?"
            val selectionArgs = arrayOf(url)

            val cursor = db.query("urls", columns, selection, selectionArgs, null, null, null)

            if (cursor.moveToFirst()) {
                // 新資料
                val data = Vector<String>().apply {
                    add(cursor.getString(cursor.getColumnIndex("url")))
                    add(cursor.getString(cursor.getColumnIndex("title")))
                    add(cursor.getString(cursor.getColumnIndex("description")))
                    add(cursor.getString(cursor.getColumnIndex("imageUrl")))
                    add(cursor.getString(cursor.getColumnIndex("isPic")))
                }

                cursor.close()
                db.close()
                data
            } else {
                // 已有
                cursor.close()
                db.close()
                null
            }
        } catch (ignored: Exception) {
            null
        }
    }

    fun addShortenUrl(url: String, title: String, description: String, shortenUrl: String) {
        try {
            val db = writableDatabase

            val values = ContentValues().apply {
                put("shorten_url", shortenUrl)
                put("title", title)
                put("description", description)
                put("url", url)
            }
            db.delete("shorten_urls", "shorten_url=?", arrayOf(shortenUrl))
            db.insertWithOnConflict("shorten_urls", null, values, SQLiteDatabase.CONFLICT_IGNORE)
            db.close()
        } catch (ignored: Exception) {
        }
    }

    @SuppressLint("Range")
    fun getShortenUrls(): Vector<ShortenUrl> {
        return try {
            val db = readableDatabase
            val columns = arrayOf("shorten_url", "title", "description", "url")
            val selection = ""

            val cursor = db.query("shorten_urls", columns, selection, null, null, null, "rowid DESC")

            if (cursor.moveToFirst()) {
                val returnList = Vector<ShortenUrl>()
                do {
                    val data = ShortenUrl().apply {
                        shorten_url = cursor.getString(cursor.getColumnIndex("shorten_url"))
                        title = cursor.getString(cursor.getColumnIndex("title"))
                        description = cursor.getString(cursor.getColumnIndex("description"))
                        url = cursor.getString(cursor.getColumnIndex("url"))
                    }
                    returnList.add(data)
                } while (cursor.moveToNext())

                cursor.close()
                db.close()
                returnList
            } else {
                cursor.close()
                db.close()
                Vector()
            }
        } catch (ignored: Exception) {
            Vector()
        }
    }

    @SuppressLint("Range")
    fun getShortenUrl(url: String): Vector<ShortenUrl> {
        return try {
            val db = readableDatabase
            val columns = arrayOf("shorten_url", "title", "description", "url")
            val selection = "url = ?"
            val selectionArgs = arrayOf(url)

            val cursor = db.query("shorten_urls", columns, selection, selectionArgs, null, null, "rowid DESC")

            if (cursor.moveToFirst()) {
                val returnList = Vector<ShortenUrl>()
                do {
                    val data = ShortenUrl().apply {
                        shorten_url = cursor.getString(cursor.getColumnIndex("shorten_url"))
                        title = cursor.getString(cursor.getColumnIndex("title"))
                        description = cursor.getString(cursor.getColumnIndex("description"))
                        url = cursor.getString(cursor.getColumnIndex("url"))
                    }
                    returnList.add(data)
                } while (cursor.moveToNext())

                cursor.close()
                db.close()
                returnList
            } else {
                cursor.close()
                db.close()
                Vector()
            }
        } catch (ignored: Exception) {
            Vector()
        }
    }

    fun clearDb() {
        try {
            val db = writableDatabase
            db.execSQL("DELETE FROM urls WHERE rowid NOT IN (SELECT rowid FROM urls ORDER BY rowid DESC LIMIT 100) ")
            db.execSQL("DELETE FROM shorten_urls WHERE rowid NOT IN (SELECT rowid FROM shorten_urls ORDER BY rowid DESC LIMIT 100) ")
            onCreate(db)
        } catch (ignored: Exception) {
        }
    }
}
