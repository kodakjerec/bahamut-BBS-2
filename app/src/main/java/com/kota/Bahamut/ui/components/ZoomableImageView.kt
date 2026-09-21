package com.kota.Bahamut.ui.components

import android.animation.ValueAnimator
import android.annotation.SuppressLint
import android.content.Context
import android.graphics.Matrix
import android.graphics.RectF
import android.graphics.drawable.Drawable
import android.util.AttributeSet
import android.view.GestureDetector
import android.view.MotionEvent
import android.view.ScaleGestureDetector
import android.view.animation.AccelerateDecelerateInterpolator
import androidx.appcompat.widget.AppCompatImageView
import kotlin.math.min

/**
 * 支援雙指縮放、三階雙擊放大 (1.0x -> 2.0x -> 3.5x) 與平移拖曳的自訂 ImageView。
 * 當圖片處於原始大小 (1.0x) 時，不鎖定父層 touch 事件，確保不影響 ArticlePage 等清單垂直滑動。
 */
class ZoomableImageView @JvmOverloads constructor(
    context: Context,
    attrs: AttributeSet? = null,
    defStyleAttr: Int = 0
) : AppCompatImageView(context, attrs, defStyleAttr) {

    private val mainMatrix = Matrix()

    private var currentScale = 1.0f
    private val minScale = 1.0f
    private val maxScale = 5.0f

    // 雙擊放大的三個階梯倍率 (1.0x -> 2.0x -> 3.5x -> 1.0x)
    private val scaleLevels = floatArrayOf(1.0f, 2.0f, 3.5f)

    private var viewWidth = 0f
    private var viewHeight = 0f
    private var drawableWidth = 0f
    private var drawableHeight = 0f

    private var lastTouchX = 0f
    private var lastTouchY = 0f
    private var isDragging = false

    private var animator: ValueAnimator? = null

    private val scaleDetector = ScaleGestureDetector(context, object : ScaleGestureDetector.SimpleOnScaleGestureListener() {
        override fun onScale(detector: ScaleGestureDetector): Boolean {
            val scaleFactor = detector.scaleFactor
            val targetScale = currentScale * scaleFactor

            val newScale = targetScale.coerceIn(minScale, maxScale)
            val factor = newScale / currentScale

            mainMatrix.postScale(factor, factor, detector.focusX, detector.focusY)
            currentScale = newScale

            checkAndApplyMatrix()

            if (currentScale > 1.05f) {
                parent?.requestDisallowInterceptTouchEvent(true)
            }
            return true
        }
    })

    private val gestureDetector = GestureDetector(context, object : GestureDetector.SimpleOnGestureListener() {
        override fun onSingleTapConfirmed(e: MotionEvent): Boolean {
            performClick()
            return true
        }

        override fun onLongPress(e: MotionEvent) {
            performLongClick()
        }

        override fun onDoubleTap(e: MotionEvent): Boolean {
            val focusX = e.x
            val focusY = e.y

            // 計算下一個放大倍率
            val targetScale = when {
                currentScale < 1.8f -> scaleLevels[1] // 2.0x
                currentScale < 3.0f -> scaleLevels[2] // 3.5x
                else -> scaleLevels[0]               // 1.0x
            }

            animateZoom(targetScale, focusX, focusY)
            return true
        }
    })

    init {
        scaleType = ScaleType.MATRIX
    }

    override fun setImageDrawable(drawable: Drawable?) {
        val isNewDrawable = (this.drawable != drawable)
        super.setImageDrawable(drawable)
        if (isNewDrawable) {
            resetMatrix()
        }
    }

    override fun onMeasure(widthMeasureSpec: Int, heightMeasureSpec: Int) {
        val d = drawable
        if (d != null && d.intrinsicWidth > 0 && d.intrinsicHeight > 0) {
            val widthSize = MeasureSpec.getSize(widthMeasureSpec)
            val widthMode = MeasureSpec.getMode(widthMeasureSpec)
            val heightMode = MeasureSpec.getMode(heightMeasureSpec)

            // 當高度為 wrapContent (即高度非 EXACTLY) 且寬度已知時，自動依據圖片比例計算 View 高度，避免 View 多餘空白頂低圖片
            if (widthMode != MeasureSpec.UNSPECIFIED && heightMode != MeasureSpec.EXACTLY) {
                val aspect = d.intrinsicHeight.toFloat() / d.intrinsicWidth.toFloat()
                val targetHeight = (widthSize * aspect).toInt()
                setMeasuredDimension(widthSize, targetHeight)
                return
            }
        }
        super.onMeasure(widthMeasureSpec, heightMeasureSpec)
    }

    override fun onLayout(changed: Boolean, left: Int, top: Int, right: Int, bottom: Int) {
        super.onLayout(changed, left, top, right, bottom)
        if (changed) {
            resetMatrix()
        }
    }

    /** 重置 Matrix，將圖片居中並依照 FIT_CENTER 貼合 */
    fun resetMatrix() {
        val d = drawable ?: return
        viewWidth = width.toFloat()
        viewHeight = height.toFloat()
        drawableWidth = d.intrinsicWidth.toFloat()
        drawableHeight = d.intrinsicHeight.toFloat()

        if (viewWidth <= 0f || viewHeight <= 0f || drawableWidth <= 0f || drawableHeight <= 0f) {
            return
        }

        mainMatrix.reset()

        val scale = min(viewWidth / drawableWidth, viewHeight / drawableHeight)
        val dx = (viewWidth - drawableWidth * scale) / 2f
        val dy = (viewHeight - drawableHeight * scale) / 2f

        mainMatrix.postScale(scale, scale)
        mainMatrix.postTranslate(dx, dy)

        currentScale = 1.0f
        imageMatrix = mainMatrix
    }

    @SuppressLint("ClickableViewAccessibility")
    override fun onTouchEvent(event: MotionEvent): Boolean {
        scaleDetector.onTouchEvent(event)
        gestureDetector.onTouchEvent(event)

        when (event.actionMasked) {
            MotionEvent.ACTION_DOWN -> {
                lastTouchX = event.x
                lastTouchY = event.y
                isDragging = false
                if (currentScale > 1.05f) {
                    parent?.requestDisallowInterceptTouchEvent(true)
                }
            }
            MotionEvent.ACTION_MOVE -> {
                val dx = event.x - lastTouchX
                val dy = event.y - lastTouchY

                if (currentScale > 1.05f && event.pointerCount == 1) {
                    if (!isDragging && (kotlin.math.abs(dx) > 5f || kotlin.math.abs(dy) > 5f)) {
                        isDragging = true
                    }

                    if (isDragging) {
                        mainMatrix.postTranslate(dx, dy)
                        checkAndApplyMatrix()
                        parent?.requestDisallowInterceptTouchEvent(true)
                    }
                }
                lastTouchX = event.x
                lastTouchY = event.y
            }
            MotionEvent.ACTION_UP, MotionEvent.ACTION_CANCEL -> {
                isDragging = false
                if (currentScale <= 1.05f) {
                    parent?.requestDisallowInterceptTouchEvent(false)
                }
            }
        }
        return true
    }

    /** 檢查並平移修正邊界，防止拖曳過頭露出空白 */
    private fun checkAndApplyMatrix() {
        val d = drawable ?: return
        val rect = RectF(0f, 0f, d.intrinsicWidth.toFloat(), d.intrinsicHeight.toFloat())
        mainMatrix.mapRect(rect)

        val rectWidth = rect.width()
        val rectHeight = rect.height()

        var deltaX = 0f
        var deltaY = 0f

        if (rectWidth >= viewWidth) {
            if (rect.left > 0f) deltaX = -rect.left
            if (rect.right < viewWidth) deltaX = viewWidth - rect.right
        } else {
            deltaX = (viewWidth - rectWidth) / 2f - rect.left
        }

        if (rectHeight >= viewHeight) {
            if (rect.top > 0f) deltaY = -rect.top
            if (rect.bottom < viewHeight) deltaY = viewHeight - rect.bottom
        } else {
            deltaY = (viewHeight - rectHeight) / 2f - rect.top
        }

        mainMatrix.postTranslate(deltaX, deltaY)
        imageMatrix = mainMatrix
    }

    /** 動畫平滑放大/縮小 */
    private fun animateZoom(targetScale: Float, focusX: Float, focusY: Float) {
        animator?.cancel()

        val startScale = currentScale
        var previousVal = startScale

        animator = ValueAnimator.ofFloat(startScale, targetScale).apply {
            duration = 220L
            interpolator = AccelerateDecelerateInterpolator()
            addUpdateListener { anim ->
                val valScale = anim.animatedValue as Float
                val factor = valScale / previousVal
                previousVal = valScale

                mainMatrix.postScale(factor, factor, focusX, focusY)
                currentScale = valScale
                checkAndApplyMatrix()
            }
            start()
        }
    }
}
