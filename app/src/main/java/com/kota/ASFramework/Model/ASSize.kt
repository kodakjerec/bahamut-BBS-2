package com.kota.ASFramework.Model

data class ASSize(
    var width: Int = 0,
    var height: Int = 0
) {
    
    constructor(width: Int, height: Int) : this() {
        this.width = width
        this.height = height
    }
    
    fun isZero(): Boolean = width == 0 && height == 0
    
    fun set(width: Int, height: Int) {
        this.width = width
        this.height = height
    }
    
    fun set(size: ASSize) {
        set(size.width, size.height)
    }
    
    override fun toString(): String = "($width,$height)"
}
