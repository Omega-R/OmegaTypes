package com.omega_r.libs.omegatypes

import android.content.Context
import android.graphics.Bitmap
import android.graphics.drawable.BitmapDrawable
import android.graphics.drawable.Drawable
import android.os.Build
import android.support.annotation.DrawableRes
import android.support.annotation.RequiresApi
import android.support.v4.content.ContextCompat
import android.view.View
import android.widget.ImageView
import java.io.Serializable

interface Image : Serializable {

    fun getDrawable(context: Context): Drawable?

    companion object {

        fun from(@DrawableRes stringRes: Int): Image = ResourceImage(stringRes)

        fun from(drawable: Drawable): Image = DrawableImage(drawable)

        fun from(bitmap: Bitmap): Image = BitmapImage(bitmap)
    }

    class ResourceImage(@DrawableRes private val resId: Int) : Image {

        override fun getDrawable(context: Context): Drawable? =
                ContextCompat.getDrawable(context, resId)

    }

    class DrawableImage(private val drawable: Drawable) : Image {

        override fun getDrawable(context: Context): Drawable = drawable

    }

    class BitmapImage(private val bitmap: Bitmap) : Image {

        override fun getDrawable(context: Context): Drawable =
                BitmapDrawable(context.resources, bitmap)

    }

}

fun ImageView.setImage(image: Image) {
    setImageDrawable(image.getDrawable(this.context))
}

@RequiresApi(Build.VERSION_CODES.JELLY_BEAN)
fun View.setBackground(image: Image) {
    background = image.getDrawable(context)
}

fun Image.applyTo(imageView: ImageView) {
    imageView.setImage(this)
}

@RequiresApi(Build.VERSION_CODES.JELLY_BEAN)
fun Image.applyBackgroundTo(view: View) {
    view.setBackground(this)
}