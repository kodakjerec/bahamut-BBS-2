package com.kota.Telnet;

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

    public void setCurrentPage(int pageID) {
        this._current_page = pageID;
    }

    public int getCurrentPage() {
        return this._current_page;
    }

    public void clear() {
    }
}
