package com.kumi.TelnetUI;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.Matrix;
import android.graphics.Paint;
import android.graphics.Rect;
import android.graphics.Typeface;
import android.os.Handler;
import android.os.Message;
import android.util.AttributeSet;
import android.util.TypedValue;
import android.view.View;
import android.view.ViewGroup;
import com.kumi.Telnet.Model.TelnetFrame;
import com.kumi.TextEncoder.B2UEncoder;
import java.util.Iterator;
import java.util.Vector;

public class TelnetView extends View {
  private float DEFAULT_TEXT_SIZE = 20.0F;
  
  private double _bitmap_block_height = 0.0D;
  
  private double _bitmap_block_width = 0.0D;
  
  private Bitmap.Config _bitmap_config = Bitmap.Config.RGB_565;
  
  private int _bitmap_space_column = 0;
  
  private int _bitmap_space_row = 0;
  
  private int _bitmap_space_x = 8;
  
  private int _bitmap_space_y = 4;
  
  private Bitmap[][] _bitmaps = (Bitmap[][])null;
  
  private boolean _blink = false;
  
  private Vector<Position> _blink_list = new Vector<Position>();
  
  private BlinkThread _blink_thread = null;
  
  private double _block_height = 15.0D;
  
  private double _block_width = 6.0D;
  
  Rect _clip = new Rect();
  
  private int _column = 80;
  
  private BitmapSpace _current_space = new BitmapSpace();
  
  private int _draw_height = 0;
  
  private boolean _draw_separator_line = false;
  
  private int _draw_width = 0;
  
  private TelnetViewDrawer _drawer = new TelnetViewDrawer();
  
  private double _en_text_size = 12.0D;
  
  private TelnetFrame _frame = null;
  
  private Handler _handler = null;
  
  private double _horizontal_unit = 1.5D;
  
  Matrix _matrix = new Matrix();
  
  private int _origin_x = 0;
  
  private int _origin_y = 0;
  
  Paint _paint = new Paint();
  
  private double _radius = 0.0D;
  
  private double _ratio = 2.5D;
  
  private int _row = 24;
  
  private float _scale_x = 1.0F;
  
  private float _scale_y = 1.0F;
  
  private Typeface _typeface = null;
  
  private double _vertical_unit = 2.0D;
  
  private double _zh_text_size = 12.0D;
  
  Typeface en_typepace = Typeface.create("MONOSPACE", 0);
  
  Typeface zh_typepace = Typeface.create("DEFAULT", 0);
  
  public TelnetView(Context paramContext) {
    super(paramContext);
    initial();
  }
  
  public TelnetView(Context paramContext, AttributeSet paramAttributeSet) {
    super(paramContext, paramAttributeSet);
    initial();
  }
  
  public TelnetView(Context paramContext, AttributeSet paramAttributeSet, int paramInt) {
    super(paramContext, paramAttributeSet, paramInt);
    initial();
  }
  
  private void cleanBitmap() {
    this._bitmaps = (Bitmap[][])null;
    this._blink_list.clear();
  }
  
  private void drawBitSpace1(int paramInt1, int paramInt2, int paramInt3, int paramInt4) {
    boolean bool = true;
    this._drawer.paint.setTextSize((int)this._en_text_size);
    char c = (char)this._frame.getPositionData(paramInt1, paramInt2);
    int j = this._frame.getPositionTextColor(paramInt1, paramInt2);
    int i = this._frame.getPositionBackgroundColor(paramInt1, paramInt2);
    this._drawer.textColor = j;
    this._drawer.backgroundColor = i;
    this._drawer.bit = 1;
    this._drawer.paint.setTypeface(this.en_typepace);
    TelnetViewDrawer telnetViewDrawer = this._drawer;
    if (!this._blink || !this._frame.getPositionBlink(paramInt1, paramInt2))
      bool = false; 
    telnetViewDrawer.blink = bool;
    this._drawer.clip = 0;
    this._drawer.drawCharAtPosition(getContext(), paramInt3, paramInt4, c);
  }
  
