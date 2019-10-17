package com.omega_r.libs.omegatypes.image

import android.content.Context
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.graphics.drawable.BitmapDrawable
import android.graphics.drawable.Drawable
import android.view.View
import android.widget.ImageView
import java.io.*

/**
 * Created by Anton Knyazev on 2019-10-03.
 */
class DrawableImage private constructor() : Image() {

    companion object {

        init {
            ImageProcessors.default.addImageProcessor(DrawableImage::class, Processor())
        }

    }

    lateinit var drawable: Drawable
        private set

    constructor(drawable: Drawable) : this() {
        this.drawable = drawable
    }

    override fun getDrawable(context: Context): Drawable {
        return drawable
    }

    @Throws(IOException::class)
    private fun writeObject(out: ObjectOutputStream) {

        val bytes = drawable.toBitmapAndRecycle {
            val stream = ByteArrayOutputStream()
            if (hasAlpha()) {
                compress(Bitmap.CompressFormat.PNG, 100, stream)
            } else {
                compress(Bitmap.CompressFormat.JPEG, 100, stream)
            }
            stream.toByteArray()
        }

        out.writeInt(bytes.size)
        out.write(bytes)

    }

    @Throws(IOException::class, ClassNotFoundException::class)
    private fun readObject(inputStream: ObjectInputStream) {
        val bufferLength = inputStream.readInt()
        val stream = ByteArrayOutputStream(bufferLength)

        inputStream.copyTo(stream, bufferLength)

        val bitmap = BitmapFactory.decodeByteArray(stream.toByteArray(), 0, bufferLength)

        // there is no other way
        drawable = BitmapDrawable(bitmap)
    }

    override fun equals(other: Any?): Boolean {
        if (this === other) return true
        if (javaClass != other?.javaClass) return false
        if (!super.equals(other)) return false

        other as DrawableImage

        if (drawable != other.drawable) return false

        return true
    }

    override fun hashCode(): Int {
        var result = super.hashCode()
        result = 31 * result + drawable.hashCode()
        return result
    }


    class Processor : ImageProcessor<DrawableImage>() {

        override fun DrawableImage.applyImageInner(imageView: ImageView, placeholderResId: Int) {
            imageView.setImageDrawable(drawable)
        }

        override fun DrawableImage.applyBackgroundInner(view: View, placeholderResId: Int) {
            Image.Processor.applyBackground(view, drawable)
        }

        override suspend fun DrawableImage.getStream(context: Context, compressFormat: Bitmap.CompressFormat, quality: Int): InputStream {
            return getDrawable(context).toBitmapAndRecycle {
                toInputStream(compressFormat, quality)
            }
        }

        override fun DrawableImage.preload(context: Context) {
            // nothing
        }

        override fun View.getDefaultPlaceholderResId(placeholderResId: Int): Int = NO_PLACEHOLDER_RES

    }

}

fun Image.Companion.from(drawable: Drawable) = DrawableImage(drawable)
