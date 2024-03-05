package com.kota.Bahamut.DataModels;

import android.annotation.SuppressLint;
import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.SQLException;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;
import com.kota.Telnet.TelnetClient;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class AppDatabase extends SQLiteOpenHelper {
    public AppDatabase(Context context) {
        super(context, TelnetClient.getClient().getUsername().toLowerCase().trim() + "_database", null, 1);
    }

    public void onCreate(SQLiteDatabase aDatabase) {
        try {
            String CREATE_TABLE_QUERY = "CREATE TABLE IF NOT EXISTS messages (" +
                    "message_id INTEGER PRIMARY KEY AUTOINCREMENT, " +
                    "sender_name TEXT NOT NULL, " +
                    "message TEXT NOT NULL, " +
                    "received_date INTEGER, " +
                    "read_date INTEGER" +
                    ")";

            aDatabase.execSQL(CREATE_TABLE_QUERY);
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    public void onUpgrade(SQLiteDatabase aDatabase, int oldVersion, int newVersion) {
    }

    public void saveMessage(String aSenderName, String aMessage) {
        ContentValues values = new ContentValues();
        values.put("sender_name", aSenderName);
        values.put("message", aMessage);
        values.put("received_date", new Date().getTime());
        SQLiteDatabase db = getWritableDatabase();
        db.insert("messages", null, values);
        db.close();
    }

    public void loadMessages() {
        SQLiteDatabase db = getReadableDatabase();
        @SuppressLint("Recycle") Cursor cursor = db.query("messages", null, null, null, null, null, "received_date DESC", "0,100");
        List<Map<String, Object>> buffer = new ArrayList<>();
        if (cursor.moveToFirst()) {
            do {
                @SuppressLint("Range") long message_id = cursor.getLong(cursor.getColumnIndex("message_id"));
                @SuppressLint("Range") String sender_name = cursor.getString(cursor.getColumnIndex("sender_name"));
                @SuppressLint("Range") String message = cursor.getString(cursor.getColumnIndex("message"));
                @SuppressLint("Range") long received_date_value = cursor.getLong(cursor.getColumnIndex("received_date"));
                @SuppressLint("Range") long read_date_value = cursor.isNull(cursor.getColumnIndex("read_date")) ? 0 : cursor.getLong(cursor.getColumnIndex("read_date"));
                HashMap<String, Object> hashMap = new HashMap<>();
                hashMap.put("message_id", message_id);
                hashMap.put("sender_name", sender_name);
                hashMap.put("message", message);
                hashMap.put("received_date", new Date(received_date_value));
                if (read_date_value > 0) {
                    hashMap.put("read_date", new Date(read_date_value));
                }
                buffer.add(hashMap);
            } while (cursor.moveToNext());
        }
        db.close();
    }
}
