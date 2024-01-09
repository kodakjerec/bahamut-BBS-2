package com.kota.ASFramework.PageController;

import android.os.Bundle;
import android.util.DisplayMetrics;
import android.view.KeyEvent;
import android.view.View;
import android.view.animation.Animation;
import android.widget.FrameLayout;

import androidx.appcompat.app.AppCompatActivity;
import androidx.core.view.ViewCompat;

import com.kota.ASFramework.Thread.ASRunner;

import java.util.Iterator;
import java.util.Vector;

public class ASNavigationController extends AppCompatActivity {
    private static ASNavigationController _current_controller = null;
    /* access modifiers changed from: private */
    public Vector<ASViewController> _add_list = new Vector<>();
    private boolean _animation_enable = true;
    /* access modifiers changed from: private */
    public Vector<ASViewController> _controllers = new Vector<>();
    private ASDeviceController _device_controller = null;
    private DisplayMetrics _display_metrics = new DisplayMetrics();
    private boolean _in_background = false;
    private boolean _is_animating = false;
    private Vector<PageCommand> _page_commands = new Vector<>();
    /* access modifiers changed from: private */
    public Vector<ASViewController> _remove_list = new Vector<>();
    /* access modifiers changed from: private */
    public ASNavigationControllerView _root_view = null;
    /* access modifiers changed from: private */
    public Vector<ASViewController> _temp_controllers = new Vector<>();

    private abstract class PageCommand {
        public boolean animated;

        public abstract void run();

        private PageCommand() {
            this.animated = true;
        }
    }

    /* access modifiers changed from: protected */
    public void onControllerWillLoad() {
    }

    /* access modifiers changed from: protected */
    public void onControllerDidLoad() {
    }

