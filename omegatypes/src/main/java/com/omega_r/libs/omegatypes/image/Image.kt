package com.omega_r.libs.omegatypes.image

import android.content.Context
import android.graphics.Bitmap
import android.graphics.Canvas
import android.graphics.drawable.BitmapDrawable
import android.graphics.drawable.Drawable
import android.os.Build
import android.view.View
import android.widget.ImageView
import android.widget.TextView
import java.io.InputStream
import java.io.Serializable

/**
 * Created by Anton Knyazev on 2019-10-03.
 */
open class Image : Serializable {

    companion object {

        const val NO_PLACEHOLDER_RES = 0

        init {
            ImagesProcessor.default.addImageProcessor(Image::class, Processor())
        }


        fun from() = Image()

    }

    open fun getDrawable(context: Context): Drawable? = null


    override fun equals(other: Any?): Boolean {
        if (this === other) return true
        if (javaClass != other?.javaClass) return false
        return true
    }

    override fun hashCode(): Int {
        return javaClass.hashCode()
    }

    class Processor : ImageProcessor<Image>() {

        companion object {

            fun applyBackground(view: View, background: Drawable?) {
                if (Build.VERSION.SDK_INT < Build.VERSION_CODES.JELLY_BEAN) {
                    @Suppress("DEPRECATION")
                    view.setBackgroundDrawable(background)
                } else {
                    view.background = background;
                }
            }

            fun applyEmptyBackground(view: View, placeholderResId: Int) {
                if (placeholderResId != NO_PLACEHOLDER_RES) {
                    view.setBackgroundResource(placeholderResId)
                } else {
                    applyBackground(view, null)
                }
            }

            inline fun <R> toBitmap(drawable: Drawable, converter: Bitmap.() -> R): R = with(drawable) {
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

        override fun Image.applyImageInner(imageView: ImageView, placeholderResId: Int) {
            if (placeholderResId == NO_PLACEHOLDER_RES) {
                imageView.setImageDrawable(null)
            } else {
                imageView.setImageResource(placeholderResId)
            }
        }

        override fun Image.applyBackgroundInner(view: View, placeholderResId: Int) {
            if (placeholderResId != NO_PLACEHOLDER_RES) {
                view.setBackgroundResource(placeholderResId)
            } else {
                applyBackground(view, null)
            }
        }

        override fun Image.getStream(context: Context, compressFormat: Bitmap.CompressFormat, quality: Int): InputStream {
            return object : InputStream() {
                override fun read() = -1
            }
        }

        override fun Image.preload(context: Context) {
            // nothing
        }

        override fun View.getDefaultPlaceholderResId(placeholderResId: Int): Int = NO_PLACEHOLDER_RES


    }

}

@JvmOverloads
fun ImageView.setImage(image: Image?, placeholderResId: Int = 0, processor: ImagesProcessor = ImagesProcessor.current) {
    with(processor) {
        if (image != null) {
            image.applyImage(this@setImage, placeholderResId)
        } else {
            if (placeholderResId == 0) {
                setImageDrawable(null)
            } else {
                setImageResource(placeholderResId)
            }
        }
    }
}

@JvmOverloads
fun View.setBackground(image: Image?, placeholderResId: Int = 0, processor: ImagesProcessor = ImagesProcessor.current) {
    with(processor) {
        if (image != null) {
            image.applyBackground(this@setBackground, placeholderResId)
        } else {
            if (placeholderResId == 0) {
                Image.Processor.applyBackground(this@setBackground, null)
            } else {
                setBackgroundResource(placeholderResId)
            }
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
