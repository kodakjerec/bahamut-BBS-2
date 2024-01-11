package com.kumi.ASFramework.PageController;

import android.content.Context;
import android.content.Intent;
import android.content.res.AssetManager;
import android.content.res.Resources;
import android.view.View;
import java.util.Vector;

public abstract class ASViewController {
  public static final int ALERT_LEFT_BUTTON = -1;
  
  public static final int ALERT_MIDDLE_BUTTON = -3;
  
  public static final int ALERT_RIGHT_BUTTON = -2;
  
  private Vector<ASViewControllerAppearListener> _appear_listeners = null;
  
  private boolean _contains_in_container = false;
  
  private ASNavigationController _controller = null;
  
  private Vector<ASViewControllerDisappearListener> _disappear_listeners = null;
  
  private boolean _is_appeared = false;
  
  private boolean _is_disappeared = true;
  
  protected boolean _is_loaded = false;
  
  private boolean _is_request_refresh = false;
  
  private boolean _marked_added = false;
  
  private boolean _marked_removed = false;
  
  private Vector<ASViewControllerOperationListener> _operation_listeners = null;
  
  private ASPageView _page_view = null;
  
  public boolean backToMainToolbarAfterExtendToolbarClicked() {
    return true;
  }
  
  protected void cleanMark() {
    this._marked_removed = false;
    this._marked_added = false;
  }
  
  public void clear() {}
  
  public boolean containsInContainer() {
    return this._contains_in_container;
  }
  
  protected void finalize() throws Throwable {
    onPageDidUnload();
    super.finalize();
  }
  
  public View findViewById(int paramInt) {
    return (this._page_view != null) ? this._page_view.findViewById(paramInt) : null;
  }
  
  public AssetManager getAssets() {
    return this._controller.getAssets();
  }
  
  public Context getContext() {
    return (Context)this._controller;
  }
  
  public ASNavigationController getNavigationController() {
    return this._controller;
  }
  
  public abstract int getPageLayout();
  
  public int getPageType() {
    return 0;
  }
  
  public ASPageView getPageView() {
    return this._page_view;
  }
  
  public Resources getResource() {
    return this._controller.getResources();
  }
  
  public Object getSystemService(String paramString) {
    return (this._controller != null) ? this._controller.getSystemService(paramString) : null;
  }
  
  protected boolean isMarkedAdded() {
    return this._marked_added;
  }
  
  protected boolean isMarkedRemoved() {
    return this._marked_removed;
  }
  
  public boolean isPageAppeared() {
    return this._is_appeared;
  }
  
  public boolean isPageDisappeared() {
    return this._is_disappeared;
  }
  
  public boolean isTopPage() {
    boolean bool = false;
    if (getNavigationController() != null && getNavigationController().getTopController() == this)
      bool = true; 
    return bool;
  }
  
  public void notifyPageDidAddToNavigationController() {
    if (this._operation_listeners != null)
      for (ASViewControllerOperationListener aSViewControllerOperationListener : new Vector(this._operation_listeners)) {
        if (aSViewControllerOperationListener != null)
          aSViewControllerOperationListener.onASViewControllerWillAddToNavigationController(this); 
      }  
    onPageDidAddToNavigationController();
  }
  
  public void notifyPageDidAppear() {
    this._is_appeared = true;
    if (this._appear_listeners != null)
      for (ASViewControllerAppearListener aSViewControllerAppearListener : new Vector(this._appear_listeners)) {
        if (aSViewControllerAppearListener != null)
          aSViewControllerAppearListener.onASViewControllerDidAppear(this); 
      }  
    onPageDidAppear();
    if (this._is_request_refresh) {
      onPageRefresh();
      this._is_request_refresh = false;
    } 
  }
  
  public void notifyPageDidDisappear() {
    this._is_disappeared = true;
    if (this._disappear_listeners != null)
      for (ASViewControllerDisappearListener aSViewControllerDisappearListener : new Vector(this._disappear_listeners)) {
        if (aSViewControllerDisappearListener != null)
          aSViewControllerDisappearListener.onASViewControllerDidDisappear(this); 
      }  
    onPageDidDisappear();
  }
  
  public void notifyPageDidRemoveFromNavigationController() {
    if (this._operation_listeners != null)
      for (ASViewControllerOperationListener aSViewControllerOperationListener : new Vector(this._operation_listeners)) {
        if (aSViewControllerOperationListener != null)
          aSViewControllerOperationListener.onASViewControllerWillRemoveFromNavigationController(this); 
      }  
    onPageDidRemoveFromNavigationController();
  }
  
  public void notifyPageWillAppear() {
    this._is_disappeared = false;
    if (this._appear_listeners != null)
      for (ASViewControllerAppearListener aSViewControllerAppearListener : new Vector(this._appear_listeners)) {
        if (aSViewControllerAppearListener != null)
          aSViewControllerAppearListener.onASViewControllerWillAppear(this); 
      }  
    onPageWillAppear();
  }
  
