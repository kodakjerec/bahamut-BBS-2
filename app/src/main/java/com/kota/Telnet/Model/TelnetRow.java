package com.kota.Telnet.Model;

import androidx.annotation.NonNull;

import com.kota.TextEncoder.B2UEncoder;

import java.util.Arrays;

public class TelnetRow {
    private TelnetRow _append_row;
    private String _cached_string;
    private int _empty_space;
    private int _quote_level;
    private int _quote_space;
    public byte[] backgroundColor;
    public byte[] bitSpace;
    public boolean[] blink;
    public byte[] data;
    public boolean[] italic;
    public byte[] textColor;

    protected void finalize() throws Throwable {
        super.finalize();
    }

    public TelnetRow() {
        data = new byte[80];
        textColor = new byte[80];
        backgroundColor = new byte[80];
        blink = new boolean[80];
        italic = new boolean[80];
        bitSpace = new byte[80];
        _quote_level = -1;
        _quote_space = 0;
        _empty_space = 0;
        _cached_string = null;
        _append_row = null;
        clear();
    }

    public TelnetRow(TelnetRow aRow) {
        data = new byte[80];
        textColor = new byte[80];
        backgroundColor = new byte[80];
        blink = new boolean[80];
        italic = new boolean[80];
        bitSpace = new byte[80];
        _quote_level = -1;
        _quote_space = 0;
        _empty_space = 0;
        _cached_string = null;
        _append_row = null;
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
        data[column] = 0;
        textColor[column] = 0;
        backgroundColor[column] = 0;
        bitSpace[column] = 0;
        blink[column] = false;
        italic[column] = false;
    }

    public void cleanCachedData() {
        _cached_string = null;
        _quote_level = -1;
    }

    public TelnetRow set(TelnetRow aRow) {
        for (int i = 0; i < 80; i++) {
            data[i] = aRow.data[i];
            textColor[i] = aRow.textColor[i];
            backgroundColor[i] = aRow.backgroundColor[i];
            bitSpace[i] = aRow.bitSpace[i];
            blink[i] = aRow.blink[i];
            italic[i] = aRow.italic[i];
        }
        cleanCachedData();
        return this;
    }

    public int getQuoteLevel() {
        if (_quote_level == -1) {
            reloadQuoteSpace();
        }
        return _quote_level;
    }

    public int getQuoteSpace() {
        if (_quote_level == -1) {
            reloadQuoteSpace();
        }
        return _quote_space;
    }

    public int getEmptySpace() {
        if (_quote_level == -1) {
            reloadQuoteSpace();
        }
        return _empty_space;
    }

    public int getDataSpace() {
        return data.length - getEmptySpace();
    }

    private void reloadQuoteSpace() {
        _quote_level = 0;
        _quote_space = 0;
        int space_count = 0;
        while (_quote_space < data.length) {
            if (data[_quote_space] == 62) {
                _quote_level++;
                space_count = 0;
            } else if (data[_quote_space] != 32 || (space_count = space_count + 1) > 1) {
                break;
            }
            _quote_space++;
        }
        _empty_space = 0;
        for (int i = data.length - 1; i >= 0 && data[i] == 0; i--) {
            _empty_space++;
        }
    }

    @NonNull
    public String toString() {
        return getRawString().substring(getQuoteSpace()).trim();
    }

    public String toContentString() {
        return getRawString().substring(getQuoteSpace());
    }

    public String getRawString() {
        if (_cached_string == null) {
            _cached_string = B2UEncoder.getInstance().encodeToString(data);
            if (_append_row != null) {
                _cached_string += _append_row.getRawString();
            }
        }
        return _cached_string;
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
            int d = data[i] & 255;
            if (d > 127 && i < to) {
                i++;
            }
            i++;
        }
        int to_position = position;
        String cached_string = getRawString();
        if (to_position > cached_string.length()) {
            to_position = cached_string.length();
        }
        return to_position < from_position ? "" : cached_string.substring(from_position, to_position);
    }

    public boolean isEmpty() {
        for (int i = 0; i < 80; i++) {
            if (data[i] != 0) {
                return false;
            }
        }
        return true;
    }

    public void append(TelnetRow row) {
        _append_row = row;
        cleanCachedData();
        System.out.println("self become:" + getRawString());
    }

    @NonNull
    public TelnetRow clone() {
        return new TelnetRow(this);
    }

    /** 雙字元判斷 */
    public void reloadSpace() {
        int column = 0;
        while (column < 80) {
            int data = this.data[column] & 255;
            if (data > 127 && column < 79) {
                boolean text_color_diff = this.textColor[column] != this.textColor[column+1];
                boolean background_color_diff = this.backgroundColor[column] != this.backgroundColor[column+1];
                if (text_color_diff || background_color_diff) {
                    this.bitSpace[column] = BitSpaceType.DOUBLE_BIT_SPACE_2_1;
                    this.bitSpace[column+1] = BitSpaceType.DOUBLE_BIT_SPACE_2_2;
                } else {
                    this.bitSpace[column] = BitSpaceType.SINGLE_BIT_SPACE_2_1;
                    this.bitSpace[column+1] = BitSpaceType.SINGLE_BIT_SPACE_2_2;
                }
                column++;
            } else {
                this.bitSpace[column] = BitSpaceType.BIT_SPACE_1;
            }
            column++;
        }
    }

    /** 回傳對應雙字元的前景色, 單雙字元已經轉換完成 */
    public byte[] getTextColor() {
        byte[] colors = new byte[80];
        int nowIndex = -1;
        for(byte bitIndex = 0; bitIndex < bitSpace.length; bitIndex++) {
            nowIndex++;
            if (bitSpace[bitIndex]>0) {
                // 雙字元
                colors[nowIndex] = textColor[bitIndex];
                bitIndex++;
            } else {
                // 單字元
                colors[nowIndex] = textColor[bitIndex];
            }
        }
        nowIndex+=1; // index和copyOfRange定位差異
        return Arrays.copyOfRange(colors, 0, nowIndex);
    }
    /** 回傳對應雙字元的背景色, 單雙字元已經轉換完成 */
    public byte[] getBackgroundColor() {
        byte[] colors= new byte[80];
        int nowIndex = -1;
        for(byte bitIndex = 0;bitIndex < bitSpace.length; bitIndex++) {
            nowIndex++;
            if (bitSpace[bitIndex]>0) {
                // 雙字元
                colors[nowIndex] = backgroundColor[bitIndex];
                bitIndex++;
            } else {
                // 單字元
                colors[nowIndex] = backgroundColor[bitIndex];
            }
        }
        nowIndex+=1; // index和copyOfRange定位差異
        return Arrays.copyOfRange(colors, 0, nowIndex);
    }
}
