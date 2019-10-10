package com.omega_r.libs.omegatypes.image

import android.content.Context
import android.graphics.Bitmap
import com.omega_r.libs.omegatypes.decoders.toBitmap
import java.io.IOException
import java.net.HttpURLConnection
import java.net.URL


/**
 * Created by Anton Knyazev on 2019-10-03.
 */
data class UrlImage(val url: String) : BaseBitmapImage() {

    companion object {

        init {
            ImageProcessors.default.addImageProcessor(UrlImage::class, Processor())
        }

    }

    class Processor : BaseBitmapImage.Processor<UrlImage>(true) {


        override suspend fun getBitmap(context: Context, image: UrlImage, width: Int?, height: Int?): Bitmap? {

            return try {
                val bytes = image.getBytes().inputStream()
                bytes.toBitmap(width, height)
            } catch (e: IOException) {
                null
            }
        }


        private fun UrlImage.getBytes(): ByteArray {
            var connection: HttpURLConnection? = null
            try {
                connection = URL(url).openConnection() as HttpURLConnection
                connection.doInput = true;
                connection.connect()
                val input = connection.inputStream
                return input.readBytes()
            } finally {
                connection?.disconnect()
            }
        }


    }


}

fun Image.Companion.from(url: String) = UrlImage(url)
