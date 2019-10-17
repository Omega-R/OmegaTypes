package com.omega_r.libs.omegatypes.tools

import android.graphics.Bitmap
import android.graphics.Matrix
import android.graphics.RectF
import android.widget.ImageView


/**
 * Created by Anton Knyazev on 2019-10-02.
 */

fun getScaledBitmap(
        dstWidth: Int,
        dstHeight: Int,
        imageScaleType: ImageView.ScaleType?,
        autoRecycle: Boolean,
        roughBitmap: Bitmap
): Bitmap {

    val scaleType = when (imageScaleType) {
        ImageView.ScaleType.MATRIX -> Matrix.ScaleToFit.FILL
        ImageView.ScaleType.FIT_XY -> Matrix.ScaleToFit.FILL
        ImageView.ScaleType.FIT_START -> Matrix.ScaleToFit.START
        ImageView.ScaleType.FIT_CENTER -> Matrix.ScaleToFit.CENTER
        ImageView.ScaleType.FIT_END -> Matrix.ScaleToFit.END
        ImageView.ScaleType.CENTER -> Matrix.ScaleToFit.CENTER
        ImageView.ScaleType.CENTER_CROP -> Matrix.ScaleToFit.CENTER
        ImageView.ScaleType.CENTER_INSIDE -> Matrix.ScaleToFit.CENTER
        else -> null
    }

    if (scaleType != null) {
        // calc exact destination size
        val m = Matrix()
        val inRect = RectF(0f, 0f, roughBitmap.width.toFloat(), roughBitmap.height.toFloat())
        val outRect = RectF(0f, 0f, dstWidth.toFloat(), dstHeight.toFloat())
        m.setRectToRect(inRect, outRect, scaleType)
        val values = FloatArray(9)
        m.getValues(values)
        val resizedBitmap = Bitmap.createScaledBitmap(
                roughBitmap,
                (roughBitmap.width * values[0]).toInt(),
                (roughBitmap.height * values[4]).toInt(),
                true
        )
        if (autoRecycle && resizedBitmap != roughBitmap) {
            roughBitmap.recycle()
        }
        return resizedBitmap

    } else {
        return roughBitmap
    }

}

