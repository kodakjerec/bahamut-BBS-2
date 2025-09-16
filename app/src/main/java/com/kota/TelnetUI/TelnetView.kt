package com.kota.TelnetUI

import android.annotation.SuppressLint
import android.content.Context
import android.graphics.Bitmap
import android.graphics.Canvas
import android.graphics.Matrix
import android.graphics.Paint
import android.graphics.Rect
import android.graphics.Typeface
import android.os.Handler
import android.os.Looper
import android.os.Message
import android.util.AttributeSet
import android.util.Log
import android.util.TypedValue
import android.view.View
import android.view.ViewGroup
import com.kota.Telnet.Model.TelnetFrame
import com.kota.TextEncoder.B2UEncoder
import java.lang.reflect.Array
import java.util.Vector
import kotlin.math.ceil
import kotlin.math.floor

/* loaded from: classes.dex */
class TelnetView : View {
    private val DEFAULT_TEXT_SIZE: Float
    private var _bitmap_block_height: Double
    private var _bitmap_block_width: Double
    private val _bitmap_config: Bitmap.Config
    private var _bitmap_space_column: Int
    private var _bitmap_space_row: Int
    private val _bitmap_space_x: Int
    private val _bitmap_space_y: Int
    private var _bitmaps: Array<Array<Bitmap?>?>?
    private var _blink: Boolean
    private val _blink_list: Vector<Position>
    private var _blink_thread: BlinkThread?
    private var _block_height: Double
    private var _block_width: Double
    var _clip: Rect
    private var _column: Int
    private val _current_space: BitmapSpace
    private var _draw_height: Int
    private val _draw_separator_line: Boolean
    private var _draw_width: Int
    private val _drawer: TelnetViewDrawer
    private var _en_text_size: Double
    private var _frame: TelnetFrame?
    private var _handler: Handler?
    private var _horizontal_unit: Double
    var _matrix: Matrix
    private val _origin_x: Int
    private val _origin_y: Int
    var _paint: Paint?
    private var _radius: Double
    private var _row: Int
    private var _scale_x: Float
    private var _scale_y: Float
    private var _typeface: Typeface?
    private var _vertical_unit: Double
    private var _zh_text_size: Double
    var enTypePace: Typeface?
    var zhTypePace: Typeface?

    class BitmapSpace private constructor() {
        var height: Int = 0
        var left: Int = 0
        var top: Int = 0
        var width: Int = 0

        fun contains(row: Int, column: Int): Boolean {
            return row >= left && row < left + width && column >= top && column < top + height
        }

        fun set(block: BitmapSpace) {
            left = block.left
            top = block.top
            width = block.width
            height = block.height
        }

        fun isEquals(block: BitmapSpace): Boolean {
            return left == block.left && top == block.top && width == block.width && height == block.height
        }

        override fun toString(): String {
            return "(" + left + " , " + top + ") (" + width + " , " + height + ")"
        }
    }

    class Position(var row: Int, var column: Int) {
        fun isEquals(aPosition: Position): Boolean {
            return row == aPosition.row && column == aPosition.column
        }
    }

    inner class BlinkThread private constructor() : Thread() {
        var _run: Boolean = true

        fun stopBlink() {
            _run = false
        }

        // java.lang.Thread, java.lang.Runnable
        override fun run() {
            _run = true
            while (_run) {
                try {
                    sleep(1000L)
                    if (this@TelnetView._handler != null) {
                        this@TelnetView._handler!!.sendEmptyMessage(0)
                    }
                } catch (e: InterruptedException) {
                    Log.e(javaClass.getSimpleName(), (if (e.message != null) e.message else "")!!)
                    return
                }
            }
        }
    }

