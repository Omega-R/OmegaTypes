package com.omega_r.libs.omegatypes.image

import android.content.Context
import android.graphics.Bitmap
import android.graphics.drawable.Drawable
import android.view.View
import android.widget.ImageView
import java.io.InputStream

/**
 * Created by Anton Knyazev on 2019-10-03.
 */
data class DrawableImage (val drawable: Drawable) : Image() {

    companion object {

        init {
            ImagesProcessor.default.addImageProcessor(DrawableImage::class, Processor())
        }

    }

    override fun getDrawable(context: Context): Drawable {
        return drawable
    }

    class Processor : ImageProcessor<DrawableImage>() {

        override fun DrawableImage.applyImageInner(imageView: ImageView, placeholderResId: Int) {
            imageView.setImageDrawable(drawable)
        }

        override fun DrawableImage.applyBackgroundInner(view: View, placeholderResId: Int) {
            Image.Processor.applyBackground(view, drawable)
        }

        override fun DrawableImage.getStream(context: Context, compressFormat: Bitmap.CompressFormat, quality: Int): InputStream {
            return getDrawable(context).toBitmap {
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
