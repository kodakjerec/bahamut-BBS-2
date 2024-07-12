package com.kota.TelnetUI;

import static android.view.ViewGroup.LayoutParams.WRAP_CONTENT;

import android.annotation.SuppressLint;
import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.Matrix;
import android.graphics.Paint;
import android.graphics.Rect;
import android.graphics.Typeface;
import android.os.Handler;
import android.os.Looper;
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
    private int _row;
    private float _scale_x;
    private float _scale_y;
    private Typeface _typeface;
    private double _vertical_unit;
    private double _zh_text_size;
    Typeface enTypePace;
    Typeface zhTypePace;

    public static class BitmapSpace {
        public int height;
        public int left;
        public int top;
        public int width;

        private BitmapSpace() {
            left = 0;
            top = 0;
            width = 0;
            height = 0;
        }

        public boolean contains(int row, int column) {
            return row >= left && row < left + width && column >= top && column < top + height;
        }

        public void set(BitmapSpace block) {
            left = block.left;
            top = block.top;
            width = block.width;
            height = block.height;
        }

        public boolean isEquals(BitmapSpace block) {
            return left == block.left && top == block.top && width == block.width && height == block.height;
        }

        @NonNull
        public String toString() {
            return "(" + left + " , " + top + ") (" + width + " , " + height + ")";
        }
    }

    public static class Position {
        public int column;
        public int row;

        public Position(int aRow, int aColumn) {
            row = aRow;
            column = aColumn;
        }

        public boolean isEquals(Position aPosition) {
            return row == aPosition.row && column == aPosition.column;
        }
    }

    public class BlinkThread extends Thread {
        boolean _run;

        private BlinkThread() {
            _run = true;
        }

        public void stopBlink() {
            _run = false;
        }

        @Override // java.lang.Thread, java.lang.Runnable
        public void run() {
            _run = true;
            while (_run) {
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
        DEFAULT_TEXT_SIZE = 20.0f;
        _frame = null;
        _row = 24;
        _column = 80;
        _draw_width = 0;
        _draw_height = 0;
        _block_width = 6.0d;
        _block_height = 15.0d;
        _zh_text_size = 12.0d;
        _en_text_size = 12.0d;
        _origin_x = 0;
        _origin_y = 0;
        _radius = 0.0d;
        _vertical_unit = 2.0d;
        _horizontal_unit = 1.5d;
        _typeface = null;
        _draw_separator_line = false;
        _blink = false;
        _blink_thread = null;
        _handler = null;
        _bitmaps = null;
        _scale_x = 1.0f;
        _scale_y = 1.0f;
        _bitmap_space_x = 8;
        _bitmap_space_y = 4;
        _bitmap_space_row = 0;
        _bitmap_space_column = 0;
        _bitmap_block_width = 0.0d;
        _bitmap_block_height = 0.0d;
        _bitmap_config = Bitmap.Config.RGB_565;
        _current_space = new BitmapSpace();
        _blink_list = new Vector<>();
        _drawer = new TelnetViewDrawer();
        zhTypePace = Typeface.create("DEFAULT", Typeface.NORMAL);
        enTypePace = Typeface.create("MONOSPACE", Typeface.NORMAL);
        _clip = new Rect();
        _paint = new Paint();
        _matrix = new Matrix();
        initial();
    }

    public TelnetView(Context context, AttributeSet attrs, int defStyle) {
        super(context, attrs, defStyle);
        DEFAULT_TEXT_SIZE = 20.0f;
        _frame = null;
        _row = 24;
        _column = 80;
        _draw_width = 0;
        _draw_height = 0;
        _block_width = 6.0d;
        _block_height = 15.0d;
        _zh_text_size = 12.0d;
        _en_text_size = 12.0d;
        _origin_x = 0;
        _origin_y = 0;
        _radius = 0.0d;
        _vertical_unit = 2.0d;
        _horizontal_unit = 1.5d;
        _typeface = null;
        _draw_separator_line = false;
        _blink = false;
        _blink_thread = null;
        _handler = null;
        _bitmaps = null;
        _scale_x = 1.0f;
        _scale_y = 1.0f;
        _bitmap_space_x = 8;
        _bitmap_space_y = 4;
        _bitmap_space_row = 0;
        _bitmap_space_column = 0;
        _bitmap_block_width = 0.0d;
        _bitmap_block_height = 0.0d;
        _bitmap_config = Bitmap.Config.RGB_565;
        _current_space = new BitmapSpace();
        _blink_list = new Vector<>();
        _drawer = new TelnetViewDrawer();
        zhTypePace = Typeface.create("DEFAULT", Typeface.NORMAL);
        enTypePace = Typeface.create("MONOSPACE", Typeface.NORMAL);
        _clip = new Rect();
        _paint = new Paint();
        _matrix = new Matrix();
        initial();
    }

    public TelnetView(Context context, AttributeSet attrs) {
        super(context, attrs);
        DEFAULT_TEXT_SIZE = 20.0f;
        _frame = null;
        _row = 24;
        _column = 80;
        _draw_width = 0;
        _draw_height = 0;
        _block_width = 6.0d;
        _block_height = 15.0d;
        _zh_text_size = 12.0d;
        _en_text_size = 12.0d;
        _origin_x = 0;
        _origin_y = 0;
        _radius = 0.0d;
        _vertical_unit = 2.0d;
        _horizontal_unit = 1.5d;
        _typeface = null;
        _draw_separator_line = false;
        _blink = false;
        _blink_thread = null;
        _handler = null;
        _bitmaps = null;
        _scale_x = 1.0f;
        _scale_y = 1.0f;
        _bitmap_space_x = 8;
        _bitmap_space_y = 4;
        _bitmap_space_row = 0;
        _bitmap_space_column = 0;
        _bitmap_block_width = 0.0d;
        _bitmap_block_height = 0.0d;
        _bitmap_config = Bitmap.Config.RGB_565;
        _current_space = new BitmapSpace();
        _blink_list = new Vector<>();
        _drawer = new TelnetViewDrawer();
        zhTypePace = Typeface.create("DEFAULT", Typeface.NORMAL);
        enTypePace = Typeface.create("MONOSPACE", Typeface.NORMAL);
        _clip = new Rect();
        _paint = new Paint();
        _matrix = new Matrix();
        initial();
    }

    @SuppressLint("HandlerLeak")
    private void startBlink() {
        if (_handler == null) {
            _handler = new Handler(Looper.getMainLooper()) { // from class: com.kota.TelnetUI.TelnetView.1
                @Override // android.os.Handler
                public void handleMessage(@NonNull Message msg) {
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
        if (_blink_thread == null) {
            _blink_thread = new BlinkThread();
            _blink_thread.start();
        }
    }

    private void stopBlink() {
        _handler = null;
        if (_blink_thread != null) {
            _blink_thread.stopBlink();
            _blink_thread = null;
        }
    }

    private void initial() {
    }

    protected void finalize() throws Throwable {
        if (_blink_thread != null) {
            stopBlink();
        }
        super.finalize();
    }

    public void setFrame(TelnetFrame aTelnetFrame) {
        _frame = aTelnetFrame;
        _frame.reloadSpace();
        cleanBitmap();
        invalidate();
    }

    public TelnetFrame getFrame() {
        return _frame;
    }

    public void setTypeface(Typeface typeface) {
        _typeface = typeface;
    }

    @Override // android.view.View
    protected void onMeasure(int widthMeasureSpec, int heightMeasureSpec) {
        int width_measure_value;
        int height_measure_value;
        cleanBitmap();
        int view_width = View.MeasureSpec.getSize(widthMeasureSpec);
        int view_height = View.MeasureSpec.getSize(heightMeasureSpec);
        ViewGroup.LayoutParams layout = getLayoutParams();
        double default_text_size = TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_SP, DEFAULT_TEXT_SIZE, getContext().getResources().getDisplayMetrics());
        _draw_width = 0;
        _draw_height = 0;
        _scale_x = 1.0f;
        _scale_y = 1.0f;
        _row = 0;
        _column = 0;
        if (_frame == null) {
            super.onMeasure(widthMeasureSpec, heightMeasureSpec);
            return;
        }
        _row = _frame.getRowSize();
        _column = 80;
        // 螢幕寬度/每行字元數 = 得到雙字元寬度(預估)
        // => /2 得到單字元寬度
        // => *2 得到雙字元寬度(精準)
        double _ratio = 2.5d;
        if (layout.width == WRAP_CONTENT && layout.height == WRAP_CONTENT) {
            _block_width = (int)(default_text_size / 2/2*2);
            _block_height = _block_width * _ratio;
            _draw_width = (int) (_block_width * _column);
            _draw_height = (int) (_block_height * _row);
            _scale_x = 1.0f;
            _scale_y = 1.0f;
            width_measure_value = View.MeasureSpec.makeMeasureSpec(_draw_width, MeasureSpec.EXACTLY);
            height_measure_value = View.MeasureSpec.makeMeasureSpec(_draw_height, MeasureSpec.EXACTLY);
        } else if (layout.height == WRAP_CONTENT) {
            _block_width = (int)(view_width / _column/2*2);
            _block_height = _block_width * _ratio;
            _draw_width = (int) (_block_width * _column);
            _draw_height = (int) (_block_height * _row);
            _scale_x = (float)view_width / _draw_width;
            width_measure_value = View.MeasureSpec.makeMeasureSpec(view_width, MeasureSpec.EXACTLY);
            height_measure_value = View.MeasureSpec.makeMeasureSpec(_draw_height, MeasureSpec.EXACTLY);
        } else if (layout.width == WRAP_CONTENT) {
            _block_width = (int)(_block_height / _ratio /2*2);
            _block_height = (double) view_height / _row;
            _draw_width = (int) (_block_width * _column);
            _draw_height = (int) (_block_height * _row);
            _scale_y = (float)view_height / _draw_height;
            width_measure_value = View.MeasureSpec.makeMeasureSpec(_draw_width, MeasureSpec.EXACTLY);
            height_measure_value = View.MeasureSpec.makeMeasureSpec(view_height, MeasureSpec.EXACTLY);
        } else {
            _block_width = (double) view_width / _column/2*2;
            _block_height = _block_width * _ratio;
            _draw_width = (int) (_block_width * _column);
            _draw_height = (int) (_block_height * _row);
            _scale_x = (float)view_width / _draw_width;
            _scale_y = (float)view_height / _draw_height;
            width_measure_value = View.MeasureSpec.makeMeasureSpec(view_width, MeasureSpec.EXACTLY);
            height_measure_value = View.MeasureSpec.makeMeasureSpec(view_height, MeasureSpec.EXACTLY);
        }
        calculateLayout();
        super.onMeasure(width_measure_value, height_measure_value);
    }

    public void calculateLayout() {
        if (_frame != null) {
            int unit_dp = (int) Math.ceil(TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_DIP, 1.0f, getContext().getResources().getDisplayMetrics()));
            _vertical_unit = (_block_height - unit_dp) / 8.0d;
            _horizontal_unit = (_block_width * 2.0d) / 7.0d;
            _radius = _block_height / 2.0d;
            if (_radius > _block_width) {
                _radius = _block_width;
            }
            _zh_text_size = _radius * 2.0d;
            _en_text_size = _zh_text_size * 0.8999999761581421d;
        }
        _bitmap_block_width = _block_width * _bitmap_space_x;
        _bitmap_block_height = _block_height * _bitmap_space_y;
        reloadDrawer();
    }

    @Override // android.view.View
    protected void onDraw(@NonNull Canvas canvas) {
        super.onDraw(canvas);
        canvas.getClipBounds(_clip);
        canvas.scale(_scale_x, _scale_y);
        BitmapSpace new_space = calculateBlock(_clip.left, _clip.top, _clip.width(), _clip.height());
        if (!_current_space.isEquals(new_space)) {
            setBitmapSpace(new_space);
        }
        int space_left = _current_space.left;
        int space_right = _current_space.left + _current_space.width;
        if (space_right > _bitmap_space_column) {
            space_right = _bitmap_space_column;
        }
        int space_top = _current_space.top;
        int space_bottom = _current_space.top + _current_space.height;
        if (space_bottom > _bitmap_space_row) {
            space_bottom = _bitmap_space_row;
        }
        for (int row = space_top; row < space_bottom; row++) {
            float origin_y = (float) (row * _bitmap_block_height);
            for (int column = space_left; column < space_right; column++) {
                Bitmap bm = getBitmap(row, column);
                if (bm != null) {
                    float origin_x = (float) (column * _bitmap_block_width);
                    _matrix.setTranslate(origin_x, origin_y);
                    canvas.drawBitmap(bm, _matrix, _paint);
                }
            }
        }
        boolean blink = _blink_list.size() > 0;
        if (blink && _blink_thread == null) {
            startBlink();
        }
        if (!blink && _blink_thread != null) {
            stopBlink();
        }
    }

    private void reloadDrawer() {
        _drawer.blockWidth = _block_width;
        _drawer.blockHeight = _block_height;
        _drawer.horizontalUnit = _horizontal_unit;
        _drawer.verticalUnit = _vertical_unit;
        _drawer.radius = _radius;
        _drawer.originX = _origin_x;
        _drawer.originY = _origin_y;
        _drawer.paint.setAntiAlias(true);
    }

    private void drawTelnet(Canvas canvas, Position aPosition, int rowStart, int rowEnd, int columnStart, int columnEnd) {
        if (_typeface != null) {
            _drawer.paint.setTypeface(_typeface);
        }
        if (_frame != null) {
            _drawer.canvas = canvas;
            Rect zh_bounds = new Rect();
            _drawer.paint.getTextBounds("國", 0, 1, zh_bounds);
            Rect en_bounds = new Rect();
            _drawer.paint.getTextBounds("D", 0, 1, en_bounds);
            _drawer.clip = (byte) 0;
            double block_offset = Math.ceil(_block_height - _zh_text_size);
            double font_offset = zh_bounds.height() - en_bounds.height();
            _drawer.textBottomOffset = (int) Math.ceil((block_offset + font_offset) / 2.0d);
            boolean blink = false;
            for (int row = rowStart; row < rowEnd; row++) {
                int cursor_row = row - rowStart;
                int column = columnStart;
                while (column < columnEnd) {
                    int cursor_column = column - columnStart;
                    if (_frame.getPositionBlink(row, column)) {
                        blink = true;
                    }
                    byte bit_space = _frame.getPositionBitSpace(row, column);
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
                Iterator<Position> it = _blink_list.iterator();
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
                    _blink_list.add(aPosition);
                }
            }
            if (_draw_separator_line) {
                _drawer.paint.setColor(1728053247);
                for (int row2 = 0; row2 < _frame.getRowSize(); row2++) {
                    int position_y = (int) (row2 * _block_height);
                    canvas.drawLine(0.0f, position_y, 480.0f, position_y, _drawer.paint);
                }
                for (int column2 = 0; column2 < 80; column2++) {
                    int position_x = (int) (column2 * _block_width);
                    canvas.drawLine(position_x, 0.0f, position_x, 360.0f, _drawer.paint);
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
        _bitmaps = null;
        _blink_list.clear();
    }

    private void resetBitmap() {
        _bitmap_space_row = 0;
        _bitmap_space_column = 0;
        if (_frame != null) {
            int row_maximum = _frame.getRowSize();
            _bitmap_space_row = (row_maximum % _bitmap_space_y > 0 ? 1 : 0) + (row_maximum / _bitmap_space_y);
            _bitmap_space_column = (80 / _bitmap_space_x) + (80 % _bitmap_space_x == 0 ? 0 : 1);
        }
        if (_bitmap_space_row > 0 && _bitmap_space_column > 0) {
            _bitmaps = (Bitmap[][]) Array.newInstance(Bitmap.class, _bitmap_space_row, _bitmap_space_column);
        }
    }

    private Bitmap getBitmap(int row, int column) {
        Bitmap bm = null;
        if (_bitmaps == null) {
            resetBitmap();
        }
        if (bitmapContainsPosition(row, column) && (bm = _bitmaps[row][column]) == null) {
            bm = createBitmap();
            _bitmaps[row][column] = bm;
            Canvas canvas = new Canvas(bm);
            int row_start = row * _bitmap_space_y;
            int row_end = (row + 1) * _bitmap_space_y;
            if (row_end > _frame.getRowSize()) {
                row_end = _frame.getRowSize();
            }
            int column_start = column * _bitmap_space_x;
            int column_end = (column + 1) * _bitmap_space_x;
            if (column_end > 80) {
                column_end = 80;
            }
            drawTelnet(canvas, new Position(row, column), row_start, row_end, column_start, column_end);
        }
        return bm;
    }

    public Bitmap createBitmap() {
        return Bitmap.createBitmap((int) _bitmap_block_width, (int) _bitmap_block_height, _bitmap_config);
    }

    public void setBitmapSpace(BitmapSpace aSpace) {
        if (_bitmaps == null) {
            resetBitmap();
        }
        int block_right = _current_space.left + _current_space.width;
        int block_bottom = _current_space.top + _current_space.height;
        for (int row = _current_space.left; row < block_right; row++) {
            for (int column = _current_space.top; column < block_bottom; column++) {
                if (!aSpace.contains(row, column)) {
                    removePositionBitmap(row, column);
                }
            }
        }
        _current_space.set(aSpace);
    }

    public void removePositionBitmap(Position aPosition) {
        removePositionBitmap(aPosition.row, aPosition.column);
    }

    public void removePositionBitmap(int row, int column) {
        if (bitmapContainsPosition(row, column)) {
            _bitmaps[row][column] = null;
        }
    }

    public BitmapSpace calculateBlock(double left, double top, double width, double height) {
        BitmapSpace block = new BitmapSpace();
        block.left = (int) Math.floor(left / _bitmap_block_width);
        block.top = (int) Math.floor(top / _bitmap_block_height);
        block.width = (((int) Math.ceil((left + width) / _bitmap_block_width)) - block.left) + 1;
        block.height = (((int) Math.ceil((top + height) / _bitmap_block_height)) - block.top) + 1;
        return block;
    }

    public boolean bitmapContainsPosition(Position aPosition) {
        return bitmapContainsPosition(aPosition.row, aPosition.column);
    }

    public boolean bitmapContainsPosition(int row, int column) {
        return _bitmaps != null && row >= 0 && row < _bitmaps.length && column >= 0 && column < _bitmaps[row].length;
    }

    private void drawBitSpace1(int row, int column, int cursor_row, int cursor_column) {
        boolean z = true;
        _drawer.paint.setTextSize((int) _en_text_size);
        char c = (char) _frame.getPositionData(row, column);
        int text_color = _frame.getPositionTextColor(row, column);
        int background_color = _frame.getPositionBackgroundColor(row, column);
        _drawer.textColor = text_color;
        _drawer.backgroundColor = background_color;
        _drawer.bit = 1;
        _drawer.paint.setTypeface(enTypePace);
        if (!_blink || !_frame.getPositionBlink(row, column)) {
            z = false;
        }
        _drawer.blink = z;
        _drawer.clip = (byte) 0;
        _drawer.drawCharAtPosition(getContext(), cursor_row, cursor_column, c);
    }

    private void drawSingleBitSpace2_1(int row, int column, int cursor_row, int cursor_column) {
        _drawer.paint.setTextSize((int) _zh_text_size);
        int upper = _frame.getPositionData(row, column);
        int lower = _frame.getPositionData(row, column + 1);
        int char_data = (upper << 8) + lower;
        char c = B2UEncoder.getInstance().encodeChar((char) char_data);
        int text_color = _frame.getPositionTextColor(row, column);
        int background_color = _frame.getPositionBackgroundColor(row, column);
        _drawer.textColor = text_color;
        _drawer.backgroundColor = background_color;
        _drawer.bit = 2;
        _drawer.paint.setTypeface(zhTypePace);
        _drawer.blink = _blink && _frame.getPositionBlink(row, column);
        _drawer.clip = (byte) 0;
        _drawer.drawCharAtPosition(getContext(), cursor_row, cursor_column, c);
    }

    private void drawSingleBitSpace2_2(int row, int column, int cursor_row, int cursor_column) {
        _drawer.paint.setTextSize((int) _zh_text_size);
        int lower = _frame.getPositionData(row, column);
        int upper = _frame.getPositionData(row, column - 1);
        int char_data = (upper << 8) + lower;
        char c = B2UEncoder.getInstance().encodeChar((char) char_data);
        int text_color = _frame.getPositionTextColor(row, column);
        int background_color = _frame.getPositionBackgroundColor(row, column);
        _drawer.textColor = text_color;
        _drawer.backgroundColor = background_color;
        _drawer.bit = 2;
        _drawer.paint.setTypeface(zhTypePace);
        _drawer.blink = _blink && _frame.getPositionBlink(row, column);
        _drawer.clip = (byte) 0;
        _drawer.drawCharAtPosition(getContext(), cursor_row, cursor_column - 1, c);
    }

    private void drawDoubleBitSpace2_1(int row, int column, int cursor_row, int cursor_column) {
        _drawer.paint.setTextSize((int) _zh_text_size);
        int upper = _frame.getPositionData(row, column);
        int lower = _frame.getPositionData(row, column + 1);
        int char_data = (upper << 8) + lower;
        char c = B2UEncoder.getInstance().encodeChar((char) char_data);
        int text_color = _frame.getPositionTextColor(row, column);
        int background_color = _frame.getPositionBackgroundColor(row, column);
        _drawer.textColor = text_color;
        _drawer.backgroundColor = background_color;
        _drawer.bit = 2;
        _drawer.paint.setTypeface(zhTypePace);
        _drawer.blink = _blink && _frame.getPositionBlink(row, column);
        _drawer.clip = (byte) 1;
        _drawer.drawCharAtPosition(getContext(), cursor_row, cursor_column, c);
    }

    private void drawDoubleBitSpace2_2(int row, int column, int cursor_row, int cursor_column) {
        _drawer.paint.setTextSize((int) _zh_text_size);
        int lower = _frame.getPositionData(row, column);
        int upper = _frame.getPositionData(row, column - 1);
        int char_data = (upper << 8) + lower;
        char c = B2UEncoder.getInstance().encodeChar((char) char_data);
        int text_color = _frame.getPositionTextColor(row, column);
        int background_color = _frame.getPositionBackgroundColor(row, column);
        _drawer.textColor = text_color;
        _drawer.backgroundColor = background_color;
        _drawer.bit = 2;
        _drawer.paint.setTypeface(zhTypePace);
        _drawer.blink = _blink && _frame.getPositionBlink(row, column);
        _drawer.clip = (byte) 2;
        _drawer.drawCharAtPosition(getContext(), cursor_row, cursor_column - 1, c);
    }
}