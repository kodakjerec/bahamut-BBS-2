package com.kumi.ASFramework.PageController;

import android.app.Activity;
import android.content.Context;
import android.os.Bundle;
import android.util.DisplayMetrics;
import android.view.KeyEvent;
import android.view.View;
import android.view.ViewGroup;
import android.view.animation.Animation;
import android.widget.FrameLayout;
import com.kumi.ASFramework.Thread.ASRunner;
import java.util.Iterator;
import java.util.Vector;

public class ASNavigationController extends Activity {
  private static ASNavigationController _current_controller = null;
  
  private Vector<ASViewController> _add_list = new Vector<ASViewController>();
  
  private boolean _animation_enable = true;
  
  private Vector<ASViewController> _controllers = new Vector<ASViewController>();
  
  private ASDeviceController _device_controller = null;
  
  private DisplayMetrics _display_metrics = new DisplayMetrics();
  
  private boolean _in_background = false;
  
  private boolean _is_animating = false;
  
  private Vector<PageCommand> _page_commands = new Vector<PageCommand>();
  
  private Vector<ASViewController> _remove_list = new Vector<ASViewController>();
  
  private ASNavigationControllerView _root_view = null;
  
  private Vector<ASViewController> _temp_controllers = new Vector<ASViewController>();
  
