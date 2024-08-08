package com.omega_r.libs.omegatypes.image

import android.content.Context
import android.graphics.Bitmap
import android.graphics.drawable.BitmapDrawable
import android.graphics.drawable.Drawable
import android.os.Handler
import android.os.Looper
import android.view.View
import android.widget.ImageView
import com.omega_r.libs.omegatypes.image.Image.Companion.NO_PLACEHOLDER_RES
import com.omega_r.libs.omegatypes.image.Image.Processor.Companion.applyEmptyBackground
import com.omega_r.libs.omegatypes.tools.ImageSizeExtractor
import com.omega_r.libs.omegatypes.tools.WrapperInputStream
import com.squareup.picasso.Picasso
import com.squareup.picasso.RequestCreator
import com.squareup.picasso.Target
import java.io.InputStream
import java.lang.ref.WeakReference
import kotlin.reflect.KClass

/**
 * Created by Anton Knyazev on 2019-10-03.
 */
class PicassoImagesProcessor(
    private val oldImagesProcessor: ImageProcessors,
    picasso: Picasso? = null,
    vararg excludeImageClasses: KClass<out Image>,
) : ImageProcessors() {

    companion object {

        fun setAsCurrentImagesProcessor(picasso: Picasso? = null) {
            current = PicassoImagesProcessor(current, picasso)
        }
    }

    private val excludeImageClasses = listOf(*excludeImageClasses)

    private val mainHandler = Handler(Looper.getMainLooper())

    private val picasso: Picasso by lazy { picasso ?: Picasso.get() }

    private fun Image.createRequestCreator(): RequestCreator? {
        if (excludeImageClasses.contains(this::class)) {
            return null
        }
        return when (this) {
            is UrlImage -> picasso.load(url)
            is JavaFileImage -> picasso.load(file)
            is UriImage -> picasso.load(uri)
            is ResourceImage -> picasso.load(resId)
            is AssetImage -> picasso.load("file:///android_asset/$fileName")
            else -> null
        }
    }

    override fun Image.applyImage(imageView: ImageView, placeholderResId: Int, onImageApplied: (() -> Unit)?) {
        createRequestCreator()?.apply {
            if (placeholderResId != NO_PLACEHOLDER_RES) placeholder(placeholderResId)
            fit()
            @Suppress("NON_EXHAUSTIVE_WHEN")
            when (imageView.scaleType) {
                ImageView.ScaleType.FIT_CENTER,
                ImageView.ScaleType.CENTER_INSIDE,
                -> centerInside()
                ImageView.ScaleType.CENTER_CROP -> centerCrop()
                else -> {
                    // nothing
                }
            }
            into(imageView)
        } ?: with(oldImagesProcessor) {
            applyImage(imageView, placeholderResId)
        }
        onImageApplied?.invoke()
    }

    override fun Image.applyBackground(view: View, placeholderResId: Int) {
        applyEmptyBackground(view, placeholderResId)

        createRequestCreator()?.apply {
            if (view.width <= 0 || view.height <= 0) {
                ImageSizeExtractor(view) {
                    applyBackground(it, placeholderResId)
                }
            } else {
                resize(view.width, view.height)
                val viewWeak = WeakReference(view)
                into(object : Target {

                    override fun onPrepareLoad(placeHolderDrawable: Drawable?) {
                        viewWeak.get()?.let {
                            Image.Processor.applyBackground(it, placeHolderDrawable)
                        }
                    }

                    override fun onBitmapFailed(e: Exception?, errorDrawable: Drawable?) {
                        viewWeak.get()?.let {
                            Image.Processor.applyBackground(it, errorDrawable)
                        }
                    }

                    override fun onBitmapLoaded(bitmap: Bitmap?, from: Picasso.LoadedFrom?) {
                        viewWeak.get()?.let {
                            Image.Processor.applyBackground(it, BitmapDrawable(view.resources, bitmap))
                        }
                    }
                })
            }
        } ?: with(oldImagesProcessor) {
            applyBackground(view, placeholderResId)
        }
    }

    override suspend fun Image.getStream(context: Context, compressFormat: Bitmap.CompressFormat, quality: Int): InputStream {
        return createRequestCreator()?.run {
            val stream = WrapperInputStream()

            val runnable = Runnable {
                into(object : Target {

                    override fun onPrepareLoad(placeHolderDrawable: Drawable?) {
                        // stream can only send data once
                    }

                    override fun onBitmapFailed(e: Exception?, errorDrawable: Drawable?) {
                        stream.inputStream = null
                    }

                    override fun onBitmapLoaded(bitmap: Bitmap?, from: Picasso.LoadedFrom?) {
                        if (bitmap != null) {
                            stream.inputStream = bitmap.toInputStream(compressFormat, quality)
                        } else {
                            stream.inputStream = null
                        }
                    }
                })
            }

            if (Looper.myLooper() != Looper.getMainLooper()) {
                mainHandler.post(runnable)
            } else {
                runnable.run()
            }

            stream
        } ?: with(oldImagesProcessor) {
            getStream(context, compressFormat, quality)
        }
    }

    override fun Image.preload(context: Context) {
        createRequestCreator()?.fetch() ?: with(oldImagesProcessor) {
            preload(context)
        }
    }
}