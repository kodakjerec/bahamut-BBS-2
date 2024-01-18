package com.kota.TelnetUI;

import android.annotation.SuppressLint;
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

import androidx.annotation.NonNull;

import com.kota.Telnet.Model.TelnetFrame;
import com.kota.TextEncoder.B2UEncoder;
import java.lang.reflect.Array;
import java.util.Iterator;
import java.util.Vector;

/* loaded from: classes.dex */
public class TelnetView extends View {
    private final float DEFAULT_TEXT_SIZE;
    private double _bitmap_block_height;
    private double _bitmap_block_width;
    private final Bitmap.Config _bitmap_config;
    private int _bitmap_space_column;
    private int _bitmap_space_row;
    private final int _bitmap_space_x;
    private final int _bitmap_space_y;
    private Bitmap[][] _bitmaps;
    private boolean _blink;
    private final Vector<Position> _blink_list;
    private BlinkThread _blink_thread;
    private double _block_height;
    private double _block_width;
    Rect _clip;
    private int _column;
    private final BitmapSpace _current_space;
    private int _draw_height;
    private final boolean _draw_separator_line;
    private int _draw_width;
    private final TelnetViewDrawer _drawer;
    private double _en_text_size;
    private TelnetFrame _frame;
    private Handler _handler;
    private double _horizontal_unit;
    Matrix _matrix;
    private final int _origin_x;
    private final int _origin_y;
    Paint _paint;
    private double _radius;
    private final double _ratio;
    private int _row;
    private float _scale_x;
    private float _scale_y;
    private Typeface _typeface;
    private double _vertical_unit;
    private double _zh_text_size;
    Typeface en_typepace;
    Typeface zh_typepace;

    public static class BitmapSpace {
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

        @NonNull
        public String toString() {
            return "(" + this.left + " , " + this.top + ") (" + this.width + " , " + this.height + ")";
        }
    }

    public static class Position {
        public int column;
        public int row;

        public Position(int aRow, int aColumn) {
            this.row = 0;
            this.column = 0;
            this.row = aRow;
            this.column = aColumn;
        }

        public boolean isEquals(Position aPosition) {
            return this.row == aPosition.row && this.column == aPosition.column;
        }
    }

    public class BlinkThread extends Thread {
        boolean _run;

        private BlinkThread() {
            this._run = true;
        }

        public void stopBlink() {
            this._run = false;
        }

