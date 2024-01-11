package com.kumi.TelnetUI;

import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Paint;
import android.graphics.Path;
import android.util.TypedValue;

public class TelnetViewDrawer {
  public static final byte CLIP_LEFT = 1;
  
  public static final byte CLIP_NONE = 0;
  
  public static final byte CLIP_RIGHT = 2;
  
  public int backgroundColor = 0;
  
  public int bit = 1;
  
  public boolean blink = false;
  
  public int blockHeight = 0;
  
  public int blockWidth = 0;
  
  public Canvas canvas = null;
  
  public byte clip = 0;
  
  public float horizontalUnit = 0.0F;
  
  private int line_width = 0;
  
  public int loc = 1;
  
  public int originX = 0;
  
  public int originY = 0;
  
  public Paint paint = new Paint();
  
  public float radius = 0.0F;
  
  public int textBottomOffset = 0;
  
  public int textColor = 0;
  
  public float verticalUnit = 0.0F;
  
  private void drawBackground(TelnetViewBlock paramTelnetViewBlock) {
    this.paint.setColor(this.backgroundColor);
    this.canvas.drawRect(paramTelnetViewBlock.Left, paramTelnetViewBlock.Top, paramTelnetViewBlock.Right, paramTelnetViewBlock.Bottom, this.paint);
  }
  
