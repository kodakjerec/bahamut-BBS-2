package com.kota.Bahamut.listPage

interface PageRefreshCommand {
    companion object {
        const val MoveToFirstPoint: Int = 0
        const val MoveToLastPoint: Int = 1
    }
}
