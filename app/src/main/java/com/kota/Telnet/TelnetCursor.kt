package com.kota.Telnet

class TelnetCursor {
    @JvmField
    var column: Int = 0
    @JvmField
    var row: Int = 0

    constructor()

    constructor(aRow: Int, aColumn: Int) {
        set(aRow, aColumn)
    }

    fun set(aRow: Int, aColumn: Int) {
        this.row = aRow
        this.column = aColumn
    }

    fun set(aCursor: TelnetCursor) {
        this.row = aCursor.row
        this.column = aCursor.column
    }

    fun equals(aRow: Int, aColumn: Int): Boolean {
        return aRow == this.row && aColumn == this.column
    }

    fun equals(aCursor: TelnetCursor): Boolean {
        return aCursor.row == this.row && aCursor.column == this.column
    }

    public override fun clone(): TelnetCursor {
        return TelnetCursor(this.row, this.column)
    }

    override fun toString(): String {
        return "( " + this.row + " , " + this.column + " )"
    }
}
