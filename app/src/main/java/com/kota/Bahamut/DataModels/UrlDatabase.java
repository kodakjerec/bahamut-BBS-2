package com.kota.Bahamut.DataModels;

import android.annotation.SuppressLint;
import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.SQLException;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;

import com.kota.Telnet.TelnetClient;
import java.util.Vector;

public class UrlDatabase extends SQLiteOpenHelper {
    public UrlDatabase(Context context) {
        super(context, TelnetClient.getClient().getUsername().toLowerCase().trim() + "_database", null, 1);
    }

    @Override
    public void onCreate(SQLiteDatabase aDatabase) {
        try {
            String CREATE_TABLE_QUERY = "CREATE TABLE IF NOT EXISTS urls (" +
                    "url TEXT PRIMARY KEY, " +
                    "title TEXT, " +
                    "description TEXT, " +
                    "imageUrl TEXT, " +
                    "isPic TEXT" +
                    ")";

            aDatabase.execSQL(CREATE_TABLE_QUERY);
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    @Override
    public void onUpgrade(SQLiteDatabase aDatabase, int i, int i1) {
        aDatabase.execSQL("DROP TABLE IF EXISTS urls");
        onCreate(aDatabase);
    }

    public void addUrl(String url, String title, String description, String imageUrl, boolean isPic) {
        ContentValues values = new ContentValues();
        values.put("url", url);
        values.put("title", title);
        values.put("description", description);
        values.put("imageUrl", imageUrl);
        values.put("isPic", isPic);
        SQLiteDatabase db = getWritableDatabase();
        db.insert("urls", null, values);
        db.close();
    }

    @SuppressLint("Range")
    public Vector<String> getUrl(String url) {
        SQLiteDatabase db = getReadableDatabase();
        String[] columns = {"url", "title", "description", "imageUrl", "isPic"};
        String selection = "url = ?";
        String[] selectionArgs = {url};

        Cursor cursor = db.query("urls", columns, selection, selectionArgs, null, null, null);

        if (cursor.moveToFirst()) {
            Vector<String> data = new Vector<>();
            data.add(cursor.getString(cursor.getColumnIndex("url")));
            data.add(cursor.getString(cursor.getColumnIndex("title")));
            data.add(cursor.getString(cursor.getColumnIndex("description")));
            data.add(cursor.getString(cursor.getColumnIndex("imageUrl")));
            data.add(cursor.getString(cursor.getColumnIndex("isPic")));

            cursor.close();
            db.close();

            return data;
        } else {
            cursor.close();
            db.close();

            return null;
        }
    }

    public void clearDb() {
        SQLiteDatabase db = getWritableDatabase();
        db.execSQL("DELETE FROM urls");
        onCreate(db);
    }
}
