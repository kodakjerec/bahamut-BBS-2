package com.kota.telnet

object TelnetConnectionClosedException : Exception() {
    private fun readResolve(): Any = TelnetConnectionClosedException
    private const val serialVersionUID = 7359377282779718712L
}
