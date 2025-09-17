package com.kota.Bahamut

interface BahamutPage {
    companion object {
        const val BAHAMUT_LOGIN: Int = 1
        const val BAHAMUT_SYSTEM_ANNOUNCEMENT: Int = 2
        const val BAHAMUT_INSTRUCTIONS: Int = 3
        const val BAHAMUT_PASSED_SIGNATURE: Int = 4
        const val BAHAMUT_MAIN: Int = 5
        const val BAHAMUT_CLASS: Int = 6
        const val BAHAMUT_SYSTEM_SETTINGS: Int = 7
        const val BAHAMUT_BLOCK_LIST: Int = 8
        const val BAHAMUT_MAIL_BOX: Int = 9
        const val BAHAMUT_BOARD: Int = 10
        const val BAHAMUT_BOOKMARK: Int = 11
        const val BAHAMUT_BOARD_LINK: Int = 12
        const val BAHAMUT_BOARD_SEARCH: Int = 13
        const val BAHAMUT_ARTICLE: Int = 14
        const val BAHAMUT_MAIL: Int = 15
        const val BAHAMUT_POST_ARTICLE: Int = 16
        const val BAHAMUT_SEND_MAIL: Int = 17
        const val BAHAMUT_BILLING: Int = 18
        const val BAHAMUT_BOARD_ESSENCE: Int = 19
        const val BAHAMUT_ARTICLE_ESSENCE: Int = 20
        const val BAHAMUT_THEME_MANAGER_PAGE: Int = 21
        const val BAHAMUT_USER_INFO_PAGE: Int = 22
        const val BAHAMUT_USER_CONFIG_PAGE: Int = 23
        const val BAHAMUT_MESSAGE_MAIN_PAGE: Int = 24
        const val BAHAMUT_MESSAGE_SUB_PAGE: Int = 25
        const val MANUAL: Int = 26
        const val START: Int = 0
        val UNKNOWN: Int = -1
    }
}
