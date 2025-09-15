package com.kota.TelnetUI

import com.kota.ASFramework.PageController.ASViewController

abstract class TelnetPage : ASViewController() {
    
    override fun onPageDidUnload() {
        clear()
        super.onPageDidUnload()
    }
    
    open fun onPagePreload(): Boolean = true
    
    open fun isPopupPage(): Boolean = false
    
    open fun isKeepOnOffline(): Boolean = false
}
