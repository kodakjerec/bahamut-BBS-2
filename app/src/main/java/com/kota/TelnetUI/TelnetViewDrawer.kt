package com.kota.TelnetUI

import android.content.Context
import android.graphics.Canvas
import android.graphics.Paint
import android.graphics.Path
import android.util.TypedValue
import com.kota.Telnet.Model.TelnetFrame
import kotlin.math.ceil

class TelnetViewDrawer {
    
    companion object {
        const val CLIP_LEFT: Byte = 1
        const val CLIP_NONE: Byte = 0
        const val CLIP_RIGHT: Byte = 2
    }
    
    var backgroundColor: Int = 0
    var bit: Int = 1
    var blink: Boolean = false
    var blockHeight: Double = 0.0
    var blockWidth: Double = 0.0
    var canvas: Canvas? = null
    var clip: Byte = 0
    var horizontalUnit: Double = 0.0
    private var lineWidth: Int = 0
    var loc: Int = 1
    var originX: Int = 0
    var originY: Int = 0
    var paint: Paint = Paint()
    var radius: Double = 0.0
    var textBottomOffset: Int = 0
    var textColor: Int = 0
    var verticalUnit: Double = 0.0
    
    // 新增的屬性用於支援更完整的功能
    private var frame: TelnetFrame? = null
    private var zhTextSize: Double = 0.0
    private var enTextSize: Double = 0.0
    private var zhTypeface: android.graphics.Typeface? = null
    private var enTypeface: android.graphics.Typeface? = null
    
    private class TelnetViewBlock {
        var bottom: Float = 0f
        var height: Float = 0f
        var left: Float = 0f
        var right: Float = 0f
        var top: Float = 0f
        var width: Float = 0f
    }
    
    // 新增設定方法
    fun setFrame(frame: TelnetFrame) {
        this.frame = frame
    }
    
    fun setTextSize(zhSize: Double, enSize: Double) {
        zhTextSize = zhSize
        enTextSize = enSize
    }
    
    fun setTypeface(zhTypeface: android.graphics.Typeface?, enTypeface: android.graphics.Typeface?) {
        this.zhTypeface = zhTypeface
        this.enTypeface = enTypeface
    }
    
    fun setBlockSize(width: Double, height: Double) {
        blockWidth = width
        blockHeight = height
    }
    
    // 繪製字符在指定位置
    fun drawCharAtPosition(context: Context, row: Int, column: Int, char: Char) {
        val currentCanvas = canvas ?: return
        
        val block = TelnetViewBlock().apply {
            width = (blockWidth * bit).toFloat()
            height = blockHeight.toFloat()
            left = (originX + (blockWidth * column)).toFloat()
            right = left + width
            top = originY + (height * row)
            bottom = top + height
        }
        
        currentCanvas.save()
        
        when (clip) {
            CLIP_LEFT -> currentCanvas.clipRect(
                block.left, block.top, 
                block.left + blockWidth.toFloat(), block.bottom
            )
            CLIP_RIGHT -> currentCanvas.clipRect(
                block.left + blockWidth.toFloat(), block.top, 
                block.right, block.bottom
            )
            else -> currentCanvas.clipRect(
                block.left, block.top, block.right, block.bottom
            )
        }
        
        drawBackground(block)
        if (!blink) {
            drawTextAtPosition(context, block, char)
        }
        
        currentCanvas.restore()
    }
    
    // 新增的方法：直接繪製字符
    fun drawChar(canvas: Canvas, char: Char, attribute: Int, x: Float, y: Float, blink: Boolean) {
        this.canvas = canvas
        this.blink = blink
        
        // 根據屬性設定顏色
        textColor = getTextColor(attribute)
        backgroundColor = getBackgroundColor(attribute)
        
        // 繪製背景
        paint.color = backgroundColor
        canvas.drawRect(x, y, x + blockWidth.toFloat(), y + blockHeight.toFloat(), paint)
        
        // 如果不是閃爍狀態，繪製文字
        if (!blink || !isBlinkAttribute(attribute)) {
            paint.color = textColor
            drawCharacter(canvas, char, x, y)
        }
    }
    
    private fun drawBackground(block: TelnetViewBlock) {
        paint.color = backgroundColor
        canvas?.drawRect(block.left, block.top, block.right, block.bottom, paint)
    }
    
    private fun drawTextAtPosition(context: Context, block: TelnetViewBlock, char: Char) {
        if (lineWidth == 0) {
            lineWidth = ceil(
                TypedValue.applyDimension(
                    TypedValue.COMPLEX_UNIT_DIP,
                    1.0f,
                    context.resources.displayMetrics
                ).toDouble()
            ).toInt()
        }
        
        paint.color = textColor
        drawCharacter(canvas, char, block.left, block.top)
    }
    
