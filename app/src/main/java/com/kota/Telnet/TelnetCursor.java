package com.kota.Telnet;

import androidx.annotation.NonNull;

public class TelnetCursor {
    public int column = 0;
    public int row = 0;

    public TelnetCursor() {
    }

    public TelnetCursor(int aRow, int aColumn) {
        set(aRow, aColumn);
    }

    public void set(int aRow, int aColumn) {
        this.row = aRow;
        this.column = aColumn;
    }

    public void set(TelnetCursor aCursor) {
        this.row = aCursor.row;
        this.column = aCursor.column;
    }

    public boolean equals(int aRow, int aColumn) {
        return aRow == this.row && aColumn == this.column;
    }

    public boolean equals(TelnetCursor aCursor) {
        return aCursor.row == this.row && aCursor.column == this.column;
    }

    @NonNull
    public TelnetCursor clone() {
        return new TelnetCursor(this.row, this.column);
    }

    @NonNull
    public String toString() {
        return "( " + this.row + " , " + this.column + " )";
    }
}