    constructor(context: Context?) : super(context) {
        DEFAULT_TEXT_SIZE = 20.0f
        _frame = null
        _row = 24
        _column = 80
        _draw_width = 0
        _draw_height = 0
        _block_width = 6.0
        _block_height = 15.0
        _zh_text_size = 12.0
        _en_text_size = 12.0
        _origin_x = 0
        _origin_y = 0
        _radius = 0.0
        _vertical_unit = 2.0
        _horizontal_unit = 1.5
        _typeface = null
        _draw_separator_line = false
        _blink = false
        _blink_thread = null
        _handler = null
        _bitmaps = null
        _scale_x = 1.0f
        _scale_y = 1.0f
        _bitmap_space_x = 8
        _bitmap_space_y = 4
        _bitmap_space_row = 0
        _bitmap_space_column = 0
        _bitmap_block_width = 0.0
        _bitmap_block_height = 0.0
        _bitmap_config = Bitmap.Config.RGB_565
        _current_space = BitmapSpace()
        _blink_list = Vector<Position>()
        _drawer = TelnetViewDrawer()
        zhTypePace = Typeface.create("DEFAULT", Typeface.NORMAL)
        enTypePace = Typeface.create("MONOSPACE", Typeface.NORMAL)
        _clip = Rect()
        _paint = Paint()
        _matrix = Matrix()
        initial()
    }

    constructor(context: Context?, attrs: AttributeSet?, defStyle: Int) : super(
        context,
        attrs,
        defStyle
    ) {
        DEFAULT_TEXT_SIZE = 20.0f
        _frame = null
        _row = 24
        _column = 80
        _draw_width = 0
        _draw_height = 0
        _block_width = 6.0
        _block_height = 15.0
        _zh_text_size = 12.0
        _en_text_size = 12.0
        _origin_x = 0
        _origin_y = 0
        _radius = 0.0
        _vertical_unit = 2.0
        _horizontal_unit = 1.5
        _typeface = null
        _draw_separator_line = false
        _blink = false
        _blink_thread = null
        _handler = null
        _bitmaps = null
        _scale_x = 1.0f
        _scale_y = 1.0f
        _bitmap_space_x = 8
        _bitmap_space_y = 4
        _bitmap_space_row = 0
        _bitmap_space_column = 0
        _bitmap_block_width = 0.0
        _bitmap_block_height = 0.0
        _bitmap_config = Bitmap.Config.RGB_565
        _current_space = BitmapSpace()
        _blink_list = Vector<Position>()
        _drawer = TelnetViewDrawer()
        zhTypePace = Typeface.create("DEFAULT", Typeface.NORMAL)
        enTypePace = Typeface.create("MONOSPACE", Typeface.NORMAL)
        _clip = Rect()
        _paint = Paint()
        _matrix = Matrix()
        initial()
    }

    constructor(context: Context?, attrs: AttributeSet?) : super(context, attrs) {
        DEFAULT_TEXT_SIZE = 20.0f
        _frame = null
        _row = 24
        _column = 80
        _draw_width = 0
        _draw_height = 0
        _block_width = 6.0
        _block_height = 15.0
        _zh_text_size = 12.0
        _en_text_size = 12.0
        _origin_x = 0
        _origin_y = 0
        _radius = 0.0
        _vertical_unit = 2.0
        _horizontal_unit = 1.5
        _typeface = null
        _draw_separator_line = false
        _blink = false
        _blink_thread = null
        _handler = null
        _bitmaps = null
        _scale_x = 1.0f
        _scale_y = 1.0f
        _bitmap_space_x = 8
        _bitmap_space_y = 4
        _bitmap_space_row = 0
        _bitmap_space_column = 0
        _bitmap_block_width = 0.0
        _bitmap_block_height = 0.0
        _bitmap_config = Bitmap.Config.RGB_565
        _current_space = BitmapSpace()
        _blink_list = Vector<Position>()
        _drawer = TelnetViewDrawer()
        zhTypePace = Typeface.create("DEFAULT", Typeface.NORMAL)
        enTypePace = Typeface.create("MONOSPACE", Typeface.NORMAL)
        _clip = Rect()
        _paint = Paint()
        _matrix = Matrix()
        initial()
    }

