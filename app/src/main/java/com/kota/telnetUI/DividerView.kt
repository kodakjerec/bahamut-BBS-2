package com.kota.telnetUI

import android.content.Context
import android.graphics.Canvas
import android.graphics.Paint
import android.util.AttributeSet
import android.view.View
import kotlin.math.ceil
import kotlin.math.floor

class DividerView : View {
    var paint: Paint = Paint()

    constructor(context: Context?) : super(context)

    constructor(context: Context?, attrs: AttributeSet?, defStyle: Int) : super(
        context,
        attrs,
        defStyle
    )

    constructor(context: Context?, attrs: AttributeSet?) : super(context, attrs)

    override fun onDraw(canvas: Canvas) {
        super.onDraw(canvas)
        val lineSize = ceil(((width.toFloat()) / (80f)).toDouble()).toFloat()
        val originX =
            floor((((width.toFloat()) - ((79f) * lineSize)) / 2.0f).toDouble()).toFloat()
        var i = 0
        while (i < 79) {
            this.paint.color = -12566464
            val canvas2 = canvas
            canvas2.drawLine(
                originX + ((i.toFloat()) * lineSize),
                0.0f,
                ((i.toFloat()) * lineSize) + originX + lineSize,
                0.0f,
                this.paint
            )
            val i2 = i + 1
            this.paint.color = 0
            canvas.drawLine(
                originX + ((i2.toFloat()) * lineSize),
                0.0f,
                ((i2.toFloat()) * lineSize) + originX + lineSize,
                0.0f,
                this.paint
            )
            i = i2 + 1
        }
    }
}
