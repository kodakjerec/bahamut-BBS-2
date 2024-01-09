package com.kota.Telnet;

public class NumberCharSequence implements CharSequence {
    private final char[] _data = new char[5];
    private String _string = null;

    public NumberCharSequence() {
        clear();
        this._string = new String(this._data);
    }

    public void clear() {
        for (int i = 0; i < 5; i++) {
            this._data[i] = '0';
        }
    }

    public void setInt(int i) {
        int number = i;
        this._data[4] = (char) ((number % 10) + 48);
        int number2 = number / 10;
        this._data[3] = (char) ((number2 % 10) + 48);
        int number3 = number2 / 10;
        this._data[2] = (char) ((number3 % 10) + 48);
        int number4 = number3 / 10;
        this._data[1] = (char) ((number4 % 10) + 48);
        int number5 = number4 / 10;
        this._data[0] = (char) ((number5 % 10) + 48);
        int number6 = number5 / 10;
    }

    public char charAt(int index) {
        return this._data[index];
    }

    public int length() {
        return 5;
    }

    public CharSequence subSequence(int start, int end) {
        return null;
    }

    public String toString() {
        return this._string;
    }
}
