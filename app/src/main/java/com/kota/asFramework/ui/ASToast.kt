package com.kota.asFramework.ui

import android.widget.Toast
import com.kota.asFramework.pageController.ASNavigationController
import com.kota.asFramework.thread.ASRunner

object ASToast {
    private var previousToast: Toast? = null
    @JvmStatic
    fun showShortToast(aToastMessage: String?) {
        object : ASRunner() {
            // from class: com.kota.ASFramework.UI.ASToast.1
            // com.kota.ASFramework.Thread.ASRunner
            override fun run() {
                if (previousToast != null) {
                    previousToast?.cancel()
                }
                previousToast = Toast.makeText(
                    ASNavigationController.getCurrentController(),
                    aToastMessage,
                    Toast.LENGTH_SHORT
                )
                previousToast?.show()
            }
        }.runInMainThread()
    }

    @JvmStatic
    fun showLongToast(aToastMessage: String?) {
        object : ASRunner() {
            // from class: com.kota.ASFramework.UI.ASToast.2
            // com.kota.ASFramework.Thread.ASRunner
            override fun run() {
                if (previousToast != null) {
                    previousToast?.cancel()
                }
                previousToast = Toast.makeText(
                    ASNavigationController.getCurrentController(),
                    aToastMessage,
                    Toast.LENGTH_LONG
                )
                previousToast?.show()
            }
        }.runInMainThread()
    }
}