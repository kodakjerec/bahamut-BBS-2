package com.kota.telnet

/* loaded from: classes.dex */
class TelnetCommand {
    var action: Byte
    var header: Byte
    var option: Byte

    constructor() {
        this.header = 0.toByte()
        this.action = 0.toByte()
        this.option = 0.toByte()
    }

    constructor(aHeader: Byte, aAction: Byte, aOption: Byte) {
        this.header = 0.toByte()
        this.action = 0.toByte()
        this.option = 0.toByte()
        this.header = aHeader
        this.action = aAction
        this.option = aOption
    }

    fun isEqualTo(aCommand: TelnetCommand): Boolean {
        return aCommand.header == this.header && aCommand.action == this.action && aCommand.option == this.option
    }

    fun isEqualTo(aHeader: Int, aAction: Int, aOption: Int): Boolean {
        return aHeader == this.header.toInt() && aAction == this.action.toInt() && aOption == this.option.toInt()
    }

    fun getCommandNameString(aCommand: Int): String {
        when (aCommand) {
            -16 -> return "SE"
            -6 -> return "SB"
            -5 -> return "WILL"
            -4 -> return "WONT"
            -3 -> return "DO"
            -2 -> return "DONT"
            -1 -> return "IAC"
            0 -> return "IS"
            1 -> return "ECHO"
            3 -> return "SG"
            24 -> return "TERMINAL_TYPE"
            31 -> return "NAWS"
            37 -> return "AUTH"
            39 -> return "NEW_ENV"
            else -> {
                val name = "UNKNOW(" + aCommand + ")"
                return name
            }
        }
    }

    override fun toString(): String {
        return getCommandNameString(this.header.toInt()) + "," + getCommandNameString(this.action.toInt()) + "," + getCommandNameString(
            this.option.toInt()
        )
    }

    companion object {
        const val AUTH: Byte = 37
        val DO: Byte = -3
        val DONT: Byte = -2
        const val ECHO: Byte = 1
        val IAC: Byte = -1
        const val IS: Byte = 0
        const val NAWS: Byte = 31
        const val NEW_ENV: Byte = 39
        val SB: Byte = -6
        val SE: Byte = -16
        const val SG: Byte = 3
        const val TERMINAL_TYPE: Byte = 24
        val WILL: Byte = -5
        val WONT: Byte = -4
    }
}
