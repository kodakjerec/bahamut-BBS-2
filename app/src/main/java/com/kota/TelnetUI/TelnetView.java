package com.kota.TelnetUI;

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
import com.kota.Telnet.Model.TelnetFrame;
import com.kota.TextEncoder.B2UEncoder;
import java.lang.reflect.Array;
import java.util.Iterator;
import java.util.Vector;

public class TelnetView extends View {
    private float DEFAULT_TEXT_SIZE = 20.0f;
    private double _bitmap_block_height = 0.0d;
    private double _bitmap_block_width = 0.0d;
    private Bitmap.Config _bitmap_config = Bitmap.Config.RGB_565;
    private int _bitmap_space_column = 0;
    private int _bitmap_space_row = 0;
    private int _bitmap_space_x = 8;
    private int _bitmap_space_y = 4;
    private Bitmap[][] _bitmaps = null;
    /* access modifiers changed from: private */
    public boolean _blink = false;
    /* access modifiers changed from: private */
    public Vector<Position> _blink_list = new Vector<>();
    private BlinkThread _blink_thread = null;
    private double _block_height = 15.0d;
    private double _block_width = 6.0d;
    Rect _clip = new Rect();
    private int _column = 80;
    private BitmapSpace _current_space = new BitmapSpace();
    private int _draw_height = 0;
    private boolean _draw_separator_line = false;
    private int _draw_width = 0;
    private TelnetViewDrawer _drawer = new TelnetViewDrawer();
    private double _en_text_size = 12.0d;
    private TelnetFrame _frame = null;
    /* access modifiers changed from: private */
    public Handler _handler = null;
    private double _horizontal_unit = 1.5d;
    Matrix _matrix = new Matrix();
    private int _origin_x = 0;
    private int _origin_y = 0;
    Paint _paint = new Paint();
    private double _radius = 0.0d;
    private double _ratio = 2.5d;
    private int _row = 24;
    private float _scale_x = 1.0f;
    private float _scale_y = 1.0f;
    private Typeface _typeface = null;
    private double _vertical_unit = 2.0d;
    private double _zh_text_size = 12.0d;
    Typeface en_typepace = Typeface.create("MONOSPACE", 0);
    Typeface zh_typepace = Typeface.create("DEFAULT", 0);

    private class BitmapSpace {
        public int height;
        public int left;
        public int top;
        public int width;

        private BitmapSpace() {
            this.left = 0;
            this.top = 0;
            this.width = 0;
            this.height = 0;
        }

        public boolean contains(int row, int column) {
            return row >= this.left && row < this.left + this.width && column >= this.top && column < this.top + this.height;
        }

        public void set(BitmapSpace block) {
            this.left = block.left;
            this.top = block.top;
            this.width = block.width;
            this.height = block.height;
        }

        public boolean isEquals(BitmapSpace block) {
            return this.left == block.left && this.top == block.top && this.width == block.width && this.height == block.height;
        }

        public String toString() {
            return "(" + this.left + " , " + this.top + ") (" + this.width + " , " + this.height + ")";
        }
    }

    private class Position {
        public int column = 0;
        public int row = 0;

        public Position(int aRow, int aColumn) {
            this.row = aRow;
            this.column = aColumn;
        }

        public boolean isEquals(Position aPosition) {
            return this.row == aPosition.row && this.column == aPosition.column;
        }
    }

    private class BlinkThread extends Thread {
        boolean _run;

        private BlinkThread() {
            this._run = true;
        }

        public void stopBlink() {
            this._run = false;
        }

        public void run() {
            this._run = true;
            while (this._run) {
                try {
                    sleep(1000);
                    if (TelnetView.this._handler != null) {
                        TelnetView.this._handler.sendEmptyMessage(0);
                    }
                } catch (InterruptedException e) {
                    e.printStackTrace();
                    return;
                }
            }
        }
    }

    public TelnetView(Context context) {
        super(context);
        initial();
    }

