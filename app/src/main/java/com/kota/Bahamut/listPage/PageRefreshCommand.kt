package com.kota.Bahamut.listPage

interface PageRefreshCommand {
    companion object {
        const val MOVE_TO_FIRST_POINT: Int = 0
        const val MOVE_TO_LAST_POINT: Int = 1
    }
}
