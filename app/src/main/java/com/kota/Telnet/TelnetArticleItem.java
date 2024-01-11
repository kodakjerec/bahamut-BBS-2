package com.kota.Telnet;

import com.kota.Telnet.Model.TelnetFrame;
import com.kota.Telnet.Model.TelnetModel;
import com.kota.Telnet.Model.TelnetRow;
import java.util.Iterator;
import java.util.Vector;

public class TelnetArticleItem {
    public static final int TYPE_HEADER = 2;
    public static final int TYPE_TELNET = 1;
    public static final int TYPE_TEXT = 0;
    public static final int TYPE_TIME = 3;
    private String _author = "";
    private String _content = "";
    private TelnetFrame _frame = null;
    private String _nickname = "";
    private int _quote_level = 0;
    private Vector<TelnetRow> _rows = new Vector<>();
    private int _type = 0;

    public String getAuthor() {
        return this._author;
    }

    public void setAuthor(String author) {
        this._author = author;
    }

    public String getNickname() {
        return this._nickname;
    }

    public void setNickname(String nickname) {
        this._nickname = nickname;
    }

    public String getContent() {
        return this._content;
    }

    public int getQuoteLevel() {
        return this._quote_level;
    }

    public void setQuoteLevel(int quoteLevel) {
        this._quote_level = quoteLevel;
    }

    public int getType() {
        return this._type;
    }

    public void setType(int type) {
        this._type = type;
    }

    public void addRow(TelnetRow row) {
        this._rows.add(row);
    }

    public TelnetRow getRow(int rowIndex) {
        return this._rows.get(rowIndex);
    }

    public TelnetRow removeRow(int rowIndex) {
        return this._rows.remove(rowIndex);
    }

    public void cleanRows() {
        this._rows.clear();
    }

    public int getRowSize() {
        return this._rows.size();
    }

    public void clear() {
        this._author = "";
        this._nickname = "";
        this._content = "";
        this._quote_level = 0;
    }

    public void build() {
        StringBuffer buffer = new StringBuffer();
        Iterator<TelnetRow> it = this._rows.iterator();
        while (it.hasNext()) {
            TelnetRow row = it.next();
            if (buffer.length() > 0) {
                buffer.append("\n");
            }
            buffer.append(row.toContentString());
        }
        this._content = buffer.toString();
    }

    public boolean isEmpty() {
        return this._content.length() == 0;
    }

    public TelnetModel getModel() {
        return new TelnetModel(this._rows.size());
    }

    public String toString() {
        StringBuffer buffer = new StringBuffer();
        buffer.append("QuoteLevel:" + this._quote_level + "\n");
        buffer.append("Author:" + this._author + "\n");
        buffer.append("Nickname:" + this._nickname + "\n");
        buffer.append("Content:" + this._content + "\n");
        return buffer.toString();
    }

    public void buildFrame() {
        this._frame = new TelnetFrame(this._rows.size());
        for (int i = 0; i < this._rows.size(); i++) {
            this._frame.setRow(i, this._rows.get(i));
        }
    }

    public TelnetFrame getFrame() {
        if (this._frame == null) {
            buildFrame();
        }
        return this._frame;
    }
}
