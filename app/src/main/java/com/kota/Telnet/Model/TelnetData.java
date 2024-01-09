package com.kota.Telnet.Model;

public class TelnetData {
    public static final byte BIT_SPACE_1 = 0;
    public static final byte DOUBLE_BIT_SPACE_2_1 = 3;
    public static final byte DOUBLE_BIT_SPACE_2_2 = 4;
    public static final byte SINGLE_BIT_SPACE_2_1 = 1;
    public static final byte SINGLE_BIT_SPACE_2_2 = 2;
    private static final int _count = 0;
    public byte backgroundColor = 0;
    public byte bitSpace = 0;
    public boolean blink = false;
    public byte data = 0;
    public boolean italic = false;
    public byte textColor = 0;

    /* access modifiers changed from: protected */
    protected void finalize() throws Throwable {
        super.finalize();
    }

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
        this.data = 0;
        this.textColor = 0;
        this.backgroundColor = 0;
        this.blink = false;
        this.italic = false;
    }

    public boolean isEmpty() {
        return this.data == 0;
    }
}
