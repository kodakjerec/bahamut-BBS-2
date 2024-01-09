package com.kota.ASFramework.Model;

public class ASSize {
    public int height = 0;
    public int width = 0;

    public ASSize() {
    }

    public ASSize(int aWidth, int aHeight) {
        this.width = aWidth;
        this.height = aHeight;
    }

    public void set(ASSize aSize) {
        set(aSize.width, aSize.height);
    }

    public void set(int aWidth, int aHeight) {
        this.width = aWidth;
        this.height = aHeight;
    }

    public String toString() {
        return "(" + this.width + "," + this.height + ")";
    }

    public boolean isZero() {
        return this.width == 0 && this.height == 0;
    }
}
