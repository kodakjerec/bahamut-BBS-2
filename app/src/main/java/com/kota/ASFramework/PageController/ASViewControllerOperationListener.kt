package com.kota.ASFramework.PageController

interface ASViewControllerOperationListener {
    fun onASViewControllerWillAddToNavigationController(viewController: ASViewController)
    fun onASViewControllerWillRemoveFromNavigationController(viewController: ASViewController)
}
