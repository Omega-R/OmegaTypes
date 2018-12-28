package com.omega_r.libs.omegatypes.picasso

import android.content.Context
import android.graphics.Bitmap
import android.graphics.drawable.BitmapDrawable
import android.graphics.drawable.Drawable
import android.support.v4.view.ViewCompat
import android.view.View
import android.widget.ImageView
import com.omega_r.libs.omegatypes.Image
import com.omega_r.libs.omegatypes.toBitmap
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
        ViewCompat.setBackground(view, null)
        Picasso.get()
                .load(url)
                .resize(view.width, view.height)
                .into(object : Target {

                    override fun onPrepareLoad(placeHolderDrawable: Drawable?) {
                        ViewCompat.setBackground(view, placeHolderDrawable)
                    }

                    override fun onBitmapFailed(e: Exception?, errorDrawable: Drawable?) {
                        ViewCompat.setBackground(view, errorDrawable)
                    }

                    override fun onBitmapLoaded(bitmap: Bitmap?, from: Picasso.LoadedFrom?) {
                        ViewCompat.setBackground(view, BitmapDrawable(view.resources, bitmap))
                    }

                })
    }

    override fun getStream(context: Context): InputStream {
        val stream = WrapperInputStream()

        Picasso.get()
                .load(url)
                .into(object : Target {

                    override fun onPrepareLoad(placeHolderDrawable: Drawable?) {
                        // stream can only send data once
                    }

                    override fun onBitmapFailed(e: Exception?, errorDrawable: Drawable?) {
                        if (errorDrawable != null) {
                            stream.inputStream = errorDrawable.toBitmap().toInputStream()
                        } else {
                            stream.inputStream = null
                        }

                    }

                    override fun onBitmapLoaded(bitmap: Bitmap?, from: Picasso.LoadedFrom?) {
                        if (bitmap != null) {
                            stream.inputStream = bitmap.toInputStream()
                        } else {
                            stream.inputStream = null
                        }
                    }

                })

        return stream
    }



}