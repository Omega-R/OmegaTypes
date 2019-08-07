package com.omega_r.libs.omegatypes.glide

import android.content.Context
import android.graphics.Bitmap
import android.graphics.drawable.Drawable
import android.view.View
import android.widget.ImageView
import com.bumptech.glide.Glide
import com.bumptech.glide.request.target.CustomTarget
import com.bumptech.glide.request.target.CustomViewTarget
import com.bumptech.glide.request.transition.Transition
import com.omega_r.libs.omegatypes.Image
import com.omega_r.libs.omegatypes.UrlImage
import com.omega_r.libs.omegatypes.toInputStream
import java.io.InputStream

/**
 * Created by Anton Knyazev on 15.04.19.
 */


class GlideImage(override val url: String) : Image(), UrlImage {

    override fun preload(context: Context) {
        Glide.with(context)
                .load(url)
                .preload()
    }

    override fun applyImage(imageView: ImageView, placeholderResId: Int) {
        Glide.with(imageView)
                .load(url)
                .apply {
                    val newPlaceholderResId = getDefaultPlaceholderResId(imageView.context, placeholderResId)

                    if (newPlaceholderResId != 0) placeholder(newPlaceholderResId)
                    into(imageView)
                }
    }

    override fun applyBackground(view: View, placeholderResId: Int) {
        super.applyBackground(view, placeholderResId)

        Glide.with(view)
                .load(url)
                .apply {
                    val newPlaceholderResId = getDefaultPlaceholderResId(view.context, placeholderResId)

                    if (newPlaceholderResId != 0) placeholder(newPlaceholderResId)

                    into(object : CustomViewTarget<View, Drawable>(view) {
                        override fun onLoadFailed(errorDrawable: Drawable?) {
                            applyBackground(view, errorDrawable)
                        }

                        override fun onResourceCleared(placeholder: Drawable?) {
                            applyBackground(view, placeholder)
                        }

                        override fun onResourceReady(resource: Drawable, transition: Transition<in Drawable>?) {
                            applyBackground(view, resource)
                        }

                    })
                }
    }

    override fun getStream(context: Context, compressFormat: Bitmap.CompressFormat, quality: Int): InputStream {
        val stream = WrapperInputStream()

        Glide.with(context)
                .asBitmap()
                .load(url)
                .into(object : CustomTarget<Bitmap>() {
                    override fun onLoadCleared(placeholder: Drawable?) {
                        // stream can only send data once
                    }

                    override fun onResourceReady(resource: Bitmap, transition: Transition<in Bitmap>?) {
                        stream.inputStream = resource.toInputStream(compressFormat, quality)
                    }

                    override fun onLoadFailed(errorDrawable: Drawable?) {
                        stream.inputStream = null
                    }

                })


        return stream
    }

}

