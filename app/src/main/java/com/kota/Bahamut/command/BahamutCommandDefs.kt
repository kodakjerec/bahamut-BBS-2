package com.kota.Bahamut.command

interface BahamutCommandDefs {
    companion object {
        const val LoadBlock: Int = 0
        const val LoadFirstBlock: Int = 1
        const val LoadLastBlock: Int = 2
        const val MoveToLastBlock: Int = 3
        const val SearchArticle: Int = 4
        const val LoadArticle: Int = 5
        const val ListArticle: Int = 6
        const val DeleteArticle: Int = 7
        const val PostArticle: Int = 8
        const val GoodArticle: Int = 9
        const val LoadForwardSameTitleItem: Int = 10
        const val TheSameTitleDown: Int = 11
        const val SendMail: Int = 12
        const val PushArticle: Int = 13
    }
}
