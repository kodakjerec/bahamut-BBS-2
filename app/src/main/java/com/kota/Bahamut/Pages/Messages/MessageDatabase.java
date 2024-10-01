package com.kota.Bahamut.Pages.Messages;

import android.annotation.SuppressLint;
import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.SQLException;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;

import com.kota.Bahamut.Service.TempSettings;
import com.kota.Telnet.TelnetClient;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;

public class MessageDatabase extends SQLiteOpenHelper {
    public MessageDatabase(Context context) {
        super(context, TelnetClient.getClient().getUsername().toLowerCase().trim() + "_database_msg", null, 1);
    }

    public void onCreate(SQLiteDatabase aDatabase) {
        try {
            String CREATE_TABLE_QUERY = "CREATE TABLE IF NOT EXISTS messages (" +
                    "message_id INTEGER PRIMARY KEY AUTOINCREMENT, " +
                    "sender_name TEXT NOT NULL, " +
                    "message TEXT NOT NULL, " +
                    "received_date INTEGER, " +
                    "read_date INTEGER," +
                    "type INTEGER" +
                    ")";

            aDatabase.execSQL(CREATE_TABLE_QUERY);
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    public void onUpgrade(SQLiteDatabase aDatabase, int oldVersion, int newVersion) {
    }

    @Override
    public void onOpen(SQLiteDatabase db) {
        onCreate(db);
    }

    /** 收到訊息 */
    public void receiveMessage(String aSenderName, String aMessage) {
        ContentValues values = new ContentValues();
        values.put("sender_name", aSenderName);
        values.put("message", aMessage);
        values.put("received_date", new Date().getTime());
        values.put("type", 0);
        try {
            SQLiteDatabase db = getWritableDatabase();
            db.insert("messages", null, values);
            db.close();
        } catch (Exception ignored){}
    }
    /** 更新讀取日期 */
    private void updateReceiveMessage(String aSenderName) {
        try {
            SQLiteDatabase db = getWritableDatabase();
            db.execSQL("UPDATE messages SET read_date = ? WHERE sender_name = ? AND read_date is null",
                    new Object[]{new Date().getTime(), aSenderName});
            db.close();
        } catch (Exception ignored) {}
    }

    /** 送出訊息 */
    public void sendMessage(String aSenderName, String aMessage) {
        ContentValues values = new ContentValues();
        values.put("sender_name", aSenderName);
        values.put("message", aMessage);
        values.put("received_date", new Date().getTime());
        values.put("read_date", new Date().getTime());
        values.put("type", 1);
        try {
            SQLiteDatabase db = getWritableDatabase();
            db.insert("messages", null, values);
            db.close();
        } catch (Exception ignored){}
    }

    /** 列出各ID最新的訊息 */
    @SuppressLint("Range")
    public List<BahaMessageList> getAllAndNewestMessage() {
        List<BahaMessageList> returnList = new ArrayList<>();

        try {
            SQLiteDatabase db = getReadableDatabase();
            String subQuery = "(SELECT message FROM messages m2 WHERE m2.sender_name = m1.sender_name ORDER BY received_date DESC LIMIT 1)";
            String[] columns = {
                    "sender_name",
                    "MAX(received_date) AS latest_received_date",
                    subQuery + " AS latest_message",
                    "COUNT(CASE WHEN read_date IS NULL THEN 1 END) AS unread_count"
            };
            String selection = "";
            String groupBy = "sender_name";
            String orderBy = "MAX(received_date) DESC";
            Cursor cursor = db.query("messages AS m1", columns, selection, null, groupBy, null, orderBy);

            if (cursor.moveToFirst()) {
                int totalUnreadCount = 0;
                do {
                    BahaMessageList data = new BahaMessageList();
                    data.setSenderName(cursor.getString(cursor.getColumnIndex("sender_name")));
                    data.setMessage(cursor.getString(cursor.getColumnIndex("latest_message")));
                    data.setReceivedDate(cursor.getInt(cursor.getColumnIndex("latest_received_date")));
                    int unreadCount = cursor.getInt(cursor.getColumnIndex("unread_count"));
                    data.setUnReadCount(unreadCount);
                    totalUnreadCount+=unreadCount;
                    returnList.add(data);
                } while (cursor.moveToNext());
                TempSettings.setNotReadMessageCount(totalUnreadCount);

                cursor.close();
                db.close();
            } else {
                cursor.close();
                db.close();
            }
        } catch (Exception ignored){ }

        return returnList;
    }

    /** 列出指定ID最新的訊息 */
    @SuppressLint("Range")
    public List<BahaMessage> getIdMessage(String aSenderName) {
        try {
            SQLiteDatabase db = getReadableDatabase();
            String[] columns = {"sender_name", "message", "received_date", "read_date", "type"};
            String selection = "sender_name=?";
            String[] selectionArgs = {aSenderName};
            String orderBy = "received_date ASC";
            Cursor cursor = db.query("messages", columns, selection, selectionArgs, null, null, orderBy);

            if (cursor.moveToFirst()) {
                List<BahaMessage> returnList = new ArrayList<>();
                do {
                    BahaMessage data = new BahaMessage();
                    data.setSenderName(cursor.getString(cursor.getColumnIndex("sender_name")));
                    data.setMessage(cursor.getString(cursor.getColumnIndex("message")));
                    data.setReceivedDate(cursor.getInt(cursor.getColumnIndex("received_date")));
                    data.setReadDate(cursor.getInt(cursor.getColumnIndex("read_date")));
                    data.setType(cursor.getInt(cursor.getColumnIndex("type")));
                    returnList.add(data);
                } while (cursor.moveToNext());

                cursor.close();
                db.close();

                updateReceiveMessage(aSenderName);

                return returnList;
            } else {
                cursor.close();
                db.close();

                return new ArrayList<>();
            }
        } catch (Exception ignored){ return new ArrayList<>(); }
    }

    /** 清除所有紀錄 */
    public void clearDb() {
        try {
            TempSettings.setNotReadMessageCount(0);
            SQLiteDatabase db = getWritableDatabase();
            db.execSQL("DELETE FROM messages ");
            onCreate(db);
        }  catch (Exception ignored){}
    }
}
