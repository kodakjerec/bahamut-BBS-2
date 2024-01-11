package com.kota.TelnetUI;

public abstract class TelnetPage extends ASViewController {
    public void onPageDidUnload() {
        clear();
        super.onPageDidUnload();
    }

    public boolean onPagePreload() {
        return true;
    }

    public boolean isPopupPage() {
        return false;
    }

    public boolean isKeepOnOffline() {
        return false;
    }
}
