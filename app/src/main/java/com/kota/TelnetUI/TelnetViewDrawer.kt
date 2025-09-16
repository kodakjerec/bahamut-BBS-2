package com.kota.TelnetUI

import android.content.Context
import android.graphics.Canvas
import android.graphics.Paint
import android.graphics.Path
import android.util.TypedValue
import kotlin.math.ceil

class TelnetViewDrawer {
    var backgroundColor: Int = 0
    var bit: Int = 1
    var blink: Boolean = false
    var blockHeight: Double = 0.0
    var blockWidth: Double = 0.0
    var canvas: Canvas? = null
    var clip: Byte = 0
    var horizontalUnit: Double = 0.0
    private var line_width = 0
    var loc: Int = 1
    var originX: Int = 0
    var originY: Int = 0
    var paint: Paint = Paint()
    var radius: Double = 0.0
    var textBottomOffset: Int = 0
    var textColor: Int = 0
    var verticalUnit: Double = 0.0

    private class TelnetViewBlock {
        var Bottom: Float = 0f
        var Height: Float = 0f
        var Left: Float = 0f
        var Right: Float = 0f
        var Top: Float = 0f
        var Width: Float = 0f
    }

    // 繪圖: 塗上文字
    fun drawCharAtPosition(aContext: Context, row: Int, column: Int, c: Char) {
        val block = TelnetViewBlock()
        block.Width = (this.blockWidth * this.bit).toFloat()
        block.Height = this.blockHeight.toFloat()
        block.Left = (this.originX + (this.blockWidth * column)).toFloat()
        block.Right = block.Left + block.Width
        block.Top = this.originY + (block.Height * row)
        block.Bottom = block.Top + block.Height
        this.canvas!!.save()
        when (this.clip) {
            1 -> this.canvas!!.clipRect(
                block.Left,
                block.Top,
                block.Left + this.blockWidth.toFloat(),
                block.Bottom
            )

            2 -> this.canvas!!.clipRect(
                block.Left + this.blockWidth.toFloat(),
                block.Top,
                block.Right,
                block.Bottom
            )

            else -> this.canvas!!.clipRect(block.Left, block.Top, block.Right, block.Bottom)
        }
        drawBackground(block)
        if (!this.blink) {
            drawTextAtPosition(aContext, block, c)
        }
        this.canvas!!.restore()
    }

    private fun drawBackground(block: TelnetViewBlock) {
        this.paint.setColor(this.backgroundColor)
        this.canvas!!.drawRect(block.Left, block.Top, block.Right, block.Bottom, this.paint)
    }