    @SuppressLint("HandlerLeak")
    private fun startBlink() {
        if (_handler == null) {
            _handler = object : Handler(Looper.getMainLooper()) {
                // from class: com.kota.TelnetUI.TelnetView.1
                // android.os.Handler
                override fun handleMessage(msg: Message) {
                    this@TelnetView._blink = !this@TelnetView._blink
                    for (position in this@TelnetView._blink_list) {
                        if (this@TelnetView.bitmapContainsPosition(position)) {
                            this@TelnetView.removePositionBitmap(position)
                        }
                    }
                    this@TelnetView.invalidate()
                }
            }
        }
        if (_blink_thread == null) {
            _blink_thread = BlinkThread()
            _blink_thread!!.start()
        }
    }

    private fun stopBlink() {
        _handler = null
        if (_blink_thread != null) {
            _blink_thread!!.stopBlink()
            _blink_thread = null
        }
    }

    private fun initial() {
    }

    @Throws(Throwable::class)
    protected fun finalize() {
        if (_blink_thread != null) {
            stopBlink()
        }
        super.finalize()
    }

    var frame: TelnetFrame?
        get() = _frame
        set(aTelnetFrame) {
            _frame = aTelnetFrame
            _frame!!.reloadSpace()
            cleanBitmap()
            invalidate()
        }

    fun setTypeface(typeface: Typeface?) {
        _typeface = typeface
    }

    // android.view.View
    override fun onMeasure(widthMeasureSpec: Int, heightMeasureSpec: Int) {
        val width_measure_value: Int
        val height_measure_value: Int
        cleanBitmap()
        val view_width = View.MeasureSpec.getSize(widthMeasureSpec)
        val view_height = View.MeasureSpec.getSize(heightMeasureSpec)
        val layout = getLayoutParams()
        val default_text_size = TypedValue.applyDimension(
            TypedValue.COMPLEX_UNIT_SP,
            DEFAULT_TEXT_SIZE,
            getContext().getResources().getDisplayMetrics()
        ).toDouble()
        _draw_width = 0
        _draw_height = 0
        _scale_x = 1.0f
        _scale_y = 1.0f
        _row = 0
        _column = 0
        if (_frame == null) {
            super.onMeasure(widthMeasureSpec, heightMeasureSpec)
            return
        }
        _row = _frame!!.getRowSize()
        _column = 80
        // 螢幕寬度/每行字元數 = 得到雙字元寬度(預估)
        // => /2 得到單字元寬度
        // => *2 得到雙字元寬度(精準)
        val _ratio = 2.5
        if (layout.width == ViewGroup.LayoutParams.WRAP_CONTENT && layout.height == ViewGroup.LayoutParams.WRAP_CONTENT) {
            _block_width = (default_text_size / 2 / 2 * 2).toInt().toDouble()
            _block_height = _block_width * _ratio
            _draw_width = (_block_width * _column).toInt()
            _draw_height = (_block_height * _row).toInt()
            _scale_x = 1.0f
            _scale_y = 1.0f
            width_measure_value = View.MeasureSpec.makeMeasureSpec(_draw_width, MeasureSpec.EXACTLY)
            height_measure_value =
                View.MeasureSpec.makeMeasureSpec(_draw_height, MeasureSpec.EXACTLY)
        } else if (layout.height == ViewGroup.LayoutParams.WRAP_CONTENT) {
            _block_width = (view_width / _column / 2 * 2).toDouble()
            _block_height = _block_width * _ratio
            _draw_width = (_block_width * _column).toInt()
            _draw_height = (_block_height * _row).toInt()
            _scale_x = view_width.toFloat() / _draw_width
            width_measure_value = View.MeasureSpec.makeMeasureSpec(view_width, MeasureSpec.EXACTLY)
            height_measure_value =
                View.MeasureSpec.makeMeasureSpec(_draw_height, MeasureSpec.EXACTLY)
        } else if (layout.width == ViewGroup.LayoutParams.WRAP_CONTENT) {
            _block_width = (_block_height / _ratio / 2 * 2).toInt().toDouble()
            _block_height = view_height.toDouble() / _row
            _draw_width = (_block_width * _column).toInt()
            _draw_height = (_block_height * _row).toInt()
            _scale_y = view_height.toFloat() / _draw_height
            width_measure_value = View.MeasureSpec.makeMeasureSpec(_draw_width, MeasureSpec.EXACTLY)
            height_measure_value =
                View.MeasureSpec.makeMeasureSpec(view_height, MeasureSpec.EXACTLY)
        } else {
            _block_width = view_width.toDouble() / _column / 2 * 2
            _block_height = _block_width * _ratio
            _draw_width = (_block_width * _column).toInt()
            _draw_height = (_block_height * _row).toInt()
            _scale_x = view_width.toFloat() / _draw_width
            _scale_y = view_height.toFloat() / _draw_height
            width_measure_value = View.MeasureSpec.makeMeasureSpec(view_width, MeasureSpec.EXACTLY)
            height_measure_value =
                View.MeasureSpec.makeMeasureSpec(view_height, MeasureSpec.EXACTLY)
        }
        calculateLayout()
        super.onMeasure(width_measure_value, height_measure_value)
    }