  private void drawDoubleBitSpace2_1(int paramInt1, int paramInt2, int paramInt3, int paramInt4) {
    boolean bool;
    this._drawer.paint.setTextSize((int)this._zh_text_size);
    int i = this._frame.getPositionData(paramInt1, paramInt2);
    int j = this._frame.getPositionData(paramInt1, paramInt2 + 1);
    char c = B2UEncoder.getInstance().encodeChar((char)((i << 8) + j));
    i = this._frame.getPositionTextColor(paramInt1, paramInt2);
    j = this._frame.getPositionBackgroundColor(paramInt1, paramInt2);
    this._drawer.textColor = i;
    this._drawer.backgroundColor = j;
    this._drawer.bit = 2;
    this._drawer.paint.setTypeface(this.zh_typepace);
    TelnetViewDrawer telnetViewDrawer = this._drawer;
    if (this._blink && this._frame.getPositionBlink(paramInt1, paramInt2)) {
      bool = true;
    } else {
      bool = false;
    } 
    telnetViewDrawer.blink = bool;
    this._drawer.clip = 1;
    this._drawer.drawCharAtPosition(getContext(), paramInt3, paramInt4, c);
  }
  
  private void drawDoubleBitSpace2_2(int paramInt1, int paramInt2, int paramInt3, int paramInt4) {
    boolean bool;
    this._drawer.paint.setTextSize((int)this._zh_text_size);
    int i = this._frame.getPositionData(paramInt1, paramInt2);
    int j = this._frame.getPositionData(paramInt1, paramInt2 - 1);
    char c = B2UEncoder.getInstance().encodeChar((char)((j << 8) + i));
    i = this._frame.getPositionTextColor(paramInt1, paramInt2);
    j = this._frame.getPositionBackgroundColor(paramInt1, paramInt2);
    this._drawer.textColor = i;
    this._drawer.backgroundColor = j;
    this._drawer.bit = 2;
    this._drawer.paint.setTypeface(this.zh_typepace);
    TelnetViewDrawer telnetViewDrawer = this._drawer;
    if (this._blink && this._frame.getPositionBlink(paramInt1, paramInt2)) {
      bool = true;
    } else {
      bool = false;
    } 
    telnetViewDrawer.blink = bool;
    this._drawer.clip = 2;
    this._drawer.drawCharAtPosition(getContext(), paramInt3, paramInt4 - 1, c);
  }
  
  private void drawSingleBitSpace2_1(int paramInt1, int paramInt2, int paramInt3, int paramInt4) {
    boolean bool;
    this._drawer.paint.setTextSize((int)this._zh_text_size);
    int i = this._frame.getPositionData(paramInt1, paramInt2);
    int j = this._frame.getPositionData(paramInt1, paramInt2 + 1);
    char c = B2UEncoder.getInstance().encodeChar((char)((i << 8) + j));
    i = this._frame.getPositionTextColor(paramInt1, paramInt2);
    j = this._frame.getPositionBackgroundColor(paramInt1, paramInt2);
    this._drawer.textColor = i;
    this._drawer.backgroundColor = j;
    this._drawer.bit = 2;
    this._drawer.paint.setTypeface(this.zh_typepace);
    TelnetViewDrawer telnetViewDrawer = this._drawer;
    if (this._blink && this._frame.getPositionBlink(paramInt1, paramInt2)) {
      bool = true;
    } else {
      bool = false;
    } 
    telnetViewDrawer.blink = bool;
    this._drawer.clip = 0;
    this._drawer.drawCharAtPosition(getContext(), paramInt3, paramInt4, c);
  }
  
  private void drawSingleBitSpace2_2(int paramInt1, int paramInt2, int paramInt3, int paramInt4) {
    boolean bool;
    this._drawer.paint.setTextSize((int)this._zh_text_size);
    int i = this._frame.getPositionData(paramInt1, paramInt2);
    int j = this._frame.getPositionData(paramInt1, paramInt2 - 1);
    char c = B2UEncoder.getInstance().encodeChar((char)((j << 8) + i));
    i = this._frame.getPositionTextColor(paramInt1, paramInt2);
    j = this._frame.getPositionBackgroundColor(paramInt1, paramInt2);
    this._drawer.textColor = i;
    this._drawer.backgroundColor = j;
    this._drawer.bit = 2;
    this._drawer.paint.setTypeface(this.zh_typepace);
    TelnetViewDrawer telnetViewDrawer = this._drawer;
    if (this._blink && this._frame.getPositionBlink(paramInt1, paramInt2)) {
      bool = true;
    } else {
      bool = false;
    } 
    telnetViewDrawer.blink = bool;
    this._drawer.clip = 0;
    this._drawer.drawCharAtPosition(getContext(), paramInt3, paramInt4 - 1, c);
  }
  