  private void drawTextAtPosition(Context paramContext, TelnetViewBlock paramTelnetViewBlock, char paramChar) {
    Canvas canvas;
    Paint paint;
    float f1;
    float f2;
    int i;
    if (this.line_width == 0)
      this.line_width = (int)Math.ceil(TypedValue.applyDimension(1, 1.0F, paramContext.getResources().getDisplayMetrics())); 
    this.paint.setColor(this.textColor);
    switch (paramChar) {
      default:
        canvas = this.canvas;
        f1 = paramTelnetViewBlock.Left;
        f2 = (paramTelnetViewBlock.Bottom - this.textBottomOffset);
        paint = this.paint;
        canvas.drawText(new char[] { paramChar }, 0, 1, f1, f2, paint);
        return;
      case '＿':
        this.canvas.drawRect(((TelnetViewBlock)paint).Left, (((TelnetViewBlock)paint).Bottom - this.line_width), ((TelnetViewBlock)paint).Right, ((TelnetViewBlock)paint).Bottom, this.paint);
        return;
      case '▕':
        this.canvas.drawRect((((TelnetViewBlock)paint).Right - this.line_width), ((TelnetViewBlock)paint).Top, ((TelnetViewBlock)paint).Right, ((TelnetViewBlock)paint).Bottom, this.paint);
        return;
      case '￣':
        this.canvas.drawRect(((TelnetViewBlock)paint).Left, ((TelnetViewBlock)paint).Top, ((TelnetViewBlock)paint).Right, (((TelnetViewBlock)paint).Top + this.line_width), this.paint);
        return;
      case '█':
        this.canvas.drawRect(((TelnetViewBlock)paint).Left, ((TelnetViewBlock)paint).Top, ((TelnetViewBlock)paint).Right, ((TelnetViewBlock)paint).Bottom, this.paint);
        return;
      case '▇':
        i = (int)(((TelnetViewBlock)paint).Height - this.verticalUnit * 1.0F);
        this.canvas.drawRect(((TelnetViewBlock)paint).Left, (((TelnetViewBlock)paint).Bottom - i), ((TelnetViewBlock)paint).Right, ((TelnetViewBlock)paint).Bottom, this.paint);
        return;
      case '▆':
        i = (int)(((TelnetViewBlock)paint).Height - this.verticalUnit * 2.0F);
        this.canvas.drawRect(((TelnetViewBlock)paint).Left, (((TelnetViewBlock)paint).Bottom - i), ((TelnetViewBlock)paint).Right, ((TelnetViewBlock)paint).Bottom, this.paint);
        return;
      case '▅':
        i = (int)(((TelnetViewBlock)paint).Height - this.verticalUnit * 3.0F);
        this.canvas.drawRect(((TelnetViewBlock)paint).Left, (((TelnetViewBlock)paint).Bottom - i), ((TelnetViewBlock)paint).Right, ((TelnetViewBlock)paint).Bottom, this.paint);
        return;
      case '▄':
        i = (int)(((TelnetViewBlock)paint).Height - this.verticalUnit * 4.0F);
        this.canvas.drawRect(((TelnetViewBlock)paint).Left, (((TelnetViewBlock)paint).Bottom - i), ((TelnetViewBlock)paint).Right, ((TelnetViewBlock)paint).Bottom, this.paint);
        return;
      case '▃':
        i = (int)(((TelnetViewBlock)paint).Height - this.verticalUnit * 5.0F);
        this.canvas.drawRect(((TelnetViewBlock)paint).Left, (((TelnetViewBlock)paint).Bottom - i), ((TelnetViewBlock)paint).Right, ((TelnetViewBlock)paint).Bottom, this.paint);
        return;
      case '▂':
        i = (int)(((TelnetViewBlock)paint).Height - this.verticalUnit * 6.0F);
        this.canvas.drawRect(((TelnetViewBlock)paint).Left, (((TelnetViewBlock)paint).Bottom - i), ((TelnetViewBlock)paint).Right, ((TelnetViewBlock)paint).Bottom, this.paint);
        return;
      case '▁':
        i = (int)(((TelnetViewBlock)paint).Height - this.verticalUnit * 7.0F);
        this.canvas.drawRect(((TelnetViewBlock)paint).Left, (((TelnetViewBlock)paint).Bottom - i), ((TelnetViewBlock)paint).Right, ((TelnetViewBlock)paint).Bottom, this.paint);
        return;
      case 'ˍ':
        i = (int)(((TelnetViewBlock)paint).Height - this.verticalUnit * 8.0F);
        this.canvas.drawRect(((TelnetViewBlock)paint).Left, (((TelnetViewBlock)paint).Bottom - i), ((TelnetViewBlock)paint).Right, ((TelnetViewBlock)paint).Bottom, this.paint);
        return;
      case '▏':
        i = (int)(this.horizontalUnit * 1.0F);
        this.canvas.drawRect(((TelnetViewBlock)paint).Left, ((TelnetViewBlock)paint).Top, (((TelnetViewBlock)paint).Left + i), ((TelnetViewBlock)paint).Bottom, this.paint);
        return;
      case '▎':
        i = (int)(this.horizontalUnit * 2.0F);
        this.canvas.drawRect(((TelnetViewBlock)paint).Left, ((TelnetViewBlock)paint).Top, (((TelnetViewBlock)paint).Left + i), ((TelnetViewBlock)paint).Bottom, this.paint);
        return;
      case '▍':
        i = (int)(this.horizontalUnit * 3.0F);
        this.canvas.drawRect(((TelnetViewBlock)paint).Left, ((TelnetViewBlock)paint).Top, (((TelnetViewBlock)paint).Left + i), ((TelnetViewBlock)paint).Bottom, this.paint);
        return;
      case '▌':
        i = (int)(this.horizontalUnit * 4.0F);
        this.canvas.drawRect(((TelnetViewBlock)paint).Left, ((TelnetViewBlock)paint).Top, (((TelnetViewBlock)paint).Left + i), ((TelnetViewBlock)paint).Bottom, this.paint);
        return;
      case '▋':
        i = (int)(this.horizontalUnit * 5.0F);
        this.canvas.drawRect(((TelnetViewBlock)paint).Left, ((TelnetViewBlock)paint).Top, (((TelnetViewBlock)paint).Left + i), ((TelnetViewBlock)paint).Bottom, this.paint);
        return;
      case '▊':
        i = (int)(this.horizontalUnit * 6.0F);
        this.canvas.drawRect(((TelnetViewBlock)paint).Left, ((TelnetViewBlock)paint).Top, (((TelnetViewBlock)paint).Left + i), ((TelnetViewBlock)paint).Bottom, this.paint);
        return;
      case '▉':
        i = (int)(this.horizontalUnit * 7.0F);
        this.canvas.drawRect(((TelnetViewBlock)paint).Left, ((TelnetViewBlock)paint).Top, (((TelnetViewBlock)paint).Left + i), ((TelnetViewBlock)paint).Bottom, this.paint);
        return;
      case '╱':
        this.canvas.drawLine(((TelnetViewBlock)paint).Left, ((TelnetViewBlock)paint).Bottom, ((TelnetViewBlock)paint).Right, ((TelnetViewBlock)paint).Top, this.paint);
        return;
      case '╲':
        this.canvas.drawLine(((TelnetViewBlock)paint).Left, ((TelnetViewBlock)paint).Top, ((TelnetViewBlock)paint).Right, ((TelnetViewBlock)paint).Bottom, this.paint);
        return;
      case '＼':
        this.canvas.drawLine(((TelnetViewBlock)paint).Left, ((TelnetViewBlock)paint).Top, ((TelnetViewBlock)paint).Right, ((TelnetViewBlock)paint).Bottom, this.paint);
        return;
      case '╳':
        this.canvas.drawLine(((TelnetViewBlock)paint).Left, ((TelnetViewBlock)paint).Bottom, ((TelnetViewBlock)paint).Right, ((TelnetViewBlock)paint).Top, this.paint);
        this.canvas.drawLine(((TelnetViewBlock)paint).Left, ((TelnetViewBlock)paint).Top, ((TelnetViewBlock)paint).Right, ((TelnetViewBlock)paint).Bottom, this.paint);
        return;
      case '─':
        i = ((TelnetViewBlock)paint).Top + ((TelnetViewBlock)paint).Height / 2 - this.line_width / 2;
        this.canvas.drawRect(((TelnetViewBlock)paint).Left, i, ((TelnetViewBlock)paint).Right, (this.line_width + i), this.paint);
        return;
      case '○':
        this.paint.setStyle(Paint.Style.STROKE);
        this.canvas.drawCircle(((TelnetViewBlock)paint).Left + this.radius, ((TelnetViewBlock)paint).Top + this.radius, this.radius, this.paint);
        this.paint.setStyle(Paint.Style.FILL);
        return;
      case '●':
        this.canvas.drawCircle(((TelnetViewBlock)paint).Left + this.radius, ((TelnetViewBlock)paint).Top + this.radius, this.radius, this.paint);
        return;
      case '◢':
        path = new Path();
        path.moveTo(((TelnetViewBlock)paint).Left, ((TelnetViewBlock)paint).Bottom);
        path.lineTo(((TelnetViewBlock)paint).Right, ((TelnetViewBlock)paint).Top);
        path.lineTo(((TelnetViewBlock)paint).Right, ((TelnetViewBlock)paint).Bottom);
        path.lineTo(((TelnetViewBlock)paint).Left, ((TelnetViewBlock)paint).Bottom);
        this.canvas.drawPath(path, this.paint);
        return;
      case '◣':
        path = new Path();
        path.moveTo(((TelnetViewBlock)paint).Left, ((TelnetViewBlock)paint).Bottom);
        path.lineTo(((TelnetViewBlock)paint).Left, ((TelnetViewBlock)paint).Top);
        path.lineTo(((TelnetViewBlock)paint).Right, ((TelnetViewBlock)paint).Bottom);
        path.lineTo(((TelnetViewBlock)paint).Left, ((TelnetViewBlock)paint).Bottom);
        this.canvas.drawPath(path, this.paint);
        return;
      case '◥':
        path = new Path();
        path.moveTo(((TelnetViewBlock)paint).Left, ((TelnetViewBlock)paint).Top);
        path.lineTo(((TelnetViewBlock)paint).Right, ((TelnetViewBlock)paint).Top);
        path.lineTo(((TelnetViewBlock)paint).Right, ((TelnetViewBlock)paint).Bottom);
        path.lineTo(((TelnetViewBlock)paint).Left, ((TelnetViewBlock)paint).Top);
        this.canvas.drawPath(path, this.paint);
        return;
      case '◤':
        break;
    } 
    Path path = new Path();
    path.moveTo(((TelnetViewBlock)paint).Left, ((TelnetViewBlock)paint).Bottom);
    path.lineTo(((TelnetViewBlock)paint).Left, ((TelnetViewBlock)paint).Top);
    path.lineTo(((TelnetViewBlock)paint).Right, ((TelnetViewBlock)paint).Top);
    path.lineTo(((TelnetViewBlock)paint).Left, ((TelnetViewBlock)paint).Bottom);
    this.canvas.drawPath(path, this.paint);
  }
  
