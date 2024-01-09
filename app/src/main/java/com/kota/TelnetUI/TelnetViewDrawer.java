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
    public int blockHeight = 0;
    public int blockWidth = 0;
    public Canvas canvas = null;
    public byte clip = 0;
    public float horizontalUnit = 0.0f;
    private int line_width = 0;
    public int loc = 1;
    public int originX = 0;
    public int originY = 0;
    public Paint paint = new Paint();
    public float radius = 0.0f;
    public int textBottomOffset = 0;
    public int textColor = 0;
    public float verticalUnit = 0.0f;

    private class TelnetViewBlock {
        public int Bottom;
        public int Height;
        public int Left;
        public int Right;
        public int Top;
        public int Width;

        private TelnetViewBlock() {
            this.Width = 0;
            this.Height = 0;
            this.Top = 0;
            this.Bottom = 0;
            this.Left = 0;
            this.Right = 0;
        }
    }

    public void drawCharAtPosition(Context aContext, int row, int column, char c) {
        TelnetViewBlock block = new TelnetViewBlock();
        block.Width = this.blockWidth * this.bit;
        block.Height = this.blockHeight;
        block.Left = this.originX + (this.blockWidth * column);
        block.Right = block.Left + block.Width;
        block.Top = this.originY + (block.Height * row);
        block.Bottom = block.Top + block.Height;
        this.canvas.save();
        switch (this.clip) {
            case 1:
                this.canvas.clipRect(block.Left, block.Top, block.Left + this.blockWidth, block.Bottom);
                break;
            case 2:
                this.canvas.clipRect(block.Left + this.blockWidth, block.Top, block.Right, block.Bottom);
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
        this.canvas.drawRect((float) block.Left, (float) block.Top, (float) block.Right, (float) block.Bottom, this.paint);
    }

    private void drawTextAtPosition(Context aContext, TelnetViewBlock block, char c) {
        if (this.line_width == 0) {
            this.line_width = (int) Math.ceil((double) TypedValue.applyDimension(1, 1.0f, aContext.getResources().getDisplayMetrics()));
        }
        this.paint.setColor(this.textColor);
        switch (c) {
            case 717:
                this.canvas.drawRect((float) block.Left, (float) (block.Bottom - ((int) (((float) block.Height) - (this.verticalUnit * 8.0f)))), (float) block.Right, (float) block.Bottom, this.paint);
                return;
            case 9472:
                int origin_y = (block.Top + (block.Height / 2)) - (this.line_width / 2);
                this.canvas.drawRect((float) block.Left, (float) origin_y, (float) block.Right, (float) (this.line_width + origin_y), this.paint);
                return;
            case 9585:
                this.canvas.drawLine((float) block.Left, (float) block.Bottom, (float) block.Right, (float) block.Top, this.paint);
                return;
            case 9586:
                this.canvas.drawLine((float) block.Left, (float) block.Top, (float) block.Right, (float) block.Bottom, this.paint);
                return;
            case 9587:
                this.canvas.drawLine((float) block.Left, (float) block.Bottom, (float) block.Right, (float) block.Top, this.paint);
                this.canvas.drawLine((float) block.Left, (float) block.Top, (float) block.Right, (float) block.Bottom, this.paint);
                return;
            case 9601:
                this.canvas.drawRect((float) block.Left, (float) (block.Bottom - ((int) (((float) block.Height) - (this.verticalUnit * 7.0f)))), (float) block.Right, (float) block.Bottom, this.paint);
                return;
            case 9602:
                this.canvas.drawRect((float) block.Left, (float) (block.Bottom - ((int) (((float) block.Height) - (this.verticalUnit * 6.0f)))), (float) block.Right, (float) block.Bottom, this.paint);
                return;
            case 9603:
                this.canvas.drawRect((float) block.Left, (float) (block.Bottom - ((int) (((float) block.Height) - (this.verticalUnit * 5.0f)))), (float) block.Right, (float) block.Bottom, this.paint);
                return;
            case 9604:
                this.canvas.drawRect((float) block.Left, (float) (block.Bottom - ((int) (((float) block.Height) - (this.verticalUnit * 4.0f)))), (float) block.Right, (float) block.Bottom, this.paint);
                return;
            case 9605:
                this.canvas.drawRect((float) block.Left, (float) (block.Bottom - ((int) (((float) block.Height) - (this.verticalUnit * 3.0f)))), (float) block.Right, (float) block.Bottom, this.paint);
                return;
            case 9606:
                this.canvas.drawRect((float) block.Left, (float) (block.Bottom - ((int) (((float) block.Height) - (this.verticalUnit * 2.0f)))), (float) block.Right, (float) block.Bottom, this.paint);
                return;
            case 9607:
                this.canvas.drawRect((float) block.Left, (float) (block.Bottom - ((int) (((float) block.Height) - (this.verticalUnit)))), (float) block.Right, (float) block.Bottom, this.paint);
                return;
            case 9608:
                this.canvas.drawRect((float) block.Left, (float) block.Top, (float) block.Right, (float) block.Bottom, this.paint);
                return;
            case 9609:
                this.canvas.drawRect((float) block.Left, (float) block.Top, (float) (block.Left + ((int) (this.horizontalUnit * 7.0f))), (float) block.Bottom, this.paint);
                return;
            case 9610:
                this.canvas.drawRect((float) block.Left, (float) block.Top, (float) (block.Left + ((int) (this.horizontalUnit * 6.0f))), (float) block.Bottom, this.paint);
                return;
            case 9611:
                this.canvas.drawRect((float) block.Left, (float) block.Top, (float) (block.Left + ((int) (this.horizontalUnit * 5.0f))), (float) block.Bottom, this.paint);
                return;
            case 9612:
                this.canvas.drawRect((float) block.Left, (float) block.Top, (float) (block.Left + ((int) (this.horizontalUnit * 4.0f))), (float) block.Bottom, this.paint);
                return;
            case 9613:
                this.canvas.drawRect((float) block.Left, (float) block.Top, (float) (block.Left + ((int) (this.horizontalUnit * 3.0f))), (float) block.Bottom, this.paint);
                return;
            case 9614:
                this.canvas.drawRect((float) block.Left, (float) block.Top, (float) (block.Left + ((int) (this.horizontalUnit * 2.0f))), (float) block.Bottom, this.paint);
                return;
            case 9615:
                this.canvas.drawRect((float) block.Left, (float) block.Top, (float) (block.Left + ((int) (this.horizontalUnit))), (float) block.Bottom, this.paint);
                return;
            case 9621:
                this.canvas.drawRect((float) (block.Right - this.line_width), (float) block.Top, (float) block.Right, (float) block.Bottom, this.paint);
                return;
            case 9675:
                this.paint.setStyle(Paint.Style.STROKE);
                this.canvas.drawCircle(((float) block.Left) + this.radius, ((float) block.Top) + this.radius, this.radius, this.paint);
                this.paint.setStyle(Paint.Style.FILL);
                return;
            case 9679:
                this.canvas.drawCircle(((float) block.Left) + this.radius, ((float) block.Top) + this.radius, this.radius, this.paint);
                return;
            case 9698:
                Path path = new Path();
                path.moveTo((float) block.Left, (float) block.Bottom);
                path.lineTo((float) block.Right, (float) block.Top);
                path.lineTo((float) block.Right, (float) block.Bottom);
                path.lineTo((float) block.Left, (float) block.Bottom);
                this.canvas.drawPath(path, this.paint);
                return;
            case 9699:
                Path path2 = new Path();
                path2.moveTo((float) block.Left, (float) block.Bottom);
                path2.lineTo((float) block.Left, (float) block.Top);
                path2.lineTo((float) block.Right, (float) block.Bottom);
                path2.lineTo((float) block.Left, (float) block.Bottom);
                this.canvas.drawPath(path2, this.paint);
                return;
            case 9700:
                Path path3 = new Path();
                path3.moveTo((float) block.Left, (float) block.Bottom);
                path3.lineTo((float) block.Left, (float) block.Top);
                path3.lineTo((float) block.Right, (float) block.Top);
                path3.lineTo((float) block.Left, (float) block.Bottom);
                this.canvas.drawPath(path3, this.paint);
                return;
            case 9701:
                Path path4 = new Path();
                path4.moveTo((float) block.Left, (float) block.Top);
                path4.lineTo((float) block.Right, (float) block.Top);
                path4.lineTo((float) block.Right, (float) block.Bottom);
                path4.lineTo((float) block.Left, (float) block.Top);
                this.canvas.drawPath(path4, this.paint);
                return;
            case 65340:
                this.canvas.drawLine((float) block.Left, (float) block.Top, (float) block.Right, (float) block.Bottom, this.paint);
                return;
            case 65343:
                this.canvas.drawRect((float) block.Left, (float) (block.Bottom - this.line_width), (float) block.Right, (float) block.Bottom, this.paint);
                return;
            case 65507:
                this.canvas.drawRect((float) block.Left, (float) block.Top, (float) block.Right, (float) (block.Top + this.line_width), this.paint);
                return;
            default:
                this.canvas.drawText(new char[]{c}, 0, 1, (float) block.Left, (float) (block.Bottom - this.textBottomOffset), this.paint);
        }
    }
}