  public void notifyPageWillDisappear() {
    this._is_appeared = false;
    if (this._disappear_listeners != null)
      for (ASViewControllerDisappearListener aSViewControllerDisappearListener : new Vector(this._disappear_listeners)) {
        if (aSViewControllerDisappearListener != null)
          aSViewControllerDisappearListener.onASViewControllerWillDisappear(this); 
      }  
    onPageWillDisappear();
  }
  
  protected boolean onBackPressed() {
    getNavigationController().popViewController();
    return true;
  }
  
  protected boolean onMenuButtonClicked() {
    return false;
  }
  
  public void onPageDidAddToNavigationController() {}
  
  public void onPageDidAppear() {}
  
  public void onPageDidDisappear() {}
  
  public void onPageDidLoad() {}
  
  public void onPageDidRemoveFromNavigationController() {}
  
  public void onPageDidUnload() {}
  
  public void onPageFreeMemory() {}
  
  public void onPageRefresh() {}
  
  public void onPageWillAppear() {}
  
  public void onPageWillDisappear() {}
  
  public boolean onReceivedGestureDown() {
    return false;
  }
  
  public boolean onReceivedGestureLeft() {
    return false;
  }
  
  public boolean onReceivedGestureRight() {
    return false;
  }
  
  public boolean onReceivedGestureUp() {
    return false;
  }
  
  protected boolean onSearchButtonClicked() {
    return false;
  }
  
  protected void onSizeChanged(int paramInt1, int paramInt2, int paramInt3, int paramInt4) {}
  
  protected void prepareForAdd() {
    this._marked_removed = false;
    this._marked_added = true;
  }
  
  protected void prepareForRemove() {
    this._marked_removed = true;
  }
  
  public void registerAppearListener(ASViewControllerAppearListener paramASViewControllerAppearListener) {
    if (this._appear_listeners == null)
      this._appear_listeners = new Vector<ASViewControllerAppearListener>(); 
    this._appear_listeners.add(paramASViewControllerAppearListener);
  }
  
  public void registerDisappearListener(ASViewControllerDisappearListener paramASViewControllerDisappearListener) {
    if (this._disappear_listeners == null)
      this._disappear_listeners = new Vector<ASViewControllerDisappearListener>(); 
    this._disappear_listeners.add(paramASViewControllerDisappearListener);
  }
  
  public void registerPageOperationListener(ASViewControllerOperationListener paramASViewControllerOperationListener) {
    if (this._operation_listeners == null)
      this._operation_listeners = new Vector<ASViewControllerOperationListener>(); 
    this._operation_listeners.add(paramASViewControllerOperationListener);
  }
  
  public void reloadLayout() {
    if (this._controller != null)
      this._controller.reloadLayout(); 
  }
  
  public void requestPageRefresh() {
    if (this._is_appeared) {
      onPageRefresh();
      return;
    } 
    this._is_request_refresh = true;
  }
  
  public void setContainsInContainer(boolean paramBoolean) {
    this._contains_in_container = paramBoolean;
  }
  
  public void setNavigationController(ASNavigationController paramASNavigationController) {
    this._controller = paramASNavigationController;
  }
  
  public void setPageView(ASPageView paramASPageView) {
    if (this._page_view != null)
      this._page_view.setOwnerController((ASViewController)null); 
    this._page_view = paramASPageView;
    if (this._page_view != null)
      this._page_view.setOwnerController(this); 
  }
  
  public void startActivity(Intent paramIntent) {
    if (this._controller != null)
      this._controller.startActivity(paramIntent); 
  }
  
  public String toString() {
    return "ASViewController[Type:" + getPageType() + "]";
  }
  
  public void unregisterAppearListener(ASViewControllerAppearListener paramASViewControllerAppearListener) {
    if (this._appear_listeners != null)
      this._appear_listeners.remove(paramASViewControllerAppearListener); 
  }
  
  public void unregisterDisappearListener(ASViewControllerDisappearListener paramASViewControllerDisappearListener) {
    if (this._disappear_listeners != null)
      this._disappear_listeners.remove(paramASViewControllerDisappearListener); 
  }
  
  public void unregisterPageOperationListener(ASViewControllerOperationListener paramASViewControllerOperationListener) {
    if (this._operation_listeners != null)
      this._operation_listeners.remove(paramASViewControllerOperationListener); 
  }
}


/* Location:              C:\Users\kodak\Downloads\反編譯\dex-tools-v2.4\classes-dex2jar.jar!\com\kumi\ASFramework\PageController\ASViewController.class
 * Java compiler version: 6 (50.0)
 * JD-Core Version:       1.1.3
 */