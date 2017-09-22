package me.gr.fishdrawable

import android.animation.Animator
import android.animation.AnimatorListenerAdapter
import android.animation.ObjectAnimator
import android.content.Context
import android.graphics.*
import android.util.AttributeSet
import android.view.Gravity
import android.view.MotionEvent
import android.view.animation.AccelerateDecelerateInterpolator
import android.widget.FrameLayout
import android.widget.ImageView
import java.util.*

/**
 * Created by GR on 2017/9/22.
 */
class FishView(context: Context, attrs: AttributeSet) : FrameLayout(context, attrs) {
    private val STROKE_WIDTH = dp2px(3f)
    private val DEFAULT_RADIUS = dp2px(42f)
    private val RIPPLE_DURATION = 800L
    private val FISH_DURATION = 1600L

    private val imageView = ImageView(context)
    private val fishDrawable = FishDrawable()
    private val paint = Paint(Paint.ANTI_ALIAS_FLAG or Paint.DITHER_FLAG)
    private val path = Path()
    private lateinit var fishAnimator: ObjectAnimator
    private val rippleAnimator = ObjectAnimator.ofFloat(this, "radius", 0f, 1f)

    private val screenWidth = resources.displayMetrics.widthPixels
    private val screenHeight = resources.displayMetrics.heightPixels
    private var alpha = 100
    private var radius = 0f
    private var touchX = 0f
    private var touchY = 0f

    init {
        setWillNotDraw(false)
        paint.style = Paint.Style.STROKE
        paint.strokeWidth = STROKE_WIDTH

        val params = LayoutParams(LayoutParams.WRAP_CONTENT, LayoutParams.WRAP_CONTENT)
        params.gravity = Gravity.CENTER
        imageView.layoutParams = params
        imageView.setImageDrawable(fishDrawable)
        addView(imageView)
    }

    override fun onMeasure(widthMeasureSpec: Int, heightMeasureSpec: Int) {
        super.onMeasure(widthMeasureSpec, heightMeasureSpec)
        setMeasuredDimension(screenWidth, screenHeight)
    }

    override fun onDraw(canvas: Canvas) {
        super.onDraw(canvas)
        paint.setARGB(alpha, 0, 125, 251)
        canvas.drawCircle(touchX, touchY, radius, paint)
    }

    override fun onTouchEvent(event: MotionEvent): Boolean {
        touchX = event.x
        touchY = event.y
        rippleAnimator.duration = RIPPLE_DURATION
        rippleAnimator.start()
        calculateTrail(PointF(touchX, touchY))
        return super.onTouchEvent(event)
    }

    private fun setRadius(value: Float) {
        alpha = (((1 - value) / 2) * 100).toInt()
        radius = DEFAULT_RADIUS * value
        invalidate()
    }

    private fun calculateTrail(touchPoint: PointF) {
        val deltaX = fishDrawable.middlePoint.x
        val deltaY = fishDrawable.middlePoint.y
        val fishMiddle = PointF(imageView.x + deltaX, imageView.y + deltaY)
        val fishHead = PointF(imageView.x + fishDrawable.headPoint.x, imageView.y + fishDrawable.headPoint.y)
        val angle = dihedralAngleForTug(fishMiddle, fishHead, touchPoint)
        val delta = dihedralAngleForX(fishMiddle, fishHead)
        val controlPoint = calculatePointF(fishMiddle, fishDrawable.HEAD_RADIUS * 1.6f, angle / 2 + delta)

        path.reset()
        path.moveTo(fishMiddle.x - deltaX, fishMiddle.y - deltaY)
        path.cubicTo(fishHead.x - deltaX, fishHead.y - deltaY, controlPoint.x - deltaX, controlPoint.y - deltaY, touchX - deltaX, touchY - deltaY)
        val tan = FloatArray(2)
        val pathMeasure = PathMeasure(path, false)

        fishAnimator = ObjectAnimator.ofFloat(imageView, "x", "y", path)
        fishAnimator.duration = FISH_DURATION
        fishAnimator.interpolator = AccelerateDecelerateInterpolator()
        fishAnimator.addListener(object : AnimatorListenerAdapter() {

            override fun onAnimationStart(animation: Animator) {
                super.onAnimationStart(animation)
                fishDrawable.waveFrequency = 2f
                val finsAnimator = fishDrawable.finsAnimator
                finsAnimator.repeatCount=Random().nextInt(3)
                finsAnimator.duration = ((Math.random() + 1) * 500).toLong()
                finsAnimator.start()
            }

            override fun onAnimationEnd(animation: Animator) {
                super.onAnimationEnd(animation)
                fishDrawable.waveFrequency = 1f
            }
        })
        fishAnimator.addUpdateListener {
            val fraction = it.animatedFraction
            pathMeasure.getPosTan(pathMeasure.length * fraction, null, tan)
            val angle = Math.toDegrees(Math.atan2(-tan[1].toDouble(), tan[0].toDouble()))
            fishDrawable.mainAngle = angle.toFloat()
        }
        fishAnimator.start()
    }

    private fun dihedralAngleForX(startPointF: PointF, endPointF: PointF): Float {
        return dihedralAngleForTug(startPointF, PointF(startPointF.x + 1, startPointF.y), endPointF)
    }

    private fun dihedralAngleForTug(centerPointF: PointF, headPointF: PointF, touchPointF: PointF): Float {
        val a = (headPointF.x - centerPointF.x) * (touchPointF.x - centerPointF.x) + (headPointF.y - centerPointF.y) * (touchPointF.y - centerPointF.y)
        val b = Math.hypot((headPointF.x - centerPointF.x).toDouble(), (headPointF.y - centerPointF.y).toDouble())
        val c = Math.hypot((touchPointF.x - centerPointF.x).toDouble(), (touchPointF.y - centerPointF.y).toDouble())
        val angle = Math.toDegrees(Math.acos(a / (b * c))).toFloat()
        val direction = (centerPointF.x - touchPointF.x) * (headPointF.y - touchPointF.y) - (centerPointF.y - touchPointF.y) * (headPointF.x - touchPointF.x)
        return if (direction == 0f) if (a >= 0f) 0f else 180f else if (direction > 0f) -angle else angle
    }
}