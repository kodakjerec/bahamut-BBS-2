package com.kota.ASFramework.PageController;

import android.app.Activity;
import android.os.Bundle;
import android.util.DisplayMetrics;
import android.view.KeyEvent;
import android.view.View;
import android.view.animation.Animation;
import android.widget.FrameLayout;

import com.kota.ASFramework.Thread.ASRunner;

import java.util.Iterator;
import java.util.Vector;

/* loaded from: classes.dex */
public class ASNavigationController extends Activity {
  private static ASNavigationController _current_controller = null;
  private ASDeviceController _device_controller = null;
  private DisplayMetrics _display_metrics = new DisplayMetrics();
  private ASNavigationControllerView _root_view = null;
  private Vector<ASViewController> _controllers = new Vector<>();
  private Vector<ASViewController> _temp_controllers = new Vector<>();
  private Vector<ASViewController> _remove_list = new Vector<>();
  private Vector<ASViewController> _add_list = new Vector<>();
  private boolean _animation_enable = true;
  private boolean _in_background = false;
  private Vector<PageCommand> _page_commands = new Vector<>();
  private boolean _is_animating = false;

  /* loaded from: classes.dex */
  private abstract class PageCommand {
    public boolean animated;

    public abstract void run();

    private PageCommand() {
      this.animated = true;
    }
  }

  protected void onControllerWillLoad() {
  }

  protected void onControllerDidLoad() {
  }

  protected void onControllerWillFinish() {
  }

  private static void setNavigationController(ASNavigationController aController) {
    _current_controller = aController;
  }

  public static ASNavigationController getCurrentController() {
    return _current_controller;
  }

  public void setNavigationTitle(String title) {
    super.setTitle(title);
  }

  protected String getControllerName() {
    return "";
  }

  private boolean onMenuPressed() {
    ASViewController page = getTopController();
    if (page == null || !page.onMenuButtonClicked()) {
      return false;
    }
    return true;
  }

  private boolean onSearchPressed() {
    ASViewController page = getTopController();
    if (page == null || !page.onSearchButtonClicked()) {
      return false;
    }
    return true;
  }

  @Override // android.app.Activity
  public void onBackPressed() {
    boolean handle_back_button = false;
    ASViewController page = getTopController();
    if (page != null && page.onBackPressed()) {
      handle_back_button = true;
    }
    if (!handle_back_button) {
      finish();
    }
  }

  public boolean onBackLongPressed() {
    return false;
  }

  @Override // android.app.Activity
  public void onCreate(Bundle savedInstanceState) {
    super.onCreate(savedInstanceState);
    setNavigationController(this);

    getWindowManager().getDefaultDisplay().getMetrics(this._display_metrics);
    ASRunner.construct();

    this._device_controller = new ASDeviceController(this);
    onControllerWillLoad();

    this._root_view = new ASNavigationControllerView(this);
    this._root_view.setPageController(this);
    setContentView(this._root_view);
    onControllerDidLoad();
  }

  @Override // android.app.Activity, android.view.KeyEvent.Callback
  public boolean onKeyLongPress(int keyCode, KeyEvent event) {
    if (keyCode == 4) {
      boolean result = onBackLongPressed();
      return result;
    }
    boolean result2 = super.onKeyLongPress(keyCode, event);
    return result2;
  }

  @Override // android.app.Activity, android.view.KeyEvent.Callback
  public boolean onKeyUp(int keyCode, KeyEvent event) {
    switch (keyCode) {
      case 82:
        boolean result = onMenuPressed();
        return result;
      case 83:
      default:
        boolean result2 = super.onKeyUp(keyCode, event);
        return result2;
      case 84:
        boolean result3 = onSearchPressed();
        return result3;
    }
  }

  public ASViewController getTopController() {
    ASViewController controller = null;
    synchronized (this._controllers) {
      if (this._controllers.size() > 0) {
        controller = this._controllers.lastElement();
      }
    }
    return controller;
  }

  private void buildPageView(ASViewController controller) {
    ASPageView page_view = new ASPageView(this);
    page_view.setLayoutParams(new FrameLayout.LayoutParams(-1, -1));
    page_view.setBackgroundColor(View.MEASURED_STATE_MASK);
    getLayoutInflater().inflate(controller.getPageLayout(), page_view);
    controller.setPageView(page_view);
    this._root_view.getContentView().addView(page_view);
  }

  private void cleanPageView(ASViewController controller) {
    controller.setPageView(null);
  }

  public void addPageView(final ASViewController aPage) {
    final View page_view = aPage.getPageView();
    this._root_view.post(new Runnable() { // from class: com.kota.ASFramework.PageController.ASNavigationController.1
      @Override // java.lang.Runnable
      public void run() {
        aPage.onPageDidDisappear();
        ASNavigationController.this._root_view.removeView(page_view);
      }
    });
  }