  private void drawTelnet(Canvas paramCanvas, Position paramPosition, int paramInt1, int paramInt2, int paramInt3, int paramInt4) {
    if (this._typeface != null)
      this._drawer.paint.setTypeface(this._typeface); 
    if (this._frame != null) {
      this._drawer.canvas = paramCanvas;
      Rect rect2 = new Rect();
      this._drawer.paint.getTextBounds("國", 0, 1, rect2);
      Rect rect1 = new Rect();
      this._drawer.paint.getTextBounds("D", 0, 1, rect1);
      this._drawer.clip = 0;
      double d1 = Math.ceil(this._block_height - this._zh_text_size);
      double d2 = Math.ceil((rect2.height() - rect1.height()));
      this._drawer.textBottomOffset = (int)Math.ceil((d1 + d2) / 2.0D);
      boolean bool = false;
      for (int i = paramInt1; i < paramInt2; i++) {
        int k = i - paramInt1;
        for (int j = paramInt3; j < paramInt4; j++) {
          int m = j - paramInt3;
          if (this._frame.getPositionBlink(i, j))
            bool = true; 
          byte b = this._frame.getPositionBitSpace(i, j);
          if (b == 1) {
            drawSingleBitSpace2_1(i, j, k, m);
            j++;
          } else if (b == 2 && j > 0) {
            drawSingleBitSpace2_2(i, j, k, m);
          } else if (b == 3) {
            drawDoubleBitSpace2_1(i, j, k, m);
          } else if (b == 4 && j > 0) {
            drawDoubleBitSpace2_2(i, j, k, m);
          } else {
            drawBitSpace1(i, j, k, m);
          } 
        } 
      } 
      if (bool) {
        paramInt2 = 0;
        Iterator<Position> iterator = this._blink_list.iterator();
        while (true) {
          paramInt1 = paramInt2;
          if (iterator.hasNext()) {
            if (((Position)iterator.next()).isEquals(paramPosition)) {
              paramInt1 = 1;
              break;
            } 
            continue;
          } 
          break;
        } 
        if (paramInt1 == 0)
          this._blink_list.add(paramPosition); 
      } 
      if (this._draw_separator_line) {
        this._drawer.paint.setColor(1728053247);
        for (paramInt1 = 0; paramInt1 < this._frame.getRowSize(); paramInt1++) {
          paramInt2 = (int)(paramInt1 * this._block_height);
          paramCanvas.drawLine(0.0F, paramInt2, 480.0F, paramInt2, this._drawer.paint);
        } 
        for (paramInt1 = 0; paramInt1 < 80; paramInt1++) {
          paramInt2 = (int)(paramInt1 * this._block_width);
          paramCanvas.drawLine(paramInt2, 0.0F, paramInt2, 360.0F, this._drawer.paint);
        } 
      } 
    } else {
      System.out.println("_telnet_model is null");
    } 
  }
  
  private Bitmap getBitmap(int paramInt1, int paramInt2) {
    Bitmap bitmap = null;
    if (this._bitmaps == null)
      resetBitmap(); 
    if (bitmapContainsPosition(paramInt1, paramInt2)) {
      Bitmap bitmap1 = this._bitmaps[paramInt1][paramInt2];
      bitmap = bitmap1;
      if (bitmap1 == null) {
        bitmap = createBitmap();
        this._bitmaps[paramInt1][paramInt2] = bitmap;
        Canvas canvas = new Canvas(bitmap);
        int m = this._bitmap_space_y;
        int j = (paramInt1 + 1) * this._bitmap_space_y;
        int i = j;
        if (j > this._frame.getRowSize())
          i = this._frame.getRowSize(); 
        int n = this._bitmap_space_x;
        int k = (paramInt2 + 1) * this._bitmap_space_x;
        j = k;
        if (k > 80)
          j = 80; 
        drawTelnet(canvas, new Position(paramInt1, paramInt2), paramInt1 * m, i, paramInt2 * n, j);
      } 
    } 
    return bitmap;
  }
  
  private void initial() {}
  
  private int match(int paramInt1, int paramInt2) {
    return paramInt1 / paramInt2 * paramInt2;
  }
  
