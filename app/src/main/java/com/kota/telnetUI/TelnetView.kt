package com.kota.telnetUI

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
import com.kota.telnet.model.TelnetFrame
import com.kota.textEncoder.B2UEncoder
import java.util.Vector
import kotlin.math.ceil
import kotlin.math.floor

class TelnetView : View {
    val defaultTextSize: Float
    private var bitmapBlockHeight: Double
    private var bitmapBlockWidth: Double
    private val bitmapConfig: Bitmap.Config
    private var bitmapSpaceColumns: Int
    private var bitmapSpaceRows: Int
    private val bitmapSpaceX: Int
    private val bitmapSpaceY: Int
    private var myBitmaps: MutableList<MutableList<Bitmap?>>?
    private var blink: Boolean
    private val blinkList: Vector<Position>
    private var blinkThread: BlinkThread?
    private var blockHeight: Double
    private var blockWidth: Double
    var rect: Rect
    private var columnCount: Int
    private val currentBitmapSpace: BitmapSpace
    private var drawHeight: Int
    private val drawSeparatorLine: Boolean
    private var drawWidth: Int
    private val drawer: TelnetViewDrawer
    private var enTextSize: Double
    private var telnetFrame: TelnetFrame?
    private var myHandler: Handler?
    private var horizontalUnit: Double
    var myMatrix: Matrix
    private val originX: Int
    private val originY: Int
    var myPaint: Paint?
    private var myRadius: Double
    private var myRow: Int
    private var myScaleX: Float
    private var myScaleY: Float
    private var typeface: Typeface?
    private var verticalUnit: Double
    private var zhTextSize: Double
    var enTypePace: Typeface?
    var zhTypePace: Typeface?