    private fun drawTextAtPosition(aContext: Context, block: TelnetViewBlock, c: Char) {
        if (this.line_width == 0) {
            this.line_width = ceil(
                TypedValue.applyDimension(
                    TypedValue.COMPLEX_UNIT_DIP,
                    1.0f,
                    aContext.getResources().getDisplayMetrics()
                ).toDouble()
            ).toInt()
        }
        this.paint.setColor(this.textColor)
        when (c) {
            717 -> {
                this.canvas!!.drawRect(
                    block.Left,
                    (block.Bottom - (((block.Height) - (this.verticalUnit * 8.0f)).toInt())),
                    block.Right,
                    block.Bottom,
                    this.paint
                )
                return
            }

            9472 -> {
                val origin_y = ((block.Top + (block.Height / 2)) - (this.line_width / 2)).toInt()
                this.canvas!!.drawRect(
                    block.Left,
                    origin_y.toFloat(),
                    block.Right,
                    (this.line_width + origin_y).toFloat(),
                    this.paint
                )
                return
            }

            9585 -> {
                this.canvas!!.drawLine(block.Left, block.Bottom, block.Right, block.Top, this.paint)
                return
            }

            9586, 65340 -> {
                this.canvas!!.drawLine(block.Left, block.Top, block.Right, block.Bottom, this.paint)
                return
            }

            9587 -> {
                this.canvas!!.drawLine(block.Left, block.Bottom, block.Right, block.Top, this.paint)
                this.canvas!!.drawLine(block.Left, block.Top, block.Right, block.Bottom, this.paint)
                return
            }

            9601 -> {
                this.canvas!!.drawRect(
                    block.Left,
                    (block.Bottom - (((block.Height) - (this.verticalUnit * 7.0f)).toInt())),
                    block.Right,
                    block.Bottom,
                    this.paint
                )
                return
            }

            9602 -> {
                this.canvas!!.drawRect(
                    block.Left,
                    (block.Bottom - (((block.Height) - (this.verticalUnit * 6.0f)).toInt())),
                    block.Right,
                    block.Bottom,
                    this.paint
                )
                return
            }

            9603 -> {
                this.canvas!!.drawRect(
                    block.Left,
                    (block.Bottom - (((block.Height) - (this.verticalUnit * 5.0f)).toInt())),
                    block.Right,
                    block.Bottom,
                    this.paint
                )
                return
            }

            9604 -> {
                this.canvas!!.drawRect(
                    block.Left,
                    (block.Bottom - (((block.Height) - (this.verticalUnit * 4.0f)).toInt())),
                    block.Right,
                    block.Bottom,
                    this.paint
                )
                return
            }

            9605 -> {
                this.canvas!!.drawRect(
                    block.Left,
                    (block.Bottom - (((block.Height) - (this.verticalUnit * 3.0f)).toInt())),
                    block.Right,
                    block.Bottom,
                    this.paint
                )
                return
            }

            9606 -> {
                this.canvas!!.drawRect(
                    block.Left,
                    (block.Bottom - (((block.Height) - (this.verticalUnit * 2.0f)).toInt())),
                    block.Right,
                    block.Bottom,
                    this.paint
                )
                return
            }

            9607 -> {
                this.canvas!!.drawRect(
                    block.Left,
                    (block.Bottom - (((block.Height) - (this.verticalUnit * 1.0f)).toInt())),
                    block.Right,
                    block.Bottom,
                    this.paint
                )
                return
            }

            9608 -> {
                this.canvas!!.drawRect(block.Left, block.Top, block.Right, block.Bottom, this.paint)
                return
            }

            9609 -> {
                this.canvas!!.drawRect(
                    block.Left,
                    block.Top,
                    (block.Left + ((this.horizontalUnit * 7.0f).toInt())),
                    block.Bottom,
                    this.paint
                )
                return
            }

            9610 -> {
                this.canvas!!.drawRect(
                    block.Left,
                    block.Top,
                    (block.Left + ((this.horizontalUnit * 6.0f).toInt())),
                    block.Bottom,
                    this.paint
                )
                return
            }

            9611 -> {
                this.canvas!!.drawRect(
                    block.Left,
                    block.Top,
                    (block.Left + ((this.horizontalUnit * 5.0f).toInt())),
                    block.Bottom,
                    this.paint
                )
                return
            }

            9612 -> {
                this.canvas!!.drawRect(
                    block.Left,
                    block.Top,
                    (block.Left + ((this.horizontalUnit * 4.0f).toInt())),
                    block.Bottom,
                    this.paint
                )
                return
            }

            9613 -> {
                this.canvas!!.drawRect(
                    block.Left,
                    block.Top,
                    (block.Left + ((this.horizontalUnit * 3.0f).toInt())),
                    block.Bottom,
                    this.paint
                )
                return
            }

            9614 -> {
                this.canvas!!.drawRect(
                    block.Left,
                    block.Top,
                    (block.Left + ((this.horizontalUnit * 2.0f).toInt())),
                    block.Bottom,
                    this.paint
                )
                return
            }

            9615 -> {
                this.canvas!!.drawRect(
                    block.Left,
                    block.Top,
                    (block.Left + ((this.horizontalUnit * 1.0f).toInt())),
                    block.Bottom,
                    this.paint
                )
                return
            }

            9621 -> {
                this.canvas!!.drawRect(
                    (block.Right - this.line_width),
                    block.Top,
                    block.Right,
                    block.Bottom,
                    this.paint
                )
                return
            }

            9675 -> {
                this.paint.setStyle(Paint.Style.STROKE)
                this.canvas!!.drawCircle(
                    block.Left + this.radius.toFloat(),
                    block.Top + this.radius.toFloat(),
                    this.radius.toFloat(),
                    this.paint
                )
                this.paint.setStyle(Paint.Style.FILL)
                return
            }

            9679 -> {
                this.canvas!!.drawCircle(
                    block.Left + this.radius.toFloat(),
                    block.Top + this.radius.toFloat(),
                    this.radius.toFloat(),
                    this.paint
                )
                return
            }

            9698 -> {
                val path = Path()
                path.moveTo(block.Left, block.Bottom)
                path.lineTo(block.Right, block.Top)
                path.lineTo(block.Right, block.Bottom)
                path.lineTo(block.Left, block.Bottom)
                this.canvas!!.drawPath(path, this.paint)
                return
            }

            9699 -> {
                val path2 = Path()
                path2.moveTo(block.Left, block.Bottom)
                path2.lineTo(block.Left, block.Top)
                path2.lineTo(block.Right, block.Bottom)
                path2.lineTo(block.Left, block.Bottom)
                this.canvas!!.drawPath(path2, this.paint)
                return
            }

            9700 -> {
                val path3 = Path()
                path3.moveTo(block.Left, block.Bottom)
                path3.lineTo(block.Left, block.Top)
                path3.lineTo(block.Right, block.Top)
                path3.lineTo(block.Left, block.Bottom)
                this.canvas!!.drawPath(path3, this.paint)
                return
            }

            9701 -> {
                val path4 = Path()
                path4.moveTo(block.Left, block.Top)
                path4.lineTo(block.Right, block.Top)
                path4.lineTo(block.Right, block.Bottom)
                path4.lineTo(block.Left, block.Top)
                this.canvas!!.drawPath(path4, this.paint)
                return
            }

            65343 -> {
                this.canvas!!.drawRect(
                    block.Left,
                    (block.Bottom - this.line_width),
                    block.Right,
                    block.Bottom,
                    this.paint
                )
                return
            }

            65507 -> {
                this.canvas!!.drawRect(
                    block.Left,
                    block.Top,
                    block.Right,
                    (block.Top + this.line_width),
                    this.paint
                )
                return
            }

            else -> this.canvas!!.drawText(
                charArrayOf(c),
                0,
                1,
                block.Left,
                (block.Bottom - this.textBottomOffset),
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
