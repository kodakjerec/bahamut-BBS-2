package com.kota.TelnetUI

import android.annotation.SuppressLint
import android.content.Context
import android.graphics.*
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
import java.util.*
import kotlin.math.ceil
import kotlin.math.floor

class TelnetView @JvmOverloads constructor(
    context: Context,
    attrs: AttributeSet? = null,
    defStyle: Int = 0
) : View(context, attrs, defStyle) {
    
    private val defaultTextSize: Float = 20.0f
    private var bitmapBlockHeight: Double = 0.0
    private var bitmapBlockWidth: Double = 0.0
    private val bitmapConfig: Bitmap.Config = Bitmap.Config.RGB_565
    private var bitmapSpaceColumn: Int = 0
    private var bitmapSpaceRow: Int = 0
    private val bitmapSpaceX: Int = 8
    private val bitmapSpaceY: Int = 8
    private var bitmaps: Array<Array<Bitmap?>>? = null
    private var blink: Boolean = false
    private val blinkList: Vector<Position> = Vector()
    private var blinkThread: BlinkThread? = null
    private var blockHeight: Double = 0.0
    private var blockWidth: Double = 0.0
    private var clip: Rect = Rect()
    private var column: Int = 0
    private val currentSpace: BitmapSpace = BitmapSpace()
    private var drawHeight: Int = 0
    private val drawSeparatorLine: Boolean = true
    private var drawWidth: Int = 0
    private val drawer: TelnetViewDrawer = TelnetViewDrawer()
    private var enTextSize: Double = 0.0
    private var frame: TelnetFrame? = null
    private var handler: Handler? = null
    private var horizontalUnit: Double = 0.0
    private var matrix: Matrix = Matrix()
    private val originX: Int = 0
    private val originY: Int = 0
    private var paint: Paint = Paint()
    private var radius: Double = 0.0
    private var row: Int = 0
    private var scaleX: Float = 1.0f
    private var scaleY: Float = 1.0f
    private var typeface: Typeface? = null
    private var verticalUnit: Double = 0.0
    private var zhTextSize: Double = 0.0
    private var enTypeface: Typeface? = null
    private var zhTypeface: Typeface? = null
    
    class BitmapSpace {
        var height: Int = 0
        var left: Int = 0
        var top: Int = 0
        var width: Int = 0
        
        fun contains(row: Int, column: Int): Boolean {
            return row >= left && row < left + width && 
                   column >= top && column < top + height
        }
        
        fun set(block: BitmapSpace) {
            left = block.left
            top = block.top
            width = block.width
            height = block.height
        }
        
        fun isEquals(block: BitmapSpace): Boolean {
            return left == block.left && top == block.top && 
                   width == block.width && height == block.height
        }
        
        override fun toString(): String {
            return "($left , $top) ($width , $height)"
        }
    }
    
    class Position(val row: Int, val column: Int) {
        fun isEquals(position: Position): Boolean {
            return row == position.row && column == position.column
        }
    }
    
    inner class BlinkThread : Thread() {
        @Volatile
        private var isRunning = true
        
        fun stopBlink() {
            isRunning = false
        }
        
        override fun run() {
            while (isRunning) {
                try {
                    sleep(1000L)
                    handler?.sendEmptyMessage(0)
                } catch (e: InterruptedException) {
                    Log.e(javaClass.simpleName, e.message ?: "Unknown error")
                    return
                }
            }
        }
    }
    
    init {
        initial()
    }
    
    private fun startBlink() {
        if (blinkThread != null) {
            blinkThread?.stopBlink()
            blinkThread = null
        }
        
        handler = @SuppressLint("HandlerLeak")
        object : Handler(Looper.getMainLooper()) {
            override fun handleMessage(msg: Message) {
                blink = !blink
                invalidate()
            }
        }
        
        blinkThread = BlinkThread()
        blinkThread?.start()
    }
    
    private fun stopBlink() {
        blinkThread?.stopBlink()
        blinkThread = null
        handler = null
    }
    
    private fun initial() {
        paint.isAntiAlias = true
        matrix = Matrix()
    }
    
    fun setFrame(telnetFrame: TelnetFrame?) {
        frame = telnetFrame
        cleanBitmap()
        resetBitmap()
        invalidate()
    }
    
    fun getFrame(): TelnetFrame? = frame
    
    fun setTypeface(typeface: Typeface?) {
        this.typeface = typeface
        val density = context.resources.displayMetrics.density
        val textSize = TypedValue.applyDimension(
            TypedValue.COMPLEX_UNIT_SP,
            defaultTextSize,
            context.resources.displayMetrics
        )
        
        paint.typeface = typeface
        paint.textSize = textSize
        
        val textBounds = Rect()
        paint.getTextBounds("中", 0, 1, textBounds)
        zhTextSize = textBounds.height().toDouble()
        
        paint.getTextBounds("A", 0, 1, textBounds)
        enTextSize = textBounds.height().toDouble()
        
        cleanBitmap()
        calculateLayout()
        resetBitmap()
        invalidate()
    }
    
    fun calculateLayout() {
        val currentFrame = frame ?: return
        
        row = currentFrame.getRowCount()
        column = currentFrame.getColumnCount()
        
        if (row == 0 || column == 0) return
        
        val density = context.resources.displayMetrics.density
        
        // 計算文字大小
        blockWidth = zhTextSize * 1.1
        blockHeight = zhTextSize * 1.3
        
        // 計算總尺寸
        val totalWidth = (blockWidth * column).toInt()
        val totalHeight = (blockHeight * row).toInt()
        
        drawWidth = totalWidth
        drawHeight = totalHeight
        
        // 計算縮放
        val viewWidth = width
        val viewHeight = height
        
        if (viewWidth > 0 && viewHeight > 0) {
            scaleX = viewWidth.toFloat() / totalWidth
            scaleY = viewHeight.toFloat() / totalHeight
            
            // 保持等比例縮放
            val scale = minOf(scaleX, scaleY)
            scaleX = scale
            scaleY = scale
        }
        
        // 計算單位大小
        horizontalUnit = blockWidth
        verticalUnit = blockHeight
        
        // 重新載入繪圖器
        reloadDrawer()
    }
    
    private fun reloadDrawer() {
        val currentFrame = frame ?: return
        drawer.setFrame(currentFrame)
        drawer.setTextSize(zhTextSize, enTextSize)
        drawer.setTypeface(zhTypeface, enTypeface)
        drawer.setBlockSize(blockWidth, blockHeight)
    }
    
    private fun drawTelnet(
        canvas: Canvas,
        position: Position,
        rowStart: Int,
        rowEnd: Int,
        columnStart: Int,
        columnEnd: Int
    ) {
        val currentFrame = frame ?: return
        
        canvas.save()
        canvas.scale(scaleX, scaleY)
        
        for (r in rowStart until rowEnd) {
            for (c in columnStart until columnEnd) {
                if (r < row && c < column) {
                    val x = (c * horizontalUnit).toFloat()
                    val y = (r * verticalUnit).toFloat()
                    
                    val char = currentFrame.getChar(r, c)
                    val attribute = currentFrame.getAttribute(r, c)
                    
                    drawer.drawChar(canvas, char, attribute, x, y, blink)
                }
            }
        }
        
        canvas.restore()
    }
    
    override fun onDraw(canvas: Canvas) {
        super.onDraw(canvas)
        
        val currentFrame = frame ?: return
        
        if (row == 0 || column == 0) return
        
        // 清空背景
        canvas.drawColor(Color.BLACK)
        
        // 繪製內容
        val position = Position(0, 0)
        drawTelnet(canvas, position, 0, row, 0, column)
        
        // 繪製分隔線（如果需要）
        if (drawSeparatorLine) {
            paint.color = Color.GRAY
            paint.strokeWidth = 1f
            
            // 垂直線
            for (c in 1 until column) {
                val x = (c * horizontalUnit * scaleX).toFloat()
                canvas.drawLine(x, 0f, x, (row * verticalUnit * scaleY).toFloat(), paint)
            }
            
            // 水平線
            for (r in 1 until row) {
                val y = (r * verticalUnit * scaleY).toFloat()
                canvas.drawLine(0f, y, (column * horizontalUnit * scaleX).toFloat(), y, paint)
            }
        }
    }
    
    override fun onSizeChanged(w: Int, h: Int, oldw: Int, oldh: Int) {
        super.onSizeChanged(w, h, oldw, oldh)
        calculateLayout()
        invalidate()
    }
    
    private fun cleanBitmap() {
        bitmaps?.let { bitmapArray ->
            for (row in bitmapArray) {
                for (bitmap in row) {
                    bitmap?.recycle()
                }
            }
        }
        bitmaps = null
    }
    
    private fun resetBitmap() {
        if (row <= 0 || column <= 0) return
        
        bitmapSpaceRow = ceil(row.toDouble() / bitmapSpaceY).toInt()
        bitmapSpaceColumn = ceil(column.toDouble() / bitmapSpaceX).toInt()
        
        bitmapBlockWidth = bitmapSpaceX * horizontalUnit
        bitmapBlockHeight = bitmapSpaceY * verticalUnit
        
        // 創建點陣圖陣列
        bitmaps = Array(bitmapSpaceRow) { Array<Bitmap?>(bitmapSpaceColumn) { null } }
    }
    
    override fun onDetachedFromWindow() {
        super.onDetachedFromWindow()
        stopBlink()
        cleanBitmap()
    }
}
