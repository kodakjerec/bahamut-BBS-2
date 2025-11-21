package com.kota.telnet.logic

interface ClassStep {
    companion object {
        const val CLOSING: Int = 3
        const val LOADING_NORMAL: Int = 0
        const val LOADING_SIZE: Int = 1
        const val LOADING_START: Int = 2
    }
}
