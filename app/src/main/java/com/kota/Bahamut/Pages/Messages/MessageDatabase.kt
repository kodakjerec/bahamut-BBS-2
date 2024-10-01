package com.kota.Bahamut.Pages.Messages

import android.annotation.SuppressLint
import android.content.ContentValues
import android.content.Context
import android.database.SQLException
import android.database.sqlite.SQLiteDatabase
import android.database.sqlite.SQLiteOpenHelper
import com.kota.Bahamut.Service.TempSettings.setNotReadMessageCount
import com.kota.Telnet.TelnetClient
import java.util.Date
import java.util.Locale

class MessageDatabase(context: Context?) :
    SQLiteOpenHelper(context, TelnetClient.getClient().username.lowercase(
        Locale.getDefault()
    ).trim { it <= ' ' } + "_database_msg", null, 1) {
    override fun onCreate(aDatabase: SQLiteDatabase) {
        try {
            val createTableQuery = "CREATE TABLE IF NOT EXISTS messages (" +
                    "message_id INTEGER PRIMARY KEY AUTOINCREMENT, " +
                    "sender_name TEXT NOT NULL, " +
                    "message TEXT NOT NULL, " +
                    "received_date INTEGER, " +
                    "read_date INTEGER," +
                    "type INTEGER" +
                    ")"
            aDatabase.execSQL(createTableQuery)
        } catch (e: SQLException) {
            e.printStackTrace()
        }
    }

    override fun onUpgrade(aDatabase: SQLiteDatabase, oldVersion: Int, newVersion: Int) {}
    override fun onOpen(db: SQLiteDatabase) {
        onCreate(db)
    }

    /** 收到訊息  */
    fun receiveMessage(aSenderName: String?, aMessage: String?) {
        val values = ContentValues()
        values.put("sender_name", aSenderName)
        values.put("message", aMessage)
        values.put("received_date", Date().time)
        values.put("type", 0)
        try {
            val db = writableDatabase
            db.insert("messages", null, values)
            db.close()
        } catch (ignored: Exception) {}
    }

    /** 更新讀取日期  */
    private fun updateReceiveMessage(aSenderName: String) {
        try {
            val db = writableDatabase
            db.execSQL(
                "UPDATE messages SET read_date = ? WHERE sender_name = ? AND read_date is null",
                arrayOf<Any>(
                    Date().time, aSenderName
                )
            )
            db.close()
        } catch (e: Exception) {
            e.printStackTrace()
        }
    }

    /** 送出訊息  */
    fun sendMessage(aSenderName: String?, aMessage: String?) {
        val values = ContentValues()
        values.put("sender_name", aSenderName)
        values.put("message", aMessage)
        values.put("received_date", Date().time)
        values.put("read_date", Date().time)
        values.put("type", 1)
        try {
            val db = writableDatabase
            db.insert("messages", null, values)
            db.close()
        } catch (ignored: Exception) {
        }
    }

    /** 列出各ID最新的訊息  */
    @SuppressLint("Range")
    fun getAllAndNewestMessage(): List<BahaMessageList> {
            val returnList: MutableList<BahaMessageList> = ArrayList()
            try {
                val db = readableDatabase
                val subQuery =
                    "(SELECT message FROM messages m2 WHERE m2.sender_name = m1.sender_name ORDER BY received_date DESC LIMIT 1)"
                val columns = arrayOf(
                    "sender_name",
                    "MAX(received_date) AS latest_received_date",
                    "$subQuery AS latest_message",
                    "COUNT(CASE WHEN read_date IS NULL THEN 1 END) AS unread_count"
                )
                val selection = ""
                val groupBy = "sender_name"
                val orderBy = "MAX(received_date) DESC"
                val cursor =
                    db.query("messages AS m1", columns, selection, null, groupBy, null, orderBy)
                if (cursor.moveToFirst()) {
                    var totalUnreadCount = 0
                    do {
                        val data = BahaMessageList()
                        data.senderName = cursor.getString(cursor.getColumnIndex("sender_name"))
                        data.message = cursor.getString(cursor.getColumnIndex("latest_message"))
                        data.receivedDate =
                            cursor.getInt(cursor.getColumnIndex("latest_received_date"))
                        val unreadCount = cursor.getInt(cursor.getColumnIndex("unread_count"))
                        data.unReadCount = unreadCount
                        totalUnreadCount += unreadCount
                        returnList.add(data)
                    } while (cursor.moveToNext())
                    setNotReadMessageCount(totalUnreadCount)
                    cursor.close()
                    db.close()
                } else {
                    cursor.close()
                    db.close()
                }
            } catch (e: Exception) {
                e.printStackTrace()
            }
            return returnList
        }

    /** 列出指定ID最新的訊息  */
    @SuppressLint("Range")
    fun getIdMessage(aSenderName: String): List<BahaMessage> {
        return try {
            val db = readableDatabase
            val columns = arrayOf("sender_name", "message", "received_date", "read_date", "type")
            val selection = "sender_name=?"
            val selectionArgs = arrayOf(aSenderName)
            val orderBy = "received_date ASC"
            val cursor =
                db.query("messages", columns, selection, selectionArgs, null, null, orderBy)
            if (cursor.moveToFirst()) {
                val returnList: MutableList<BahaMessage> = ArrayList()
                do {
                    val data = BahaMessage()
                    data.senderName = cursor.getString(cursor.getColumnIndex("sender_name"))
                    data.message = cursor.getString(cursor.getColumnIndex("message"))
                    data.receivedDate = cursor.getInt(cursor.getColumnIndex("received_date"))
                    data.readDate = cursor.getInt(cursor.getColumnIndex("read_date"))
                    data.type = cursor.getInt(cursor.getColumnIndex("type"))
                    returnList.add(data)
                } while (cursor.moveToNext())
                cursor.close()
                db.close()
                updateReceiveMessage(aSenderName)
                returnList
            } else {
                cursor.close()
                db.close()
                ArrayList()
            }
        } catch (ignored: Exception) {
            ArrayList()
        }
    }

    /** 清除所有紀錄  */
    fun clearDb() {
        try {
            setNotReadMessageCount(0)
            val db = writableDatabase
            db.execSQL("DELETE FROM messages ")
            onCreate(db)
        } catch (ignored: Exception) {
        }
    }
}
