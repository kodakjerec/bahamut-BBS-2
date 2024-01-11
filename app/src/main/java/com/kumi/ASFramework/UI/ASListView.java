package com.kumi.ASFramework.UI;

import android.content.Context;
import android.util.AttributeSet;
import android.view.GestureDetector;
import android.view.MotionEvent;
import android.widget.ListView;

public class ASListView extends ListView implements GestureDetector.OnGestureListener {
  private GestureDetector _gesture_detector = null;
  
  private boolean _scroll_on_bottom = false;
  
  private boolean _scroll_on_top = false;
  
  public ASListViewExtentOptionalDelegate extendOptionalDelegate = null;
  
  public ASListViewOverscrollDelegate overscrollDelegate = null;
  
  public ASListView(Context paramContext) {
    super(paramContext);
    init();
  }
  
  public ASListView(Context paramContext, AttributeSet paramAttributeSet) {
    super(paramContext, paramAttributeSet);
    init();
  }
  
  public ASListView(Context paramContext, AttributeSet paramAttributeSet, int paramInt) {
    super(paramContext, paramAttributeSet, paramInt);
    init();
  }
  
  private void detectScrollPosition(MotionEvent paramMotionEvent) {
    // Byte code:
    //   0: aload_0
    //   1: monitorenter
    //   2: aload_0
    //   3: iconst_0
    //   4: putfield _scroll_on_top : Z
    //   7: aload_0
    //   8: iconst_0
    //   9: putfield _scroll_on_bottom : Z
    //   12: aload_0
    //   13: invokevirtual getChildCount : ()I
    //   16: ifle -> 83
    //   19: aload_0
    //   20: invokevirtual getFirstVisiblePosition : ()I
    //   23: ifne -> 42
    //   26: aload_0
    //   27: iconst_0
    //   28: invokevirtual getChildAt : (I)Landroid/view/View;
    //   31: invokevirtual getTop : ()I
    //   34: iflt -> 42
    //   37: aload_0
    //   38: iconst_1
    //   39: putfield _scroll_on_top : Z
    //   42: aload_0
    //   43: invokevirtual getLastVisiblePosition : ()I
    //   46: aload_0
    //   47: invokevirtual getCount : ()I
    //   50: iconst_1
    //   51: isub
    //   52: if_icmpne -> 80
    //   55: aload_0
    //   56: aload_0
    //   57: invokevirtual getChildCount : ()I
    //   60: iconst_1
    //   61: isub
    //   62: invokevirtual getChildAt : (I)Landroid/view/View;
    //   65: invokevirtual getBottom : ()I
    //   68: aload_0
    //   69: invokevirtual getHeight : ()I
    //   72: if_icmpgt -> 80
    //   75: aload_0
    //   76: iconst_1
    //   77: putfield _scroll_on_bottom : Z
    //   80: aload_0
    //   81: monitorexit
    //   82: return
    //   83: aload_0
    //   84: iconst_1
    //   85: putfield _scroll_on_top : Z
    //   88: aload_0
    //   89: iconst_1
    //   90: putfield _scroll_on_bottom : Z
    //   93: goto -> 80
    //   96: astore_1
    //   97: aload_0
    //   98: monitorexit
    //   99: aload_1
    //   100: athrow
    // Exception table:
    //   from	to	target	type
    //   2	42	96	finally
    //   42	80	96	finally
    //   80	82	96	finally
    //   83	93	96	finally
    //   97	99	96	finally
  }
  
  private void init() {
    this._gesture_detector = new GestureDetector(getContext(), this);
  }
  
  public boolean onDown(MotionEvent paramMotionEvent) {
    return true;
  }
  
