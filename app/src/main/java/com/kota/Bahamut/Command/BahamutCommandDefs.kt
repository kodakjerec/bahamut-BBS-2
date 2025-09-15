package com.kota.Bahamut.Command

interface BahamutCommandDefs {
    companion object {
        const val LoadBlock = 0
        const val LoadFirstBlock = 1
        const val LoadLastBlock = 2
        const val MoveToLastBlock = 3
        const val SearchArticle = 4
        const val LoadArticle = 5
        const val ListArticle = 6
        const val DeleteArticle = 7
        const val PostArticle = 8
        const val GoodArticle = 9
        const val LoadForwardSameTitleItem = 10
        const val TheSameTitleDown = 11
        const val SendMail = 12
        const val PushArticle = 13
    }
}
