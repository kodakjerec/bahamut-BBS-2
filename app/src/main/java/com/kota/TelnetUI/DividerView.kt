package com.kota.TelnetUI

import android.content.Context
import android.graphics.Canvas
import android.graphics.Paint
import android.util.AttributeSet
import android.view.View
import kotlin.math.ceil
import kotlin.math.floor

class DividerView : View {
    var _paint: Paint = Paint()

    constructor(context: Context?) : super(context)

    constructor(context: Context?, attrs: AttributeSet?, defStyle: Int) : super(
        context,
        attrs,
        defStyle
    )

    constructor(context: Context?, attrs: AttributeSet?) : super(context, attrs)

    override fun onDraw(canvas: Canvas) {
        super.onDraw(canvas)
        val line_size = ceil(((getWidth().toFloat()) / (80f)).toDouble()).toFloat()
        val origin_x =
            floor((((getWidth().toFloat()) - ((79f) * line_size)) / 2.0f).toDouble()).toFloat()
        var i = 0
        while (i < 79) {
            this._paint.setColor(-12566464)
            val canvas2 = canvas
            canvas2.drawLine(
                origin_x + ((i.toFloat()) * line_size),
                0.0f,
                ((i.toFloat()) * line_size) + origin_x + line_size,
                0.0f,
                this._paint
            )
            val i2 = i + 1
            this._paint.setColor(0)
            canvas.drawLine(
                origin_x + ((i2.toFloat()) * line_size),
                0.0f,
                ((i2.toFloat()) * line_size) + origin_x + line_size,
                0.0f,
                this._paint
            )
            i = i2 + 1
        }
    }
}
