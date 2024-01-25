package com.kota.Telnet;

import com.kota.Telnet.Model.TelnetRow;
import java.util.Vector;

public class TelnetArticlePage {
    private Vector<TelnetRow> rows = new Vector<>();

    public void addRow(TelnetRow row) {
        this.rows.add(row.clone());
    }

    public int getRowCount() {
        return this.rows.size();
    }

    public TelnetRow getRow(int index) {
        return this.rows.get(index);
    }

    public void clear() {
        this.rows.clear();
    }
}
