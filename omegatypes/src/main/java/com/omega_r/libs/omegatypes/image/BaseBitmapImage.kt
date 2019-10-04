package com.omega_r.libs.omegatypes.image

import android.content.Context
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.graphics.drawable.BitmapDrawable
import android.view.View
import android.widget.ImageView
import com.omega_r.libs.omegatypes.tools.ImageAsyncExecutor.Companion.executeImageAsync
import com.omega_r.libs.omegatypes.tools.ImageSizeExtractor
import com.omega_r.libs.omegatypes.tools.stripeBitmapExtractor
import java.io.InputStream

/**
 * Created by Anton Knyazev on 2019-10-03.
 */
abstract class BaseBitmapImage : Image() {

    abstract class Processor<I : BaseBitmapImage>(private val autoRecycle: Boolean) : ImageProcessor<I>() {

        override fun I.applyImageInner(imageView: ImageView, placeholderResId: Int) {
            val width = imageView.width
            val height = imageView.height

            if (placeholderResId != NO_PLACEHOLDER_RES) {
                imageView.setImageResource(placeholderResId)
            }

            if (width <= 0 || height <= 0) {
                ImageSizeExtractor(imageView) { target ->
                    applyImageInner(target, placeholderResId)
                }
            } else {
                val imageScaleType = imageView.scaleType
                executeImageAsync(imageView) { context ->
                    stripeBitmapExtractor(width, height, imageScaleType, autoRecycle) {
                        getBitmap(context, this, it)
                    }
                }
            }
        }

        protected abstract fun getBitmap(context: Context, image: I, options: BitmapFactory.Options?): Bitmap?

        override fun I.applyBackgroundInner(view: View, placeholderResId: Int) {
            Image.Processor.applyBackground(view, BitmapDrawable(view.resources, getBitmap(view.context, this, null)))
        }

        override fun I.getStream(context: Context, compressFormat: Bitmap.CompressFormat, quality: Int): InputStream {
            return getBitmap(context, this, null)
                    .toInputStream(compressFormat, quality)
        }

        override fun I.preload(context: Context) {
            // nothing
        }

    }

}