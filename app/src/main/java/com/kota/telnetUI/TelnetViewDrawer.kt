package com.kota.telnetUI

import android.content.Context
import android.graphics.Canvas
import android.graphics.Paint
import android.graphics.Path
import android.util.TypedValue
import kotlin.math.ceil
import androidx.core.graphics.withSave

class TelnetViewDrawer {
    var backgroundColor: Int = 0
    var bit: Int = 1
    var blink: Boolean = false
    var blockHeight: Double = 0.0
    var blockWidth: Double = 0.0
    var canvas: Canvas? = null
    var clip: Byte = 0
    var horizontalUnit: Double = 0.0
    private var lineWidth = 0
    var loc: Int = 1
    var originX: Int = 0
    var originY: Int = 0
    var paint: Paint = Paint()
    var radius: Double = 0.0
    var textBottomOffset: Int = 0
    var textColor: Int = 0
    var verticalUnit: Double = 0.0

    private class TelnetViewBlock {
        var bottom: Float = 0f
        var height: Float = 0f
        var left: Float = 0f
        var right: Float = 0f
        var top: Float = 0f
        var width: Float = 0f
    }

    // 繪圖: 塗上文字
    fun drawCharAtPosition(aContext: Context, row: Int, column: Int, c: Char) {
        val block = TelnetViewBlock()
        block.width = (this.blockWidth * this.bit).toFloat()
        block.height = this.blockHeight.toFloat()
        block.left = (this.originX + (this.blockWidth * column)).toFloat()
        block.right = block.left + block.width
        block.top = this.originY + (block.height * row)
        block.bottom = block.top + block.height

        this.canvas!!.withSave {
            when (this@TelnetViewDrawer.clip) {
                CLIP_LEFT -> clipRect(
                    block.left,
                    block.top,
                    block.left + this@TelnetViewDrawer.blockWidth.toFloat(),
                    block.bottom
                )

                CLIP_RIGHT -> clipRect(
                    block.left + this@TelnetViewDrawer.blockWidth.toFloat(),
                    block.top,
                    block.right,
                    block.bottom
                )

                else -> clipRect(block.left, block.top, block.right, block.bottom)
            }
            drawBackground(block)
            if (!this@TelnetViewDrawer.blink) {
                drawTextAtPosition(aContext, block, c)
            }
        }
    }

    private fun drawBackground(block: TelnetViewBlock) {
        this.paint.color = this.backgroundColor
        this.canvas!!.drawRect(block.left, block.top, block.right, block.bottom, this.paint)
    }