  private void reloadDrawer() {
    this._drawer.blockWidth = (int)this._block_width;
    this._drawer.blockHeight = (int)this._block_height;
    this._drawer.horizontalUnit = (int)this._horizontal_unit;
    this._drawer.verticalUnit = (int)this._vertical_unit;
    this._drawer.radius = (int)this._radius;
    this._drawer.originX = this._origin_x;
    this._drawer.originY = this._origin_y;
    this._drawer.paint.setAntiAlias(true);
  }
  
  private void resetBitmap() {
    boolean bool = true;
    this._bitmap_space_row = 0;
    this._bitmap_space_column = 0;
    if (this._frame != null) {
      int i = this._frame.getRowSize();
      int j = i / this._bitmap_space_y;
      if (i % this._bitmap_space_y > 0) {
        i = 1;
      } else {
        i = 0;
      } 
      this._bitmap_space_row = i + j;
      j = 80 / this._bitmap_space_x;
      if (80 % this._bitmap_space_x > 0) {
        i = bool;
      } else {
        i = 0;
      } 
      this._bitmap_space_column = j + i;
    } 
    if (this._bitmap_space_row > 0 && this._bitmap_space_column > 0)
      this._bitmaps = new Bitmap[this._bitmap_space_row][this._bitmap_space_column]; 
  }
  
  private void startBlink() {
    if (this._handler == null)
      this._handler = new Handler() {
          final TelnetView this$0;
          
          public void handleMessage(Message param1Message) {
            boolean bool;
            TelnetView telnetView = TelnetView.this;
            if (!TelnetView.this._blink) {
              bool = true;
            } else {
              bool = false;
            } 
            TelnetView.access$202(telnetView, bool);
            for (TelnetView.Position position : TelnetView.this._blink_list) {
              if (TelnetView.this.bitmapContainsPosition(position))
                TelnetView.this.removePositionBitmap(position); 
            } 
            TelnetView.this.invalidate();
          }
        }; 
    if (this._blink_thread == null) {
      this._blink_thread = new BlinkThread();
      this._blink_thread.start();
    } 
  }
  
  private void stopBlink() {
    this._handler = null;
    if (this._blink_thread != null) {
      this._blink_thread.stopBlink();
      this._blink_thread = null;
    } 
  }
  
  public boolean bitmapContainsPosition(int paramInt1, int paramInt2) {
    return (this._bitmaps != null && paramInt1 >= 0 && paramInt1 < this._bitmaps.length && paramInt2 >= 0 && paramInt2 < (this._bitmaps[paramInt1]).length);
  }
  
  public boolean bitmapContainsPosition(Position paramPosition) {
    return bitmapContainsPosition(paramPosition.row, paramPosition.column);
  }
  
  public BitmapSpace calculateBlock(double paramDouble1, double paramDouble2, double paramDouble3, double paramDouble4) {
    BitmapSpace bitmapSpace = new BitmapSpace();
    bitmapSpace.left = (int)Math.floor(paramDouble1 / this._bitmap_block_width);
    bitmapSpace.top = (int)Math.floor(paramDouble2 / this._bitmap_block_height);
    bitmapSpace.width = (int)Math.ceil((paramDouble1 + paramDouble3) / this._bitmap_block_width) - bitmapSpace.left + 1;
    bitmapSpace.height = (int)Math.ceil((paramDouble2 + paramDouble4) / this._bitmap_block_height) - bitmapSpace.top + 1;
    return bitmapSpace;
  }
  
  public void calculateLayout() {
    if (this._frame != null) {
      int i = (int)Math.ceil(TypedValue.applyDimension(1, 1.0F, getContext().getResources().getDisplayMetrics()));
      this._vertical_unit = (this._block_height - i) / 8.0D;
      this._horizontal_unit = this._block_width * 2.0D / 7.0D;
      this._radius = match((int)(this._block_height / 2.0D), 2);
      if (this._radius > this._block_width)
        this._radius = this._block_width; 
      this._zh_text_size = match((int)(this._radius * 2.0D), 2);
      this._en_text_size = match((int)(this._zh_text_size * 0.8999999761581421D), 2);
    } 
    this._bitmap_block_width = this._block_width * this._bitmap_space_x;
    this._bitmap_block_height = this._block_height * this._bitmap_space_y;
    reloadDrawer();
  }
  
