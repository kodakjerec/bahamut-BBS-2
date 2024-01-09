package com.kota.ASFramework.PageController;

public interface ASViewControllerOperationListener {
    void onASViewControllerWillAddToNavigationController(ASViewController aSViewController);

    void onASViewControllerWillRemoveFromNavigationController(ASViewController aSViewController);
}
