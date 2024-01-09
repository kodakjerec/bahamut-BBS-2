package com.kota.ASFramework.Dialog;

public interface ASListDialogItemClickListener {
    void onListDialogItemClicked(ASListDialog aSListDialog, int i, String str);

    boolean onListDialogItemLongClicked(ASListDialog aSListDialog, int i, String str);
}
