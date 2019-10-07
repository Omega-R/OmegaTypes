package com.omega_r.libs.omegatypes.image

import android.content.Context
import android.graphics.Bitmap
import android.view.View
import android.widget.ImageView
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import java.io.InputStream
import kotlin.coroutines.CoroutineContext
import kotlin.reflect.KClass

/**
 * Created by Anton Knyazev on 2019-10-03.
 */
abstract class ImageProcessors : CoroutineScope {

    companion object {

        val default = Default()

        var current: ImageProcessors = default

    }

    override val coroutineContext: CoroutineContext
        get() = Dispatchers.Default

    abstract fun Image.applyImage(imageView: ImageView, placeholderResId: Int)

    abstract fun Image.applyBackground(view: View, placeholderResId: Int)

    abstract suspend fun Image.getStream(context: Context, compressFormat: Bitmap.CompressFormat, quality: Int): InputStream

    abstract fun Image.preload(context: Context)

    class Default : ImageProcessors() {

        private val map: MutableMap<KClass<out Image>, ImageProcessor<*>> = mutableMapOf()

        fun <I : Image> addImageProcessor(imageClass: KClass<I>, imageProcessor: ImageProcessor<I>) {
            map[imageClass] = imageProcessor
        }

        override fun Image.applyImage(imageView: ImageView, placeholderResId: Int) = with(getImageProcessor()) {
            applyImage(imageView, placeholderResId)
        }

        override fun Image.applyBackground(view: View, placeholderResId: Int) = with(getImageProcessor()) {
            applyBackground(view, placeholderResId)
        }

        override suspend fun Image.getStream(
                context: Context,
                compressFormat: Bitmap.CompressFormat,
                quality: Int
        ): InputStream = withContext(coroutineContext) {
            val processor = getImageProcessor()
            with(processor) {
                getStream(context, compressFormat, quality)
            }
        }

        override fun Image.preload(context: Context) {
            with(getImageProcessor()) {
                preload(context)
            }
        }

        private fun Image.getImageProcessor(): ImageProcessor<Image> {
            @Suppress("UNCHECKED_CAST")
            return map[this::class] as? ImageProcessor<Image>
                    ?: default.map[this::class] as? ImageProcessor<Image>
                    ?: throw IllegalArgumentException("ImageProcessor not found for ${this::class}")
        }
    }

}