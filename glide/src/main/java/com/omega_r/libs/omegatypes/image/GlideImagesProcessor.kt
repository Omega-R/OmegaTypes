package com.omega_r.libs.omegatypes.image

import android.content.Context
import android.graphics.Bitmap
import android.graphics.drawable.Drawable
import android.view.View
import android.widget.ImageView
import com.bumptech.glide.Glide
import com.bumptech.glide.RequestBuilder
import com.bumptech.glide.RequestManager
import com.bumptech.glide.request.target.CustomTarget
import com.bumptech.glide.request.target.CustomViewTarget
import com.bumptech.glide.request.transition.Transition
import com.omega_r.libs.omegatypes.glide.WrapperInputStream
import java.io.InputStream
import kotlin.reflect.KClass

/**
 * Created by Anton Knyazev on 2019-10-03.
 */
class GlideImagesProcessor(
        private val oldImagesProcessor: ImagesProcessor,
        vararg excludeImageClasses: KClass<out Image>
) : ImagesProcessor() {

    companion object {

        fun setAsCurrentImagesProcessor() {
            current = GlideImagesProcessor(current)
        }

    }

    private val excludeImageClasses = listOf(*excludeImageClasses)

    private fun RequestManager.createRequestBuilder(image: Image): RequestBuilder<Drawable>? {
        if (excludeImageClasses.contains(image::class)) {
            return null
        }
        return when (image) {
            is UrlImage -> load(image.url)
            is UriImage -> load(image.uri)
            is FileImage -> load(image.file)
            is ResourceImage -> load(image.resId)
            is BitmapImage -> load(image.bitmap)
            is DrawableImage -> load(image.drawable)
            is ByteArrayImage -> load(image.byteArray)
            else -> null
        }
    }

    override fun Image.applyImage(imageView: ImageView, placeholderResId: Int) {
        Glide.with(imageView)
                .createRequestBuilder(this)
                ?.apply {
                    if (placeholderResId != Image.NO_PLACEHOLDER_RES) placeholder(placeholderResId)
                    into(imageView)
                } ?: with(oldImagesProcessor) {
            applyImage(imageView, placeholderResId)
        }
    }

    override fun Image.applyBackground(view: View, placeholderResId: Int) {
        Image.Processor.applyEmptyBackground(view, placeholderResId)
        Glide.with(view)
                .createRequestBuilder(this)
                ?.apply {

                    if (placeholderResId != Image.NO_PLACEHOLDER_RES) placeholder(placeholderResId)

                    into(object : CustomViewTarget<View, Drawable>(view) {
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

                } ?: with(oldImagesProcessor) {
            applyBackground(view, placeholderResId)
        }
    }

    override fun Image.getStream(context: Context, compressFormat: Bitmap.CompressFormat, quality: Int): InputStream {
        return Glide.with(context)
                .createRequestBuilder(this)
                ?.run {
                    val stream = WrapperInputStream()

                    into(object : CustomTarget<Drawable>() {
                        override fun onLoadCleared(placeholder: Drawable?) {
                            // nothing
                        }

                        override fun onResourceReady(resource: Drawable, transition: Transition<in Drawable>?) {
                            stream.inputStream = Image.Processor.toBitmap(resource) {
                                toInputStream(compressFormat, quality)
                            }
                        }

                    })
                    stream

                } ?: with(oldImagesProcessor) {
            getStream(context, compressFormat, quality)
        }

    }

    override fun Image.preload(context: Context) {
        Glide.with(context)
                .createRequestBuilder(this)
                ?.apply {
                    preload()
                } ?: with(oldImagesProcessor) {
            preload(context)
        }
    }

}