package com.kota.Telnet.Model;

public class TelnetData {
    private static int _count = 0;
    public byte data = 0;
    public byte textColor = 0;
    public byte backgroundColor = 0;
    public boolean blink = false;
    public boolean italic = false;

    public TelnetData() {
    }

    public TelnetData(TelnetData aData) {
        set(aData);
    }

    public void set(TelnetData aData) {
        this.data = aData.data;
        this.textColor = aData.textColor;
        this.backgroundColor = aData.backgroundColor;
        this.blink = aData.blink;
        this.italic = aData.italic;
    }

    public TelnetData clone() {
        return new TelnetData(this);
    }

    public void clear() {
        this.data = (byte) 0;
        this.textColor = (byte) 0;
        this.backgroundColor = (byte) 0;
        this.blink = false;
        this.italic = false;
    }

    public boolean isEmpty() {
        return this.data == 0;
    }
}