  public void removePageView(final ASViewController aPage) {
    final View page_view = aPage.getPageView();
    this._root_view.post(new Runnable() { // from class: com.kota.ASFramework.PageController.ASNavigationController.2
      @Override // java.lang.Runnable
      public void run() {
        aPage.onPageDidDisappear();
        ASNavigationController.this._root_view.removeView(page_view);
      }
    });
  }

  public void removePageView(final ASViewController aPage, Animation aAnimation) {
    aAnimation.setAnimationListener(new Animation.AnimationListener() { // from class: com.kota.ASFramework.PageController.ASNavigationController.3
      @Override // android.view.animation.Animation.AnimationListener
      public void onAnimationStart(Animation animation) {
      }

      @Override // android.view.animation.Animation.AnimationListener
      public void onAnimationRepeat(Animation animation) {
      }

      @Override // android.view.animation.Animation.AnimationListener
      public void onAnimationEnd(Animation animation) {
        ASNavigationController.this.removePageView(aPage);
      }
    });
    View page_view = aPage.getPageView();
    page_view.startAnimation(aAnimation);
  }

  private void animatePopViewController(final ASViewController aRemovePage, final ASViewController aAddPage, boolean animated) {
    if (aRemovePage != null) {
      aRemovePage.notifyPageWillDisappear();
      if (animated) {
        removePageView(aRemovePage, ASAnimation.getFadeOutToRightAnimation());
      } else {
        removePageView(aRemovePage);
      }
    }
    if (aAddPage != null) {
      buildPageView(aAddPage);
      aAddPage.onPageDidLoad();
      aAddPage.onPageRefresh();
      aAddPage.notifyPageWillAppear();
    }
    new ASNavigationControllerPopAnimation(aRemovePage, aAddPage) { // from class: com.kota.ASFramework.PageController.ASNavigationController.4
      @Override // com.kota.ASFramework.PageController.ASNavigationControllerPopAnimation
      public void onAnimationFinished() {
        if (aRemovePage != null) {
          aRemovePage.onPageDidDisappear();
        }
        Vector<View> remove_list = new Vector<>();
        for (int i = 0; i < ASNavigationController.this._root_view.getContentView().getChildCount(); i++) {
          View page_view = ASNavigationController.this._root_view.getContentView().getChildAt(i);
          if (page_view != aAddPage.getPageView()) {
            remove_list.add(page_view);
          }
        }
        Iterator<View> it = remove_list.iterator();
        while (it.hasNext()) {
          View view = it.next();
          ASNavigationController.this._root_view.getContentView().removeView(view);
        }
        ASNavigationController.this.cleanPageView(aRemovePage);
        Iterator it2 = ASNavigationController.this._controllers.iterator();
        while (it2.hasNext()) {
          ASViewController controller = (ASViewController) it2.next();
          if (controller != aAddPage && controller.getPageView() != null) {
            ASNavigationController.this.cleanPageView(controller);
          }
        }
        if (aAddPage != null) {
          aAddPage.notifyPageDidAppear();
        }
        ASNavigationController.this.onPageCommandExecuteFinished();
      }
    }.start(animated);
  }

  private void animatedPushViewController(final ASViewController sourceController, final ASViewController targetController, boolean animated) {
    if (targetController != null) {
      buildPageView(targetController);
      targetController.onPageDidLoad();
      targetController.onPageRefresh();
    }
    if (sourceController != null) {
      sourceController.notifyPageWillDisappear();
    }
    if (targetController != null) {
      targetController.notifyPageWillAppear();
    }
    new ASNavigationControllerPushAnimation(sourceController, targetController) { // from class: com.kota.ASFramework.PageController.ASNavigationController.5
      @Override // com.kota.ASFramework.PageController.ASNavigationControllerPushAnimation
      public void onAnimationFinished() {
        if (sourceController != null) {
          sourceController.onPageDidDisappear();
        }
        if (targetController != null) {
          Vector<View> remove_list = new Vector<>();
          for (int i = 0; i < ASNavigationController.this._root_view.getContentView().getChildCount(); i++) {
            View page_view = ASNavigationController.this._root_view.getContentView().getChildAt(i);
            if (page_view != targetController.getPageView()) {
              remove_list.add(page_view);
            }
          }
          Iterator<View> it = remove_list.iterator();
          while (it.hasNext()) {
            View view = it.next();
            ASNavigationController.this._root_view.getContentView().removeView(view);
          }
        }
        Iterator it2 = ASNavigationController.this._controllers.iterator();
        while (it2.hasNext()) {
          ASViewController controller = (ASViewController) it2.next();
          if (controller != targetController && controller.getPageView() != null) {
            ASNavigationController.this.cleanPageView(controller);
          }
        }
        if (targetController != null) {
          targetController.notifyPageDidAppear();
        }
        ASNavigationController.this.onPageCommandExecuteFinished();
      }
    }.start(animated);
  }

