package com.kota.TelnetUI

import com.kota.ASFramework.PageController.ASViewController

abstract class TelnetPage : ASViewController() {
    override fun onPageDidUnload() {
        clear()
        super.onPageDidUnload()
    }

    open fun onPagePreload(): Boolean {
        return true
    }

    open val isPopupPage: Boolean
        get() = false

    open val isKeepOnOffline: Boolean
        get() = false
}