  public boolean onFling(MotionEvent paramMotionEvent1, MotionEvent paramMotionEvent2, float paramFloat1, float paramFloat2) {
    // Byte code:
    //   0: fload_3
    //   1: invokestatic abs : (F)F
    //   4: fstore #6
    //   6: fload #4
    //   8: invokestatic abs : (F)F
    //   11: fstore #5
    //   13: aload_0
    //   14: getfield extendOptionalDelegate : Lcom/kumi/ASFramework/UI/ASListViewExtentOptionalDelegate;
    //   17: ifnull -> 144
    //   20: fload #6
    //   22: getstatic com/kumi/ASFramework/PageController/ASGestureView.filter : I
    //   25: i2f
    //   26: fcmpl
    //   27: ifle -> 144
    //   30: fload #6
    //   32: getstatic com/kumi/ASFramework/PageController/ASGestureView.range : F
    //   35: fload #5
    //   37: fmul
    //   38: fcmpl
    //   39: ifle -> 144
    //   42: aload_0
    //   43: invokevirtual getChildCount : ()I
    //   46: ifle -> 144
    //   49: aload_0
    //   50: monitorenter
    //   51: aload_1
    //   52: invokevirtual getY : ()F
    //   55: fstore_3
    //   56: iconst_0
    //   57: istore #7
    //   59: iload #7
    //   61: aload_0
    //   62: invokevirtual getChildCount : ()I
    //   65: if_icmpge -> 142
    //   68: aload_0
    //   69: iload #7
    //   71: invokevirtual getChildAt : (I)Landroid/view/View;
    //   74: astore_1
    //   75: aload_1
    //   76: invokevirtual getHeight : ()I
    //   79: ifle -> 220
    //   82: aload_1
    //   83: invokevirtual getTop : ()I
    //   86: i2f
    //   87: fload_3
    //   88: fcmpg
    //   89: ifgt -> 220
    //   92: aload_1
    //   93: invokevirtual getBottom : ()I
    //   96: i2f
    //   97: fload_3
    //   98: fcmpl
    //   99: iflt -> 220
    //   102: aload_0
    //   103: invokevirtual getFirstVisiblePosition : ()I
    //   106: istore #8
    //   108: aload_0
    //   109: getfield extendOptionalDelegate : Lcom/kumi/ASFramework/UI/ASListViewExtentOptionalDelegate;
    //   112: aload_0
    //   113: iload #8
    //   115: iload #7
    //   117: iadd
    //   118: invokeinterface onASListViewHandleExtentOptional : (Lcom/kumi/ASFramework/UI/ASListView;I)Z
    //   123: ifeq -> 142
    //   126: aload_2
    //   127: invokestatic obtain : (Landroid/view/MotionEvent;)Landroid/view/MotionEvent;
    //   130: astore_1
    //   131: aload_1
    //   132: iconst_3
    //   133: invokevirtual setAction : (I)V
    //   136: aload_0
    //   137: aload_1
    //   138: invokespecial onTouchEvent : (Landroid/view/MotionEvent;)Z
    //   141: pop
    //   142: aload_0
    //   143: monitorexit
    //   144: aload_0
    //   145: getfield overscrollDelegate : Lcom/kumi/ASFramework/UI/ASListViewOverscrollDelegate;
    //   148: ifnull -> 218
    //   151: fload #5
    //   153: getstatic com/kumi/ASFramework/PageController/ASGestureView.filter : I
    //   156: i2f
    //   157: fcmpl
    //   158: ifle -> 218
    //   161: fload #5
    //   163: getstatic com/kumi/ASFramework/PageController/ASGestureView.range : F
    //   166: fload #6
    //   168: fmul
    //   169: fcmpl
    //   170: ifle -> 218
    //   173: aload_0
    //   174: invokevirtual getChildCount : ()I
    //   177: ifle -> 218
    //   180: aload_0
    //   181: monitorenter
    //   182: fload #4
    //   184: fconst_0
    //   185: fcmpg
    //   186: ifge -> 231
    //   189: aload_0
    //   190: getfield _scroll_on_bottom : Z
    //   193: ifeq -> 231
    //   196: aload_0
    //   197: getfield overscrollDelegate : Lcom/kumi/ASFramework/UI/ASListViewOverscrollDelegate;
    //   200: aload_0
    //   201: invokeinterface onASListViewDelectedOverscrollTop : (Lcom/kumi/ASFramework/UI/ASListView;)V
    //   206: aload_0
    //   207: iconst_0
    //   208: putfield _scroll_on_top : Z
    //   211: aload_0
    //   212: iconst_0
    //   213: putfield _scroll_on_bottom : Z
    //   216: aload_0
    //   217: monitorexit
    //   218: iconst_1
    //   219: ireturn
    //   220: iinc #7, 1
    //   223: goto -> 59
    //   226: astore_1
    //   227: aload_0
    //   228: monitorexit
    //   229: aload_1
    //   230: athrow
    //   231: fload #4
    //   233: fconst_0
    //   234: fcmpl
    //   235: ifle -> 206
    //   238: aload_0
    //   239: getfield _scroll_on_top : Z
    //   242: ifeq -> 206
    //   245: aload_0
    //   246: getfield overscrollDelegate : Lcom/kumi/ASFramework/UI/ASListViewOverscrollDelegate;
    //   249: aload_0
    //   250: invokeinterface onASListViewDelectedOverscrollBottom : (Lcom/kumi/ASFramework/UI/ASListView;)V
    //   255: goto -> 206
    //   258: astore_1
    //   259: aload_0
    //   260: monitorexit
    //   261: aload_1
    //   262: athrow
    // Exception table:
    //   from	to	target	type
    //   51	56	226	finally
    //   59	142	226	finally
    //   142	144	226	finally
    //   189	206	258	finally
    //   206	218	258	finally
    //   227	229	226	finally
    //   238	255	258	finally
    //   259	261	258	finally
  }
  
  public void onLongPress(MotionEvent paramMotionEvent) {}
  
  public boolean onScroll(MotionEvent paramMotionEvent1, MotionEvent paramMotionEvent2, float paramFloat1, float paramFloat2) {
    return false;
  }
  
  public void onShowPress(MotionEvent paramMotionEvent) {}
  
  public boolean onSingleTapUp(MotionEvent paramMotionEvent) {
    return false;
  }
  
  public boolean onTouchEvent(MotionEvent paramMotionEvent) {
    this._gesture_detector.onTouchEvent(paramMotionEvent);
    if (paramMotionEvent.getAction() == 0)
      detectScrollPosition(paramMotionEvent); 
    return super.onTouchEvent(paramMotionEvent);
  }
}


/* Location:              C:\Users\kodak\Downloads\反編譯\dex-tools-v2.4\classes-dex2jar.jar!\com\kumi\ASFramework\UI\ASListView.class
 * Java compiler version: 6 (50.0)
 * JD-Core Version:       1.1.3
 */