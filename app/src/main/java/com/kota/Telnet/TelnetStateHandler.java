package com.kota.Telnet;

import com.kota.Telnet.Model.TelnetRow;

import java.util.Vector;

public abstract class TelnetStateHandler {
    int _current_page = 0;

    public abstract void handleState();

    public void cleanRow(int row) {
        if (TelnetClient.getModel() != null) {
            TelnetClient.getModel().cleanRow(row);
        }
    }

    public void cleanFrame() {
        if (TelnetClient.getModel() != null) {
            TelnetClient.getModel().cleanFrame();
        }
    }

    public String getRowString(int row) {
        if (TelnetClient.getModel() != null) {
            return TelnetClient.getModel().getRowString(row);
        }
        return "";
    }

    public Vector<TelnetRow> getRows() {
        return TelnetClient.getModel().getRows();
    }

    public void setCurrentPage(int pageID) {
        this._current_page = pageID;
    }

    public int getCurrentPage() {
        return this._current_page;
    }

    public void clear() {
    }
}