    fun calculateLayout() {
        if (_frame != null) {
            val unit_dp = ceil(
                TypedValue.applyDimension(
                    TypedValue.COMPLEX_UNIT_DIP,
                    1.0f,
                    getContext().getResources().getDisplayMetrics()
                ).toDouble()
            ).toInt()
            _vertical_unit = (_block_height - unit_dp) / 8.0
            _horizontal_unit = (_block_width * 2.0) / 7.0
            _radius = _block_height / 2.0
            if (_radius > _block_width) {
                _radius = _block_width
            }
            _zh_text_size = _radius * 2.0
            _en_text_size = _zh_text_size * 0.8999999761581421
        }
        _bitmap_block_width = _block_width * _bitmap_space_x
        _bitmap_block_height = _block_height * _bitmap_space_y
        reloadDrawer()
    }

    // android.view.View
    override fun onDraw(canvas: Canvas) {
        super.onDraw(canvas)
        canvas.getClipBounds(_clip)
        canvas.scale(_scale_x, _scale_y)
        val new_space = calculateBlock(
            _clip.left.toDouble(),
            _clip.top.toDouble(),
            _clip.width().toDouble(),
            _clip.height().toDouble()
        )
        if (!_current_space.isEquals(new_space)) {
            setBitmapSpace(new_space)
        }
        val space_left = _current_space.left
        var space_right = _current_space.left + _current_space.width
        if (space_right > _bitmap_space_column) {
            space_right = _bitmap_space_column
        }
        val space_top = _current_space.top
        var space_bottom = _current_space.top + _current_space.height
        if (space_bottom > _bitmap_space_row) {
            space_bottom = _bitmap_space_row
        }
        for (row in space_top..<space_bottom) {
            val origin_y = (row * _bitmap_block_height).toFloat()
            for (column in space_left..<space_right) {
                val bm = getBitmap(row, column)
                if (bm != null) {
                    val origin_x = (column * _bitmap_block_width).toFloat()
                    _matrix.setTranslate(origin_x, origin_y)
                    canvas.drawBitmap(bm, _matrix, _paint)
                }
            }
        }
        val blink = _blink_list.size > 0
        if (blink && _blink_thread == null) {
            startBlink()
        }
        if (!blink && _blink_thread != null) {
            stopBlink()
        }
    }

    private fun reloadDrawer() {
        _drawer.blockWidth = _block_width
        _drawer.blockHeight = _block_height
        _drawer.horizontalUnit = _horizontal_unit
        _drawer.verticalUnit = _vertical_unit
        _drawer.radius = _radius
        _drawer.originX = _origin_x
        _drawer.originY = _origin_y
        _drawer.paint.setAntiAlias(true)
    }

