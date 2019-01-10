package com.omega_r.libs.omegatypes

import android.content.Context
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.graphics.Canvas
import android.graphics.Rect
import android.graphics.drawable.BitmapDrawable
import android.graphics.drawable.Drawable
import android.view.View
import android.widget.ImageView
import com.omega_r.libs.omegatypes.Image.Companion.applyBackground
import java.io.*

open class Image : Serializable {

    open fun applyImage(imageView: ImageView) {
        imageView.setImageDrawable(null)
    }

    open fun applyBackground(view: View) {
        applyBackground(view, null)
    }


    @Throws(IOException::class)
    open fun getStream(context: Context,
                       compressFormat: Bitmap.CompressFormat = Bitmap.CompressFormat.JPEG, quality: Int = 100): InputStream {
        return object : InputStream() {
            override fun read() = -1
        }
    }

    protected fun applyBackground(view: View, background: Drawable?) {
        Image.applyBackground(view, background)
    }

    companion object {

        @JvmStatic
        fun empty() = Image()

        @JvmStatic
        fun from(stringRes: Int): Image = ResourceImage(stringRes)

        @JvmStatic
        fun from(drawable: Drawable): Image = DrawableImage(drawable)

        @JvmStatic
        fun from(bitmap: Bitmap): Image = BitmapImage(bitmap)

        internal fun applyBackground(view: View, background: Drawable?) {
            if (android.os.Build.VERSION.SDK_INT < android.os.Build.VERSION_CODES.JELLY_BEAN) {
                @Suppress("DEPRECATION")
                view.setBackgroundDrawable(background)
            } else {
                view.background = background;
            }
        }
    }

    class ResourceImage(private val resId: Int) : Image() {

        override fun applyImage(imageView: ImageView) {
            imageView.setImageResource(resId)
        }

        override fun applyBackground(view: View) {
            view.setBackgroundResource(resId)
        }

        override fun getStream(context: Context, compressFormat: Bitmap.CompressFormat, quality: Int): InputStream {
            return BitmapFactory.decodeResource(context.resources, resId)
                    .toInputStream(compressFormat, quality)
        }

    }

    class DrawableImage(private val drawable: Drawable) : Image() {

        override fun applyImage(imageView: ImageView) {
            imageView.setImageDrawable(drawable)
        }

        override fun applyBackground(view: View) {
            applyBackground(view, drawable)
        }

        override fun getStream(context: Context, compressFormat: Bitmap.CompressFormat, quality: Int): InputStream {
            return drawable.toBitmap().toInputStream(compressFormat, quality)
        }

    }

    class BitmapImage(private val bitmap: Bitmap) : Image() {

        override fun applyImage(imageView: ImageView) {
            imageView.setImageBitmap(bitmap)
        }

        override fun applyBackground(view: View) {
            applyBackground(view, BitmapDrawable(view.resources, bitmap))
        }

        override fun getStream(context: Context, compressFormat: Bitmap.CompressFormat, quality: Int): InputStream {
            return bitmap.toInputStream(compressFormat, quality)
        }
    }

}

fun Bitmap.toInputStream(compressFormat: Bitmap.CompressFormat, quality: Int): InputStream {
    val stream = ByteArrayOutputStream()
    compress(compressFormat, quality, stream)
    val byteArray = stream.toByteArray()
    return ByteArrayInputStream(byteArray)
}

fun Drawable.toBitmap(
        width: Int = intrinsicWidth,
        height: Int = intrinsicHeight,
        config: Bitmap.Config? = null
): Bitmap {
    if (this is BitmapDrawable) {
        if (config == null || bitmap.config == config) {
            // Fast-path to return original. Bitmap.createScaledBitmap will do this check, but it
            // involves allocation and two jumps into native code so we perform the check ourselves.
            if (width == intrinsicWidth && height == intrinsicHeight) {
                return bitmap
            }
            return Bitmap.createScaledBitmap(bitmap, width, height, true)
        }
    }

    val newRect = Rect(bounds)

    val bitmap = Bitmap.createBitmap(width, height, config ?: Bitmap.Config.ARGB_8888)
    setBounds(0, 0, width, height)
    draw(Canvas(bitmap))

    setBounds(newRect.left, newRect.top, newRect.right, newRect.bottom)
    return bitmap
}

fun ImageView.setImage(image: Image?) {
    image?.applyImage(this) ?: setImageDrawable(null)
}

fun View.setBackground(image: Image?) {
    image?.applyBackground(this) ?: applyBackground(this, null)

}

fun Image.applyTo(imageView: ImageView) {
    imageView.setImage(this)
}
