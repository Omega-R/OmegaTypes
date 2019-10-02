package com.omega_r.libs.omegatypes.picasso

import android.content.Context
import android.graphics.Bitmap
import android.graphics.drawable.BitmapDrawable
import android.graphics.drawable.Drawable
import android.os.Handler
import android.os.Looper
import android.view.View
import android.widget.ImageView
import com.omega_r.libs.omegatypes.Image
import com.omega_r.libs.omegatypes.UrlImage
import com.omega_r.libs.omegatypes.toInputStream
import com.squareup.picasso.Picasso
import com.squareup.picasso.Target
import java.io.InputStream

/**
 * Created by Anton Knyazev on 28.12.18.
 */


class PicassoImage(override val url: String) : Image(), UrlImage {

    override fun preload(context: Context) {
        Picasso.get()
                .load(url)
                .fetch()
    }

    override fun applyImage(imageView: ImageView, placeholderResId: Int) {
        val newPlaceholderResId = getDefaultPlaceholderResId(imageView.context, placeholderResId)
        Picasso.get()
                .load(url)
                .apply {
                    if (newPlaceholderResId != 0) placeholder(newPlaceholderResId)
                    fit()
                    when (imageView.scaleType) {
                        ImageView.ScaleType.FIT_CENTER,
                        ImageView.ScaleType.CENTER_INSIDE -> centerInside()
                        ImageView.ScaleType.CENTER_CROP -> centerCrop()
                    }
                    into(imageView)
                }
    }

    override fun applyBackground(view: View, placeholderResId: Int) {
        val newPlaceholderResId = getDefaultPlaceholderResId(view.context, placeholderResId)
        super.applyBackground(view, newPlaceholderResId)

        val requestCreator = Picasso.get().load(url)

        if (newPlaceholderResId != 0) {
            requestCreator.placeholder(newPlaceholderResId)
        }

        if (view.width > 0 && view.height > 0) {
            requestCreator.resize(view.width, view.height)
        }

        requestCreator.into(object : Target {

            override fun onPrepareLoad(placeHolderDrawable: Drawable?) {
                applyBackground(view, placeHolderDrawable)
            }

            override fun onBitmapFailed(e: Exception?, errorDrawable: Drawable?) {
                applyBackground(view, errorDrawable)
            }

            override fun onBitmapLoaded(bitmap: Bitmap?, from: Picasso.LoadedFrom?) {
                applyBackground(view, BitmapDrawable(view.resources, bitmap))
            }

        })
    }

    override fun getStream(context: Context, compressFormat: Bitmap.CompressFormat, quality: Int): InputStream {
        val stream = WrapperInputStream()


        val runnable = Runnable {
            Picasso.get()
                    .load(url)
                    .into(object : Target {

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
            Handler(Looper.getMainLooper()).post(runnable)
        } else {
            runnable.run()
        }

        return stream
    }

    override fun equals(other: Any?): Boolean {
        if (this === other) return true
        if (javaClass != other?.javaClass) return false

        other as PicassoImage

        if (url != other.url) return false

        return true
    }

    override fun hashCode(): Int {
        return url.hashCode()
    }

}
