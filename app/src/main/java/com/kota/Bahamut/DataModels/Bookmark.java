package com.kota.Bahamut.DataModels;

import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.StreamCorruptedException;
import org.json.JSONException;
import org.json.JSONObject;

public class Bookmark {
    public static final String OPTIONAL_BOOKMARK = "0";
    public static final String OPTIONAL_HOSTORY = "1";
    public static final int version = 1;
    private String _author = "";
    private String _board = "";
    private String _detail = "";
    private byte[] _ext_data = new byte[0];
    private String _gy = "";
    private String _keyword = "";
    private String _mark = "n";
    private String _title = "";
    public int index = 0;
    public String optional = OPTIONAL_BOOKMARK;
    public int weight = 0;

    public Bookmark() {
    }

    public Bookmark(ObjectInputStream aInputStream) throws IOException {
        importFromStream(aInputStream);
    }

    public Bookmark(JSONObject obj) throws JSONException {
        importFromJSON(obj);
    }

    public JSONObject exportToJSON() throws Exception {
        JSONObject obj = new JSONObject();
        obj.put("version", 1);
        obj.put("index", this.index);
        obj.put("optional", this.optional);
        obj.put("board", this._board);
        obj.put("title", this._title);
        obj.put("weight", this.weight);
        obj.put("detail", this._detail);
        obj.put("keyword", this._keyword);
        obj.put("author", this._author);
        obj.put("mark", this._mark);
        obj.put("gy", this._gy);
        return obj;
    }

    public void importFromJSON(JSONObject obj) throws JSONException {
        this.index = obj.isNull("index") ? 0 : obj.getInt("index");
        if (this.index == 0) {
            System.out.print("index:0");
        }
        this.optional = obj.getString("optional");
        this._board = obj.getString("board");
        this._title = obj.getString("title");
        this.weight = obj.getInt("weight");
        this._detail = obj.getString("detail");
        this._keyword = obj.getString("keyword");
        this._author = obj.getString("author");
        this._mark = obj.getString("mark");
        this._gy = obj.getString("gy");
    }

    public void importFromStream(ObjectInputStream aStream) throws IOException {
        aStream.readInt();
        this.index = 0;
        this.optional = aStream.readUTF();
        this._board = aStream.readUTF();
        this._title = aStream.readUTF();
        this.weight = aStream.readInt();
        this._detail = aStream.readUTF();
        this._keyword = aStream.readUTF();
        this._author = aStream.readUTF();
        this._mark = aStream.readUTF();
        this._gy = aStream.readUTF();
        this._ext_data = new byte[aStream.readInt()];
        aStream.read(this._ext_data);
    }

    public String getTitle() {
        return this._title;
    }

    public void setTitle(String title) {
        if (title == null) {
            this._title = "";
        } else {
            this._title = title;
        }
    }

    public String getKeyword() {
        return this._keyword;
    }

    public void setKeyword(String keyword) {
        if (keyword == null) {
            this._keyword = "";
        } else {
            this._keyword = keyword;
        }
    }

    public String getAuthor() {
        return this._author;
    }

    public void setAuthor(String author) {
        if (author == null) {
            this._author = "";
        } else {
            this._author = author;
        }
    }

    public String getMark() {
        return this._mark;
    }

    public void setMark(String mark) {
        if (mark == null || !mark.equals("y")) {
            this._mark = "n";
        } else {
            this._mark = mark;
        }
    }

    public String getGy() {
        return this._gy;
    }

    public void setGy(String gy) {
        if (gy == null) {
            this._gy = "";
        } else {
            this._gy = gy;
        }
    }

    public void setBoard(String board) {
        if (board == null) {
            this._board = "";
        } else {
            this._board = board;
        }
    }

    public String getBoard() {
        return this._board;
    }

    public String generateTitle() {
        String title = "";
        if (this._keyword.trim().length() > 0) {
            title = "標題:" + this._keyword;
        }
        if (title.length() == 0 && this._author.trim().length() > 0) {
            title = "作者:" + this._author;
        }
        if (title.length() == 0 && this._gy.trim().length() > 0) {
            title = "推薦:" + this._gy;
        }
        if (this._mark.equals("y")) {
            title = "M " + title;
        }
        if (title.length() == 0) {
            return "未指定";
        }
        return title;
    }
}