    private fun drawCharacter(canvas: Canvas?, char: Char, x: Float, y: Float) {
        val currentCanvas = canvas ?: return
        
        when (char.code) {
            717 -> {
                // 特殊字符：底部填充
                currentCanvas.drawRect(
                    x, y + blockHeight.toFloat() - (blockHeight - verticalUnit * 8.0).toFloat(),
                    x + blockWidth.toFloat(), y + blockHeight.toFloat(), paint
                )
            }
            9472 -> {
                // 水平線
                val originY = (y + blockHeight / 2 - lineWidth / 2).toInt()
                currentCanvas.drawRect(
                    x, originY.toFloat(),
                    x + blockWidth.toFloat(), (lineWidth + originY).toFloat(), paint
                )
            }
            9585 -> {
                // 反斜線
                currentCanvas.drawLine(
                    x, y + blockHeight.toFloat(),
                    x + blockWidth.toFloat(), y, paint
                )
            }
            9586, 65340 -> {
                // 正斜線
                currentCanvas.drawLine(
                    x, y,
                    x + blockWidth.toFloat(), y + blockHeight.toFloat(), paint
                )
            }
            9587 -> {
                // 交叉線
                currentCanvas.drawLine(
                    x, y + blockHeight.toFloat(),
                    x + blockWidth.toFloat(), y, paint
                )
                currentCanvas.drawLine(
                    x, y,
                    x + blockWidth.toFloat(), y + blockHeight.toFloat(), paint
                )
            }
            in 9601..9608 -> {
                // 不同高度的填充塊
                val level = char.code - 9600
                val fillHeight = (verticalUnit * (9 - level)).toFloat()
                currentCanvas.drawRect(
                    x, y + blockHeight.toFloat() - fillHeight,
                    x + blockWidth.toFloat(), y + blockHeight.toFloat(), paint
                )
            }
            in 9609..9615 -> {
                // 不同寬度的填充塊
                val level = char.code - 9608
                val fillWidth = (horizontalUnit * (8 - level)).toFloat()
                currentCanvas.drawRect(
                    x, y,
                    x + fillWidth, y + blockHeight.toFloat(), paint
                )
            }
            9621 -> {
                // 右邊線
                currentCanvas.drawRect(
                    x + blockWidth.toFloat() - lineWidth, y,
                    x + blockWidth.toFloat(), y + blockHeight.toFloat(), paint
                )
            }
            9675 -> {
                // 空心圓
                paint.style = Paint.Style.STROKE
                currentCanvas.drawCircle(
                    x + radius.toFloat(), y + radius.toFloat(),
                    radius.toFloat(), paint
                )
                paint.style = Paint.Style.FILL
            }
            9679 -> {
                // 實心圓
                currentCanvas.drawCircle(
                    x + radius.toFloat(), y + radius.toFloat(),
                    radius.toFloat(), paint
                )
            }
            9698 -> {
                // 右下三角
                val path = Path().apply {
                    moveTo(x, y + blockHeight.toFloat())
                    lineTo(x + blockWidth.toFloat(), y)
                    lineTo(x + blockWidth.toFloat(), y + blockHeight.toFloat())
                    lineTo(x, y + blockHeight.toFloat())
                }
                currentCanvas.drawPath(path, paint)
            }
            9699 -> {
                // 左下三角
                val path = Path().apply {
                    moveTo(x, y + blockHeight.toFloat())
                    lineTo(x, y)
                    lineTo(x + blockWidth.toFloat(), y + blockHeight.toFloat())
                    lineTo(x, y + blockHeight.toFloat())
                }
                currentCanvas.drawPath(path, paint)
            }
            9700 -> {
                // 左上三角
                val path = Path().apply {
                    moveTo(x, y + blockHeight.toFloat())
                    lineTo(x, y)
                    lineTo(x + blockWidth.toFloat(), y)
                    lineTo(x, y + blockHeight.toFloat())
                }
                currentCanvas.drawPath(path, paint)
            }
            9701 -> {
                // 右上三角
                val path = Path().apply {
                    moveTo(x, y)
                    lineTo(x + blockWidth.toFloat(), y)
                    lineTo(x + blockWidth.toFloat(), y + blockHeight.toFloat())
                    lineTo(x, y)
                }
                currentCanvas.drawPath(path, paint)
            }
            65343 -> {
                // 底線
                currentCanvas.drawRect(
                    x, y + blockHeight.toFloat() - lineWidth,
                    x + blockWidth.toFloat(), y + blockHeight.toFloat(), paint
                )
            }
            65507 -> {
                // 上線
                currentCanvas.drawRect(
                    x, y,
                    x + blockWidth.toFloat(), y + lineWidth, paint
                )
            }
            else -> {
                // 一般文字
                currentCanvas.drawText(
                    charArrayOf(char), 0, 1,
                    x, y + blockHeight.toFloat() - textBottomOffset, paint
                )
            }
        }
    }
    
    private fun getTextColor(attribute: Int): Int {
        // 從屬性中提取文字顏色
        // 這裡需要根據實際的屬性格式來實現
        return android.graphics.Color.WHITE // 預設白色
    }
    
    private fun getBackgroundColor(attribute: Int): Int {
        // 從屬性中提取背景顏色
        // 這裡需要根據實際的屬性格式來實現
        return android.graphics.Color.BLACK // 預設黑色
    }
    
    private fun isBlinkAttribute(attribute: Int): Boolean {
        // 檢查是否為閃爍屬性
        // 這裡需要根據實際的屬性格式來實現
        return (attribute and 0x80) != 0 // 假設第8位是閃爍位
    }
}
