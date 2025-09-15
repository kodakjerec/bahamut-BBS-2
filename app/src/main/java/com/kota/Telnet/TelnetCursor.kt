package com.kota.Telnet

data class TelnetCursor(
    var row: Int = 0,
    var column: Int = 0
) {
    
    fun set(row: Int, column: Int) {
        this.row = row
        this.column = column
    }
    
    fun set(cursor: TelnetCursor) {
        this.row = cursor.row
        this.column = cursor.column
    }
    
    fun equals(row: Int, column: Int): Boolean {
        return row == this.row && column == this.column
    }
    
    fun equals(cursor: TelnetCursor): Boolean {
        return cursor.row == this.row && cursor.column == this.column
    }
    
    fun clone(): TelnetCursor {
        return TelnetCursor(this.row, this.column)
    }
    
    override fun toString(): String {
        return "( $row , $column )"
    }
}
