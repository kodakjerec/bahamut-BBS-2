package com.kota.Bahamut.command

interface BahamutCommandDef {
    companion object {
        const val LOAD_BLOCK: Int = 0
        const val LOAD_FIRST_BLOCK: Int = 1
        const val LOAD_LAST_BLOCK: Int = 2
        const val MOVE_TO_LAST_BLOCK: Int = 3
        const val SEARCH_ARTICLE: Int = 4
        const val LOAD_ARTICLE: Int = 5
        const val LIST_ARTICLE: Int = 6
        const val DELETE_ARTICLE: Int = 7
        const val POST_ARTICLE: Int = 8
        const val GOOD_ARTICLE: Int = 9
        const val LOAD_FORWARD_SAME_ARTICLE: Int = 10
        const val THE_SAME_TITLE_DOWN: Int = 11
        const val SEND_MAIL: Int = 12
        const val PUSH_ARTICLE: Int = 13
    }
}
