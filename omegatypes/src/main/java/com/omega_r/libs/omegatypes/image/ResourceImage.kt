package com.omega_r.libs.omegatypes.image

import android.content.Context
import android.graphics.Bitmap
import android.graphics.drawable.Drawable
import android.os.Build
import android.view.View
import android.widget.ImageView
import java.io.InputStream

/**
 * Created by Anton Knyazev on 2019-10-03.
 */
data class ResourceImage(val resId: Int) : Image() {

    companion object {

        init {
            ImageProcessors.default.addImageProcessor(ResourceImage::class, Processor())
        }

    }

    override fun getDrawable(context: Context): Drawable? {
        return if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
            context.getDrawable(resId)
        } else {
            @Suppress("DEPRECATION")
            context.resources.getDrawable(resId)
        }
    }

    class Processor : ImageProcessor<ResourceImage>() {

        override fun ResourceImage.applyImageInner(imageView: ImageView, placeholderResId: Int) {
            imageView.setImageResource(resId)
        }

        override fun ResourceImage.applyBackgroundInner(view: View, placeholderResId: Int) {
            view.setBackgroundResource(resId)
        }

        override suspend fun ResourceImage.getStream(context: Context, compressFormat: Bitmap.CompressFormat, quality: Int): InputStream {
            return getDrawable(context)!!.toBitmapAndRecycle {
                toInputStream(compressFormat, quality)
            }
        }

        override fun ResourceImage.preload(context: Context) {
            // nothing
        }

        override fun View.getDefaultPlaceholderResId(placeholderResId: Int): Int = NO_PLACEHOLDER_RES

    }

}

fun Image.Companion.from(drawableRes: Int) = ResourceImage(drawableRes)
