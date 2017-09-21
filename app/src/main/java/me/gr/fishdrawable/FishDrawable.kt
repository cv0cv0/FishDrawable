package me.gr.fishdrawable

import android.animation.ObjectAnimator
import android.animation.ValueAnimator
import android.content.res.Resources
import android.graphics.*
import android.graphics.drawable.Drawable
import android.os.Build
import android.util.TypedValue
import android.view.animation.LinearInterpolator
import java.util.*


/**
 * Created by GR on 2017/9/20.
 */
class FishDrawable : Drawable() {
    private val HEAD_RADIUS = dp2px(16f)
    private val BODY_LENGTH = HEAD_RADIUS * 3.2f
    private val FINS_LENGTH = HEAD_RADIUS * 1.3f
    private val TAIL1_LENGTH = HEAD_RADIUS * 1.12f
    private val TAIL2_LENGTH = HEAD_RADIUS * 1.3f
    private val TOTAL_LENGTH = HEAD_RADIUS * 6.79f
    private val BODY_COLOR = Color.argb(220, 244, 92, 71)
    private val FINS_COLOR = Color.argb(100, 244, 92, 71)
    private val OTHER_COLOR = Color.argb(160, 244, 92, 71)
    private val FINS_ANGLE = 115f
    private val SWAY_DURATION = 180_000L

    private val paint = Paint(Paint.ANTI_ALIAS_FLAG or Paint.DITHER_FLAG)
    private val path = Path()
    private val finsAnimator = ObjectAnimator.ofFloat(this, "finsAngle", 0f, 1f, 0f)
    private lateinit var headPoint: PointF
    private var middlePoint = PointF(HEAD_RADIUS * 4.18f, HEAD_RADIUS * 4.18f)
    private lateinit var endBodyPoint: PointF
    private lateinit var endTail1Point: PointF
    private lateinit var endTail2Point: PointF
    private var mainAngle = 90f
    private var bodyAngle = 0f
    private var finsAngle = 0f
    private var tail1Angle = 0f
    private var tail2Angle = 0f
    private var waveFrequency = 1f
    private var currentValue = 0

    init {
        finsAnimator.repeatMode = ValueAnimator.REVERSE
        finsAnimator.repeatCount = Random().nextInt(3)

        val animator = ValueAnimator.ofInt(0, 54000)
        animator.duration = SWAY_DURATION
        animator.interpolator = LinearInterpolator()
        animator.repeatCount = ValueAnimator.INFINITE
        animator.repeatMode = ValueAnimator.REVERSE
        animator.addUpdateListener {
            currentValue = it.animatedValue as Int
            invalidateSelf()
        }
        animator.start()
    }

