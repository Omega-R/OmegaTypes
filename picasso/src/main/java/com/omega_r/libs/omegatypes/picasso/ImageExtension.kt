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
import com.omega_r.libs.omegatypes.toInputStream
import com.squareup.picasso.Picasso
import com.squareup.picasso.Target
import java.io.InputStream

/**
 * Created by Anton Knyazev on 28.12.18.
 */

fun Image.Companion.from(url: String) = PicassoImage(url)

class PicassoImage(private val url: String) : Image() {

    override fun applyImage(imageView: ImageView) {
        Picasso.get()
                .load(url)
                .fit()
                .centerCrop()
                .into(imageView)
    }

    override fun applyBackground(view: View) {
        applyBackground(view, null)

        val requestCreator = Picasso.get().load(url)

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

}