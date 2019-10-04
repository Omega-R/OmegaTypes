package com.omega_r.libs.omegatypes.image

import android.content.Context
import android.graphics.Bitmap
import android.graphics.Bitmap.CompressFormat
import android.graphics.Canvas
import android.graphics.drawable.BitmapDrawable
import android.graphics.drawable.Drawable
import android.util.TypedValue
import android.view.View
import android.widget.ImageView
import com.omega_r.libs.omegatypes.R
import com.omega_r.libs.omegatypes.image.Image.Companion.NO_PLACEHOLDER_RES
import java.io.ByteArrayInputStream
import java.io.ByteArrayOutputStream
import java.io.InputStream

/**
 * Created by Anton Knyazev on 2019-10-03.
 */
abstract class ImageProcessor<I : Image> {

    fun I.applyImage(imageView: ImageView, placeholderResId: Int = NO_PLACEHOLDER_RES) {
        val newPlaceholderResId = imageView.getDefaultPlaceholderResId(placeholderResId)
        applyImageInner(imageView, newPlaceholderResId)
    }

    fun I.applyBackground(view: View, placeholderResId: Int = NO_PLACEHOLDER_RES) {
        val newPlaceholderResId = view.getDefaultPlaceholderResId(placeholderResId)
        applyBackgroundInner(view, newPlaceholderResId)
    }

    abstract fun I.getStream(context: Context, compressFormat: CompressFormat = CompressFormat.JPEG, quality: Int = 100): InputStream

    abstract fun I.preload(context: Context)

    protected abstract fun I.applyImageInner(imageView: ImageView, placeholderResId: Int = NO_PLACEHOLDER_RES)

    protected abstract fun I.applyBackgroundInner(view: View, placeholderResId: Int = NO_PLACEHOLDER_RES)

    protected open fun View.getDefaultPlaceholderResId(placeholderResId: Int): Int {
        return if (placeholderResId != NO_PLACEHOLDER_RES) {
            placeholderResId
        } else {
            TypedValue().run {
                if (context.theme.resolveAttribute(R.attr.omegaTypePlaceholderDefault, this, true)) data else {
                    NO_PLACEHOLDER_RES
                }
            }
        }
    }

    protected inline fun <R> Drawable.toBitmap(converter: Bitmap.() -> R): R {
        if (this is BitmapDrawable) {
            return converter(bitmap)
        }

        val newBitmap = if (intrinsicWidth <= 0 || intrinsicHeight <= 0) {
            Bitmap.createBitmap(500, 500, Bitmap.Config.ARGB_8888)!!
        } else {
            Bitmap.createBitmap(intrinsicWidth, intrinsicHeight, Bitmap.Config.ARGB_8888)!!
        }

        try {
            val oldBounds = copyBounds()
            setBounds(0, 0, newBitmap.width, newBitmap.height)

            draw(Canvas(newBitmap))

            bounds = oldBounds
            return converter(newBitmap)
        } finally {
            newBitmap.recycle()
        }
    }

}


fun Bitmap?.toInputStream(compressFormat: Bitmap.CompressFormat, quality: Int): InputStream {
    val stream = ByteArrayOutputStream()
    val byteArray = if (this != null) {
        compress(compressFormat, quality, stream)
        stream.toByteArray()
    } else {
        ByteArray(0)
    }
    return ByteArrayInputStream(byteArray)
}