        @Override // java.lang.Thread, java.lang.Runnable
        public void run() {
            this._run = true;
            while (this._run) {
                try {
                    sleep(1000L);
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
        this.DEFAULT_TEXT_SIZE = 20.0f;
        this._frame = null;
        this._row = 24;
        this._column = 80;
        this._draw_width = 0;
        this._draw_height = 0;
        this._ratio = 2.5d;
        this._block_width = 6.0d;
        this._block_height = 15.0d;
        this._zh_text_size = 12.0d;
        this._en_text_size = 12.0d;
        this._origin_x = 0;
        this._origin_y = 0;
        this._radius = 0.0d;
        this._vertical_unit = 2.0d;
        this._horizontal_unit = 1.5d;
        this._typeface = null;
        this._draw_separator_line = false;
        this._blink = false;
        this._blink_thread = null;
        this._handler = null;
        this._bitmaps = null;
        this._scale_x = 1.0f;
        this._scale_y = 1.0f;
        this._bitmap_space_x = 8;
        this._bitmap_space_y = 4;
        this._bitmap_space_row = 0;
        this._bitmap_space_column = 0;
        this._bitmap_block_width = 0.0d;
        this._bitmap_block_height = 0.0d;
        this._bitmap_config = Bitmap.Config.RGB_565;
        this._current_space = new BitmapSpace();
        this._blink_list = new Vector<>();
        this._drawer = new TelnetViewDrawer();
        this.zh_typepace = Typeface.create("DEFAULT", Typeface.NORMAL);
        this.en_typepace = Typeface.create("MONOSPACE", Typeface.NORMAL);
        this._clip = new Rect();
        this._paint = new Paint();
        this._matrix = new Matrix();
        initial();
    }

    public TelnetView(Context context, AttributeSet attrs, int defStyle) {
        super(context, attrs, defStyle);
        this.DEFAULT_TEXT_SIZE = 20.0f;
        this._frame = null;
        this._row = 24;
        this._column = 80;
        this._draw_width = 0;
        this._draw_height = 0;
        this._ratio = 2.5d;
        this._block_width = 6.0d;
        this._block_height = 15.0d;
        this._zh_text_size = 12.0d;
        this._en_text_size = 12.0d;
        this._origin_x = 0;
        this._origin_y = 0;
        this._radius = 0.0d;
        this._vertical_unit = 2.0d;
        this._horizontal_unit = 1.5d;
        this._typeface = null;
        this._draw_separator_line = false;
        this._blink = false;
        this._blink_thread = null;
        this._handler = null;
        this._bitmaps = null;
        this._scale_x = 1.0f;
        this._scale_y = 1.0f;
        this._bitmap_space_x = 8;
        this._bitmap_space_y = 4;
        this._bitmap_space_row = 0;
        this._bitmap_space_column = 0;
        this._bitmap_block_width = 0.0d;
        this._bitmap_block_height = 0.0d;
        this._bitmap_config = Bitmap.Config.RGB_565;
        this._current_space = new BitmapSpace();
        this._blink_list = new Vector<>();
        this._drawer = new TelnetViewDrawer();
        this.zh_typepace = Typeface.create("DEFAULT", Typeface.NORMAL);
        this.en_typepace = Typeface.create("MONOSPACE", Typeface.NORMAL);
        this._clip = new Rect();
        this._paint = new Paint();
        this._matrix = new Matrix();
        initial();
    }

    public TelnetView(Context context, AttributeSet attrs) {
        super(context, attrs);
        this.DEFAULT_TEXT_SIZE = 20.0f;
        this._frame = null;
        this._row = 24;
        this._column = 80;
        this._draw_width = 0;
        this._draw_height = 0;
        this._ratio = 2.5d;
        this._block_width = 6.0d;
        this._block_height = 15.0d;
        this._zh_text_size = 12.0d;
        this._en_text_size = 12.0d;
        this._origin_x = 0;
        this._origin_y = 0;
        this._radius = 0.0d;
        this._vertical_unit = 2.0d;
        this._horizontal_unit = 1.5d;
        this._typeface = null;
        this._draw_separator_line = false;
        this._blink = false;
        this._blink_thread = null;
        this._handler = null;
        this._bitmaps = null;
        this._scale_x = 1.0f;
        this._scale_y = 1.0f;
        this._bitmap_space_x = 8;
        this._bitmap_space_y = 4;
        this._bitmap_space_row = 0;
        this._bitmap_space_column = 0;
        this._bitmap_block_width = 0.0d;
        this._bitmap_block_height = 0.0d;
        this._bitmap_config = Bitmap.Config.RGB_565;
        this._current_space = new BitmapSpace();
        this._blink_list = new Vector<>();
        this._drawer = new TelnetViewDrawer();
        this.zh_typepace = Typeface.create("DEFAULT", Typeface.NORMAL);
        this.en_typepace = Typeface.create("MONOSPACE", Typeface.NORMAL);
        this._clip = new Rect();
        this._paint = new Paint();
        this._matrix = new Matrix();
        initial();
    }

    @SuppressLint("HandlerLeak")
    private void startBlink() {
        if (this._handler == null) {
            this._handler = new Handler() { // from class: com.kumi.TelnetUI.TelnetView.1
                @Override // android.os.Handler
                public void handleMessage(Message msg) {
                    TelnetView.this._blink = !TelnetView.this._blink;
                    for (Position position : TelnetView.this._blink_list) {
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

    protected void finalize() throws Throwable {
        if (this._blink_thread != null) {
            stopBlink();
        }
        super.finalize();
    }

    public void setFrame(TelnetFrame aTelnetFrame) {
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

    @Override // android.view.View
    protected void onMeasure(int widthMeasureSpec, int heightMeasureSpec) {
        int width_measure_value;
        int height_measure_value;
        cleanBitmap();
        int view_width = View.MeasureSpec.getSize(widthMeasureSpec);
        int view_height = View.MeasureSpec.getSize(heightMeasureSpec);
        ViewGroup.LayoutParams layout = getLayoutParams();
        int default_text_size = match((int) TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_SP, this.DEFAULT_TEXT_SIZE, getContext().getResources().getDisplayMetrics()), 4);
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
        this._row = this._frame.getRowSize();
        this._column = 80;
        if (layout.width == -2 && layout.height == -2) {
            this._block_width = default_text_size / 2;
            this._block_height = match((int) (this._block_width * this._ratio), 2);
            this._draw_width = (int) (this._block_width * this._column);
            this._draw_height = (int) (this._block_height * this._row);
            this._scale_x = 1.0f;
            this._scale_y = 1.0f;
            width_measure_value = View.MeasureSpec.makeMeasureSpec(this._draw_width, MeasureSpec.EXACTLY);
            height_measure_value = View.MeasureSpec.makeMeasureSpec(this._draw_height, MeasureSpec.EXACTLY);
        } else if (layout.height == -2) {
            this._block_width = match(view_width / this._column, 2);
            this._block_height = match((int) (this._block_width * this._ratio), 2);
            this._draw_width = (int) (this._block_width * this._column);
            this._draw_height = (int) (this._block_height * this._row);
            this._scale_x = view_width / this._draw_width;
            width_measure_value = View.MeasureSpec.makeMeasureSpec(view_width, MeasureSpec.EXACTLY);
            height_measure_value = View.MeasureSpec.makeMeasureSpec(this._draw_height, MeasureSpec.EXACTLY);
        } else if (layout.width == -2) {
            this._block_height = match(view_height / this._row, 2);
            this._block_width = match((int) (this._block_height / this._ratio), 2);
            this._draw_width = (int) (this._block_width * this._column);
            this._draw_height = (int) (this._block_height * this._row);
            this._scale_y = view_height / this._draw_height;
            width_measure_value = View.MeasureSpec.makeMeasureSpec(this._draw_width, MeasureSpec.EXACTLY);
            height_measure_value = View.MeasureSpec.makeMeasureSpec(view_height, MeasureSpec.EXACTLY);
        } else {
            this._block_width = match(view_width / this._column, 2);
            this._block_height = match((int) (this._block_width * this._ratio), 2);
            this._draw_width = (int) (this._block_width * this._column);
            this._draw_height = (int) (this._block_height * this._row);
            this._scale_x = view_width / this._draw_width;
            this._scale_y = view_height / this._draw_height;
            width_measure_value = View.MeasureSpec.makeMeasureSpec(view_width, MeasureSpec.EXACTLY);
            height_measure_value = View.MeasureSpec.makeMeasureSpec(view_height, MeasureSpec.EXACTLY);
        }
        calculateLayout();
        super.onMeasure(width_measure_value, height_measure_value);
    }

    public void calculateLayout() {
        if (this._frame != null) {
            int unit_dp = (int) Math.ceil(TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_DIP, 1.0f, getContext().getResources().getDisplayMetrics()));
            this._vertical_unit = (this._block_height - unit_dp) / 8.0d;
            this._horizontal_unit = (this._block_width * 2.0d) / 7.0d;
            this._radius = match((int) (this._block_height / 2.0d), 2);
            if (this._radius > this._block_width) {
                this._radius = this._block_width;
            }
            this._zh_text_size = match((int) (this._radius * 2.0d), 2);
            this._en_text_size = match((int) (this._zh_text_size * 0.8999999761581421d), 2);
        }
        this._bitmap_block_width = this._block_width * this._bitmap_space_x;
        this._bitmap_block_height = this._block_height * this._bitmap_space_y;
        reloadDrawer();
    }

    @Override // android.view.View
    protected void onDraw(Canvas canvas) {
        super.onDraw(canvas);
        canvas.getClipBounds(this._clip);
        canvas.scale(this._scale_x, this._scale_y);
        BitmapSpace new_space = calculateBlock(this._clip.left, this._clip.top, this._clip.width(), this._clip.height());
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
            float origin_y = (float) (row * this._bitmap_block_height);
            for (int column = space_left; column < space_right; column++) {
                Bitmap bm = getBitmap(row, column);
                if (bm == null) {
                    System.out.println(row + "," + column + " bitmap is null");
                } else {
                    float origin_x = (float) (column * this._bitmap_block_width);
                    this._matrix.setTranslate(origin_x, origin_y);
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
        this._drawer.horizontalUnit = (int) this._horizontal_unit;
        this._drawer.verticalUnit = (int) this._vertical_unit;
        this._drawer.radius = (int) this._radius;
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
            this._drawer.clip = (byte) 0;
            double block_offset = Math.ceil(this._block_height - this._zh_text_size);
            double font_offset = Math.ceil(zh_bounds.height() - en_bounds.height());
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
                    } else if (bit_space == 4 && column > 0) {
                        drawDoubleBitSpace2_2(row, column, cursor_row, cursor_column);
                    } else {
                        drawBitSpace1(row, column, cursor_row, cursor_column);
                    }
                    column++;
                }
            }
            if (blink) {
                boolean contains_in_blink_link = false;
                Iterator<Position> it = this._blink_list.iterator();
                while (true) {
                    if (!it.hasNext()) {
                        break;
                    }
                    Position position = it.next();
                    if (position.isEquals(aPosition)) {
                        contains_in_blink_link = true;
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
                    int position_y = (int) (row2 * this._block_height);
                    canvas.drawLine(0.0f, position_y, 480.0f, position_y, this._drawer.paint);
                }
                for (int column2 = 0; column2 < 80; column2++) {
                    int position_x = (int) (column2 * this._block_width);
                    canvas.drawLine(position_x, 0.0f, position_x, 360.0f, this._drawer.paint);
                }
                return;
            }
            return;
        }
        System.out.println("_telnet_model is null");
    }

    @Override // android.view.View
    protected void onDetachedFromWindow() {
        super.onDetachedFromWindow();
        stopBlink();
    }

    private void cleanBitmap() {
        this._bitmaps = null;
        this._blink_list.clear();
    }

    private void resetBitmap() {
        this._bitmap_space_row = 0;
        this._bitmap_space_column = 0;
        if (this._frame != null) {
            int row_maximum = this._frame.getRowSize();
            this._bitmap_space_row = (row_maximum % this._bitmap_space_y > 0 ? 1 : 0) + (row_maximum / this._bitmap_space_y);
            this._bitmap_space_column = (80 / this._bitmap_space_x) + (80 % this._bitmap_space_x == 0 ? 0 : 1);
        }
        if (this._bitmap_space_row > 0 && this._bitmap_space_column > 0) {
            this._bitmaps = (Bitmap[][]) Array.newInstance(Bitmap.class, this._bitmap_space_row, this._bitmap_space_column);
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
        this._drawer.paint.setTextSize((int) this._en_text_size);
        char c = (char) this._frame.getPositionData(row, column);
        int text_color = this._frame.getPositionTextColor(row, column);
        int background_color = this._frame.getPositionBackgroundColor(row, column);
        this._drawer.textColor = text_color;
        this._drawer.backgroundColor = background_color;
        this._drawer.bit = 1;
        this._drawer.paint.setTypeface(this.en_typepace);
        if (!this._blink || !this._frame.getPositionBlink(row, column)) {
            z = false;
        }
        this._drawer.blink = z;
        this._drawer.clip = (byte) 0;
        this._drawer.drawCharAtPosition(getContext(), cursor_row, cursor_column, c);
    }

    private void drawSingleBitSpace2_1(int row, int column, int cursor_row, int cursor_column) {
        this._drawer.paint.setTextSize((int) this._zh_text_size);
        int upper = this._frame.getPositionData(row, column);
        int lower = this._frame.getPositionData(row, column + 1);
        int char_data = (upper << 8) + lower;
        char c = B2UEncoder.getInstance().encodeChar((char) char_data);
        int text_color = this._frame.getPositionTextColor(row, column);
        int background_color = this._frame.getPositionBackgroundColor(row, column);
        this._drawer.textColor = text_color;
        this._drawer.backgroundColor = background_color;
        this._drawer.bit = 2;
        this._drawer.paint.setTypeface(this.zh_typepace);
        this._drawer.blink = this._blink && this._frame.getPositionBlink(row, column);
        this._drawer.clip = (byte) 0;
        this._drawer.drawCharAtPosition(getContext(), cursor_row, cursor_column, c);
    }

    private void drawSingleBitSpace2_2(int row, int column, int cursor_row, int cursor_column) {
        this._drawer.paint.setTextSize((int) this._zh_text_size);
        int lower = this._frame.getPositionData(row, column);
        int upper = this._frame.getPositionData(row, column - 1);
        int char_data = (upper << 8) + lower;
        char c = B2UEncoder.getInstance().encodeChar((char) char_data);
        int text_color = this._frame.getPositionTextColor(row, column);
        int background_color = this._frame.getPositionBackgroundColor(row, column);
        this._drawer.textColor = text_color;
        this._drawer.backgroundColor = background_color;
        this._drawer.bit = 2;
        this._drawer.paint.setTypeface(this.zh_typepace);
        this._drawer.blink = this._blink && this._frame.getPositionBlink(row, column);
        this._drawer.clip = (byte) 0;
        this._drawer.drawCharAtPosition(getContext(), cursor_row, cursor_column - 1, c);
    }

    private void drawDoubleBitSpace2_1(int row, int column, int cursor_row, int cursor_column) {
        this._drawer.paint.setTextSize((int) this._zh_text_size);
        int upper = this._frame.getPositionData(row, column);
        int lower = this._frame.getPositionData(row, column + 1);
        int char_data = (upper << 8) + lower;
        char c = B2UEncoder.getInstance().encodeChar((char) char_data);
        int text_color = this._frame.getPositionTextColor(row, column);
        int background_color = this._frame.getPositionBackgroundColor(row, column);
        this._drawer.textColor = text_color;
        this._drawer.backgroundColor = background_color;
        this._drawer.bit = 2;
        this._drawer.paint.setTypeface(this.zh_typepace);
        this._drawer.blink = this._blink && this._frame.getPositionBlink(row, column);
        this._drawer.clip = (byte) 1;
        this._drawer.drawCharAtPosition(getContext(), cursor_row, cursor_column, c);
    }

    private void drawDoubleBitSpace2_2(int row, int column, int cursor_row, int cursor_column) {
        this._drawer.paint.setTextSize((int) this._zh_text_size);
        int lower = this._frame.getPositionData(row, column);
        int upper = this._frame.getPositionData(row, column - 1);
        int char_data = (upper << 8) + lower;
        char c = B2UEncoder.getInstance().encodeChar((char) char_data);
        int text_color = this._frame.getPositionTextColor(row, column);
        int background_color = this._frame.getPositionBackgroundColor(row, column);
        this._drawer.textColor = text_color;
        this._drawer.backgroundColor = background_color;
        this._drawer.bit = 2;
        this._drawer.paint.setTypeface(this.zh_typepace);
        this._drawer.blink = this._blink && this._frame.getPositionBlink(row, column);
        this._drawer.clip = (byte) 2;
        this._drawer.drawCharAtPosition(getContext(), cursor_row, cursor_column - 1, c);
    }
}