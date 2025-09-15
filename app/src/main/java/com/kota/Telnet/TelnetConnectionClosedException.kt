package com.kota.Telnet

class TelnetConnectionClosedException : Exception("Telnet Closed") {
    companion object {
        private const val serialVersionUID = 7359377282779718712L
    }
}
