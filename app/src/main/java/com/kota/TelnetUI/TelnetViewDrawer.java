package com.kota.TelnetUI;

import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Paint;
import android.graphics.Path;
import android.util.TypedValue;

public class TelnetViewDrawer {
    public static final byte CLIP_LEFT = 1;
    public static final byte CLIP_NONE = 0;
    public static final byte CLIP_RIGHT = 2;
    public int backgroundColor = 0;
    public int bit = 1;
    public boolean blink = false;
    public double blockHeight = 0;
    public double blockWidth = 0;
    public Canvas canvas = null;
    public byte clip = 0;
    public double horizontalUnit = 0.0f;
    private int line_width = 0;
    public int loc = 1;
    public int originX = 0;
    public int originY = 0;
    public Paint paint = new Paint();
    public double radius = 0.0f;
    public int textBottomOffset = 0;
    public int textColor = 0;
    public double verticalUnit = 0.0f;

    private static class TelnetViewBlock {
        public float Bottom;
        public float Height;
        public float Left;
        public float Right;
        public float Top;
        public float Width;

        private TelnetViewBlock() {
            this.Width = 0;
            this.Height = 0;
            this.Top = 0;
            this.Bottom = 0;
            this.Left = 0;
            this.Right = 0;
        }
    }

    // 繪圖: 塗上文字
    public void drawCharAtPosition(Context aContext, int row, int column, char c) {
        TelnetViewBlock block = new TelnetViewBlock();
        block.Width = (float) (this.blockWidth * this.bit);
        block.Height = (float) this.blockHeight;
        block.Left = (float) (this.originX + (this.blockWidth * column));
        block.Right = block.Left + block.Width;
        block.Top = this.originY + (block.Height * row);
        block.Bottom = block.Top + block.Height;
        this.canvas.save();
        switch (this.clip) {
            case 1:
                this.canvas.clipRect(block.Left, block.Top, block.Left +  (float)this.blockWidth, block.Bottom);
                break;
            case 2:
                this.canvas.clipRect(block.Left +  (float)this.blockWidth, block.Top, block.Right, block.Bottom);
                break;
            default:
                this.canvas.clipRect(block.Left, block.Top, block.Right, block.Bottom);
                break;
        }
        drawBackground(block);
        if (!this.blink) {
            drawTextAtPosition(aContext, block, c);
        }
        this.canvas.restore();
    }

    private void drawBackground(TelnetViewBlock block) {
        this.paint.setColor(this.backgroundColor);
        this.canvas.drawRect( block.Left,  block.Top,  block.Right,  block.Bottom, this.paint);
    }

