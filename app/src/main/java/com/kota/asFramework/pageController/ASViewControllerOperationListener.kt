package com.kota.asFramework.pageController

interface ASViewControllerOperationListener {
    fun onASViewControllerWillAddToNavigationController(paramASViewController: ASViewController?)

    fun onASViewControllerWillRemoveFromNavigationController(paramASViewController: ASViewController?)
}

