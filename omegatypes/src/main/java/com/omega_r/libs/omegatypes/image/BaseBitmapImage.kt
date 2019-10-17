package com.omega_r.libs.omegatypes.image

import android.content.Context
import android.graphics.Bitmap
import android.graphics.drawable.BitmapDrawable
import android.view.View
import android.widget.ImageView
import com.omega_r.libs.omegatypes.decoders.BitmapDecoders
import com.omega_r.libs.omegatypes.tools.ImageAsyncExecutor.Companion.executeImageAsync
import com.omega_r.libs.omegatypes.tools.ImageSizeExtractor
import com.omega_r.libs.omegatypes.tools.getScaledBitmap
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import java.io.InputStream
import java.lang.ref.WeakReference
import java.util.*

/**
 * Created by Anton Knyazev on 2019-10-03.
 */
abstract class BaseBitmapImage : Image() {

    abstract class Processor<I : BaseBitmapImage>(private val autoRecycle: Boolean) : ImageProcessor<I>() {

        override fun I.applyImageInner(imageView: ImageView, placeholderResId: Int) {
            if (placeholderResId != NO_PLACEHOLDER_RES) {
                imageView.setImageResource(placeholderResId)
            } else {
                imageView.setImageDrawable(null)
            }

            val width = imageView.width
            val height = imageView.height


            if (width <= 0 || height <= 0) {
                ImageSizeExtractor(imageView) { target ->
                    applyImageInner(target, placeholderResId)
                }
            } else {
                val imageScaleType = imageView.scaleType
                executeImageAsync(imageView, extractor = { context ->
                    getBitmap(context, this, width, height)?.run {
                        getScaledBitmap(width, height, imageScaleType, autoRecycle, this)
                    }
                }, setter = ImageView::setImageBitmap)
            }
        }

        protected abstract suspend fun getBitmap(context: Context, image: I, width: Int? = null, height: Int? = null): Bitmap?

        override fun I.applyBackgroundInner(view: View, placeholderResId: Int) {
            val viewWeak = WeakReference(view)
            ImageProcessors.current.launch {
                val view1 = viewWeak.get() ?: return@launch
                val bitmap = getBitmap(view1.context, this@applyBackgroundInner, null)
                withContext(Dispatchers.Main) {
                    val view2 = viewWeak.get() ?: return@withContext
                    Image.Processor.applyBackground(view2, bitmap?.let { BitmapDrawable(view2.resources, it) })
                }
            }
        }

        override suspend fun I.getStream(context: Context, compressFormat: Bitmap.CompressFormat, quality: Int): InputStream {
            return withContext(ImageProcessors.current.coroutineContext) {
                val bitmap = getBitmap(context, this@getStream, null)

                try {
                    return@withContext bitmap
                            .toInputStream(compressFormat, quality)
                } finally {
                    if (autoRecycle && bitmap != null) {
                        BitmapDecoders.current.recycle(bitmap)
                    }
                }
            }
        }

        override fun I.preload(context: Context) {
            // nothing
        }

    }

}