    private fun drawTextAtPosition(aContext: Context, block: TelnetViewBlock, c: Char) {
        if (this.lineWidth == 0) {
            this.lineWidth = ceil(
                TypedValue.applyDimension(
                    TypedValue.COMPLEX_UNIT_DIP,
                    1.0f,
                    aContext.resources.displayMetrics
                ).toDouble()
            ).toInt()
        }
        this.paint.color = this.textColor
        when (c.code) {
            717 -> {
                this.canvas!!.drawRect(
                    block.left,
                    (block.bottom - (((block.height) - (this.verticalUnit * 8.0f)).toInt())),
                    block.right,
                    block.bottom,
                    this.paint
                )
                return
            }

            9472 -> {
                val originY = ((block.top + (block.height / 2)) - (this.lineWidth / 2)).toInt()
                this.canvas!!.drawRect(
                    block.left,
                    originY.toFloat(),
                    block.right,
                    (this.lineWidth + originY).toFloat(),
                    this.paint
                )
                return
            }

            9585 -> {
                this.canvas!!.drawLine(block.left, block.bottom, block.right, block.top, this.paint)
                return
            }

            9586, 65340 -> {
                this.canvas!!.drawLine(block.left, block.top, block.right, block.bottom, this.paint)
                return
            }

            9587 -> {
                this.canvas!!.drawLine(block.left, block.bottom, block.right, block.top, this.paint)
                this.canvas!!.drawLine(block.left, block.top, block.right, block.bottom, this.paint)
                return
            }

            9601 -> {
                this.canvas!!.drawRect(
                    block.left,
                    (block.bottom - (((block.height) - (this.verticalUnit * 7.0f)).toInt())),
                    block.right,
                    block.bottom,
                    this.paint
                )
                return
            }

            9602 -> {
                this.canvas!!.drawRect(
                    block.left,
                    (block.bottom - (((block.height) - (this.verticalUnit * 6.0f)).toInt())),
                    block.right,
                    block.bottom,
                    this.paint
                )
                return
            }

            9603 -> {
                this.canvas!!.drawRect(
                    block.left,
                    (block.bottom - (((block.height) - (this.verticalUnit * 5.0f)).toInt())),
                    block.right,
                    block.bottom,
                    this.paint
                )
                return
            }

            9604 -> {
                this.canvas!!.drawRect(
                    block.left,
                    (block.bottom - (((block.height) - (this.verticalUnit * 4.0f)).toInt())),
                    block.right,
                    block.bottom,
                    this.paint
                )
                return
            }

            9605 -> {
                this.canvas!!.drawRect(
                    block.left,
                    (block.bottom - (((block.height) - (this.verticalUnit * 3.0f)).toInt())),
                    block.right,
                    block.bottom,
                    this.paint
                )
                return
            }

            9606 -> {
                this.canvas!!.drawRect(
                    block.left,
                    (block.bottom - (((block.height) - (this.verticalUnit * 2.0f)).toInt())),
                    block.right,
                    block.bottom,
                    this.paint
                )
                return
            }

            9607 -> {
                this.canvas!!.drawRect(
                    block.left,
                    (block.bottom - (((block.height) - (this.verticalUnit * 1.0f)).toInt())),
                    block.right,
                    block.bottom,
                    this.paint
                )
                return
            }

            9608 -> {
                this.canvas!!.drawRect(block.left, block.top, block.right, block.bottom, this.paint)
                return
            }

            9609 -> {
                this.canvas!!.drawRect(
                    block.left,
                    block.top,
                    (block.left + ((this.horizontalUnit * 7.0f).toInt())),
                    block.bottom,
                    this.paint
                )
                return
            }

            9610 -> {
                this.canvas!!.drawRect(
                    block.left,
                    block.top,
                    (block.left + ((this.horizontalUnit * 6.0f).toInt())),
                    block.bottom,
                    this.paint
                )
                return
            }

            9611 -> {
                this.canvas!!.drawRect(
                    block.left,
                    block.top,
                    (block.left + ((this.horizontalUnit * 5.0f).toInt())),
                    block.bottom,
                    this.paint
                )
                return
            }

            9612 -> {
                this.canvas!!.drawRect(
                    block.left,
                    block.top,
                    (block.left + ((this.horizontalUnit * 4.0f).toInt())),
                    block.bottom,
                    this.paint
                )
                return
            }

            9613 -> {
                this.canvas!!.drawRect(
                    block.left,
                    block.top,
                    (block.left + ((this.horizontalUnit * 3.0f).toInt())),
                    block.bottom,
                    this.paint
                )
                return
            }

            9614 -> {
                this.canvas!!.drawRect(
                    block.left,
                    block.top,
                    (block.left + ((this.horizontalUnit * 2.0f).toInt())),
                    block.bottom,
                    this.paint
                )
                return
            }

            9615 -> {
                this.canvas!!.drawRect(
                    block.left,
                    block.top,
                    (block.left + ((this.horizontalUnit * 1.0f).toInt())),
                    block.bottom,
                    this.paint
                )
                return
            }

            9621 -> {
                this.canvas!!.drawRect(
                    (block.right - this.lineWidth),
                    block.top,
                    block.right,
                    block.bottom,
                    this.paint
                )
                return
            }

            9675 -> {
                this.paint.style = Paint.Style.STROKE
                this.canvas!!.drawCircle(
                    block.left + this.radius.toFloat(),
                    block.top + this.radius.toFloat(),
                    this.radius.toFloat(),
                    this.paint
                )
                this.paint.style = Paint.Style.FILL
                return
            }

            9679 -> {
                this.canvas!!.drawCircle(
                    block.left + this.radius.toFloat(),
                    block.top + this.radius.toFloat(),
                    this.radius.toFloat(),
                    this.paint
                )
                return
            }

            9698 -> {
                val path = Path()
                path.moveTo(block.left, block.bottom)
                path.lineTo(block.right, block.top)
                path.lineTo(block.right, block.bottom)
                path.lineTo(block.left, block.bottom)
                this.canvas!!.drawPath(path, this.paint)
                return
            }

            9699 -> {
                val path2 = Path()
                path2.moveTo(block.left, block.bottom)
                path2.lineTo(block.left, block.top)
                path2.lineTo(block.right, block.bottom)
                path2.lineTo(block.left, block.bottom)
                this.canvas!!.drawPath(path2, this.paint)
                return
            }

            9700 -> {
                val path3 = Path()
                path3.moveTo(block.left, block.bottom)
                path3.lineTo(block.left, block.top)
                path3.lineTo(block.right, block.top)
                path3.lineTo(block.left, block.bottom)
                this.canvas!!.drawPath(path3, this.paint)
                return
            }

            9701 -> {
                val path4 = Path()
                path4.moveTo(block.left, block.top)
                path4.lineTo(block.right, block.top)
                path4.lineTo(block.right, block.bottom)
                path4.lineTo(block.left, block.top)
                this.canvas!!.drawPath(path4, this.paint)
                return
            }

            65343 -> {
                this.canvas!!.drawRect(
                    block.left,
                    (block.bottom - this.lineWidth),
                    block.right,
                    block.bottom,
                    this.paint
                )
                return
            }

            65507 -> {
                this.canvas!!.drawRect(
                    block.left,
                    block.top,
                    block.right,
                    (block.top + this.lineWidth),
                    this.paint
                )
                return
            }

            else -> this.canvas!!.drawText(
                charArrayOf(c),
                0,
                1,
                block.left,
                (block.bottom - this.textBottomOffset),
                this.paint
            )
        }
    }

    companion object {
        const val CLIP_LEFT: Byte = 1
        const val CLIP_NONE: Byte = 0
        const val CLIP_RIGHT: Byte = 2
    }
}