    /* access modifiers changed from: protected */
    public void onControllerWillFinish() {
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

    /* access modifiers changed from: protected */
    public String getControllerName() {
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

    public boolean onKeyLongPress(int keyCode, KeyEvent event) {
        if (keyCode == 4) {
            return onBackLongPressed();
        }
        return super.onKeyLongPress(keyCode, event);
    }

    public boolean onKeyUp(int keyCode, KeyEvent event) {
        switch (keyCode) {
            case 82:
                return onMenuPressed();
            case 84:
                return onSearchPressed();
            default:
                return super.onKeyUp(keyCode, event);
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
        page_view.setBackgroundColor(ViewCompat.MEASURED_STATE_MASK);
        page_view.setAnimationCacheEnabled(false);
        getLayoutInflater().inflate(controller.getPageLayout(), page_view);
        controller.setPageView(page_view);
        this._root_view.getContentView().addView(page_view);
    }

    /* access modifiers changed from: private */
    public void cleanPageView(ASViewController controller) {
        controller.setPageView((ASPageView) null);
    }

    public void addPageView(final ASViewController aPage) {
        final View page_view = aPage.getPageView();
        this._root_view.post(new Runnable() {
            public void run() {
                aPage.onPageDidDisappear();
                ASNavigationController.this._root_view.removeView(page_view);
            }
        });
    }

    public void removePageView(final ASViewController aPage) {
        final View page_view = aPage.getPageView();
        this._root_view.post(new Runnable() {
            public void run() {
                aPage.onPageDidDisappear();
                ASNavigationController.this._root_view.removeView(page_view);
            }
        });
    }

    public void removePageView(final ASViewController aPage, Animation aAnimation) {
        aAnimation.setAnimationListener(new Animation.AnimationListener() {
            public void onAnimationStart(Animation animation) {
            }

            public void onAnimationRepeat(Animation animation) {
            }

            public void onAnimationEnd(Animation animation) {
                ASNavigationController.this.removePageView(aPage);
            }
        });
        aPage.getPageView().startAnimation(aAnimation);
    }

    /* access modifiers changed from: private */
    public void animatePopViewController(ASViewController aRemovePage, ASViewController aAddPage, boolean animated) {
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
        final ASViewController aSViewController = aRemovePage;
        final ASViewController aSViewController2 = aAddPage;
        new ASNavigationControllerPopAnimation(aRemovePage, aAddPage) {
            public void onAnimationFinished() {
                if (aSViewController != null) {
                    aSViewController.onPageDidDisappear();
                }
                Vector<View> remove_list = new Vector<>();
                for (int i = 0; i < ASNavigationController.this._root_view.getContentView().getChildCount(); i++) {
                    View page_view = ASNavigationController.this._root_view.getContentView().getChildAt(i);
                    if (page_view != aSViewController2.getPageView()) {
                        remove_list.add(page_view);
                    }
                }
                Iterator<View> it = remove_list.iterator();
                while (it.hasNext()) {
                    ASNavigationController.this._root_view.getContentView().removeView(it.next());
                }
                ASNavigationController.this.cleanPageView(aSViewController);
                Iterator it2 = ASNavigationController.this._controllers.iterator();
                while (it2.hasNext()) {
                    ASViewController controller = (ASViewController) it2.next();
                    if (!(controller == aSViewController2 || controller.getPageView() == null)) {
                        ASNavigationController.this.cleanPageView(controller);
                    }
                }
                if (aSViewController2 != null) {
                    aSViewController2.notifyPageDidAppear();
                }
                ASNavigationController.this.onPageCommandExecuteFinished();
            }
        }.start(animated);
    }

    /* access modifiers changed from: private */
    public void animatedPushViewController(ASViewController sourceController, ASViewController targetController, boolean animated) {
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
        final ASViewController aSViewController = sourceController;
        final ASViewController aSViewController2 = targetController;
        new ASNavigationControllerPushAnimation(sourceController, targetController) {
            public void onAnimationFinished() {
                if (aSViewController != null) {
                    aSViewController.onPageDidDisappear();
                }
                if (aSViewController2 != null) {
                    Vector<View> remove_list = new Vector<>();
                    for (int i = 0; i < ASNavigationController.this._root_view.getContentView().getChildCount(); i++) {
                        View page_view = ASNavigationController.this._root_view.getContentView().getChildAt(i);
                        if (page_view != aSViewController2.getPageView()) {
                            remove_list.add(page_view);
                        }
                    }
                    Iterator<View> it = remove_list.iterator();
                    while (it.hasNext()) {
                        ASNavigationController.this._root_view.getContentView().removeView(it.next());
                    }
                }
                Iterator it2 = ASNavigationController.this._controllers.iterator();
                while (it2.hasNext()) {
                    ASViewController controller = (ASViewController) it2.next();
                    if (!(controller == aSViewController2 || controller.getPageView() == null)) {
                        ASNavigationController.this.cleanPageView(controller);
                    }
                }
                if (aSViewController2 != null) {
                    aSViewController2.notifyPageDidAppear();
                }
                ASNavigationController.this.onPageCommandExecuteFinished();
            }
        }.start(animated);
    }

    public void pushViewController(ASViewController aController) {
        pushViewController(aController, this._animation_enable);
    }

    public void pushViewController(final ASViewController aController, boolean animated) {
        PageCommand command = new PageCommand() {
            public void run() {
                if (aController == null) {
                    return;
                }
                if (ASNavigationController.this._temp_controllers.size() <= 0 || aController != ASNavigationController.this._temp_controllers.lastElement()) {
                    ASNavigationController.this._temp_controllers.add(aController);
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
        PageCommand command = new PageCommand() {
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
        PageCommand command = new PageCommand() {
            public void run() {
                if (aController == null) {
                    return;
                }
                if (ASNavigationController.this._temp_controllers.size() <= 0 || aController != ASNavigationController.this._temp_controllers.lastElement()) {
                    while (ASNavigationController.this._temp_controllers.size() > 0 && ASNavigationController.this._temp_controllers.lastElement() != aController) {
                        ASNavigationController.this._temp_controllers.remove(ASNavigationController.this._temp_controllers.size() - 1);
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
        PageCommand command = new PageCommand() {
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
        new ASRunner() {
            public void run() {
                ASViewController source_controller;
                ASViewController target_controller;
                if (ASNavigationController.this._controllers.size() > 0) {
                    source_controller = (ASViewController) ASNavigationController.this._controllers.lastElement();
                } else {
                    source_controller = null;
                }
                if (ASNavigationController.this._temp_controllers.size() > 0) {
                    target_controller = (ASViewController) ASNavigationController.this._temp_controllers.lastElement();
                } else {
                    target_controller = null;
                }
                boolean pop = ASNavigationController.this._controllers.contains(target_controller);
                Iterator it = ASNavigationController.this._controllers.iterator();
                while (it.hasNext()) {
                    ((ASViewController) it.next()).prepareForRemove();
                }
                Iterator it2 = ASNavigationController.this._temp_controllers.iterator();
                while (it2.hasNext()) {
                    ((ASViewController) it2.next()).prepareForAdd();
                }
                Iterator it3 = ASNavigationController.this._controllers.iterator();
                while (it3.hasNext()) {
                    ASViewController controller = (ASViewController) it3.next();
                    if (controller.isMarkedRemoved()) {
                        ASNavigationController.this._remove_list.add(controller);
                    }
                    controller.cleanMark();
                }
                Iterator it4 = ASNavigationController.this._temp_controllers.iterator();
                while (it4.hasNext()) {
                    ASViewController controller2 = (ASViewController) it4.next();
                    if (controller2.isMarkedAdded()) {
                        controller2.setNavigationController(ASNavigationController.this);
                        ASNavigationController.this._add_list.add(controller2);
                    }
                    controller2.cleanMark();
                }
                ASNavigationController.this._controllers.removeAllElements();
                ASNavigationController.this._controllers.addAll(ASNavigationController.this._temp_controllers);
                Iterator it5 = ASNavigationController.this._add_list.iterator();
                while (it5.hasNext()) {
                    ((ASViewController) it5.next()).notifyPageDidAddToNavigationController();
                }
                Iterator it6 = ASNavigationController.this._remove_list.iterator();
                while (it6.hasNext()) {
                    ((ASViewController) it6.next()).notifyPageDidRemoveFromNavigationController();
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

    /* access modifiers changed from: protected */
    public void onSizeChanged(int newWidth, int newHeight, int oldWidth, int oldHeight) {
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
        if (rotation == 1 || rotation == 3) {
            return 2;
        }
        return 1;
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

    public void finish() {
        onControllerWillFinish();
        getDeviceController().unlockWifi();
        super.finish();
    }

    public boolean isAnimationEnable() {
        return this._animation_enable;
    }

    public void setAnimationEnable(boolean enable) {
        this._animation_enable = enable;
    }

    /* access modifiers changed from: protected */
    public void onPause() {
        super.onPause();
        this._in_background = true;
    }

    /* access modifiers changed from: protected */
    public void onResume() {
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
            System.out.print(it.next().getPageType() + " ");
        }
        System.out.print("\n");
    }

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

    /* JADX WARNING: Code restructure failed: missing block: B:10:0x0011, code lost:
        if (r4._page_commands.size() <= 0) goto L_0x0040;
     */
    /* JADX WARNING: Code restructure failed: missing block: B:11:0x0013, code lost:
        monitor-enter(r4);
     */
    /* JADX WARNING: Code restructure failed: missing block: B:14:?, code lost:
        r4._is_animating = true;
     */
    /* JADX WARNING: Code restructure failed: missing block: B:15:0x0017, code lost:
        monitor-exit(r4);
     */
    /* JADX WARNING: Code restructure failed: missing block: B:17:?, code lost:
        r4._temp_controllers.addAll(r4._controllers);
        r0 = r4._page_commands.firstElement().animated;
     */
    /* JADX WARNING: Code restructure failed: missing block: B:19:0x002f, code lost:
        if (r4._page_commands.size() <= 0) goto L_0x003d;
     */
    /* JADX WARNING: Code restructure failed: missing block: B:21:0x003b, code lost:
        if (r4._page_commands.firstElement().animated == r0) goto L_0x004b;
     */
    /* JADX WARNING: Code restructure failed: missing block: B:22:0x003d, code lost:
        exchangeViewControllers(r0);
     */
    /* JADX WARNING: Code restructure failed: missing block: B:23:0x0040, code lost:
        monitor-exit(r2);
     */
    /* JADX WARNING: Code restructure failed: missing block: B:37:0x004b, code lost:
        r4._page_commands.remove(0).run();
     */
    /* JADX WARNING: Code restructure failed: missing block: B:46:?, code lost:
        return;
     */
    /* JADX WARNING: Code restructure failed: missing block: B:6:0x0008, code lost:
        r2 = r4._page_commands;
     */
    /* JADX WARNING: Code restructure failed: missing block: B:7:0x000a, code lost:
        monitor-enter(r2);
     */
    /* Code decompiled incorrectly, please refer to instructions dump. */
    public void executePageCommand() {
        /*
            r4 = this;
            monitor-enter(r4)
            boolean r1 = r4._is_animating     // Catch:{ all -> 0x0045 }
            if (r1 == 0) goto L_0x0007
            monitor-exit(r4)     // Catch:{ all -> 0x0045 }
        L_0x0006:
            return
        L_0x0007:
            monitor-exit(r4)     // Catch:{ all -> 0x0045 }
            java.util.Vector<com.kota.ASFramework.PageController.ASNavigationController$PageCommand> r2 = r4._page_commands
            monitor-enter(r2)
            java.util.Vector<com.kota.ASFramework.PageController.ASNavigationController$PageCommand> r1 = r4._page_commands     // Catch:{ all -> 0x0042 }
            int r1 = r1.size()     // Catch:{ all -> 0x0042 }
            if (r1 <= 0) goto L_0x0040
            monitor-enter(r4)     // Catch:{ all -> 0x0042 }
            r1 = 1
            r4._is_animating = r1     // Catch:{ all -> 0x0048 }
            monitor-exit(r4)     // Catch:{ all -> 0x0048 }
            java.util.Vector<com.kota.ASFramework.PageController.ASViewController> r1 = r4._temp_controllers     // Catch:{ all -> 0x0042 }
            java.util.Vector<com.kota.ASFramework.PageController.ASViewController> r3 = r4._controllers     // Catch:{ all -> 0x0042 }
            r1.addAll(r3)     // Catch:{ all -> 0x0042 }
            java.util.Vector<com.kota.ASFramework.PageController.ASNavigationController$PageCommand> r1 = r4._page_commands     // Catch:{ all -> 0x0042 }
            java.lang.Object r1 = r1.firstElement()     // Catch:{ all -> 0x0042 }
            com.kota.ASFramework.PageController.ASNavigationController$PageCommand r1 = (com.kota.ASFramework.PageController.ASNavigationController.PageCommand) r1     // Catch:{ all -> 0x0042 }
            boolean r0 = r1.animated     // Catch:{ all -> 0x0042 }
        L_0x0029:
            java.util.Vector<com.kota.ASFramework.PageController.ASNavigationController$PageCommand> r1 = r4._page_commands     // Catch:{ all -> 0x0042 }
            int r1 = r1.size()     // Catch:{ all -> 0x0042 }
            if (r1 <= 0) goto L_0x003d
            java.util.Vector<com.kota.ASFramework.PageController.ASNavigationController$PageCommand> r1 = r4._page_commands     // Catch:{ all -> 0x0042 }
            java.lang.Object r1 = r1.firstElement()     // Catch:{ all -> 0x0042 }
            com.kota.ASFramework.PageController.ASNavigationController$PageCommand r1 = (com.kota.ASFramework.PageController.ASNavigationController.PageCommand) r1     // Catch:{ all -> 0x0042 }
            boolean r1 = r1.animated     // Catch:{ all -> 0x0042 }
            if (r1 == r0) goto L_0x004b
        L_0x003d:
            r4.exchangeViewControllers(r0)     // Catch:{ all -> 0x0042 }
        L_0x0040:
            monitor-exit(r2)     // Catch:{ all -> 0x0042 }
            goto L_0x0006
        L_0x0042:
            r1 = move-exception
            monitor-exit(r2)     // Catch:{ all -> 0x0042 }
            throw r1
        L_0x0045:
            r1 = move-exception
            monitor-exit(r4)     // Catch:{ all -> 0x0045 }
            throw r1
        L_0x0048:
            r1 = move-exception
            monitor-exit(r4)     // Catch:{ all -> 0x0048 }
            throw r1     // Catch:{ all -> 0x0042 }
        L_0x004b:
            java.util.Vector<com.kota.ASFramework.PageController.ASNavigationController$PageCommand> r1 = r4._page_commands     // Catch:{ all -> 0x0042 }
            r3 = 0
            java.lang.Object r1 = r1.remove(r3)     // Catch:{ all -> 0x0042 }
            com.kota.ASFramework.PageController.ASNavigationController$PageCommand r1 = (com.kota.ASFramework.PageController.ASNavigationController.PageCommand) r1     // Catch:{ all -> 0x0042 }
            r1.run()     // Catch:{ all -> 0x0042 }
            goto L_0x0029
        */
        throw new UnsupportedOperationException("Method not decompiled: com.kota.ASFramework.PageController.ASNavigationController.executePageCommand():void");
    }

    /* access modifiers changed from: private */
    public void onPageCommandExecuteFinished() {
        synchronized (this) {
            this._is_animating = false;
        }
        this._temp_controllers.clear();
        executePageCommand();
    }
}