  public void pushViewController(ASViewController aController) {
    pushViewController(aController, this._animation_enable);
  }

  public void pushViewController(final ASViewController aController, boolean animated) {
    PageCommand command = new PageCommand() { // from class: com.kota.ASFramework.PageController.ASNavigationController.6


      @Override // com.kota.ASFramework.PageController.ASNavigationController.PageCommand
      public void run() {
        if (aController != null) {
          if (ASNavigationController.this._temp_controllers.size() <= 0 || aController != ASNavigationController.this._temp_controllers.lastElement()) {
            ASNavigationController.this._temp_controllers.add(aController);
          }
        }
      }

    };
    command.animated = animated;
    pushPageCommand(command);
  }

  public void popViewController() {
    popViewController(this._animation_enable);
  }

  public void popViewController(boolean animated) {
    PageCommand command = new PageCommand() { // from class: com.kota.ASFramework.PageController.ASNavigationController.7
      @Override // com.kota.ASFramework.PageController.ASNavigationController.PageCommand
      public void run() {
        if (ASNavigationController.this._temp_controllers.size() > 0) {
          ASNavigationController.this._temp_controllers.remove(ASNavigationController.this._temp_controllers.size() - 1);
        }
      }
    };
    command.animated = animated;
    pushPageCommand(command);
  }

  public void popToViewController(ASViewController aController) {
    popToViewController(aController, this._animation_enable);
  }

  public void popToViewController(final ASViewController aController, boolean animated) {

    PageCommand command = new PageCommand() { // from class: com.kota.ASFramework.PageController.ASNavigationController.8


      @Override // com.kota.ASFramework.PageController.ASNavigationController.PageCommand
      public void run() {
        if (aController != null) {
          if (ASNavigationController.this._temp_controllers.size() <= 0 || aController != ASNavigationController.this._temp_controllers.lastElement()) {
            while (ASNavigationController.this._temp_controllers.size() > 0 && ASNavigationController.this._temp_controllers.lastElement() != aController) {
              ASNavigationController.this._temp_controllers.remove(ASNavigationController.this._temp_controllers.size() - 1);
            }
          }
        }
      }
    };
    command.animated = animated;
    pushPageCommand(command);
  }

  public void setViewControllers(Vector<ASViewController> aControllerList) {
    setViewControllers(aControllerList, this._animation_enable);
  }

  public void setViewControllers(final Vector<ASViewController> aControllerList, boolean animated) {
    PageCommand command = new PageCommand() { // from class: com.kota.ASFramework.PageController.ASNavigationController.9

      @Override // com.kota.ASFramework.PageController.ASNavigationController.PageCommand
      public void run() {
        if (aControllerList != null) {
          ASNavigationController.this._temp_controllers.removeAllElements();
          ASNavigationController.this._temp_controllers.addAll(aControllerList);
        }
      }
    };
    command.animated = animated;
    pushPageCommand(command);
  }

  public void exchangeViewControllers(final boolean animated) {
    new ASRunner() { // from class: com.kota.ASFramework.PageController.ASNavigationController.10
      @Override // com.kota.ASFramework.Thread.ASRunner
      public void run() {
        ASViewController source_controller = ASNavigationController.this._controllers.size() > 0 ? (ASViewController) ASNavigationController.this._controllers.lastElement() : null;
        ASViewController target_controller = ASNavigationController.this._temp_controllers.size() > 0 ? (ASViewController) ASNavigationController.this._temp_controllers.lastElement() : null;
        boolean pop = ASNavigationController.this._controllers.contains(target_controller);
        Iterator it = ASNavigationController.this._controllers.iterator();
        while (it.hasNext()) {
          ASViewController controller = (ASViewController) it.next();
          controller.prepareForRemove();
        }
        Iterator it2 = ASNavigationController.this._temp_controllers.iterator();
        while (it2.hasNext()) {
          ASViewController controller2 = (ASViewController) it2.next();
          controller2.prepareForAdd();
        }
        Iterator it3 = ASNavigationController.this._controllers.iterator();
        while (it3.hasNext()) {
          ASViewController controller3 = (ASViewController) it3.next();
          if (controller3.isMarkedRemoved()) {
            ASNavigationController.this._remove_list.add(controller3);
          }
          controller3.cleanMark();
        }
        Iterator it4 = ASNavigationController.this._temp_controllers.iterator();
        while (it4.hasNext()) {
          ASViewController controller4 = (ASViewController) it4.next();
          if (controller4.isMarkedAdded()) {
            controller4.setNavigationController(ASNavigationController.this);
            ASNavigationController.this._add_list.add(controller4);
          }
          controller4.cleanMark();
        }
        ASNavigationController.this._controllers.removeAllElements();
        ASNavigationController.this._controllers.addAll(ASNavigationController.this._temp_controllers);
        Iterator it5 = ASNavigationController.this._add_list.iterator();
        while (it5.hasNext()) {
          ASViewController controller5 = (ASViewController) it5.next();
          controller5.notifyPageDidAddToNavigationController();
        }
        Iterator it6 = ASNavigationController.this._remove_list.iterator();
        while (it6.hasNext()) {
          ASViewController controller6 = (ASViewController) it6.next();
          controller6.notifyPageDidRemoveFromNavigationController();
        }
        ASNavigationController.this._add_list.clear();
        ASNavigationController.this._remove_list.clear();
        if (source_controller == target_controller) {
          ASNavigationController.this.onPageCommandExecuteFinished();
        } else if (pop) {
          ASNavigationController.this.animatePopViewController(source_controller, target_controller, animated);
        } else {
          ASNavigationController.this.animatedPushViewController(source_controller, target_controller, animated);
        }
      }
    }.runInMainThread();
  }

