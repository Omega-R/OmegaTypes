package com.omega_r.libs.omegatypes

import android.content.Context
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.graphics.Canvas
import android.graphics.drawable.BitmapDrawable
import android.graphics.drawable.Drawable
import android.os.Build
import android.util.Base64
import android.util.TypedValue
import android.view.View
import android.widget.ImageView
import android.widget.TextView
import com.omega_r.libs.omegatypes.Image.Companion.applyBackground
import java.io.*

open class Image : Serializable {

    @JvmOverloads
    open fun applyImage(imageView: ImageView, placeholderResId: Int = 0) {
        val newPlaceholderResId = getDefaultPlaceholderResId(imageView.context, placeholderResId)

        if (newPlaceholderResId == 0) {
            imageView.setImageDrawable(null)
        } else {
            imageView.setImageResource(newPlaceholderResId)
        }
    }

    @JvmOverloads
    open fun applyBackground(view: View, placeholderResId: Int = 0) {
        val newPlaceholderResId = getDefaultPlaceholderResId(view.context, placeholderResId)

        if (newPlaceholderResId != 0) {
            view.setBackgroundResource(newPlaceholderResId)
        } else {
            applyBackground(view, null)
        }
    }


    @JvmOverloads
    @Throws(IOException::class)
    open fun getStream(context: Context,
                       compressFormat: Bitmap.CompressFormat = Bitmap.CompressFormat.JPEG, quality: Int = 100): InputStream {
        return object : InputStream() {
            override fun read() = -1
        }
    }

    open fun getDrawable(context: Context): Drawable? = null

    open fun preload(context: Context) {
        // nothing
    }

    protected fun applyBackground(view: View, background: Drawable?) {
        Image.applyBackground(view, background)
    }

    protected fun getDefaultPlaceholderResId(context: Context, placeholderResId: Int): Int {
        return if (placeholderResId != 0) {
            placeholderResId
        } else {
            TypedValue().run {
                if (context.theme.resolveAttribute(R.attr.omegaTypePlaceholderDefault, this, true)) data else 0
            }
        }
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

        @JvmStatic
        fun from(imageBytes: ByteArray): Image {
            return from(BitmapFactory.decodeByteArray(imageBytes, 0, imageBytes.size) ?: return empty())
        }

        @JvmStatic
        fun from(base64String: String, flags: Int): Image {
            val position = base64String.indexOf(",")
            val data = if (position != -1) {
                base64String.substring(position + 1)
            } else {
                base64String
            }
            return from(Base64.decode(data, flags))
        }


        internal fun applyBackground(view: View, background: Drawable?) {
            if (android.os.Build.VERSION.SDK_INT < android.os.Build.VERSION_CODES.JELLY_BEAN) {
                @Suppress("DEPRECATION")
                view.setBackgroundDrawable(background)
            } else {
                view.background = background;
            }
        }
    }
}

class ResourceImage(private val resId: Int) : Image() {

    override fun applyImage(imageView: ImageView, placeholderResId: Int) {
        imageView.setImageResource(resId)
    }

    override fun applyBackground(view: View, placeholderResId: Int) {
        view.setBackgroundResource(resId)
    }

    override fun getStream(context: Context, compressFormat: Bitmap.CompressFormat, quality: Int): InputStream {
        return getDrawable(context).toBitmap {
            toInputStream(compressFormat, quality)
        }
    }

    override fun getDrawable(context: Context): Drawable {
        return if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
            context.getDrawable(resId)!!
        } else {
            context.resources.getDrawable(resId)!!
        }
    }
}

class DrawableImage(private val innerDrawable: Drawable) : Image() {

    override fun applyImage(imageView: ImageView, placeholderResId: Int) {
        imageView.setImageDrawable(innerDrawable)
    }

    override fun applyBackground(view: View, placeholderResId: Int) {
        applyBackground(view, innerDrawable)
    }

    override fun getStream(context: Context, compressFormat: Bitmap.CompressFormat, quality: Int): InputStream {
        return innerDrawable.toBitmap {
            toInputStream(compressFormat, quality)
        }
    }

    override fun getDrawable(context: Context) = innerDrawable

}

class BitmapImage(private val bitmap: Bitmap) : Image() {

    override fun applyImage(imageView: ImageView, placeholderResId: Int) {
        imageView.setImageBitmap(bitmap)
    }

    override fun applyBackground(view: View, placeholderResId: Int) {
        applyBackground(view, BitmapDrawable(view.resources, bitmap))
    }

    override fun getStream(context: Context, compressFormat: Bitmap.CompressFormat, quality: Int): InputStream {
        return bitmap.toInputStream(compressFormat, quality)
    }

    override fun getDrawable(context: Context) = BitmapDrawable(context.resources, bitmap)
}

fun Bitmap.toInputStream(compressFormat: Bitmap.CompressFormat, quality: Int): InputStream {
    val stream = ByteArrayOutputStream()
    compress(compressFormat, quality, stream)
    val byteArray = stream.toByteArray()
    return ByteArrayInputStream(byteArray)
}

private inline fun <R> Drawable.toBitmap(converter: Bitmap.() -> R): R {
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

@JvmOverloads
fun ImageView.setImage(image: Image?, placeholderResId: Int = 0) {
    if (image != null) {
        image.applyImage(this, placeholderResId)
    } else {
        if (placeholderResId == 0) {
            setImageDrawable(null)
        } else {
            setImageResource(placeholderResId)
        }
    }
}

@JvmOverloads
fun View.setBackground(image: Image?, placeholderResId: Int = 0) {
    if (image != null) {
        image.applyBackground(this, placeholderResId)
    } else {
        if (placeholderResId == 0) {
            applyBackground(this, null)
        } else {
            setBackgroundResource(placeholderResId)
        }
    }
}

private fun TextView.getImage(index: Int): Image? {
    val drawables = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.JELLY_BEAN_MR1) compoundDrawablesRelative else compoundDrawables
    return drawables[index]?.let { Image.from(it) }
}

private fun TextView.setImage(index: Int, image: Image?) {
    val drawables = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.JELLY_BEAN_MR1) compoundDrawablesRelative else compoundDrawables

    drawables[index] = image?.getDrawable(context)

    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.JELLY_BEAN_MR1) {
        setCompoundDrawablesRelativeWithIntrinsicBounds(drawables[0], drawables[1], drawables[2], drawables[3])
    } else {
        setCompoundDrawablesWithIntrinsicBounds(drawables[0], drawables[1], drawables[2], drawables[3])
    }
}

var TextView.imageStart: Image?
    get() = getImage(0)
    set(value) = setImage(0, value)

var TextView.imageEnd: Image?
    get() = getImage(2)
    set(value) = setImage(2, value)

var TextView.imageTop: Image?
    get() = getImage(1)
    set(value) = setImage(1, value)

var TextView.imageBottom: Image?
    get() = getImage(3)
    set(value) = setImage(3, value)