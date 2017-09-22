package me.gr.fishdrawable

import android.content.res.Resources
import android.graphics.PointF
import android.util.TypedValue

/**
 * Created by GR on 2017/9/22.
 */
fun dp2px(dp: Float): Float = TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_DIP, dp, Resources.getSystem().displayMetrics)


fun calculatePointF(startPointF: PointF, length: Float, angle: Float): PointF {
    val deltaX = Math.cos(Math.toRadians(angle.toDouble())) * length
    val deltaY = Math.sin(Math.toRadians(angle.toDouble() - 180)) * length
    return PointF(startPointF.x + deltaX.toFloat(), startPointF.y + deltaY.toFloat())
}