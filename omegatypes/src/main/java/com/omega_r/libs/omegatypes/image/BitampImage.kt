package com.omega_r.libs.omegatypes.image

import android.content.Context
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.graphics.drawable.BitmapDrawable
import android.graphics.drawable.Drawable

/**
 * Created by Anton Knyazev on 2019-10-03.
 */
data class BitmapImage(val bitmap: Bitmap) : BaseBitmapImage() {

    companion object {

        init {
            ImagesProcessor.default.addImageProcessor(BitmapImage::class, Processor())
        }

    }

    override fun getDrawable(context: Context) = BitmapDrawable(context.resources, bitmap)

    class Processor : BaseBitmapImage.Processor<BitmapImage>(false) {

        override fun getBitmap(context: Context, image: BitmapImage, options: BitmapFactory.Options?): Bitmap? {
            val bitmap = image.bitmap
            if (options?.inJustDecodeBounds == true) {
                options.outWidth = bitmap.width
                options.outHeight = bitmap.height
            }
            return bitmap
        }

    }

}

fun Image.Companion.from(bitmap: Bitmap) = BitmapImage(bitmap)
