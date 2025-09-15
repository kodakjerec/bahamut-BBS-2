package com.kota.Telnet

class TelnetCommand {
    companion object {
        const val AUTH: Byte = 37
        const val DO: Byte = -3
        const val DONT: Byte = -2
        const val ECHO: Byte = 1
        const val IAC: Byte = -1
        const val IS: Byte = 0
        const val NAWS: Byte = 31
        const val NEW_ENV: Byte = 39
        const val SB: Byte = -6
        const val SE: Byte = -16
        const val SG: Byte = 3
        const val TERMINAL_TYPE: Byte = 24
        const val WILL: Byte = -5
        const val WONT: Byte = -4
    }
    
    var header: Byte = 0
    var action: Byte = 0
    var option: Byte = 0
    
    constructor() {
        header = 0
        action = 0
        option = 0
    }
    
    constructor(header: Byte, action: Byte, option: Byte) {
        this.header = header
        this.action = action
        this.option = option
    }
    
    fun isEqualTo(command: TelnetCommand): Boolean {
        return command.header == header && command.action == action && command.option == option
    }
    
    fun isEqualTo(headerValue: Int, actionValue: Int, optionValue: Int): Boolean {
        return headerValue == header.toInt() && actionValue == action.toInt() && optionValue == option.toInt()
    }
    
    fun getCommandNameString(command: Int): String {
        return when (command) {
            SE.toInt() -> "SE"
            SB.toInt() -> "SB"
            WILL.toInt() -> "WILL"
            WONT.toInt() -> "WONT"
            DO.toInt() -> "DO"
            DONT.toInt() -> "DONT"
            IAC.toInt() -> "IAC"
            IS.toInt() -> "IS"
            ECHO.toInt() -> "ECHO"
            SG.toInt() -> "SG"
            TERMINAL_TYPE.toInt() -> "TERMINAL_TYPE"
            NAWS.toInt() -> "NAWS"
            AUTH.toInt() -> "AUTH"
            NEW_ENV.toInt() -> "NEW_ENV"
            else -> "UNKNOWN($command)"
        }
    }
    
    override fun toString(): String {
        return "${getCommandNameString(header.toInt())},${getCommandNameString(action.toInt())},${getCommandNameString(option.toInt())}"
    }
}