    private void drawTextAtPosition(Context aContext, TelnetViewBlock block, char c) {
        if (this.line_width == 0) {
            this.line_width = (int) Math.ceil((double) TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_DIP, 1.0f, aContext.getResources().getDisplayMetrics()));
        }
        this.paint.setColor(this.textColor);
        switch (c) {
            case 717:
                this.canvas.drawRect( block.Left,  (block.Bottom - ((int) (( block.Height) - (this.verticalUnit * 8.0f)))),  block.Right,  block.Bottom, this.paint);
                return;
            case 9472:
                int origin_y = (int) ((block.Top + (block.Height / 2)) - (this.line_width / 2));
                this.canvas.drawRect( block.Left,  origin_y,  block.Right,  (this.line_width + origin_y), this.paint);
                return;
            case 9585:
                this.canvas.drawLine( block.Left,  block.Bottom,  block.Right,  block.Top, this.paint);
                return;
            case 9586:
            case 65340:
                this.canvas.drawLine( block.Left,  block.Top,  block.Right,  block.Bottom, this.paint);
                return;
            case 9587:
                this.canvas.drawLine( block.Left,  block.Bottom,  block.Right,  block.Top, this.paint);
                this.canvas.drawLine( block.Left,  block.Top,  block.Right,  block.Bottom, this.paint);
                return;
            case 9601:
                this.canvas.drawRect( block.Left,  (block.Bottom - ((int) (( block.Height) - (this.verticalUnit * 7.0f)))),  block.Right,  block.Bottom, this.paint);
                return;
            case 9602:
                this.canvas.drawRect( block.Left,  (block.Bottom - ((int) (( block.Height) - (this.verticalUnit * 6.0f)))),  block.Right,  block.Bottom, this.paint);
                return;
            case 9603:
                this.canvas.drawRect( block.Left,  (block.Bottom - ((int) (( block.Height) - (this.verticalUnit * 5.0f)))),  block.Right,  block.Bottom, this.paint);
                return;
            case 9604:
                this.canvas.drawRect( block.Left,  (block.Bottom - ((int) (( block.Height) - (this.verticalUnit * 4.0f)))),  block.Right,  block.Bottom, this.paint);
                return;
            case 9605:
                this.canvas.drawRect( block.Left,  (block.Bottom - ((int) (( block.Height) - (this.verticalUnit * 3.0f)))),  block.Right,  block.Bottom, this.paint);
                return;
            case 9606:
                this.canvas.drawRect( block.Left,  (block.Bottom - ((int) (( block.Height) - (this.verticalUnit * 2.0f)))),  block.Right,  block.Bottom, this.paint);
                return;
            case 9607:
                this.canvas.drawRect( block.Left,  (block.Bottom - ((int) (( block.Height) - (this.verticalUnit * 1.0f)))),  block.Right,  block.Bottom, this.paint);
                return;
            case 9608:
                this.canvas.drawRect( block.Left,  block.Top,  block.Right,  block.Bottom, this.paint);
                return;
            case 9609:
                this.canvas.drawRect( block.Left,  block.Top,  (block.Left + ((int) (this.horizontalUnit * 7.0f))),  block.Bottom, this.paint);
                return;
            case 9610:
                this.canvas.drawRect( block.Left,  block.Top,  (block.Left + ((int) (this.horizontalUnit * 6.0f))),  block.Bottom, this.paint);
                return;
            case 9611:
                this.canvas.drawRect( block.Left,  block.Top,  (block.Left + ((int) (this.horizontalUnit * 5.0f))),  block.Bottom, this.paint);
                return;
            case 9612:
                this.canvas.drawRect( block.Left,  block.Top,  (block.Left + ((int) (this.horizontalUnit * 4.0f))),  block.Bottom, this.paint);
                return;
            case 9613:
                this.canvas.drawRect( block.Left,  block.Top,  (block.Left + ((int) (this.horizontalUnit * 3.0f))),  block.Bottom, this.paint);
                return;
            case 9614:
                this.canvas.drawRect( block.Left,  block.Top,  (block.Left + ((int) (this.horizontalUnit * 2.0f))),  block.Bottom, this.paint);
                return;
            case 9615:
                this.canvas.drawRect( block.Left,  block.Top,  (block.Left + ((int) (this.horizontalUnit * 1.0f))),  block.Bottom, this.paint);
                return;
            case 9621:
                this.canvas.drawRect( (block.Right - this.line_width),  block.Top,  block.Right,  block.Bottom, this.paint);
                return;
            case 9675:
                this.paint.setStyle(Paint.Style.STROKE);
                this.canvas.drawCircle(block.Left + (float)this.radius, block.Top + (float)this.radius, (float)this.radius, this.paint);
                this.paint.setStyle(Paint.Style.FILL);
                return;
            case 9679:
                this.canvas.drawCircle(block.Left + (float)this.radius, block.Top + (float)this.radius, (float)this.radius, this.paint);
                return;
            case 9698:
                Path path = new Path();
                path.moveTo( block.Left,  block.Bottom);
                path.lineTo( block.Right,  block.Top);
                path.lineTo( block.Right,  block.Bottom);
                path.lineTo( block.Left,  block.Bottom);
                this.canvas.drawPath(path, this.paint);
                return;
            case 9699:
                Path path2 = new Path();
                path2.moveTo( block.Left,  block.Bottom);
                path2.lineTo( block.Left,  block.Top);
                path2.lineTo( block.Right,  block.Bottom);
                path2.lineTo( block.Left,  block.Bottom);
                this.canvas.drawPath(path2, this.paint);
                return;
            case 9700:
                Path path3 = new Path();
                path3.moveTo( block.Left,  block.Bottom);
                path3.lineTo( block.Left,  block.Top);
                path3.lineTo( block.Right,  block.Top);
                path3.lineTo( block.Left,  block.Bottom);
                this.canvas.drawPath(path3, this.paint);
                return;
            case 9701:
                Path path4 = new Path();
                path4.moveTo( block.Left,  block.Top);
                path4.lineTo( block.Right,  block.Top);
                path4.lineTo( block.Right,  block.Bottom);
                path4.lineTo( block.Left,  block.Top);
                this.canvas.drawPath(path4, this.paint);
                return;
            case 65343:
                this.canvas.drawRect( block.Left,  (block.Bottom - this.line_width),  block.Right,  block.Bottom, this.paint);
                return;
            case 65507:
                this.canvas.drawRect( block.Left,  block.Top,  block.Right,  (block.Top + this.line_width), this.paint);
                return;
            default:
                this.canvas.drawText(new char[]{c}, 0, 1,  block.Left,  (block.Bottom - this.textBottomOffset), this.paint);
        }
    }
}