  private void animatePopViewController(final ASViewController aRemovePage, final ASViewController aAddPage, boolean paramBoolean) {
    if (aRemovePage != null) {
      aRemovePage.notifyPageWillDisappear();
      if (paramBoolean) {
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
    (new ASNavigationControllerPopAnimation(aRemovePage, aAddPage) {
        final ASNavigationController this$0;
        
        final ASViewController val$aAddPage;
        
        final ASViewController val$aRemovePage;
        
        public void onAnimationFinished() {
          if (aRemovePage != null)
            aRemovePage.onPageDidDisappear(); 
          Vector<View> vector = new Vector();
          for (byte b = 0; b < ASNavigationController.this._root_view.getContentView().getChildCount(); b++) {
            View view = ASNavigationController.this._root_view.getContentView().getChildAt(b);
            if (view != aAddPage.getPageView())
              vector.add(view); 
          } 
          for (View view : vector)
            ASNavigationController.this._root_view.getContentView().removeView(view); 
          ASNavigationController.this.cleanPageView(aRemovePage);
          for (ASViewController aSViewController : ASNavigationController.this._controllers) {
            if (aSViewController != aAddPage && aSViewController.getPageView() != null)
              ASNavigationController.this.cleanPageView(aSViewController); 
          } 
          if (aAddPage != null)
            aAddPage.notifyPageDidAppear(); 
          ASNavigationController.this.onPageCommandExecuteFinished();
        }
      }).start(paramBoolean);
  }
  
  private void animatedPushViewController(final ASViewController sourceController, final ASViewController targetController, boolean paramBoolean) {
    if (targetController != null) {
      buildPageView(targetController);
      targetController.onPageDidLoad();
      targetController.onPageRefresh();
    } 
    if (sourceController != null)
      sourceController.notifyPageWillDisappear(); 
    if (targetController != null)
      targetController.notifyPageWillAppear(); 
    (new ASNavigationControllerPushAnimation(sourceController, targetController) {
        final ASNavigationController this$0;
        
        final ASViewController val$sourceController;
        
        final ASViewController val$targetController;
        
        public void onAnimationFinished() {
          if (sourceController != null)
            sourceController.onPageDidDisappear(); 
          if (targetController != null) {
            Vector<View> vector = new Vector();
            for (byte b = 0; b < ASNavigationController.this._root_view.getContentView().getChildCount(); b++) {
              View view = ASNavigationController.this._root_view.getContentView().getChildAt(b);
              if (view != targetController.getPageView())
                vector.add(view); 
            } 
            for (View view : vector)
              ASNavigationController.this._root_view.getContentView().removeView(view); 
          } 
          for (ASViewController aSViewController : ASNavigationController.this._controllers) {
            if (aSViewController != targetController && aSViewController.getPageView() != null)
              ASNavigationController.this.cleanPageView(aSViewController); 
          } 
          if (targetController != null)
            targetController.notifyPageDidAppear(); 
          ASNavigationController.this.onPageCommandExecuteFinished();
        }
      }).start(paramBoolean);
  }
  
  private void buildPageView(ASViewController paramASViewController) {
    ASPageView aSPageView = new ASPageView((Context)this);
    aSPageView.setLayoutParams((ViewGroup.LayoutParams)new FrameLayout.LayoutParams(-1, -1));
    aSPageView.setBackgroundColor(-16777216);
    aSPageView.setAnimationCacheEnabled(false);
    getLayoutInflater().inflate(paramASViewController.getPageLayout(), (ViewGroup)aSPageView);
    paramASViewController.setPageView(aSPageView);
    this._root_view.getContentView().addView((View)aSPageView);
  }
  
  private void cleanPageView(ASViewController paramASViewController) {
    paramASViewController.setPageView(null);
  }
  
  public static ASNavigationController getCurrentController() {
    return _current_controller;
  }
  
  private boolean onMenuPressed() {
    boolean bool2 = false;
    ASViewController aSViewController = getTopController();
    boolean bool1 = bool2;
    if (aSViewController != null) {
      bool1 = bool2;
      if (aSViewController.onMenuButtonClicked())
        bool1 = true; 
    } 
    return bool1;
  }
  
  private void onPageCommandExecuteFinished() {
    // Byte code:
    //   0: aload_0
    //   1: monitorenter
    //   2: aload_0
    //   3: iconst_0
    //   4: putfield _is_animating : Z
    //   7: aload_0
    //   8: monitorexit
    //   9: aload_0
    //   10: getfield _temp_controllers : Ljava/util/Vector;
    //   13: invokevirtual clear : ()V
    //   16: aload_0
    //   17: invokevirtual executePageCommand : ()V
    //   20: return
    //   21: astore_1
    //   22: aload_0
    //   23: monitorexit
    //   24: aload_1
    //   25: athrow
    // Exception table:
    //   from	to	target	type
    //   2	9	21	finally
    //   22	24	21	finally
  }
  
  private boolean onSearchPressed() {
    boolean bool2 = false;
    ASViewController aSViewController = getTopController();
    boolean bool1 = bool2;
    if (aSViewController != null) {
      bool1 = bool2;
      if (aSViewController.onSearchButtonClicked())
        bool1 = true; 
    } 
    return bool1;
  }
  
  private static void setNavigationController(ASNavigationController paramASNavigationController) {
    _current_controller = paramASNavigationController;
  }
  
  public void addPageView(final ASViewController aPage) {
    final ASPageView page_view = aPage.getPageView();
    this._root_view.post(new Runnable() {
          final ASNavigationController this$0;
          
          final ASViewController val$aPage;
          
          final View val$page_view;
          
          public void run() {
            aPage.onPageDidDisappear();
            ASNavigationController.this._root_view.removeView(page_view);
          }
        });
  }
  
  public boolean containsViewController(ASViewController paramASViewController) {
    return this._controllers.contains(paramASViewController);
  }
  
  public void exchangeViewControllers(final boolean animated) {
    (new ASRunner() {
        final ASNavigationController this$0;
        
        final boolean val$animated;
        
        public void run() {
          ASViewController aSViewController1;
          ASViewController aSViewController2;
          if (ASNavigationController.this._controllers.size() > 0) {
            aSViewController1 = ASNavigationController.this._controllers.lastElement();
          } else {
            aSViewController1 = null;
          } 
          if (ASNavigationController.this._temp_controllers.size() > 0) {
            aSViewController2 = ASNavigationController.this._temp_controllers.lastElement();
          } else {
            aSViewController2 = null;
          } 
          boolean bool = ASNavigationController.this._controllers.contains(aSViewController2);
          Iterator<ASViewController> iterator = ASNavigationController.this._controllers.iterator();
          while (iterator.hasNext())
            ((ASViewController)iterator.next()).prepareForRemove(); 
          iterator = ASNavigationController.this._temp_controllers.iterator();
          while (iterator.hasNext())
            ((ASViewController)iterator.next()).prepareForAdd(); 
          for (ASViewController aSViewController : ASNavigationController.this._controllers) {
            if (aSViewController.isMarkedRemoved())
              ASNavigationController.this._remove_list.add(aSViewController); 
            aSViewController.cleanMark();
          } 
          for (ASViewController aSViewController : ASNavigationController.this._temp_controllers) {
            if (aSViewController.isMarkedAdded()) {
              aSViewController.setNavigationController(ASNavigationController.this);
              ASNavigationController.this._add_list.add(aSViewController);
            } 
            aSViewController.cleanMark();
          } 
          ASNavigationController.this._controllers.removeAllElements();
          ASNavigationController.this._controllers.addAll(ASNavigationController.this._temp_controllers);
          iterator = ASNavigationController.this._add_list.iterator();
          while (iterator.hasNext())
            ((ASViewController)iterator.next()).notifyPageDidAddToNavigationController(); 
          iterator = ASNavigationController.this._remove_list.iterator();
          while (iterator.hasNext())
            ((ASViewController)iterator.next()).notifyPageDidRemoveFromNavigationController(); 
          ASNavigationController.this._add_list.clear();
          ASNavigationController.this._remove_list.clear();
          if (aSViewController1 != aSViewController2) {
            if (bool) {
              ASNavigationController.this.animatePopViewController(aSViewController1, aSViewController2, animated);
              return;
            } 
            ASNavigationController.this.animatedPushViewController(aSViewController1, aSViewController2, animated);
            return;
          } 
          ASNavigationController.this.onPageCommandExecuteFinished();
        }
      }).runInMainThread();
  }
  
  public void executePageCommand() {
    // Byte code:
    //   0: aload_0
    //   1: monitorenter
    //   2: aload_0
    //   3: getfield _is_animating : Z
    //   6: ifeq -> 12
    //   9: aload_0
    //   10: monitorexit
    //   11: return
    //   12: aload_0
    //   13: monitorexit
    //   14: aload_0
    //   15: getfield _page_commands : Ljava/util/Vector;
    //   18: astore_2
    //   19: aload_2
    //   20: monitorenter
    //   21: aload_0
    //   22: getfield _page_commands : Ljava/util/Vector;
    //   25: invokevirtual size : ()I
    //   28: ifle -> 98
    //   31: aload_0
    //   32: monitorenter
    //   33: aload_0
    //   34: iconst_1
    //   35: putfield _is_animating : Z
    //   38: aload_0
    //   39: monitorexit
    //   40: aload_0
    //   41: getfield _temp_controllers : Ljava/util/Vector;
    //   44: aload_0
    //   45: getfield _controllers : Ljava/util/Vector;
    //   48: invokevirtual addAll : (Ljava/util/Collection;)Z
    //   51: pop
    //   52: aload_0
    //   53: getfield _page_commands : Ljava/util/Vector;
    //   56: invokevirtual firstElement : ()Ljava/lang/Object;
    //   59: checkcast com/kumi/ASFramework/PageController/ASNavigationController$PageCommand
    //   62: getfield animated : Z
    //   65: istore_1
    //   66: aload_0
    //   67: getfield _page_commands : Ljava/util/Vector;
    //   70: invokevirtual size : ()I
    //   73: ifle -> 93
    //   76: aload_0
    //   77: getfield _page_commands : Ljava/util/Vector;
    //   80: invokevirtual firstElement : ()Ljava/lang/Object;
    //   83: checkcast com/kumi/ASFramework/PageController/ASNavigationController$PageCommand
    //   86: getfield animated : Z
    //   89: iload_1
    //   90: if_icmpeq -> 118
    //   93: aload_0
    //   94: iload_1
    //   95: invokevirtual exchangeViewControllers : (Z)V
    //   98: aload_2
    //   99: monitorexit
    //   100: goto -> 11
    //   103: astore_3
    //   104: aload_2
    //   105: monitorexit
    //   106: aload_3
    //   107: athrow
    //   108: astore_2
    //   109: aload_0
    //   110: monitorexit
    //   111: aload_2
    //   112: athrow
    //   113: astore_3
    //   114: aload_0
    //   115: monitorexit
    //   116: aload_3
    //   117: athrow
    //   118: aload_0
    //   119: getfield _page_commands : Ljava/util/Vector;
    //   122: iconst_0
    //   123: invokevirtual remove : (I)Ljava/lang/Object;
    //   126: checkcast com/kumi/ASFramework/PageController/ASNavigationController$PageCommand
    //   129: invokevirtual run : ()V
    //   132: goto -> 66
    // Exception table:
    //   from	to	target	type
    //   2	11	108	finally
    //   12	14	108	finally
    //   21	33	103	finally
    //   33	40	113	finally
    //   40	66	103	finally
    //   66	93	103	finally
    //   93	98	103	finally
    //   98	100	103	finally
    //   104	106	103	finally
    //   109	111	108	finally
    //   114	116	113	finally
    //   116	118	103	finally
    //   118	132	103	finally
  }
  
  public void finish() {
    onControllerWillFinish();
    getDeviceController().unlockWifi();
    super.finish();
  }
  
  protected String getControllerName() {
    return "";
  }
  
  public int getCurrentOrientation() {
    byte b = 1;
    int i = getWindowManager().getDefaultDisplay().getRotation();
    if (i == 1 || i == 3)
      b = 2; 
    return b;
  }
  
  public ASDeviceController getDeviceController() {
    return this._device_controller;
  }
  
  public int getScreenHeight() {
    return this._display_metrics.heightPixels;
  }
  
  public int getScreenWidth() {
    return this._display_metrics.widthPixels;
  }
  
  public ASViewController getTopController() {
    null = null;
    synchronized (this._controllers) {
      if (this._controllers.size() > 0)
        null = this._controllers.lastElement(); 
      return null;
    } 
  }
  
  public Vector<ASViewController> getViewControllers() {
    return new Vector<ASViewController>(this._controllers);
  }
  
  public boolean isAnimationEnable() {
    return this._animation_enable;
  }
  
  public boolean isInBackground() {
    return this._in_background;
  }
  
  public boolean onBackLongPressed() {
    return false;
  }
  
  public void onBackPressed() {
    boolean bool2 = false;
    ASViewController aSViewController = getTopController();
    boolean bool1 = bool2;
    if (aSViewController != null) {
      bool1 = bool2;
      if (aSViewController.onBackPressed())
        bool1 = true; 
    } 
    if (!bool1)
      finish(); 
  }
  
  protected void onControllerDidLoad() {}
  
  protected void onControllerWillFinish() {}
  
  protected void onControllerWillLoad() {}
  
  public void onCreate(Bundle paramBundle) {
    super.onCreate(paramBundle);
    setNavigationController(this);
    getWindowManager().getDefaultDisplay().getMetrics(this._display_metrics);
    ASRunner.construct();
    this._device_controller = new ASDeviceController((Context)this);
    onControllerWillLoad();
    this._root_view = new ASNavigationControllerView((Context)this);
    this._root_view.setPageController(this);
    setContentView((View)this._root_view);
    onControllerDidLoad();
  }
  
  public boolean onKeyLongPress(int paramInt, KeyEvent paramKeyEvent) {
    return (paramInt == 4) ? onBackLongPressed() : super.onKeyLongPress(paramInt, paramKeyEvent);
  }
  
  public boolean onKeyUp(int paramInt, KeyEvent paramKeyEvent) {
    switch (paramInt) {
      default:
        return super.onKeyUp(paramInt, paramKeyEvent);
      case 84:
        return onSearchPressed();
      case 82:
        break;
    } 
    return onMenuPressed();
  }
  
  public void onLowMemory() {
    System.out.println("on low memory");
    super.onLowMemory();
  }
  
  protected void onPause() {
    super.onPause();
    this._in_background = true;
  }
  
  public boolean onReceivedGestureDown() {
    ASViewController aSViewController = getTopController();
    return (aSViewController != null) ? aSViewController.onReceivedGestureDown() : false;
  }
  
  public boolean onReceivedGestureLeft() {
    ASViewController aSViewController = getTopController();
    return (aSViewController != null) ? aSViewController.onReceivedGestureLeft() : false;
  }
  
  public boolean onReceivedGestureRight() {
    ASViewController aSViewController = getTopController();
    return (aSViewController != null) ? aSViewController.onReceivedGestureRight() : false;
  }
  
  public boolean onReceivedGestureUp() {
    ASViewController aSViewController = getTopController();
    return (aSViewController != null) ? aSViewController.onReceivedGestureUp() : false;
  }
  
  protected void onResume() {
    super.onResume();
    this._in_background = false;
  }
  
  protected void onSizeChanged(int paramInt1, int paramInt2, int paramInt3, int paramInt4) {
    ASViewController aSViewController = getTopController();
    if (aSViewController != null)
      aSViewController.onSizeChanged(paramInt1, paramInt2, paramInt3, paramInt4); 
  }
  
  public void popToViewController(ASViewController paramASViewController) {
    popToViewController(paramASViewController, this._animation_enable);
  }
  
  public void popToViewController(final ASViewController aController, boolean paramBoolean) {
    PageCommand pageCommand = new PageCommand() {
        final ASNavigationController this$0;
        
        final ASViewController val$aController;
        
        public void run() {
          if (aController != null && (ASNavigationController.this._temp_controllers.size() <= 0 || aController != ASNavigationController.this._temp_controllers.lastElement()))
            while (ASNavigationController.this._temp_controllers.size() > 0 && ASNavigationController.this._temp_controllers.lastElement() != aController)
              ASNavigationController.this._temp_controllers.remove(ASNavigationController.this._temp_controllers.size() - 1);  
        }
      };
    pageCommand.animated = paramBoolean;
    pushPageCommand(pageCommand);
  }
  
  public void popViewController() {
    popViewController(this._animation_enable);
  }
  
  public void popViewController(boolean paramBoolean) {
    PageCommand pageCommand = new PageCommand() {
        final ASNavigationController this$0;
        
        public void run() {
          if (ASNavigationController.this._temp_controllers.size() > 0)
            ASNavigationController.this._temp_controllers.remove(ASNavigationController.this._temp_controllers.size() - 1); 
        }
      };
    pageCommand.animated = paramBoolean;
    pushPageCommand(pageCommand);
  }
  
  public void printControllers() {
    printControllers(this._controllers);
  }
  
  public void printControllers(Vector<ASViewController> paramVector) {
    for (ASViewController aSViewController : paramVector)
      System.out.print(aSViewController.getPageType() + " "); 
    System.out.print("\n");
  }
  
  public void pushPageCommand(PageCommand paramPageCommand) {
    synchronized (this._page_commands) {
      this._page_commands.add(paramPageCommand);
      executePageCommand();
      return;
    } 
  }
  
  public void pushViewController(ASViewController paramASViewController) {
    pushViewController(paramASViewController, this._animation_enable);
  }
  
  public void pushViewController(final ASViewController aController, boolean paramBoolean) {
    PageCommand pageCommand = new PageCommand() {
        final ASNavigationController this$0;
        
        final ASViewController val$aController;
        
        public void run() {
          if (aController != null && (ASNavigationController.this._temp_controllers.size() <= 0 || aController != ASNavigationController.this._temp_controllers.lastElement()))
            ASNavigationController.this._temp_controllers.add(aController); 
        }
      };
    pageCommand.animated = paramBoolean;
    pushPageCommand(pageCommand);
  }
  
  public void reloadLayout() {
    if (this._root_view != null)
      this._root_view.requestLayout(); 
  }
  
  public void removePageView(final ASViewController aPage) {
    final ASPageView page_view = aPage.getPageView();
    this._root_view.post(new Runnable() {
          final ASNavigationController this$0;
          
          final ASViewController val$aPage;
          
          final View val$page_view;
          
          public void run() {
            aPage.onPageDidDisappear();
            ASNavigationController.this._root_view.removeView(page_view);
          }
        });
  }
  
  public void removePageView(final ASViewController aPage, Animation paramAnimation) {
    paramAnimation.setAnimationListener(new Animation.AnimationListener() {
          final ASNavigationController this$0;
          
          final ASViewController val$aPage;
          
          public void onAnimationEnd(Animation param1Animation) {
            ASNavigationController.this.removePageView(aPage);
          }
          
          public void onAnimationRepeat(Animation param1Animation) {}
          
          public void onAnimationStart(Animation param1Animation) {}
        });
    aPage.getPageView().startAnimation(paramAnimation);
  }
  
  public void setAnimationEnable(boolean paramBoolean) {
    this._animation_enable = paramBoolean;
  }
  
  public void setNavigationTitle(String paramString) {
    setTitle(paramString);
  }
  
  public void setViewControllers(Vector<ASViewController> paramVector) {
    setViewControllers(paramVector, this._animation_enable);
  }
  
  public void setViewControllers(final Vector<ASViewController> aControllerList, boolean paramBoolean) {
    PageCommand pageCommand = new PageCommand() {
        final ASNavigationController this$0;
        
        final Vector val$aControllerList;
        
        public void run() {
          if (aControllerList != null) {
            ASNavigationController.this._temp_controllers.removeAllElements();
            ASNavigationController.this._temp_controllers.addAll(aControllerList);
          } 
        }
      };
    pageCommand.animated = paramBoolean;
    pushPageCommand(pageCommand);
  }
  
  private abstract class PageCommand {
    public boolean animated = true;
    
    final ASNavigationController this$0;
    
    private PageCommand() {}
    
    public abstract void run();
  }
}


/* Location:              C:\Users\kodak\Downloads\反編譯\dex-tools-v2.4\classes-dex2jar.jar!\com\kumi\ASFramework\PageController\ASNavigationController.class
 * Java compiler version: 6 (50.0)
 * JD-Core Version:       1.1.3
 */