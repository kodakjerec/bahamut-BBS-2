package com.kota.Bahamut.DataModels;
/*
  基本的書籤元件
 */

import static com.kota.Bahamut.Service.CommonFunctions.getContextString;

import com.kota.Bahamut.R;

import org.json.JSONException;
import org.json.JSONObject;

import java.io.IOException;
import java.io.ObjectInputStream;

public class Bookmark {
    public static final String OPTIONAL_BOOKMARK = "0"; // 書籤
    public static final String OPTIONAL_STORY = "1"; // 紀錄
    public static final int version = 1;
    String _author = "";
    String _board = "";
    String _detail = "";
    byte[] _ext_data = new byte[0];
    String _gy = "";
    String _keyword = "";
    String _mark = "n";
    String _title = "";
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
        obj.put("index", index);
        obj.put("optional", optional);
        obj.put("board", _board);
        obj.put("title", _title);
        obj.put("weight", weight);
        obj.put("detail", _detail);
        obj.put("keyword", _keyword);
        obj.put("author", _author);
        obj.put("mark", _mark);
        obj.put("gy", _gy);
        return obj;
    }

    public void importFromJSON(JSONObject obj) throws JSONException {
        index = obj.isNull("index") ? 0 : obj.getInt("index");
        if (index == 0) {
            System.out.print("index:0");
        }
        optional = obj.getString("optional");
        _board = obj.getString("board");
        _title = obj.getString("title");
        weight = obj.getInt("weight");
        _detail = obj.getString("detail");
        _keyword = obj.getString("keyword");
        _author = obj.getString("author");
        _mark = obj.getString("mark");
        _gy = obj.getString("gy");
    }

    public void importFromStream(ObjectInputStream aStream) throws IOException {
        aStream.readInt();
        index = 0;
        optional = aStream.readUTF();
        _board = aStream.readUTF();
        _title = aStream.readUTF();
        weight = aStream.readInt();
        _detail = aStream.readUTF();
        _keyword = aStream.readUTF();
        _author = aStream.readUTF();
        _mark = aStream.readUTF();
        _gy = aStream.readUTF();
        _ext_data = new byte[aStream.readInt()];
        aStream.read(_ext_data);
    }

    public String getTitle() {
        return _title;
    }

    public void setTitle(String title) {
        if (title == null) {
            _title = "";
        } else {
            _title = title;
        }
    }

    public String getKeyword() {
        return _keyword;
    }

    public void setKeyword(String keyword) {
        if (keyword == null) {
            _keyword = "";
        } else {
            _keyword = keyword;
        }
    }

    public String getAuthor() {
        return _author;
    }

    public void setAuthor(String author) {
        if (author == null) {
            _author = "";
        } else {
            _author = author;
        }
    }

    public String getMark() {
        return _mark;
    }

    public void setMark(String mark) {
        if (mark == null || !mark.equals("y")) {
            _mark = "n";
        } else {
            _mark = mark;
        }
    }

    public String getGy() {
        return _gy;
    }

    public void setGy(String gy) {
        if (gy == null) {
            _gy = "";
        } else {
            _gy = gy;
        }
    }

    public void setBoard(String board) {
        if (board == null) {
            _board = "";
        } else {
            _board = board;
        }
    }

    public String getBoard() {
        return _board;
    }

    public String generateTitle() {
        String title = "";
        if (_keyword.trim().length() > 0) {
            title = getContextString(R.string.title_) + _keyword;
        }
        if (title.length() == 0 && _author.trim().length() > 0) {
            title = getContextString(R.string.author_) + _author;
        }
        if (title.length() == 0 && _gy.trim().length() > 0) {
            title = getContextString(R.string.do_gy_) + _gy;
        }
        if (_mark.equals("y")) {
            title = "M " + title;
        }
        if (title.length() == 0) {
            return getContextString(R.string.no_assign);
        }
        return title;
    }
}
