package com.kota.Telnet.Model;

import com.kota.Telnet.Reference.TelnetAnsiCode;
import java.util.Iterator;
import java.util.Vector;

/* loaded from: classes.dex */
public class TelnetFrame {
    public static final int DEFAULT_COLUMN = 80;
    public static final int DEFAULT_ROW = 24;
    private static int _count = 0;
    public Vector<TelnetRow> rows = new Vector<>();

    protected void finalize() throws Throwable {
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
        int data = getRow(row).data[column];
        return data & 255;
    }

    public int getPositionTextColor(int row, int column) {
        byte color_index = getRow(row).textColor[column];
        return TelnetAnsiCode.getTextColor(color_index);
    }

    public int getPositionBackgroundColor(int row, int column) {
        byte color_index = getRow(row).backgroundColor[column];
        return TelnetAnsiCode.getBackgroundColor(color_index);
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
        TelnetRow row = this.rows.get(index);
        this.rows.set(index, this.rows.get(andIndex));
        this.rows.set(andIndex, row);
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
        TelnetRow row = this.rows.remove(index);
        return row;
    }

    public void reloadSpace() {
        for (int row = 0; row < getRowSize(); row++) {
            int column = 0;
            while (column < 80) {
                int data = getPositionData(row, column);
                if (data > 127 && column < 79) {
                    boolean text_color_diff = getPositionTextColor(row, column) != getPositionTextColor(row, column + 1);
                    boolean background_color_diff = getPositionBackgroundColor(row, column) != getPositionBackgroundColor(row, column + 1);
                    if (text_color_diff || background_color_diff) {
                        setPositionBitSpace(row, column, (byte) 3);
                        setPositionBitSpace(row, column + 1, (byte) 4);
                    } else {
                        setPositionBitSpace(row, column, (byte) 1);
                        setPositionBitSpace(row, column + 1, (byte) 2);
                    }
                    column++;
                } else {
                    setPositionBitSpace(row, column, (byte) 0);
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
                s.append(String.format("%1$02d ", Byte.valueOf(row.backgroundColor[j])));
            }
            System.out.println(s.toString());
        }
    }

    public void cleanCachedData() {
        Iterator<TelnetRow> it = this.rows.iterator();
        while (it.hasNext()) {
            TelnetRow row = it.next();
            row.cleanCachedData();
        }
    }
}
