package com.kota.Bahamut.Dialogs;

import java.util.Vector;

public interface Dialog_SearchArticle_Listener {
    void onSearchDialogSearchButtonClickedWithValues(Vector<String> vector);

    void onSearchDialogCancelButtonClicked();
}
