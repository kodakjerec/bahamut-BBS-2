package com.kota.Telnet;

public class TelnetAnsi {
    public static byte DEFAULT_BACKGROUND_COLOR = 0;
    private static boolean DEFAULT_TEXT_BLINK = false;
    private static boolean DEFAULT_TEXT_BRIGHT = false;
    public static byte DEFAULT_TEXT_COLOR = 7;
    private static boolean DEFAULT_TEXT_ITALIC = false;
    public byte backgroundColor = 0;
    public boolean textBlink = false;
    public boolean textBright = false;
    public byte textColor = 0;
    public boolean textItalic = false;

    public TelnetAnsi() {
        resetToDefaultState();
    }

    public void resetToDefaultState() {
        this.textColor = DEFAULT_TEXT_COLOR;
        this.textBlink = DEFAULT_TEXT_BLINK;
        this.textBright = DEFAULT_TEXT_BRIGHT;
        this.textItalic = DEFAULT_TEXT_ITALIC;
        this.backgroundColor = DEFAULT_BACKGROUND_COLOR;
    }

    public static byte getDefaultTextColor() {
        return DEFAULT_TEXT_COLOR;
    }

    public static byte getDefaultBackgroundColor() {
        return DEFAULT_BACKGROUND_COLOR;
    }

    public static boolean getDefaultTextBlink() {
        return DEFAULT_TEXT_BLINK;
    }

    public static boolean getDefaultTextItalic() {
        return DEFAULT_TEXT_ITALIC;
    }
}