    override fun draw(canvas: Canvas) {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
            canvas.saveLayerAlpha(0f, 0f, canvas.width.toFloat(), canvas.height.toFloat(), 240)
        } else {
            canvas.saveLayerAlpha(0f, 0f, canvas.width.toFloat(), canvas.height.toFloat(), 240, Canvas.ALL_SAVE_FLAG)
        }
        calculateValue()
        paint.color = OTHER_COLOR
        drawHead(canvas)
        paint.color = BODY_COLOR
        drawBody(canvas)
        paint.color = FINS_COLOR
        drawFins(canvas)
        paint.color = OTHER_COLOR
        drawTail1(canvas)
        drawTail2(canvas)
        drawTail3(canvas)
    }

    override fun setAlpha(alpha: Int) {
        paint.alpha = alpha
    }

    override fun getOpacity(): Int {
        return PixelFormat.TRANSLUCENT
    }

    override fun setColorFilter(colorFilter: ColorFilter) {
        paint.colorFilter = colorFilter
    }

    override fun getIntrinsicHeight(): Int {
        return (HEAD_RADIUS * 8.36f).toInt()
    }

    override fun getIntrinsicWidth(): Int {
        return (HEAD_RADIUS * 8.36f).toInt()
    }

    private fun setFinsAngle(currentValue: Float) {
        finsAngle = currentValue * 45
    }

    private fun drawHead(canvas: Canvas) {
        canvas.drawCircle(headPoint.x, headPoint.y, HEAD_RADIUS, paint)
    }

    private fun drawBody(canvas: Canvas) {
        val controlLeft = calculatePointF(headPoint, BODY_LENGTH * 0.56f, bodyAngle - 130)
        val controlRight = calculatePointF(headPoint, BODY_LENGTH * 0.56f, bodyAngle + 130)
        val point1 = calculatePointF(headPoint, HEAD_RADIUS, bodyAngle - 80)
        val point2 = calculatePointF(endBodyPoint, HEAD_RADIUS * 0.7f, bodyAngle - 90)
        val point3 = calculatePointF(endBodyPoint, HEAD_RADIUS * 0.7f, bodyAngle + 90)
        val point4 = calculatePointF(headPoint, HEAD_RADIUS, bodyAngle + 80)

        path.reset()
        path.moveTo(point1.x, point1.y)
        path.quadTo(controlLeft.x, controlLeft.y, point2.x, point2.y)
        path.lineTo(point3.x, point3.y)
        path.quadTo(controlRight.x, controlRight.y, point4.x, point4.y)
        canvas.drawPath(path, paint)
    }

    private fun drawFins(canvas: Canvas) {
        val leftStartPoint = calculatePointF(headPoint, HEAD_RADIUS * 0.9f, bodyAngle + 110)
        val leftEndPoint = calculatePointF(leftStartPoint, FINS_LENGTH, bodyAngle + finsAngle + 180)
        val leftControlPoint = calculatePointF(leftStartPoint, FINS_LENGTH * 1.8f, bodyAngle + FINS_ANGLE + finsAngle)
        path.reset()
        path.moveTo(leftStartPoint.x, leftStartPoint.y)
        path.quadTo(leftControlPoint.x, leftControlPoint.y, leftEndPoint.x, leftEndPoint.y)
        canvas.drawPath(path, paint)

        val rightStartPoint = calculatePointF(headPoint, HEAD_RADIUS * 0.9f, bodyAngle - 110)
        val rightEndPoint = calculatePointF(rightStartPoint, FINS_LENGTH, bodyAngle - finsAngle - 180)
        val rightControlPoint = calculatePointF(rightStartPoint, FINS_LENGTH * 1.8f, bodyAngle - FINS_ANGLE - finsAngle)
        path.reset()
        path.moveTo(rightStartPoint.x, rightStartPoint.y)
        path.quadTo(rightControlPoint.x, rightControlPoint.y, rightEndPoint.x, rightEndPoint.y)
        canvas.drawPath(path, paint)
    }

    private fun drawTail1(canvas: Canvas) {
        val point1 = calculatePointF(endBodyPoint, HEAD_RADIUS * 0.7f, tail1Angle - 90)
        val point2 = calculatePointF(endTail1Point, HEAD_RADIUS * 0.42f, tail1Angle - 90)
        val point3 = calculatePointF(endTail1Point, HEAD_RADIUS * 0.42f, tail1Angle + 90)
        val point4 = calculatePointF(endBodyPoint, HEAD_RADIUS * 0.7f, tail1Angle + 90)

        path.reset()
        path.moveTo(point1.x, point1.y)
        path.lineTo(point2.x, point2.y)
        path.lineTo(point3.x, point3.y)
        path.lineTo(point4.x, point4.y)
        canvas.drawPath(path, paint)
        canvas.drawCircle(endBodyPoint.x, endBodyPoint.y, HEAD_RADIUS * 0.7f, paint)
        canvas.drawCircle(endTail1Point.x, endTail1Point.y, HEAD_RADIUS * 0.42f, paint)
    }

    private fun drawTail2(canvas: Canvas) {
        val point1 = calculatePointF(endTail1Point, HEAD_RADIUS * 0.42f, tail2Angle - 90)
        val point2 = calculatePointF(endTail2Point, HEAD_RADIUS * 0.168f, tail2Angle - 90)
        val point3 = calculatePointF(endTail2Point, HEAD_RADIUS * 0.168f, tail2Angle + 90)
        val point4 = calculatePointF(endTail1Point, HEAD_RADIUS * 0.42f, tail2Angle + 90)

        path.reset()
        path.moveTo(point1.x, point1.y)
        path.lineTo(point2.x, point2.y)
        path.lineTo(point3.x, point3.y)
        path.lineTo(point4.x, point4.y)
        canvas.drawPath(path, paint)
    }

    private fun drawTail3(canvas: Canvas) {
        val width = Math.abs(Math.sin(Math.toRadians(currentValue * waveFrequency * 1.7)) * HEAD_RADIUS * 0.42f + HEAD_RADIUS / 5f * 3f).toFloat()
        val endPoint1 = calculatePointF(endTail1Point, TAIL2_LENGTH, tail2Angle - 180)
        val endPoint2 = calculatePointF(endTail1Point, TAIL2_LENGTH - dp2px(3f), tail2Angle - 180)
        val point1 = calculatePointF(endPoint1, width, tail2Angle - 90)
        val point2 = calculatePointF(endPoint1, width, tail2Angle + 90)
        val point3 = calculatePointF(endPoint2, width - dp2px(6f), tail2Angle - 90)
        val point4 = calculatePointF(endPoint2, width - dp2px(6f), tail2Angle + 90)

        path.reset()
        path.moveTo(endTail1Point.x, endTail1Point.y)
        path.lineTo(point3.x, point3.y)
        path.lineTo(point4.x, point4.y)
        canvas.drawPath(path, paint)

        path.reset()
        path.moveTo(endTail1Point.x, endTail1Point.y)
        path.lineTo(point1.x, point1.y)
        path.lineTo(point2.x, point2.y)
        canvas.drawPath(path, paint)
    }

    private fun calculateValue() {
        bodyAngle = mainAngle + (Math.sin(Math.toRadians(currentValue * waveFrequency * 1.2)) * 2).toFloat()
        headPoint = calculatePointF(middlePoint, BODY_LENGTH / 2, mainAngle)
        endBodyPoint = calculatePointF(headPoint, BODY_LENGTH, bodyAngle - 180)
        tail1Angle = bodyAngle + (Math.cos(Math.toRadians(currentValue * waveFrequency * 1.5))).toFloat() * 15f
        endTail1Point = calculatePointF(endBodyPoint, TAIL1_LENGTH, tail1Angle - 180f)
        tail2Angle = bodyAngle + (Math.sin(Math.toRadians(currentValue * waveFrequency * 1.5))).toFloat() * 35f
        endTail2Point = calculatePointF(endTail1Point, TAIL2_LENGTH, tail2Angle - 180f)
    }

    private fun calculatePointF(startPointF: PointF, length: Float, angle: Float): PointF {
        val deltaX = Math.cos(Math.toRadians(angle.toDouble())) * length
        val deltaY = Math.sin(Math.toRadians(angle.toDouble() - 180)) * length
        return PointF(startPointF.x + deltaX.toFloat(), startPointF.y + deltaY.toFloat())
    }

    private fun dp2px(dp: Float): Float = TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_DIP, dp, Resources.getSystem().displayMetrics)
}