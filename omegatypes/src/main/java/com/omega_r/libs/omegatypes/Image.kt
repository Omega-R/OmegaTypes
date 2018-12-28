package com.omega_r.libs.omegatypes

import android.content.Context
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.graphics.Canvas
import android.graphics.Rect
import android.graphics.drawable.BitmapDrawable
import android.graphics.drawable.Drawable
import android.os.Build
import android.support.annotation.DrawableRes
import android.support.annotation.Px
import android.support.annotation.RequiresApi
import android.support.v4.content.ContextCompat
import android.support.v4.view.ViewCompat
import android.system.Os.read
import android.view.View
import android.widget.ImageView
import java.io.*

open class Image : Serializable {

    open fun applyImage(imageView: ImageView) {
        imageView.setImageDrawable(null)
    }

    open fun applyBackground(view: View) {
        ViewCompat.setBackground(view, null)
    }

    @Throws(IOException::class)
    open fun getStream(context: Context): InputStream {
        return object:InputStream() {
            override fun read() = -1
        }
    }

    companion object {

        @JvmStatic
        fun empty() = Image()

        @JvmStatic
        fun from(@DrawableRes stringRes: Int): Image = ResourceImage(stringRes)

        @JvmStatic
        fun from(drawable: Drawable): Image = DrawableImage(drawable)

        @JvmStatic
        fun from(bitmap: Bitmap): Image = BitmapImage(bitmap)
    }

    class ResourceImage(@DrawableRes private val resId: Int) : Image() {

        override fun applyImage(imageView: ImageView) {
            imageView.setImageResource(resId)
        }

        override fun applyBackground(view: View) {
            view.setBackgroundResource(resId)
        }

        override fun getStream(context: Context): InputStream {
            return BitmapFactory.decodeResource(context.resources, resId)
                    .toInputStream()
        }

    }

    class DrawableImage(private val drawable: Drawable) : Image() {

        override fun applyImage(imageView: ImageView) {
            imageView.setImageDrawable(drawable)
        }

        override fun applyBackground(view: View) {
            ViewCompat.setBackground(view, drawable)
        }

        override fun getStream(context: Context): InputStream {
            return drawable.toBitmap().toInputStream()
        }

    }

    class BitmapImage(private val bitmap: Bitmap) : Image() {

        override fun applyImage(imageView: ImageView) {
            imageView.setImageBitmap(bitmap)
        }

        override fun applyBackground(view: View) {
            ViewCompat.setBackground(view, BitmapDrawable(view.resources, bitmap))
        }

        override fun getStream(context: Context): InputStream {
            return bitmap.toInputStream()
        }
    }

}

fun Bitmap.toInputStream(): InputStream {
    val stream = ByteArrayOutputStream()
    compress(Bitmap.CompressFormat.JPEG, 100, stream)
    val byteArray = stream.toByteArray()
    return ByteArrayInputStream(byteArray)
}

fun Drawable.toBitmap(
        @Px width: Int = intrinsicWidth,
        @Px height: Int = intrinsicHeight,
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
    image?.applyBackground(this) ?: ViewCompat.setBackground(this, null)

}

fun Image.applyTo(imageView: ImageView) {
    imageView.setImage(this)
}
