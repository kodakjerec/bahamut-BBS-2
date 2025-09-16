package com.kota.asFramework.model

class ASSize {
    var height: Int = 0

    var width: Int = 0

    constructor()

    constructor(paramInt1: Int, paramInt2: Int) {
        this.width = paramInt1
        this.height = paramInt2
    }

    val isZero: Boolean
        get() = (this.width == 0 && this.height == 0)

    fun set(paramInt1: Int, paramInt2: Int) {
        this.width = paramInt1
        this.height = paramInt2
    }

    fun set(paramASSize: ASSize) {
        set(paramASSize.width, paramASSize.height)
    }

    override fun toString(): String {
        return "(" + this.width + "," + this.height + ")"
    }
} /* Location:              C:\Users\kodak\Downloads\反編譯\dex-tools-v2.4\classes-dex2jar.jar!\com\kumi\ASFramework\Model\ASSize.class
 * Java compiler version: 6 (50.0)
 * JD-Core Version:       1.1.3
 */


