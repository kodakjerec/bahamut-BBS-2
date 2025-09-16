package com.kota.asFramework.dialog

interface ASListDialogItemClickListener {
    fun onListDialogItemClicked(
        paramASListDialog: ASListDialog?,
        paramInt: Int,
        paramString: String?
    )

    fun onListDialogItemLongClicked(
        paramASListDialog: ASListDialog?,
        paramInt: Int,
        paramString: String?
    ): Boolean
} /* Location:              C:\Users\kodak\Downloads\反編譯\dex-tools-v2.4\classes-dex2jar.jar!\com\kumi\ASFramework\Dialog\ASListDialogItemClickListener.class
 * Java compiler version: 6 (50.0)
 * JD-Core Version:       1.1.3
 */