    private fun drawTelnet(
        canvas: Canvas,
        aPosition: Position,
        rowStart: Int,
        rowEnd: Int,
        columnStart: Int,
        columnEnd: Int
    ) {
        if (_typeface != null) {
            _drawer.paint.setTypeface(_typeface)
        }
        if (_frame != null) {
            _drawer.canvas = canvas
            val zh_bounds = Rect()
            _drawer.paint.getTextBounds("國", 0, 1, zh_bounds)
            val en_bounds = Rect()
            _drawer.paint.getTextBounds("D", 0, 1, en_bounds)
            _drawer.clip = 0.toByte()
            val block_offset = ceil(_block_height - _zh_text_size)
            val font_offset = (zh_bounds.height() - en_bounds.height()).toDouble()
            _drawer.textBottomOffset = ceil((block_offset + font_offset) / 2.0).toInt()
            var blink = false
            for (row in rowStart..<rowEnd) {
                val cursor_row = row - rowStart
                var column = columnStart
                while (column < columnEnd) {
                    val cursor_column = column - columnStart
                    if (_frame!!.getPositionBlink(row, column)) {
                        blink = true
                    }
                    val bit_space = _frame!!.getPositionBitSpace(row, column)
                    if (bit_space.toInt() == 1) {
                        drawSingleBitSpace2_1(row, column, cursor_row, cursor_column)
                        column++
                    } else if (bit_space.toInt() == 2 && column > 0) {
                        drawSingleBitSpace2_2(row, column, cursor_row, cursor_column)
                    } else if (bit_space.toInt() == 3) {
                        drawDoubleBitSpace2_1(row, column, cursor_row, cursor_column)
                    } else if (bit_space.toInt() == 4 && column > 0) {
                        drawDoubleBitSpace2_2(row, column, cursor_row, cursor_column)
                    } else {
                        drawBitSpace1(row, column, cursor_row, cursor_column)
                    }
                    column++
                }
            }
            if (blink) {
                var contains_in_blink_link = false
                val it = _blink_list.iterator()
                while (true) {
                    if (!it.hasNext()) {
                        break
                    }
                    val position = it.next()
                    if (position.isEquals(aPosition)) {
                        contains_in_blink_link = true
                        break
                    }
                }
                if (!contains_in_blink_link) {
                    _blink_list.add(aPosition)
                }
            }
            if (_draw_separator_line) {
                _drawer.paint.setColor(1728053247)
                for (row2 in 0..<_frame!!.getRowSize()) {
                    val position_y = (row2 * _block_height).toInt()
                    canvas.drawLine(
                        0.0f,
                        position_y.toFloat(),
                        480.0f,
                        position_y.toFloat(),
                        _drawer.paint
                    )
                }
                for (column2 in 0..79) {
                    val position_x = (column2 * _block_width).toInt()
                    canvas.drawLine(
                        position_x.toFloat(),
                        0.0f,
                        position_x.toFloat(),
                        360.0f,
                        _drawer.paint
                    )
                }
                return
            }
            return
        }
        println("_telnet_model is null")
    }

    // android.view.View
    override fun onDetachedFromWindow() {
        super.onDetachedFromWindow()
        stopBlink()
    }

    private fun cleanBitmap() {
        _bitmaps = null
        _blink_list.clear()
    }

    private fun resetBitmap() {
        _bitmap_space_row = 0
        _bitmap_space_column = 0
        if (_frame != null) {
            val row_maximum = _frame!!.getRowSize()
            _bitmap_space_row =
                (if (row_maximum % _bitmap_space_y > 0) 1 else 0) + (row_maximum / _bitmap_space_y)
            _bitmap_space_column =
                (80 / _bitmap_space_x) + (if (80 % _bitmap_space_x == 0) 0 else 1)
        }
        if (_bitmap_space_row > 0 && _bitmap_space_column > 0) {
            _bitmaps = Array.newInstance(
                Bitmap::class.java,
                _bitmap_space_row,
                _bitmap_space_column
            ) as kotlin.Array<kotlin.Array<Bitmap?>?>
        }
    }

