package com.kota.Telnet.Model;

import com.kota.TextEncoder.B2UEncoder;

public class TelnetRow {
    private static final int _count = 0;
    private TelnetRow _append_row = null;
    private String _cached_string = null;
    private int _empty_space = 0;
    private int _quote_level = -1;
    private int _quote_space = 0;
    public byte[] backgroundColor = new byte[80];
    public byte[] bitSpace = new byte[80];
    public boolean[] blink = new boolean[80];
    public byte[] data = new byte[80];
    public boolean[] italic = new boolean[80];
    public byte[] textColor = new byte[80];

    /* access modifiers changed from: protected */
    protected void finalize() throws Throwable {
        super.finalize();
    }

    public TelnetRow() {
        clear();
    }

    public TelnetRow(TelnetRow aRow) {
        clear();
        set(aRow);
    }

    public void clear() {
        for (int i = 0; i < 80; i++) {
            cleanColumn(i);
        }
        cleanCachedData();
    }

    public void cleanColumn(int column) {
        this.data[column] = 0;
        this.textColor[column] = 0;
        this.backgroundColor[column] = 0;
        this.bitSpace[column] = 0;
        this.blink[column] = false;
        this.italic[column] = false;
    }

    public void cleanCachedData() {
        this._cached_string = null;
        this._quote_level = -1;
    }

    public TelnetRow set(TelnetRow aRow) {
        for (int i = 0; i < 80; i++) {
            this.data[i] = aRow.data[i];
            this.textColor[i] = aRow.textColor[i];
            this.backgroundColor[i] = aRow.backgroundColor[i];
            this.bitSpace[i] = aRow.bitSpace[i];
            this.blink[i] = aRow.blink[i];
            this.italic[i] = aRow.italic[i];
        }
        cleanCachedData();
        return this;
    }

    public int getQuoteLevel() {
        if (this._quote_level == -1) {
            reloadQuoteSpace();
        }
        return this._quote_level;
    }

    public int getQuoteSpace() {
        if (this._quote_level == -1) {
            reloadQuoteSpace();
        }
        return this._quote_space;
    }

    public int getEmptySpace() {
        if (this._quote_level == -1) {
            reloadQuoteSpace();
        }
        return this._empty_space;
    }

    public int getDataSpace() {
        return this.data.length - getEmptySpace();
    }

    private void reloadQuoteSpace() {
        this._quote_level = 0;
        this._quote_space = 0;
        int space_count = 0;
        this._quote_space = 0;
        while (this._quote_space < this.data.length) {
            if (this.data[this._quote_space] != 62) {
                if (this.data[this._quote_space] != 32 || (space_count = space_count + 1) > 1) {
                    break;
                }
            } else {
                this._quote_level++;
                space_count = 0;
            }
            this._quote_space++;
        }
        this._empty_space = 0;
        int i = this.data.length - 1;
        while (i >= 0 && this.data[i] == 0) {
            this._empty_space++;
            i--;
        }
    }

    public String toString() {
        return getRawString().substring(getQuoteSpace()).trim();
    }

    public String toContentString() {
        return getRawString().substring(getQuoteSpace());
    }

    public String getRawString() {
        if (this._cached_string == null) {
            this._cached_string = B2UEncoder.getInstance().encodeToString(this.data);
            if (this._append_row != null) {
                this._cached_string += this._append_row.getRawString();
            }
        }
        return this._cached_string;
    }

    public String getSpaceString(int from, int to) {
        int from_position = 0;
        int position = 0;
        int i = 0;
        while (i <= to) {
            if (i == from) {
                from_position = position;
            }
            position++;
            if ((this.data[i] & 255) > 127 && i < to) {
                i++;
            }
            i++;
        }
        int to_position = position;
        String cached_string = getRawString();
        if (to_position > cached_string.length()) {
            to_position = cached_string.length();
        }
        if (to_position < from_position) {
            return "";
        }
        return cached_string.substring(from_position, to_position);
    }

    public boolean isEmpty() {
        for (int i = 0; i < 80; i++) {
            if (this.data[i] != 0) {
                return false;
            }
        }
        return true;
    }

    public void append(TelnetRow row) {
        this._append_row = row;
        cleanCachedData();
        System.out.println("self become:" + getRawString());
    }

    public TelnetRow clone() {
        return new TelnetRow(this);
    }

    public boolean isHttpUrl() {
        return false;
    }
}