  public Bitmap createBitmap() {
    return Bitmap.createBitmap((int)this._bitmap_block_width, (int)this._bitmap_block_height, this._bitmap_config);
  }
  
  protected void finalize() throws Throwable {
    if (this._blink_thread != null)
      stopBlink(); 
    super.finalize();
  }
  
  public TelnetFrame getFrame() {
    return this._frame;
  }
  
  protected void onDetachedFromWindow() {
    super.onDetachedFromWindow();
    stopBlink();
  }
  
  protected void onDraw(Canvas paramCanvas) {
    super.onDraw(paramCanvas);
    paramCanvas.getClipBounds(this._clip);
    paramCanvas.scale(this._scale_x, this._scale_y);
    BitmapSpace bitmapSpace = calculateBlock(this._clip.left, this._clip.top, this._clip.width(), this._clip.height());
    if (!this._current_space.isEquals(bitmapSpace))
      setBitmapSpace(bitmapSpace); 
    int n = this._current_space.left;
    int j = this._current_space.left + this._current_space.width;
    int i = j;
    if (j > this._bitmap_space_column)
      i = this._bitmap_space_column; 
    int k = this._current_space.top;
    int m = this._current_space.top + this._current_space.height;
    j = m;
    if (m > this._bitmap_space_row)
      j = this._bitmap_space_row; 
    while (k < j) {
      float f = (float)(k * this._bitmap_block_height);
      for (m = n; m < i; m++) {
        Bitmap bitmap = getBitmap(k, m);
        if (bitmap == null) {
          System.out.println(k + "," + m + " bitmap is null");
        } else {
          float f1 = (float)(m * this._bitmap_block_width);
          this._matrix.setTranslate(f1, f);
          paramCanvas.drawBitmap(bitmap, this._matrix, this._paint);
        } 
      } 
      k++;
    } 
    if (this._blink_list.size() > 0) {
      i = 1;
    } else {
      i = 0;
    } 
    if (i != 0 && this._blink_thread == null)
      startBlink(); 
    if (i == 0 && this._blink_thread != null)
      stopBlink(); 
  }
  
  protected void onMeasure(int paramInt1, int paramInt2) {
    cleanBitmap();
    int j = View.MeasureSpec.getSize(paramInt1);
    int i = View.MeasureSpec.getSize(paramInt2);
    ViewGroup.LayoutParams layoutParams = getLayoutParams();
    int k = match((int)TypedValue.applyDimension(2, this.DEFAULT_TEXT_SIZE, getContext().getResources().getDisplayMetrics()), 4);
    this._draw_width = 0;
    this._draw_height = 0;
    this._scale_x = 1.0F;
    this._scale_y = 1.0F;
    this._row = 0;
    this._column = 0;
    if (this._frame == null) {
      super.onMeasure(paramInt1, paramInt2);
      return;
    } 
    this._row = this._frame.getRowSize();
    this._column = 80;
    if (layoutParams.width == -2 && layoutParams.height == -2) {
      this._block_width = (k / 2);
      this._block_height = match((int)(this._block_width * this._ratio), 2);
      this._draw_width = (int)(this._block_width * this._column);
      this._draw_height = (int)(this._block_height * this._row);
      this._scale_x = 1.0F;
      this._scale_y = 1.0F;
      paramInt2 = View.MeasureSpec.makeMeasureSpec(this._draw_width, 1073741824);
      paramInt1 = View.MeasureSpec.makeMeasureSpec(this._draw_height, 1073741824);
    } else if (layoutParams.height == -2) {
      this._block_width = match(j / this._column, 2);
      this._block_height = match((int)(this._block_width * this._ratio), 2);
      this._draw_width = (int)(this._block_width * this._column);
      this._draw_height = (int)(this._block_height * this._row);
      this._scale_x = j / this._draw_width;
      paramInt2 = View.MeasureSpec.makeMeasureSpec(j, 1073741824);
      paramInt1 = View.MeasureSpec.makeMeasureSpec(this._draw_height, 1073741824);
    } else if (layoutParams.width == -2) {
      this._block_height = match(i / this._row, 2);
      this._block_width = match((int)(this._block_height / this._ratio), 2);
      this._draw_width = (int)(this._block_width * this._column);
      this._draw_height = (int)(this._block_height * this._row);
      this._scale_y = i / this._draw_height;
      paramInt2 = View.MeasureSpec.makeMeasureSpec(this._draw_width, 1073741824);
      paramInt1 = View.MeasureSpec.makeMeasureSpec(i, 1073741824);
    } else {
      this._block_width = match(j / this._column, 2);
      this._block_height = match((int)(this._block_width * this._ratio), 2);
      this._draw_width = (int)(this._block_width * this._column);
      this._draw_height = (int)(this._block_height * this._row);
      this._scale_x = j / this._draw_width;
      this._scale_y = i / this._draw_height;
      paramInt2 = View.MeasureSpec.makeMeasureSpec(j, 1073741824);
      paramInt1 = View.MeasureSpec.makeMeasureSpec(i, 1073741824);
    } 
    calculateLayout();
    super.onMeasure(paramInt2, paramInt1);
  }
  
