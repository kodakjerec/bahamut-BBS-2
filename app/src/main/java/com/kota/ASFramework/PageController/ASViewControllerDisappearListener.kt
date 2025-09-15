package com.kota.ASFramework.PageController

interface ASViewControllerDisappearListener {
    fun onASViewControllerDidDisappear(viewController: ASViewController)
    fun onASViewControllerWillDisappear(viewController: ASViewController)
}
