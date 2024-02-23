package com.kota.Bahamut.Pages;

import com.kota.Bahamut.BahamutPage;
import com.kota.Bahamut.R;
import com.kota.TelnetUI.TelnetPage;

public class InstructionsPage extends TelnetPage {
    @Override
    public int getPageType() {
        return BahamutPage.BAHAMUT_INSTRUCTIONS;
    }

    @Override
    public int getPageLayout() { return R.layout.instruction_page; }

    @Override // com.kota.TelnetUI.TelnetPage
    public boolean isPopupPage() {
        return true;
    }

    @Override // com.kota.TelnetUI.TelnetPage
    public boolean isKeepOnOffline() {
        return true;
    }

    @Override
    public void onPageDidLoad() {

    }
}
