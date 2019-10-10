package com.omega_r.libs.omegatypes.image

import android.content.Context
import android.graphics.Bitmap
import android.util.Base64
import com.omega_r.libs.omegatypes.decoders.toBitmap

/**
 * Created by Anton Knyazev on 2019-10-03.
 */
data class Base64Image(val base64String: String, val flags: Int) : BaseBitmapImage() {

    companion object {

        init {
            ImageProcessors.default.addImageProcessor(Base64Image::class, Processor())
        }

    }

    class Processor : BaseBitmapImage.Processor<Base64Image>(true) {

        override suspend fun getBitmap(context: Context, image: Base64Image, width: Int?, height: Int?): Bitmap? {
            val base64String = image.base64String
            val position = base64String.indexOf(",")
            val data = if (position != -1) base64String.substring(position + 1) else base64String
            val byteArray = Base64.decode(data, image.flags)

            return byteArray.toBitmap(width, height)
        }

    }

}

fun Image.Companion.from(base64String: String, flags: Int) = Base64Image(base64String, flags)