    private fun getBitmap(row: Int, column: Int): Bitmap? {
        var bm: Bitmap? = null
        if (_bitmaps == null) {
            resetBitmap()
        }
        if (bitmapContainsPosition(row, column) && (_bitmaps!![row]!![column].also {
                bm = it
            }) == null) {
            bm = createBitmap()
            _bitmaps!![row]!![column] = bm
            val canvas = Canvas(bm)
            val row_start = row * _bitmap_space_y
            var row_end = (row + 1) * _bitmap_space_y
            if (row_end > _frame!!.getRowSize()) {
                row_end = _frame!!.getRowSize()
            }
            val column_start = column * _bitmap_space_x
            var column_end = (column + 1) * _bitmap_space_x
            if (column_end > 80) {
                column_end = 80
            }
            drawTelnet(canvas, Position(row, column), row_start, row_end, column_start, column_end)
        }
        return bm
    }

    fun createBitmap(): Bitmap {
        return Bitmap.createBitmap(
            _bitmap_block_width.toInt(),
            _bitmap_block_height.toInt(),
            _bitmap_config
        )
    }

    fun setBitmapSpace(aSpace: BitmapSpace) {
        if (_bitmaps == null) {
            resetBitmap()
        }
        val block_right = _current_space.left + _current_space.width
        val block_bottom = _current_space.top + _current_space.height
        for (row in _current_space.left..<block_right) {
            for (column in _current_space.top..<block_bottom) {
                if (!aSpace.contains(row, column)) {
                    removePositionBitmap(row, column)
                }
            }
        }
        _current_space.set(aSpace)
    }

    fun removePositionBitmap(aPosition: Position) {
        removePositionBitmap(aPosition.row, aPosition.column)
    }

    fun removePositionBitmap(row: Int, column: Int) {
        if (bitmapContainsPosition(row, column)) {
            _bitmaps!![row]!![column] = null
        }
    }

    fun calculateBlock(left: Double, top: Double, width: Double, height: Double): BitmapSpace {
        val block = BitmapSpace()
        block.left = floor(left / _bitmap_block_width).toInt()
        block.top = floor(top / _bitmap_block_height).toInt()
        block.width = ((ceil((left + width) / _bitmap_block_width).toInt()) - block.left) + 1
        block.height = ((ceil((top + height) / _bitmap_block_height).toInt()) - block.top) + 1
        return block
    }

    fun bitmapContainsPosition(aPosition: Position): Boolean {
        return bitmapContainsPosition(aPosition.row, aPosition.column)
    }

    fun bitmapContainsPosition(row: Int, column: Int): Boolean {
        return _bitmaps != null && row >= 0 && row < _bitmaps!!.size && column >= 0 && column < _bitmaps!![row]!!.size
    }

    private fun drawBitSpace1(row: Int, column: Int, cursor_row: Int, cursor_column: Int) {
        var z = true
        _drawer.paint.setTextSize(_en_text_size.toInt().toFloat())
        val c = _frame!!.getPositionData(row, column).toChar()
        val text_color = _frame!!.getPositionTextColor(row, column)
        val background_color = _frame!!.getPositionBackgroundColor(row, column)
        _drawer.textColor = text_color
        _drawer.backgroundColor = background_color
        _drawer.bit = 1
        _drawer.paint.setTypeface(enTypePace)
        if (!_blink || !_frame!!.getPositionBlink(row, column)) {
            z = false
        }
        _drawer.blink = z
        _drawer.clip = 0.toByte()
        _drawer.drawCharAtPosition(getContext(), cursor_row, cursor_column, c)
    }

