package com.kota.ASFramework.UI

import android.widget.Toast
import com.kota.ASFramework.PageController.ASNavigationController
import com.kota.ASFramework.Thread.ASRunner

object ASToast {
    private var previousToast: Toast? = null
    
    fun showShortToast(message: String) {
        object : ASRunner() {
            override fun run() {
                previousToast?.cancel()
                previousToast = Toast.makeText(
                    ASNavigationController.getCurrentController(), 
                    message, 
                    Toast.LENGTH_SHORT
                )
                previousToast?.show()
            }
        }.runInMainThread()
    }
    
    fun showLongToast(message: String) {
        object : ASRunner() {
            override fun run() {
                previousToast?.cancel()
                previousToast = Toast.makeText(
                    ASNavigationController.getCurrentController(), 
                    message, 
                    Toast.LENGTH_LONG
                )
                previousToast?.show()
            }
        }.runInMainThread()
    }
}