  public Vector<ASViewController> getViewControllers() {
    return new Vector<>(this._controllers);
  }

  public boolean containsViewController(ASViewController aController) {
    return this._controllers.contains(aController);
  }

  protected void onSizeChanged(int newWidth, int newHeight, int oldWidth, int oldHeight) {
    ASViewController page = getTopController();
    if (page != null) {
      page.onSizeChanged(newWidth, newHeight, oldWidth, oldHeight);
    }
  }

  public void reloadLayout() {
    if (this._root_view != null) {
      this._root_view.requestLayout();
    }
  }

  public boolean onReceivedGestureUp() {
    ASViewController page = getTopController();
    if (page != null) {
      return page.onReceivedGestureUp();
    }
    return false;
  }

  public boolean onReceivedGestureDown() {
    ASViewController page = getTopController();
    if (page != null) {
      return page.onReceivedGestureDown();
    }
    return false;
  }

  public boolean onReceivedGestureLeft() {
    ASViewController page = getTopController();
    if (page != null) {
      return page.onReceivedGestureLeft();
    }
    return false;
  }

  public boolean onReceivedGestureRight() {
    ASViewController page = getTopController();
    if (page != null) {
      return page.onReceivedGestureRight();
    }
    return false;
  }

  public int getCurrentOrientation() {
    int rotation = getWindowManager().getDefaultDisplay().getRotation();
    if (rotation != 1 && rotation != 3) {
      return 1;
    }
    return 2;
  }

  public int getScreenWidth() {
    return this._display_metrics.widthPixels;
  }

  public int getScreenHeight() {
    return this._display_metrics.heightPixels;
  }

  public ASDeviceController getDeviceController() {
    return this._device_controller;
  }

  @Override // android.app.Activity
  public void finish() {
    onControllerWillFinish();
    getDeviceController().unlockWifi();
    getDeviceController().unlockWake();
    super.finish();
  }

  public boolean isAnimationEnable() {
    return this._animation_enable;
  }

  public void setAnimationEnable(boolean enable) {
    this._animation_enable = enable;
  }

  @Override // android.app.Activity
  protected void onPause() {
    super.onPause();
    this._in_background = true;
  }

  @Override // android.app.Activity
  protected void onResume() {
    super.onResume();
    this._in_background = false;
  }

  public boolean isInBackground() {
    return this._in_background;
  }

  public void printControllers() {
    printControllers(this._controllers);
  }

  public void printControllers(Vector<ASViewController> controllers) {
    Iterator<ASViewController> it = controllers.iterator();
    while (it.hasNext()) {
      ASViewController controller = it.next();
      System.out.print(controller.getPageType() + " ");
    }
    System.out.print("\n");
  }

  @Override // android.app.Activity, android.content.ComponentCallbacks
  public void onLowMemory() {
    System.out.println("on low memory");
    super.onLowMemory();
  }

  public void pushPageCommand(PageCommand aCommand) {
    synchronized (this._page_commands) {
      this._page_commands.add(aCommand);
    }
    executePageCommand();
  }

  public void executePageCommand() {
    synchronized (this) {
      if (!this._is_animating) {
        synchronized (this._page_commands) {
          if (this._page_commands.size() > 0) {
            synchronized (this) {
              this._is_animating = true;
            }
            this._temp_controllers.addAll(this._controllers);
            boolean animated = this._page_commands.firstElement().animated;
            while (this._page_commands.size() > 0 && this._page_commands.firstElement().animated == animated) {
              this._page_commands.remove(0).run();
            }
            exchangeViewControllers(animated);
          }
        }
      }
    }
  }

  private void onPageCommandExecuteFinished() {
    synchronized (this) {
      this._is_animating = false;
    }
    this._temp_controllers.clear();
    executePageCommand();
  }
}
