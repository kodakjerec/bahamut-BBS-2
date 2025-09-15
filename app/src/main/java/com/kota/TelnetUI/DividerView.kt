package com.kota.TelnetUI

import android.content.Context
import android.graphics.Canvas
import android.graphics.Paint
import android.util.AttributeSet
import android.view.View
import kotlin.math.ceil
import kotlin.math.floor

class DividerView @JvmOverloads constructor(
    context: Context,
    attrs: AttributeSet? = null,
    defStyle: Int = 0
) : View(context, attrs, defStyle) {
    
    private val paint = Paint()
    
    override fun onDraw(canvas: Canvas) {
        super.onDraw(canvas)
        
        val lineSize = ceil(width / 80.0).toFloat()
        val originX = floor((width - (79 * lineSize)) / 2.0).toFloat()
        
        var i = 0
        while (i < 79) {
            paint.color = -12566464
            canvas.drawLine(
                originX + (i * lineSize),
                0f,
                originX + (i * lineSize) + lineSize,
                0f,
                paint
            )
            
            val i2 = i + 1
            paint.color = 0
            canvas.drawLine(
                originX + (i2 * lineSize),
                0f,
                originX + (i2 * lineSize) + lineSize,
                0f,
                paint
            )
            i = i2 + 1
        }
    }
}
