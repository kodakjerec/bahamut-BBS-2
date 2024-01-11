package com.kota.Telnet.Model;

import com.kota.Telnet.Reference.TelnetAnsiCode;
import com.kota.Telnet.TelnetCommand;
import java.util.Iterator;
import java.util.Vector;

public class TelnetFrame {
    public static final int DEFAULT_COLUMN = 80;
    public static final int DEFAULT_ROW = 24;
    private static int _count = 0;
    public Vector<TelnetRow> rows = new Vector<>();

    /* access modifiers changed from: protected */
    public void finalize() throws Throwable {
        super.finalize();
    }

    public TelnetFrame() {
        initialData(24);
        clear();
    }

    public TelnetFrame(int row) {
        initialData(row);
        clear();
    }

    public TelnetFrame(TelnetFrame aFrame) {
        set(aFrame);
    }

    public void set(TelnetFrame aFrame) {
        if (this.rows.size() != aFrame.getRowSize()) {
            initialData(aFrame.getRowSize());
        }
        for (int i = 0; i < this.rows.size(); i++) {
            this.rows.get(i).set(aFrame.getRow(i));
        }
    }

    public void initialData(int row) {
        this.rows.clear();
        for (int i = 0; i < row; i++) {
            this.rows.add(new TelnetRow());
        }
        clear();
    }

    public void clear() {
        for (int row = 0; row < this.rows.size(); row++) {
            getRow(row).clear();
        }
    }

    public void setPositionBitSpace(int row, int column, byte aBitSpace) {
        getRow(row).bitSpace[column] = aBitSpace;
    }

    public byte getPositionBitSpace(int row, int column) {
        return getRow(row).bitSpace[column];
    }

    public boolean getPositionBlink(int row, int column) {
        return getRow(row).blink[column];
    }

    public int getPositionData(int row, int column) {
        return getRow(row).data[column] & TelnetCommand.IAC;
    }

    public int getPositionTextColor(int row, int column) {
        return TelnetAnsiCode.getTextColor(getRow(row).textColor[column]);
    }

    public int getPositionBackgroundColor(int row, int column) {
        return TelnetAnsiCode.getBackgroundColor(getRow(row).backgroundColor[column]);
    }

    public void cleanPositionData(int row, int column) {
        getRow(row).cleanColumn(column);
    }

    public void setRow(int index, TelnetRow aRow) {
        if (index >= 0 && index < this.rows.size()) {
            this.rows.set(index, aRow);
        }
    }

    public TelnetRow getRow(int index) {
        return this.rows.get(index);
    }

    public TelnetRow getFirstRow() {
        return this.rows.firstElement();
    }

    public TelnetRow getLastestRow() {
        return this.rows.lastElement();
    }

    public void switchRow(int index, int andIndex) {
        this.rows.set(index, this.rows.get(andIndex));
        this.rows.set(andIndex, this.rows.get(index));
    }

    public int getRowSize() {
        return this.rows.size();
    }

    public TelnetFrame clone() {
        return new TelnetFrame(this);
    }

    public boolean isEmpty() {
        for (int i = 0; i < this.rows.size(); i++) {
            if (!this.rows.get(i).isEmpty()) {
                return false;
            }
        }
        return true;
    }

    public TelnetRow removeRow(int index) {
        return this.rows.remove(index);
    }

    public void reloadSpace() {
        boolean text_color_diff;
        boolean background_color_diff;
        for (int row = 0; row < getRowSize(); row++) {
            int column = 0;
            while (column < 80) {
                if (getPositionData(row, column) <= 127 || column >= 79) {
                    setPositionBitSpace(row, column, (byte) 0);
                } else {
                    if (getPositionTextColor(row, column) != getPositionTextColor(row, column + 1)) {
                        text_color_diff = true;
                    } else {
                        text_color_diff = false;
                    }
                    if (getPositionBackgroundColor(row, column) != getPositionBackgroundColor(row, column + 1)) {
                        background_color_diff = true;
                    } else {
                        background_color_diff = false;
                    }
                    if (text_color_diff || background_color_diff) {
                        setPositionBitSpace(row, column, (byte) 3);
                        setPositionBitSpace(row, column + 1, (byte) 4);
                    } else {
                        setPositionBitSpace(row, column, (byte) 1);
                        setPositionBitSpace(row, column + 1, (byte) 2);
                    }
                    column++;
                }
                column++;
            }
        }
    }

    public void printBackgroundColor() {
        for (int i = 0; i < this.rows.size(); i++) {
            StringBuffer s = new StringBuffer();
            TelnetRow row = this.rows.get(i);
            for (int j = 0; j < 80; j++) {
                s.append(String.format("%1$02d ", new Object[]{Byte.valueOf(row.backgroundColor[j])}));
            }
            System.out.println(s.toString());
        }
    }

    public void cleanCachedData() {
        Iterator<TelnetRow> it = this.rows.iterator();
        while (it.hasNext()) {
            it.next().cleanCachedData();
        }
    }
}