  public void removePositionBitmap(int paramInt1, int paramInt2) {
    if (bitmapContainsPosition(paramInt1, paramInt2))
      this._bitmaps[paramInt1][paramInt2] = null; 
  }
  
  public void removePositionBitmap(Position paramPosition) {
    removePositionBitmap(paramPosition.row, paramPosition.column);
  }
  
  public void setBitmapSpace(BitmapSpace paramBitmapSpace) {
    if (this._bitmaps == null)
      resetBitmap(); 
    int j = this._current_space.left;
    int k = this._current_space.width;
    int m = this._current_space.top;
    int n = this._current_space.height;
    for (int i = this._current_space.left; i < j + k; i++) {
      for (int i1 = this._current_space.top; i1 < m + n; i1++) {
        if (!paramBitmapSpace.contains(i, i1))
          removePositionBitmap(i, i1); 
      } 
    } 
    this._current_space.set(paramBitmapSpace);
  }
  
  public void setFrame(TelnetFrame paramTelnetFrame) {
    this._frame = null;
    this._frame = paramTelnetFrame;
    this._frame.reloadSpace();
    cleanBitmap();
    invalidate();
  }
  
  public void setTypeface(Typeface paramTypeface) {
    this._typeface = paramTypeface;
  }
  
  private class BitmapSpace {
    public int height = 0;
    
    public int left = 0;
    
    final TelnetView this$0;
    
    public int top = 0;
    
    public int width = 0;
    
    private BitmapSpace() {}
    
    public boolean contains(int param1Int1, int param1Int2) {
      return (param1Int1 >= this.left && param1Int1 < this.left + this.width && param1Int2 >= this.top && param1Int2 < this.top + this.height);
    }
    
    public boolean isEquals(BitmapSpace param1BitmapSpace) {
      return (this.left == param1BitmapSpace.left && this.top == param1BitmapSpace.top && this.width == param1BitmapSpace.width && this.height == param1BitmapSpace.height);
    }
    
    public void set(BitmapSpace param1BitmapSpace) {
      this.left = param1BitmapSpace.left;
      this.top = param1BitmapSpace.top;
      this.width = param1BitmapSpace.width;
      this.height = param1BitmapSpace.height;
    }
    
    public String toString() {
      return "(" + this.left + " , " + this.top + ") (" + this.width + " , " + this.height + ")";
    }
  }
  
  private class BlinkThread extends Thread {
    boolean _run = true;
    
    final TelnetView this$0;
    
    private BlinkThread() {}
    
    public void run() {
      this._run = true;
      while (this._run) {
        try {
          sleep(1000L);
          if (TelnetView.this._handler != null)
            TelnetView.this._handler.sendEmptyMessage(0); 
        } catch (InterruptedException interruptedException) {
          interruptedException.printStackTrace();
          break;
        } 
      } 
    }
    
    public void stopBlink() {
      this._run = false;
    }
  }
  
  private class Position {
    public int column = 0;
    
    public int row = 0;
    
    final TelnetView this$0;
    
    public Position(int param1Int1, int param1Int2) {
      this.row = param1Int1;
      this.column = param1Int2;
    }
    
    public boolean isEquals(Position param1Position) {
      return (this.row == param1Position.row && this.column == param1Position.column);
    }
  }
}


/* Location:              C:\Users\kodak\Downloads\反編譯\dex-tools-v2.4\classes-dex2jar.jar!\com\kumi\TelnetUI\TelnetView.class
 * Java compiler version: 6 (50.0)
 * JD-Core Version:       1.1.3
 */