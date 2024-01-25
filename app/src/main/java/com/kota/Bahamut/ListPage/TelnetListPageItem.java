package com.kota.Bahamut.ListPage;

public class TelnetListPageItem {
    public int Number = 0;
    public boolean isBlocked = false;
    public boolean isDeleted = false;
    public boolean isLoading = false;

    public void set(TelnetListPageItem aData) {
        if (aData != null) {
            this.isDeleted = aData.isDeleted;
            this.isLoading = aData.isLoading;
        }
    }

    public void clear() {
        this.Number = 0;
        this.isDeleted = false;
        this.isLoading = false;
        this.isBlocked = false;
    }
}
