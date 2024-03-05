package com.kota.Bahamut.DataModels;

import android.content.Context;
import android.content.SharedPreferences;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.ObjectInputStream;
import java.util.Iterator;
import java.util.Vector;

public class ArticleTempStore {
    public static final int version = 1;
    private final Context _context;
    private String _file_path = null;
    public Vector<ArticleTemp> articles = new Vector<>();

    public static void upgrade(Context context, String aFilePath) {
        new ArticleTempStore(context, aFilePath).load();
    }

    public ArticleTempStore(Context context, String aFilePath) {
        this._context = context;
        this._file_path = aFilePath;
        for (int i=0;i<10;i++) {
            this.articles.add(new ArticleTemp());
        }
    }

    public ArticleTempStore(Context context) {
        this._context = context;
        for (int i=0;i<10;i++) {
            this.articles.add(new ArticleTemp());
        }
        load();
    }

    public void load() {
        System.out.println("load article store from file");
        boolean load_from_file = false;
        if (this._file_path != null && this._file_path.length() > 0) {
            File file = new File(this._file_path);
            if (file.exists()) {
                try {
                    InputStream file_input_stream = new FileInputStream(file);
                    ObjectInputStream input_stream = new ObjectInputStream(file_input_stream);
                    importFromStream(input_stream);
                    input_stream.close();
                    file_input_stream.close();
                    file.delete();
                    load_from_file = true;
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
        }
        if (load_from_file) {
            store();
        }
        if (!load_from_file && this._context != null) {
            try {
                String save_data = this._context.getSharedPreferences("article_temp", 0).getString("save_data", "");
                if (save_data.length() > 0) {
                    importFromJSON(new JSONObject(save_data));
                }
            } catch (Exception e2) {
                e2.printStackTrace();
            }
        }
    }

    public void store() {
        if (this._context != null) {
            try {
                SharedPreferences perf = this._context.getSharedPreferences("article_temp", 0);
                perf.edit().putString("save_data", exportToJSON().toString()).commit();
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
    }

    public void importFromJSON(JSONObject obj) throws JSONException {
        JSONArray data = obj.getJSONArray("data");
        this.articles.clear();
        for (int i = 0; i < data.length(); i++) {
            JSONObject item_data = data.getJSONObject(i);
            ArticleTemp temp = new ArticleTemp();
            temp.importFromJSON(item_data);
            this.articles.add(temp);
        }
    }

    public JSONObject exportToJSON() throws JSONException {
        JSONObject obj = new JSONObject();
        JSONArray save_data = new JSONArray();
        for (ArticleTemp article : this.articles) {
            save_data.put(article.exportToJSON());
        }
        obj.put("data", save_data);
        return obj;
    }

    public void importFromStream(ObjectInputStream aStream) throws IOException {
        aStream.readInt();
        int size = aStream.readInt();
        for (int i = 0; i < size; i++) {
            ArticleTemp article = new ArticleTemp();
            article.importFromStream(aStream);
            this.articles.add(article);
        }
    }
}
