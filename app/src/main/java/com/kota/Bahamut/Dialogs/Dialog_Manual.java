package com.kota.Bahamut.Dialogs;

import com.kota.ASFramework.Dialog.ASDialog;
import com.kota.Bahamut.R;

public class Dialog_Manual extends ASDialog {
    public Dialog_Manual(int layout) {
        super(R.style.Dialog_NoTitle_FullScreen);
        setContentView(layout);
    }
}
