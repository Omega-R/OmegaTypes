package com.omega_r.libs.omegatypes.image

import android.content.Context
import android.graphics.Bitmap
import android.graphics.drawable.Drawable
import android.view.View
import android.widget.ImageView
import com.bumptech.glide.Glide
import com.bumptech.glide.RequestBuilder
import com.bumptech.glide.request.target.CustomViewTarget
import com.bumptech.glide.request.transition.Transition
import com.omega_r.libs.omegatypes.image.Image.Companion.NO_PLACEHOLDER_RES
import java.io.InputStream
import kotlin.reflect.KClass

/**
 * Created by Anton Knyazev on 2019-10-03.
 */
class GlideImagesProcessor(
        private val oldImagesProcessor: ImageProcessors,
        vararg excludeImageClasses: KClass<out Image>
) : ImageProcessors() {

    companion object {

        fun setAsCurrentImagesProcessor() {
            current = GlideImagesProcessor(current)
        }

    }

    private val excludeImageClasses = listOf(*excludeImageClasses)

    private fun <T> RequestBuilder<T>.createRequestBuilder(image: Image): RequestBuilder<T>? {
        if (excludeImageClasses.contains(image::class)) {
            return null
        }
        return when (image) {
            is UrlImage -> load(image.url)
            is UriImage -> load(image.uri)
            is JavaFileImage -> load(image.file)
            is ResourceImage -> load(image.resId)
            is BitmapImage -> load(image.bitmap)
            is DrawableImage -> load(image.drawable)
            is ByteArrayImage -> load(image.byteArray)
            else -> null
        }
    }

    override fun Image.applyImage(imageView: ImageView, placeholderResId: Int) {
        Glide.with(imageView)
                .asDrawable()
                .createRequestBuilder(this)
                ?.applyPlaceholder(placeholderResId)
                ?.into(imageView)
                ?: applyOld { applyImage(imageView, placeholderResId) }
    }

    override fun Image.applyBackground(view: View, placeholderResId: Int) {
        Image.Processor.applyEmptyBackground(view, placeholderResId)
        Glide.with(view)
                .asDrawable()
                .createRequestBuilder(this)
                ?.applyPlaceholder(placeholderResId)
                ?.into(object : CustomViewTarget<View, Drawable>(view) {

                    override fun onLoadFailed(errorDrawable: Drawable?) {
                        Image.Processor.applyBackground(view, errorDrawable)
                    }

                    override fun onResourceCleared(placeholder: Drawable?) {
                        Image.Processor.applyBackground(view, placeholder)
                    }

                    override fun onResourceReady(resource: Drawable, transition: Transition<in Drawable>?) {
                        Image.Processor.applyBackground(view, resource)
                    }

                })
                ?: applyOld { applyBackground(view, placeholderResId) }
    }

    override suspend fun Image.getStream(context: Context, compressFormat: Bitmap.CompressFormat, quality: Int): InputStream {
        return Glide.with(context)
                .asBitmap()
                .createRequestBuilder(this)
                ?.run {
                    val futureTarget = submit()
                    try {
                        val bitmap = futureTarget.get()
                        bitmap.toInputStream(compressFormat, quality)
                    } finally {
                        Glide.with(context)
                                .clear(futureTarget)
                    }
                } ?: applyOld { getStream(context, compressFormat, quality) }
    }

    override fun Image.preload(context: Context) {
        Glide.with(context)
                .asDrawable()
                .createRequestBuilder(this)
                ?.preload()
                ?: applyOld { preload(context) }
    }

    private fun <T> RequestBuilder<T>.applyPlaceholder(placeholderResId: Int): RequestBuilder<T> {
        return if (placeholderResId != NO_PLACEHOLDER_RES) placeholder(placeholderResId) else this
    }

    private inline fun <R> applyOld(block: ImageProcessors.() -> R): R {
        return block(oldImagesProcessor)
    }
}