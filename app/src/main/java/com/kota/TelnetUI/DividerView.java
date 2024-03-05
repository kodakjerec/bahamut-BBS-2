package com.kota.TelnetUI;

import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Paint;
import android.util.AttributeSet;
import android.view.View;

public class DividerView extends View {
    Paint _paint = new Paint();

    public DividerView(Context context) {
        super(context);
    }

    public DividerView(Context context, AttributeSet attrs, int defStyle) {
        super(context, attrs, defStyle);
    }

    public DividerView(Context context, AttributeSet attrs) {
        super(context, attrs);
    }

    protected void onDraw(Canvas canvas) {
        super.onDraw(canvas);
        float line_size = (float) Math.ceil((double) (((float) getWidth()) / ((float) 80)));
        float origin_x = (float) Math.floor((double) ((((float) getWidth()) - (((float) 79) * line_size)) / 2.0f));
        int i = 0;
        while (i < 79) {
            this._paint.setColor(-12566464);
            Canvas canvas2 = canvas;
            canvas2.drawLine(origin_x + (((float) i) * line_size), 0.0f, (((float) i) * line_size) + origin_x + line_size, 0.0f, this._paint);
            int i2 = i + 1;
            this._paint.setColor(0);
            canvas.drawLine(origin_x + (((float) i2) * line_size), 0.0f, (((float) i2) * line_size) + origin_x + line_size, 0.0f, this._paint);
            i = i2 + 1;
        }
    }
}