  public void drawCharAtPosition(Context paramContext, int paramInt1, int paramInt2, char paramChar) {
    // Byte code:
    //   0: new com/kumi/TelnetUI/TelnetViewDrawer$TelnetViewBlock
    //   3: dup
    //   4: aload_0
    //   5: aconst_null
    //   6: invokespecial <init> : (Lcom/kumi/TelnetUI/TelnetViewDrawer;Lcom/kumi/TelnetUI/TelnetViewDrawer$1;)V
    //   9: astore #5
    //   11: aload #5
    //   13: aload_0
    //   14: getfield blockWidth : I
    //   17: aload_0
    //   18: getfield bit : I
    //   21: imul
    //   22: putfield Width : I
    //   25: aload #5
    //   27: aload_0
    //   28: getfield blockHeight : I
    //   31: putfield Height : I
    //   34: aload #5
    //   36: aload_0
    //   37: getfield originX : I
    //   40: aload_0
    //   41: getfield blockWidth : I
    //   44: iload_3
    //   45: imul
    //   46: iadd
    //   47: putfield Left : I
    //   50: aload #5
    //   52: aload #5
    //   54: getfield Left : I
    //   57: aload #5
    //   59: getfield Width : I
    //   62: iadd
    //   63: putfield Right : I
    //   66: aload #5
    //   68: aload_0
    //   69: getfield originY : I
    //   72: aload #5
    //   74: getfield Height : I
    //   77: iload_2
    //   78: imul
    //   79: iadd
    //   80: putfield Top : I
    //   83: aload #5
    //   85: aload #5
    //   87: getfield Top : I
    //   90: aload #5
    //   92: getfield Height : I
    //   95: iadd
    //   96: putfield Bottom : I
    //   99: aload_0
    //   100: getfield canvas : Landroid/graphics/Canvas;
    //   103: invokevirtual save : ()I
    //   106: pop
    //   107: aload_0
    //   108: getfield clip : B
    //   111: tableswitch default -> 132, 1 -> 190, 2 -> 226
    //   132: aload_0
    //   133: getfield canvas : Landroid/graphics/Canvas;
    //   136: aload #5
    //   138: getfield Left : I
    //   141: aload #5
    //   143: getfield Top : I
    //   146: aload #5
    //   148: getfield Right : I
    //   151: aload #5
    //   153: getfield Bottom : I
    //   156: invokevirtual clipRect : (IIII)Z
    //   159: pop
    //   160: aload_0
    //   161: aload #5
    //   163: invokespecial drawBackground : (Lcom/kumi/TelnetUI/TelnetViewDrawer$TelnetViewBlock;)V
    //   166: aload_0
    //   167: getfield blink : Z
    //   170: ifne -> 182
    //   173: aload_0
    //   174: aload_1
    //   175: aload #5
    //   177: iload #4
    //   179: invokespecial drawTextAtPosition : (Landroid/content/Context;Lcom/kumi/TelnetUI/TelnetViewDrawer$TelnetViewBlock;C)V
    //   182: aload_0
    //   183: getfield canvas : Landroid/graphics/Canvas;
    //   186: invokevirtual restore : ()V
    //   189: return
    //   190: aload_0
    //   191: getfield canvas : Landroid/graphics/Canvas;
    //   194: aload #5
    //   196: getfield Left : I
    //   199: aload #5
    //   201: getfield Top : I
    //   204: aload #5
    //   206: getfield Left : I
    //   209: aload_0
    //   210: getfield blockWidth : I
    //   213: iadd
    //   214: aload #5
    //   216: getfield Bottom : I
    //   219: invokevirtual clipRect : (IIII)Z
    //   222: pop
    //   223: goto -> 160
    //   226: aload_0
    //   227: getfield canvas : Landroid/graphics/Canvas;
    //   230: aload #5
    //   232: getfield Left : I
    //   235: aload_0
    //   236: getfield blockWidth : I
    //   239: iadd
    //   240: aload #5
    //   242: getfield Top : I
    //   245: aload #5
    //   247: getfield Right : I
    //   250: aload #5
    //   252: getfield Bottom : I
    //   255: invokevirtual clipRect : (IIII)Z
    //   258: pop
    //   259: goto -> 160
  }
  
  private class TelnetViewBlock {
    public int Bottom = 0;
    
    public int Height = 0;
    
    public int Left = 0;
    
    public int Right = 0;
    
    public int Top = 0;
    
    public int Width = 0;
    
    final TelnetViewDrawer this$0;
    
    private TelnetViewBlock() {}
  }
}


/* Location:              C:\Users\kodak\Downloads\反編譯\dex-tools-v2.4\classes-dex2jar.jar!\com\kumi\TelnetUI\TelnetViewDrawer.class
 * Java compiler version: 6 (50.0)
 * JD-Core Version:       1.1.3
 */