    class BitmapSpace() {
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
            return "($left , $top) ($width , $height)"
        }
    }

    class Position(var row: Int, var column: Int) {
        fun isEquals(aPosition: Position): Boolean {
            return row == aPosition.row && column == aPosition.column
        }
    }

    inner class BlinkThread() : Thread() {
        var myRun: Boolean = true

        fun stopBlink() {
            myRun = false
        }

        // java.lang.Thread, java.lang.Runnable
        override fun run() {
            myRun = true
            while (myRun) {
                try {
                    sleep(1000L)
                    if (this@TelnetView.myHandler != null) {
                        this@TelnetView.myHandler!!.sendEmptyMessage(0)
                    }
                } catch (e: InterruptedException) {
                    Log.e(javaClass.simpleName, (if (e.message != null) e.message else "")!!)
                    return
                }
            }
        }
    }

    constructor(context: Context?) : super(context) {
        defaultTextSize = 20.0f
        telnetFrame = null
        myRow = 24
        columnCount = 80
        drawWidth = 0
        drawHeight = 0
        blockWidth = 6.0
        blockHeight = 15.0
        zhTextSize = 12.0
        enTextSize = 12.0
        originX = 0
        originY = 0
        myRadius = 0.0
        verticalUnit = 2.0
        horizontalUnit = 1.5
        typeface = null
        drawSeparatorLine = false
        blink = false
        blinkThread = null
        myHandler = null
        myBitmaps = null
        myScaleX = 1.0f
        myScaleY = 1.0f
        bitmapSpaceX = 8
        bitmapSpaceY = 4
        bitmapSpaceRows = 0
        bitmapSpaceColumns = 0
        bitmapBlockWidth = 0.0
        bitmapBlockHeight = 0.0
        bitmapConfig = Bitmap.Config.RGB_565
        currentBitmapSpace = BitmapSpace()
        blinkList = Vector<Position>()
        drawer = TelnetViewDrawer()
        zhTypePace = Typeface.create("DEFAULT", Typeface.NORMAL)
        enTypePace = Typeface.create("MONOSPACE", Typeface.NORMAL)
        rect = Rect()
        myPaint = Paint()
        myMatrix = Matrix()
        initial()
    }

    constructor(context: Context?, attrs: AttributeSet?, defStyle: Int) : super(
        context,
        attrs,
        defStyle
    ) {
        defaultTextSize = 20.0f
        telnetFrame = null
        myRow = 24
        columnCount = 80
        drawWidth = 0
        drawHeight = 0
        blockWidth = 6.0
        blockHeight = 15.0
        zhTextSize = 12.0
        enTextSize = 12.0
        originX = 0
        originY = 0
        myRadius = 0.0
        verticalUnit = 2.0
        horizontalUnit = 1.5
        typeface = null
        drawSeparatorLine = false
        blink = false
        blinkThread = null
        myHandler = null
        myBitmaps = null
        myScaleX = 1.0f
        myScaleY = 1.0f
        bitmapSpaceX = 8
        bitmapSpaceY = 4
        bitmapSpaceRows = 0
        bitmapSpaceColumns = 0
        bitmapBlockWidth = 0.0
        bitmapBlockHeight = 0.0
        bitmapConfig = Bitmap.Config.RGB_565
        currentBitmapSpace = BitmapSpace()
        blinkList = Vector<Position>()
        drawer = TelnetViewDrawer()
        zhTypePace = Typeface.create("DEFAULT", Typeface.NORMAL)
        enTypePace = Typeface.create("MONOSPACE", Typeface.NORMAL)
        rect = Rect()
        myPaint = Paint()
        myMatrix = Matrix()
        initial()
    }

    constructor(context: Context?, attrs: AttributeSet?) : super(context, attrs) {
        defaultTextSize = 20.0f
        telnetFrame = null
        myRow = 24
        columnCount = 80
        drawWidth = 0
        drawHeight = 0
        blockWidth = 6.0
        blockHeight = 15.0
        zhTextSize = 12.0
        enTextSize = 12.0
        originX = 0
        originY = 0
        myRadius = 0.0
        verticalUnit = 2.0
        horizontalUnit = 1.5
        typeface = null
        drawSeparatorLine = false
        blink = false
        blinkThread = null
        myHandler = null
        myBitmaps = null
        myScaleX = 1.0f
        myScaleY = 1.0f
        bitmapSpaceX = 8
        bitmapSpaceY = 4
        bitmapSpaceRows = 0
        bitmapSpaceColumns = 0
        bitmapBlockWidth = 0.0
        bitmapBlockHeight = 0.0
        bitmapConfig = Bitmap.Config.RGB_565
        currentBitmapSpace = BitmapSpace()
        blinkList = Vector<Position>()
        drawer = TelnetViewDrawer()
        zhTypePace = Typeface.create("DEFAULT", Typeface.NORMAL)
        enTypePace = Typeface.create("MONOSPACE", Typeface.NORMAL)
        rect = Rect()
        myPaint = Paint()
        myMatrix = Matrix()
        initial()
    }

    @SuppressLint("HandlerLeak")
    private fun startBlink() {
        if (myHandler == null) {
            myHandler = object : Handler(Looper.getMainLooper()) {
                // from class: com.kota.TelnetUI.TelnetView.1
                // android.os.Handler
                override fun handleMessage(msg: Message) {
                    this@TelnetView.blink = !this@TelnetView.blink
                    for (position in this@TelnetView.blinkList) {
                        if (this@TelnetView.bitmapContainsPosition(position)) {
                            this@TelnetView.removePositionBitmap(position)
                        }
                    }
                    this@TelnetView.invalidate()
                }
            }
        }
        if (blinkThread == null) {
            blinkThread = BlinkThread()
            blinkThread!!.start()
        }
    }

    private fun stopBlink() {
        myHandler = null
        if (blinkThread != null) {
            blinkThread!!.stopBlink()
            blinkThread = null
        }
    }

    private fun initial() {
    }

    var frame: TelnetFrame?
        get() = telnetFrame
        set(aTelnetFrame) {
            telnetFrame = aTelnetFrame
            telnetFrame!!.reloadSpace()
            cleanBitmap()
            invalidate()
        }

    fun setTypeface(typeface: Typeface?) {
        this@TelnetView.typeface = typeface
    }

    // android.view.View
    override fun onMeasure(widthMeasureSpec: Int, heightMeasureSpec: Int) {
        val widthMeasureValue: Int
        val heightMeasureValue: Int
        cleanBitmap()
        val viewWidth = MeasureSpec.getSize(widthMeasureSpec)
        val viewHeight = MeasureSpec.getSize(heightMeasureSpec)
        val layout = layoutParams
        val defaultTextSize1 = TypedValue.applyDimension(
            TypedValue.COMPLEX_UNIT_SP,
            defaultTextSize,
            context.resources.displayMetrics
        ).toDouble()
        drawWidth = 0
        drawHeight = 0
        myScaleX = 1.0f
        myScaleY = 1.0f
        myRow = 0
        columnCount = 0
        if (telnetFrame == null) {
            super.onMeasure(widthMeasureSpec, heightMeasureSpec)
            return
        }
        myRow = telnetFrame!!.rowSize
        columnCount = 80
        // 螢幕寬度/每行字元數 = 得到雙字元寬度(預估)
        // => /2 得到單字元寬度
        // => *2 得到雙字元寬度(精準)
        val myRatio = 2.5
        if (layout.width == ViewGroup.LayoutParams.WRAP_CONTENT && layout.height == ViewGroup.LayoutParams.WRAP_CONTENT) {
            blockWidth = (defaultTextSize1 / 2 / 2 * 2).toInt().toDouble()
            blockHeight = blockWidth * myRatio
            drawWidth = (blockWidth * columnCount).toInt()
            drawHeight = (blockHeight * myRow).toInt()
            myScaleX = 1.0f
            myScaleY = 1.0f
            widthMeasureValue = MeasureSpec.makeMeasureSpec(drawWidth, MeasureSpec.EXACTLY)
            heightMeasureValue =
                MeasureSpec.makeMeasureSpec(drawHeight, MeasureSpec.EXACTLY)
        } else if (layout.height == ViewGroup.LayoutParams.WRAP_CONTENT) {
            blockWidth = (viewWidth / columnCount / 2 * 2).toDouble()
            blockHeight = blockWidth * myRatio
            drawWidth = (blockWidth * columnCount).toInt()
            drawHeight = (blockHeight * myRow).toInt()
            myScaleX = viewWidth.toFloat() / drawWidth
            widthMeasureValue = MeasureSpec.makeMeasureSpec(viewWidth, MeasureSpec.EXACTLY)
            heightMeasureValue =
                MeasureSpec.makeMeasureSpec(drawHeight, MeasureSpec.EXACTLY)
        } else if (layout.width == ViewGroup.LayoutParams.WRAP_CONTENT) {
            blockWidth = (blockHeight / myRatio / 2 * 2).toInt().toDouble()
            blockHeight = viewHeight.toDouble() / myRow
            drawWidth = (blockWidth * columnCount).toInt()
            drawHeight = (blockHeight * myRow).toInt()
            myScaleY = viewHeight.toFloat() / drawHeight
            widthMeasureValue = MeasureSpec.makeMeasureSpec(drawWidth, MeasureSpec.EXACTLY)
            heightMeasureValue =
                MeasureSpec.makeMeasureSpec(viewHeight, MeasureSpec.EXACTLY)
        } else {
            blockWidth = viewWidth.toDouble() / columnCount / 2 * 2
            blockHeight = blockWidth * myRatio
            drawWidth = (blockWidth * columnCount).toInt()
            drawHeight = (blockHeight * myRow).toInt()
            myScaleX = viewWidth.toFloat() / drawWidth
            myScaleY = viewHeight.toFloat() / drawHeight
            widthMeasureValue = MeasureSpec.makeMeasureSpec(viewWidth, MeasureSpec.EXACTLY)
            heightMeasureValue =
                MeasureSpec.makeMeasureSpec(viewHeight, MeasureSpec.EXACTLY)
        }
        calculateLayout()
        super.onMeasure(widthMeasureValue, heightMeasureValue)
    }

    fun calculateLayout() {
        if (telnetFrame != null) {
            val unitDp = ceil(
                TypedValue.applyDimension(
                    TypedValue.COMPLEX_UNIT_DIP,
                    1.0f,
                    context.resources.displayMetrics
                ).toDouble()
            ).toInt()
            verticalUnit = (blockHeight - unitDp) / 8.0
            horizontalUnit = (blockWidth * 2.0) / 7.0
            myRadius = blockHeight / 2.0
            if (myRadius > blockWidth) {
                myRadius = blockWidth
            }
            zhTextSize = myRadius * 2.0
            enTextSize = zhTextSize * 0.8999999761581421
        }
        bitmapBlockWidth = blockWidth * bitmapSpaceX
        bitmapBlockHeight = blockHeight * bitmapSpaceY
        reloadDrawer()
    }

    // android.view.View
    override fun onDraw(canvas: Canvas) {
        super.onDraw(canvas)
        canvas.getClipBounds(rect)
        canvas.scale(myScaleX, myScaleY)
        val newSpace = calculateBlock(
            rect.left.toDouble(),
            rect.top.toDouble(),
            rect.width().toDouble(),
            rect.height().toDouble()
        )
        if (!currentBitmapSpace.isEquals(newSpace)) {
            setBitmapSpace(newSpace)
        }
        val spaceLeft = currentBitmapSpace.left
        var spaceRight = currentBitmapSpace.left + currentBitmapSpace.width
        if (spaceRight > bitmapSpaceColumns) {
            spaceRight = bitmapSpaceColumns
        }
        val spaceTop = currentBitmapSpace.top
        var spaceBottom = currentBitmapSpace.top + currentBitmapSpace.height
        if (spaceBottom > bitmapSpaceRows) {
            spaceBottom = bitmapSpaceRows
        }
        for (row in spaceTop..<spaceBottom) {
            val originY = (row * bitmapBlockHeight).toFloat()
            for (column in spaceLeft..<spaceRight) {
                val bm = getBitmap(row, column)
                if (bm != null) {
                    val originX = (column * bitmapBlockWidth).toFloat()
                    myMatrix.setTranslate(originX, originY)
                    canvas.drawBitmap(bm, myMatrix, myPaint)
                }
            }
        }
        val blink = blinkList.isNotEmpty()
        if (blink && blinkThread == null) {
            startBlink()
        }
        if (!blink && blinkThread != null) {
            stopBlink()
        }
    }

    private fun reloadDrawer() {
        drawer.blockWidth = blockWidth
        drawer.blockHeight = blockHeight
        drawer.horizontalUnit = horizontalUnit
        drawer.verticalUnit = verticalUnit
        drawer.radius = myRadius
        drawer.originX = originX
        drawer.originY = originY
        drawer.paint.isAntiAlias = true
    }

    private fun drawTelnet(
        canvas: Canvas,
        aPosition: Position,
        rowStart: Int,
        rowEnd: Int,
        columnStart: Int,
        columnEnd: Int
    ) {
        if (typeface != null) {
            drawer.paint.typeface = typeface
        }
        if (telnetFrame != null) {
            drawer.canvas = canvas
            val zhBounds = Rect()
            drawer.paint.getTextBounds("國", 0, 1, zhBounds)
            val enBounds = Rect()
            drawer.paint.getTextBounds("D", 0, 1, enBounds)
            drawer.clip = 0.toByte()
            val blockOffset = ceil(blockHeight - zhTextSize)
            val fontOffset = (zhBounds.height() - enBounds.height()).toDouble()
            drawer.textBottomOffset = ceil((blockOffset + fontOffset) / 2.0).toInt()
            var blink = false
            for (row in rowStart..<rowEnd) {
                val cursorRow = row - rowStart
                var column = columnStart
                while (column < columnEnd) {
                    val cursorColumn = column - columnStart
                    if (telnetFrame!!.getPositionBlink(row, column)) {
                        blink = true
                    }
                    val bitSpace = telnetFrame!!.getPositionBitSpace(row, column)
                    if (bitSpace.toInt() == 1) {
                        drawSingleBitSpace2r1(row, column, cursorRow, cursorColumn)
                        column++
                    } else if (bitSpace.toInt() == 2 && column > 0) {
                        drawSingleBitSpace2r2(row, column, cursorRow, cursorColumn)
                    } else if (bitSpace.toInt() == 3) {
                        drawDoubleBitSpace2r1(row, column, cursorRow, cursorColumn)
                    } else if (bitSpace.toInt() == 4 && column > 0) {
                        drawDoubleBitSpace2r2(row, column, cursorRow, cursorColumn)
                    } else {
                        drawBitSpace1(row, column, cursorRow, cursorColumn)
                    }
                    column++
                }
            }
            if (blink) {
                var containsInBlinkLink = false
                val it = blinkList.iterator()
                while (true) {
                    if (!it.hasNext()) {
                        break
                    }
                    val position = it.next()
                    if (position.isEquals(aPosition)) {
                        containsInBlinkLink = true
                        break
                    }
                }
                if (!containsInBlinkLink) {
                    blinkList.add(aPosition)
                }
            }
            if (drawSeparatorLine) {
                drawer.paint.color = 1728053247
                for (row2 in 0..<telnetFrame!!.rowSize) {
                    val positionY = (row2 * blockHeight).toInt()
                    canvas.drawLine(
                        0.0f,
                        positionY.toFloat(),
                        480.0f,
                        positionY.toFloat(),
                        drawer.paint
                    )
                }
                for (column2 in 0..79) {
                    val positionX = (column2 * blockWidth).toInt()
                    canvas.drawLine(
                        positionX.toFloat(),
                        0.0f,
                        positionX.toFloat(),
                        360.0f,
                        drawer.paint
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
        myBitmaps = null
        blinkList.clear()
    }

    private fun resetBitmap() {
        bitmapSpaceRows = 0
        bitmapSpaceColumns = 0
        if (telnetFrame != null) {
            val rowMaximum = telnetFrame!!.rowSize
            bitmapSpaceRows =
                (if (rowMaximum % bitmapSpaceY > 0) 1 else 0) + (rowMaximum / bitmapSpaceY)
            bitmapSpaceColumns =
                (80 / bitmapSpaceX) + (if (80 % bitmapSpaceX == 0) 0 else 1)
        }
        if (bitmapSpaceRows > 0 && bitmapSpaceColumns > 0) {
            myBitmaps = MutableList(bitmapSpaceRows) {
                MutableList(bitmapSpaceColumns) { null }
            }
        }
    }

    private fun getBitmap(row: Int, column: Int): Bitmap? {
        var bm: Bitmap? = null
        if (myBitmaps == null) {
            resetBitmap()
        }
        if (bitmapContainsPosition(row, column) && (myBitmaps!![row][column].also {
                bm = it
            }) == null) {
            bm = createBitmap()
            myBitmaps!![row][column] = bm  // 這樣就可以正常賦值了
            val canvas = Canvas(bm)
            val rowStart = row * bitmapSpaceY
            var rowEnd = (row + 1) * bitmapSpaceY
            if (rowEnd > telnetFrame!!.rowSize) {
                rowEnd = telnetFrame!!.rowSize
            }
            val columnStart = column * bitmapSpaceX
            var columnEnd = (column + 1) * bitmapSpaceX
            if (columnEnd > 80) {
                columnEnd = 80
            }
            drawTelnet(canvas, Position(row, column), rowStart, rowEnd, columnStart, columnEnd)
        }
        return bm
    }

    fun createBitmap(): Bitmap {
        return Bitmap.createBitmap(
            bitmapBlockWidth.toInt(),
            bitmapBlockHeight.toInt(),
            bitmapConfig
        )
    }

    fun setBitmapSpace(aSpace: BitmapSpace) {
        if (myBitmaps == null) {
            resetBitmap()
        }
        val blockRight = currentBitmapSpace.left + currentBitmapSpace.width
        val blockBottom = currentBitmapSpace.top + currentBitmapSpace.height
        for (row in currentBitmapSpace.left..<blockRight) {
            for (column in currentBitmapSpace.top..<blockBottom) {
                if (!aSpace.contains(row, column)) {
                    removePositionBitmap(row, column)
                }
            }
        }
        currentBitmapSpace.set(aSpace)
    }

    fun removePositionBitmap(aPosition: Position) {
        removePositionBitmap(aPosition.row, aPosition.column)
    }

    fun removePositionBitmap(row: Int, column: Int) {
        if (bitmapContainsPosition(row, column)) {
            myBitmaps!![row][column] = null
        }
    }

    fun calculateBlock(left: Double, top: Double, width: Double, height: Double): BitmapSpace {
        val block = BitmapSpace()
        block.left = floor(left / bitmapBlockWidth).toInt()
        block.top = floor(top / bitmapBlockHeight).toInt()
        block.width = ((ceil((left + width) / bitmapBlockWidth).toInt()) - block.left) + 1
        block.height = ((ceil((top + height) / bitmapBlockHeight).toInt()) - block.top) + 1
        return block
    }

    fun bitmapContainsPosition(aPosition: Position): Boolean {
        return bitmapContainsPosition(aPosition.row, aPosition.column)
    }

    fun bitmapContainsPosition(row: Int, column: Int): Boolean {
        return myBitmaps != null && row >= 0 && row < myBitmaps!!.size && column >= 0 && column < myBitmaps!![row].size
    }

    private fun drawBitSpace1(row: Int, column: Int, cursorRow: Int, cursorColumn: Int) {
        var z = true
        drawer.paint.textSize = enTextSize.toInt().toFloat()
        val c = telnetFrame!!.getPositionData(row, column).toChar()
        val textColor = telnetFrame!!.getPositionTextColor(row, column)
        val backgroundColor = telnetFrame!!.getPositionBackgroundColor(row, column)
        drawer.textColor = textColor
        drawer.backgroundColor = backgroundColor
        drawer.bit = 1
        drawer.paint.typeface = enTypePace
        if (!blink || !telnetFrame!!.getPositionBlink(row, column)) {
            z = false
        }
        drawer.blink = z
        drawer.clip = 0.toByte()
        drawer.drawCharAtPosition(context, cursorRow, cursorColumn, c)
    }

    private fun drawSingleBitSpace2r1(row: Int, column: Int, cursorRow: Int, cursorColumn: Int) {
        drawer.paint.textSize = zhTextSize.toInt().toFloat()
        val upper = telnetFrame!!.getPositionData(row, column)
        val lower = telnetFrame!!.getPositionData(row, column + 1)
        val charData = (upper shl 8) + lower
        val c: Char = B2UEncoder.instance!!.encodeChar(charData.toChar())
        val textColor = telnetFrame!!.getPositionTextColor(row, column)
        val backgroundColor = telnetFrame!!.getPositionBackgroundColor(row, column)
        drawer.textColor = textColor
        drawer.backgroundColor = backgroundColor
        drawer.bit = 2
        drawer.paint.typeface = zhTypePace
        drawer.blink = blink && telnetFrame!!.getPositionBlink(row, column)
        drawer.clip = 0.toByte()
        drawer.drawCharAtPosition(context, cursorRow, cursorColumn, c)
    }

    private fun drawSingleBitSpace2r2(row: Int, column: Int, cursorRow: Int, cursorColumn: Int) {
        drawer.paint.textSize = zhTextSize.toInt().toFloat()
        val lower = telnetFrame!!.getPositionData(row, column)
        val upper = telnetFrame!!.getPositionData(row, column - 1)
        val charData = (upper shl 8) + lower
        val c: Char = B2UEncoder.instance!!.encodeChar(charData.toChar())
        val textColor = telnetFrame!!.getPositionTextColor(row, column)
        val backgroundColor = telnetFrame!!.getPositionBackgroundColor(row, column)
        drawer.textColor = textColor
        drawer.backgroundColor = backgroundColor
        drawer.bit = 2
        drawer.paint.typeface = zhTypePace
        drawer.blink = blink && telnetFrame!!.getPositionBlink(row, column)
        drawer.clip = 0.toByte()
        drawer.drawCharAtPosition(context, cursorRow, cursorColumn - 1, c)
    }

    private fun drawDoubleBitSpace2r1(row: Int, column: Int, cursorRow: Int, cursorColumn: Int) {
        drawer.paint.textSize = zhTextSize.toInt().toFloat()
        val upper = telnetFrame!!.getPositionData(row, column)
        val lower = telnetFrame!!.getPositionData(row, column + 1)
        val charData = (upper shl 8) + lower
        val c: Char = B2UEncoder.instance!!.encodeChar(charData.toChar())
        val textColor = telnetFrame!!.getPositionTextColor(row, column)
        val backgroundColor = telnetFrame!!.getPositionBackgroundColor(row, column)
        drawer.textColor = textColor
        drawer.backgroundColor = backgroundColor
        drawer.bit = 2
        drawer.paint.typeface = zhTypePace
        drawer.blink = blink && telnetFrame!!.getPositionBlink(row, column)
        drawer.clip = 1.toByte()
        drawer.drawCharAtPosition(context, cursorRow, cursorColumn, c)
    }

    private fun drawDoubleBitSpace2r2(row: Int, column: Int, cursorRow: Int, cursorColumn: Int) {
        drawer.paint.textSize = zhTextSize.toInt().toFloat()
        val lower = telnetFrame!!.getPositionData(row, column)
        val upper = telnetFrame!!.getPositionData(row, column - 1)
        val charData = (upper shl 8) + lower
        val c: Char = B2UEncoder.instance!!.encodeChar(charData.toChar())
        val textColor = telnetFrame!!.getPositionTextColor(row, column)
        val backgroundColor = telnetFrame!!.getPositionBackgroundColor(row, column)
        drawer.textColor = textColor
        drawer.backgroundColor = backgroundColor
        drawer.bit = 2
        drawer.paint.typeface = zhTypePace
        drawer.blink = blink && telnetFrame!!.getPositionBlink(row, column)
        drawer.clip = 2.toByte()
        drawer.drawCharAtPosition(context, cursorRow, cursorColumn - 1, c)
    }
}