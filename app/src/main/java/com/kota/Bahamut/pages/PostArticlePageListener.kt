package com.kota.Bahamut.pages

interface PostArticlePageListener {
    fun onPostDialogEditButtonClicked(
        postArticlePage: PostArticlePage?,
        str: String?,
        str2: String?,
        str3: String?
    )

    fun onPostDialogSendButtonClicked(
        postArticlePage: PostArticlePage?,
        str: String?,
        str2: String?,
        str3: String?,
        str4: String?,
        str5: String?,
        boolean6: Boolean?
    )
}