    public TelnetView(Context context, AttributeSet attrs, int defStyle) {
        super(context, attrs, defStyle);
        initial();
    }

    public TelnetView(Context context, AttributeSet attrs) {
        super(context, attrs);
        initial();
    }

    private void startBlink() {
        if (this._handler == null) {
            this._handler = new Handler() {
                public void handleMessage(Message msg) {
                    boolean unused = TelnetView.this._blink = !TelnetView.this._blink;
                    Iterator it = TelnetView.this._blink_list.iterator();
                    while (it.hasNext()) {
                        Position position = (Position) it.next();
                        if (TelnetView.this.bitmapContainsPosition(position)) {
                            TelnetView.this.removePositionBitmap(position);
                        }
                    }
                    TelnetView.this.invalidate();
                }
            };
        }
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

    private void initial() {
    }

    /* access modifiers changed from: protected */
    public void finalize() throws Throwable {
        if (this._blink_thread != null) {
            stopBlink();
        }
        super.finalize();
    }

    public void setFrame(TelnetFrame aTelnetFrame) {
        this._frame = null;
        this._frame = aTelnetFrame;
        this._frame.reloadSpace();
        cleanBitmap();
        invalidate();
    }

    public TelnetFrame getFrame() {
        return this._frame;
    }

    public void setTypeface(Typeface typeface) {
        this._typeface = typeface;
    }

    private int match(int value, int root) {
        return (value / root) * root;
    }

    /* access modifiers changed from: protected */
    public void onMeasure(int widthMeasureSpec, int heightMeasureSpec) {
        int width_measure_value;
        int height_measure_value;
        cleanBitmap();
        int view_width = View.MeasureSpec.getSize(widthMeasureSpec);
        int view_height = View.MeasureSpec.getSize(heightMeasureSpec);
        ViewGroup.LayoutParams layout = getLayoutParams();
        int default_text_size = match((int) TypedValue.applyDimension(2, this.DEFAULT_TEXT_SIZE, getContext().getResources().getDisplayMetrics()), 4);
        this._draw_width = 0;
        this._draw_height = 0;
        this._scale_x = 1.0f;
        this._scale_y = 1.0f;
        this._row = 0;
        this._column = 0;
        if (this._frame == null) {
            super.onMeasure(widthMeasureSpec, heightMeasureSpec);
            return;
        }
        int i = widthMeasureSpec;
        int i2 = heightMeasureSpec;
        this._row = this._frame.getRowSize();
        this._column = 80;
        if (layout.width == -2 && layout.height == -2) {
            this._block_width = (double) (default_text_size / 2);
            this._block_height = (double) match((int) (this._block_width * this._ratio), 2);
            this._draw_width = (int) (this._block_width * ((double) this._column));
            this._draw_height = (int) (this._block_height * ((double) this._row));
            this._scale_x = 1.0f;
            this._scale_y = 1.0f;
            width_measure_value = View.MeasureSpec.makeMeasureSpec(this._draw_width, 1073741824);
            height_measure_value = View.MeasureSpec.makeMeasureSpec(this._draw_height, 1073741824);
        } else if (layout.height == -2) {
            this._block_width = (double) match(view_width / this._column, 2);
            this._block_height = (double) match((int) (this._block_width * this._ratio), 2);
            this._draw_width = (int) (this._block_width * ((double) this._column));
            this._draw_height = (int) (this._block_height * ((double) this._row));
            this._scale_x = ((float) view_width) / ((float) this._draw_width);
            width_measure_value = View.MeasureSpec.makeMeasureSpec(view_width, 1073741824);
            height_measure_value = View.MeasureSpec.makeMeasureSpec(this._draw_height, 1073741824);
        } else if (layout.width == -2) {
            this._block_height = (double) match(view_height / this._row, 2);
            this._block_width = (double) match((int) (this._block_height / this._ratio), 2);
            this._draw_width = (int) (this._block_width * ((double) this._column));
            this._draw_height = (int) (this._block_height * ((double) this._row));
            this._scale_y = ((float) view_height) / ((float) this._draw_height);
            width_measure_value = View.MeasureSpec.makeMeasureSpec(this._draw_width, 1073741824);
            height_measure_value = View.MeasureSpec.makeMeasureSpec(view_height, 1073741824);
        } else {
            this._block_width = (double) match(view_width / this._column, 2);
            this._block_height = (double) match((int) (this._block_width * this._ratio), 2);
            this._draw_width = (int) (this._block_width * ((double) this._column));
            this._draw_height = (int) (this._block_height * ((double) this._row));
            this._scale_x = ((float) view_width) / ((float) this._draw_width);
            this._scale_y = ((float) view_height) / ((float) this._draw_height);
            width_measure_value = View.MeasureSpec.makeMeasureSpec(view_width, 1073741824);
            height_measure_value = View.MeasureSpec.makeMeasureSpec(view_height, 1073741824);
        }
        calculateLayout();
        super.onMeasure(width_measure_value, height_measure_value);
    }

    public void calculateLayout() {
        if (this._frame != null) {
            this._vertical_unit = (this._block_height - ((double) ((int) Math.ceil((double) TypedValue.applyDimension(1, 1.0f, getContext().getResources().getDisplayMetrics()))))) / 8.0d;
            this._horizontal_unit = (this._block_width * 2.0d) / 7.0d;
            this._radius = (double) match((int) (this._block_height / 2.0d), 2);
            if (this._radius > this._block_width) {
                this._radius = this._block_width;
            }
            this._zh_text_size = (double) match((int) (this._radius * 2.0d), 2);
            this._en_text_size = (double) match((int) (this._zh_text_size * 0.8999999761581421d), 2);
        }
        this._bitmap_block_width = this._block_width * ((double) this._bitmap_space_x);
        this._bitmap_block_height = this._block_height * ((double) this._bitmap_space_y);
        reloadDrawer();
    }

    /* access modifiers changed from: protected */
    public void onDraw(Canvas canvas) {
        super.onDraw(canvas);
        canvas.getClipBounds(this._clip);
        canvas.scale(this._scale_x, this._scale_y);
        BitmapSpace new_space = calculateBlock((double) this._clip.left, (double) this._clip.top, (double) this._clip.width(), (double) this._clip.height());
        if (!this._current_space.isEquals(new_space)) {
            setBitmapSpace(new_space);
        }
        int space_left = this._current_space.left;
        int space_right = this._current_space.left + this._current_space.width;
        if (space_right > this._bitmap_space_column) {
            space_right = this._bitmap_space_column;
        }
        int space_top = this._current_space.top;
        int space_bottom = this._current_space.top + this._current_space.height;
        if (space_bottom > this._bitmap_space_row) {
            space_bottom = this._bitmap_space_row;
        }
        for (int row = space_top; row < space_bottom; row++) {
            float origin_y = (float) (((double) row) * this._bitmap_block_height);
            for (int column = space_left; column < space_right; column++) {
                Bitmap bm = getBitmap(row, column);
                if (bm == null) {
                    System.out.println(row + "," + column + " bitmap is null");
                } else {
                    this._matrix.setTranslate((float) (((double) column) * this._bitmap_block_width), origin_y);
                    canvas.drawBitmap(bm, this._matrix, this._paint);
                }
            }
        }
        boolean blink = this._blink_list.size() > 0;
        if (blink && this._blink_thread == null) {
            startBlink();
        }
        if (!blink && this._blink_thread != null) {
            stopBlink();
        }
    }

    private void reloadDrawer() {
        this._drawer.blockWidth = (int) this._block_width;
        this._drawer.blockHeight = (int) this._block_height;
        this._drawer.horizontalUnit = (float) ((int) this._horizontal_unit);
        this._drawer.verticalUnit = (float) ((int) this._vertical_unit);
        this._drawer.radius = (float) ((int) this._radius);
        this._drawer.originX = this._origin_x;
        this._drawer.originY = this._origin_y;
        this._drawer.paint.setAntiAlias(true);
    }

    private void drawTelnet(Canvas canvas, Position aPosition, int rowStart, int rowEnd, int columnStart, int columnEnd) {
        if (this._typeface != null) {
            this._drawer.paint.setTypeface(this._typeface);
        }
        if (this._frame != null) {
            this._drawer.canvas = canvas;
            Rect zh_bounds = new Rect();
            this._drawer.paint.getTextBounds("國", 0, 1, zh_bounds);
            Rect en_bounds = new Rect();
            this._drawer.paint.getTextBounds("D", 0, 1, en_bounds);
            this._drawer.clip = 0;
            double block_offset = Math.ceil(this._block_height - this._zh_text_size);
            double font_offset = Math.ceil((double) (zh_bounds.height() - en_bounds.height()));
            this._drawer.textBottomOffset = (int) Math.ceil((block_offset + font_offset) / 2.0d);
            boolean blink = false;
            for (int row = rowStart; row < rowEnd; row++) {
                int cursor_row = row - rowStart;
                int column = columnStart;
                while (column < columnEnd) {
                    int cursor_column = column - columnStart;
                    if (this._frame.getPositionBlink(row, column)) {
                        blink = true;
                    }
                    byte bit_space = this._frame.getPositionBitSpace(row, column);
                    if (bit_space == 1) {
                        drawSingleBitSpace2_1(row, column, cursor_row, cursor_column);
                        column++;
                    } else if (bit_space == 2 && column > 0) {
                        drawSingleBitSpace2_2(row, column, cursor_row, cursor_column);
                    } else if (bit_space == 3) {
                        drawDoubleBitSpace2_1(row, column, cursor_row, cursor_column);
                    } else if (bit_space != 4 || column <= 0) {
                        drawBitSpace1(row, column, cursor_row, cursor_column);
                    } else {
                        drawDoubleBitSpace2_2(row, column, cursor_row, cursor_column);
                    }
                    column++;
                }
            }
            if (blink) {
                boolean contains_in_blink_link = false;
                Iterator<Position> it = this._blink_list.iterator();
                while (true) {
                    if (it.hasNext()) {
                        if (it.next().isEquals(aPosition)) {
                            contains_in_blink_link = true;
                            break;
                        }
                    } else {
                        break;
                    }
                }
                if (!contains_in_blink_link) {
                    this._blink_list.add(aPosition);
                }
            }
            if (this._draw_separator_line) {
                this._drawer.paint.setColor(1728053247);
                for (int row2 = 0; row2 < this._frame.getRowSize(); row2++) {
                    int position_y = (int) (((double) row2) * this._block_height);
                    canvas.drawLine(0.0f, (float) position_y, 480.0f, (float) position_y, this._drawer.paint);
                }
                for (int column2 = 0; column2 < 80; column2++) {
                    int position_x = (int) (((double) column2) * this._block_width);
                    canvas.drawLine((float) position_x, 0.0f, (float) position_x, 360.0f, this._drawer.paint);
                }
                return;
            }
            return;
        }
        System.out.println("_telnet_model is null");
    }

    /* access modifiers changed from: protected */
    public void onDetachedFromWindow() {
        super.onDetachedFromWindow();
        stopBlink();
    }

    private void cleanBitmap() {
        this._bitmaps = null;
        this._blink_list.clear();
    }

    private void resetBitmap() {
        int i;
        int i2 = 1;
        this._bitmap_space_row = 0;
        this._bitmap_space_column = 0;
        if (this._frame != null) {
            int row_maximum = this._frame.getRowSize();
            int i3 = row_maximum / this._bitmap_space_y;
            if (row_maximum % this._bitmap_space_y > 0) {
                i = 1;
            } else {
                i = 0;
            }
            this._bitmap_space_row = i + i3;
            int i4 = 80 / this._bitmap_space_x;
            if (80 % this._bitmap_space_x <= 0) {
                i2 = 0;
            }
            this._bitmap_space_column = i4 + i2;
        }
        if (this._bitmap_space_row > 0 && this._bitmap_space_column > 0) {
            this._bitmaps = (Bitmap[][]) Array.newInstance(Bitmap.class, new int[]{this._bitmap_space_row, this._bitmap_space_column});
        }
    }

    private Bitmap getBitmap(int row, int column) {
        Bitmap bm = null;
        if (this._bitmaps == null) {
            resetBitmap();
        }
        if (bitmapContainsPosition(row, column) && (bm = this._bitmaps[row][column]) == null) {
            bm = createBitmap();
            this._bitmaps[row][column] = bm;
            Canvas canvas = new Canvas(bm);
            int row_start = row * this._bitmap_space_y;
            int row_end = (row + 1) * this._bitmap_space_y;
            if (row_end > this._frame.getRowSize()) {
                row_end = this._frame.getRowSize();
            }
            int column_start = column * this._bitmap_space_x;
            int column_end = (column + 1) * this._bitmap_space_x;
            if (column_end > 80) {
                column_end = 80;
            }
            drawTelnet(canvas, new Position(row, column), row_start, row_end, column_start, column_end);
        }
        return bm;
    }

    public Bitmap createBitmap() {
        return Bitmap.createBitmap((int) this._bitmap_block_width, (int) this._bitmap_block_height, this._bitmap_config);
    }

    public void setBitmapSpace(BitmapSpace aSpace) {
        if (this._bitmaps == null) {
            resetBitmap();
        }
        int block_right = this._current_space.left + this._current_space.width;
        int block_bottom = this._current_space.top + this._current_space.height;
        for (int row = this._current_space.left; row < block_right; row++) {
            for (int column = this._current_space.top; column < block_bottom; column++) {
                if (!aSpace.contains(row, column)) {
                    removePositionBitmap(row, column);
                }
            }
        }
        this._current_space.set(aSpace);
    }

    public void removePositionBitmap(Position aPosition) {
        removePositionBitmap(aPosition.row, aPosition.column);
    }

    public void removePositionBitmap(int row, int column) {
        if (bitmapContainsPosition(row, column)) {
            this._bitmaps[row][column] = null;
        }
    }

    public BitmapSpace calculateBlock(double left, double top, double width, double height) {
        BitmapSpace block = new BitmapSpace();
        block.left = (int) Math.floor(left / this._bitmap_block_width);
        block.top = (int) Math.floor(top / this._bitmap_block_height);
        block.width = (((int) Math.ceil((left + width) / this._bitmap_block_width)) - block.left) + 1;
        block.height = (((int) Math.ceil((top + height) / this._bitmap_block_height)) - block.top) + 1;
        return block;
    }

    public boolean bitmapContainsPosition(Position aPosition) {
        return bitmapContainsPosition(aPosition.row, aPosition.column);
    }

    public boolean bitmapContainsPosition(int row, int column) {
        return this._bitmaps != null && row >= 0 && row < this._bitmaps.length && column >= 0 && column < this._bitmaps[row].length;
    }

    private void drawBitSpace1(int row, int column, int cursor_row, int cursor_column) {
        boolean z = true;
        this._drawer.paint.setTextSize((float) ((int) this._en_text_size));
        char c = (char) this._frame.getPositionData(row, column);
        int text_color = this._frame.getPositionTextColor(row, column);
        int background_color = this._frame.getPositionBackgroundColor(row, column);
        this._drawer.textColor = text_color;
        this._drawer.backgroundColor = background_color;
        this._drawer.bit = 1;
        this._drawer.paint.setTypeface(this.en_typepace);
        TelnetViewDrawer telnetViewDrawer = this._drawer;
        if (!this._blink || !this._frame.getPositionBlink(row, column)) {
            z = false;
        }
        telnetViewDrawer.blink = z;
        this._drawer.clip = 0;
        this._drawer.drawCharAtPosition(getContext(), cursor_row, cursor_column, c);
    }

    private void drawSingleBitSpace2_1(int row, int column, int cursor_row, int cursor_column) {
        this._drawer.paint.setTextSize((float) ((int) this._zh_text_size));
        char c = B2UEncoder.getInstance().encodeChar((char) ((this._frame.getPositionData(row, column) << 8) + this._frame.getPositionData(row, column + 1)));
        int text_color = this._frame.getPositionTextColor(row, column);
        int background_color = this._frame.getPositionBackgroundColor(row, column);
        this._drawer.textColor = text_color;
        this._drawer.backgroundColor = background_color;
        this._drawer.bit = 2;
        this._drawer.paint.setTypeface(this.zh_typepace);
        this._drawer.blink = this._blink && this._frame.getPositionBlink(row, column);
        this._drawer.clip = 0;
        this._drawer.drawCharAtPosition(getContext(), cursor_row, cursor_column, c);
    }

    private void drawSingleBitSpace2_2(int row, int column, int cursor_row, int cursor_column) {
        this._drawer.paint.setTextSize((float) ((int) this._zh_text_size));
        char c = B2UEncoder.getInstance().encodeChar((char) ((this._frame.getPositionData(row, column - 1) << 8) + this._frame.getPositionData(row, column)));
        int text_color = this._frame.getPositionTextColor(row, column);
        int background_color = this._frame.getPositionBackgroundColor(row, column);
        this._drawer.textColor = text_color;
        this._drawer.backgroundColor = background_color;
        this._drawer.bit = 2;
        this._drawer.paint.setTypeface(this.zh_typepace);
        this._drawer.blink = this._blink && this._frame.getPositionBlink(row, column);
        this._drawer.clip = 0;
        this._drawer.drawCharAtPosition(getContext(), cursor_row, cursor_column - 1, c);
    }

    private void drawDoubleBitSpace2_1(int row, int column, int cursor_row, int cursor_column) {
        this._drawer.paint.setTextSize((float) ((int) this._zh_text_size));
        char c = B2UEncoder.getInstance().encodeChar((char) ((this._frame.getPositionData(row, column) << 8) + this._frame.getPositionData(row, column + 1)));
        int text_color = this._frame.getPositionTextColor(row, column);
        int background_color = this._frame.getPositionBackgroundColor(row, column);
        this._drawer.textColor = text_color;
        this._drawer.backgroundColor = background_color;
        this._drawer.bit = 2;
        this._drawer.paint.setTypeface(this.zh_typepace);
        this._drawer.blink = this._blink && this._frame.getPositionBlink(row, column);
        this._drawer.clip = 1;
        this._drawer.drawCharAtPosition(getContext(), cursor_row, cursor_column, c);
    }

    private void drawDoubleBitSpace2_2(int row, int column, int cursor_row, int cursor_column) {
        this._drawer.paint.setTextSize((float) ((int) this._zh_text_size));
        char c = B2UEncoder.getInstance().encodeChar((char) ((this._frame.getPositionData(row, column - 1) << 8) + this._frame.getPositionData(row, column)));
        int text_color = this._frame.getPositionTextColor(row, column);
        int background_color = this._frame.getPositionBackgroundColor(row, column);
        this._drawer.textColor = text_color;
        this._drawer.backgroundColor = background_color;
        this._drawer.bit = 2;
        this._drawer.paint.setTypeface(this.zh_typepace);
        this._drawer.blink = this._blink && this._frame.getPositionBlink(row, column);
        this._drawer.clip = 2;
        this._drawer.drawCharAtPosition(getContext(), cursor_row, cursor_column - 1, c);
    }
}
