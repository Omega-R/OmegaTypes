package com.omega_r.libs.omegatypes.image

import android.content.Context
import android.graphics.Bitmap
import android.graphics.drawable.Drawable
import android.util.Log
import android.view.View
import android.widget.ImageView
import com.bumptech.glide.Glide
import com.bumptech.glide.RequestBuilder
import com.bumptech.glide.load.DataSource
import com.bumptech.glide.load.engine.GlideException
import com.bumptech.glide.load.resource.drawable.DrawableTransitionOptions
import com.bumptech.glide.request.RequestListener
import com.bumptech.glide.request.target.CustomViewTarget
import com.bumptech.glide.request.target.Target
import com.bumptech.glide.request.transition.Transition
import com.omega_r.libs.omegatypes.decoders.BitmapDecoders
import com.omega_r.libs.omegatypes.decoders.SimpleBitmapDecoders
import com.omega_r.libs.omegatypes.image.Image.Companion.NO_PLACEHOLDER_RES
import java.io.InputStream
import kotlin.reflect.KClass
import kotlin.time.Duration
import kotlin.time.Duration.Companion.milliseconds
import kotlin.time.DurationUnit

/**
 * Created by Anton Knyazev on 2019-10-03.
 */
open class GlideImagesProcessor(
    protected val oldImagesProcessor: ImageProcessors,
    vararg excludeImageClasses: KClass<out Image>,
    private val customBuilder: CustomRequestBuilder? = null,
    private val fadeDuration: Duration? = 500.milliseconds
) : ImageProcessors() {

    companion object {

        fun setAsCurrentImagesProcessor(customRequestBuilder: CustomRequestBuilder? = null) {
            current = GlideImagesProcessor(current, customBuilder = customRequestBuilder)
        }

        fun setGlideBitmapPool(context: Context) {
            BitmapDecoders.current = SimpleBitmapDecoders(GlideBitmapPool(Glide.get(context).bitmapPool))
        }
    }

    private val excludeImageClasses = listOf(*excludeImageClasses)

    protected fun <T> RequestBuilder<T>.createRequestBuilder(image: Image): RequestBuilder<T>? {
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
            is AssetImage -> load("file:///android_asset/" + image.fileName)
            else -> if (customBuilder?.handles(image) == true) customBuilder.createRequestBuilder(this, image) else null
        }
    }

    override fun Image.applyImage(imageView: ImageView, placeholderResId: Int, onImageApplied: (() -> Unit)?) {
        Glide.with(imageView)
            .asDrawable()
            .createRequestBuilder(this)
            ?.applyPlaceholder(placeholderResId)
            ?.addListener(GlideImageRequestListener(onImageApplied))
            ?.run { fadeDuration?.let { transition(DrawableTransitionOptions.withCrossFade(it.toInt(DurationUnit.MILLISECONDS)))}}
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

    protected fun <T> RequestBuilder<T>.applyPlaceholder(placeholderResId: Int): RequestBuilder<T> {
        return if (placeholderResId != NO_PLACEHOLDER_RES) placeholder(placeholderResId) else this
    }

    protected inline fun <R> applyOld(block: ImageProcessors.() -> R): R {
        return block(oldImagesProcessor)
    }

    interface CustomRequestBuilder {

        fun handles(image: Image): Boolean

        fun <T> createRequestBuilder(builder: RequestBuilder<T>, image: Image): RequestBuilder<T>?
    }
}

class GlideImageRequestListener(private val onImageLoaded: (() -> Unit)?) : RequestListener<Drawable> {

    companion object {

        private val TAG = GlideImageRequestListener::class.java.name
    }

    override fun onLoadFailed(
        e: GlideException?,
        model: Any?,
        target: Target<Drawable>?,
        isFirstResource: Boolean,
    ): Boolean {
        Log.e(TAG, "Image load failed: ", e)
        if (isFirstResource) {
            onImageLoaded?.invoke()
        }
        return false
    }

    override fun onResourceReady(
        resource: Drawable,
        model: Any?,
        target: Target<Drawable>,
        dataSource: DataSource,
        isFirstResource: Boolean,
    ): Boolean {
        if (isFirstResource) {
            onImageLoaded?.invoke()
        }
        return false
    }
}