    private fun drawSingleBitSpace2_1(row: Int, column: Int, cursor_row: Int, cursor_column: Int) {
        _drawer.paint.setTextSize(_zh_text_size.toInt().toFloat())
        val upper = _frame!!.getPositionData(row, column)
        val lower = _frame!!.getPositionData(row, column + 1)
        val char_data = (upper shl 8) + lower
        val c: Char = B2UEncoder.getInstance().encodeChar(char_data.toChar())
        val text_color = _frame!!.getPositionTextColor(row, column)
        val background_color = _frame!!.getPositionBackgroundColor(row, column)
        _drawer.textColor = text_color
        _drawer.backgroundColor = background_color
        _drawer.bit = 2
        _drawer.paint.setTypeface(zhTypePace)
        _drawer.blink = _blink && _frame!!.getPositionBlink(row, column)
        _drawer.clip = 0.toByte()
        _drawer.drawCharAtPosition(getContext(), cursor_row, cursor_column, c)
    }

    private fun drawSingleBitSpace2_2(row: Int, column: Int, cursor_row: Int, cursor_column: Int) {
        _drawer.paint.setTextSize(_zh_text_size.toInt().toFloat())
        val lower = _frame!!.getPositionData(row, column)
        val upper = _frame!!.getPositionData(row, column - 1)
        val char_data = (upper shl 8) + lower
        val c: Char = B2UEncoder.getInstance().encodeChar(char_data.toChar())
        val text_color = _frame!!.getPositionTextColor(row, column)
        val background_color = _frame!!.getPositionBackgroundColor(row, column)
        _drawer.textColor = text_color
        _drawer.backgroundColor = background_color
        _drawer.bit = 2
        _drawer.paint.setTypeface(zhTypePace)
        _drawer.blink = _blink && _frame!!.getPositionBlink(row, column)
        _drawer.clip = 0.toByte()
        _drawer.drawCharAtPosition(getContext(), cursor_row, cursor_column - 1, c)
    }

    private fun drawDoubleBitSpace2_1(row: Int, column: Int, cursor_row: Int, cursor_column: Int) {
        _drawer.paint.setTextSize(_zh_text_size.toInt().toFloat())
        val upper = _frame!!.getPositionData(row, column)
        val lower = _frame!!.getPositionData(row, column + 1)
        val char_data = (upper shl 8) + lower
        val c: Char = B2UEncoder.getInstance().encodeChar(char_data.toChar())
        val text_color = _frame!!.getPositionTextColor(row, column)
        val background_color = _frame!!.getPositionBackgroundColor(row, column)
        _drawer.textColor = text_color
        _drawer.backgroundColor = background_color
        _drawer.bit = 2
        _drawer.paint.setTypeface(zhTypePace)
        _drawer.blink = _blink && _frame!!.getPositionBlink(row, column)
        _drawer.clip = 1.toByte()
        _drawer.drawCharAtPosition(getContext(), cursor_row, cursor_column, c)
    }

    private fun drawDoubleBitSpace2_2(row: Int, column: Int, cursor_row: Int, cursor_column: Int) {
        _drawer.paint.setTextSize(_zh_text_size.toInt().toFloat())
        val lower = _frame!!.getPositionData(row, column)
        val upper = _frame!!.getPositionData(row, column - 1)
        val char_data = (upper shl 8) + lower
        val c: Char = B2UEncoder.getInstance().encodeChar(char_data.toChar())
        val text_color = _frame!!.getPositionTextColor(row, column)
        val background_color = _frame!!.getPositionBackgroundColor(row, column)
        _drawer.textColor = text_color
        _drawer.backgroundColor = background_color
        _drawer.bit = 2
        _drawer.paint.setTypeface(zhTypePace)
        _drawer.blink = _blink && _frame!!.getPositionBlink(row, column)
        _drawer.clip = 2.toByte()
        _drawer.drawCharAtPosition(getContext(), cursor_row, cursor_column - 1, c)
    }
}